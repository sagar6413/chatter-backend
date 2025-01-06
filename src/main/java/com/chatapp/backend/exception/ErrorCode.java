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

    //Username Already Exists Error
    USERNAME_ALREADY_EXISTS(2006, HttpStatus.BAD_REQUEST, "Username already exists"),
    USER_NOT_FOUND(2007, HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_ALREADY_EXISTS(2007, HttpStatus.BAD_REQUEST, "Email already exists"),
    INVALID_EMAIL(2008, HttpStatus.BAD_REQUEST, "Invalid email"),
    INVALID_USERNAME(2009, HttpStatus.BAD_REQUEST, "Invalid username"),
    INVALID_PASSWORD(2010, HttpStatus.BAD_REQUEST, "Invalid password"),
    INVALID_DISPLAY_NAME(2011, HttpStatus.BAD_REQUEST, "Invalid display name"),
    INVALID_USER_DATA(2012, HttpStatus.BAD_REQUEST, "Invalid user data"),
    UNSUCCESSFUL_USER_CREATION(2013, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user"),
    UNSUCCESSFUL_USER_UPDATE(2014, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user"),
    UNSUCCESSFUL_USER_DELETION(2015, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user"),
    UNSUCCESSFUL_USER_FETCHING(2016, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user"),
    // Blog Post Errors
    BLOG_POST_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "Blog post not found"),
    UNAUTHORIZED_BLOG_POST_UPDATE(3001, HttpStatus.FORBIDDEN, "Not authorized to update this blog post"),
    UNAUTHORIZED_BLOG_POST_DELETE(3002, HttpStatus.FORBIDDEN, "Not authorized to delete this blog post"),
    INVALID_BLOG_POST_DATA(3003, HttpStatus.BAD_REQUEST, "Invalid blog post data"),
    UNSUCCESSFUL_BLOG_POST_CREATION(3004, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create blog post"),
    UNSUCCESSFUL_BLOG_POST_UPDATE(3005, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update blog post"),
    UNSUCCESSFUL_BLOG_POST_DELETION(3006, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete blog post"),
    UNSUCCESSFUL_BLOG_POST_FETCHING(3007, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blog post"),

    // Category Errors
    CATEGORY_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "Category not found"),
    INVALID_CATEGORY_DATA(4001, HttpStatus.BAD_REQUEST, "Invalid category data"),
    UNSUCCESSFUL_CATEGORY_FETCHING(4002, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch category"),

    // Tag Errors
    TAG_NOT_FOUND(5000, HttpStatus.NOT_FOUND, "Tag not found"),
    INVALID_TAG_DATA(5001, HttpStatus.BAD_REQUEST, "Invalid tag data"),

    // Cache Errors
    CACHE_OPERATION_FAILED(6000, HttpStatus.INTERNAL_SERVER_ERROR, "Cache operation failed"),

    // Database Errors
    DATABASE_OPERATION_FAILED(7000, HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed"),
    CONSTRAINT_VIOLATION(7001, HttpStatus.BAD_REQUEST, "Database constraint violation"),

    // Validation Errors
    INVALID_INPUT_DATA(8000, HttpStatus.BAD_REQUEST, "Invalid input data"),
    MISSING_REQUIRED_FIELD(8001, HttpStatus.BAD_REQUEST, "Missing required field"),
    METHOD_ARGUMENT_NOT_VALID(8002, HttpStatus.BAD_REQUEST, "Method argument not valid"),
    ENTITY_NOT_FOUND(8003, HttpStatus.NOT_FOUND, "Entity not found"),
    METHOD_NOT_ALLOWED(8004, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    ILLEGAL_ARGUMENT(8005, HttpStatus.BAD_REQUEST, "Illegal argument"),
    INTERNAL_AUTHENTICATION_SERVICE_ERROR(8008, HttpStatus.INTERNAL_SERVER_ERROR, "Internal authentication service error"),
    BAD_CREDENTIALS(8009, HttpStatus.UNAUTHORIZED, "Bad credentials"),
    ACCESS_DENIED(8010, HttpStatus.FORBIDDEN, "Access denied"),
    JWT_SIGNATURE_INVALID(8011, HttpStatus.UNAUTHORIZED, "JWT signature is invalid"),
    JWT_EXPIRED(8012, HttpStatus.UNAUTHORIZED, "JWT token has expired"),
    JWT_MALFORMED(8013, HttpStatus.BAD_REQUEST, "JWT token is malformed"),
    DATA_INTEGRITY_VIOLATION(8014, HttpStatus.CONFLICT, "Data integrity violation"),


    // External Service Errors
    EXTERNAL_SERVICE_UNAVAILABLE(9000, HttpStatus.SERVICE_UNAVAILABLE, "External service is currently unavailable"),

    //Group Errors
    GROUP_NOT_FOUND(10000, HttpStatus.NOT_FOUND, "Group not found"),
    GROUP_FULL(10001, HttpStatus.BAD_REQUEST, "Group is full"),
    GROUP_MEMBERSHIP_NOT_FOUND(10002, HttpStatus.NOT_FOUND, "Not a member of this group"),

    //Chat Errors
    CHAT_NOT_FOUND(11000, HttpStatus.NOT_FOUND, "Chat not found");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}