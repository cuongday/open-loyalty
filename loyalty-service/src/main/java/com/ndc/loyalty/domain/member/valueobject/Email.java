package com.ndc.loyalty.domain.member.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Email Value Object
 * 
 * Immutable email address với validation logic
 * Ensures email format correctness
 * 
 * @author NDC Team
 */
@Getter
@EqualsAndHashCode
@ToString
public class Email implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // RFC 5322 compliant email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    @NotBlank(message = "Email cannot be blank")
    private final String address;

    /**
     * Private constructor để ensure immutability
     */
    private Email(String address) {
        this.address = address.toLowerCase().trim();
    }

    /**
     * Tạo Email từ string address
     */
    public static Email of(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
        
        String normalizedAddress = address.toLowerCase().trim();
        
        if (!isValidEmail(normalizedAddress)) {
            throw new IllegalArgumentException("Invalid email format: " + address);
        }
        
        return new Email(normalizedAddress);
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String normalizedEmail = email.toLowerCase().trim();
        
        // Check length constraints
        if (normalizedEmail.length() > 254) {
            return false;
        }
        
        // Check local part length (before @)
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex > 0 && normalizedEmail.substring(0, atIndex).length() > 64) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }

    /**
     * Get domain part của email
     */
    public String getDomain() {
        int atIndex = address.indexOf('@');
        return atIndex > 0 ? address.substring(atIndex + 1) : "";
    }

    /**
     * Get local part của email (before @)
     */
    public String getLocalPart() {
        int atIndex = address.indexOf('@');
        return atIndex > 0 ? address.substring(0, atIndex) : address;
    }

    /**
     * Check if email belongs to specific domain
     */
    public boolean belongsToDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }
        return getDomain().equalsIgnoreCase(domain.trim());
    }

    /**
     * Mask email for privacy (e.g., jo**@example.com)
     */
    public String mask() {
        String localPart = getLocalPart();
        String domain = getDomain();
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "*@" + domain;
        } else {
            return localPart.substring(0, 2) + "**@" + domain;
        }
    }

    /**
     * Check if email is from corporate domain
     */
    public boolean isCorporateEmail() {
        String domain = getDomain();
        return !isPersonalEmailDomain(domain);
    }

    /**
     * Check if domain is known personal email provider
     */
    private boolean isPersonalEmailDomain(String domain) {
        String[] personalDomains = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
            "live.com", "aol.com", "icloud.com", "protonmail.com",
            "ymail.com", "rocketmail.com", "mail.com"
        };
        
        for (String personalDomain : personalDomains) {
            if (domain.equalsIgnoreCase(personalDomain)) {
                return true;
            }
        }
        return false;
    }
}