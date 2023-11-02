package com.elleined.forumapi.service.mention;

import com.elleined.forumapi.exception.BlockedException;
import com.elleined.forumapi.exception.MentionException;
import com.elleined.forumapi.exception.ResourceNotFoundException;
import com.elleined.forumapi.model.*;
import com.elleined.forumapi.model.mention.CommentMention;
import com.elleined.forumapi.repository.MentionRepository;
import com.elleined.forumapi.service.BlockService;
import com.elleined.forumapi.service.CommentService;
import com.elleined.forumapi.service.ModalTrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentMentionService implements MentionService<CommentMention, Comment> {
    private final CommentService commentService;
    private final MentionRepository mentionRepository;

    private final BlockService blockService;

    private final ModalTrackerService modalTrackerService;
    @Override
    public CommentMention mention(User mentioningUser, User mentionedUser, Comment comment) {
        if (commentService.isDeleted(comment)) throw new ResourceNotFoundException("Cannot mention! The comment with id of " + comment.getId() + " you are trying to mention might already been deleted or does not exists!");
        if (blockService.isBlockedBy(mentioningUser, mentionedUser)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUser.getId());
        if (blockService.isYouBeenBlockedBy(mentioningUser, mentionedUser)) throw  new BlockedException("Cannot mention! Mentioned user with id of " + mentionedUser.getId() + " already blocked you");
        if (mentioningUser.equals(mentionedUser)) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");


        NotificationStatus notificationStatus = modalTrackerService.isModalOpen(mentionedUser.getId(), comment.getPost().getId(), ModalTracker.Type.COMMENT)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        CommentMention commentMention = CommentMention.commentMentionBuilder()
                .mentioningUser(mentioningUser)
                .mentionedUser(mentionedUser)
                .createdAt(LocalDateTime.now())
                .comment(comment)
                .notificationStatus(notificationStatus)
                .build();

        mentioningUser.getSentCommentMentions().add(commentMention);
        mentionedUser.getReceiveCommentMentions().add(commentMention);
        comment.getMentions().add(commentMention);
        mentionRepository.save(commentMention);
        log.debug("User with id of {} mentioned user with id of {} in comment with id of {}", mentioningUser.getId(), mentionedUser.getId(), comment.getId());
        return commentMention;
    }

    @Override
    public List<CommentMention> mentionAll(User mentioningUser, Set<User> mentionedUsers, Comment comment) {
        return mentionedUsers.stream()
                .map(mentionedUser -> mention(mentioningUser, mentionedUser, comment))
                .toList();
    }

    @Override
    public List<CommentMention> getAllUnreadNotification(User currentUser) {
        return currentUser.getReceiveCommentMentions()
                .stream()
                .filter(mention -> !blockService.isBlockedBy(currentUser, mention.getMentionedUser()))
                .filter(mention -> !blockService.isYouBeenBlockedBy(currentUser, mention.getMentionedUser()))
                .filter(mention -> mention.getComment().getStatus() == Status.ACTIVE)
                .filter(mention -> mention.getNotificationStatus() == NotificationStatus.UNREAD)
                .toList();
    }
}
