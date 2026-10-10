package com.frigora.frigoraplatform.monitoring.interfaces.rest.resources;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertSeverity;
import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.EAlertStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertResource {

    @NotNull
    private Long equipmentId;

    @NotNull
    private Long siteId;

    @NotBlank
    private String type;

    @NotNull
    private EAlertSeverity severity;

    @NotBlank
    private String description;

    @NotNull
    private EAlertStatus status;
}
