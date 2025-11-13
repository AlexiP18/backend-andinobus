package com.andinobus.backendsmartcode.common.errors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones con formato uniforme de respuesta.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.Detail> details = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorResponse.Detail(fe.getField(), fe.getDefaultMessage()));
        }
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
                "Request inválido", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest req) {
        List<ErrorResponse.Detail> details = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorResponse.Detail(fe.getField(), fe.getDefaultMessage()));
        }
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "BIND_ERROR",
                "Error de enlace de parámetros", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorResponse.Detail> details = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
            details.add(new ErrorResponse.Detail(field, v.getMessage()));
        }
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "CONSTRAINT_VIOLATION",
                "Violación de restricciones", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "MESSAGE_NOT_READABLE",
                "Cuerpo de la solicitud inválido o mal formado", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        List<ErrorResponse.Detail> details = List.of(new ErrorResponse.Detail(ex.getParameterName(), "parámetro requerido"));
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "MISSING_PARAMETER",
                "Faltan parámetros requeridos", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        List<ErrorResponse.Detail> details = List.of(new ErrorResponse.Detail(ex.getName(), "tipo inválido"));
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "TYPE_MISMATCH",
                "Tipo de parámetro inválido", details, req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({NotFoundException.class, NoSuchElementException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "NOT_FOUND",
                ex.getMessage() == null ? "Recurso no encontrado" : ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED.value(), "METHOD_NOT_ALLOWED",
                "Método HTTP no permitido", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "UNSUPPORTED_MEDIA_TYPE",
                "Tipo de contenido no soportado", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
                ex.getMessage() == null ? "Parámetros inválidos" : ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                "Ha ocurrido un error inesperado", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
