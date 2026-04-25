package com.kalyani.feed_demo.repository;

import com.kalyani.feed_demo.model.FeedEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FeedEntryRepository extends MongoRepository<FeedEntry, String> {

    // ranked by score, cursor-based
    // MongoTemplate for the cursor quer
    List<FeedEntry> findByUserIdOrderByScoreDesc(String userId);

    void deleteByUserIdAndPostId(String userId, String postId);
}