package com.rio_rishabhNEU.UserApp.ExceptionHandlers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GenericExceptionHandler {

    @ExceptionHandler(EmailNotAvailableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleEmailNotAvailableException(EmailNotAvailableException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotProvidedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleEmailNotProvidedException(EmailNotProvidedException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}


class ErrorResponse {

    public String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    // Getter and setter
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
