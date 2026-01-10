package io.openleap.iam.principal.service.keycloak.web;

import io.openleap.iam.principal.exception.UserAlreadyExistsException;
import io.openleap.iam.principal.service.keycloak.KeycloakService;
import io.openleap.iam.principal.service.keycloak.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Profile("keycloak.web")
@Service
public class KeycloakWebService implements KeycloakService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakWebService.class);

    private final KeycloakClient keycloakClient;

    public KeycloakWebService(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public String createUser(User user) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.username());
            userData.put("email", user.email());
            userData.put("firstName", user.firstName());
            userData.put("lastName", user.lastName());
            userData.put("enabled", user.enabled());
            userData.put("emailVerified", user.emailVerified());

            ResponseEntity<Void> response = keycloakClient.createUser(userData);
            String createdUserId = keycloakClient.extractUserIdFromLocationHeader(response.getHeaders().getFirst("Location"));

            if (createdUserId == null) {
                throw new RuntimeException("Failed to create user");
            }
            return createdUserId;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistsException(user.email());
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
}
