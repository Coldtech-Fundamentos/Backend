package com.frigora.frigoraplatform.reporting.domain.services;


import com.frigora.frigoraplatform.reporting.domain.model.aggregates.Report;
import com.frigora.frigoraplatform.reporting.domain.model.commands.CreateReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.DeleteReportCommand;
import com.frigora.frigoraplatform.reporting.domain.model.commands.UpdateReportCommand;

import java.util.Optional;

public interface ReportCommandService {
    Optional<Report> handle(CreateReportCommand command);
    Optional<Report> handle(UpdateReportCommand command);
    void handle(DeleteReportCommand command);
}