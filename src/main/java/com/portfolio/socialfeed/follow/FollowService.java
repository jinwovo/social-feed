package com.portfolio.socialfeed.follow;

import com.portfolio.socialfeed.user.AppUserRepository;
import com.portfolio.socialfeed.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository follows;
    private final AppUserRepository users;

    @Transactional
    public void follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("a user cannot follow themselves");
        }
        if (!users.existsById(followerId)) {
            throw new UserNotFoundException(followerId);
        }
        if (!users.existsById(followeeId)) {
            throw new UserNotFoundException(followeeId);
        }
        if (!follows.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            follows.save(new Follow(followerId, followeeId));
        }
    }
}
