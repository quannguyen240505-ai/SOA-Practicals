package com.example.ordermanagement.exception;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<Map<String,String>> api(ApiException ex){return ResponseEntity.status(ex.getStatus()).body(Map.of("error",ex.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException ex){return ResponseEntity.badRequest().body(Map.of("error","Invalid input: "+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()));}
}
