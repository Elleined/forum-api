package com.elleined.forumapi.controller;

import com.elleined.forumapi.dto.UserDTO;
import com.elleined.forumapi.mapper.UserMapper;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.service.ForumService;
import com.elleined.forumapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final ForumService forumService;

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDTO save(@Valid @RequestBody UserDTO userDTO) {
        return forumService.saveUser(userDTO);
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable("id") int id) {
        User user = userService.getById(id);
        return userMapper.toDTO(user);
    }

    @GetMapping("/{currentUserId}/getSuggestedMentions")
    public List<UserDTO> getSuggestedMentions(@PathVariable("currentUserId") int currentUserId,
                                              @RequestParam("name") String name) {
        return forumService.getSuggestedMentions(currentUserId, name);
    }

    @GetMapping("/uuid/{uuid}")
    public UserDTO getByUUID(@PathVariable("uuid") String uuid) {
        User user = userService.getByUUID(uuid);
        return userMapper.toDTO(user);
    }
}
