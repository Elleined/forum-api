package com.elleined.forumapi.controller.user.friend;

import com.elleined.forumapi.model.User;
import com.elleined.forumapi.model.friend.FriendRequest;
import com.elleined.forumapi.service.UserService;
import com.elleined.forumapi.service.friend.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{currentUserId}/friend-requests")
public class FriendRequestController {

    private final FriendService friendService;
    private final UserService userService;

    @GetMapping
    public Set<FriendRequest> getAllFriendRequests(@PathVariable("currentUserId") int currentUserId) {
        User currentUser = userService.getById(currentUserId);
        return friendService.getAllFriendRequests(currentUser);
    }

    @PostMapping("/send/{userToAddId}")
    public void sendFriendRequest(@PathVariable("currentUserId") int currentUserId,
                                  @PathVariable("userToAddId") int userToAddId) {
        User currentUser = userService.getById(currentUserId);
        User userToAdd = userService.getById(userToAddId);
        friendService.sendFriendRequest(currentUser, userToAdd);
    }

    @PatchMapping("/{friendRequestId}/accept")
    public void acceptFriendRequest(@PathVariable("currentUserId") int currentUserId,
                                    @PathVariable("friendRequestId") int friendRequestId) {
        User currentUser = userService.getById(currentUserId);
        FriendRequest friendRequest = friendService.getById(friendRequestId);
        friendService.acceptFriendRequest(currentUser, friendRequest);
    }
}