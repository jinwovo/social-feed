package com.portfolio.socialfeed.timeline;

import com.portfolio.socialfeed.follow.FollowRepository;
import com.portfolio.socialfeed.post.Post;
import com.portfolio.socialfeed.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds a user's home timeline with a <strong>hybrid fan-out</strong>: the pre-computed timeline
 * (fan-out-on-write, cheap reads for normal authors) merged with posts pulled at read time from any
 * celebrity authors the user follows (fan-out-on-read, avoids fanning a celebrity post to millions).
 */
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final FollowRepository follows;
    private final PostRepository posts;
    private final TimelineStore timelines;

    @Transactional(readOnly = true)
    public List<FeedItem> homeTimeline(UUID userId, int limit) {
        Map<UUID, Double> candidates = new HashMap<>();

        // Push portion: pre-fanned-out posts from normal authors.
        for (TimelineStore.ScoredId s : timelines.homeTimeline(userId, limit)) {
            candidates.merge(s.postId(), s.score(), Math::max);
        }

        // Pull portion: celebrity authors are not fanned out on write, so merge their recent posts now.
        for (UUID followeeId : follows.findFolloweeIds(userId)) {
            if (timelines.isCelebrity(followeeId)) {
                for (TimelineStore.ScoredId s : timelines.authorFeed(followeeId, limit)) {
                    candidates.merge(s.postId(), s.score(), Math::max);
                }
            }
        }

        List<UUID> topIds = candidates.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        Map<UUID, Post> byId = posts.findAllById(topIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        return topIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(p -> new FeedItem(p.getId(), p.getAuthorId(), p.getContent(), p.getCreatedAt()))
                .toList();
    }
}
