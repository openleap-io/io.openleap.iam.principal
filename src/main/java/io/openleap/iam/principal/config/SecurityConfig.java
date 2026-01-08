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
        http.securityMatcher("/iam/principal/**")
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/iam/principal/**").hasAuthority("ROLE_iam.principal.read"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter())));

        return http.build();
    }
}