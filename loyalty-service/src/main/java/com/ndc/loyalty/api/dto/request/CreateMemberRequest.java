package com.ndc.loyalty.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;

/**
 * Create Member Request DTO
 * 
 * Request object cho tạo member mới
 * 
 * @author NDC Team
 */
@Data
@Schema(description = "Request to create a new member")
public class CreateMemberRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Member email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(description = "Member first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(description = "Member last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+84|84|0)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$", 
             message = "Phone number must be a valid Vietnamese mobile number")
    @Schema(description = "Member phone number", example = "0912345678")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Member date of birth", example = "1990-01-15")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Schema(description = "Member gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
    private String gender;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    @Schema(description = "Member address", example = "123 Main Street")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "Member city", example = "Ho Chi Minh City")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Schema(description = "Member country", example = "Vietnam")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Schema(description = "Member postal code", example = "70000")
    private String postalCode;

    @Size(max = 100, message = "Referral code must not exceed 100 characters")
    @Schema(description = "Referral code from existing member", example = "REF123456")
    private String referralCode;

    @Size(max = 100, message = "Source channel must not exceed 100 characters")
    @Schema(description = "Registration source channel", example = "MOBILE_APP", 
            allowableValues = {"WEBSITE", "MOBILE_APP", "STORE", "CALL_CENTER", "SOCIAL_MEDIA"})
    private String sourceChannel;

    @Schema(description = "Custom member attributes")
    private Map<String, String> customAttributes;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Additional notes", example = "VIP customer")
    private String notes;
}