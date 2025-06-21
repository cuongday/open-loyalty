package com.ndc.loyalty.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Command Response DTO
 * 
 * Standard response object cho tất cả command operations
 * 
 * @author NDC Team
 */
@Data
@Builder
@Schema(description = "Standard command response")
public class CommandResponse {

    @Schema(description = "Command execution ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String commandId;

    @Schema(description = "Command execution status", example = "SUCCESS", 
            allowableValues = {"SUCCESS", "VALIDATION_ERROR", "INVALID_STATE", "ERROR"})
    private String status;

    @Schema(description = "Response message", example = "Member created successfully")
    private String message;

    @Schema(description = "Resource ID that was affected", example = "123e4567-e89b-12d3-a456-426614174000")
    private String resourceId;

    @Builder.Default
    @Schema(description = "Response timestamp")
    private Instant timestamp = Instant.now();

    @Schema(description = "Additional response data")
    private Object data;

    @Schema(description = "Error details if any")
    private String errorDetails;

    /**
     * Create success response
     */
    public static CommandResponse success(String commandId, String message) {
        return CommandResponse.builder()
                .commandId(commandId)
                .status("SUCCESS")
                .message(message)
                .build();
    }

    /**
     * Create success response with resource ID
     */
    public static CommandResponse success(String commandId, String message, String resourceId) {
        return CommandResponse.builder()
                .commandId(commandId)
                .status("SUCCESS")
                .message(message)
                .resourceId(resourceId)
                .build();
    }

    /**
     * Create validation error response
     */
    public static CommandResponse validationError(String message) {
        return CommandResponse.builder()
                .status("VALIDATION_ERROR")
                .message(message)
                .build();
    }

    /**
     * Create error response
     */
    public static CommandResponse error(String message) {
        return CommandResponse.builder()
                .status("ERROR")
                .message(message)
                .build();
    }

    /**
     * Create error response with details
     */
    public static CommandResponse error(String message, String errorDetails) {
        return CommandResponse.builder()
                .status("ERROR")
                .message(message)
                .errorDetails(errorDetails)
                .build();
    }

    /**
     * Check if command was successful
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if command had validation error
     */
    public boolean isValidationError() {
        return "VALIDATION_ERROR".equals(status);
    }

    /**
     * Check if command had error
     */
    public boolean isError() {
        return "ERROR".equals(status);
    }
}