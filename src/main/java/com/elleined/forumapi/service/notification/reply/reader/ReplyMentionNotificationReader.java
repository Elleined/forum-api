package com.elleined.forumapi.service.notification.reply.reader;

import com.elleined.forumapi.model.Comment;
import com.elleined.forumapi.model.NotificationStatus;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.model.mention.ReplyMention;
import com.elleined.forumapi.repository.MentionRepository;
import com.elleined.forumapi.service.notification.mention.MentionNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@Qualifier("replyMentionNotificationReader")
public class ReplyMentionNotificationReader implements ReplyNotificationReaderService {
    private final MentionNotificationService<ReplyMention> replyMentionNotificationService;
    private final MentionRepository mentionRepository;

    @Override
    public void readAll(User currentUser, Comment comment) {
        List<ReplyMention> receiveUnreadReplyMentions = replyMentionNotificationService.getAllUnreadNotification(currentUser);
        receiveUnreadReplyMentions.stream()
                .filter(replyMention -> replyMention.getReply().getComment().equals(comment))
                .forEach(replyMention -> replyMention.setNotificationStatus(NotificationStatus.READ));

        mentionRepository.saveAll(receiveUnreadReplyMentions);
        log.debug("Reading all reply mentions for current user with id of {} success", currentUser.getId());
    }
}
