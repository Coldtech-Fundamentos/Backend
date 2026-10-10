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
        var result = reportRepository.findById(command.id());
        if (result.isEmpty()) {
            throw new IllegalArgumentException("El reporte con ID " + command.id() + " no existe.");
        }

        var report = result.get();
        // El método de actualizaciÃ³n que definimos en el Aggregate
        report.updateReport(
                command.tenantId(),
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
    public void handle(DeleteReportCommand command) {
        var result = reportRepository.findById(command.reportId());
        if (result.isEmpty()) {
            throw new IllegalArgumentException("El reporte con ID " + command.reportId() + " no existe.");
        }
        reportRepository.delete(result.get());
    }
}