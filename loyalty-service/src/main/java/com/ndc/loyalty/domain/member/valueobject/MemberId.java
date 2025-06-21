package com.ndc.loyalty.domain.member.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.axonframework.common.IdentifierFactory;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

/**
 * Member Identifier Value Object
 * 
 * Immutable identifier cho Member aggregate
 * Sử dụng UUID cho unique identification
 * 
 * @author NDC Team
 */
@Getter
@EqualsAndHashCode
@ToString
public class MemberId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @NotBlank(message = "Member ID cannot be blank")
    private final String identifier;

    /**
     * Private constructor để ensure immutability
     */
    private MemberId(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Tạo MemberId mới với UUID
     */
    public static MemberId generate() {
        return new MemberId(IdentifierFactory.getInstance().generateIdentifier());
    }

    /**
     * Tạo MemberId từ existing identifier
     */
    public static MemberId of(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Member identifier cannot be null or empty");
        }
        return new MemberId(identifier);
    }

    /**
     * Tạo MemberId từ UUID
     */
    public static MemberId of(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return new MemberId(uuid.toString());
    }

    /**
     * Validate identifier format (UUID)
     */
    public boolean isValid() {
        try {
            UUID.fromString(identifier);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Convert to UUID
     */
    public UUID toUUID() {
        return UUID.fromString(identifier);
    }
}