package com.elvaco.mvp.web;

import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorControllerTest extends IntegrationTest {
  @Test
  public void internalErrorsAreMappedCorrectly() {
    assertThat(
      asUser().get(
        Url.builder().path("/errors").build(),
        ErrorMessageDto.class
      ).getBody()
    ).isEqualTo(new ErrorMessageDto(
      "Internal server error, please contact support.",
      HttpStatus.INTERNAL_SERVER_ERROR.value()
    ));
  }

  @Test
  public void unauthenticatedAccessIsDisallowed() {
    assertThat(
      restClient().get(
        Url.builder().path("/errors").build(),
        ErrorMessageDto.class
      ).getStatusCode()
    ).isEqualTo(
      HttpStatus.UNAUTHORIZED
    );
  }
}
