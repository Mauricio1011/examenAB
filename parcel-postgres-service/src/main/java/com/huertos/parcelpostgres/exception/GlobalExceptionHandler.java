package com.huertos.parcelpostgres.exception;

import com.huertos.parcelpostgres.dto.ApiErrorResponse;
import com.huertos.parcelpostgres.dto.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ParcelNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ParcelNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Parcela no encontrada", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateParcelException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateParcelException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Parcela duplicada", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String raw = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
                ? ex.getMostSpecificCause().getMessage().toLowerCase()
                : "";
        if (raw.contains("uk_parcel_name") || raw.contains("duplicate") || raw.contains("unique")) {
            return build(HttpStatus.CONFLICT, "Parcela duplicada", ErrorMessages.PARCEL_DUPLICATE_GENERIC, request, List.of());
        }
        log.warn("Violación de integridad al procesar {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ErrorMessages.PARCEL_INTEGRITY, request, List.of());
    }

    @ExceptionHandler(InvalidParcelDataException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalid(InvalidParcelDataException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request, ex.getFieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fields.add(new FieldErrorDetail(error.getField(), defaultMessage(error))));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                fields.add(new FieldErrorDetail(error.getObjectName(), defaultMessage(error))));
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ErrorMessages.PARCEL_DATA_INVALID, request, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fields = ex.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDetail(lastSegment(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ErrorMessages.PARCEL_DATA_INVALID, request, fields);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fields = ex.getAllErrors().stream()
                .map(error -> new FieldErrorDetail(resolveField(error), defaultMessage(error)))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ErrorMessages.PARCEL_DATA_INVALID, request, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", resolveUnreadableMessage(ex), request, List.of());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleMissingRoute(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ErrorMessages.RESOURCE_NOT_FOUND, request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado al procesar {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", ErrorMessages.INTERNAL_ERROR, request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error, String message,
                                                   HttpServletRequest request, List<FieldErrorDetail> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }

    private String defaultMessage(MessageSourceResolvable error) {
        return error.getDefaultMessage() == null ? ErrorMessages.PARCEL_DATA_INVALID : error.getDefaultMessage();
    }

    private String resolveField(MessageSourceResolvable error) {
        if (error instanceof org.springframework.validation.FieldError fieldError) {
            return fieldError.getField();
        }
        String[] codes = error.getCodes();
        if (codes != null && codes.length > 0) {
            return lastSegment(codes[0]);
        }
        return "request";
    }

    private String lastSegment(String value) {
        int index = value.lastIndexOf('.');
        return index >= 0 ? value.substring(index + 1) : value;
    }

    private String resolveUnreadableMessage(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String specific = current.getMessage();
            if (specific != null && specific.contains("ARCILLOSO")) {
                return ErrorMessages.SOIL_TYPE_INVALID;
            }
            current = current.getCause();
        }
        return ErrorMessages.BODY_INVALID;
    }
}
