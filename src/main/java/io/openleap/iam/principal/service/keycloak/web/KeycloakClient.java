package io.openleap.iam.principal.service.keycloak.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Profile("keycloak.web")
@Component
public class KeycloakClient {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakClient.class);
    private static final DateTimeFormatter KEYCLOAK_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String adminUsername;
    private final String adminPassword;
    private String accessToken;
    private LocalDateTime tokenExpiry;

    public KeycloakClient(
            @Value("${keycloak.server-url:http://localhost:8080}") String serverUrl,
            @Value("${keycloak.realm:datapart}") String realm,
            @Value("${keycloak.client-id:datapart-authorization}") String clientId,
            @Value("${keycloak.client-secret:your-client-secret}") String clientSecret,
            @Value("${keycloak.admin-username:admin}") String adminUsername,
            @Value("${keycloak.admin-password:admin}") String adminPassword) {

        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.baseUrl = serverUrl;

        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<Void> createUser(Map<String, Object> userData) {
        ensureValidToken();
        String url = String.format("/admin/realms/%s/users", realm);

        return getVoidResponseEntity(userData, url);
    }

    @NotNull
    private ResponseEntity<Void> getVoidResponseEntity(Map<String, Object> organizationData, String url) {
        HttpEntity<Map<String, Object>> requestEntity = getMapHttpEntity(organizationData);

        return restTemplate.postForEntity(baseUrl + url, requestEntity, Void.class);
    }

    private HttpEntity<Map<String, Object>> getMapHttpEntity(Map<String, Object> organizationData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(organizationData, headers);
        return requestEntity;
    }

    public String extractUserIdFromLocationHeader(String locationHeader) {
        if (locationHeader == null) {
            return null;
        }

        // Extract ID from URL like: /admin/realms/datapart/users/12345
        String[] parts = locationHeader.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }

        return null;
    }

    public void ensureValidToken() {
        if (accessToken == null || tokenExpiry == null || LocalDateTime.now().isAfter(tokenExpiry)) {
            obtainAccessToken();
        }
    }

    private void obtainAccessToken() {
        try {
            String url = String.format("/realms/%s/protocol/openid-connect/token", realm);

            // Build form data as URL-encoded string
            String formData = String.format(
                    "grant_type=password&client_id=%s&client_secret=%s&username=%s&password=%s",
                    clientId, clientSecret, adminUsername, adminPassword
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.http.HttpEntity<String> requestEntity =
                    new org.springframework.http.HttpEntity<>(formData, headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl + url, requestEntity, JsonNode.class);

            accessToken = response.getBody().get("access_token").asText();
            int expiresIn = response.getBody().get("expires_in").asInt();
            tokenExpiry = LocalDateTime.now().plusSeconds(expiresIn - 60); // 60 seconds buffer

        } catch (Exception e) {
            logger.error("Error obtaining access token", e);
            throw new RuntimeException("Failed to obtain access token", e);
        }
    }
}
