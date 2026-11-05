# Frigora Platform Backend

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.0.6](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2025.1.4](https://img.shields.io/badge/Spring_Cloud-2025.1.4-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)

Backend for Frigora, a refrigeration monitoring and maintenance platform. This repository contains the original Spring Boot monolith, an independent Reporting Service, and an API Gateway. The migration to microservices is in progress.

## Run locally

Install JDK 21 and Git. Then clone the repository and run the Gateway from the application directory:

~~~powershell
git clone --branch develop https://github.com/Coldtech-Fundamentos/Backend.git Backend
cd Backend\Frigora-Platform-main
.\mvnw.cmd -f services\api-gateway\pom.xml spring-boot:run
~~~

The Gateway listens on `http://localhost:8080`. It routes `/api/v1/reports/**` to Reporting Service on port 8085 and other `/api/**` requests to the monolith on port 8081. The upstream services must be running to process requests.

Before starting the monolith, configure an authorized local test database. Its current configuration uses `ddl-auto=update`; do not run it against a remote database or real data. Reporting Service requires its own MySQL database and environment variables.

## Repository structure

~~~text
Backend/
├── README.md
├── CONTRIBUTING.md
├── AGENTS.md
└── Frigora-Platform-main/
    ├── pom.xml
    └── services/
        ├── api-gateway/
        └── reporting-service/
~~~

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, architecture boundaries, Git Flow, commit messages, tests, and pull request guidance.
