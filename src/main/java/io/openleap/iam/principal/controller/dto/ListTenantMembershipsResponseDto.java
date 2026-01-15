package io.openleap.iam.principal.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for listing tenant memberships.
 */
public class ListTenantMembershipsResponseDto {

    /**
     * List of membership items
     */
    @JsonProperty("items")
    private List<TenantMembershipItemDto> items;

    /**
     * Total number of memberships
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

    public List<TenantMembershipItemDto> getItems() {
        return items;
    }

    public void setItems(List<TenantMembershipItemDto> items) {
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
     * DTO for a single tenant membership item.
     */
    public static class TenantMembershipItemDto {

        /**
         * Membership ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * Principal ID
         */
        @JsonProperty("principal_id")
        private String principalId;

        /**
         * Tenant ID
         */
        @JsonProperty("tenant_id")
        private String tenantId;

        /**
         * Membership start date
         */
        @JsonProperty("valid_from")
        private String validFrom;

        /**
         * Membership end date (null = no expiry)
         */
        @JsonProperty("valid_to")
        private String validTo;

        /**
         * Membership status
         */
        @JsonProperty("status")
        private String status;

        /**
         * Whether this is the primary tenant
         */
        @JsonProperty("is_primary")
        private Boolean isPrimary;

        // Getters and Setters

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(String principalId) {
            this.principalId = principalId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(String validFrom) {
            this.validFrom = validFrom;
        }

        public String getValidTo() {
            return validTo;
        }

        public void setValidTo(String validTo) {
            this.validTo = validTo;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getIsPrimary() {
            return isPrimary;
        }

        public void setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }
}
