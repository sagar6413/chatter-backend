package com.chatapp.backend.controller;


import com.chatapp.backend.dto.ContactDTO;
import com.chatapp.backend.dto.UserDTO;
import com.chatapp.backend.dto.request.SignInDTO;
import com.chatapp.backend.dto.request.SignUpDTO;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.service.ContactService;
import com.chatapp.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ContactService contactService;

    //Register a new user
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody SignUpDTO signUpDTO) {
        User registeredUser = userService.registerUser(signUpDTO);
        return new ResponseEntity<>(mapToDTO(registeredUser), HttpStatus.CREATED);
    }

    //Login a user
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestBody SignInDTO signInDTO) {
        User loggedInUser = userService.loginUser(signInDTO);
        return new ResponseEntity<>(mapToDTO(loggedInUser), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(mapToDTO(user), HttpStatus.OK);
    }

    @GetMapping("/{userId}/chats")
    public ResponseEntity<List<ContactDTO>> getUserChats(@PathVariable Long userId,
                                                         @RequestParam(required = false) Boolean unreadOnly) {
        log.info("Fetching chats for user, unreadOnly: {}", unreadOnly);
        List<ContactDTO> chats = contactService.getUserChats(userId, unreadOnly);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return new ResponseEntity<>(mapToDTO(user), HttpStatus.OK);
    }

    @PostMapping("/{userId}/online")
    public ResponseEntity<Void> setUserOnline(@PathVariable Long userId) {
        userService.setUserOnline(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/offline")
    public ResponseEntity<Void> setUserOffline(@PathVariable Long userId) {
        userService.setUserOffline(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/displayname")
    public ResponseEntity<UserDTO> updateDisplayName(@PathVariable Long userId, @RequestBody String displayName) {
        User updatedUser = userService.updateDisplayName(userId, displayName);
        return ResponseEntity.ok(mapToDTO(updatedUser));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching users with query: {}", query);
        List<UserDTO> users = userService.searchUsers(
                query
        );
        return ResponseEntity.ok((List<UserDTO>) users);
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(user.getUsername(), user.getDisplayName());
    }
}