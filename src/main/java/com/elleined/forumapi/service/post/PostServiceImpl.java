package com.elleined.forumapi.service.post;

import com.elleined.forumapi.exception.BlockedException;
import com.elleined.forumapi.exception.EmptyBodyException;
import com.elleined.forumapi.exception.NotOwnedException;
import com.elleined.forumapi.exception.ResourceNotFoundException;
import com.elleined.forumapi.model.Comment;
import com.elleined.forumapi.model.Post;
import com.elleined.forumapi.model.User;
import com.elleined.forumapi.model.like.PostLike;
import com.elleined.forumapi.model.mention.PostMention;
import com.elleined.forumapi.service.LikeService;
import com.elleined.forumapi.service.MentionService;
import com.elleined.forumapi.service.PinService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostServiceImpl
        extends PinService<Post, Comment>,
        MentionService<Post>,
        LikeService<Post>,
        SavedPostService {

    Post save(User currentUser, String body, MultipartFile attachedPicture, Set<User> mentionedUsers)
            throws EmptyBodyException,
            BlockedException,
            ResourceNotFoundException,
            IOException;

    void delete(User currentUser, Post post) throws NotOwnedException;

    Post updateBody(User currentUser, Post post, String newBody)
            throws ResourceNotFoundException,
            NotOwnedException;

    Post updateCommentSectionStatus(User currentUser, Post post);

    Post getById(int postId) throws ResourceNotFoundException;

    List<Post> getAll(User currentUser);

    Optional<Comment> getPinnedComment(Post post) throws ResourceNotFoundException;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    int getTotalCommentsAndReplies(Post post);

    @Override
    void pin(User currentUser, Post post, Comment comment) throws NotOwnedException, ResourceNotFoundException;

    @Override
    void unpin(Comment comment);

    @Override
    PostMention mention(User mentioningUser, User mentionedUser, Post post);

    @Override
    void mentionAll(User mentioningUser, Set<User> mentionedUsers, Post post);

    @Override
    PostLike like(User respondent, Post post)
            throws ResourceNotFoundException,
            BlockedException;

    @Override
    void unLike(User respondent, Post post);

    @Override
    boolean isLiked(User respondent, Post post);
}