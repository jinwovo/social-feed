package com.portfolio.socialfeed.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Getter
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String handle;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AppUser() {
    }

    public AppUser(String handle) {
        this.id = UUID.randomUUID();
        this.handle = handle;
        this.createdAt = Instant.now();
    }
}
