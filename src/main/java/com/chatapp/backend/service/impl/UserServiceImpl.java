package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.UserDTO;
import com.chatapp.backend.dto.request.SignInDTO;
import com.chatapp.backend.dto.request.SignUpDTO;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.chatapp.backend.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public User registerUser(SignUpDTO signUpDTO) {
        log.info("Registering new user with username: {}", signUpDTO.username());
        if (userRepository.findByUsername(signUpDTO.username()).isEmpty()) {
            log.warn("User registration failed: Username {} already exists.", signUpDTO.username());
            throw new ApiException(USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                        .username(signUpDTO.username())
                        .displayName(signUpDTO.displayName())
                        .password(signUpDTO.password())
                        .lastActiveAt(Instant.now()) // Set initial last active time
                        .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
    }

    // Example: Method to update user's display name (can be extended)
    @Transactional
    @Override
    public User updateDisplayName(Long userId, String displayName) {
        log.info("Updating display name for user ID: {} to: {}", userId, displayName);
        User user = getUserById(userId);
        user.setDisplayName(displayName);
        return userRepository.save(user);
    }

    // Example: Method to handle user coming online (update last active)
    @Transactional
    @Override
    public void setUserOnline(Long userId) {
        log.info("User {} is online.", userId);
        updateUserLastActive(userId); // For simplicity, just update last active
        // In a more complex scenario, you might update a presence status here.
    }

    @Override
    public List<UserDTO> searchUsers(String query) {
        return null;
    }

    @Override
    public User loginUser(SignInDTO signInDTO) {
        User user = userRepository.findByUsername(signInDTO.username())
                                  .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (!user.getPassword().equals(signInDTO.password())) {
            throw new ApiException(INVALID_CREDENTIALS);
        }

        return user;
    }

    @Transactional
    public void updateUserLastActive(Long userId) {
        log.debug("Updating last active time for user ID: {}", userId);
        User user = getUserById(userId);
        user.setLastActiveAt(Instant.now());
        userRepository.save(user);
    }

    // Example: Method to handle user going offline
    @Transactional
    @Override
    public void setUserOffline(Long userId) {
        log.info("User {} is offline.", userId);
        // You might update a presence status here.
    }
}