package com.project.event_ticket_platform.controllers.advice;

import com.project.event_ticket_platform.exceptions.EmailAlreadyExistsException;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.EventNotPublishedException;
import com.project.event_ticket_platform.exceptions.EventValidationException;
import com.project.event_ticket_platform.exceptions.InsufficientTicketsException;
import com.project.event_ticket_platform.exceptions.InvalidCredentialsException;
import com.project.event_ticket_platform.exceptions.InvalidRoleException;
import com.project.event_ticket_platform.exceptions.OrganizerNotFoundException;
import com.project.event_ticket_platform.exceptions.TicketNotFoundException;
import com.project.event_ticket_platform.exceptions.TicketTypeInUseException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotActiveException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotBelongsToEventException;
import com.project.event_ticket_platform.exceptions.TicketTypeNotFoundException;
import com.project.event_ticket_platform.exceptions.UnauthorizedAccessException;
import com.project.event_ticket_platform.exceptions.UserNotFoundException;
import com.project.event_ticket_platform.exceptions.QrCodeGenerationException;
import com.project.event_ticket_platform.exceptions.QrCodeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setTitle("Email already registered");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(OrganizerNotFoundException.class)
	public ProblemDetail handleOrganizerNotFound(OrganizerNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("Organizer not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(EventValidationException.class)
	public ProblemDetail handleEventValidation(EventValidationException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Invalid event data");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(EventNotFoundException.class)
	public ProblemDetail handleEventNotFound(EventNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("Event not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
						(first, ignored) -> first));

		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Validation failed");
		problem.setDetail("One or more fields failed validation.");
		if (!validationErrors.isEmpty()) {
			problem.setProperty("errors", validationErrors);
		}
		return problem;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Invalid argument type");
		problem.setDetail("Parameter '" + exception.getName() + "' has invalid value '" + exception.getValue() + "'.");
		return problem;
	}

	@ExceptionHandler(EventNotPublishedException.class)
	public ProblemDetail handleEventNotPublished(EventNotPublishedException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Event not published");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ProblemDetail handleUserNotFound(UserNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("User not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(TicketTypeNotFoundException.class)
	public ProblemDetail handleTicketTypeNotFound(TicketTypeNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("Ticket type not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(InsufficientTicketsException.class)
	public ProblemDetail handleInsufficientTickets(InsufficientTicketsException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Insufficient tickets available");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(QrCodeNotFoundException.class)
	public ProblemDetail handleQrCodeNotFound(QrCodeNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("QR code not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(QrCodeGenerationException.class)
	public ProblemDetail handleQrCodeGenerationException(QrCodeGenerationException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		problem.setTitle("QR Code generation failed");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(TicketNotFoundException.class)
	public ProblemDetail handleTicketNotFound(TicketNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("Ticket not found");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ProblemDetail handleUnauthorizedAccess(UnauthorizedAccessException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problem.setTitle("Unauthorized access");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(TicketTypeNotBelongsToEventException.class)
	public ProblemDetail handleTicketTypeNotBelongsToEvent(TicketTypeNotBelongsToEventException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Ticket type does not belong to event");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(TicketTypeNotActiveException.class)
	public ProblemDetail handleTicketTypeNotActive(TicketTypeNotActiveException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Ticket type is not active");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
		problem.setTitle("Invalid credentials");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(TicketTypeInUseException.class)
	public ProblemDetail handleTicketTypeInUse(TicketTypeInUseException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setTitle("Ticket type in use");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(InvalidRoleException.class)
	public ProblemDetail handleInvalidRole(InvalidRoleException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setTitle("Invalid role");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problem.setTitle("Access denied");
		problem.setDetail(exception.getMessage() != null ? exception.getMessage() : "You do not have permission to access this resource.");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGeneric(Exception exception) {
		log.error("Unexpected error", exception);
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		problem.setTitle("Unexpected error");
		problem.setDetail("An unexpected error occurred.");
		return problem;
	}
}
