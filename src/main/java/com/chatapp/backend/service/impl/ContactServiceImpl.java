package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.*;
import com.chatapp.backend.entity.*;
import com.chatapp.backend.entity.enums.ConversationType;
import com.chatapp.backend.entity.enums.GroupRole;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.exception.ErrorCode;
import com.chatapp.backend.repository.ContactRepository;
import com.chatapp.backend.repository.MessageRepository;
import com.chatapp.backend.repository.UnreadMessageRepository;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.ContactService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {
    private final MessageRepository messageRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UnreadMessageRepository unreadMessageRepository;

    @Transactional
    @Override
    public ContactDTO createContact(CreateContactRequestDTO request) {
        if (request.type() == ConversationType.PRIVATE && request.participantIds().size() == 2) {
            Optional<Conversation> existingContact = contactRepository.findOneToOneContactByParticipants(
                    request.participantIds().get(0),
                    request.participantIds().get(1)
            );
            if (existingContact.isPresent()) {
                return mapToContactDTO(existingContact.get());
            }
        }

        List<User> participants = userRepository.findAllById(request.participantIds());
        if (participants.size() != request.participantIds().size()) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Conversation conversation = Conversation.builder()
                                                .type(request.type())
                                                .createdAt(Instant.now())
                                                .build();

        Conversation savedConversation = contactRepository.save(conversation);
        participants.forEach(participant -> {
            GroupSetting membership = GroupSetting.builder()
                                                  .user(participant)
                                                  .group(savedConversation.getGroup())
                                                  .role(GroupRole.MEMBER)
                                                  .isActive(true)
                                                  .joinedAt(Instant.now())
                                                  .build();
            savedConversation.getGroup().getGroupParticipants().add(membership);
        });

        return mapToContactDTO(contactRepository.save(savedConversation));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ContactDTO> getUserContacts(Long userId, Boolean unreadOnly) {
        List<Conversation> conversations = Boolean.TRUE.equals(unreadOnly)
                ? contactRepository.findContactsWithUnreadMessages(userId)
                : contactRepository.findContactsByUserId(userId);

        return conversations.stream()
                            .map(this::getContactDTO)
                            .collect(Collectors.toList());
    }



    private ContactDTO getContactDTO(Conversation conversation) {
        Message latestMessage = messageRepository.findLatestMessage(conversation.getContact_id());
        return ContactDTO.builder()
                         .id(conversation.getContact_id())
                         .type(conversation.getType())
                         .participants(mapToUserDTOs(conversation.getGroup().getGroupParticipants().stream().map(GroupSetting::getUser).collect(Collectors.toList())))
                         .latestMessage(latestMessage != null ? mapToMessageDto(latestMessage) : null)
                         .unreadCount((long) conversation.getUnreadMessages().size())
                         .build();
    }

    @Transactional
    @Override
    public void sendPrivateMessage(MessageDTO messageDTO) {
        User sender = userService.getUserById(messageDTO.senderId());
        User recipient = userService.getUserById(messageDTO.receiverId());
        Conversation conversation = findOrCreateOneToOneContact(sender, recipient);

        Message message = Message.builder()
                                 .sender(sender)
                                 .conversation(conversation)
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
    public Conversation findOrCreateOneToOneContact(User user1, User user2) {
        log.info("Finding or creating one-to-one contact between users {} and {}", user1.getId(), user2.getId());

        return contactRepository.findOneToOneContactByParticipants(user1.getId(), user2.getId())
                             .orElseGet(() -> {
                                 Conversation conversation = Conversation.builder()
                                                                         .type(ConversationType.PRIVATE)
                                                                         .createdAt(Instant.now())
                                                                         .build();

                                 Conversation savedConversation = contactRepository.save(conversation);

                                 // Add both users as participants
                                 List.of(user1, user2).forEach(user -> {
                                     GroupSetting membership = GroupSetting.builder()
                                                                           .user(user)
                                                                           .group(savedConversation.getGroup())
                                                                           .role(GroupRole.MEMBER)
                                                                           .isActive(true)
                                                                           .joinedAt(Instant.now())
                                                                           .build();
                                     savedConversation.getGroup().getGroupParticipants().add(membership);
                                 });

                                 return contactRepository.save(savedConversation);
                             });
    }

    @Transactional
    @Override
    public void saveAndProcessGroupMessage(Long contactId, MessageDTO messageDTO) {
        Conversation conversation = contactRepository.findById(contactId)
                                                     .orElseThrow(() -> new ApiException(CONTACT_NOT_FOUND));

        Message message = Message.builder()
                                 .conversation(conversation)
                                 .sender(userRepository.getReferenceById(messageDTO.senderId()))
                                 .content(messageDTO.content())
                                 .createdAt(Instant.now())
                                 .build();

        Message savedMessage = messageRepository.save(message);
        MessageResponseDTO responseDTO = mapToResponseDTO(savedMessage);

        // Create unread entries and notify online participants
        conversation.getGroup().getGroupParticipants().stream()
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
    public void markMessagesAsRead(Long contactId, Long lastReadMessageId, Long userId) {
        List<UnreadMessage> unreadMessages = unreadMessageRepository
                .findUnreadMessagesForUserAndContact(userId, contactId, lastReadMessageId);

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
    public List<ContactMessageDTO> getContactHistory(Long contactId) {
        return messageRepository.findByContactId(contactId).stream()
                                .map(this::mapToContactMessageDTO)
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

    // Add these methods to the ContactServiceImpl class

    private ContactDTO mapToContactDTO(Conversation conversation) {
        return getContactDTO(conversation);
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
                                 .contactId(message.getConversation().getContact_id())
                                 .senderId(message.getSender().getId())
                                 .content(message.getContent())
                                 .createdAt(message.getCreatedAt())
                                 .build());
    }

    private ContactMessageDTO mapToContactMessageDTO(Message message) {
        return ContactMessageDTO.builder()
                                .id(message.getId())
                                .contactId(message.getConversation().getContact_id())
                                .senderId(message.getSender().getId())
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build();
    }

    private MessageResponseDTO mapToResponseDTO(Message message) {
        return MessageResponseDTO.builder()
                                 .id(message.getId())
                                 .contactId(message.getConversation().getContact_id())
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
                       .members(mapToUserDTOs(group.getGroupParticipants().stream().map(GroupSetting::getUser).collect(Collectors.toList())))
                       .createdAt(group.getCreatedAt())
                       .build();
    }
}