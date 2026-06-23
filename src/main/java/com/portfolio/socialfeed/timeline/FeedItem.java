package com.portfolio.socialfeed.timeline;

import java.time.Instant;
import java.util.UUID;

public record FeedItem(UUID postId, UUID authorId, String content, Instant createdAt) {
}
