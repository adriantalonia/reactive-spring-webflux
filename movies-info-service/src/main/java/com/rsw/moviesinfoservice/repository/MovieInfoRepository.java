package com.rsw.moviesinfoservice.repository;

import com.rsw.moviesinfoservice.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {
}
