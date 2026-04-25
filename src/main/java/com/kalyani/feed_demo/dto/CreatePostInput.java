package com.kalyani.feed_demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostInput {
    private String authorId;
    private String Content;
    private List<String> mediaUrls;
}

