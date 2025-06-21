package com.ndc.loyalty.domain.member.command;

import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import com.ndc.loyalty.domain.member.valueobject.PhoneNumber;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;

/**
 * Create Member Command
 * 
 * Command để tạo member mới trong loyalty system
 * Follows CQRS pattern với Axon Framework
 * 
 * @author NDC Team
 */
@Data
@Builder
@EqualsAndHashCode
public class CreateMemberCommand {

    @TargetAggregateIdentifier
    @NotNull(message = "Member ID cannot be null")
    private final MemberId memberId;

    @NotNull(message = "Email cannot be null")
    private final Email email;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private final String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private final String lastName;

    @NotNull(message = "Phone number cannot be null")
    private final PhoneNumber phone;

    @Past(message = "Date of birth must be in the past")
    private final LocalDate dateOfBirth;

    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private final String gender;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private final String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private final String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private final String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private final String postalCode;

    @Size(max = 100, message = "Referral code must not exceed 100 characters")
    private final String referralCode;

    @Size(max = 100, message = "Source channel must not exceed 100 characters")
    private final String sourceChannel;

    @Builder.Default
    private final Map<String, String> customAttributes = Map.of();

    @Builder.Default
    private final Instant timestamp = Instant.now();

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private final String notes;

    /**
     * Static factory method với mandatory fields
     */
    public static CreateMemberCommand create(MemberId memberId, Email email, 
                                           String firstName, String lastName, 
                                           PhoneNumber phone) {
        return CreateMemberCommand.builder()
                .memberId(memberId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .build();
    }

    /**
     * Static factory method với đầy đủ thông tin
     */
    public static CreateMemberCommand createComplete(MemberId memberId, Email email,
                                                   String firstName, String lastName,
                                                   PhoneNumber phone, LocalDate dateOfBirth,
                                                   String gender, String address,
                                                   String city, String country,
                                                   String postalCode) {
        return CreateMemberCommand.builder()
                .memberId(memberId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(address)
                .city(city)
                .country(country)
                .postalCode(postalCode)
                .build();
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
     * Check if member is adult (18+ years old)
     */
    public boolean isAdult() {
        if (dateOfBirth == null) {
            return false;
        }
        return LocalDate.now().minusYears(18).isAfter(dateOfBirth) ||
               LocalDate.now().minusYears(18).isEqual(dateOfBirth);
    }

    /**
     * Get member age
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Validate command data
     */
    public void validate() {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID is required");
        }
        
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        
        if (phone == null) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        // Validate age if date of birth is provided
        if (dateOfBirth != null) {
            if (dateOfBirth.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }
            
            LocalDate maxAge = LocalDate.now().minusYears(120);
            if (dateOfBirth.isBefore(maxAge)) {
                throw new IllegalArgumentException("Date of birth is too far in the past");
            }
        }
        
        // Validate gender
        if (gender != null && !gender.trim().isEmpty()) {
            String normalizedGender = gender.trim().toUpperCase();
            if (!normalizedGender.equals("MALE") && 
                !normalizedGender.equals("FEMALE") && 
                !normalizedGender.equals("OTHER")) {
                throw new IllegalArgumentException("Gender must be MALE, FEMALE, or OTHER");
            }
        }
    }
}