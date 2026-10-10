package com.frigora.frigoraplatform.monitoring.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frigora.frigoraplatform.monitoring.domain.model.valueobjects.StatusEquipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResource {
    private Long id;
    private UUID equipmentId;
    private String model;
    private String type;
    private String serial;
    private StatusEquipment status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime installed;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastSeen;

    private Float setPoint;

    private String name;
    private String manufacturer;
    private Boolean online;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updated;

    private Long ownerId;
}
