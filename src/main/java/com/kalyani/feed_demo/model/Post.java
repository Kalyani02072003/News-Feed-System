package com.kalyani.feed_demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    private String authorId;

    private String authorUserame;
    private String authorAvatar;

    private String content;
    private List<String> mediaUrls;

    private int likeCount;
    private int commentCount;
    private int shareCount;

    private double score;
    private Instant createdAt;

}