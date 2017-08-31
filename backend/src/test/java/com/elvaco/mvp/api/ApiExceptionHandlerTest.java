package com.elvaco.mvp.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.elvaco.mvp.dto.ErrorMessageDTO;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiExceptionHandlerTest {

  @Test
  public void checkException() throws Exception {
    ResponseEntity<ErrorMessageDTO> response = new ApiExceptionHandler().handle(new Exception("test"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().message).isEqualTo("test");
  }
}
