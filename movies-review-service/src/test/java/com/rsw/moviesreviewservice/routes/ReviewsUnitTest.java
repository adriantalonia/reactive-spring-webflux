package com.rsw.moviesreviewservice.routes;

import com.rsw.moviesreviewservice.domain.Review;
import com.rsw.moviesreviewservice.exceptionhandler.GlobalErrorHandler;
import com.rsw.moviesreviewservice.handler.ReviewHandler;
import com.rsw.moviesreviewservice.repository.ReviewReactiveRepository;
import com.rsw.moviesreviewservice.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class}) // inject dependencies
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc",
                1L, "Awesome Movie", 9.0)));

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
                    assertEquals("abc", savedReview.getReviewId());

                });

    }

    @Test
    void getReviews() {
        //given
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

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
        var reviewId = "abc";
        var reviewUpdated = new Review(null, 1L, "Awesome test", 7.0);
        var existingReview = new Review("abc", 1L, "Awesome Movie", 9.0);


        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(reviewUpdated));

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .bodyValue(reviewUpdated)
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
        //given
        var reviewId = "abc";
        var existingReview = new Review("abc", 1L, "Awesome Movie", 9.0);

        //when
        when(reviewReactiveRepository.findById(isA(String.class))).thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.deleteById(isA(String.class))).thenReturn(Mono.empty().then());

        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {
        //given
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0));

        var uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1)
                .buildAndExpand()
                .toUri();

        //when
        when(reviewReactiveRepository.findByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);

    }

    // validations

    @Test
    void addReviewValidation() {
        //given
        var review = new Review(null, null, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc",
                1L, "Awesome Movie", 9.0)));

        //when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null");

    }

}
