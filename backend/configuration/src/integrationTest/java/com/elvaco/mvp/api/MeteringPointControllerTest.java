package com.elvaco.mvp.api;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.core.usecase.MeteringPoints;
import com.elvaco.mvp.dto.MeteringPointDto;
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
  private MeteringPoints meteringPointRepository;

  @Before
  public void setUp() {
    for (int x = 1; x <= 55; x++) {
      String status = x % 10 == 0 ? "Warning" : "Ok";
      mockMeteringPoint(x, status);
    }

    restClient().loginWith("evanil@elvaco.se", "eva123");
  }

  @After
  public void tearDown() {
    meteringPointRepository.deleteAll();

    restClient().logout();
  }

  @Test
  public void findById() {
    List<MeteringPoint> meteringPoints = meteringPointRepository.findAll();

    ResponseEntity<MeteringPointDto> response = asElvacoUser()
      .get("/meters/" + meteringPoints.get(0).id, MeteringPointDto.class);

    MeteringPointDto meteringPoint = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meteringPoint.id).isEqualTo(meteringPoints.get(0).id);
  }

  @Test
  public void findAll() {
    Page<MeteringPointDto> response = asElvacoUser()
      .getPage("/meters", MeteringPointDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(20);
    assertThat(response.getTotalPages()).isEqualTo(3);

    response = asElvacoUser()
      .getPage("/meters?page=2", MeteringPointDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(15);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAllWithinPeriod() {
    Page<MeteringPointDto> response = restClient()
      .getPage(
        "/meters?before=2001-01-20T10:10:00.00Z&after=2001-01-10T10:10:00.00Z",
        MeteringPointDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(10);
    assertThat(response.getNumberOfElements()).isEqualTo(10);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllWithPredicates() {
    Page<MeteringPointDto> response = restClient()
      .getPage("/meters?status=Warning", MeteringPointDto.class);

    assertThat(response.getTotalElements()).isEqualTo(5);
    assertThat(response.getNumberOfElements()).isEqualTo(5);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllMapDataForMeteringPoints() {
    ResponseEntity<List> response = asElvacoUser()
      .get("/meters/map-data", List.class);

    assertThat(response.getBody().size()).isEqualTo(55);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void mockMeteringPoint(int seed, String status) {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    MeteringPoint meteringPoint = new MeteringPoint(
      status,
      new LocationBuilder()
        .coordinate(new GeoCoordinate(1.1, 1.1, 1.0)).build(),
      created,
      new PropertyCollection(new UserProperty("abc123", "Some project"))
    );
    meteringPointRepository.save(meteringPoint);
  }
}
