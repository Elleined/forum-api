package com.elleined.forumapi.service;

import com.elleined.forumapi.exception.*;
import com.elleined.forumapi.mapper.ReplyMapper;
import com.elleined.forumapi.model.*;
import com.elleined.forumapi.repository.ReplyRepository;
import com.elleined.forumapi.service.block.BlockService;
import com.elleined.forumapi.service.mention.ReplyMentionService;
import com.elleined.forumapi.service.pin.CommentPinReplyService;
import com.elleined.forumapi.validator.StringValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ReplyMapper replyMapper;

    private final ModalTrackerService modalTrackerService;

    private final BlockService blockService;

    private final CommentPinReplyService commentPinReplyService;

    private final ReplyMentionService replyMentionService;

    public Reply save(User currentUser, Comment comment, String body, MultipartFile attachedPicture, Set<User> mentionedUsers) throws EmptyBodyException,
            ClosedCommentSectionException,
            ResourceNotFoundException,
            BlockedException, IOException {

        if (StringValidator.isNotValidBody(body)) throw new EmptyBodyException("Reply body cannot be empty!");
        if (comment.isCommentSectionClosed()) throw new ClosedCommentSectionException("Cannot reply to this comment because author already closed the comment section for this post!");
        if (comment.isInactive()) throw new ResourceNotFoundException("The comment you trying to reply is either be deleted or does not exists anymore!");
        if (blockService.isBlockedBy(currentUser, comment.getCommenter())) throw new BlockedException("Cannot reply because you blocked this user already!");
        if (blockService.isYouBeenBlockedBy(currentUser, comment.getCommenter())) throw new BlockedException("Cannot reply because this user block you already!");

        NotificationStatus status = modalTrackerService.isModalOpen(comment.getCommenter().getId(), comment.getId(), ModalTracker.Type.REPLY) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Reply reply = replyMapper.toEntity(body, currentUser, comment, attachedPicture.getOriginalFilename(), status);
        replyRepository.save(reply);

        if (mentionedUsers != null) replyMentionService.mentionAll(currentUser, mentionedUsers, reply);
        log.debug("Reply with id of {} saved successfully!", reply.getId());
        return reply;
    }

    public void delete(User currentUser, Comment comment, Reply reply) throws NotOwnedException {
        if (comment.doesNotHave(reply)) throw new NotOwnedException("Comment with id of " + comment.getId() +  " does not have reply with id of " + reply.getId());
        if (currentUser.notOwned(reply)) throw new NotOwnedException("User with id of " + currentUser.getId() + " doesn't have reply with id of " + reply.getId());

        reply.setStatus(Status.INACTIVE);
        replyRepository.save(reply);
        if (comment.getPinnedReply() != null && comment.getPinnedReply().equals(reply)) commentPinReplyService.unpin(reply);
        log.debug("Reply with id of {} are now inactive!", reply.getId());
    }

    public Reply updateBody(User currentUser, Reply reply, String newReplyBody)
            throws ResourceNotFoundException,
            NotOwnedException {

        if (reply.getBody().equals(newReplyBody)) return reply;
        if (currentUser.notOwned(reply)) throw new NotOwnedException("User with id of " + currentUser.getId() + " doesn't have reply with id of " + reply.getId());

        reply.setBody(newReplyBody);
        replyRepository.save(reply);
        log.debug("Reply with id of {} updated with the new body of {}", reply.getId(), newReplyBody);
        return reply;
    }

    public List<Reply> getAllByComment(User currentUser, Comment comment) throws ResourceNotFoundException {
        if (comment.isInactive()) throw new ResourceNotFoundException("Comment with id of " + comment.getId() + " might already been deleted or does not exists anymore!");

        Reply pinnedReply = comment.getPinnedReply();
        List<Reply> replies = new ArrayList<>(comment.getReplies().stream()
                .filter(Reply::isActive)
                .filter(reply -> !reply.equals(pinnedReply))
                .filter(reply -> !blockService.isBlockedBy(currentUser, reply.getReplier()))
                .filter(reply -> !blockService.isYouBeenBlockedBy(currentUser, reply.getReplier()))
                .sorted(Comparator.comparing(Reply::getDateCreated))
                .toList());
        if (pinnedReply != null) replies.add(0, pinnedReply); // Prioritizing pinned reply
        return replies;
    }

    public Reply getById(int replyId) throws ResourceNotFoundException {
        return replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
    }
}
