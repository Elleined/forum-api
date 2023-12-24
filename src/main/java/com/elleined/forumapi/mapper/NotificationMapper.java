package com.elleined.forumapi.mapper;

import com.elleined.forumapi.dto.notification.CommentNotification;
import com.elleined.forumapi.dto.notification.PostNotification;
import com.elleined.forumapi.dto.notification.ReplyNotification;
import com.elleined.forumapi.model.ModalTracker;
import com.elleined.forumapi.model.mention.CommentMention;
import com.elleined.forumapi.model.mention.PostMention;
import com.elleined.forumapi.model.mention.ReplyMention;
import com.elleined.forumapi.service.CommentService;
import com.elleined.forumapi.service.Formatter;
import com.elleined.forumapi.service.ReplyService;
import com.elleined.forumapi.service.notification.comment.CommentNotificationService;
import com.elleined.forumapi.service.notification.reply.ReplyNotificationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring", imports = {Formatter.class, ModalTracker.Type.class})
public abstract class NotificationMapper {

    @Autowired
    @Lazy
    protected CommentService commentService;

    protected CommentNotificationService commentNotificationService;
    protected ReplyNotificationService replyNotificationService;
    @Autowired @Lazy
    protected ReplyService replyService;

    @Mappings({
            @Mapping(target = "id", source = "postMention.id"),
            @Mapping(target = "receiverId", expression = "java(postMention.getReceiverId())"),
            @Mapping(target = "message", expression = "java(postMention.getMessage())"),
            @Mapping(target = "respondentId", source = "postMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "postMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(postMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(postMention.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "postMention.notificationStatus"),

            @Mapping(target = "postId", source = "postMention.post.id"),
    })
    public abstract PostNotification toMentionNotification(PostMention postMention);

    @Mappings({
            @Mapping(target = "id", source = "commentMention.id"),
            @Mapping(target = "receiverId", expression = "java(commentMention.getReceiverId())"),
            @Mapping(target = "message", expression = "java(commentMention.getMessage())"),
            @Mapping(target = "respondentId", source = "commentMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "commentMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(commentMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(commentMention.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "commentMention.notificationStatus"),

            @Mapping(target = "postId", source = "commentMention.comment.post.id"),
            @Mapping(target = "commentId", source = "commentMention.comment.id"),
            @Mapping(target = "count", expression = "java(1)")
    })
    public abstract CommentNotification toMentionNotification(CommentMention commentMention);

    @Mappings({
            @Mapping(target = "id", source = "replyMention.id"),
            @Mapping(target = "receiverId", expression = "java(replyMention.getReceiverId())"),
            @Mapping(target = "message", expression = "java(replyMention.getMessage())"),
            @Mapping(target = "respondentId", source = "replyMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "replyMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(replyMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(replyMention.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "notificationStatus"),

            @Mapping(target = "postId", source = "replyMention.reply.comment.post.id"),
            @Mapping(target = "commentId", source = "replyMention.reply.comment.id"),
            @Mapping(target = "replyId", source = "replyMention.reply.id"),
            @Mapping(target = "count", expression = "java(1)")
    })
    public abstract ReplyNotification toMentionNotification(ReplyMention replyMention);

}

