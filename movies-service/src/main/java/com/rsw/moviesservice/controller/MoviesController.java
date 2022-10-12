package com.rsw.moviesservice.controller;

import com.rsw.moviesservice.client.MoviesInfoRestClient;
import com.rsw.moviesservice.client.ReviewsRestClient;
import com.rsw.moviesservice.domain.Movie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MoviesInfoRestClient moviesInfoRestClient;
    private ReviewsRestClient reviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId){

        return moviesInfoRestClient.retrieveMovieInfo(movieId)
                //moviesInfoRestClient.retrieveMovieInfo_exchange(movieId)
                .flatMap(movieInfo -> {
                    var reviewList = reviewsRestClient.retrieveReviews(movieId)
                            .collectList();
                    return reviewList.map(reviews -> new Movie(movieInfo, reviews));
                });

    }
}