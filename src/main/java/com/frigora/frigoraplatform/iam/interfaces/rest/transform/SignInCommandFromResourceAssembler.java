package com.frigora.frigoraplatform.iam.interfaces.rest.transform;

import com.frigora.frigoraplatform.iam.domain.model.commands.SignInCommand;
import com.frigora.frigoraplatform.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.username(), signInResource.password());
    }
}
