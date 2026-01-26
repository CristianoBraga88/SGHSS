package br.com.vidaplus.sghss_api.exception;

import com.auth0.jwt.exceptions.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exceções de negócio customizadas
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Business Exception: {}", ex.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Business Rule Violation");
        error.put("message", ex.getMessage());
        error.put("path", getPath(request));

        if (ex.getDetails() != null) {
            error.put("details", ex.getDetails());
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource Not Found: {}", ex.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Resource Not Found");
        error.put("message", ex.getMessage());
        error.put("path", getPath(request));

        Map<String, Object> details = new HashMap<>();
        details.put("resource", ex.getResourceName());
        details.put("field", ex.getFieldName());
        details.put("value", ex.getFieldValue());
        error.put("details", details);

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.warn("Validation Exception: {}", ex.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");
        error.put("message", ex.getMessage());
        error.put("path", getPath(request));

        if (ex.getField() != null) {
            Map<String, Object> validation = new HashMap<>();
            validation.put("field", ex.getField());
            validation.put("message", ex.getMessage());
            if (ex.getRejectedValue() != null) {
                validation.put("rejectedValue", ex.getRejectedValue());
            }

            List<Map<String, Object>> validationErrors = new ArrayList<>();
            validationErrors.add(validation);
            error.put("validationErrors", validationErrors);
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Validação do Spring
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Method Argument Not Valid: {}", ex.getMessage());

        List<Map<String, Object>> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            Map<String, Object> validationError = new HashMap<>();

            if (error instanceof FieldError fieldError) {
                validationError.put("field", fieldError.getField());
                validationError.put("rejectedValue", fieldError.getRejectedValue());
            } else {
                validationError.put("field", error.getObjectName());
            }

            validationError.put("message", error.getDefaultMessage());
            validationErrors.add(validationError);
        });

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");
        error.put("message", "Campos inválidos na requisição");
        error.put("path", getPath(request));
        error.put("validationErrors", validationErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Violação de constraints do banco
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint Violation: {}", ex.getMessage());

        List<Map<String, Object>> validationErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            Map<String, Object> validationError = new HashMap<>();
            validationError.put("field", violation.getPropertyPath().toString());
            validationError.put("message", violation.getMessage());
            validationError.put("rejectedValue", violation.getInvalidValue());
            validationErrors.add(validationError);
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Database Constraint Violation");
        error.put("message", "Violação de restrições do banco de dados");
        error.put("path", getPath(request));
        error.put("validationErrors", validationErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data Integrity Violation: {}", ex.getMessage());

        String message = "Violação de integridade de dados";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "Registro duplicado";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Violação de chave estrangeira";
            } else if (ex.getMessage().contains("cannot be null")) {
                message = "Campo obrigatório não informado";
            }
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", "Data Integrity Error");
        error.put("message", message);
        error.put("path", getPath(request));

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Exceções de segurança
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access Denied: {}", ex.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.FORBIDDEN.value());
        error.put("error", "Access Denied");
        error.put("message", "Acesso negado. Permissões insuficientes.");
        error.put("path", getPath(request));

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Authentication Failed: {}", ex.getMessage());

        String message = "Falha na autenticação";
        if (ex instanceof BadCredentialsException) {
            message = "Credenciais inválidas";
        } else if (ex instanceof DisabledException) {
            message = "Usuário desativado";
        } else if (ex instanceof LockedException) {
            message = "Usuário bloqueado";
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Authentication Failed");
        error.put("message", message);
        error.put("path", getPath(request));

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // Exceções JWT do Auth0
    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<Map<String, Object>> handleJWTVerificationException(
            JWTVerificationException ex, WebRequest request) {
        log.warn("JWT Verification Failed: {}", ex.getMessage());

        String errorType = "Invalid Token";
        String message = "Token JWT inválido";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // Verifica o tipo específico de erro JWT do Auth0
        if (ex instanceof TokenExpiredException) {
            errorType = "Token Expired";
            message = "Token JWT expirado. Faça login novamente.";
        } else if (ex instanceof AlgorithmMismatchException) {
            errorType = "Algorithm Mismatch";
            message = "Algoritmo de assinatura JWT incompatível.";
        } else if (ex instanceof SignatureVerificationException) {
            errorType = "Invalid Signature";
            message = "Assinatura JWT inválida.";
        } else if (ex instanceof InvalidClaimException) {
            errorType = "Invalid Claim";
            message = "Claim JWT inválido: " + ((InvalidClaimException) ex).getMessage();
        } else if (ex instanceof JWTDecodeException) {
            errorType = "Malformed Token";
            message = "Token JWT malformado.";
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", errorType);
        error.put("message", message);
        error.put("path", getPath(request));

        Map<String, Object> details = new HashMap<>();
        details.put("jwtError", ex.getMessage());
        error.put("details", details);

        return new ResponseEntity<>(error, status);
    }

    // Erros de tipo/parâmetro
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Argument Type Mismatch: {}", ex.getMessage());

        String message = String.format("Parâmetro '%s' deve ser do tipo %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Invalid Parameter Type");
        error.put("message", message);
        error.put("path", getPath(request));

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected Error: ", ex);

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", "Ocorreu um erro interno no servidor");
        error.put("path", getPath(request));

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extrai o caminho da requisição do WebRequest
     */
    private String getPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}