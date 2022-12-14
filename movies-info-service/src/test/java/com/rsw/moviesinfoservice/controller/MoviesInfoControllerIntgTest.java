package com.rsw.moviesinfoservice.controller;

import com.rsw.moviesinfoservice.domain.MovieInfo;
import com.rsw.moviesinfoservice.repository.MovieInfoRepository;
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
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=5.0.6")
class MoviesInfoControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    static String MOVIES_INFO_URL = "/v1/movieInfos";

    @BeforeEach
    void setUp() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        //when
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });

        //then
    }

    @Test
    void getAllMovieInfoFlux() {

        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);

    }

    @Test
    void getMovieInfoById() {

        var movieInfoId = "abc";

        webTestClient.get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
                /*.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movie = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movie);
                });*/

    }

    @Test
    void updatedMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Dark Knight Rises",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoId = "abc";
        //when
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals("Dark Knight Rises", updatedMovieInfo.getName());
                });

        //then
    }

    @Test
    void deleteMovieInfoById() {

        var movieInfoId = "abc";

        webTestClient.delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }


    /**
     * ResponseEntity Reactive Types
     **/

    @Test
    void updatedMovieInfoNotFound() {
        //given
        var movieInfoId = "def";
        var movieInfo = new MovieInfo(null, "Dark Knight Rises",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();

        //then
    }

    @Test
    void getMovieInfoByIdNotFound() {

        var movieInfoId = "def";

        webTestClient.get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void findByYear() {
        //given

        //when
        var movies = movieInfoRepository.findByYear(2005).log();
        //then
        StepVerifier.create(movies)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void getAMovieInfoByYear() {

        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void findByName() {
        //given
        var name = "The Dark Knight";
        //when
        var movies = movieInfoRepository.findByName(name);
        //then
        StepVerifier.create(movies)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void getAllMovieInfos_Stream() {

        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(savedMovieInfo).getMovieInfoId() != null;

                });

        var moviesStreamFlux = webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(moviesStreamFlux)
                .assertNext(movieInfo1 -> {
                    assert movieInfo1.getMovieInfoId()!=null;
                })
                .thenCancel()
                .verify();

    }

}