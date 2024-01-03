package com.elleined.forumapi.controller.reply;

import com.elleined.forumapi.dto.ReactionDTO;
import com.elleined.forumapi.mapper.react.ReplyReactionMapper;
import com.elleined.forumapi.model.Reply;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.model.react.Emoji;
import com.elleined.forumapi.model.react.ReplyReact;
import com.elleined.forumapi.service.ReplyService;
import com.elleined.forumapi.service.UserService;
import com.elleined.forumapi.service.emoji.EmojiService;
import com.elleined.forumapi.service.react.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/{currentUserId}/posts/comments/{commentId}/replies/{replyId}/reactions")
public class ReplyReactionController {
    private final UserService userService;

    private final ReplyService replyService;
    private final ReactionService<Reply, ReplyReact> replyReactionService;
    private final ReplyReactionMapper replyReactionMapper;

    private final EmojiService emojiService;

    @GetMapping
    public List<ReactionDTO> getAll(@PathVariable("replyId") int replyId) {
        Reply reply = replyService.getById(replyId);
        return replyReactionService.getAll(reply).stream()
                .map(replyReactionMapper::toDTO)
                .toList();
    }

    @GetMapping("/type")
    public List<ReactionDTO> getAllReactionByEmojiType(@PathVariable("replyId") int replyId,
                                                       @RequestParam("type") Emoji.Type type) {
        Reply reply = replyService.getById(replyId);
        return replyReactionService.getAllReactionByEmojiType(reply, type).stream()
                .map(replyReactionMapper::toDTO)
                .toList();
    }

    @PostMapping
    public ReactionDTO save(@PathVariable("currentUserId") int currentUserId,
                            @PathVariable("replyId") int replyId,
                            @RequestParam("type") Emoji.Type type) {
        User currentUser = userService.getById(currentUserId);
        Reply reply = replyService.getById(replyId);
        Emoji emoji = emojiService.getByType(type);

        if (currentUser.isAlreadyReactedTo(reply)) {
            ReplyReact replyReact = replyReactionService.getByUserReaction(currentUser, reply);
            replyReactionService.update(currentUser, reply, replyReact, emoji);
            return replyReactionMapper.toDTO(replyReact);
        }
        ReplyReact replyReact = replyReactionService.save(currentUser, reply, emoji);
        return replyReactionMapper.toDTO(replyReact);
    }

    @DeleteMapping("/{reactionId}")
    public void delete(@PathVariable("currentUserId") int currentUserId,
                       @PathVariable("replyId") int replyId,
                       @PathVariable("reactionId") int reactionId) {
        User currentUser = userService.getById(currentUserId);
        Reply reply = replyService.getById(replyId);
        ReplyReact replyReact = replyReactionService.getById(reactionId);
        replyReactionService.delete(currentUser, reply, replyReact);
    }
}
