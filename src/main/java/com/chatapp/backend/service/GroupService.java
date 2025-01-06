package com.chatapp.backend.service;


import com.chatapp.backend.dto.AddGroupMembersRequestDTO;
import com.chatapp.backend.dto.CreateGroupRequestDTO;
import com.chatapp.backend.dto.GroupDTO;
import com.chatapp.backend.dto.UpdateGroupRequestDTO;
import com.chatapp.backend.entity.Group;
import com.chatapp.backend.entity.User;
import jakarta.validation.Valid;

import java.util.List;

public interface GroupService {
    Group createGroup(@Valid CreateGroupRequestDTO createGroupRequestDTO);

    void addMembersToGroup(@Valid AddGroupMembersRequestDTO addGroupMembersRequestDTO);

    void removeMemberFromGroup(Long groupId, Long userId);

    List<User> getGroupMembers(Long groupId);

    Group getGroupById(Long groupId);

    boolean isUserAdminOfGroup(Long userId, Long groupId);

    GroupDTO updateGroup(Long groupId, UpdateGroupRequestDTO request);
}
