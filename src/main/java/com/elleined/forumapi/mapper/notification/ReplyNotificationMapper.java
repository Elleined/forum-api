package com.elleined.forumapi.mapper.notification;

import com.elleined.forumapi.dto.notification.ReplyNotification;
import com.elleined.forumapi.model.Reply;
import com.elleined.forumapi.service.Formatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", imports = Formatter.class)
public interface ReplyNotificationMapper extends NotificationMapper<Reply, ReplyNotification> {

    @Override
    @Mappings(value = {
            @Mapping(target = "id", source = "reply.id"),
            @Mapping(target = "receiverId", source = "reply.replier.id"),
            @Mapping(target = "message", expression = "java(getMessage(reply))"),
            @Mapping(target = "respondentPicture", source = "reply.replier.picture"),
            @Mapping(target = "respondentId", source = "reply.replier.id"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(reply.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(reply.getDateCreated()))"),
            @Mapping(target = "notificationStatus", source = "reply.notificationStatus"),

            @Mapping(target = "postId", source = "reply.comment.post.id"),
            @Mapping(target = "commentId", source = "reply.comment.id"),
            @Mapping(target = "replyId", source = "reply.id"),

            @Mapping(target = "count", expression = "java(getNotificationCount(reply))"),
    })
    ReplyNotification toNotification(Reply reply);

    default String getMessage(Reply reply) {
        return reply.getReplier().getName() + " replied to your comment: " + "\"" + reply.getComment().getBody() + "\"";
    }

    default int getNotificationCount(Reply reply) {
        return replyNotificationService.notificationCountForReplier(reply.getComment().getCommenter(), reply.getComment(), reply.getReplier());
    }
}
