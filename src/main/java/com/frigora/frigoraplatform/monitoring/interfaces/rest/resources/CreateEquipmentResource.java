package com.frigora.frigoraplatform.monitoring.interfaces.rest.resources;

import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.StatusEquipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentResource {

    @NotBlank
    private String model;

    @NotBlank
    private String type;

    @NotBlank
    private String serial;

    @NotNull
    private StatusEquipment status;

    private LocalDateTime installed;

    private LocalDateTime lastSeen;

    private Float setPoint;

    @NotBlank
    private String name;

    @NotBlank
    private String manufacturer;

    @NotNull
    private Boolean online;
}
