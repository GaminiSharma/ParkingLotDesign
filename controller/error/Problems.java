package com.parking.controller.error;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Builds RFC 9457 problem details with stable, machine-readable {@code type} URNs. */
final class Problems {
    private Problems() {
    }

    static ProblemDetail of(HttpStatus status, String code, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("urn:parking:problem:" + code));
        return pd;
    }
}
