package com.inditex.similarproducts.api.infrastructure.web;

import com.inditex.similarproducts.api.domain.ExternalServiceException;
import com.inditex.similarproducts.api.domain.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex,
                                                               HttpServletRequest request)
    {
        log.warn("Product not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                                                        ex.getMessage(),
                                                        request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Void> handleExternalService(ExternalServiceException ex)
    {
        log.error("External service error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request)
    {
        log.warn("Validation error: {}", ex.getMessage());

        String message = ex.getConstraintViolations()
                            .stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                HttpServletRequest request)
    {
        log.warn("Invalid argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja NoResourceFoundException cuando la URL tiene un path variable vacío (ej: /product//similar).
     * Spring no mapea esto al controller, pero nosotros lo convertimos a 400 Bad Request si es nuestro endpoint.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                               HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Si la URL es de nuestro endpoint de productos similar con path variable vacío
        if (requestURI.matches("/product/+similar"))
        {
            log.warn("Empty productId in URL: {}", requestURI);

            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Product ID is required",
                    requestURI
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Para otros casos, devolvemos 404 Not Found
        log.warn("Resource not found: {}", requestURI);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Maneja CompletionException que envuelve excepciones del procesamiento asíncrono.
     * Desenvuelve la causa original y la maneja apropiadamente.
     */
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<?> handleCompletionException(CompletionException ex, HttpServletRequest request)
    {
        Throwable cause = ex.getCause();

        if (cause instanceof ProductNotFoundException) {
            return handleProductNotFound((ProductNotFoundException) cause, request);
        }

        if (cause instanceof ExternalServiceException) {
            return handleExternalService((ExternalServiceException) cause);
        }

        log.error("Unexpected error in async operation", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

