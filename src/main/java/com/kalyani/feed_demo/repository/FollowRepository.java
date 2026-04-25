package com.kalyani.feed_demo.repository;

import com.kalyani.feed_demo.model.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FollowRepository extends MongoRepository<Follow, String> {
    List<Follow> findByFollowerId(String followerId);  // who does this user follow
    List<Follow> findByFolloweeId(String followeeId);  // who follows this user
    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);
}