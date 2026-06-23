package com.portfolio.socialfeed.web;

import com.portfolio.socialfeed.user.HandleTakenException;
import com.portfolio.socialfeed.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ApiError(String error, String message) {
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(UserNotFoundException e) {
        return new ApiError("USER_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(HandleTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(HandleTakenException e) {
        return new ApiError("HANDLE_TAKEN", e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(Exception e) {
        return new ApiError("BAD_REQUEST", e.getMessage());
    }
}
