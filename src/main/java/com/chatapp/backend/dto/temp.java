package com.chatapp.backend.dto.request;

import com.chatapp.backend.entity.enums.ConversationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class CreateConversationRequest {
    @NotNull(message = "Conversation type is required")
    private ConversationType type;

    @NotEmpty(message = "Participant IDs are required")
    @Size(min = 1, max = 256, message = "Number of participants must be between 1 and 256")
    private Set<Long> participantIds;

    // Only required for group conversations
    private GroupSettingsRequest groupSettings;
}

@Data
public class GroupSettingsRequest {
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private boolean onlyAdminsCanSend;

    @Min(value = 1, message = "Message retention days must be at least 1")
    @Max(value = 365, message = "Message retention days cannot exceed 365")
    private Integer messageRetentionDays;

    @Min(value = 2, message = "Maximum members must be at least 2")
    @Max(value = 256, message = "Maximum members cannot exceed 256")
    private Integer maxMembers;

    private boolean isPublic;
}

@Data
public class SendMessageRequest {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @Size(max = 4000, message = "Message content cannot exceed 4000 characters")
    private String content;

    private Set<MediaUploadRequest> mediaItems;
}

@Data
public class MediaUploadRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be greater than 0")
    @Max(value = 104857600, message = "File size cannot exceed 100MB") // 100MB limit
    private Long fileSize;

    @NotBlank(message = "File type is required")
    private String fileType;

    @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "Invalid checksum format")
    private String checksum;
}

@Data
public class UserRegistrationRequest {
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "Username must be alphanumeric and between 3-50 characters")
    private String username;

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long and contain at least one digit, " +
                    "one uppercase letter, one lowercase letter, and one special character"
    )
    private String password;

    private UserPreferencesRequest preferences;
}

@Data
public class UserPreferencesRequest {
    private boolean notificationEnabled = true;

    @Pattern(regexp = "^(light|dark)$", message = "Theme must be either 'light' or 'dark'")
    private String theme = "light";
}

// Response DTOs
package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.*;
        import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {
    private Long id;
    private ConversationType type;
    private Set<UserSummaryResponse> participants;
    private int participantCount;
    private MessageResponse lastMessage;
    private GroupSettingsResponse groupSettings;
    private Instant createdAt;
    private Instant updatedAt;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupSettingsResponse {
    private String name;
    private String description;
    private UserSummaryResponse creator;
    private boolean onlyAdminsCanSend;
    private Integer messageRetentionDays;
    private Integer maxMembers;
    private boolean isPublic;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private UserSummaryResponse sender;
    private String content;
    private Set<MediaResponse> mediaItems;
    private MessageType type;
    private MessageStatus status;
    private Set<ReactionResponse> reactions;
    private Instant createdAt;
    private Instant editedAt;
    private DeliveryStatusSummary deliveryStatus;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadUrl;
    private MediaUploadStatus uploadStatus;
    private String metadata;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStatusSummary {
    private int totalRecipients;
    private int readCount;
    private int deliveredCount;
    private Set<UserSummaryResponse> unreadRecipients;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionResponse {
    private Long id;
    private UserSummaryResponse user;
    private ReactionType type;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String displayName;
    private UserStatus status;
    private Instant lastSeenAt;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponse {
    private Long id;
    private String username;
    private String displayName;
    private UserStatus status;
    private Instant lastSeenAt;
    private int unreadMessagesCount;
    private UserPreferencesResponse preferences;
    private Instant createdAt;
}

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPreferencesResponse {
    private boolean notificationEnabled;
    private String theme;
}