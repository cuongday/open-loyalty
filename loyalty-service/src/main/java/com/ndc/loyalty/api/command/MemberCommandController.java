package com.ndc.loyalty.api.command;

import com.ndc.loyalty.api.dto.request.CreateMemberRequest;
import com.ndc.loyalty.api.dto.request.UpdateMemberProfileRequest;
import com.ndc.loyalty.api.dto.request.VerifyEmailRequest;
import com.ndc.loyalty.api.dto.response.CommandResponse;
import com.ndc.loyalty.domain.member.command.CreateMemberCommand;
import com.ndc.loyalty.domain.member.command.UpdateMemberProfileCommand;
import com.ndc.loyalty.domain.member.command.VerifyMemberEmailCommand;
import com.ndc.loyalty.domain.member.valueobject.Email;
import com.ndc.loyalty.domain.member.valueobject.MemberId;
import com.ndc.loyalty.domain.member.valueobject.PhoneNumber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

/**
 * Member Command Controller
 * 
 * REST Controller cho Member command operations (write side)
 * Implements CQRS pattern - chỉ handle write operations
 * 
 * @author NDC Team
 */
@RestController
@RequestMapping("/api/members/commands")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Member Commands", description = "Member write operations API")
public class MemberCommandController {

    private final CommandGateway commandGateway;

    /**
     * Create new member
     */
    @PostMapping("/create")
    @Operation(summary = "Create new member", description = "Create a new member in the loyalty program")
    public ResponseEntity<CommandResponse> createMember(
            @Valid @RequestBody CreateMemberRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating new member with email: {}", request.getEmail());
        
        try {
            // Generate member ID
            MemberId memberId = MemberId.generate();
            
            // Build command
            CreateMemberCommand command = CreateMemberCommand.builder()
                    .memberId(memberId)
                    .email(Email.of(request.getEmail()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(PhoneNumber.of(request.getPhone()))
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .referralCode(request.getReferralCode())
                    .sourceChannel(request.getSourceChannel())
                    .customAttributes(request.getCustomAttributes())
                    .notes(request.getNotes())
                    .build();
            
            // Send command
            CompletableFuture<Object> future = commandGateway.send(command);
            
            // Wait for completion (with timeout)
            future.get();
            
            CommandResponse response = CommandResponse.builder()
                    .commandId(memberId.getIdentifier())
                    .status("SUCCESS")
                    .message("Member created successfully")
                    .resourceId(memberId.getIdentifier())
                    .build();
            
            log.info("Member created successfully with ID: {}", memberId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for creating member: {}", e.getMessage());
            
            CommandResponse response = CommandResponse.builder()
                    .status("VALIDATION_ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error creating member", e);
            
            CommandResponse response = CommandResponse.builder()
                    .status("ERROR")
                    .message("Internal server error occurred")
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update member profile
     */
    @PutMapping("/{memberId}/update-profile")
    @Operation(summary = "Update member profile", description = "Update member profile information")
    public ResponseEntity<CommandResponse> updateMemberProfile(
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberProfileRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Updating member profile for ID: {}", memberId);
        
        try {
            // Build command
            UpdateMemberProfileCommand.UpdateMemberProfileCommandBuilder builder = 
                    UpdateMemberProfileCommand.builder()
                            .memberId(MemberId.of(memberId))
                            .updatedBy(getUpdatedBy(httpRequest));
            
            // Set fields that are being updated
            if (request.getNewEmail() != null) {
                builder.newEmail(Email.of(request.getNewEmail()));
            }
            
            if (request.getNewFirstName() != null) {
                builder.newFirstName(request.getNewFirstName());
            }
            
            if (request.getNewLastName() != null) {
                builder.newLastName(request.getNewLastName());
            }
            
            if (request.getNewPhone() != null) {
                builder.newPhone(PhoneNumber.of(request.getNewPhone()));
            }
            
            if (request.getNewDateOfBirth() != null) {
                builder.newDateOfBirth(request.getNewDateOfBirth());
            }
            
            if (request.getNewGender() != null) {
                builder.newGender(request.getNewGender());
            }
            
            if (request.getNewAddress() != null) {
                builder.newAddress(request.getNewAddress());
            }
            
            if (request.getNewCity() != null) {
                builder.newCity(request.getNewCity());
            }
            
            if (request.getNewCountry() != null) {
                builder.newCountry(request.getNewCountry());
            }
            
            if (request.getNewPostalCode() != null) {
                builder.newPostalCode(request.getNewPostalCode());
            }
            
            if (request.getCustomAttributesToUpdate() != null) {
                builder.customAttributesToUpdate(request.getCustomAttributesToUpdate());
            }
            
            if (request.getNotes() != null) {
                builder.notes(request.getNotes());
            }
            
            UpdateMemberProfileCommand command = builder.build();
            
            // Send command
            CompletableFuture<Object> future = commandGateway.send(command);
            future.get();
            
            CommandResponse response = CommandResponse.builder()
                    .commandId(memberId)
                    .status("SUCCESS")
                    .message("Member profile updated successfully")
                    .resourceId(memberId)
                    .build();
            
            log.info("Member profile updated successfully for ID: {}", memberId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for updating member profile: {}", e.getMessage());
            
            CommandResponse response = CommandResponse.builder()
                    .status("VALIDATION_ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error updating member profile for ID: {}", memberId, e);
            
            CommandResponse response = CommandResponse.builder()
                    .status("ERROR")
                    .message("Internal server error occurred")
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify member email
     */
    @PostMapping("/{memberId}/verify-email")
    @Operation(summary = "Verify member email", description = "Verify member email address with verification code")
    public ResponseEntity<CommandResponse> verifyMemberEmail(
            @PathVariable String memberId,
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Verifying email for member ID: {}", memberId);
        
        try {
            // Build command
            VerifyMemberEmailCommand command = VerifyMemberEmailCommand.createWithContext(
                    MemberId.of(memberId),
                    request.getVerificationCode(),
                    getVerifiedBy(httpRequest),
                    getClientIpAddress(httpRequest),
                    httpRequest.getHeader("User-Agent")
            );
            
            // Send command
            CompletableFuture<Object> future = commandGateway.send(command);
            future.get();
            
            CommandResponse response = CommandResponse.builder()
                    .commandId(memberId)
                    .status("SUCCESS")
                    .message("Email verified successfully")
                    .resourceId(memberId)
                    .build();
            
            log.info("Email verified successfully for member ID: {}", memberId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid verification code for member ID: {}", memberId);
            
            CommandResponse response = CommandResponse.builder()
                    .status("VALIDATION_ERROR")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (IllegalStateException e) {
            log.warn("Invalid state for email verification for member ID: {}", memberId);
            
            CommandResponse response = CommandResponse.builder()
                    .status("INVALID_STATE")
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Error verifying email for member ID: {}", memberId, e);
            
            CommandResponse response = CommandResponse.builder()
                    .status("ERROR")
                    .message("Internal server error occurred")
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get updated by information từ request
     */
    private String getUpdatedBy(HttpServletRequest request) {
        // In real implementation, extract from JWT token or session
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // Extract user info from JWT token
            return "USER_FROM_TOKEN"; // Placeholder
        }
        return "SYSTEM";
    }

    /**
     * Get verified by information từ request
     */
    private String getVerifiedBy(HttpServletRequest request) {
        return getUpdatedBy(request);
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}