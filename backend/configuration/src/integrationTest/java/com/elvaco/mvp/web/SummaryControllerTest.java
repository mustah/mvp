package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeterSummaryDto;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryControllerTest extends IntegrationTest {

  @Test
  public void whenNoMetersGetEmptySummaryInfo() {
    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto());
  }

  @Test
  public void getSummaryOfMetersShouldHaveOneResult() {
    given(logicalMeter().location(stockholm().build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
  }

  @Test
  public void metersWithSameAddress() {
    given(logicalMeter().location(stockholm().build()));
    given(logicalMeter().location(stockholm().build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }

  @Test
  public void shouldBeAbleToSearchForMetersWithAlarms() {
    var logicalMeter = given(logicalMeter().location(stockholm().build()));
    given(logicalMeter().location(stockholm().build()));

    var start = context().now();

    given(alarm(logicalMeter).start(start));

    var url = Url.builder()
      .path("/summary/meters")
      .parameter(AFTER, start)
      .parameter(BEFORE, start.plusDays(2))
      .parameter(ALARM, "yes")
      .build();

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get(url, MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
  }

  @Test
  public void filteredByStatus() {
    given(logicalMeter().location(stockholm().build()));
    given(logicalMeter().location(kungsbacka().address("gatan med G").build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get(
        "/summary/meters?reported=ok&before=2019-01-01T00:00:00Z&after=2018-01-01T00:00:00Z",
        MeterSummaryDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void metersWithDifferentAddresses() {
    given(logicalMeter().location(stockholm().build()));
    given(logicalMeter().location(kungsbacka().build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 2, 2));
  }

  @Test
  public void metersWithSameCityDifferentStreetAddress() {
    given(logicalMeter().location(stockholm().address("kungsgatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 2").build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 3));
  }

  @Test
  public void metersWithUnknownLocation() {
    given(logicalMeter().location(Location.UNKNOWN_LOCATION));
    given(logicalMeter().location(stockholm().address("kungsgatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 2").build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 2));
  }

  @Test
  public void userCanOnlyGetSummaryForMetersWithinTheOrganisation() {
    given(logicalMeter().location(stockholm().address("kungsgatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 2").build()));

    ResponseEntity<MeterSummaryDto> response = asOtherUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void fetchOnlyMetersInKungsbacka() {
    given(logicalMeter().location(stockholm().address("kungsgatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 1").build()));
    given(logicalMeter().location(kungsbacka().address("drottninggatan 2").build()));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters?city=sverige,kungsbacka", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 2));
  }

  @Test
  public void fetchNumberOfMetersDistinct_WhenTheyHaveManyStatusUpdates() {
    LogicalMeter logicalMeter = given(logicalMeter().location(stockholm().build()));
    given(logicalMeter().location(stockholm().build()));

    given(
      statusLog(logicalMeter).status(StatusType.OK).start(context().now()),
      statusLog(logicalMeter).status(StatusType.WARNING).start(context().now().plusDays(1))
    );

    ResponseEntity<MeterSummaryDto> response = asUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }
}
