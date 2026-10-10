package com.frigora.frigoraplatform.iam.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Roles
 * <p>
 *     This enum represents the roles in the system.
 * </p>
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Roles {
    Owner,
    Provider,
}
