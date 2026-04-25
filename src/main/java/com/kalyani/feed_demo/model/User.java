package com.kalyani.feed_demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String displayName;
    private String avatarUrl;
    private int followerCount;
    private int followingCount;
    private boolean isCelebrity; // will be true if followerCount > 10000

}