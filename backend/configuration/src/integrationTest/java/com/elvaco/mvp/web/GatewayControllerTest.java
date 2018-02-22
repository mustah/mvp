package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GatewayDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class GatewayControllerTest extends IntegrationTest {

  @Autowired
  private GatewayJpaRepository jpaRepository;

  @After
  public void tearDown() {
    jpaRepository.deleteAll();
  }

  @Test
  public void fetchAllGateways() {
    ResponseEntity<List> response = asSuperAdmin().get("/gateways", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void createNewGateway() {
    GatewayDto requestModel = new GatewayDto(null, "123", "2100");

    ResponseEntity<GatewayDto> response = asSuperAdmin()
      .post("/gateways", requestModel, GatewayDto.class);

    GatewayDto created = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(created.id).isPositive();
    assertThat(created.serial).isEqualTo("123");
    assertThat(created.productModel).isEqualTo("2100");
  }
}
