package com.portfolio.socialfeed.post;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(UUID id, UUID authorId, String content, Instant createdAt) {

    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getAuthorId(), post.getContent(), post.getCreatedAt());
    }
}
