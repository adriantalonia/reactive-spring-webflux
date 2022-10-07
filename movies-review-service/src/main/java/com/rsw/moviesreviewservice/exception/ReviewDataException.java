package com.rsw.moviesreviewservice.exception;

public class ReviewDataException extends RuntimeException {
    private String message;

    public ReviewDataException(String s) {
        super(s);
        this.message = s;
    }
}
