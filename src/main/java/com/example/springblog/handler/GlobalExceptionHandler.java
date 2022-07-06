

//전체 잘못된 요청 처리하는 코드

package com.example.springblog.handler;

import com.example.springblog.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RestController;


@ControllerAdvice
@RestController
public class GlobalExceptionHandler {


 @ExceptionHandler(value=IllegalArgumentException.class)
 public String handlerArgumentException(IllegalArgumentException e) {
  return "<h1>" + e.getMessage() + "</h1>";
 }
}