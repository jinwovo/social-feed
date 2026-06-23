package com.portfolio.socialfeed.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository users;

    @Transactional
    public AppUser create(String handle) {
        if (users.existsByHandle(handle)) {
            throw new HandleTakenException(handle);
        }
        return users.save(new AppUser(handle));
    }
}
