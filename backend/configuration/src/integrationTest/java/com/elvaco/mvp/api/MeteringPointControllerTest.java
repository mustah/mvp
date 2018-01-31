package com.elvaco.mvp.api;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.dto.MeteringPointDto;
import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class MeteringPointControllerTest extends IntegrationTest {

  @Autowired
  MeteringPoints meteringPointRepository;

  @Before
  public void setUp() {
    for (int x = 0; x < 55; x++) {
      mockMeteringPoint();
    }

    restClient().loginWith("evanil@elvaco.se", "eva123");
  }

  private void mockMeteringPoint() {
    MeteringPoint meteringPoint = new MeteringPoint(
      1L,
      "ok",
      new Location(1.1, 1.1, 1.1),
      new PropertyCollection(new UserProperty("abc123", "Some project"))
    );
    meteringPointRepository.save(meteringPoint);
  }

  @After
  public void tearDown() {
    meteringPointRepository.deleteAll();
    restClient().logout();
  }

  @Test
  public void findMatchesPropertyCollection() {
    PropertyCollectionDto request = new PropertyCollectionDto(new UserPropertyDto("abc123"));

    ResponseEntity<List> response = asElvacoUser()
      .post("/meters/property-collections", request, List.class);

    Map<String, Object> result = (Map<String, Object>) response
      .getBody()
      .get(0);

    assertThat(result).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void cannotFindMatchingPropertyCollection() {
    PropertyCollectionDto request = new PropertyCollectionDto(new UserPropertyDto("xyz"));

    ResponseEntity<List> response = asElvacoUser()
      .post("/meters/property-collections", request, List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void findById() {
    List<MeteringPoint> meteringPoints = meteringPointRepository.findAll();

    ResponseEntity<MeteringPointEntity> response = asElvacoUser()
      .get("/meters/" + meteringPoints.get(0).id, MeteringPointEntity.class);

    MeteringPointEntity meteringPoint = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meteringPoint.id).isEqualTo(meteringPoints.get(0).id);
  }

  @Test
  public void findAll() {
    Page<MeteringPointDto> response = asElvacoUser()
      .getPage("/meters", MeteringPointDto.class)
      .getBody()
      .newPage();

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(20);
    assertThat(response.getTotalPages()).isEqualTo(3);

    response = asElvacoUser()
      .getPage("/meters?page=2", MeteringPointDto.class)
      .getBody()
      .newPage();

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(15);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAllMapDataForMeteringPoints() {
    ResponseEntity<List> response = asElvacoUser()
      .get("/meters/map-data", List.class);

    assertThat(response.getBody().size()).isEqualTo(55);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
