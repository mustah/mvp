package com.elvaco.mvp.api;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.properycollection.UserPropertyDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import static com.elvaco.mvp.testdata.RestClient.restClient;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointControllerTest extends IntegrationTest {

  @Before
  public void setUp() {
    restClient().loginWith("evanil@elvaco.se", "eva123");
  }

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void requestModelContainsInPropertyCollection() {
    PropertyCollectionDTO request = new PropertyCollectionDTO(new UserPropertyDTO("abc123"));

    ResponseEntity<List> response = restClient()
      .post("/mps/property-collections", request, List.class);

    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  public void requestModelDoesNotContainsInPropertyCollection() {
    PropertyCollectionDTO request = new PropertyCollectionDTO(new UserPropertyDTO("xyz"));

    ResponseEntity<List> response = restClient()
      .post("/mps/property-collections", request, List.class);

    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void findMeterPointByMoid() {
    ResponseEntity<MeteringPointEntity> response = restClient()
      .get("/mps/2", MeteringPointEntity.class);

    assertThat(response.getBody().moid).isEqualTo("2");
  }

  @Test
  public void findAllMeteringPoints() {
    ResponseEntity<List> response = restClient()
      .get("/mps", List.class);

    assertThat(response.getBody()).isNotEmpty();
  }
}
