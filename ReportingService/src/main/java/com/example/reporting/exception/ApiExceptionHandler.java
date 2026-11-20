package com.example.reporting.exception;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(ApiException.class)
	ResponseEntity<Map<String, String>> api(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
	ResponseEntity<Map<String, String>> bad(Exception ex) {
		return ResponseEntity.badRequest().body(Map.of("error", "Invalid request body"));
	}

	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	ResponseEntity<Map<String, String>> conflict(Exception ex) {
		return ResponseEntity.status(409).body(Map.of("error", "Conflicting or invalid report data"));
	}
}
