package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Comparator;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.OrganisationWithUsers;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.MeasurementDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerPagedTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void isPageable() {
    var date = context().now();
    var logicalGasMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));
    given(measurementSeries()
      .forMeter(logicalGasMeter)
      .withQuantity(Quantity.VOLUME)
      .startingAt(context().now())
      .withValues(1, 2, 5, 6));

    var logicalMeter2 = given(logicalMeter().meterDefinition(DEFAULT_GAS));
    given(measurementSeries()
      .forMeter(logicalMeter2)
      .startingAt(date.plusHours(4))
      .withQuantity(Quantity.VOLUME)
      .withValues(7));

    var url = urlFrom(logicalGasMeter.id);

    Page<MeasurementDto> firstPage = asUser().getPage(url, MeasurementDto.class);

    assertThat(firstPage.getTotalElements()).isEqualTo(4);
    assertThat(firstPage.getTotalPages()).isEqualTo(1);
    assertThat(firstPage.getContent())
      .usingComparatorForElementFieldsWithType(
        Comparator.comparing(ChronoZonedDateTime::toInstant),
        ZonedDateTime.class
      )
      .usingFieldByFieldElementComparator()
      .containsExactlyInAnyOrder(
        new MeasurementDto("Volume", 1.0, "m³", date),
        new MeasurementDto("Volume", 2.0, "m³", date.plusHours(1)),
        new MeasurementDto("Volume", 5.0, "m³", date.plusHours(2)),
        new MeasurementDto("Volume", 6.0, "m³", date.plusHours(3))
      );
  }

  @Test
  public void unableToAccessOtherOrganisation() {
    var created = context().now();
    OrganisationWithUsers organisationWithUsers = given(organisation(), user());
    var meter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING)
      .organisationId(organisationWithUsers.getId()));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(created)
      .withQuantity(Quantity.DIFFERENCE_TEMPERATURE)
      .withValues(285.59));

    var url = urlFrom(meter.id);
    Page<MeasurementDto> wrongUserResponse = asUser().getPage(url, MeasurementDto.class);

    assertThat(wrongUserResponse).hasSize(0);

    Page<MeasurementDto> correctUserResponse = as(organisationWithUsers.getUser()).getPage(
      url,
      MeasurementDto.class
    );

    assertThat(correctUserResponse).hasSize(1);
  }

  @Test
  public void defaultsToDecidedUponUnits() {
    var after = context().now();
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));
    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(after)
      .withQuantity(Quantity.ENERGY)
      .withValues(1.0));

    var url = urlFrom(districtHeatingMeter.id);
    Page<MeasurementDto> firstPage = asUser().getPage(url, MeasurementDto.class);

    assertThat(firstPage.getContent()).extracting("unit").containsExactly("kWh");
  }

  private static Url urlFrom(UUID logicalMeterId) {
    return Url.builder()
      .path("/measurements/paged/")
      .parameter(LOGICAL_METER_ID, logicalMeterId)
      .build();
  }
}
