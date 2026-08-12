package com.example.Spring_Coffee_Shop_Project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CustomerException extends RuntimeException {

    private int status;
    private String message;

}
