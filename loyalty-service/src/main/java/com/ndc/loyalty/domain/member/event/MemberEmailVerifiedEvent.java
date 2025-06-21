package com.ndc.loyalty.domain.member.event;

import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Member Email Verified Event
 * 
 * Domain event được emit khi member email được verify thành công
 * 
 * @author NDC Team
 */
@Data
@Builder
@EqualsAndHashCode
public class MemberEmailVerifiedEvent {

    private final MemberId memberId;
    private final Email email;
    private final String verificationCode;
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    private final String verifiedBy;
    private final String ipAddress;
    private final String userAgent;

    /**
     * Static factory method
     */
    public static MemberEmailVerifiedEvent create(MemberId memberId, Email email, String verificationCode) {
        return MemberEmailVerifiedEvent.builder()
                .memberId(memberId)
                .email(email)
                .verificationCode(verificationCode)
                .build();
    }

    /**
     * Static factory method với context
     */
    public static MemberEmailVerifiedEvent createWithContext(MemberId memberId, 
                                                            Email email, 
                                                            String verificationCode,
                                                            String verifiedBy,
                                                            String ipAddress,
                                                            String userAgent) {
        return MemberEmailVerifiedEvent.builder()
                .memberId(memberId)
                .email(email)
                .verificationCode(verificationCode)
                .verifiedBy(verifiedBy)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Check if verification was automated
     */
    public boolean isAutomatedVerification() {
        return verifiedBy == null || "SYSTEM".equalsIgnoreCase(verifiedBy);
    }

    /**
     * Check if verification has context info
     */
    public boolean hasContextInfo() {
        return ipAddress != null || userAgent != null;
    }

    /**
     * Event type for routing
     */
    public String getEventType() {
        return "MemberEmailVerified";
    }

    /**
     * Get event version for compatibility
     */
    public String getEventVersion() {
        return "1.0";
    }
}