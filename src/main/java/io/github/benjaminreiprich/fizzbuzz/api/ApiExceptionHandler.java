package io.github.benjaminreiprich.fizzbuzz.api;

import io.github.benjaminreiprich.fizzbuzz.api.dto.InvalidParameter;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every error into an RFC 9457 Problem Details response (ADR-0008). The base class already covers Spring MVC's
 * own exceptions (404, 405, 415...); this class adds field-level validation errors and a generic 500.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = exception.getBody();
        problem.setDetail("Invalid request parameters.");
        problem.setProperty("errors", invalidParameters(exception));
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    // Sorted so that the response does not depend on the validator's iteration order.
    private static List<InvalidParameter> invalidParameters(MethodArgumentNotValidException exception) {
        return exception.getFieldErrors().stream()
                .map(error -> new InvalidParameter(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(InvalidParameter::field).thenComparing(InvalidParameter::message))
                .toList();
    }

    // The exception is logged with its stack trace, but nothing about it reaches the client.
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while handling a request", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }
}
