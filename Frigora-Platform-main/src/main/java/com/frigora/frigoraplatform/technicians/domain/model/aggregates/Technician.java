package com.frigora.frigoraplatform.technicians.domain.model.aggregates;

import com.frigora.frigoraplatform.technicians.domain.model.commands.CreateTechnicianCommand;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private String phone;

    @Column(name = "provider_id", nullable = false)
    private Integer providerId;

    protected Technician() {
        this.name = "";
        this.specialty = "";
        this.phone = "";
        this.providerId = 0;
    }

    public Technician(CreateTechnicianCommand command) {
        this.name = command.name();
        this.specialty = command.specialty();
        this.phone = command.phone();
        this.providerId = command.providerId();
    }

    public void update(String name, String specialty, String phone) {
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
    }
}