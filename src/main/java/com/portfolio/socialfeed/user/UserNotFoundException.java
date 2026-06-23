package com.portfolio.socialfeed.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("user not found: " + id);
    }
}
