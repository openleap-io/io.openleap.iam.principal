package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for cross-tenant principal search.
 */
public class CrossTenantSearchResponseDto {

    /**
     * List of principal items
     */
    @JsonProperty("items")
    private List<CrossTenantPrincipalItemDto> items;

    /**
     * Total number of matching principals
     */
    @JsonProperty("total")
    private long total;

    /**
     * Current page number
     */
    @JsonProperty("page")
    private int page;

    /**
     * Page size
     */
    @JsonProperty("size")
    private int size;

    // Getters and Setters

    public List<CrossTenantPrincipalItemDto> getItems() {
        return items;
    }

    public void setItems(List<CrossTenantPrincipalItemDto> items) {
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
     * DTO for a single principal item in cross-tenant search results.
     */
    public static class CrossTenantPrincipalItemDto {

        /**
         * Principal ID
         */
        @JsonProperty("principal_id")
        private String principalId;

        /**
         * Principal type (HUMAN, SERVICE, SYSTEM, DEVICE)
         */
        @JsonProperty("principal_type")
        private String principalType;

        /**
         * Username
         */
        @JsonProperty("username")
        private String username;

        /**
         * Email address
         */
        @JsonProperty("email")
        private String email;

        /**
         * Principal status
         */
        @JsonProperty("status")
        private String status;

        /**
         * Primary tenant ID
         */
        @JsonProperty("primary_tenant_id")
        private String primaryTenantId;

        // Getters and Setters

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

        public String getPrincipalType() {
            return principalType;
        }

        public void setPrincipalType(String principalType) {
            this.principalType = principalType;
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
    }
}
