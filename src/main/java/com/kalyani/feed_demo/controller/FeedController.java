package com.kalyani.feed_demo.controller;

import com.kalyani.feed_demo.dto.FeedConnection;
import com.kalyani.feed_demo.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    //  to: feed(userId, limit, cursor) in schema.graphqls
    @QueryMapping
    public FeedConnection feed(
            @Argument String userId,
            @Argument int limit,
            @Argument String cursor) {

        return feedService.getFeed(userId, limit, cursor);
    }

    //  to: followUser(followerId, followeeId) in schema.graphqls
    @MutationMapping
    public boolean followUser(
            @Argument String followerId,
            @Argument String followeeId) {

        return feedService.followUser(followerId, followeeId);
    }
}