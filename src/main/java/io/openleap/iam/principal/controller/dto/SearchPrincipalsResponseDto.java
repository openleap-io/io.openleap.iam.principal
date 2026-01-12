package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for searching principals.
 */
public class SearchPrincipalsResponseDto {

    /**
     * List of matching principals
     */
    @JsonProperty("items")
    private List<PrincipalSearchItem> items;

    /**
     * Total count of matching principals
     */
    @JsonProperty("total")
    private long total;

    /**
     * Current page number (1-indexed)
     */
    @JsonProperty("page")
    private int page;

    /**
     * Page size
     */
    @JsonProperty("size")
    private int size;

    // Getters and Setters

    public List<PrincipalSearchItem> getItems() {
        return items;
    }

    public void setItems(List<PrincipalSearchItem> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Item representing a principal in search results.
     */
    public static class PrincipalSearchItem {

        @JsonProperty("principal_id")
        private String principalId;

        @JsonProperty("username")
        private String username;

        @JsonProperty("email")
        private String email;

        @JsonProperty("principal_type")
        private String principalType;

        @JsonProperty("status")
        private String status;

        @JsonProperty("primary_tenant_id")
        private String primaryTenantId;

        @JsonProperty("last_login_at")
        private String lastLoginAt;

        @JsonProperty("created_at")
        private String createdAt;

        // Getters and Setters

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPrincipalType() {
            return principalType;
        }

        public void setPrincipalType(String principalType) {
            this.principalType = principalType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPrimaryTenantId() {
            return primaryTenantId;
        }

        public void setPrimaryTenantId(String primaryTenantId) {
            this.primaryTenantId = primaryTenantId;
        }

        public String getLastLoginAt() {
            return lastLoginAt;
        }

        public void setLastLoginAt(String lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
