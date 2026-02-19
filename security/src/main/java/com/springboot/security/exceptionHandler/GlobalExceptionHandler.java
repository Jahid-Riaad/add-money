package com.springboot.security.exceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

//    @ExceptionHandler(UsernameNotFoundException.class)
//    public ResponseEntity<?> handleUserNotFoundException(UsernameNotFoundException exception) {
//        StringBuilder errorMessage = new StringBuilder();
//        errorMessage.append(exception.getMessage());
//        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
//    }

}
