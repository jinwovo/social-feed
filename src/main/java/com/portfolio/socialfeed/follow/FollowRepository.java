package com.portfolio.socialfeed.follow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countByFolloweeId(UUID followeeId);

    @Query("select f.followerId from Follow f where f.followeeId = :id")
    List<UUID> findFollowerIds(@Param("id") UUID followeeId);

    @Query("select f.followeeId from Follow f where f.followerId = :id")
    List<UUID> findFolloweeIds(@Param("id") UUID followerId);
}
