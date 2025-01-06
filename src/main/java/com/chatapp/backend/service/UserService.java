package com.chatapp.backend.service;

import com.chatapp.backend.dto.UserDTO;
import com.chatapp.backend.dto.request.SignInDTO;
import com.chatapp.backend.dto.request.SignUpDTO;
import com.chatapp.backend.entity.User;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    User registerUser(@Valid SignUpDTO signUpDTO);

    User loginUser(SignInDTO signInDTO);

    User getUserById(Long userId);

    User getUserByUsername(String username);

    User updateDisplayName(Long userId, String displayName);

    void setUserOffline(Long userId);

    void setUserOnline(Long userId);

    List<UserDTO> searchUsers(String query);

}
