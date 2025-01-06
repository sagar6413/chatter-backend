package com.chatapp.backend.controller;

import com.chatapp.backend.dto.AddGroupMembersRequestDTO;
import com.chatapp.backend.dto.CreateGroupRequestDTO;
import com.chatapp.backend.dto.GroupDTO;
import com.chatapp.backend.dto.UpdateGroupRequestDTO;
import com.chatapp.backend.entity.Group;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody CreateGroupRequestDTO createGroupRequestDTO) {
        log.info("Creating new group: {}", createGroupRequestDTO);
        Group createdGroup = groupService.createGroup(createGroupRequestDTO);
        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }

    @PostMapping("/add-members")
    public ResponseEntity<Void> addMembersToGroup(@Valid @RequestBody AddGroupMembersRequestDTO addGroupMembersRequestDTO) {
        groupService.addMembersToGroup(addGroupMembersRequestDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMemberFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMemberFromGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<User>> getGroupMembers(@PathVariable Long groupId) {
        List<User> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @GetMapping("/{groupId}/isAdmin")
    public ResponseEntity<Boolean> isUserAdminOfGroup(@RequestParam Long userId, @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.isUserAdminOfGroup(userId, groupId));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(
            @PathVariable Long groupId,
            @RequestBody UpdateGroupRequestDTO request) {
        log.info("Updating group {}: {}", groupId, request);
        GroupDTO updatedGroup = groupService.updateGroup(groupId, request);

        // Notify all group members about the update
//        updatedGroup.getMembers().forEach(member ->
//                messagingTemplate.convertAndSendToUser(
//                        member.getUserId().toString(),
//                        "/queue/groups",
//                        GroupNotification.updated(updatedGroup)
//                )
//        );

        return ResponseEntity.ok(updatedGroup);
    }
}