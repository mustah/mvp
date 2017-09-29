package com.elvaco.mvp.api;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.properycollection.UserPropertyDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import static com.elvaco.mvp.testdata.RestClient.restClient;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
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
  public void findMatchesPropertyCollection() {
    PropertyCollectionDTO request = new PropertyCollectionDTO(new UserPropertyDTO("abc123"));

    ResponseEntity<List> response = restClient()
      .post("/mps/property-collections", request, List.class);

    Map<String, Object> result = (Map<String, Object>) response
      .getBody()
      .get(0);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.get("id")).isEqualTo(3);
    assertThat(result.get("moid")).isEqualTo("3");
    assertThat(result.get("status")).isEqualTo(200);
    assertThat(result.get("message")).isEqualTo("Low battery.");
  }

  @Test
  public void cannotFindMatchingPropertyCollection() {
    PropertyCollectionDTO request = new PropertyCollectionDTO(new UserPropertyDTO("xyz"));

    ResponseEntity<List> response = restClient()
      .post("/mps/property-collections", request, List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void findByMoid() {
    ResponseEntity<MeteringPointEntity> response = restClient()
      .get("/mps/2", MeteringPointEntity.class);

    MeteringPointEntity meteringPoint = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meteringPoint.moid).isEqualTo("2");
    assertThat(meteringPoint.status).isEqualTo(0);
    assertThat(meteringPoint.message).isEmpty();
    assertThat(meteringPoint.propertyCollection).isNull();
  }

  @Test
  public void findAll() {
    ResponseEntity<List> response = restClient()
      .get("/mps", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }
}
