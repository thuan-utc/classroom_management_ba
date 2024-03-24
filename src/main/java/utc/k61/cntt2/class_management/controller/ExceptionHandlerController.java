package utc.k61.cntt2.class_management.controller;

import lombok.extern.log4j.Log4j2;
import org.apache.http.auth.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import utc.k61.cntt2.class_management.dto.ErrorJson;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Log4j2
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorJson> handleException(Exception ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("Exception occurred while processing request {}", requestUri, ex);

        // Check if the exception is an instance of AuthenticationException
        if (ex instanceof BadCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorJson(ex.getMessage(), "401", requestUri));
        }

        // For all other exceptions, return an INTERNAL_SERVER_ERROR response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorJson("An unexpected error occurred", "500", requestUri));
    }
}
