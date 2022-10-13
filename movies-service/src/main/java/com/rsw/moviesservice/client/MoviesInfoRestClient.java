package com.rsw.moviesservice.client;

import com.rsw.moviesservice.domain.MovieInfo;
import com.rsw.moviesservice.exception.MoviesInfoClientException;
import com.rsw.moviesservice.exception.MoviesInfoServerException;
import com.rsw.moviesservice.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {


        /*var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof  MoviesInfoServerException)
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure())
                ));*/
        var url = moviesInfoUrl.concat("/{id}");

        return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException("There is no MovieInfo available for the passed in Id : " + movieId, clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                }))
                .bodyToMono(MovieInfo.class)
                //.retry(3)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        var url = moviesInfoUrl.concat("/stream");
        /*var retrySpec = RetrySpec.fixedDelay(3, Duration.ofSeconds(1))
                .filter((ex) -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure())));*/

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new MoviesInfoServerException(response)));
                }))
                .bodyToFlux(MovieInfo.class)
                //.retry(3)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }
}
