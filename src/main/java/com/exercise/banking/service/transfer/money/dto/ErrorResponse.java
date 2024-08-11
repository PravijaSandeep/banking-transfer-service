package com.exercise.banking.service.transfer.money.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	
	@NotNull(message = "The timestmp is required")
	@Schema(description = "Timestamp", example = "2024-08-11T17:26:13.58163")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private final LocalDateTime timestamp; 
	
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
		this.timestamp = LocalDateTime.now();
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
