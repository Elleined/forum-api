package com.elleined.forumapi.controller;

import com.elleined.forumapi.dto.CommentDTO;
import com.elleined.forumapi.dto.ReplyDTO;
import com.elleined.forumapi.mapper.CommentMapper;
import com.elleined.forumapi.mapper.ReplyMapper;
import com.elleined.forumapi.model.Comment;
import com.elleined.forumapi.model.Post;
import com.elleined.forumapi.model.Reply;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.model.like.CommentLike;
import com.elleined.forumapi.service.CommentService;
import com.elleined.forumapi.service.ModalTrackerService;
import com.elleined.forumapi.service.ReplyService;
import com.elleined.forumapi.service.UserService;
import com.elleined.forumapi.service.notification.reader.comment.CommentLikeNotificationReader;
import com.elleined.forumapi.service.notification.reader.comment.CommentMentionNotificationReader;
import com.elleined.forumapi.service.notification.reader.comment.CommentNotificationReader;
import com.elleined.forumapi.service.post.PostService;
import com.elleined.forumapi.service.ws.WSNotificationService;
import com.elleined.forumapi.service.ws.WSService;
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
    private final UserService userService;

    private final PostService postService;

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    private final ReplyMapper replyMapper;
    private final ReplyService replyService;

    private final WSNotificationService wsNotificationService;
    private final WSService wsService;

    private final CommentLikeNotificationReader commentLikeNotificationReader;
    private final CommentMentionNotificationReader commentMentionNotificationReader;
    private final CommentNotificationReader commentNotificationReader;

    private final ModalTrackerService modalTrackerService;

    @GetMapping
    public List<CommentDTO> getAllByPost(@PathVariable("currentUserId") int currentUserId,
                                         @PathVariable("postId") int postId) {

        User currentUser = userService.getById(currentUserId);
        Post post = postService.getById(postId);

        commentNotificationReader.readAll(currentUser, post);
        commentLikeNotificationReader.readAll(currentUser, post);
        commentMentionNotificationReader.readAll(currentUser, post);

        modalTrackerService.saveTrackerOfUserById(currentUserId, postId, "COMMENT");
        return commentService.getAllByPost(currentUser, post).stream()
                .map(commentMapper::toDTO)
                .toList();
    }

    @GetMapping("/getPinnedReply/{commentId}")
    public Optional<ReplyDTO> getPinnedReply(@PathVariable("commentId") int commentId) {
        Comment comment = commentService.getById(commentId);
        Optional<Reply> reply = commentService.getPinnedReply(comment);

        return Optional.of( replyMapper.toDTO(reply.orElseThrow()) );
    }

    @PostMapping
    public CommentDTO save(@PathVariable("currentUserId") int currentUserId,
                                  @PathVariable("postId") int postId,
                                  @RequestParam("body") String body,
                                  @RequestPart(required = false, value = "attachedPicture") MultipartFile attachedPicture,
                                  @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds) throws IOException {

        User currentUser = userService.getById(currentUserId);
        Set<User> mentionedUsers = userService.getAllById(mentionedUserIds);
        Post post = postService.getById(postId);

        Comment comment = commentService.save(currentUser, post, body, attachedPicture, mentionedUsers);
        if (mentionedUsers != null) wsNotificationService.broadcastCommentMentions(comment.getMentions());
        wsNotificationService.broadcastCommentNotification(comment);
        wsService.broadcastComment(comment);

        return commentMapper.toDTO(comment);
    }

    @DeleteMapping("/{commentId}")
    public CommentDTO delete(@PathVariable("currentUserId") int currentUserId,
                             @PathVariable("postId") int postId,
                             @PathVariable("commentId") int commentId) {

        User currentUser = userService.getById(currentUserId);
        Post post = postService.getById(postId);
        Comment comment = commentService.getById(commentId);

        commentService.delete(currentUser, post, comment);
        wsService.broadcastComment(comment);
        return commentMapper.toDTO(comment);
    }

    @PatchMapping("/upvote/{commentId}")
    public CommentDTO updateUpvote(@PathVariable("currentUserId") int currentUserId,
                                          @PathVariable("commentId") int commentId) {
        User respondent = userService.getById(currentUserId);
        Comment comment = commentService.getById(commentId);

        Comment updatedComment = commentService.updateUpvote(respondent, comment);
        return commentMapper.toDTO(updatedComment);
    }

    @PatchMapping("/body/{commentId}")
    public CommentDTO updateBody(@PathVariable("currentUserId") int currentUserId,
                                        @PathVariable("postId") int postId,
                                        @PathVariable("commentId") int commentId,
                                        @RequestParam("newCommentBody") String newCommentBody) {

        User currentUser = userService.getById(currentUserId);
        Post post = postService.getById(postId);
        Comment comment = commentService.getById(commentId);

        Comment updatedComment = commentService.updateBody(currentUser, post, comment, newCommentBody);
        wsService.broadcastComment(updatedComment);

        return commentMapper.toDTO(updatedComment);
    }

    @PatchMapping("/like/{commentId}")
    public CommentDTO like(@PathVariable("currentUserId") int respondentId,
                           @PathVariable("commentId") int commentId) {

        User respondent = userService.getById(respondentId);
        Comment comment = commentService.getById(commentId);

        if (commentService.isLiked(respondent, comment)) {
            commentService.unLike(respondent, comment);
            return commentMapper.toDTO(comment);
        }

        CommentLike commentLike = commentService.like(respondent, comment);
        wsNotificationService.broadcastLike(commentLike);

        return commentMapper.toDTO(comment);
    }

    @PatchMapping("/{commentId}/pinReply/{replyId}")
    public CommentDTO pinReply(@PathVariable("currentUserId") int currentUserId,
                               @PathVariable("commentId") int commentId,
                               @PathVariable("replyId") int replyId) {

        User currentUSer = userService.getById(currentUserId);
        Comment comment = commentService.getById(commentId);
        Reply reply = replyService.getById(replyId);

        commentService.pin(currentUSer, comment, reply);
        return commentMapper.toDTO(comment);
    }
}
