package com.portfolio.socialfeed.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String handle, Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getHandle(), user.getCreatedAt());
    }
}
