package com.frigora.frigoraplatform.reporting.domain.model.aggregates;

import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportStatus;
import com.frigora.frigoraplatform.reporting.domain.model.valueobjects.EReportType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "reports")
@Getter
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer tenantId;

    @Enumerated(EnumType.STRING)
    private EReportType type;

    private Integer equipmentId;

    private String title;

    @Enumerated(EnumType.STRING)
    private EReportStatus status;

    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String url;

    @Embedded
    private ReportAudit audit = new ReportAudit();

    protected Report() {}

    public Report(Integer tenantId, EReportType type, Integer equipmentId, String title,
                  EReportStatus status, String summary, String content, String url) {
        this.tenantId = tenantId;
        this.type = type;
        this.equipmentId = equipmentId;
        this.title = title;
        this.status = status;
        this.summary = summary;
        this.content = content;
        this.url = url;
    }

    public Report(CreateReportCommand command) {
        this.tenantId = command.tenantId();
        this.type = command.type();
        this.equipmentId = command.equipmentId();
        this.title = command.title();
        this.status = EReportStatus.IN_PROGRESS;
        this.summary = command.summary();
        this.content = command.content();
        this.url = command.url();
    }

    public void updateReport(Integer tenantId, Integer equipmentId, String title, EReportStatus status,
                             String summary, String content, String url) {
        this.tenantId = tenantId;
        this.equipmentId = equipmentId;
        this.title = title;
        this.status = status;
        this.summary = summary;
        this.content = content;
        this.url = url;
    }
}