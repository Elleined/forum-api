package com.elleined.forumapi.controller;

import com.elleined.forumapi.dto.CommentDTO;
import com.elleined.forumapi.dto.ReplyDTO;
import com.elleined.forumapi.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/{currentUserId}/posts/{postId}/comments")
public class CommentController {

    private final ForumService forumService;

    @GetMapping
    public List<CommentDTO> getAllByPost(@PathVariable("currentUserId") int currentUserId,
                                         @PathVariable("postId") int postId) {
        return forumService.getAllByPost(currentUserId, postId);
    }

    @GetMapping("/getPinnedReply/{commentId}")
    public Optional<ReplyDTO> getPinnedReply(@PathVariable("commentId") int commentId) {
        return forumService.getPinnedReply(commentId);
    }

    @PostMapping
    public CommentDTO saveComment(@PathVariable("currentUserId") int currentUserId,
                                  @PathVariable("postId") int postId,
                                  @RequestParam("body") String body,
                                  @RequestPart(required = false, value = "attachedPicture") MultipartFile attachedPicture,
                                  @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds) throws IOException {

        return forumService.saveComment(currentUserId, postId, body, attachedPicture, mentionedUserIds);
    }

    @DeleteMapping("/{commentId}")
    public CommentDTO delete(@PathVariable("currentUserId") int currentUserId,
                             @PathVariable("postId") int postId,
                             @PathVariable("commentId") int commentId) {

        return forumService.deleteComment(currentUserId, postId, commentId);
    }

    @PatchMapping("/upvote/{commentId}")
    public CommentDTO updateCommentUpvote(@PathVariable("currentUserId") int currentUserId,
                                          @PathVariable("commentId") int commentId) {

        return forumService.updateUpvote(currentUserId, commentId);
    }

    @PatchMapping("/body/{commentId}")
    public CommentDTO updateCommentBody(@PathVariable("currentUserId") int currentUserId,
                                        @PathVariable("postId") int postId,
                                        @PathVariable("commentId") int commentId,
                                        @RequestParam("newCommentBody") String newCommentBody) {

        return forumService.updateCommentBody(currentUserId, postId, commentId, newCommentBody);
    }

    @PatchMapping("/like/{commentId}")
    public CommentDTO likeComment(@PathVariable("currentUserId") int respondentId,
                                  @PathVariable("postId") int postId,
                                  @PathVariable("commentId") int commentId) {

        return forumService.likeComment(respondentId, postId, commentId);
    }

    @PatchMapping("/{commentId}/pinReply/{replyId}")
    public CommentDTO pinReply(@PathVariable("currentUserId") int currentUserId,
                               @PathVariable("commentId") int commentId,
                               @PathVariable("replyId") int replyId) {

        return forumService.pinReply(currentUserId, commentId, replyId);
    }
}
