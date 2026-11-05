package com.frigora.frigoraplatform.iam.infrastructure.hashing.bcrypt;

import com.frigora.frigoraplatform.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;


public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
