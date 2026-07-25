package com.cambistaonline.auth.infrastructure.config;

import com.cambistaonline.auth.application.usecases.AuthenticateUserUseCase;
import com.cambistaonline.auth.application.usecases.GetCurrentUserUseCase;
import com.cambistaonline.auth.application.usecases.RegisterUserUseCase;
import com.cambistaonline.auth.domain.ports.JwtTokenPort;
import com.cambistaonline.auth.domain.ports.PasswordEncoderPort;
import com.cambistaonline.auth.domain.ports.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        return new RegisterUserUseCase(userRepositoryPort, passwordEncoderPort);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            JwtTokenPort jwtTokenPort
    ) {
        return new AuthenticateUserUseCase(userRepositoryPort, passwordEncoderPort, jwtTokenPort);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(
            UserRepositoryPort userRepositoryPort
    ) {
        return new GetCurrentUserUseCase(userRepositoryPort);
    }
}
