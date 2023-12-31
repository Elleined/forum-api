package com.elleined.forumapi.mapper;


import com.elleined.forumapi.dto.PostDTO;
import com.elleined.forumapi.model.Post;
import com.elleined.forumapi.model.Status;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.service.Formatter;
import com.elleined.forumapi.service.post.PostService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring", imports = {
        Formatter.class,
        Status.class,
        Post.CommentSectionStatus.class
}, uses = UserMapper.class)
public abstract class PostMapper {

    @Autowired @Lazy
    protected PostService postService;

    @Mappings({
            // Should not be touched!
            @Mapping(target = "id", ignore = true),

            // Required
            @Mapping(target = "author", expression = "java(currentUser)"),
            @Mapping(target = "body", expression = "java(body)"),

            // Required auto fill
            @Mapping(target = "dateCreated", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "status", expression = "java(Status.ACTIVE)"),
            @Mapping(target = "commentSectionStatus", expression = "java(CommentSectionStatus.OPEN)"),

            // Required list
            @Mapping(target = "mentions", expression = "java(new java.util.HashSet<>())"),
            @Mapping(target = "reactions", expression = "java(new java.util.ArrayList<>())"),
            @Mapping(target = "savingUsers", expression = "java(new java.util.HashSet<>())"),
            @Mapping(target = "sharers", expression = "java(new java.util.HashSet<>())"),
            @Mapping(target = "comments", expression = "java(new java.util.ArrayList<>())"),

            // Optional
            @Mapping(target = "attachedPicture", expression = "java(picture)"),
            @Mapping(target = "pinnedComment", expression = "java(null)"),
    })
    public abstract Post toEntity(String body,
                                  @Context User currentUser,
                                  @Context String picture);

    @Mappings({
            @Mapping(target = "formattedDateCreated", expression = "java(Formatter.formatDateWithoutYear(post.getDateCreated()))"),
            @Mapping(target = "formattedTimeCreated", expression = "java(Formatter.formatTime(post.getDateCreated()))"),
            @Mapping(target = "authorId", source = "post.author.id"),
            @Mapping(target = "authorName", source = "post.author.name"),
            @Mapping(target = "authorPicture", source = "post.author.picture"),
            @Mapping(target = "totalCommentAndReplies", expression = "java(postService.getTotalCommentsAndReplies(post))"),
            @Mapping(target = "status", source = "post.status"),
            @Mapping(target = "commentSectionStatus", source = "post.commentSectionStatus"),
            @Mapping(target = "mentionedUsers", source = "post.mentions"),
            @Mapping(target = "attachedPicture", source = "post.attachedPicture"),
            @Mapping(target = "pinnedCommentId", source = "post.pinnedComment.id"),
    })
    public abstract PostDTO toDTO(Post post);
}
