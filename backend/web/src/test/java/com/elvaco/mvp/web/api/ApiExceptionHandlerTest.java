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

import static com.elvaco.mvp.web.util.Constants.ACCESS_IS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApiExceptionHandlerTest {

  private ApiExceptionHandler apiExceptionHandler;

  @Before
  public void setUp() {
    apiExceptionHandler = new ApiExceptionHandler();
  }

  @Test
  public void mappedException() throws Exception {
    Exception bandwidthException = new BandwidthExceededException("Oh no");

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(bandwidthException);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
    assertThat(response.getBody().message).isEqualTo("Oh no");
  }

  @Test
  public void internalErrorIsRethrown() {
    assertThatThrownBy(() -> apiExceptionHandler.handle(new Exception("test")))
      .isInstanceOf(Exception.class).hasMessage("test");
  }

  @Test
  public void accessDeniedException() {
    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new AccessDeniedException(ACCESS_IS_DENIED));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message).isEqualTo(ACCESS_IS_DENIED);
  }

  @Test
  public void clientAbortException() throws IOException {
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
    private ClientAbortException() {
      super(new NullPointerException("Test"));
    }
  }
}
