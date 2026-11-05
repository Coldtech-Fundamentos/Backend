package com.frigora.frigoraplatform.reporting.application.internal.commandservices;

import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.DeleteReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.UpdateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.repositories.ReportRepository;
import com.frigora.frigoraplatform.reporting.domain.services.ReportCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReportCommandServiceImpl implements ReportCommandService {

    private final ReportRepository reportRepository;

    // Inyección de dependencias por constructor
    public ReportCommandServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional
    public Optional<Report> handle(CreateReportCommand command) {
        var report = new Report(command);
        reportRepository.save(report);
        return Optional.of(report);
    }

    @Override
    @Transactional
    public Optional<Report> handle(UpdateReportCommand command) {
        var result = reportRepository.findByIdAndTenantId(command.id(), command.tenantId());
        if (result.isEmpty()) {
            return Optional.empty();
        }

        var report = result.get();
        // El método de actualizaciÃ³n que definimos en el Aggregate
        report.updateReport(
                command.equipmentId(),
                command.title(),
                command.status(),
                command.summary(),
                command.content(),
                command.url()
        );

        reportRepository.save(report);
        return Optional.of(report);
    }

    @Override
    @Transactional
    public boolean handle(DeleteReportCommand command) {
        var result = reportRepository.findByIdAndTenantId(command.reportId(), command.tenantId());
        if (result.isEmpty()) {
            return false;
        }
        reportRepository.delete(result.get());
        return true;
    }
}
