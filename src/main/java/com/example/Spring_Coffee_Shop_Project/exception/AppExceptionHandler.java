package com.example.Spring_Coffee_Shop_Project.exception;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {CustomerException.class})
    public ResponseEntity<CommonResponse> handleCustomException(CustomerException ex, WebRequest webRequest) {
        ex.printStackTrace();
        return ResponseEntity.ok(new CommonResponse(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<CommonResponse> handleServerException(Exception ex, WebRequest webRequest) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError().body(new CommonResponse(500, "UNEXPECTED_ERROR"));
    }
}