package com.portfolio.socialfeed.post;

import com.portfolio.socialfeed.follow.FollowRepository;
import com.portfolio.socialfeed.timeline.TimelineStore;
import com.portfolio.socialfeed.user.AppUserRepository;
import com.portfolio.socialfeed.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Creates a post and fans it out. Normal authors are fanned out on write (push the post id onto each
 * follower's home timeline). Authors above the celebrity threshold are flagged instead and their
 * posts are pulled at read time — the standard answer to the "celebrity with millions of followers"
 * fan-out problem. See ADR-0001.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository posts;
    private final FollowRepository follows;
    private final AppUserRepository users;
    private final TimelineStore timelines;

    @Value("${feed.celebrity-follower-threshold:3}")
    private long celebrityThreshold;

    @Transactional
    public Post create(UUID authorId, String content) {
        if (!users.existsById(authorId)) {
            throw new UserNotFoundException(authorId);
        }

        Post post = new Post(UUID.randomUUID(), authorId, content, Instant.now());
        posts.save(post);

        long score = post.getCreatedAt().toEpochMilli();
        timelines.addToAuthorFeed(authorId, post.getId(), score);

        long followers = follows.countByFolloweeId(authorId);
        if (followers > celebrityThreshold) {
            timelines.markCelebrity(authorId);
            log.debug("author {} is a celebrity ({} followers) — pull at read, no fan-out", authorId, followers);
        } else {
            for (UUID followerId : follows.findFollowerIds(authorId)) {
                timelines.pushToHome(followerId, post.getId(), score);
            }
        }
        return post;
    }
}
