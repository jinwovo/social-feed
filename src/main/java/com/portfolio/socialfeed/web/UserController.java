package com.portfolio.socialfeed.web;

import com.portfolio.socialfeed.follow.FollowService;
import com.portfolio.socialfeed.user.UserResponse;
import com.portfolio.socialfeed.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(request.handle()));
    }

    @PostMapping("/{id}/follow/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable UUID id, @PathVariable UUID targetId) {
        followService.follow(id, targetId);
    }

    public record CreateUserRequest(@NotBlank String handle) {
    }
}
