package com.exercise.banking.service.transfer.money.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.exercise.banking.service.transfer.money.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    
    public GlobalExceptionHandler(MessageSource messageSource) {
    	this.messageSource = messageSource;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        String message = messageSource.getMessage("error.account.notfound", null, LocaleContextHolder.getLocale());
        logError(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getRequestId(),HttpStatus.NOT_FOUND, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex) {
        String message = messageSource.getMessage("error.insufficient.funds", null, LocaleContextHolder.getLocale());
        logError(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getRequestId(),HttpStatus.BAD_REQUEST, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PayeeNotRegisteredException.class)
    public ResponseEntity<ErrorResponse> handlePayeeNotRegisteredException(PayeeNotRegisteredException ex) {
        String message = messageSource.getMessage("error.payee.notregistered", null, LocaleContextHolder.getLocale());
        logError(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getRequestId(),HttpStatus.NOT_FOUND, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        String message = messageSource.getMessage("error.unexpected", null, LocaleContextHolder.getLocale());
        logger.error(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    	String message = messageSource.getMessage("error.validation.failed", null, LocaleContextHolder.getLocale());
        logger.error(message, ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message, errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    	String message = messageSource.getMessage("error.validation.json", null, LocaleContextHolder.getLocale());
    	logger.error(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
    	String message = messageSource.getMessage("error.validation.content.type", null, LocaleContextHolder.getLocale());
        logger.error(message, ex);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    /**
     * Logs the error in logs with request Id
     * @param msg
     * @param exception
     */
    private void logError(String msg, BaseTransferException ex) {
    	logger.error("Request ID: {} Message : {}",ex.getRequestId(),msg, ex);
    }
}
