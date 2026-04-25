package com.kalyani.feed_demo;

import com.kalyani.feed_demo.model.Follow;
import com.kalyani.feed_demo.model.User;
import com.kalyani.feed_demo.repository.FollowRepository;
import com.kalyani.feed_demo.repository.UserRepository;
import com.kalyani.feed_demo.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.util.List;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class FeedDemoApplication implements CommandLineRunner {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final PostService postService;

	public static void main(String[] args) {
		SpringApplication.run(FeedDemoApplication.class, args);
	}

	@Override
	public void run(String... args) {

		// seed if DB is empty
		if (userRepository.count() > 0) {
			log.info("Data already seeded. Skipping.");
			return;
		}

		log.info("Seeding demo data...");

		// manually add users
		User alice = userRepository.save(User.builder()
				.username("alice")
				.displayName("Alice Dev")
				.avatarUrl("https://i.pravatar.cc/150?u=alice")
				.followerCount(0).followingCount(0)
				.isCelebrity(false).build());

		User bob = userRepository.save(User.builder()
				.username("bob")
				.displayName("Bob Coder")
				.avatarUrl("https://i.pravatar.cc/150?u=bob")
				.followerCount(0).followingCount(0)
				.isCelebrity(false).build());

		User celebrity = userRepository.save(User.builder()
				.username("techguru")
				.displayName("Tech Guru")
				.avatarUrl("https://i.pravatar.cc/150?u=techguru")
				.followerCount(50000).followingCount(10)
				.isCelebrity(true).build());          // pre-set as celebrity

		// alice follows Bob and the celebrity
		followRepository.saveAll(List.of(
				Follow.builder().followerId(alice.getId())
						.followeeId(bob.getId()).createdAt(Instant.now()).build(),
				Follow.builder().followerId(alice.getId())
						.followeeId(celebrity.getId()).createdAt(Instant.now()).build()
		));

		// add bobs follower count
		bob.setFollowerCount(1);
		userRepository.save(bob);

		// bob makes 3 posts and these will fan-out to Alice's feed
		postService.createPost(bob.getId(),
				"Thriller to share that I achieved nothing", null);
		postService.createPost(bob.getId(),
				"Hi connections, happy to share that i got laid off", null);
		postService.createPost(bob.getId(),
				"Hi connections, plsss buy my AI course that does nothing great to your life and will guarantee absolutely no jobs in future", null);

		// celebrity creates a post NO fan-out, fetched at read time
		postService.createPost(celebrity.getId(),
				"10M followers and counting. This time, I'm opening my own DSA company",
				null);

		log.info("Seeded users: alice={}, bob={}, celebrity={}",
				alice.getId(), bob.getId(), celebrity.getId());
		log.info("Alice's ID for demo queries: {}", alice.getId());
	}
}
