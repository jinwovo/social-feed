package com.portfolio.socialfeed.follow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** A directed follow edge (follower → followee), with a surrogate id and a unique pair constraint. */
@Entity
@Table(name = "follow")
@Getter
public class Follow {

    @Id
    private UUID id;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Follow() {
    }

    public Follow(UUID followerId, UUID followeeId) {
        this.id = UUID.randomUUID();
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.createdAt = Instant.now();
    }
}
