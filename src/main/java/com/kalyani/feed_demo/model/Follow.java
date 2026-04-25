package com.kalyani.feed_demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "follows")
public class Follow {

    @Id
    private String id;

    @Indexed                  //  index give me everyone this user follows
    private String followerId;

    @Indexed                  // index give me all followers of this user
    private String followeeId;

    private Instant createdAt;
}