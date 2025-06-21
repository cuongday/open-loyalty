package com.ndc.loyalty.domain.member.command;

import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import com.ndc.loyalty.domain.member.valueobject.PhoneNumber;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;

/**
 * Update Member Profile Command
 * 
 * Command để update thông tin profile của member
 * Chỉ update các fields được provide (không null)
 * 
 * @author NDC Team
 */
@Data
@Builder
@EqualsAndHashCode
public class UpdateMemberProfileCommand {

    @TargetAggregateIdentifier
    @NotNull(message = "Member ID cannot be null")
    private final MemberId memberId;

    private final Email newEmail;

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private final String newFirstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private final String newLastName;

    private final PhoneNumber newPhone;

    @Past(message = "Date of birth must be in the past")
    private final LocalDate newDateOfBirth;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private final String newGender;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private final String newAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private final String newCity;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private final String newCountry;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private final String newPostalCode;

    @Builder.Default
    private final Map<String, String> customAttributesToUpdate = Map.of();

    @Builder.Default
    private final Instant timestamp = Instant.now();

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private final String notes;

    private final String updatedBy;

    /**
     * Static factory method để update email
     */
    public static UpdateMemberProfileCommand updateEmail(MemberId memberId, Email newEmail) {
        return UpdateMemberProfileCommand.builder()
                .memberId(memberId)
                .newEmail(newEmail)
                .build();
    }

    /**
     * Static factory method để update phone
     */
    public static UpdateMemberProfileCommand updatePhone(MemberId memberId, PhoneNumber newPhone) {
        return UpdateMemberProfileCommand.builder()
                .memberId(memberId)
                .newPhone(newPhone)
                .build();
    }

    /**
     * Static factory method để update name
     */
    public static UpdateMemberProfileCommand updateName(MemberId memberId, String firstName, String lastName) {
        return UpdateMemberProfileCommand.builder()
                .memberId(memberId)
                .newFirstName(firstName)
                .newLastName(lastName)
                .build();
    }

    /**
     * Check if command has any updates
     */
    public boolean hasUpdates() {
        return newEmail != null ||
               newFirstName != null ||
               newLastName != null ||
               newPhone != null ||
               newDateOfBirth != null ||
               newGender != null ||
               newAddress != null ||
               newCity != null ||
               newCountry != null ||
               newPostalCode != null ||
               (customAttributesToUpdate != null && !customAttributesToUpdate.isEmpty());
    }

    /**
     * Check if email is being updated
     */
    public boolean isEmailUpdate() {
        return newEmail != null;
    }

    /**
     * Check if phone is being updated
     */
    public boolean isPhoneUpdate() {
        return newPhone != null;
    }

    /**
     * Check if personal info is being updated
     */
    public boolean isPersonalInfoUpdate() {
        return newFirstName != null || newLastName != null || 
               newDateOfBirth != null || newGender != null;
    }

    /**
     * Check if address info is being updated
     */
    public boolean isAddressUpdate() {
        return newAddress != null || newCity != null || 
               newCountry != null || newPostalCode != null;
    }

    /**
     * Get new full name nếu có update
     */
    public String getNewFullName() {
        if (newFirstName != null && newLastName != null) {
            return newFirstName + " " + newLastName;
        }
        return null;
    }

    /**
     * Validate command data
     */
    public void validate() {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID is required");
        }

        if (!hasUpdates()) {
            throw new IllegalArgumentException("At least one field must be updated");
        }

        // Validate date of birth if provided
        if (newDateOfBirth != null) {
            if (newDateOfBirth.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }
            
            LocalDate maxAge = LocalDate.now().minusYears(120);
            if (newDateOfBirth.isBefore(maxAge)) {
                throw new IllegalArgumentException("Date of birth is too far in the past");
            }
        }

        // Validate gender if provided
        if (newGender != null && !newGender.trim().isEmpty()) {
            String normalizedGender = newGender.trim().toUpperCase();
            if (!normalizedGender.equals("MALE") && 
                !normalizedGender.equals("FEMALE") && 
                !normalizedGender.equals("OTHER")) {
                throw new IllegalArgumentException("Gender must be MALE, FEMALE, or OTHER");
            }
        }

        // Validate names if provided
        if (newFirstName != null && newFirstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (newLastName != null && newLastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }
}