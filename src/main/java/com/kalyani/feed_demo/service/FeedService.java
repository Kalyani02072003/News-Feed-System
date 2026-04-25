package com.kalyani.feed_demo.service;

import com.kalyani.feed_demo.dto.FeedConnection;
import com.kalyani.feed_demo.model.FeedEntry;
import com.kalyani.feed_demo.model.Follow;
import com.kalyani.feed_demo.model.Post;
import com.kalyani.feed_demo.model.User;
import com.kalyani.feed_demo.repository.FeedEntryRepository;
import com.kalyani.feed_demo.repository.FollowRepository;
import com.kalyani.feed_demo.repository.PostRepository;
import com.kalyani.feed_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    // Celebrity threshold users above this skip fan-out on write
    private static final int CELEBRITY_THRESHOLD = 10_000;

    private final FollowRepository followRepository;
    private final FeedEntryRepository feedEntryRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;   // for cursor queries

    // FAN-OUT  called after every post is created
    public void fanOut(Post post, User author) {

        if (author.getFollowerCount() >= CELEBRITY_THRESHOLD) {
            // celebrity path skip fan-out on write
            // their posts are fetched at read time and merged in
            log.info("Celebrity post by {}. Skipping fan-out. Will merge at read time.",
                    author.getUsername());
            return;
        }

        // normal user push post into every follower's feed collection
        List<Follow> followers = followRepository.findByFolloweeId(author.getId());

        log.info("Fanning out post {} to {} followers", post.getId(), followers.size());

        List<FeedEntry> entries = followers.stream()
                .map(follow -> FeedEntry.builder()
                        .userId(follow.getFollowerId())   // goes into this follower's feed
                        .postId(post.getId())
                        .authorId(post.getAuthorId())
                        .score(post.getScore())
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());

        // Bulk insert one DB call for all followers
        feedEntryRepository.saveAll(entries);

        log.info("Fan-out complete for post {}", post.getId());
    }

    // READ FEED  fan-in happens here
    public FeedConnection getFeed(String userId, int limit, String cursor) {

        // load pre-built feed entries (fan-out on write results)
        List<FeedEntry> feedEntries = fetchFeedEntries(userId, limit + 1, cursor);

        // get IDs of celebrities this user follow their posts were NOT fanned out, fetch them now (fan-out on read)
        List<Post> celebrityPosts = fetchCelebrityPosts(userId);

        // resolve full post objects from feed entries
        List<String> postIds = feedEntries.stream()
                .map(FeedEntry::getPostId)
                .collect(Collectors.toList());

        List<Post> feedPosts = postIds.isEmpty()
                ? new ArrayList<>()
                : postRepository.findAllById(postIds);

        // fan-In  merge both lists
        Set<String> seenPostIds = new HashSet<>();
        List<Post> merged = new ArrayList<>();

        for (Post p : feedPosts) {
            if (seenPostIds.add(p.getId())) merged.add(p);
        }
        for (Post p : celebrityPosts) {
            if (seenPostIds.add(p.getId())) merged.add(p);   // deduplication
        }

        // re rank the merged list by score descending
        merged.sort(Comparator.comparingDouble(Post::getScore).reversed());

        // Cursor pagination
        boolean hasNextPage = merged.size() > limit;
        List<Post> page = hasNextPage ? merged.subList(0, limit) : merged;

        String nextCursor = null;
        if (hasNextPage && !page.isEmpty()) {
            Post last = page.get(page.size() - 1);
            // Cursor encodes score + id so we can resume exactly here
            nextCursor = encodeCursor(last.getScore(), last.getId());
        }

        return FeedConnection.builder()
                .posts(page)
                .nextCursor(nextCursor)
                .hasNextPage(hasNextPage)
                .build();
    }

    // follow  also triggers partial fan-in backfill
    public boolean followUser(String followerId, String followeeId) {
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);

        // update follower count on the followee
        userRepository.findById(followeeId).ifPresent(user -> {
            user.setFollowerCount(user.getFollowerCount() + 1);

            // re evaluate celebrity status
            user.setCelebrity(user.getFollowerCount() >= CELEBRITY_THRESHOLD);
            userRepository.save(user);
        });

        log.info("User {} now follows {}", followerId, followeeId);
        return true;
    }


    // helper functuions

    private List<FeedEntry> fetchFeedEntries(String userId, int limit, String cursor) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));

        if (cursor != null) {
            // decode cursor to get score + id of last seen post
            double[] decoded = decodeCursor(cursor);
            double lastScore = decoded[0];
            String lastId = String.valueOf((long) decoded[1]);

            // fetch entries with score less than cursor score
            // this is the cursor pagination logic
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("score").lt(lastScore),
                    Criteria.where("score").is(lastScore)
                            .and("postId").lt(lastId)
            ));
        }

        query.with(Sort.by(Sort.Direction.DESC, "score"));
        query.limit(limit);

        return mongoTemplate.find(query, FeedEntry.class);
    }

    private List<Post> fetchCelebrityPosts(String userId) {
        // find all followees of this user
        List<String> followeeIds = followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());

        if (followeeIds.isEmpty()) return new ArrayList<>();

        //  find celebrities
        Query celebQuery = new Query();
        celebQuery.addCriteria(
                Criteria.where("_id").in(followeeIds)
                        .and("isCelebrity").is(true)
        );
        List<User> celebrities = mongoTemplate.find(celebQuery, User.class);

        if (celebrities.isEmpty()) return new ArrayList<>();

        // Fetch their recent posts
        List<String> celebIds = celebrities.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        Query postQuery = new Query();
        postQuery.addCriteria(Criteria.where("authorId").in(celebIds));
        postQuery.with(Sort.by(Sort.Direction.DESC, "score"));
        postQuery.limit(50);   // cap celebrity posts in merge

        return mongoTemplate.find(postQuery, Post.class);
    }

    // cursor = base64(score:id)
    private String encodeCursor(double score, String id) {
        String raw = score + ":" + id;
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    private double[] decodeCursor(String cursor) {
        String raw = new String(Base64.getDecoder().decode(cursor));
        String[] parts = raw.split(":");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1])   // postId stored as numeric for comparison
        };
    }
}