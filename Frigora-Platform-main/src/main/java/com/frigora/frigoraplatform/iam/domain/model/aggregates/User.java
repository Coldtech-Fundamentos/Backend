package com.frigora.frigoraplatform.iam.domain.model.aggregates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.frigora.frigoraplatform.iam.domain.model.valueobjects.Roles;
import com.frigora.frigoraplatform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User aggregate root
 * This class represents the aggregate root for the User entity.
 *
 * @see AuditableAbstractAggregateRoot
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends AuditableAbstractAggregateRoot<User> {

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 120)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles role;

    public User(String username, String password, Roles role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User updateUsername(String username) {
        this.username = username;
        return this;
    }

    public User updatePassword(String password) {
        this.password = password;
        return this;
    }
}
