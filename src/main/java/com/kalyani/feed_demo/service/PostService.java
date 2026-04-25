package com.kalyani.feed_demo.service;

import com.kalyani.feed_demo.model.Post;
import com.kalyani.feed_demo.model.User;
import com.kalyani.feed_demo.repository.PostRepository;
import com.kalyani.feed_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FeedService feedService;

    public Post createPost(String authorId, String content, List<String> mediaUrls){

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found: "+ authorId));

        Post post = Post.builder()
                .authorId(authorId)
                .authorUserame(author.getUsername())
                .authorAvatar(author.getAvatarUrl())
                .content(content)
                .mediaUrls(mediaUrls != null ? mediaUrls : new ArrayList<>())
                .likeCount(0)
                .commentCount(0)
                .shareCount(0)
                .score(0.0)
                .createdAt(Instant.now())
                .build();

        Post saved = postRepository.save(post);

        saved.setScore(computeScore(saved));
        postRepository.save(saved);

        log.info("Post created: {} by user: {}", saved.getId(), authorId);

        feedService.fanOut(saved,author);
        return saved;
    }

    public Post likePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        post.setLikeCount(post.getLikeCount() + 1);

        // Recompute score after like
        post.setScore(computeScore(post));
        return postRepository.save(post);
    }

    // maybe a better function for this based on trends and data
    public double computeScore(Post post) {
        long ageInHours = (Instant.now().getEpochSecond()
                - post.getCreatedAt().getEpochSecond()) / 3600;

        return (post.getLikeCount() * 1.0)
                + (post.getCommentCount() * 1.5)
                + (post.getShareCount() * 2.0)
                - (ageInHours * 0.5);
    }

    public Post findById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }
}