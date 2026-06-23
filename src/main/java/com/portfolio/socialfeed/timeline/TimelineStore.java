package com.portfolio.socialfeed.timeline;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Redis-backed timelines. Each timeline is a sorted set scored by post timestamp:
 * {@code timeline:{userId}} holds a user's pre-computed home feed (fan-out-on-write), and
 * {@code posts:{authorId}} holds an author's own posts (used by the fan-out-on-read path and
 * profiles). {@code celebrities} is the set of authors whose posts are pulled at read time.
 */
@Component
@RequiredArgsConstructor
public class TimelineStore {

    private static final int TIMELINE_CAP = 800;
    private static final String CELEBRITIES = "celebrities";

    private final StringRedisTemplate redis;

    private static String homeKey(UUID userId) {
        return "timeline:" + userId;
    }

    private static String authorKey(UUID authorId) {
        return "posts:" + authorId;
    }

    /** Fan-out on write: push a post onto a follower's home timeline, capped to the newest entries. */
    public void pushToHome(UUID followerId, UUID postId, long score) {
        String key = homeKey(followerId);
        redis.opsForZSet().add(key, postId.toString(), score);
        redis.opsForZSet().removeRange(key, 0, -(TIMELINE_CAP + 1));
    }

    /** The author's own feed — always maintained; serves the pull path and profile views. */
    public void addToAuthorFeed(UUID authorId, UUID postId, long score) {
        redis.opsForZSet().add(authorKey(authorId), postId.toString(), score);
    }

    public void markCelebrity(UUID authorId) {
        redis.opsForSet().add(CELEBRITIES, authorId.toString());
    }

    public boolean isCelebrity(UUID authorId) {
        return Boolean.TRUE.equals(redis.opsForSet().isMember(CELEBRITIES, authorId.toString()));
    }

    public List<ScoredId> homeTimeline(UUID userId, int limit) {
        return reverseRange(homeKey(userId), limit);
    }

    public List<ScoredId> authorFeed(UUID authorId, int limit) {
        return reverseRange(authorKey(authorId), limit);
    }

    private List<ScoredId> reverseRange(String key, int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
        if (tuples == null) {
            return List.of();
        }
        return tuples.stream()
                .map(t -> new ScoredId(UUID.fromString(t.getValue()), t.getScore()))
                .toList();
    }

    public record ScoredId(UUID postId, double score) {
    }
}
