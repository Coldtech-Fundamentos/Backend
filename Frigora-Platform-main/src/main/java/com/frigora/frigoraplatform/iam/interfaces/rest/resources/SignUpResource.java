package com.frigora.frigoraplatform.iam.interfaces.rest.resources;

import com.frigora.frigoraplatform.iam.domain.model.valueobjects.Roles;

import java.util.List;

public record SignUpResource(String username, String password, Roles roles) {
}
