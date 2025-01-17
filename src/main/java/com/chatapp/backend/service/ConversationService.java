package com.chatapp.backend.service;


import com.chatapp.backend.dto.request.GroupRequest;
import com.chatapp.backend.dto.request.GroupSettingsRequest;
import com.chatapp.backend.dto.request.MessageRequest;
import com.chatapp.backend.dto.response.*;
import com.chatapp.backend.entity.enums.MessageStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ConversationService {

    GroupConversationResponse createGroupConversation(@Valid GroupRequest request);

    GroupSettingsResponse updateGroupSettings(Long groupId, @Valid GroupSettingsRequest request);

    Set<UserResponse> addParticipants(Long groupId, Set<String> participantUsernames);

    Set<UserResponse> removeParticipant(Long groupId, String username);

    PrivateConversationResponse createPrivateConversation(String username);

    Page<MessageResponse> getMessages(String username, Long conversationId, Pageable pageable);

    MessageDeliveryStatusResponse changeMessageStatus(String username, Long messageId, MessageStatus status);

    void deleteConversation(Long conversationId);

    Page<PrivateConversationResponse> getUserPrivateChats(  Pageable pageable);

    Page<GroupConversationResponse> getUserGroupChats( Pageable pageable);

    CompletableFuture<MessageResponse> processPrivateMessage(MessageRequest messageRequest);

    CompletableFuture<MessageResponse> processGroupMessage(Long conversationId, MessageRequest messageRequest);

    Page<ConversationResponse> getUserConversations(Pageable pageable);

    ConversationResponse getConversation(Long conversationId);
}
