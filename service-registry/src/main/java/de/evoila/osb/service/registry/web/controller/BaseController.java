package de.evoila.osb.service.registry.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.osb.service.registry.exceptions.*;
import de.evoila.osb.service.registry.web.bodies.ErrorResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

@RestController
public class BaseController {

    private static Logger log = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<?> handleInvalidFieldException(InvalidFieldException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletResponse response) {
        if (ex.getMessage().contains("declared Enum instance names"))
            return new ResponseEntity<ErrorResponse>(new ErrorResponse("A field does not match the desired enum."), HttpStatus.BAD_REQUEST);
        return handleException(ex, response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleResourceAccessException(ResourceAccessException ex, HttpServletResponse response) {
        String errMessage;
        HttpStatus status;
        if (ex.getCause() instanceof UnknownHostException) {
            errMessage = "Request could not find host";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getCause() instanceof ConnectTimeoutException) {
            errMessage = "Request timed out";
            status = HttpStatus.REQUEST_TIMEOUT;
        } else if (ex.getCause() instanceof ConnectException && ex.getMessage().contains("Connection refused")) {
            errMessage = "Request was refused";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getCause() instanceof SocketTimeoutException) {
            errMessage = ex.getCause().getMessage();
            status = HttpStatus.REQUEST_TIMEOUT;
        } else {
            errMessage = "Request failed due to an unexpected error.";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        log.error(errMessage, ex);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(errMessage), status);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> handleHttpClientErrorException(HttpClientErrorException ex, HttpServletResponse response) {
        log.error("Request failed due to a client error: " + ex.getResponseBodyAsString(), ex);
        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(ex.getResponseBodyAsString(), Map.class);
        } catch (IOException e) {
            log.error("Parsing response body into json failed.");
            return new ResponseEntity<String>("", ex.getStatusCode());
        }
        return new ResponseEntity<Map<String, Object>>(map, ex.getStatusCode());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariableException(MissingPathVariableException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<?> handleServletRequestBindingException(ServletRequestBindingException ex, HttpServletResponse response) throws ServletRequestBindingException {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletResponse response) {
        BindingResult result = ex.getBindingResult();
        String message = "Missing required fields:";
        for (FieldError error : result.getFieldErrors()) {
            message += " " + error.getField();
        }
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotSharedException.class)
    public ResponseEntity<ErrorResponse> handleNotSharedException(NotSharedException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SharedContextInvalidException.class)
    public ResponseEntity<?> handleSharedContextInvalidException(SharedContextInvalidException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InUseException.class)
    public ResponseEntity<?> handleInUseException(InUseException ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(ex.getMessage()), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletResponse response) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("An unexpected error occured: '" + ex.getMessage() + "'"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
