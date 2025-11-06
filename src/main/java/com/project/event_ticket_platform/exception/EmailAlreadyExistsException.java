package com.project.event_ticket_platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException(String email) {
		super("A user with email %s already exists".formatted(email));
	}
}
