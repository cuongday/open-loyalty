package com.ndc.loyalty.domain.member.command;

import com.ndc.loyalty.domain.member.valueobject.MemberId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Verify Member Email Command
 * 
 * Command để verify email address của member
 * Sử dụng verification code được gửi qua email
 * 
 * @author NDC Team
 */
@Data
@Builder
@EqualsAndHashCode
public class VerifyMemberEmailCommand {

    @TargetAggregateIdentifier
    @NotNull(message = "Member ID cannot be null")
    private final MemberId memberId;

    @NotBlank(message = "Verification code cannot be blank")
    @Size(min = 6, max = 10, message = "Verification code must be between 6 and 10 characters")
    private final String verificationCode;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final String verifiedBy;
    
    private final String ipAddress;
    
    private final String userAgent;

    /**
     * Static factory method
     */
    public static VerifyMemberEmailCommand create(MemberId memberId, String verificationCode) {
        return VerifyMemberEmailCommand.builder()
                .memberId(memberId)
                .verificationCode(verificationCode)
                .build();
    }

    /**
     * Static factory method với đầy đủ context
     */
    public static VerifyMemberEmailCommand createWithContext(MemberId memberId, 
                                                           String verificationCode,
                                                           String verifiedBy,
                                                           String ipAddress,
                                                           String userAgent) {
        return VerifyMemberEmailCommand.builder()
                .memberId(memberId)
                .verificationCode(verificationCode)
                .verifiedBy(verifiedBy)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Validate command data
     */
    public void validate() {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID is required");
        }
        
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification code is required");
        }
        
        String normalizedCode = verificationCode.trim();
        if (normalizedCode.length() < 6 || normalizedCode.length() > 10) {
            throw new IllegalArgumentException("Verification code must be between 6 and 10 characters");
        }
        
        // Check if code contains only alphanumeric characters
        if (!normalizedCode.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Verification code must contain only alphanumeric characters");
        }
    }

    /**
     * Get normalized verification code
     */
    public String getNormalizedVerificationCode() {
        return verificationCode != null ? verificationCode.trim().toUpperCase() : null;
    }

    /**
     * Check if verification is automated (system initiated)
     */
    public boolean isAutomatedVerification() {
        return verifiedBy == null || "SYSTEM".equalsIgnoreCase(verifiedBy);
    }
}