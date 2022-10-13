package com.rsw.moviesinfoservice.controller;

import com.rsw.moviesinfoservice.domain.MovieInfo;
import com.rsw.moviesinfoservice.service.MoviesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/movieInfos")
public class MoviesInfoController {

    private MoviesInfoService moviesInfoService;
    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedMovieInfo -> movieInfoSink.tryEmitNext(savedMovieInfo));
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovieInfos() {
        return movieInfoSink.asFlux();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfoFlux(@RequestParam(value = "year", required = false) Integer year) {
        log.info("year : {} ", year);
        if (year != null) {
            return moviesInfoService.getMovieInfoByYear(year).log();
        }
        return moviesInfoService.getAllMovieInfos();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return moviesInfoService.getMovieById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo movieInfo, @PathVariable String id) {
        return moviesInfoService.updateMovieInfo(movieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return moviesInfoService.deleteMovie(id);
    }

}
