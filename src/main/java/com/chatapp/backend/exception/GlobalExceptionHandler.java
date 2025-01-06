package com.chatapp.backend.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException ex) {
        log.error("API Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                             .body(createProblemDetail(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint Violation Exception: {}", ex.getMessage(), ex);
        Map<String, String> violations = ex.getConstraintViolations()
                                           .stream()
                                           .collect(Collectors.toMap(violation -> violation.getPropertyPath()
                                                                                           .toString(), ConstraintViolation::getMessage, (error1, error2) -> error1));

        ProblemDetail problemDetail = createProblemDetail(ErrorCode.CONSTRAINT_VIOLATION, "Constraint violation");
        problemDetail.setProperty("violations", violations);
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
                             .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Generic Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                             .body(createProblemDetail(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    private ProblemDetail createProblemDetail(ErrorCode errorCode, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.name());
        problemDetail.setType(URI.create("https://api.yourdomain.com/errors/" + errorCode.getCode()));
        problemDetail.setProperty("errorCode", errorCode.getCode());
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ProblemDetail> handleSignatureException(SignatureException ex) {
        log.error("Signature Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.JWT_SIGNATURE_INVALID.getHttpStatus())
                             .body(createProblemDetail(ErrorCode.JWT_SIGNATURE_INVALID, ex.getMessage()));
    }

//    @ExceptionHandler(LockedException.class)
//    public ResponseEntity<ProblemDetail> handleLockedException(LockedException ex) {
//        log.error("Locked Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.ACCOUNT_LOCKED.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.ACCOUNT_LOCKED, ex.getMessage()));
//    }
//
//    @ExceptionHandler(DisabledException.class)
//    public ResponseEntity<ProblemDetail> handleDisabledException(DisabledException ex) {
//        log.error("Disabled Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.ACCOUNT_DISABLED.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.ACCOUNT_DISABLED, ex.getMessage()));
//    }
//
//    @ExceptionHandler(InternalAuthenticationServiceException.class)
//    public ResponseEntity<ProblemDetail> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
//        log.error("Internal Authentication Service Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.INTERNAL_AUTHENTICATION_SERVICE_ERROR.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.INTERNAL_AUTHENTICATION_SERVICE_ERROR, ex.getMessage()));
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
//        log.error("Bad Credentials Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.BAD_CREDENTIALS.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.BAD_CREDENTIALS, ex.getMessage()));
//    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access Denied Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                             .body(createProblemDetail(ErrorCode.ACCESS_DENIED, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Method Argument Type Mismatch Exception: {}", ex.getMessage(), ex);
        String detail = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(), Objects.requireNonNull(ex.getRequiredType())
                                                                                                                                                  .getSimpleName());
        return ResponseEntity.status(ErrorCode.ILLEGAL_ARGUMENT.getHttpStatus())
                             .body(createProblemDetail(ErrorCode.ILLEGAL_ARGUMENT, detail));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data Integrity Violation Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getHttpStatus())
                             .body(createProblemDetail(ErrorCode.DATA_INTEGRITY_VIOLATION, ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Method Argument Not Valid Exception: {}", ex.getMessage(), ex);
        Map<String, String> fieldErrors = ex.getBindingResult()
                                            .getFieldErrors()
                                            .stream()
                                            .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (error1, error2) -> error1));

        ProblemDetail problemDetail = createProblemDetail(ErrorCode.METHOD_ARGUMENT_NOT_VALID, "Validation failed");
        problemDetail.setProperty("errors", fieldErrors);
        return ResponseEntity.status(ErrorCode.METHOD_ARGUMENT_NOT_VALID.getHttpStatus())
                             .body(problemDetail);
    }

//    @ExceptionHandler(ExpiredJwtException.class)
//    public ResponseEntity<ProblemDetail> handleExpiredJwtException(ExpiredJwtException ex) {
//        log.error("Expired JWT Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.JWT_EXPIRED.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.JWT_EXPIRED, ex.getMessage()));
//    }
//
//    @ExceptionHandler(MalformedJwtException.class)
//    public ResponseEntity<ProblemDetail> handleMalformedJwtException(MalformedJwtException ex) {
//        log.error("Malformed JWT Exception: {}", ex.getMessage(), ex);
//        return ResponseEntity.status(ErrorCode.JWT_MALFORMED.getHttpStatus())
//                             .body(createProblemDetail(ErrorCode.JWT_MALFORMED, ex.getMessage()));
//    }
}