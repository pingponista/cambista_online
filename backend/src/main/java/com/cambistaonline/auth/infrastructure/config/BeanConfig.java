package com.cambistaonline.auth.infrastructure.config;

import com.cambistaonline.auth.application.ports.inbound.AuthenticateUserUseCase;
import com.cambistaonline.auth.application.ports.inbound.GetCurrentUserUseCase;
import com.cambistaonline.auth.application.ports.inbound.RegisterUserUseCase;
import com.cambistaonline.auth.application.ports.outbound.JwtTokenPort;
import com.cambistaonline.auth.application.ports.outbound.PasswordEncoderPort;
import com.cambistaonline.auth.application.ports.outbound.UserPersistencePort;
import com.cambistaonline.auth.application.service.AuthenticateUserService;
import com.cambistaonline.auth.application.service.GetCurrentUserService;
import com.cambistaonline.auth.application.service.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserPersistencePort userPersistencePort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        return new RegisterUserService(userPersistencePort, passwordEncoderPort);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserPersistencePort userPersistencePort,
            PasswordEncoderPort passwordEncoderPort,
            JwtTokenPort jwtTokenPort
    ) {
        return new AuthenticateUserService(userPersistencePort, passwordEncoderPort, jwtTokenPort);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(
            UserPersistencePort userPersistencePort
    ) {
        return new GetCurrentUserService(userPersistencePort);
    }
}
