package io.github.benjaminreiprich.fizzbuzz.api;

import io.github.benjaminreiprich.fizzbuzz.api.dto.InvalidParameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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

    // Sorting makes the response independent of the order in which the validator reports errors.
    private static final Comparator<InvalidParameter> BY_FIELD_THEN_MESSAGE =
            Comparator.comparing(InvalidParameter::field).thenComparing(InvalidParameter::message);

    // Spring calls this method when @Valid rejects a request. The base class already builds a 400 Problem Details
    // body (exception.getBody()); we complete it with the list of invalid parameters, then let the base class
    // (handleExceptionInternal) write the response as it does for every other error.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = exception.getBody();
        problem.setDetail("Invalid request parameters.");
        problem.setProperty("errors", invalidParameters(exception));
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    private static List<InvalidParameter> invalidParameters(MethodArgumentNotValidException exception) {
        List<InvalidParameter> invalidParameters = new ArrayList<>();
        for (FieldError error : exception.getFieldErrors()) {
            invalidParameters.add(new InvalidParameter(error.getField(), error.getDefaultMessage()));
        }
        invalidParameters.sort(BY_FIELD_THEN_MESSAGE);
        return invalidParameters;
    }

    // Any other exception: logged with its stack trace, but nothing about it reaches the client.
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while handling a request", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }
}
