package com.kalyani.feed_demo.dto;

import com.kalyani.feed_demo.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedConnection {
    private List<Post> posts;
    private String nextCursor; // null means no more pages
    private boolean hasNextPage;
}