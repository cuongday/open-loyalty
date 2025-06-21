package com.ndc.loyalty.domain.member.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Phone Number Value Object
 * 
 * Immutable phone number với validation logic
 * Supports multiple international formats
 * 
 * @author NDC Team
 */
@Getter
@EqualsAndHashCode
@ToString
public class PhoneNumber implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Vietnam phone number patterns
    private static final Pattern VN_MOBILE_PATTERN = Pattern.compile(
        "^(\\+84|84|0)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$"
    );
    
    // International phone number pattern (E.164 format)
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile(
        "^\\+[1-9]\\d{1,14}$"
    );
    
    @NotBlank(message = "Phone number cannot be blank")
    private final String number;
    private final String countryCode;
    private final String nationalNumber;

    /**
     * Private constructor để ensure immutability
     */
    private PhoneNumber(String number, String countryCode, String nationalNumber) {
        this.number = number;
        this.countryCode = countryCode;
        this.nationalNumber = nationalNumber;
    }

    /**
     * Tạo PhoneNumber từ string number
     */
    public static PhoneNumber of(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        String normalizedNumber = normalizePhoneNumber(phoneNumber);
        
        if (!isValidPhoneNumber(normalizedNumber)) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
        
        // Parse country code và national number
        String[] parsed = parsePhoneNumber(normalizedNumber);
        String countryCode = parsed[0];
        String nationalNumber = parsed[1];
        
        return new PhoneNumber(normalizedNumber, countryCode, nationalNumber);
    }

    /**
     * Normalize phone number by removing spaces, dashes, parentheses
     */
    private static String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "").trim();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String normalized = normalizePhoneNumber(phoneNumber);
        
        // Check length constraints
        if (normalized.length() < 8 || normalized.length() > 15) {
            return false;
        }
        
        // Check Vietnam mobile pattern
        if (VN_MOBILE_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        
        // Check international pattern
        return INTERNATIONAL_PATTERN.matcher(normalized).matches();
    }

    /**
     * Parse phone number thành country code và national number
     */
    private static String[] parsePhoneNumber(String phoneNumber) {
        String countryCode = "";
        String nationalNumber = phoneNumber;
        
        if (phoneNumber.startsWith("+84")) {
            countryCode = "+84";
            nationalNumber = phoneNumber.substring(3);
        } else if (phoneNumber.startsWith("84")) {
            countryCode = "+84";
            nationalNumber = phoneNumber.substring(2);
        } else if (phoneNumber.startsWith("0")) {
            countryCode = "+84";
            nationalNumber = phoneNumber.substring(1);
        } else if (phoneNumber.startsWith("+")) {
            // Extract country code for international numbers
            for (int i = 1; i <= 4 && i < phoneNumber.length(); i++) {
                String potential = phoneNumber.substring(0, i + 1);
                if (isKnownCountryCode(potential)) {
                    countryCode = potential;
                    nationalNumber = phoneNumber.substring(i + 1);
                    break;
                }
            }
        }
        
        return new String[]{countryCode, nationalNumber};
    }

    /**
     * Check if country code is known
     */
    private static boolean isKnownCountryCode(String code) {
        // Common country codes
        String[] knownCodes = {
            "+1", "+7", "+20", "+27", "+30", "+31", "+32", "+33", "+34", "+36", "+39", 
            "+40", "+41", "+43", "+44", "+45", "+46", "+47", "+48", "+49", "+51", 
            "+52", "+53", "+54", "+55", "+56", "+57", "+58", "+60", "+61", "+62", 
            "+63", "+64", "+65", "+66", "+81", "+82", "+84", "+86", "+90", "+91", 
            "+92", "+93", "+94", "+95", "+98"
        };
        
        for (String knownCode : knownCodes) {
            if (code.equals(knownCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Format phone number theo E.164 format
     */
    public String toE164Format() {
        if (countryCode.isEmpty()) {
            return "+" + number;
        }
        return countryCode + nationalNumber;
    }

    /**
     * Format phone number cho display (Vietnam format)
     */
    public String toDisplayFormat() {
        if (isVietnameseNumber()) {
            if (nationalNumber.length() == 9) {
                // Mobile format: 0xx xxx xxxx
                return "0" + nationalNumber.substring(0, 2) + " " + 
                       nationalNumber.substring(2, 5) + " " + 
                       nationalNumber.substring(5);
            }
        }
        
        // Default format
        return number;
    }

    /**
     * Mask phone number for privacy
     */
    public String mask() {
        if (isVietnameseNumber() && nationalNumber.length() == 9) {
            return "0" + nationalNumber.substring(0, 2) + " *** " + 
                   nationalNumber.substring(6);
        }
        
        // Default masking
        if (number.length() > 4) {
            return number.substring(0, 2) + "***" + 
                   number.substring(number.length() - 2);
        }
        
        return "***";
    }

    /**
     * Check if this is Vietnamese phone number
     */
    public boolean isVietnameseNumber() {
        return "+84".equals(countryCode) || 
               VN_MOBILE_PATTERN.matcher("0" + nationalNumber).matches();
    }

    /**
     * Check if this is mobile number
     */
    public boolean isMobileNumber() {
        if (isVietnameseNumber()) {
            return VN_MOBILE_PATTERN.matcher("0" + nationalNumber).matches();
        }
        
        // For international numbers, assume mobile if length > 10
        return nationalNumber.length() > 7;
    }

    /**
     * Get operator info for Vietnamese mobile numbers
     */
    public String getVietnameseOperator() {
        if (!isVietnameseNumber() || !isMobileNumber()) {
            return "UNKNOWN";
        }
        
        String prefix = nationalNumber.substring(0, 2);
        
        switch (prefix) {
            case "32": case "33": case "34": case "35": case "36": case "37": case "38": case "39":
                return "VIETTEL";
            case "56": case "58": case "59":
                return "VIETNAMOBILE";
            case "70": case "76": case "77": case "78": case "79":
                return "GMOBILE";
            case "81": case "82": case "83": case "84": case "85": case "88":
                return "VINAPHONE";
            case "86": case "87": case "89":
                return "VIETTEL";
            case "90": case "93": case "94": case "96": case "97": case "98": case "99":
                return "MOBIFONE";
            case "91": case "92": case "95":
                return "VINAPHONE";
            default:
                return "UNKNOWN";
        }
    }
}