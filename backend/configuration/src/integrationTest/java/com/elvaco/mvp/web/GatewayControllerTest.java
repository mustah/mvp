package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GatewayDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class GatewayControllerTest extends IntegrationTest {

  private Organisation dailyPlanet;

  @Before
  public void setUp() {
    dailyPlanet = given(organisation());
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    organisations.deleteById(dailyPlanet.id);
  }

  @Test
  public void superAdminCanGetSingleGateway() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<GatewayDto> response = asSuperAdmin()
      .get("/gateways/" + gatewayId, GatewayDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(gatewayId);
  }

  private Gateway saveGateway(UUID organisationId) {
    return gateways.save(Gateway.builder()
      .organisationId(organisationId)
      .serial(randomUUID().toString())
      .productModel(randomUUID().toString())
      .build());
  }
}
