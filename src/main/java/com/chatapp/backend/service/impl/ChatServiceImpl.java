package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.*;
import com.chatapp.backend.entity.*;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.exception.ErrorCode;
import com.chatapp.backend.repository.ChatRepository;
import com.chatapp.backend.repository.MessageRepository;
import com.chatapp.backend.repository.UnreadMessageRepository;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.ChatService;
import com.chatapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.chatapp.backend.exception.ErrorCode.CHAT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageRepository unreadMessageRepository;

    @Transactional
    @Override
    public ChatDTO createChat(CreateChatRequestDTO request) {
        if (request.type() == ChatType.PRIVATE && request.participantIds().size() == 2) {
            Optional<Chat> existingChat = chatRepository.findOneToOneChatByParticipants(
                    request.participantIds().get(0),
                    request.participantIds().get(1)
            );
            if (existingChat.isPresent()) {
                return mapToChatDTO(existingChat.get());
            }
        }

        List<User> participants = userRepository.findAllById(request.participantIds());
        if (participants.size() != request.participantIds().size()) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Chat chat = Chat.builder()
                        .type(request.type())
                        .createdAt(Instant.now())
                        .build();

        Chat savedChat = chatRepository.save(chat);
        participants.forEach(participant -> {
            GroupParticipant membership = GroupParticipant.builder()
                                                          .user(participant)
                                                          .group(savedChat.getGroup())
                                                          .role(GroupRole.MEMBER)
                                                          .isActive(true)
                                                          .joinedAt(Instant.now())
                                                          .build();
            savedChat.getGroup().getGroupParticipants().add(membership);
        });

        return mapToChatDTO(chatRepository.save(savedChat));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatDTO> getUserChats(Long userId, Boolean unreadOnly) {
        List<Chat> chats = Boolean.TRUE.equals(unreadOnly)
                ? chatRepository.findChatsWithUnreadMessages(userId)
                : chatRepository.findChatsByUserId(userId);

        return chats.stream()
                    .map(this::getChatDTO)
                    .collect(Collectors.toList());
    }



    private ChatDTO getChatDTO(Chat chat) {
        Message latestMessage = messageRepository.findLatestMessage(chat.getId());
        return ChatDTO.builder()
                      .id(chat.getId())
                      .type(chat.getType())
                      .participants(mapToUserDTOs(chat.getGroup().getGroupParticipants().stream().map(GroupParticipant::getUser).collect(Collectors.toList())))
                      .latestMessage(latestMessage != null ? mapToMessageDto(latestMessage) : null)
                      .unreadCount((long) chat.getUnreadMessages().size())
                      .build();
    }

    @Transactional
    @Override
    public void sendPrivateMessage(MessageDTO messageDTO) {
        User sender = userService.getUserById(messageDTO.senderId());
        User recipient = userService.getUserById(messageDTO.receiverId());
        Chat chat = findOrCreateOneToOneChat(sender, recipient);

        Message message = Message.builder()
                                 .sender(sender)
                                 .chat(chat)
                                 .content(messageDTO.content())
                                 .createdAt(Instant.now())
                                 .build();

        Message savedMessage = messageRepository.save(message);
        MessageResponseDTO responseDTO = mapToResponseDTO(savedMessage);

        // Send to recipient's private queue
        messagingTemplate.convertAndSendToUser(
                String.valueOf(recipient.getId()),
                "/queue/private",
                responseDTO
        );

        // Handle offline recipient
        if (isUserOffline(recipient)) {
            createUnreadMessage(savedMessage, recipient);
        }
    }

    @Transactional
    public Chat findOrCreateOneToOneChat(User user1, User user2) {
        log.info("Finding or creating one-to-one chat between users {} and {}", user1.getId(), user2.getId());

        return chatRepository.findOneToOneChatByParticipants(user1.getId(), user2.getId())
                             .orElseGet(() -> {
                                 Chat chat = Chat.builder()
                                                 .type(ChatType.PRIVATE)
                                                 .createdAt(Instant.now())
                                                 .build();

                                 Chat savedChat = chatRepository.save(chat);

                                 // Add both users as participants
                                 List.of(user1, user2).forEach(user -> {
                                     GroupParticipant membership = GroupParticipant.builder()
                                                                                   .user(user)
                                                                                   .group(savedChat.getGroup())
                                                                                   .role(GroupRole.MEMBER)
                                                                                   .isActive(true)
                                                                                   .joinedAt(Instant.now())
                                                                                   .build();
                                     savedChat.getGroup().getGroupParticipants().add(membership);
                                 });

                                 return chatRepository.save(savedChat);
                             });
    }

    @Transactional
    @Override
    public void saveAndProcessGroupMessage(Long chatId, MessageDTO messageDTO) {
        Chat chat = chatRepository.findById(chatId)
                                  .orElseThrow(() -> new ApiException(CHAT_NOT_FOUND));

        Message message = Message.builder()
                                 .chat(chat)
                                 .sender(userRepository.getReferenceById(messageDTO.senderId()))
                                 .content(messageDTO.content())
                                 .createdAt(Instant.now())
                                 .build();

        Message savedMessage = messageRepository.save(message);
        MessageResponseDTO responseDTO = mapToResponseDTO(savedMessage);

        // Create unread entries and notify online participants
        chat.getGroup().getGroupParticipants().stream()
            .filter(m -> !m.getUser().getId().equals(messageDTO.senderId()))
            .forEach(member -> {
                if (isUserOffline(member.getUser())) {
                    createUnreadMessage(savedMessage, member.getUser());
                }
                // Notify online users
                messagingTemplate.convertAndSendToUser(
                        member.getUser().getId().toString(),
                        "/queue/messages",
                        responseDTO
                );
            });
    }

    @Transactional
    @Override
    public void markMessagesAsRead(Long chatId, Long lastReadMessageId, Long userId) {
        List<UnreadMessage> unreadMessages = unreadMessageRepository
                .findUnreadMessagesForUserAndChat(userId, chatId, lastReadMessageId);

        unreadMessages.forEach(unreadMessage -> {
            unreadMessage.setReceivedAt(Instant.now());
            unreadMessageRepository.save(unreadMessage);

            // Send read receipt to message sender
            Message message = unreadMessage.getMessage();
            messagingTemplate.convertAndSendToUser(
                    message.getSender().getId().toString(),
                    "/queue/read-receipts",
                    new ReadReceiptDto(message.getId(), userId, LocalDateTime.now())
            );
        });
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO> getChatHistory(Long chatId) {
        return messageRepository.findByChatId(chatId).stream()
                                .map(this::mapToChatMessageDTO)
                                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean isUserOffline(User user) {
        return user.getLastActiveAt().isBefore(Instant.now().minusSeconds(60));
    }

    private void createUnreadMessage(Message message, User recipient) {
        UnreadMessage unreadMessage = UnreadMessage.builder()
                                                   .message(message)
                                                   .recipient(recipient)
                                                   .createdAt(Instant.now())
                                                   .build();
        unreadMessageRepository.save(unreadMessage);
    }

    // Add these methods to the ChatServiceImpl class

    private ChatDTO mapToChatDTO(Chat chat) {
        return getChatDTO(chat);
    }

    private List<UserDTO> mapToUserDTOs(List<User> users) {
        return users.stream()
                          .map(user -> UserDTO.builder()
                                                    .username(user.getUsername())
                                                    .displayName(user.getDisplayName())
                                                    .build())
                          .collect(Collectors.toList());
    }

    private List<MessageDTO> mapToMessageDto(Message message) {
        return List.of(MessageDTO.builder()
                                 .id(message.getId())
                                 .chatId(message.getChat().getId())
                                 .senderId(message.getSender().getId())
                                 .content(message.getContent())
                                 .createdAt(message.getCreatedAt())
                                 .build());
    }

    private ChatMessageDTO mapToChatMessageDTO(Message message) {
        return ChatMessageDTO.builder()
                             .id(message.getId())
                             .chatId(message.getChat().getId())
                             .senderId(message.getSender().getId())
                             .content(message.getContent())
                             .createdAt(message.getCreatedAt())
                             .build();
    }

    private MessageResponseDTO mapToResponseDTO(Message message) {
        return MessageResponseDTO.builder()
                                 .id(message.getId())
                                 .chatId(message.getChat().getId())
                                 .senderId(message.getSender().getId())
                                 .content(message.getContent())
                                 .createdAt(message.getCreatedAt())
                                 .build();
    }

    private GroupDTO mapToGroupDTO(Group group) {
        return GroupDTO.builder()
                       .id(group.getId())
                       .name(group.getName())
                       .description(group.getDescription())
                       .members(mapToUserDTOs(group.getGroupParticipants().stream().map(GroupParticipant::getUser).collect(Collectors.toList())))
                       .createdAt(group.getCreatedAt())
                       .build();
    }
}