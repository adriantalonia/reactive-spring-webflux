package com.rsw.moviesinfoservice.controller;

import com.rsw.moviesinfoservice.domain.MovieInfo;
import com.rsw.moviesinfoservice.service.MoviesInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/movieInfos")
public class MoviesInfoController {

    private MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<MovieInfo> getAllMovieInfoFlux() {
        return moviesInfoService.getAllMovieInfos();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> getMovieInfoById(@PathVariable String id) {
        return moviesInfoService.getMovieById(id);
    }
}
