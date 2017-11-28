package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.repository.MeteringPointRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class MeteringPointControllerTest extends IntegrationTest {

  @Autowired
  MeteringPointRepository repository;
  @Before
  public void setUp() {
    MeteringPointEntity mp = new MeteringPointEntity();
    mp.propertyCollection = new PropertyCollection()
            .put("user", new UserPropertyDTO("abc123", "Some project"))
            .putArray("numbers", Arrays.asList(1, 2, 3, 17));
    repository.save(mp);
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

    assertThat(result).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
  public void findById() {
    ResponseEntity<MeteringPointEntity> response = restClient()
      .get("/mps/2", MeteringPointEntity.class);

    MeteringPointEntity meteringPoint = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meteringPoint.id).isEqualTo(2L);
  }

  @Test
  public void findAll() {
    ResponseEntity<List> response = restClient()
      .get("/mps", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }
}
