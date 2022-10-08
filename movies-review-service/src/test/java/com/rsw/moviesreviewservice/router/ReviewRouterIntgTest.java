package com.rsw.moviesreviewservice.router;

import com.rsw.moviesreviewservice.domain.Review;
import com.rsw.moviesreviewservice.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=5.0.6")
class ReviewRouterIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();

    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        //when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var savedReview = reviewResponse.getResponseBody();
                    assert savedReview != null;
                    assertNotNull(savedReview.getReviewId());
                });

    }

    @Test
    void getReviews() {

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        //given
        var reviewId = "abc";
        var review = new Review(null, 1L, "Awesome test", 7.0);

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var reviewMovieInfo = reviewEntityExchangeResult.getResponseBody();
                    assert reviewMovieInfo != null;
                    assert reviewMovieInfo.getMovieInfoId() != null;
                    assertEquals("Awesome test", reviewMovieInfo.getComment());
                    assertEquals(7.0, reviewMovieInfo.getRating());
                });
    }

    @Test
    void deleteReview() {
        var reviewId = "abc";

        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {
        var uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }
}