package com.chatapp.backend.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    // Generic Errors
    INTERNAL_SERVER_ERROR(1000, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    BAD_REQUEST(1001, HttpStatus.BAD_REQUEST, "Invalid request"),
    NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED(1003, HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN(1004, HttpStatus.FORBIDDEN, "Access forbidden"),
    UNSUCCESSFUL_SERIALIZATION(1005, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize data"),

    // Authentication & Authorization Errors
    INVALID_CREDENTIALS(2000, HttpStatus.UNAUTHORIZED, "Invalid email and/or password"),
    ACCOUNT_LOCKED(2001, HttpStatus.FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(2002, HttpStatus.FORBIDDEN, "User account is disabled"),
    INVALID_TOKEN(2003, HttpStatus.UNAUTHORIZED, "Invalid authentication token"),
    EXPIRED_TOKEN(2004, HttpStatus.UNAUTHORIZED, "Authentication token has expired"),
    INSUFFICIENT_PERMISSIONS(2005, HttpStatus.FORBIDDEN, "Insufficient permissions for this operation"),
    USERNAME_ALREADY_EXISTS(2006, HttpStatus.BAD_REQUEST, "Username already exists"),
    USER_NOT_FOUND(2007, HttpStatus.NOT_FOUND, "User not found"),
    INVALID_EMAIL(2008, HttpStatus.BAD_REQUEST, "Invalid email"),
    INVALID_USERNAME(2009, HttpStatus.BAD_REQUEST, "Invalid username"),
    INVALID_PASSWORD(2010, HttpStatus.BAD_REQUEST, "Invalid password"),
    INVALID_DISPLAY_NAME(2011, HttpStatus.BAD_REQUEST, "Invalid display name"),
    INVALID_USER_DATA(2012, HttpStatus.BAD_REQUEST, "Invalid user data"),
    JWT_SIGNATURE_INVALID(2014, HttpStatus.UNAUTHORIZED, "JWT signature is invalid"),
    JWT_EXPIRED(2015, HttpStatus.UNAUTHORIZED, "JWT token has expired"),
    JWT_MALFORMED(2016, HttpStatus.BAD_REQUEST, "JWT token is malformed"),
    ACCESS_DENIED(2017, HttpStatus.FORBIDDEN, "Access denied"),
    BAD_CREDENTIALS(2018, HttpStatus.UNAUTHORIZED, "Bad credentials"),
    ILLEGAL_ARGUMENT(2019, HttpStatus.BAD_REQUEST, "Illegal argument"),
    EMAIL_ALREADY_EXISTS(2020, HttpStatus.BAD_REQUEST, "Email already exists"),

    // Cache Errors
    CACHE_OPERATION_FAILED(6000, HttpStatus.INTERNAL_SERVER_ERROR, "Cache operation failed"),
    CACHE_READ_FAILED(6001, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read from cache"),
    CACHE_WRITE_FAILED(6002, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write to cache"),
    CACHE_DELETE_FAILED(6003, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete from cache"),

    // Database Errors
    DATABASE_OPERATION_FAILED(7000, HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed"),
    CONSTRAINT_VIOLATION(7001, HttpStatus.BAD_REQUEST, "Database constraint violation"),
    DATABASE_READ_FAILED(7002, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read from database"),
    DATABASE_WRITE_FAILED(7003, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write to database"),
    DATABASE_UPDATE_FAILED(7004, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update database"),
    DATABASE_DELETE_FAILED(7005, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete from database"),
    PRIMARY_KEY_VIOLATION(7006, HttpStatus.BAD_REQUEST, "Primary key violation"),
    FOREIGN_KEY_VIOLATION(7007, HttpStatus.BAD_REQUEST, "Foreign key violation"),
    UNIQUE_CONSTRAINT_VIOLATION(7008, HttpStatus.BAD_REQUEST, "Unique constraint violation"),
    UNSUCCESSFUL_USER_CREATION(7009, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user"),
    UNSUCCESSFUL_USER_UPDATE(7010, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user"),
    UNSUCCESSFUL_USER_DELETION(7011, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user"),
    UNSUCCESSFUL_USER_FETCHING(7012, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user"),
    ENTITY_NOT_FOUND(7013, HttpStatus.NOT_FOUND, "Entity not found"),
    DATA_INTEGRITY_VIOLATION(7014, HttpStatus.CONFLICT, "Data integrity violation"),


    // Validation Errors
    INVALID_INPUT_DATA(8000, HttpStatus.BAD_REQUEST, "Invalid input data"),
    MISSING_REQUIRED_FIELD(8001, HttpStatus.BAD_REQUEST, "Missing required field"),
    METHOD_ARGUMENT_NOT_VALID(8002, HttpStatus.BAD_REQUEST, "Method argument not valid"),
    METHOD_NOT_ALLOWED(8004, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    INTERNAL_AUTHENTICATION_SERVICE_ERROR(8005, HttpStatus.INTERNAL_SERVER_ERROR, "Internal authentication service error"),

    // External Service Errors
    EXTERNAL_SERVICE_UNAVAILABLE(9000, HttpStatus.SERVICE_UNAVAILABLE, "External service is currently unavailable"),

    //Group Errors
    GROUP_NOT_FOUND(10000, HttpStatus.NOT_FOUND, "Group not found"),
    GROUP_FULL(10001, HttpStatus.BAD_REQUEST, "Group is full"),
    GROUP_MEMBERSHIP_NOT_FOUND(10002, HttpStatus.NOT_FOUND, "Not a member of this group"),
    USER_NOT_IN_GROUP(10003, HttpStatus.BAD_REQUEST, "User is not a member of this group"),

    //Chat Errors
    CONVERSATION_NOT_FOUND(11000, HttpStatus.NOT_FOUND, "Chat not found"),
    INVALID_CONVERSATION_TYPE(11001, HttpStatus.BAD_REQUEST, "Invalid conversation type"),

    //Message Errors
    MESSAGE_NOT_FOUND(12001, HttpStatus.NOT_FOUND, "Message not found"),
    INVALID_STATUS_TRANSITION(12002, HttpStatus.BAD_REQUEST, "Invalid message status transition"),
    CONVERSATION_DELETION_FAILED(12003, HttpStatus.BAD_REQUEST, "Failed to delete conversation");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}