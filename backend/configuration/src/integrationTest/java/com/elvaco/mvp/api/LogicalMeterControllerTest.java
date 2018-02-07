package com.elvaco.mvp.api;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.LogicalMeterDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class LogicalMeterControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeters logicalMeterRepository;

  @Before
  public void setUp() {
    logicalMeterRepository.deleteAll();

    for (int x = 1; x <= 55; x++) {
      String status = x % 10 == 0 ? "Warning" : "Ok";
      mockLogicalMeter(x, status);
    }

    restClient().loginWith("evanil@elvaco.se", "eva123");
  }

  @After
  public void tearDown() {
    logicalMeterRepository.deleteAll();

    restClient().logout();
  }

  @Test
  public void findById() {
    List<LogicalMeter> logicalMeters = logicalMeterRepository.findAll();

    ResponseEntity<LogicalMeterDto> response = asElvacoUser()
      .get("/meters/" + logicalMeters.get(0).id, LogicalMeterDto.class);

    LogicalMeterDto logicalMeterDto = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logicalMeterDto.id).isEqualTo(logicalMeters.get(0).id);
  }

  @Test
  public void findAll() {
    Page<LogicalMeterDto> response = asElvacoUser()
      .getPage("/meters", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(20);
    assertThat(response.getTotalPages()).isEqualTo(3);

    response = asElvacoUser()
      .getPage("/meters?page=2", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);
    assertThat(response.getNumberOfElements()).isEqualTo(15);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAllWithinPeriod() {
    Page<LogicalMeterDto> response = restClient()
      .getPage(
        "/meters?before=2001-01-20T10:10:00.00Z&after=2001-01-10T10:10:00.00Z",
        LogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(10);
    assertThat(response.getNumberOfElements()).isEqualTo(10);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllWithPredicates() {
    Page<LogicalMeterDto> response = restClient()
      .getPage("/meters?status=Warning", LogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(5);
    assertThat(response.getNumberOfElements()).isEqualTo(5);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findAllMapDataForLogicalMeters() {
    ResponseEntity<List> response = asElvacoUser()
      .get("/meters/map-data", List.class);

    assertThat(response.getBody().size()).isEqualTo(55);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void mockLogicalMeter(int seed, String status) {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(created);
    calendar.add(Calendar.DATE, seed);
    created = calendar.getTime();

    LogicalMeter logicalMeter = new LogicalMeter(
      status,
      new LocationBuilder()
        .coordinate(new GeoCoordinate(1.1, 1.1, 1.0)).build(),
      created,
      new PropertyCollection(new UserProperty("abc123", "Some project"))
    );
    logicalMeterRepository.save(logicalMeter);
  }
}
