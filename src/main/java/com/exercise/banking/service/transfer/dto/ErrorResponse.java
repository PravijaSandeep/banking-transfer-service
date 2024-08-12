package com.exercise.banking.service.transfer.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {
	
	@Schema(description = "Request ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	private final UUID requestId;
	
	@NotNull(message = "HttpStatus code is required")
	@Schema(description = "HttpStatus code", example = "BAD_REQUEST")
	private final HttpStatus status; // HTTP Status code
	
	@NotNull(message = "The error message is required")
	@Schema(description = "Error message", example = "Insufficient funds in account")
	private final String message; // The error reason
	
	@NotNull(message = "The timestamp is required")
    @Schema(description = "Timestamp in ISO 8601 format", example = "2024-08-11T17:26:13.581630Z")
    private final String timestamp; 
	
	@Schema(description = "Validation errors", example = """
			\t{
			\t\t"validationErrors": {
			\t\t\t"payerAccNumber": "Payer account number is required"
			\t\t}
			\t}
			\t""")
	private final Map<String, String> validationErrors; // includes all validation errors

	public ErrorResponse(UUID requestId, HttpStatus status, String message, Map<String, String> validationErrors) {
		this.requestId = requestId;
		this.status = status;
		this.message = message;
		this.timestamp = Instant.now().toString();
		this.validationErrors = validationErrors;
	}
	
	public ErrorResponse(HttpStatus status, String message, Map<String, String> validationErrors) {
		this(null, status, message, validationErrors);
	}

	public ErrorResponse(UUID requestId,HttpStatus status, String message) {
		this(requestId, status, message, null);
	}
	
	public ErrorResponse(HttpStatus status, String message) {
		this(null, status, message, null);
	}

}
