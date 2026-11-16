package com.parking.controller.error;

import com.parking.application.LotAlreadyExistsException;
import com.parking.application.LotFullException;
import com.parking.application.LotNotFoundException;
import com.parking.application.PaymentFailedException;
import com.parking.application.TicketNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * One place that turns exceptions into HTTP. Spring's own failures (malformed JSON, bad enum, missing body,
 * wrong method...) are handled by the base class as problem+json; we add the business ones.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({LotNotFoundException.class, TicketNotFoundException.class})
    ProblemDetail notFound(RuntimeException e) {
        String code = e instanceof LotNotFoundException ? "lot-not-found" : "ticket-not-found";
        return Problems.of(HttpStatus.NOT_FOUND, code, "Not found", e.getMessage());
    }

    @ExceptionHandler(LotAlreadyExistsException.class)
    ProblemDetail lotExists(LotAlreadyExistsException e) {
        return Problems.of(HttpStatus.CONFLICT, "lot-already-exists", "Lot already exists", e.getMessage());
    }

    @ExceptionHandler(LotFullException.class)
    ProblemDetail lotFull(LotFullException e) {
        ProblemDetail pd = Problems.of(HttpStatus.CONFLICT, "lot-full", "Lot is full", e.getMessage());
        pd.setProperty("vehicleType", e.vehicleType());
        return pd;
    }

    @ExceptionHandler(PaymentFailedException.class)
    ProblemDetail paymentFailed(PaymentFailedException e) {
        return Problems.of(HttpStatus.PAYMENT_REQUIRED, "payment-failed", "Payment failed", e.getMessage());
    }

    /** Domain value objects reject malformed input (blank plate, bad lot id...) with IllegalArgumentException. */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return Problems.of(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Unhandled error", e);
        return Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal error", "Unexpected error"); // no internals leaked
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = Problems.of(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", "One or more fields are invalid");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return handleExceptionInternal(ex, pd, headers, HttpStatus.BAD_REQUEST, request);
    }
}
