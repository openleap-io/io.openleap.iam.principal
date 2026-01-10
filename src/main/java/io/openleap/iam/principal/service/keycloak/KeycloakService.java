package io.openleap.iam.principal.service.keycloak;

import io.openleap.iam.principal.service.keycloak.dto.User;

public interface KeycloakService {
    String createUser(User user);
}
