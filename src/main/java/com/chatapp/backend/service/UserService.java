package com.chatapp.backend.service;

import com.chatapp.backend.dto.request.SignInRequest;
import com.chatapp.backend.dto.request.SignUpRequest;
import com.chatapp.backend.dto.request.UserPreferenceRequest;
import com.chatapp.backend.dto.request.UserRequest;
import com.chatapp.backend.dto.response.AuthenticationResponse;
import com.chatapp.backend.dto.response.UserPreferenceResponse;
import com.chatapp.backend.dto.response.UserResponse;
import com.chatapp.backend.entity.enums.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    AuthenticationResponse signUp(@Valid SignUpRequest request);

    AuthenticationResponse signIn(@Valid SignInRequest request);

    UserResponse getUserByUsername(String username);

    void logout();

    UserResponse updateUser(String username, @Valid UserRequest request);

    UserPreferenceResponse updatePreferences(@Valid UserPreferenceRequest request);

    UserStatus updateUserStatus(String username, UserStatus status);

    Page<UserResponse> searchUsers(String query, Pageable pageable);

    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
}
