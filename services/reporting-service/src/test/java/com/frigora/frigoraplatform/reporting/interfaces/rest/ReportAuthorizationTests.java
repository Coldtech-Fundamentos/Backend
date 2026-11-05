package com.frigora.frigoraplatform.reporting.interfaces.rest;

import com.frigora.frigoraplatform.reporting.application.internal.commandservices.ReportCommandServiceImpl;
import com.frigora.frigoraplatform.reporting.application.internal.queryservices.ReportQueryServiceImpl;
import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.queries.GetAllReportsQuery;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;
import com.frigora.frigoraplatform.reporting.domain.repositories.ReportRepository;
import com.frigora.frigoraplatform.shared.infrastructure.security.AuthenticatedIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real controller/services with a mocked persistence boundary; no Spring context or database. */
class ReportAuthorizationTests {
    private static final String BASE = "/api/v1/reports";
    private static final String CREATE = """
            {"type":"Inspection","equipmentId":50,"title":"Inspection",
             "summary":"Summary","content":"Content","url":"https://example.test/report"}
            """;
    private static final String UPDATE = """
            {"equipmentId":51,"title":"Updated","status":"Completed",
             "summary":"Updated summary","content":"Updated content","url":"https://example.test/updated"}
            """;
    private ReportRepository repository;
    private ReportQueryServiceImpl queries;
    private MockMvc mvc;
    private Map<Integer, Report> reports;

    @BeforeEach
    void setUp() {
        reports = new LinkedHashMap<>();
        reports.put(1, report(1, 101));
        reports.put(2, report(2, 202));
        repository = mock(ReportRepository.class);
        when(repository.findByIdAndTenantId(anyInt(), anyInt())).thenAnswer(invocation ->
                Optional.ofNullable(reports.get(invocation.getArgument(0)))
                        .filter(report -> report.getTenantId().equals(invocation.getArgument(1))));
        when(repository.findAllByTenantId(anyInt())).thenAnswer(invocation -> reports.values().stream()
                .filter(report -> report.getTenantId().equals(invocation.getArgument(0))).toList());
        when(repository.findAllByEquipmentIdAndTenantId(anyInt(), anyInt())).thenAnswer(invocation ->
                reports.values().stream().filter(report -> report.getEquipmentId().equals(invocation.getArgument(0))
                        && report.getTenantId().equals(invocation.getArgument(1))).toList());
        when(repository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            if (report.getId() == null) ReflectionTestUtils.setField(report, "id", reports.size() + 1);
            reports.put(report.getId(), report);
            return report;
        });
        doAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            reports.remove(report.getId());
            return null;
        }).when(repository).delete(any(Report.class));
        queries = new ReportQueryServiceImpl(repository);
        mvc = MockMvcBuilders.standaloneSetup(new ReportController(
                new ReportCommandServiceImpl(repository), queries)).build();
    }

    private Report report(int id, int owner) {
        var report = new Report(owner, EReportType.INSPECTION, 50, "Inspection",
                EReportStatus.IN_PROGRESS, "Summary", "Content", "https://example.test/report");
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }

    private Authentication authentication(Integer id) {
        var identity = mock(AuthenticatedIdentity.class);
        when(identity.getUserId()).thenReturn(id);
        when(identity.getUsername()).thenReturn("user-" + id);
        return new UsernamePasswordAuthenticationToken(identity, null, List.of());
    }

    @Test
    void createsAndReadsOwnReportsWithExistingJsonContract() throws Exception {
        for (int owner : List.of(101, 202)) {
            var auth = authentication(owner);
            var result = mvc.perform(post(BASE).principal(auth).contentType(MediaType.APPLICATION_JSON).content(CREATE))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.length()").value(9))
                    .andExpect(jsonPath("$.tenantId").value(owner))
                    .andExpect(jsonPath("$.type").value("Inspection"))
                    .andExpect(jsonPath("$.status").value("InProgress"))
                    .andExpect(jsonPath("$.equipmentId").value(50))
                    .andExpect(jsonPath("$.title").value("Inspection"))
                    .andExpect(jsonPath("$.summary").value("Summary"))
                    .andExpect(jsonPath("$.content").value("Content"))
                    .andExpect(jsonPath("$.url").value("https://example.test/report")).andReturn();
            int id = reports.size();
            mvc.perform(get(BASE + "/" + id).principal(auth)).andExpect(status().isOk())
                    .andExpect(content().json(result.getResponse().getContentAsString()));
        }
    }

    @Test
    void listsOnlyOwnedReportsIncludingWhenFilteringBySharedEquipment() throws Exception {
        for (int owner : List.of(101, 202)) {
            for (var request : List.of(get(BASE), get(BASE).param("equipmentId", "50"))) {
                mvc.perform(request.principal(authentication(owner))).andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].tenantId").value(owner));
            }
            mvc.perform(get(BASE).param("equipmentId", "999").principal(authentication(owner)))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
        }
    }

    @Test
    void hidesForeignAndMissingReportsForReadsUpdatesAndDeletes() throws Exception {
        for (int owner : List.of(101, 202)) {
            int foreignId = owner == 101 ? 2 : 1;
            for (int id : List.of(foreignId, 999)) {
                for (var request : List.of(get(BASE + "/" + id),
                        put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(UPDATE),
                        delete(BASE + "/" + id))) {
                    mvc.perform(request.principal(authentication(owner))).andExpect(status().isNotFound())
                            .andExpect(content().string(""));
                }
            }
        }
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
        assertThat(reports).hasSize(2);
        assertThat(reports.get(1).getTitle()).isEqualTo("Inspection");
        assertThat(reports.get(2).getTitle()).isEqualTo("Inspection");
    }

    @Test
    void ownerCanUpdateWithoutChangingOwnershipAndDeleteWithExistingSuccessResponses() throws Exception {
        for (int id : List.of(1, 2)) {
            int owner = reports.get(id).getTenantId();
            var auth = authentication(owner);
            mvc.perform(put(BASE + "/" + id).principal(auth).contentType(MediaType.APPLICATION_JSON).content(UPDATE))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(9))
                    .andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.tenantId").value(owner))
                    .andExpect(jsonPath("$.title").value("Updated"))
                    .andExpect(jsonPath("$.status").value("Completed"))
                    .andExpect(jsonPath("$.type").value("Inspection"));
            assertThat(reports.get(id).getTenantId()).isEqualTo(owner);
            mvc.perform(delete(BASE + "/" + id).principal(auth)).andExpect(status().isOk())
                    .andExpect(content().string(""));
            mvc.perform(get(BASE + "/" + id).principal(auth)).andExpect(status().isNotFound());
        }
    }

    @Test
    void legacyIdentityWithoutUserIdReturns401ForEveryOperationBeforePersistence() throws Exception {
        List<MockHttpServletRequestBuilder> requests = List.of(get(BASE), get(BASE).param("equipmentId", "50"),
                get(BASE + "/1"), post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE),
                put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(UPDATE), delete(BASE + "/1"));
        for (var request : requests) {
            mvc.perform(request.principal(authentication(null))).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void anonymousRequestReturns401() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void aggregateUpdatePreservesOwnerAndGeneralQueryIsAlsoScoped() {
        var report = reports.get(1);
        report.updateReport(51, "Updated", EReportStatus.COMPLETED, "Summary", "Content", "url");
        assertThat(report.getTenantId()).isEqualTo(101);
        assertThat(queries.handle(new GetAllReportsQuery(101))).containsExactly(report);
        assertThat(queries.handle(new GetAllReportsQuery(202))).containsExactly(reports.get(2));
    }
}
