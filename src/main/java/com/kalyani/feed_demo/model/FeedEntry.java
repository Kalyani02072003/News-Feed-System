package com.kalyani.feed_demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "feeds")
@CompoundIndexes({
        // index in the system  ranked feed per user
        @CompoundIndex(name = "user_score_idx", def = "{'userId': 1, 'score': -1}")
})
public class FeedEntry {

    @Id
    private String id;

    private String userId;     // whose feed this belongs to
    private String postId;     // which post
    private String authorId;
    private double score;      // copied from post for fast sorting
    private Instant createdAt;
}