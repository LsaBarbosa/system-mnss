package br.com.novaalianca.mnss.sharedinfra.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        HttpStatus status = exception.status();
        return ResponseEntity
                .status(status)
                .body(ApiError.withoutFields(exception.code(), exception.getMessage(), status.value(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiError.withFields(
                        VALIDATION_ERROR,
                        "Campos invalidos na requisicao.",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .sorted(Comparator.comparing(ApiFieldError::field))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ApiError.withFields(
                        VALIDATION_ERROR,
                        "Campos invalidos na requisicao.",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(ApiError.withoutFields(
                        BAD_REQUEST,
                        "Requisicao invalida.",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.withoutFields(
                        INTERNAL_ERROR,
                        "Erro interno inesperado.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getRequestURI()));
    }
}
