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
import java.util.List;
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
    
    @Override
    public String createClient(String clientId, List<String> allowedScopes) {
        try {
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", clientId);
            clientData.put("enabled", true);
            clientData.put("clientAuthenticatorType", "client-secret");
            clientData.put("serviceAccountsEnabled", true);
            clientData.put("standardFlowEnabled", false);
            clientData.put("directAccessGrantsEnabled", false);
            clientData.put("publicClient", false);
            clientData.put("protocol", "openid-connect");
            
            // Set default client scopes if provided
            if (allowedScopes != null && !allowedScopes.isEmpty()) {
                clientData.put("defaultClientScopes", allowedScopes);
            }
            
            // Create the client
            ResponseEntity<Map<String, Object>> response = keycloakClient.createClient(clientData);
            
            if (response.getStatusCode() != HttpStatus.CREATED && response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RuntimeException("Failed to create client in Keycloak: " + response.getStatusCode());
            }
            
            // Keycloak doesn't return the secret in the create response, so we need to fetch it
            logger.debug("Client created, fetching client secret for: {}", clientId);
            return fetchClientSecret(clientId);
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new RuntimeException("Client already exists: " + clientId, e);
            }
            logger.error("Error creating client", e);
            throw new RuntimeException("Failed to create client in Keycloak", e);
        } catch (Exception e) {
            logger.error("Error creating client", e);
            throw new RuntimeException("Failed to create client in Keycloak", e);
        }
    }
    
    private String fetchClientSecret(String clientId) {
        try {
            ResponseEntity<Map<String, Object>> secretResponse = keycloakClient.getClientSecret(clientId);
            Map<String, Object> secretBody = secretResponse.getBody();
            if (secretBody != null && secretBody.containsKey("value")) {
                return (String) secretBody.get("value");
            }
            throw new RuntimeException("Client secret not found in response for client: " + clientId);
        } catch (Exception e) {
            logger.error("Error fetching client secret for client: {}", clientId, e);
            throw new RuntimeException("Failed to fetch client secret from Keycloak", e);
        }
    }
}
