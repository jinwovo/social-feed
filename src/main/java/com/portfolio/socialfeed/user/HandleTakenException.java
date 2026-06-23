package com.portfolio.socialfeed.user;

public class HandleTakenException extends RuntimeException {

    public HandleTakenException(String handle) {
        super("handle already taken: " + handle);
    }
}
