package com.elvaco.mvp.web.api;

import java.io.IOException;

import com.elvaco.mvp.web.dto.ErrorMessageDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiExceptionHandlerTest {

  private ApiExceptionHandler apiExceptionHandler;

  @Before
  public void setUp() {
    apiExceptionHandler = new ApiExceptionHandler();
  }

  @Test
  public void mappedException() {
    Exception bandwidthException = new BandwidthExceededException("Oh no");

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(bandwidthException);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
    assertThat(response.getBody().message).isEqualTo("Oh no");
  }

  @Test
  public void internalError() {
    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new Exception("test"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().message).isEqualTo(ApiExceptionHandler.INTERNAL_ERROR_MESSAGE);
  }

  @Test
  public void accessDeniedException() {
    String message = "Access is denied";

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new AccessDeniedException(message));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message).isEqualTo(message);
  }

  @Test
  public void clientAbortException() {
    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new ClientAbortException());

    assertThat(response).isNull();
  }

  @Test
  public void aopCaughtTypeError_InvalidDataAccessApiUsageException_uuid() {
    String message = "Invalid UUID string: NotAValidUUID; nested exception is "
      + "java.lang.IllegalArgumentException: Invalid UUID string: NotAValidUUID";

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new InvalidDataAccessApiUsageException(message));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Invalid UUID string: NotAValidUUID");
  }

  @Test
  public void aopCaughtTypeError_InvalidDataAccessApiUsageException_SomethingElse() {
    String message = "A message";

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new InvalidDataAccessApiUsageException(message));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(message);
  }

  @ResponseStatus(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
  private static class BandwidthExceededException extends Exception {

    private static final long serialVersionUID = 1;

    private BandwidthExceededException(String message) {
      super(message);
    }
  }

  private static class ClientAbortException extends IOException {
    public ClientAbortException() {
      super(new NullPointerException("Test"));
    }
  }
}
