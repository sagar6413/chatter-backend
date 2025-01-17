package com.chatapp.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SignUpRequest(@NotBlank(message = "Username is required") @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be alphanumeric and between 3-50 characters") String username,
                            @NotBlank(message = "Display name is required") @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters") String displayName,
                            @NotBlank(message = "Password is required") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must be at least 8 characters long and contain at least one digit, " + "one uppercase letter, one lowercase letter, and one special character") String password) {
}
