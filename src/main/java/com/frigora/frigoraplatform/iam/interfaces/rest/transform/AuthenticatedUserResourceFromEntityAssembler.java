package com.frigora.frigoraplatform.iam.interfaces.rest.transform;

import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername(),
                token,
                user.getRole().name()
        );
    }
}