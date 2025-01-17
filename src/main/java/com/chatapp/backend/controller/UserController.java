package com.chatapp.backend.controller;

import com.chatapp.backend.dto.request.SignInRequest;
import com.chatapp.backend.dto.request.SignUpRequest;
import com.chatapp.backend.dto.request.UserPreferenceRequest;
import com.chatapp.backend.dto.request.UserRequest;
import com.chatapp.backend.dto.response.AuthenticationResponse;
import com.chatapp.backend.dto.response.UserPreferenceResponse;
import com.chatapp.backend.dto.response.UserResponse;
import com.chatapp.backend.entity.enums.UserStatus;
import com.chatapp.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    //auth related
    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Processing signup request for user: {}", request.username());
        return ResponseEntity.ok(userService.signUp(request));
    }


    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signIn(@Valid @RequestBody SignInRequest request) {
        log.info("Processing signin request for user: {}", request.username());
        return ResponseEntity.ok(userService.signIn(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refreshing token for user.");
        userService.refreshToken(request, response);
        return ResponseEntity.ok().build();
    }

    //user related

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        log.info("Fetching user details for username: {}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String username, @Valid @RequestBody UserRequest request) {
        log.info("Updating details for user: {}", username);
        return ResponseEntity.ok(userService.updateUser(username, request));
    }


    @PutMapping("/me/preferences")
    public ResponseEntity<UserPreferenceResponse> updatePreferences(@Valid @RequestBody UserPreferenceRequest request) {
        log.info("Updating preference");
        return ResponseEntity.ok(userService.updatePreferences(request));
    }


    @PutMapping("/{username}/status")
    public ResponseEntity<UserStatus> updateUserStatus(@PathVariable String username, @RequestBody UserStatus status) {
        log.info("Updating status for user: {} to {}", username, status);
        return ResponseEntity.ok(userService.updateUserStatus(username, status));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String query, Pageable pageable) {
        log.info("Searching users with query: {}", query);
        return ResponseEntity.ok(userService.searchUsers(query, pageable));
    }
}
