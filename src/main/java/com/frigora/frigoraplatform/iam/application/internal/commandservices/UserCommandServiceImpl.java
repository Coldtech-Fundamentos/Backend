package com.frigora.frigoraplatform.iam.application.internal.commandservices;

import com.frigora.frigoraplatform.iam.application.internal.outboundservices.hashing.HashingService;
import com.frigora.frigoraplatform.iam.application.internal.outboundservices.tokens.TokenService;
import com.frigora.frigoraplatform.iam.domain.model.aggregates.User;
import com.frigora.frigoraplatform.iam.domain.model.commands.SignInCommand;
import com.frigora.frigoraplatform.iam.domain.model.commands.SignUpCommand;
import com.frigora.frigoraplatform.iam.domain.services.UserCommandService;
import com.frigora.frigoraplatform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User command service implementation
 * <p>
 *     This class implements the {@link UserCommandService} interface and provides the implementation for the
 *     {@link SignInCommand} and {@link SignUpCommand} commands.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final HashingService hashingService;

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) throw new RuntimeException("Invalid username or password");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid username or password");
        var token = tokenService.generateToken(user.get());
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username " + command.username() + " is already taken");

        var hashedPassword = hashingService.encode(command.password());
        var user = new User(command.username(), hashedPassword, command.roles());
        try {
            userRepository.save(user);
            return Optional.of(user);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while creating user: " + e.getMessage());
        }
    }
}
