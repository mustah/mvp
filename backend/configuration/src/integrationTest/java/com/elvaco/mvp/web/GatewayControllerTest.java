package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class GatewayControllerTest extends IntegrationTest {

  @Test
  public void fetchAllGateways() {
    ResponseEntity<List> response = asSuperAdmin().get("/gateways", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

}
