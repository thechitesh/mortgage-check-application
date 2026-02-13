package com.myorg.mortgage.exception;

import com.myorg.mortgage.app.model.ErrorDto;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    private final WebRequest webRequest = Mockito.mock(ServletWebRequest.class);

    @Test
    public void testHandleAuthExceptions() {
        Exception exception = new JwtException("JWT error");
        ResponseEntity<Object> response = globalExceptionHandler.handleAuthExceptions(exception, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorDto error = (ErrorDto) response.getBody();
        assertNotNull(error);
        assertEquals("JWT error", error.getMessage());
        assertEquals("ERR-001", error.getCode());
    }

    @Test
    public void testHandleRuntimeExceptions() {
        RuntimeException exception = new RuntimeException("Runtime error");
        ResponseEntity<Object> response = globalExceptionHandler.handleRuntimeExceptions(exception, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorDto error = (ErrorDto) response.getBody();
        assertNotNull(error);
        assertEquals("Something went wrong", error.getMessage());
        assertEquals("ERR-003", error.getCode());
    }

    @Test
    public void testHandleAllExceptions() {
        Exception exception = new Exception("General error");
        when(webRequest.getAttribute("filter.exception", WebRequest.SCOPE_REQUEST)).thenReturn(null);
        ResponseEntity<String> response = globalExceptionHandler.handleAllExceptions(exception, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: General error", response.getBody());
    }

    @Test
    public void testHandleAllExceptionsWithFilterException() {
        Exception exception = new Exception("General error");
        Exception filterException = new Exception("Filter error");
        when(webRequest.getAttribute("filter.exception", WebRequest.SCOPE_REQUEST)).thenReturn(filterException);
        ResponseEntity<String> response = globalExceptionHandler.handleAllExceptions(exception, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Filter exception: Filter error", response.getBody());
    }

}
