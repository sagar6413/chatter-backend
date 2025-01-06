package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.*;
import com.chatapp.backend.entity.*;
import com.chatapp.backend.exception.ApiException;
import com.chatapp.backend.exception.ErrorCode;
import com.chatapp.backend.repository.ChatRepository;
import com.chatapp.backend.repository.GroupMembershipRepository;
import com.chatapp.backend.repository.GroupRepository;
import com.chatapp.backend.repository.UserRepository;
import com.chatapp.backend.service.GroupService;
import com.chatapp.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chatapp.backend.entity.GroupRole.ADMIN;
import static com.chatapp.backend.entity.GroupRole.MEMBER;
import static com.chatapp.backend.exception.ErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;
import static com.chatapp.backend.exception.ErrorCode.GROUP_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserService userService;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Group createGroup(CreateGroupRequestDTO createGroupRequestDTO) {
        log.info("Creating new group: {}", createGroupRequestDTO.name());

        User creator = userService.getUserById(createGroupRequestDTO.creatorId());

        Chat chat = Chat.builder()
                        .type(ChatType.GROUP)
                        .createdAt(Instant.now())
                        .build();
        Chat savedChat = chatRepository.save(chat);


        Group group = Group.builder()
                           .name(createGroupRequestDTO.name())
                           .creator(creator)
                           .chat(savedChat)
                           .createdAt(Instant.now())
                           .build();
        Group savedGroup = groupRepository.save(group);


        //Add creator as a member (admin)
        GroupParticipant creatorMembership = GroupParticipant.builder()
                                                             .user(creator)
                                                             .group(savedGroup)
                                                             .role(ADMIN)
                                                             .joinedAt(Instant.now())
                                                             .isActive(true)
                                                             .build();

        groupMembershipRepository.save(creatorMembership);

        addMembersToGroup(
                AddGroupMembersRequestDTO.builder()
                                         .groupId(savedGroup.getId())
                                         .memberIds(createGroupRequestDTO.memberIds())
                                         .build()
        );

        // Notify group members about the new group
//        group.getMembers().forEach(member ->
//                messagingTemplate.convertAndSendToUser(
//                        member.getUserId().toString(),
//                        "/queue/groups",
//                        GroupNotification.created(group)
//                )
//        );


        log.info("Group created successfully with ID: {}", savedGroup.getId());
        return savedGroup;
    }

    /**
     * Creates a new group with specified members.
     * Creator is automatically assigned as admin.
     */
    @Transactional
    public GroupDTO createGroupTest(CreateGroupRequestDTO request) {
        // Create chat first
        Chat chat = Chat.builder()
                        .type(ChatType.GROUP)
                        .createdAt(Instant.now())
                        .build();
        Chat savedChat = chatRepository.save(chat);

        // Create group
        Group group = Group.builder()
                           .chat(savedChat)
                           .name(request.name())
                           .description(request.description())
                           .creator(userRepository.getReferenceById(request.creatorId()))
                           .createdAt(Instant.now())
                           .maxMembers(256)
                           .build();
        Group savedGroup = groupRepository.save(group);

        // Add members
        Set<GroupParticipant> memberships = new HashSet<>();
        request.memberIds().forEach(userId -> {
            User user = userRepository.getReferenceById(userId);
            boolean isAdmin = userId.equals(request.creatorId());

            GroupParticipant membership = GroupParticipant.builder()
                                                          .group(savedGroup)
                                                          .user(user)
                                                          .role(isAdmin ? ADMIN : MEMBER)
                                                          .joinedAt(Instant.now())
                                                          .isActive(true)
                                                          .build();
            memberships.add(membership);
        });
        savedGroup.setGroupParticipants(memberships);

        return mapToGroupDTO(groupRepository.save(savedGroup));
    }

    @Transactional
    @Override
    public void addMembersToGroup(AddGroupMembersRequestDTO addGroupMembersRequestDTO) {
        log.info("Adding members to group: {}", addGroupMembersRequestDTO.groupId());

        Group group = groupRepository.findById(addGroupMembersRequestDTO.groupId())
                                     .orElseThrow(() -> new ApiException(GROUP_NOT_FOUND));

        List<User> members = addGroupMembersRequestDTO.memberIds().stream()
                                                      .map(userService::getUserById)
                                                      .toList();


        members.forEach(member -> {
            GroupParticipant membership = GroupParticipant.builder()
                                                          .user(member)
                                                          .group(group)
                                                          .role(MEMBER)
                                                          .joinedAt(Instant.now())
                                                          .isActive(true)
                                                          .build();
            groupMembershipRepository.save(membership);
        });

        // Notify new members
//        request.getUserIds().forEach(userId ->
//                messagingTemplate.convertAndSendToUser(
//                        userId.toString(),
//                        "/queue/groups",
//                        GroupNotification.added(updatedGroup)
//                )
//        );
    }


    /**
     * Adds new members to an existing group.
     * Verifies group capacity and member uniqueness.
     */
    @Transactional
    public GroupDTO addMembers(Long groupId, List<Long> userIds) {
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new ApiException(GROUP_NOT_FOUND));

        // Check group capacity
        if (group.getGroupParticipants().size() + userIds.size() > group.getMaxMembers()) {
            throw new ApiException((ErrorCode.GROUP_FULL));
        }

        // Add new members
        userIds.forEach(userId -> {
            if (group.getGroupParticipants().stream()
                     .noneMatch(m -> m.getUser().getId().equals(userId))) {
                User user = userRepository.getReferenceById(userId);
                GroupParticipant membership = GroupParticipant.builder()
                                                              .group(group)
                                                              .user(user)
                                                              .role(MEMBER)
                                                              .joinedAt(Instant.now())
                                                              .isActive(true)
                                                              .build();
                group.getGroupParticipants().add(membership);
            }
        });

        return mapToGroupDTO(groupRepository.save(group));
    }

    private GroupDTO mapToGroupDTO(Group save) {
        return GroupDTO.builder()
                       .id(save.getId())
                       .name(save.getName())
                       .description(save.getDescription())
                       .createdAt(save.getCreatedAt())
                       .members(save.getGroupParticipants().stream()
                                    .map(m -> UserDTO.builder()
                                                     .id(m.getUser().getId())
                                                     .username(m.getUser().getUsername())
                                                     .displayName(m.getUser().getDisplayName())
                                                     .build())
                                    .toList())
                       .build();
    }

    /**
     * Updates group information.
     * Only admins can perform this operation.
     */
    @Transactional
    @Override
    public GroupDTO updateGroup(Long groupId, UpdateGroupRequestDTO request) {
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new ApiException(GROUP_NOT_FOUND));

        group.setName(request.name());
        group.setDescription(request.description());

        // Update chat name to maintain consistency
        Chat chat = group.getChat();
        chatRepository.save(chat);

        return mapToGroupDTO(groupRepository.save(group));
    }

    @Transactional
    @Override
    public void removeMemberFromGroup(Long groupId, Long userId) {
        log.info("Removing user {} from group {}", userId, groupId);
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new ApiException(GROUP_NOT_FOUND));
        User user = userService.getUserById(userId);

        GroupParticipant membership = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId)
                                                               .orElseThrow(() -> new ApiException(GROUP_MEMBERSHIP_NOT_FOUND));
        membership.setActive(false);

//        // Notify removed member
//        messagingTemplate.convertAndSendToUser(
//                userId.toString(),
//                "/queue/groups",
//                GroupNotification.removed(groupId)
//        );
        groupMembershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getGroupMembers(Long groupId) {
        log.debug("Fetching members for group ID: {}", groupId);
        return groupMembershipRepository.findByGroupIdAndIsActiveTrue(groupId).stream()
                                        .map(GroupParticipant::getUser)
                                        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Group getGroupById(Long groupId) {
        log.debug("Fetching group by id {}", groupId);
        return groupRepository.findById(groupId).orElseThrow(() -> new ApiException(GROUP_NOT_FOUND));
    }


    @Transactional(readOnly = true)
    @Override
    public boolean isUserAdminOfGroup(Long userId, Long groupId) {
        log.debug("Checking if user {} is admin of group {}", userId, groupId);
        GroupParticipant groupParticipant = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId)
                                                                     .orElseThrow(() -> new ApiException(GROUP_MEMBERSHIP_NOT_FOUND));
        return groupParticipant.getRole().equals(ADMIN);
    }
}