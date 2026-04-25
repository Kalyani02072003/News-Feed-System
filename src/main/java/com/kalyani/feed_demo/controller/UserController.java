package com.kalyani.feed_demo.controller;

import com.kalyani.feed_demo.model.Post;
import com.kalyani.feed_demo.model.User;
import com.kalyani.feed_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @QueryMapping
    public User user(@Argument String id) {
        return userRepository.findById(id).orElseThrow();
    }

    // ─────────────────────────────────────────────────────────────
    // @BatchMapping solve N+1 in Spring GraphQL
    //
    // instead for each post -> fetch author (N queries)
    // it collect all posts -> fetch all authors in ONE query
    //
    // Spring calls this automatically when author field is resolved
    // across a list of posts never call it manually.
    // ─────────────────────────────────────────────────────────────
    @BatchMapping
    public Map<Post, User> author(List<Post> posts) {

        // Collect all unique author IDs from the batch of posts
        List<String> authorIds = posts.stream()
                .map(Post::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        // ONE DB call for all authors
        Map<String, User> authorMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Map each post back to its author
        return posts.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        post -> authorMap.get(post.getAuthorId())
                ));
    }
}