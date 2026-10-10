package com.frigora.frigoraplatform.assetsManagement.domain.model.aggregates;

import com.frigora.frigoraplatform.assetsManagement.domain.model.commands.CreateSiteCommand;
import com.frigora.frigoraplatform.feedback.domain.model.aggregates.Review;
import com.frigora.frigoraplatform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Site extends AuditableAbstractAggregateRoot<Site> {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Long ownerId;

    protected Site() {
        this.name = "";
        this.address = "";
        this.contactName = "";
        this.phone = "";
    }

    protected Site(String name, String address, String contactName, String phone) {
        this.name = name;
        this.address = address;
        this.contactName = contactName;
        this.phone = phone;
    }

    /**
     * Constructor for Site aggregate
     *
     * @param command The command to create a site
     */
    public Site(CreateSiteCommand command) {
        this.name = command.name();
        this.address = command.address();
        this.contactName = command.contactName();
        this.phone = command.phone();
        this.ownerId = command.ownerId();
    }

}
