package com.ndc.loyalty.domain.member.event;

import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import com.ndc.loyalty.domain.member.valueobject.PhoneNumber;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;

/**
 * Member Created Event
 * 
 * Domain event được emit khi member mới được tạo
 * Contains đầy đủ thông tin của member được tạo
 * 
 * @author NDC Team
 */
@Data
@Builder
@EqualsAndHashCode
public class MemberCreatedEvent {

    private final MemberId memberId;
    private final Email email;
    private final String firstName;
    private final String lastName;
    private final PhoneNumber phone;
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String address;
    private final String city;
    private final String country;
    private final String postalCode;
    private final String referralCode;
    private final String sourceChannel;
    
    @Builder.Default
    private final Map<String, String> customAttributes = Map.of();
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    private final String notes;

    /**
     * Static factory method từ basic info
     */
    public static MemberCreatedEvent create(MemberId memberId, Email email, 
                                          String firstName, String lastName, 
                                          PhoneNumber phone) {
        return MemberCreatedEvent.builder()
                .memberId(memberId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .build();
    }

    /**
     * Static factory method từ complete info
     */
    public static MemberCreatedEvent createComplete(MemberId memberId, Email email,
                                                  String firstName, String lastName,
                                                  PhoneNumber phone, LocalDate dateOfBirth,
                                                  String gender, String address,
                                                  String city, String country,
                                                  String postalCode, String referralCode,
                                                  String sourceChannel,
                                                  Map<String, String> customAttributes,
                                                  String notes) {
        return MemberCreatedEvent.builder()
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
                .referralCode(referralCode)
                .sourceChannel(sourceChannel)
                .customAttributes(customAttributes != null ? customAttributes : Map.of())
                .notes(notes)
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
     * Check if member came from referral
     */
    public boolean isFromReferral() {
        return referralCode != null && !referralCode.trim().isEmpty();
    }

    /**
     * Check if member has custom attributes
     */
    public boolean hasCustomAttributes() {
        return customAttributes != null && !customAttributes.isEmpty();
    }

    /**
     * Get custom attribute value
     */
    public String getCustomAttribute(String key) {
        if (customAttributes == null || key == null) {
            return null;
        }
        return customAttributes.get(key);
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
     * Get formatted address
     */
    public String getFormattedAddress() {
        StringBuilder addressBuilder = new StringBuilder();
        
        if (address != null && !address.trim().isEmpty()) {
            addressBuilder.append(address.trim());
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(city.trim());
        }
        
        if (country != null && !country.trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(country.trim());
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(postalCode.trim());
        }
        
        return addressBuilder.length() > 0 ? addressBuilder.toString() : null;
    }

    /**
     * Event type for routing
     */
    public String getEventType() {
        return "MemberCreated";
    }

    /**
     * Get event version for compatibility
     */
    public String getEventVersion() {
        return "1.0";
    }
}