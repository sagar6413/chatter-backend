package com.chatapp.backend.service.impl;

import com.chatapp.backend.config.jwt.JwtService;
import com.chatapp.backend.dto.request.SignInRequest;
import com.chatapp.backend.dto.request.SignUpRequest;
import com.chatapp.backend.dto.request.UserPreferenceRequest;
import com.chatapp.backend.dto.request.UserRequest;
import com.chatapp.backend.dto.response.AuthenticationResponse;
import com.chatapp.backend.dto.response.UserPreferenceResponse;
import com.chatapp.backend.dto.response.UserResponse;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.entity.UserPreferences;
import com.chatapp.backend.entity.enums.UserStatus;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.UserService;
import com.chatapp.backend.util.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

import static com.chatapp.backend.exception.ErrorCode.USERNAME_ALREADY_EXISTS;
import static com.chatapp.backend.exception.ErrorCode.USER_NOT_FOUND;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthenticationResponse signUp(SignUpRequest request) {
        log.info("Starting user registration process for username: {}", request.username());

        // Check if username already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Registration failed - Username already exists: {}", request.username());
            throw new ApiException(USERNAME_ALREADY_EXISTS);
        }

        // Create new user
        User user = objectMapper.mapSignUpDTOToUser(request);
        System.out.println("user: " + user);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully. UserId: {}, Username: {}", savedUser.getId(), savedUser.getUsername());

        return generateAuthenticationResponse(savedUser);
    }

    private AuthenticationResponse generateAuthenticationResponse(User user) {
        String accessToken = jwtService.generateAccessToken(Collections.singletonMap("userId", user.getId()), user);
        String refreshToken = jwtService.generateRefreshToken(user);

        System.out.println("accessToken: " + accessToken);
        System.out.println("refreshToken: " + refreshToken);
        log.info("Updating refresh token for user: {}", user.getId());
        userRepository.updateRefreshToken(user.getId(), refreshToken);
        log.info("Refresh token updated successfully for user: {}, refresh token: {}", user.getId(), refreshToken);

        return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    @Transactional
    public AuthenticationResponse signIn(SignInRequest request) {
        log.info("Processing sign-in request for username: {}", request.username());

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = (User) auth.getPrincipal();

        return generateAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid refresh token");
        }

        String refreshToken = authHeader.substring(7);
        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
           throw new RuntimeException("Invalid refresh token");
        }
        User user = getUserByUsernameOrThrow(username);
        jwtService.validateToken(refreshToken, user);
        return generateAuthenticationResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user details for username: {}", username);

        User user = getUserByUsernameOrThrow(username);

        return objectMapper.mapUserToUserResponse(user);
    }

    private User getUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User not found: {}", username);
            return new ApiException(USER_NOT_FOUND);
        });
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UserRequest request) {
        log.info("Processing user update request for username: {}", username);

        User user = getUserByUsernameOrThrow(username);

        User updatedUser = objectMapper.mapUserRequestToUser(request, user);

        User savedUser = userRepository.save(updatedUser);
        log.info("User updated successfully. UserId: {}", savedUser.getId());

        return objectMapper.mapUserToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserPreferenceResponse updatePreferences(UserPreferenceRequest request) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Updating preferences for username: {}", user.getUsername());

        UserPreferences preferences = objectMapper.mapUserPreferenceRequestToUserPreferences(request, user.getPreferences());
        user.setPreferences(preferences);

        User savedUser = userRepository.save(user);

        log.info("User preferences updated successfully. UserId: {}", savedUser.getId());
        return objectMapper.mapUserPreferencesToUserPreferencesResponse(savedUser.getPreferences());
    }

    @Override
    @Transactional
    public UserStatus updateUserStatus(String username, UserStatus status) {
        log.info("Updating status to {} for username: {}", status, username);

        User user = getUserByUsernameOrThrow(username);

        user.setStatus(status);
        user.setLastSeenAt(Instant.now());
        User savedUser = userRepository.save(user);

        log.info("User status updated successfully. UserId: {}, Status: {}", savedUser.getId(), savedUser.getStatus());
        return savedUser.getStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        log.info("Searching users with query: '{}', Page: {}, Size: {}", query, pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.searchUsers(query, pageable);
        return users.map(objectMapper::mapUserToUserResponse);
    }

    @Override
    @Transactional
    public void logout() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepository.updateRefreshToken(user.getId(), null);
    }
}