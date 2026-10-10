package com.frigora.frigoraplatform.iam.interfaces.rest.transform;

import com.frigora.frigoraplatform.iam.domain.model.commands.SignUpCommand;
import com.frigora.frigoraplatform.iam.interfaces.rest.resources.SignUpResource;


public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
                resource.username(),
                resource.password(),
                resource.roles()
        );
    }
}
