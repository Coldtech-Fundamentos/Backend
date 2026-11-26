import argparse
import json
import os
from pathlib import Path
import secrets
import shutil
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request


ROOT = Path(__file__).resolve().parents[1]


def free_port():
    with socket.socket() as listener:
        listener.bind(("127.0.0.1", 0))
        return listener.getsockname()[1]


def request(port, path, method="GET", data=None, token=None):
    headers = {}
    body = None
    if data is not None:
        body = json.dumps(data).encode()
        headers["Content-Type"] = "application/json"
    if token is not None:
        headers["Authorization"] = "Bearer " + token
    outgoing = urllib.request.Request(
        f"http://127.0.0.1:{port}{path}", body, headers, method=method
    )
    try:
        response = urllib.request.urlopen(outgoing, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        content = response.read()
        try:
            result = json.loads(content) if content else None
        except ValueError:
            result = None
        return response.status, result


def expect(status, result):
    assert result[0] == status, f"Expected HTTP {status}, got {result[0]}"
    return result[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--java", default="java")
    options = parser.parse_args()
    h2_jars = sorted((Path.home() / ".m2/repository/com/h2database/h2").glob("*/*.jar"))
    assert h2_jars, "Run IAM Maven tests first to resolve the test-only H2 driver"
    h2 = h2_jars[-1]
    jars = {
        "iam": ROOT / "services/iam-service/target/iam-service-0.0.1-SNAPSHOT.jar",
        "reporting": ROOT / "services/reporting-service/target/reporting-service-0.0.1-SNAPSHOT.jar",
        "gateway": ROOT / "services/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar",
        "monolith": ROOT / "target/frigora-platform-0.0.1-SNAPSHOT.jar",
    }
    assert all(jar.exists() for jar in jars.values()), "Package all four applications first"
    ports = {name: free_port() for name in jars}
    processes = []
    secret = secrets.token_urlsafe(64)
    shared = {
        "LOADER_PATH": str(h2),
        "SPRING_DATASOURCE_DRIVER_CLASS_NAME": "org.h2.Driver",
        "SPRING_DATASOURCE_USERNAME": "sa",
        "SPRING_DATASOURCE_PASSWORD": "",
        "SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT": "org.hibernate.dialect.H2Dialect",
        "SPRING_JPA_HIBERNATE_DDL_AUTO": "validate",
        "AUTHORIZATION_JWT_SECRET": secret,
        "IAM_JWT_SECRET": secret,
        "REPORTING_JWT_SECRET": secret,
        "SPRINGDOC_API_DOCS_ENABLED": "false",
        "SPRINGDOC_SWAGGER_UI_ENABLED": "false",
    }

    with tempfile.TemporaryDirectory(prefix="frigora-iam-test-") as logs:
        def start(name, integrated=True):
            environment = os.environ.copy()
            environment.update(shared)
            environment.update({
                "SERVER_PORT": str(ports[name]),
                "SPRING_DATASOURCE_URL":
                    f"jdbc:h2:mem:{name};MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "IAM_SERVICE_URL": f"http://127.0.0.1:{ports['iam']}",
                "MONOLITH_URL": f"http://127.0.0.1:{ports['monolith']}",
                "REPORTING_SERVICE_URL": f"http://127.0.0.1:{ports['reporting']}",
                "MONOLITH_JWT_IDENTITY_ENABLED": str(integrated).lower(),
            })
            if name == "monolith":
                environment["SPRING_JPA_HIBERNATE_DDL_AUTO"] = "create-drop"
            log = open(Path(logs) / f"{name}.log", "wb")
            process = subprocess.Popen([
                options.java, "-cp", str(jars[name]),
                "org.springframework.boot.loader.launch.PropertiesLauncher",
            ], cwd=ROOT, env=environment, stdout=log, stderr=subprocess.STDOUT)
            processes.append((process, log))
            deadline = time.monotonic() + 180
            while time.monotonic() < deadline:
                if process.poll() is not None:
                    raise AssertionError(f"{name} exited during startup; local log: {log.name}")
                try:
                    with socket.create_connection(("127.0.0.1", ports[name]), timeout=1):
                        return process
                except OSError:
                    time.sleep(0.25)
            raise AssertionError(f"{name} startup timed out")

        def register(port, username, role="Owner"):
            expect(200, request(port, "/api/v1/authentication/sign-up", "POST", {
                "username": username, "password": "synthetic-password", "roles": role,
            }))
            authenticated = expect(200, request(port, "/api/v1/authentication/sign-in", "POST", {
                "username": username, "password": "synthetic-password",
            }))
            return authenticated["token"], authenticated["id"]

        try:
            start("iam")
            start("reporting")
            monolith = start("monolith")
            start("gateway")
            gateway = ports["gateway"]
            owner, owner_id = register(gateway, "iam-only-owner")
            other, other_id = register(gateway, "iam-only-other")
            provider, _ = register(gateway, "iam-only-provider", "Provider")
            monolith_users = expect(200, request(ports["monolith"], "/api/v1/users", token=owner))
            assert monolith_users == [], "IAM users must not be duplicated in the monolith"
            site = expect(201, request(gateway, "/api/v1/sites", "POST", {
                "name": "Synthetic site", "address": "Test address",
                "contactName": "Synthetic contact", "phone": "123456789",
            }, owner))
            own_sites = expect(200, request(gateway, "/api/v1/sites", token=owner))
            assert any(row["id"] == site["id"] for row in own_sites)
            assert expect(200, request(gateway, "/api/v1/sites", token=other)) == []
            report = expect(201, request(gateway, "/api/v1/reports", "POST", {
                "type": "Inspection", "equipmentId": 50, "title": "Synthetic inspection",
                "summary": "Summary", "content": "Synthetic content",
                "url": "https://example.test/report",
            }, owner))
            expect(200, request(gateway, f"/api/v1/reports/{report['id']}", token=owner))
            expect(404, request(gateway, f"/api/v1/reports/{report['id']}", token=other))
            assert expect(200, request(gateway, "/api/v1/reports", token=other)) == []
            for path in ("/api/v1/sites", "/api/v1/reports"):
                expect(401, request(gateway, path, token="invalid"))
            expect(200, request(gateway, "/api/v1/equipment", token=owner))
            expect(200, request(gateway, "/api/v1/alert", token=owner))
            monolith.terminate()
            monolith.wait(timeout=30)
            start("monolith", integrated=False)
            expect(401, request(gateway, "/api/v1/sites", token=owner))
            legacy, _ = register(ports["monolith"], "legacy-only-owner")
            expect(200, request(gateway, "/api/v1/sites", token=legacy))
            print("PASS: Gateway -> IAM registration/login -> Reporting and monolith; "
                  "owner-scoped lists, invalid JWT 401, empty monolith users and legacy mode.")
            print("Scope: four real HTTP applications with separate in-memory H2 databases; "
                  "no MySQL certification or production role-policy assertion.")
        except BaseException:
            diagnostics = ROOT / "target/iam-integration"
            diagnostics.mkdir(parents=True, exist_ok=True)
            for process, log in processes:
                log.flush()
                shutil.copyfile(log.name, diagnostics / Path(log.name).name)
            print(f"Local diagnostics: {diagnostics}")
            raise
        finally:
            for process, log in reversed(processes):
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=30)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait(timeout=10)
                log.close()


if __name__ == "__main__":
    main()
