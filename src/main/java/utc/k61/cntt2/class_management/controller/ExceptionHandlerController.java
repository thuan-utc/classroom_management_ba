package utc.k61.cntt2.class_management.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import utc.k61.cntt2.class_management.dto.ErrorJson;
import utc.k61.cntt2.class_management.exception.BusinessException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Log4j2
public class ExceptionHandlerController {
    @ExceptionHandler
    public ResponseEntity<ErrorJson> handleException(Exception ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("Exception occurred while processing request {}", requestUri, ex);

        // Check if the exception is an instance of AuthenticationException
        if (ex instanceof BadCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorJson(ex.getMessage(), "401", requestUri));
        } else if (ex instanceof BusinessException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorJson(ex.getMessage(), "400", requestUri));
        } else if (ex instanceof MethodArgumentNotValidException) {
            // Get all validation errors
            BindingResult result = ((MethodArgumentNotValidException) (ex)).getBindingResult();
            List<FieldError> fieldErrors = result.getFieldErrors();

            // Prepare a list of error messages
            List<String> errorMessages = new ArrayList<>();
            for (FieldError fieldError : fieldErrors) {
                errorMessages.add(fieldError.getDefaultMessage());
            }

            // Return BAD_REQUEST status with error messages
            return ResponseEntity.badRequest()
                    .body(new ErrorJson(errorMessages.toString(), "400", requestUri));
        }

        // For all other exceptions, return an INTERNAL_SERVER_ERROR response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorJson("An unexpected error occurred", "500", requestUri));
    }
}
