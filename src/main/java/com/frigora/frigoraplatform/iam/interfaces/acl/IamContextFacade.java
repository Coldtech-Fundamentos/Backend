package com.frigora.frigoraplatform.iam.interfaces.acl;

import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.domain.model.commands.SignUpCommand;
import com.frigora.frigoraplatform.iam.domain.model.queries.GetUserByIdQuery;
import com.frigora.frigoraplatform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.frigora.frigoraplatform.iam.domain.model.valueobjects.Roles;
import com.frigora.frigoraplatform.iam.domain.services.UserCommandService;
import com.frigora.frigoraplatform.iam.domain.services.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * IamContextFacade
 * <p>
 *     This class is a facade for the IAM context. It provides a simple interface for other bounded contexts to interact with the
 *     IAM context.
 *     This class is a part of the ACL layer.
 * </p>
 *
 */
@Service
@RequiredArgsConstructor
public class IamContextFacade {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public long createUser(String username, String password, Roles role) {
        var signUpCommand = new SignUpCommand(username, password, role);
        userCommandService.handle(signUpCommand);
        var query = new GetUserByUsernameQuery(username);
        var result = userQueryService.handle(query);
        return result.map(user -> user.getId()).orElse(0);
    }

    public long fetchUserIdByUsername(String username) {
        var query = new GetUserByUsernameQuery(username);
        var result = userQueryService.handle(query);
        return result.map(user -> user.getId()).orElse(0);
    }

    public String fetchUsernameByUserId(Long userId) {
        var query = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(query);
        return result.map(User::getUsername).orElse("");
    }
}
