package com.kalyani.feed_demo.controller;

import com.kalyani.feed_demo.model.Post;
import com.kalyani.feed_demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @QueryMapping
    public Post post(@Argument String id) {
        return postService.findById(id);
    }

    //  to: createPost(authorId, content) in schema.graphqls
    @MutationMapping
    public Post createPost(
            @Argument String authorId,
            @Argument String content,
            @Argument List<String> mediaUrls) {

        return postService.createPost(authorId, content, mediaUrls);
    }

    // to: likePost(postId, userId) in schema.graphqls
    @MutationMapping
    public Post likePost(
            @Argument String postId,
            @Argument String userId) {

        return postService.likePost(postId, userId);
    }
}