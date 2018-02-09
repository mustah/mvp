package com.elvaco.mvp.web.api;

import com.elvaco.mvp.web.dto.ErrorMessageDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiExceptionHandlerTest {

  private ApiExceptionHandler apiExceptionHandler;

  @Before
  public void setUp() {
    apiExceptionHandler = new ApiExceptionHandler();
  }

  @Test
  public void checkException() {
    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new Exception("test"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().message).isEqualTo("test");
  }

  @Test
  public void accessDeniedException() {
    String message = "Access is denied";

    ResponseEntity<ErrorMessageDto> response =
      apiExceptionHandler.handle(new AccessDeniedException(message));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message).isEqualTo(message);
  }
}
