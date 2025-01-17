package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.request.GroupRequest;
import com.chatapp.backend.dto.request.GroupSettingsRequest;
import com.chatapp.backend.dto.request.MessageRequest;
import com.chatapp.backend.dto.response.*;
import com.chatapp.backend.entity.*;
import com.chatapp.backend.entity.enums.ConversationType;
import com.chatapp.backend.entity.enums.MessageStatus;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.repository.ConversationRepository;
import com.chatapp.backend.repository.MessageDeliveryStatusRepository;
import com.chatapp.backend.repository.MessageRepository;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.ConversationService;
import com.chatapp.backend.util.FindOrThrowHelper;
import com.chatapp.backend.util.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chatapp.backend.entity.enums.ConversationType.GROUP;
import static com.chatapp.backend.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private static final String CONVERSATION_ID_LOG = "conversationId";
    private static final String USERNAME_LOG = "username";
    private static final String MESSAGE_ID = "messageId";

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final MessageDeliveryStatusRepository messageDeliveryStatusRepository;
    private final FindOrThrowHelper findOrThrowHelper;

    @Override
    @Transactional
    public GroupConversationResponse createGroupConversation(GroupRequest request) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("operation", "createGroupConversation")) {
            log.info("Creating new group conversation with {} participants", request.participantUsernames().size());

            Set<User> participants = findOrThrowHelper.findUsersOrThrow(request.participantUsernames());

            Conversation groupConversation = objectMapper.mapGroupRequestToConversation(request, participants);
            Conversation savedConversation = conversationRepository.save(groupConversation);

            log.info("Group conversation created successfully with ID: {}", savedConversation.getId());
            return objectMapper.mapConversationToGroupConversationResponse(savedConversation);
        } catch (Exception e) {
            log.error("Error creating group conversation", e);
            throw new ApiException(INTERNAL_SERVER_ERROR, "Error creating group conversation");
        }
    }


    @Override
    @Transactional
    public GroupSettingsResponse updateGroupSettings(Long groupId, GroupSettingsRequest request) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(groupId))) {
            log.info("Updating group settings");

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(groupId);
            GroupSettings updatedSettings = objectMapper.mapGroupSettingsRequestToGroupSettings(request);

            conversation.setGroupSettings(updatedSettings);
            conversationRepository.save(conversation);

            log.info("Group settings updated successfully");
            return objectMapper.mapGroupSettingsToGroupSettingsResponse(updatedSettings);
        } catch (Exception e) {
            log.error("Error updating group settings", e);
            throw new ApiException(INTERNAL_SERVER_ERROR, "Error updating group settings");
        }
    }

    @Override
    @Transactional
    @Async
    public CompletableFuture<MessageResponse> processPrivateMessage(MessageRequest messageRequest) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("messageType", "private")) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            MDC.put(USERNAME_LOG, user.getUsername());
            log.info("Processing private message for user: {}", user.getUsername());

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(messageRequest.conversationId());

            Message message = objectMapper.mapMessageRequestToMessage(messageRequest, user, conversation);

            Message savedMessage = messageRepository.save(message);
            MDC.put(MESSAGE_ID, String.valueOf(savedMessage.getId()));
            log.info("Private message saved successfully with ID: {}", savedMessage.getId());

            return CompletableFuture.completedFuture(objectMapper.mapMessageToMessageResponse(savedMessage));
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Error processing private message");
        } finally {
            MDC.remove(MESSAGE_ID);
            MDC.remove(USERNAME_LOG);
        }
    }

    @Override
    @Transactional
    @Async
    public CompletableFuture<MessageResponse> processGroupMessage(Long conversationId, MessageRequest messageRequest) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(conversationId))) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("Processing group message for conversation: {}", conversationId);

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(conversationId);
            preSaveGroupMessage(conversation);

            Message message = objectMapper.mapMessageRequestToMessage(messageRequest, user, conversation);

            Message savedMessage = messageRepository.save(message);
            MDC.put(MESSAGE_ID, String.valueOf(savedMessage.getId()));
            log.info("Group message saved successfully with ID: {}", savedMessage.getId());

            return CompletableFuture.completedFuture(objectMapper.mapMessageToMessageResponse(savedMessage));
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Error processing group message");
        } finally {
            MDC.remove(CONVERSATION_ID_LOG);
            MDC.remove(MESSAGE_ID);
            MDC.remove(USERNAME_LOG);
        }
    }

    // Participant Management Methods
    @Override
    @Transactional
    public Set<UserResponse> addParticipants(Long groupId, Set<String> participantUsernames) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(groupId))) {
            log.info("Adding {} participants to group", participantUsernames.size());

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(groupId);
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            preSaveGroupSettings(conversation, user);

            Set<User> newParticipants = participantUsernames.stream()
                                                            .map(findOrThrowHelper::findUserOrThrow)
                                                            .collect(Collectors.toSet());

            // Check for duplicate participants
            Set<User> existingParticipants = conversation.getParticipants();
            Set<User> duplicateParticipants = newParticipants.stream()
                                                             .filter(existingParticipants::contains)
                                                             .collect(Collectors.toSet());

            if (!duplicateParticipants.isEmpty()) {
                log.warn("Attempted to add existing participants: {}", duplicateParticipants.stream()
                                                                                            .map(User::getUsername)
                                                                                            .collect(Collectors.joining(", ")));
            }

            // Add only new participants
            newParticipants.removeAll(duplicateParticipants);
            existingParticipants.addAll(newParticipants);
            conversation.setParticipantCount(existingParticipants.size());

            Conversation savedConversation = conversationRepository.save(conversation);
            log.info("Successfully added {} new participants", newParticipants.size());

            return savedConversation.getParticipants()
                                    .stream()
                                    .map(objectMapper::mapUserToUserResponse)
                                    .collect(Collectors.toSet());
        }
    }

    @Override
    @Transactional
    public Set<UserResponse> removeParticipant(Long groupId, String username) {
        try (MDC.MDCCloseable groupContext = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(groupId)); MDC.MDCCloseable userContext = MDC.putCloseable(USERNAME_LOG, username)) {

            log.info("Attempting to remove participant from group");
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(groupId);
            preSaveGroupSettings(conversation, user);

            User userToRemove = findOrThrowHelper.findUserOrThrow(username);

            if (!conversation.getParticipants().remove(userToRemove)) {
                log.warn("User was not a participant in the group");
                throw new ApiException(GROUP_MEMBERSHIP_NOT_FOUND, "User is not a participant in this group");
            }

            conversation.setParticipantCount(conversation.getParticipants().size());
            Conversation savedConversation = conversationRepository.save(conversation);

            log.info("Successfully removed participant from group");
            return savedConversation.getParticipants()
                                    .stream()
                                    .map(objectMapper::mapUserToUserResponse)
                                    .collect(Collectors.toSet());
        }
    }

    // Chat Creation Methods
    @Override
    @Transactional
    public PrivateConversationResponse createPrivateConversation(String username) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(USERNAME_LOG, username)) {
            log.info("Creating new private conversation");

            User participant = findOrThrowHelper.findUserOrThrow(username);
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Check if private conversation already exists
            Optional<Conversation> existingConversation = conversationRepository.findPrivateConversationBetweenUsers(username, user.getUsername());

            if (existingConversation.isPresent()) {
                log.info("Found existing private conversation, returning it");
                return objectMapper.mapConversationToPrivateConversationResponse(existingConversation.get());
            }
            Conversation privateConversation = Conversation.builder()
                                                           .type(ConversationType.PRIVATE)
                                                           .participants(Set.of(participant, user))
                                                           .participantCount(2)
                                                           .createdAt(Instant.now())
                                                           .build();

            Conversation savedConversation = conversationRepository.save(privateConversation);
            log.info("Private conversation created successfully with ID: {}", savedConversation.getId());

            return objectMapper.mapConversationToPrivateConversationResponse(savedConversation);
        }
    }

    // Message Operations Methods
    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(String username, Long conversationId, Pageable pageable) {
        try (MDC.MDCCloseable convContext = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(conversationId)); MDC.MDCCloseable userContext = MDC.putCloseable(USERNAME_LOG, username)) {

            log.info("Fetching messages for conversation, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

            // Verify user has access to this conversation
            Conversation conversation = findOrThrowHelper.findConversationOrThrow(conversationId);
            User user = findOrThrowHelper.findUserOrThrow(username);

            if (!conversation.getParticipants().contains(user)) {
                log.warn("User attempted to access messages without being a participant");
                throw new ApiException(ACCESS_DENIED, "User is not a participant in this conversation");
            }

            Page<Message> messages = messageRepository.findByConversationIdAndSenderUsername(conversationId, username, pageable);

            if (messages.isEmpty()) {
                log.info("No messages found for the specified criteria");
            }

            return messages.map(objectMapper::mapMessageToMessageResponse);
        }
    }

    @Override
    @Transactional
    public MessageDeliveryStatusResponse changeMessageStatus(String username, Long messageId, MessageStatus status) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable("messageId", String.valueOf(messageId))) {
            log.info("Changing message status to: {}", status);

            MessageDeliveryStatus messageDeliveryStatus = messageDeliveryStatusRepository.findByMessageIdAndRecipientUsername(messageId, username)
                                                                                         .orElseThrow(() -> {
                                                                                             log.warn("Message delivery status not found");
                                                                                             return new ApiException(MESSAGE_NOT_FOUND, "Message delivery status not found");
                                                                                         });

            // Validate status transition
            preSaveStatusTransition(messageDeliveryStatus.getStatus(), status);

            messageDeliveryStatus.setStatus(status);

            MessageDeliveryStatus savedMessageDeliveryStatus = messageDeliveryStatusRepository.save(messageDeliveryStatus);

            log.info("Message status updated successfully");

            return objectMapper.mapMessageDeliveryStatusToMessageDeliveryStatusResponse(savedMessageDeliveryStatus);
        }
    }

    // Conversation Retrieval Methods
    @Override
    @Transactional(readOnly = true)
    public Page<PrivateConversationResponse> getUserPrivateChats(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try (MDC.MDCCloseable ignored = MDC.putCloseable(USERNAME_LOG, user.getUsername())) {
            log.info("Fetching private conversations,  page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Conversation> conversations = conversationRepository.findPrivateConversationsByUserUsername(user.getUsername(), pageable);

            if (conversations.isEmpty()) {
                log.info("No private conversations found for user");
            }

            return conversations.map(objectMapper::mapConversationToPrivateConversationResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupConversationResponse> getUserGroupChats(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try (MDC.MDCCloseable ignored = MDC.putCloseable(USERNAME_LOG, user.getUsername())) {
            log.info("Fetching group conversations,page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Conversation> conversations = conversationRepository.findGroupConversationsByParticipantUsername(user.getUsername(), pageable);


            if (conversations.isEmpty()) {
                log.info("No group conversations found for user");
            }

            return conversations.map(objectMapper::mapConversationToGroupConversationResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long conversationId) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(conversationId))) {
            log.info("Fetching conversation details");

            Conversation conversation = findOrThrowHelper.findConversationOrThrow(conversationId);

            log.info("Successfully retrieved conversation of type: {}", conversation.getType());
            return objectMapper.mapConversationToConversationResponse(conversation);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getUserConversations(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try (MDC.MDCCloseable ignored = MDC.putCloseable(USERNAME_LOG, user.getUsername())) {
            log.info("Fetching conversations for user, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Conversation> conversations = conversationRepository.findConversationsByParticipantUsername(user.getUsername(), pageable);

            if (conversations.isEmpty()) {
                log.warn("No conversations found for user");
            }

            return conversations.map(objectMapper::mapConversationToConversationResponse);
        }
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        try (MDC.MDCCloseable ignored = MDC.putCloseable(CONVERSATION_ID_LOG, String.valueOf(conversationId))) {
            log.info("Attempting to delete conversation");

            try {
                conversationRepository.deleteById(conversationId);
                log.info("Conversation deleted successfully");
            } catch (Exception e) {
                log.error("Failed to delete conversation", e);
                throw new ApiException(CONVERSATION_DELETION_FAILED, "Error deleting conversation: " + conversationId);
            }
        }
    }

    // Private Helper Methods

    private void preSaveGroupSettings(Conversation conversation, User user) {
        if (!GROUP.equals(conversation.getType())) {
            throw new ApiException(INVALID_CONVERSATION_TYPE, "Operation only allowed for group conversations");
        }
        if (!conversation.getGroupSettings().getAdmins().contains(user)) {
            throw new ApiException(ACCESS_DENIED, "User is not an admin in this group");
        }
    }

    private void preSaveStatusTransition(MessageStatus currentStatus, MessageStatus newStatus) {
        if (currentStatus == MessageStatus.DELIVERED && newStatus == MessageStatus.SENT) {
            throw new ApiException(INVALID_STATUS_TRANSITION, "Cannot change message status from " + currentStatus + " to " + newStatus);
        }
    }

    private void preSaveGroupMessage(Conversation conversation) {
        if (!conversation.getType().equals(GROUP)) {
            log.warn("Invalid conversation type for group message: {}", conversation.getId());
            throw new ApiException(INVALID_CONVERSATION_TYPE, "Conversation is not a group chat");
        }

        if (!conversation.getParticipants().contains(findOrThrowHelper.findUserOrThrow("username"))) {
            log.warn("User is not a participant in group conversation: {}", conversation.getId());
            throw new ApiException(USER_NOT_IN_GROUP, "User is not a participant in this group");
        }
        log.info("Group message request validated successfully for conversation: {}", conversation.getId());
    }
}

/**
 * @Service
 * @Transactional
 * public class MessageDeliveryService {
 *     private final MessageDeliveryStatusRepository deliveryStatusRepository;
 *
 *     public MessageDeliveryService(MessageDeliveryStatusRepository deliveryStatusRepository) {
 *         this.deliveryStatusRepository = deliveryStatusRepository;
 *     }
 *
 *     public void markConversationAsRead(Long conversationId, String username) {
 *         deliveryStatusRepository.updateStatusesForConversation(
 *             conversationId,
 *             username,
 *             DeliveryStatus.UNREAD,
 *             DeliveryStatus.READ
 *         );
 *     }
 *
 *     public Map<Long, Long> getUnreadCounts(String username) {
 *         return deliveryStatusRepository.countUnreadMessagesPerConversation(username)
 *             .stream()
 *             .collect(Collectors.toMap(
 *                 UnreadMessageCount::getConversationId,
 *                 UnreadMessageCount::getUnreadCount
 *             ));
 *     }
 * }
 */