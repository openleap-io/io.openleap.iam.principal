package io.openleap.iam.principal.service.keycloak.logger;

import io.openleap.iam.principal.service.keycloak.KeycloakService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("logger")
@Service
public class KeycloakLoggerService implements KeycloakService {
    @Override
    public String createUser(io.openleap.iam.principal.service.keycloak.dto.User user) {
        System.out.println("createUser called with user: " + user);
        return "logger-user-id";
    }

    @Override
    public void updateUser(String keycloakUserId, io.openleap.iam.principal.service.keycloak.dto.User user) {
        System.out.println("updateUser called with keycloakUserId: " + keycloakUserId + ", user: " + user);
    }

    @Override
    public String createClient(String clientId, java.util.List<String> allowedScopes) {
        System.out.println("createClient called with clientId: " + clientId + ", allowedScopes: " + allowedScopes);
        return "logger-client-secret";
    }

    @Override
    public void updateClient(String clientId, boolean enabled) {
        System.out.println("updateClient called with clientId: " + clientId + ", enabled: " + enabled);
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        System.out.println("deleteUser called with keycloakUserId: " + keycloakUserId);
    }

    @Override
    public void deleteClient(String clientId) {
        System.out.println("deleteClient called with clientId: " + clientId);
    }

    @Override
    public String regenerateClientSecret(String clientId) {
        System.out.println("regenerateClientSecret called with clientId: " + clientId);
        return "logger-new-client-secret";
    }
}
