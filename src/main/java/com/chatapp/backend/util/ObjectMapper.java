package com.chatapp.backend.util;

import com.chatapp.backend.dto.request.*;
import com.chatapp.backend.dto.response.*;
import com.chatapp.backend.entity.*;
import com.chatapp.backend.entity.enums.MediaType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chatapp.backend.entity.enums.ConversationType.GROUP;
import static com.chatapp.backend.entity.enums.MessageStatus.NOT_SENT;
import static com.chatapp.backend.entity.enums.UserStatus.OFFLINE;

@Component
public class ObjectMapper {
    //---------------------------------------------------------------------------------------------
    // User Mappings
    //---------------------------------------------------------------------------------------------

    public User mapSignUpDTOToUser(SignUpRequest request) {
        return User.builder()
                   .username(request.username())
                   .displayName(request.displayName())
                   .password(request.password())
                   .status(OFFLINE)
                   .lastSeenAt(Instant.now())
                   .emailVerified(true)
                   .isActive(true)
                   .build();
    }

    public UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                           .id(user.getId())
                           .username(user.getUsername())
                           .displayName(user.getDisplayName())
                           .status(user.getStatus())
                           .lastSeenAt(user.getLastSeenAt())
                           .preferences(mapUserPreferencesToUserPreferencesResponse(user.getPreferences()))
                           .createdAt(user.getCreatedAt())
                           .build();
    }

    public User mapUserRequestToUser(UserRequest request, User user) {
        Optional.ofNullable(request.username()).ifPresent(user::setUsername);
        Optional.ofNullable(request.displayName()).ifPresent(user::setDisplayName);
        Optional.ofNullable(request.preferences())
                .ifPresent(pref -> mapUserPreferenceRequestToUserPreferences(pref, user.getPreferences()));
        return user;
    }

    public UserPreferenceResponse mapUserPreferencesToUserPreferencesResponse(UserPreferences preferences) {
        return UserPreferenceResponse.builder()
                                     .notificationEnabled(preferences.isNotificationEnabled())
                                     .theme(preferences.getTheme())
                                     .build();
    }

    public UserPreferences mapUserPreferenceRequestToUserPreferences(UserPreferenceRequest request, UserPreferences preferences) {
        Optional.ofNullable(request.theme()).ifPresent(preferences::setTheme);
        preferences.setNotificationEnabled(request.notificationEnabled());
        return preferences;
    }

    //---------------------------------------------------------------------------------------------
    // Conversation Mappings
    //---------------------------------------------------------------------------------------------

    public PrivateConversationResponse mapConversationToPrivateConversationResponse(Conversation conversation) {
        return PrivateConversationResponse.builder()
                                          .conversationId(conversation.getId())
                                          // TODO:  This is a hack, fix it later when spring security is added by fetching loggedin user username
                                          .contact(mapUserToUserResponse(conversation.getParticipants()
                                                                                     .stream()
                                                                                     .filter(user -> !user.getUsername()
                                                                                                          .equals("LoggedInUserUserName"))
                                                                                     .toList()
                                                                                     .getFirst()))
                                          .lastMessage(mapMessageToMessageResponse(conversation.getLastMessage()))
                                          .createdAt(conversation.getCreatedAt())
                                          .updatedAt(conversation.getUpdatedAt())
                                          .build();
    }

    public GroupConversationResponse mapConversationToGroupConversationResponse(Conversation conversation) {
        GroupSettings groupSettings = conversation.getGroupSettings();
        return GroupConversationResponse.builder()
                                        .conversationId(conversation.getId())
                                        .participants(mapUsersToUserResponses(conversation.getParticipants()))
                                        .participantCount(conversation.getParticipantCount())
                                        .lastMessage(mapMessageToMessageResponse(conversation.getLastMessage()))
                                        .createdAt(conversation.getCreatedAt())
                                        .updatedAt(conversation.getUpdatedAt())
                                        .groupName(groupSettings.getName())
                                        .groupDescription(groupSettings.getDescription())
                                        .creatorName(groupSettings.getCreator().getDisplayName())
                                        .creatorUserName(groupSettings.getCreator().getUsername())
                                        .onlyAdminsCanSend(groupSettings.isOnlyAdminsCanSend())
                                        .messageRetentionDays(groupSettings.getMessageRetentionDays())
                                        .maxMembers(groupSettings.getMaxMembers())
                                        .isGroupPublic(groupSettings.isPublic())
                                        .build();
    }

    public ConversationResponse mapConversationToConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                                   .conversationId(conversation.getId())
                                   // TODO:  This is a hack, fix it later when spring security is added by fetching loggedin user username
                                   .contact(mapUserToUserResponse(conversation.getParticipants()
                                                                              .stream()
                                                                              .filter(user -> !user.getUsername()
                                                                                                   .equals("LoggedInUserUserName"))
                                                                              .toList()
                                                                              .getFirst()))
                                   .lastMessage(mapMessageToMessageResponse(conversation.getLastMessage()))
                                   .createdAt(conversation.getCreatedAt())
                                   .updatedAt(conversation.getUpdatedAt())
                                   .build();
    }

    public GroupSettings mapGroupSettingsRequestToGroupSettings(GroupSettingsRequest request, Conversation conversation, User creator, Set<User> admins) {
        return GroupSettings.builder()
                            .conversation(conversation)
                            .name(request.name())
                            .description(request.description())
                            .creator(creator)
                            .onlyAdminsCanSend(request.onlyAdminsCanSend())
                            .messageRetentionDays(request.messageRetentionDays())
                            .maxMembers(request.maxMembers())
                            .isPublic(request.isPublic())
                            .admins(admins)
                            .build();
    }

    public GroupSettings mapGroupSettingsRequestToGroupSettings(GroupSettingsRequest request) {
        return GroupSettings.builder()
                            .name(request.name())
                            .description(request.description())
                            .onlyAdminsCanSend(request.onlyAdminsCanSend())
                            .messageRetentionDays(request.messageRetentionDays())
                            .maxMembers(request.maxMembers())
                            .isPublic(request.isPublic())
                            .build();
    }

    public GroupSettingsResponse mapGroupSettingsToGroupSettingsResponse(GroupSettings groupSettings) {
        return new GroupSettingsResponse(groupSettings.getName(), groupSettings.getDescription(), groupSettings.getCreator(), groupSettings.isOnlyAdminsCanSend(), groupSettings.getMessageRetentionDays(), groupSettings.getMaxMembers(), groupSettings.isPublic(), groupSettings.getAdmins());
    }

    public Conversation mapGroupRequestToConversation(GroupRequest request, Set<User> participants) {
        return Conversation.builder()
                           .type(GROUP)
                           .participants(participants)
                           .participantCount(participants.size())
                           .groupSettings(mapGroupSettingsRequestToGroupSettings(request.groupSettings()))
                           .createdAt(Instant.now())
                           .build();
    }


    //---------------------------------------------------------------------------------------------
    // Message Mappings
    //---------------------------------------------------------------------------------------------

    public Message mapMessageRequestToMessage(MessageRequest request, User sender, Conversation conversation) {
        Message message = Message.builder()
                                 .content(request.content())
                                 .sender(sender)
                                 .deliveryStatuses(conversation.getParticipants()
                                                               .stream()
                                                               .filter(user -> !user.getUsername()
                                                                                    .equals("LOGGED_IN_USER"))//TODO : Replace with authenticated user from spring context once implemented Spring security
                                                               .map(user -> MessageDeliveryStatus.builder()
                                                                                                 .recipient(user)
                                                                                                 .status(NOT_SENT)
                                                                                                 .statusTimestamp(Instant.now())
                                                                                                 .build())
                                                               .collect(Collectors.toSet()))
                                 .conversation(conversation)
                                 .build();

        message.initializeDeliveryStatus(conversation.getParticipants());
        return message;
    }

    public MessageResponse mapMessageToMessageResponse(Message message) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                              .id(message.getId())
                              .conversationId(message.getConversation().getId())
                              .senderUsername(message.getSender().getUsername())
                              .senderDisplayName(message.getSender().getDisplayName())
                              .content(message.getContent())
                              .mediaItems(mapMediaItemsToMediaResponses(message.getMediaItems()))
                              .type(message.getType())
                              .reactions(mapReactionsToReactionResponses(message.getReactions()))
                              .createdAt(message.getCreatedAt())
                              .editedAt(message.getEditedAt())
                              .deliveryStatus(message.getDeliveryStatuses()
                                                     .stream()
                                                     .map(this::mapMessageDeliveryStatusToMessageDeliveryStatusResponse)
                                                     .collect(Collectors.toSet()))
                              .build();
    }

    //---------------------------------------------------------------------------------------------
    // Media Mappings
    //---------------------------------------------------------------------------------------------

    public Media mapMediaRequestToMedia(MediaUploadRequest request) {
        return Media.builder()
                    .fileName(request.fileName())
                    .fileSize(request.fileSize())
                    .fileType(MediaType.valueOf(request.fileType())) // Assuming fileType is stored as an Enum in Media
                    .checksum(request.checksum())
                    .build();
    }

    public MediaResponse mapMediaToMediaResponse(Media media) {
        return MediaResponse.builder()
                            .id(media.getId())
                            .fileName(media.getFileName())
                            .fileType(media.getFileType().name()) // Convert Enum back to String for DTO
                            .fileSize(media.getFileSize())
                            .uploadStatus(media.getUploadStatus())
                            .metadata(media.getMetadata())
                            .build();
    }

    //---------------------------------------------------------------------------------------------
    // Reaction Mappings
    //---------------------------------------------------------------------------------------------

    public ReactionResponse mapReactionToReactionResponse(Reaction reaction) {
        return ReactionResponse.builder()
                               .id(reaction.getId())
                               .username(reaction.getUser().getUsername())
                               .displayName(reaction.getUser().getDisplayName())
                               .type(reaction.getType())
                               .build();
    }

    //---------------------------------------------------------------------------------------------
    // Helper Methods for Collections
    //---------------------------------------------------------------------------------------------

    private Set<UserResponse> mapUsersToUserResponses(Set<User> users) {
        return users.stream().map(this::mapUserToUserResponse).collect(Collectors.toSet());
    }

    private Set<MediaResponse> mapMediaItemsToMediaResponses(Set<Media> mediaItems) {
        return mediaItems.stream().map(this::mapMediaToMediaResponse).collect(Collectors.toSet());
    }

    private Set<ReactionResponse> mapReactionsToReactionResponses(Set<Reaction> reactions) {
        return reactions.stream().map(this::mapReactionToReactionResponse).collect(Collectors.toSet());
    }

    //---------------------------------------------------------------------------------------------
    // Delivery Status Mapping
    //---------------------------------------------------------------------------------------------

    public MessageDeliveryStatusResponse mapMessageDeliveryStatusToMessageDeliveryStatusResponse(MessageDeliveryStatus deliveryStatus) {
        return MessageDeliveryStatusResponse.builder()
                                            .messageDeliveryStatusId(deliveryStatus.getId())
                                            .statusTimestamp(deliveryStatus.getStatusTimestamp())
                                            .recipient(mapUserToUserResponse(deliveryStatus.getRecipient()))
                                            .status(deliveryStatus.getStatus())
                                            .build();
    }
}
