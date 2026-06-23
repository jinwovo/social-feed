package com.portfolio.socialfeed.web;

import com.portfolio.socialfeed.timeline.FeedItem;
import com.portfolio.socialfeed.timeline.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/{id}/timeline")
    public List<FeedItem> timeline(@PathVariable UUID id, @RequestParam(defaultValue = "20") int limit) {
        return timelineService.homeTimeline(id, Math.min(Math.max(limit, 1), 100));
    }
}
