package com.portfolio.socialfeed;

import com.portfolio.socialfeed.follow.FollowService;
import com.portfolio.socialfeed.post.Post;
import com.portfolio.socialfeed.post.PostService;
import com.portfolio.socialfeed.timeline.FeedItem;
import com.portfolio.socialfeed.timeline.TimelineService;
import com.portfolio.socialfeed.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the hybrid fan-out: a normal author's post is pushed to followers' timelines, while a
 * celebrity's post is not fanned out and is pulled in at read time — both merged newest-first.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "feed.celebrity-follower-threshold=1")
class FanOutTest {

    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;
    @Autowired
    private PostService postService;
    @Autowired
    private TimelineService timelineService;
    @Autowired
    private StringRedisTemplate redis;

    @Test
    void pushes_normal_authors_and_pulls_celebrities() throws InterruptedException {
        UUID alice = userService.create("alice").getId();
        UUID bob = userService.create("bob").getId();
        UUID celeb = userService.create("celeb").getId();
        UUID other = userService.create("other").getId();

        // celeb gets 2 followers (> threshold 1) -> celebrity; bob gets 1 -> normal author.
        followService.follow(alice, celeb);
        followService.follow(other, celeb);
        followService.follow(alice, bob);

        Post bobPost = postService.create(bob, "from bob");
        Thread.sleep(5); // guarantee a strictly later timestamp for the celebrity post
        Post celebPost = postService.create(celeb, "from celeb");

        // celeb is flagged; bob is pushed to alice's timeline, celeb is NOT (pulled at read).
        assertTrue(redis.opsForSet().isMember("celebrities", celeb.toString()),
                "celebrity author should be flagged");
        assertEquals(1L, redis.opsForZSet().size("timeline:" + alice),
                "only the normal author's post is pushed to alice's timeline");

        // alice's home timeline merges both, newest first.
        List<FeedItem> timeline = timelineService.homeTimeline(alice, 20);
        assertEquals(2, timeline.size(), "timeline merges the pushed and the pulled post");
        assertEquals(celebPost.getId(), timeline.get(0).postId(), "celebrity post (pulled) is newest");
        assertEquals(bobPost.getId(), timeline.get(1).postId(), "normal author post (pushed) follows");
    }
}
