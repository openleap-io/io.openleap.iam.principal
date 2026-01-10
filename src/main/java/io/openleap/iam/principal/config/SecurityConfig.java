package io.openleap.iam.principal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static io.openleap.starter.core.security.SecurityKeycloakConfig.customJwtAuthenticationConverter;

@Profile({"keycloak"})
@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain iamPrincipalFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/iam/principals/**")
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/api/v1/iam/principals").hasAuthority("ROLE_iam.principal:create")
                                        .requestMatchers("/api/v1/iam/principals/service").hasAuthority("ROLE_iam.service_principal:create")
                                        .requestMatchers("/api/v1/iam/principals/system").hasAuthority("ROLE_iam.system_principal:create")
                                        )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter())));

        return http.build();
    }
}