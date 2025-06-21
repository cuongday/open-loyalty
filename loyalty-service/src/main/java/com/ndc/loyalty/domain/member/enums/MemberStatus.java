package com.ndc.loyalty.domain.member.enums;

import lombok.Getter;

/**
 * Member Status Enum
 * 
 * Defines possible states của Member trong loyalty system
 * 
 * @author NDC Team
 */
@Getter
public enum MemberStatus {
    
    /**
     * Active member - có thể participate trong loyalty program
     */
    ACTIVE("Active", "Member is active and can participate in loyalty program"),
    
    /**
     * Inactive member - temporarily disabled
     */
    INACTIVE("Inactive", "Member is temporarily inactive"),
    
    /**
     * Suspended member - disciplinary action
     */
    SUSPENDED("Suspended", "Member is suspended due to policy violations"),
    
    /**
     * Pending verification - awaiting email/phone verification
     */
    PENDING_VERIFICATION("Pending Verification", "Member is pending email or phone verification"),
    
    /**
     * Blocked member - permanently blocked
     */
    BLOCKED("Blocked", "Member is permanently blocked"),
    
    /**
     * Deactivated by member - self-deactivated
     */
    DEACTIVATED("Deactivated", "Member has deactivated their account");

    private final String displayName;
    private final String description;

    MemberStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if member có thể participate trong loyalty activities
     */
    public boolean canParticipate() {
        return this == ACTIVE;
    }

    /**
     * Check if member có thể login
     */
    public boolean canLogin() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }

    /**
     * Check if member có thể receive communications
     */
    public boolean canReceiveCommunications() {
        return this == ACTIVE || this == INACTIVE || this == PENDING_VERIFICATION;
    }

    /**
     * Check if status is terminal (cannot be changed)
     */
    public boolean isTerminal() {
        return this == BLOCKED || this == DEACTIVATED;
    }

    /**
     * Get valid transition statuses từ current status
     */
    public MemberStatus[] getValidTransitions() {
        switch (this) {
            case PENDING_VERIFICATION:
                return new MemberStatus[]{ACTIVE, SUSPENDED, BLOCKED};
            case ACTIVE:
                return new MemberStatus[]{INACTIVE, SUSPENDED, BLOCKED, DEACTIVATED};
            case INACTIVE:
                return new MemberStatus[]{ACTIVE, SUSPENDED, BLOCKED, DEACTIVATED};
            case SUSPENDED:
                return new MemberStatus[]{ACTIVE, BLOCKED, DEACTIVATED};
            case BLOCKED:
            case DEACTIVATED:
                return new MemberStatus[]{}; // Terminal states
            default:
                return new MemberStatus[]{};
        }
    }

    /**
     * Check if transition to target status is valid
     */
    public boolean canTransitionTo(MemberStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        if (this == targetStatus) {
            return false; // Same status transition not allowed
        }
        
        MemberStatus[] validTransitions = getValidTransitions();
        for (MemberStatus validStatus : validTransitions) {
            if (validStatus == targetStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get CSS class cho UI display
     */
    public String getCssClass() {
        switch (this) {
            case ACTIVE:
                return "badge-success";
            case INACTIVE:
                return "badge-warning";
            case SUSPENDED:
                return "badge-danger";
            case PENDING_VERIFICATION:
                return "badge-info";
            case BLOCKED:
                return "badge-dark";
            case DEACTIVATED:
                return "badge-secondary";
            default:
                return "badge-light";
        }
    }
}