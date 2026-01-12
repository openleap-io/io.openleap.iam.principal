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
    
    /**
     * Updates a client in Keycloak (e.g., to enable/disable it).
     *
     * @param clientId the client ID
     * @param enabled whether the client should be enabled
     */
    void updateClient(String clientId, boolean enabled);

    /**
     * Deletes a user from Keycloak.
     *
     * @param keycloakUserId the Keycloak user ID
     */
    void deleteUser(String keycloakUserId);

    /**
     * Deletes a client from Keycloak.
     *
     * @param clientId the client ID
     */
    void deleteClient(String clientId);

    /**
     * Regenerates the client secret for a service principal.
     *
     * @param clientId the client ID
     * @return the new client secret
     */
    String regenerateClientSecret(String clientId);
}
