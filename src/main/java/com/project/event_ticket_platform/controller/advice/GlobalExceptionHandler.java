package com.project.event_ticket_platform.controller.advice;

import com.project.event_ticket_platform.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setTitle("Email already registered");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (first, ignored) -> first));

		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Validation failed");
		problem.setDetail("One or more fields failed validation.");
		if (!validationErrors.isEmpty()) {
			problem.setProperty("errors", validationErrors);
		}
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGeneric(Exception exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		problem.setTitle("Unexpected error");
		problem.setDetail(exception.getMessage());
		return problem;
	}
}
