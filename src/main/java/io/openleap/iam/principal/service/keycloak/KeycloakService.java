package io.openleap.iam.principal.service.keycloak;

import io.openleap.iam.principal.service.keycloak.dto.User;

public interface KeycloakService {
    String createUser(User user);
    
    /**
     * Updates a user in Keycloak.
     * 
     * @param keycloakUserId the Keycloak user ID
     * @param user the user data to update
     */
    void updateUser(String keycloakUserId, User user);
    
    /**
     * Creates an OAuth2 client in Keycloak for service principal authentication.
     * 
     * @param clientId the client ID (typically the service name)
     * @param allowedScopes the OAuth2 scopes allowed for this client
     * @return the client secret (returned only once during creation)
     */
    String createClient(String clientId, java.util.List<String> allowedScopes);
}
