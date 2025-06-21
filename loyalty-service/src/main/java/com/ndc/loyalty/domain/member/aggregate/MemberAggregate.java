package com.ndc.loyalty.domain.member.aggregate;

import com.ndc.loyalty.domain.member.command.CreateMemberCommand;
import com.ndc.loyalty.domain.member.command.UpdateMemberProfileCommand;
import com.ndc.loyalty.domain.member.command.VerifyMemberEmailCommand;
import com.ndc.loyalty.domain.member.enums.MemberStatus;
import com.ndc.loyalty.domain.member.event.MemberCreatedEvent;
import com.ndc.loyalty.domain.member.event.MemberEmailVerifiedEvent;
import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import com.ndc.loyalty.domain.member.valueobject.PhoneNumber;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.LocalDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Member Aggregate
 * 
 * Domain aggregate cho Member trong loyalty system
 * Implements CQRS + Event Sourcing pattern với Axon Framework
 * Chứa business logic và state management cho Member
 * 
 * @author NDC Team
 */
@Aggregate
@Getter
@Slf4j
public class MemberAggregate {

    @AggregateIdentifier
    private MemberId memberId;
    
    private Email email;
    private String firstName;
    private String lastName;
    private PhoneNumber phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String referralCode;
    private String sourceChannel;
    
    private MemberStatus status;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String emailVerificationCode;
    private Instant emailVerificationExpiry;
    private String phoneVerificationCode;
    private Instant phoneVerificationExpiry;
    
    private Map<String, String> customAttributes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private long version;

    /**
     * Default constructor cho Axon Framework
     */
    protected MemberAggregate() {
        // Required by Axon
    }

    /**
     * Command Handler cho CreateMemberCommand
     */
    @CommandHandler
    public MemberAggregate(CreateMemberCommand command) {
        log.info("Creating new member with ID: {}", command.getMemberId());
        
        // Validate command
        command.validate();
        
        // Validate business rules
        validateCreateMemberBusinessRules(command);
        
        // Apply MemberCreatedEvent
        AggregateLifecycle.apply(MemberCreatedEvent.createComplete(
            command.getMemberId(),
            command.getEmail(),
            command.getFirstName(),
            command.getLastName(),
            command.getPhone(),
            command.getDateOfBirth(),
            command.getGender(),
            command.getAddress(),
            command.getCity(),
            command.getCountry(),
            command.getPostalCode(),
            command.getReferralCode(),
            command.getSourceChannel(),
            command.getCustomAttributes(),
            command.getNotes()
        ));
        
        log.info("Member created successfully with ID: {}", command.getMemberId());
    }

    /**
     * Command Handler cho UpdateMemberProfileCommand
     */
    @CommandHandler
    public void handle(UpdateMemberProfileCommand command) {
        log.info("Updating member profile for ID: {}", command.getMemberId());
        
        // Validate command
        command.validate();
        
        // Validate business rules
        validateUpdateProfileBusinessRules(command);
        
        // Apply updates based on what fields are provided
        if (command.isEmailUpdate()) {
            updateEmail(command.getNewEmail());
        }
        
        if (command.isPersonalInfoUpdate()) {
            updatePersonalInfo(command);
        }
        
        if (command.isAddressUpdate()) {
            updateAddressInfo(command);
        }
        
        // Update custom attributes if provided
        if (command.getCustomAttributesToUpdate() != null && !command.getCustomAttributesToUpdate().isEmpty()) {
            updateCustomAttributes(command.getCustomAttributesToUpdate());
        }
        
        log.info("Member profile updated successfully for ID: {}", command.getMemberId());
    }

    /**
     * Command Handler cho VerifyMemberEmailCommand
     */
    @CommandHandler
    public void handle(VerifyMemberEmailCommand command) {
        log.info("Verifying email for member ID: {}", command.getMemberId());
        
        // Validate command
        command.validate();
        
        // Validate business rules for email verification
        validateEmailVerificationBusinessRules(command);
        
        // Apply MemberEmailVerifiedEvent
        AggregateLifecycle.apply(MemberEmailVerifiedEvent.createWithContext(
            command.getMemberId(),
            this.email,
            command.getVerificationCode(),
            command.getVerifiedBy(),
            command.getIpAddress(),
            command.getUserAgent()
        ));
        
        log.info("Email verified successfully for member ID: {}", command.getMemberId());
    }

    /**
     * Event Sourcing Handler cho MemberCreatedEvent
     */
    @EventSourcingHandler
    public void on(MemberCreatedEvent event) {
        this.memberId = event.getMemberId();
        this.email = event.getEmail();
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.phone = event.getPhone();
        this.dateOfBirth = event.getDateOfBirth();
        this.gender = event.getGender();
        this.address = event.getAddress();
        this.city = event.getCity();
        this.country = event.getCountry();
        this.postalCode = event.getPostalCode();
        this.referralCode = event.getReferralCode();
        this.sourceChannel = event.getSourceChannel();
        
        this.status = MemberStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.customAttributes = new HashMap<>(event.getCustomAttributes());
        this.createdAt = event.getTimestamp();
        this.updatedAt = event.getTimestamp();
        this.version = 0;
        
        // Generate email verification code
        this.emailVerificationCode = generateVerificationCode();
        this.emailVerificationExpiry = Instant.now().plusSeconds(24 * 60 * 60); // 24 hours
        
        log.debug("Member aggregate state updated from MemberCreatedEvent: {}", memberId);
    }

    /**
     * Event Sourcing Handler cho MemberEmailVerifiedEvent
     */
    @EventSourcingHandler
    public void on(MemberEmailVerifiedEvent event) {
        this.emailVerified = true;
        this.emailVerificationCode = null;
        this.emailVerificationExpiry = null;
        this.updatedAt = event.getTimestamp();
        this.version++;
        
        // If both email and phone are verified, activate member
        if (this.emailVerified && this.phoneVerified) {
            this.status = MemberStatus.ACTIVE;
        }
        
        log.debug("Member email verified, aggregate state updated: {}", memberId);
    }

    // =====================================================
    // BUSINESS LOGIC METHODS
    // =====================================================

    /**
     * Validate business rules cho CreateMemberCommand
     */
    private void validateCreateMemberBusinessRules(CreateMemberCommand command) {
        // Check age restriction
        if (command.getDateOfBirth() != null && !command.isAdult()) {
            throw new IllegalArgumentException("Member must be at least 18 years old");
        }
        
        // Additional business rules can be added here
    }

    /**
     * Validate business rules cho UpdateMemberProfileCommand
     */
    private void validateUpdateProfileBusinessRules(UpdateMemberProfileCommand command) {
        // Check if member can be updated
        if (this.status == MemberStatus.BLOCKED || this.status == MemberStatus.DEACTIVATED) {
            throw new IllegalStateException("Cannot update profile for blocked or deactivated member");
        }
        
        // If email is being updated, mark as unverified
        if (command.isEmailUpdate() && !command.getNewEmail().equals(this.email)) {
            // Email verification will be required
        }
    }

    /**
     * Validate business rules cho email verification
     */
    private void validateEmailVerificationBusinessRules(VerifyMemberEmailCommand command) {
        // Check if member exists and can be verified
        if (this.status == MemberStatus.BLOCKED || this.status == MemberStatus.DEACTIVATED) {
            throw new IllegalStateException("Cannot verify email for blocked or deactivated member");
        }
        
        // Check if email is already verified
        if (this.emailVerified) {
            throw new IllegalStateException("Email is already verified");
        }
        
        // Check verification code
        if (!command.getNormalizedVerificationCode().equals(this.emailVerificationCode)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        
        // Check if verification code is expired
        if (this.emailVerificationExpiry != null && Instant.now().isAfter(this.emailVerificationExpiry)) {
            throw new IllegalArgumentException("Verification code has expired");
        }
    }

    /**
     * Update email address
     */
    private void updateEmail(Email newEmail) {
        if (!newEmail.equals(this.email)) {
            this.email = newEmail;
            this.emailVerified = false; // Reset verification status
            this.emailVerificationCode = generateVerificationCode();
            this.emailVerificationExpiry = Instant.now().plusSeconds(24 * 60 * 60);
        }
    }

    /**
     * Update personal information
     */
    private void updatePersonalInfo(UpdateMemberProfileCommand command) {
        if (command.getNewFirstName() != null) {
            this.firstName = command.getNewFirstName();
        }
        
        if (command.getNewLastName() != null) {
            this.lastName = command.getNewLastName();
        }
        
        if (command.getNewDateOfBirth() != null) {
            this.dateOfBirth = command.getNewDateOfBirth();
        }
        
        if (command.getNewGender() != null) {
            this.gender = command.getNewGender();
        }
    }

    /**
     * Update address information
     */
    private void updateAddressInfo(UpdateMemberProfileCommand command) {
        if (command.getNewAddress() != null) {
            this.address = command.getNewAddress();
        }
        
        if (command.getNewCity() != null) {
            this.city = command.getNewCity();
        }
        
        if (command.getNewCountry() != null) {
            this.country = command.getNewCountry();
        }
        
        if (command.getNewPostalCode() != null) {
            this.postalCode = command.getNewPostalCode();
        }
    }

    /**
     * Update custom attributes
     */
    private void updateCustomAttributes(Map<String, String> attributesToUpdate) {
        if (this.customAttributes == null) {
            this.customAttributes = new HashMap<>();
        }
        
        this.customAttributes.putAll(attributesToUpdate);
    }

    /**
     * Generate verification code
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if member has complete profile
     */
    public boolean hasCompleteProfile() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null &&
               phone != null &&
               dateOfBirth != null &&
               gender != null && !gender.trim().isEmpty();
    }

    /**
     * Check if member is fully verified
     */
    public boolean isFullyVerified() {
        return emailVerified && phoneVerified;
    }

    /**
     * Check if member can participate in loyalty program
     */
    public boolean canParticipate() {
        return status.canParticipate() && isFullyVerified();
    }
}