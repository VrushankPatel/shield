package com.shield.common.exception;

import com.shield.audit.service.SystemLogService;
import com.shield.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final SystemLogService systemLogService;

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .orElse("Validation failed");

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""));

        systemLogService.logWarn(
                GlobalExceptionHandler.class.getSimpleName(),
                message,
                ex,
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        systemLogService.logWarn(GlobalExceptionHandler.class.getSimpleName(), ex.getMessage(), ex, request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        systemLogService.logWarn(GlobalExceptionHandler.class.getSimpleName(), ex.getMessage(), ex, request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        systemLogService.logWarn(GlobalExceptionHandler.class.getSimpleName(), ex.getMessage(), ex, request.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        systemLogService.logWarn(GlobalExceptionHandler.class.getSimpleName(), "Access denied", ex, request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        systemLogService.logError(GlobalExceptionHandler.class.getSimpleName(), ex.getMessage(), ex, request.getRequestURI());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
        return ResponseEntity.status(status).body(response);
    }
}
