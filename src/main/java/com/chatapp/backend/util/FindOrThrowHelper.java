package com.chatapp.backend.util;

import com.chatapp.backend.entity.Conversation;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.repository.ConversationRepository;
import com.chatapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.chatapp.backend.exception.ErrorCode.CONVERSATION_NOT_FOUND;
import static com.chatapp.backend.exception.ErrorCode.USER_NOT_FOUND;

@Component
@Slf4j
@RequiredArgsConstructor
public class FindOrThrowHelper {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public Set<User> findUsersOrThrow(Set<String> usernames) {
        return usernames.stream().map(this::findUserOrThrow).collect(Collectors.toSet());

    }

    public User findUserOrThrow(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User not found: {}", username);
            return new ApiException(USER_NOT_FOUND, "User not found: " + username);
        });
    }

    public Conversation findConversationOrThrow(Long conversationId) {
        return conversationRepository.findById(conversationId).orElseThrow(() -> {
            log.warn("Conversation not found: {}", conversationId);
            return new ApiException(CONVERSATION_NOT_FOUND, "Conversation not found: " + conversationId);
        });
    }
}
