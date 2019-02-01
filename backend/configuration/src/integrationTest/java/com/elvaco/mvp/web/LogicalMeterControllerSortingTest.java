package com.elvaco.mvp.web;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_COOLING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterControllerSortingTest extends IntegrationTest {

  @Test
  public void findAll_SortsByFacilityByDefault() {
    given(
      logicalMeter().externalId("0005"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004"),
      logicalMeter().externalId("0002")
    );

    testSorting("", meter -> meter.facility, List.of("0001", "0002", "0003", "0004", "0005"));
  }

  @Test
  public void findAll_SortsByFacilityIfSortPropertyIsInvalid() {
    given(
      logicalMeter().externalId("0005"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004"),
      logicalMeter().externalId("0002")
    );

    testSorting(
      //this says desc
      "invalid,desc",
      meter -> meter.facility,
      // but since the property is invalid, we do asc anyway
      List.of("0001", "0002", "0003", "0004", "0005")
    );
  }

  @Test
  public void findAll_SortsByFacility() {
    given(
      logicalMeter().externalId("0005"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004"),
      logicalMeter().externalId("0002")
    );

    testSorting(
      "facility,asc",
      meter -> meter.facility,
      List.of("0001", "0002", "0003", "0004", "0005")
    );

    testSorting(
      "facility,desc",
      meter -> meter.facility,
      List.of("0005", "0004", "0003", "0002", "0001")
    );
  }

  @Test
  public void findAll_SortByAddress() {
    given(
      logicalMeter().location(kungsbacka().address("a-gatan 2").build()),
      logicalMeter().location(kungsbacka().address("a-gatan 1").build()),
      logicalMeter().location(stockholm().address("b-gatan 1").build())
    );

    testSorting(
      "address,asc",
      meter -> meter.location.address,
      List.of("a-gatan 1", "a-gatan 2", "b-gatan 1")
    );

    testSorting(
      "address,desc",
      meter -> meter.location.address,
      List.of("b-gatan 1", "a-gatan 2", "a-gatan 1")
    );
  }

  @Test
  public void findAll_SortByAddress_NordicCollation() {
    given(
      logicalMeter().location(kungsbacka().address("a 2").build()),
      logicalMeter().location(kungsbacka().address("ä 1").build()),
      logicalMeter().location(kungsbacka().address("a 1").build()),
      logicalMeter().location(kungsbacka().address("ø 1").build()),
      logicalMeter().location(kungsbacka().address("å 1").build()),
      logicalMeter().location(kungsbacka().address("o 1").build()),
      logicalMeter().location(kungsbacka().address("æ 1").build()),
      logicalMeter().location(kungsbacka().address("ö 1").build())
    );

    testSorting(
      "address,asc",
      meter -> meter.location.address,
      List.of("a 1", "a 2", "o 1", "å 1", "ä 1", "æ 1", "ö 1", "ø 1")
    );
  }

  @Test
  public void findAll_SortByManufacturer() {
    given(logicalMeter(), physicalMeter().manufacturer("AAA"));
    given(logicalMeter(), physicalMeter().manufacturer("CCC"));
    given(logicalMeter(), physicalMeter().manufacturer("BBB"));

    testSorting(
      "manufacturer,asc",
      meter -> meter.manufacturer,
      List.of("AAA", "BBB", "CCC")
    );

    testSorting(
      "manufacturer,desc",
      meter -> meter.manufacturer,
      List.of("CCC", "BBB", "AAA")
    );
  }

  @Test
  public void findAll_SortByCity() {
    given(
      logicalMeter().location(kungsbacka().build()),
      logicalMeter().location(kungsbacka().city("alingsås").build()),
      logicalMeter().location(stockholm().build())
    );

    testSorting(
      "city,asc",
      meter -> meter.location.city,
      List.of("alingsås", "kungsbacka", "stockholm")
    );

    testSorting(
      "city,desc",
      meter -> meter.location.city,
      List.of("stockholm", "kungsbacka", "alingsås")
    );
  }

  @Test
  public void findAll_SortByCity_NordicCollation() {
    given(
      logicalMeter().location(kungsbacka().city("østad").build()),
      logicalMeter().location(kungsbacka().city("åstad").build()),
      logicalMeter().location(kungsbacka().city("astad").build()),
      logicalMeter().location(kungsbacka().city("ystad").build()),
      logicalMeter().location(kungsbacka().city("östad").build())
    );

    testSorting(
      "city,asc",
      meter -> meter.location.city,
      List.of("astad", "ystad", "åstad", "östad", "østad")
    );
  }

  @Test
  public void findAll_SortByMeterAddress() {
    given(logicalMeter(), physicalMeter().address("1234"));
    given(logicalMeter(), physicalMeter().address("2345"));
    given(logicalMeter(), physicalMeter().address("3456"));

    testSorting(
      "secondaryAddress,asc",
      meter -> meter.address,
      List.of("1234", "2345", "3456")
    );

    testSorting(
      "secondaryAddress,desc",
      meter -> meter.address,
      List.of("3456", "2345", "1234")
    );
  }

  @Test
  public void findAll_SortByMeterAddress_IsNumericSort() {
    given(logicalMeter(), physicalMeter().address("10000"));
    given(logicalMeter(), physicalMeter().address("999"));
    given(logicalMeter(), physicalMeter().address("5"));

    testSorting(
      "secondaryAddress,asc",
      meter -> meter.address,
      List.of("5", "999", "10000")
    );

    testSorting(
      "secondaryAddress,desc",
      meter -> meter.address,
      List.of("10000", "999", "5")
    );
  }

  @Test
  public void findAll_SortByMedium() {
    given(
      logicalMeter().meterDefinition(DISTRICT_HEATING_METER),
      logicalMeter().meterDefinition(DISTRICT_COOLING_METER),
      logicalMeter().meterDefinition(HOT_WATER_METER)
    );

    testSorting(
      "medium,asc",
      meter -> meter.medium,
      List.of(DISTRICT_COOLING_METER.medium, DISTRICT_HEATING_METER.medium, HOT_WATER_METER.medium)
    );

    testSorting(
      "medium,desc",
      meter -> meter.medium,
      List.of(HOT_WATER_METER.medium, DISTRICT_HEATING_METER.medium, DISTRICT_COOLING_METER.medium)
    );
  }

  @Test
  public void findAll_SortByGatewaySerial() {
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter(),
      logicalMeter(),
      logicalMeter()
    ));
    given(
      gateway().serial("1234").meter(meters.get(0)),
      gateway().serial("4321").meter(meters.get(1)),
      gateway().serial("3142").meter(meters.get(2))
    );

    testSorting(
      "gatewaySerial,asc",
      meter -> meter.gatewaySerial,
      List.of("1234", "3142", "4321")
    );

    testSorting(
      "gatewaySerial,desc",
      meter -> meter.gatewaySerial,
      List.of("4321", "3142", "1234")
    );
  }

  @Test
  public void findAll_SortByGatewaySerial_IsNumericSort() {
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter(),
      logicalMeter(),
      logicalMeter()
    ));
    given(
      gateway().serial("1000").meter(meters.get(0)),
      gateway().serial("999").meter(meters.get(1)),
      gateway().serial("110").meter(meters.get(2))
    );

    testSorting(
      "gatewaySerial,asc",
      meter -> meter.gatewaySerial,
      List.of("110", "999", "1000")
    );

    testSorting(
      "gatewaySerial,desc",
      meter -> meter.gatewaySerial,
      List.of("1000", "999", "110")
    );
  }

  private void testSorting(
    String sort,
    Function<PagedLogicalMeterDto, String> actual,
    List<String> expectedProperties
  ) {
    Url url = Url.builder()
      .path("/meters")
      .size(expectedProperties.size())
      .page(0)
      .sortBy(sort)
      .build();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(response)
      .extracting(actual)
      .containsExactlyElementsOf(
        expectedProperties.stream().map(Assertions::tuple).collect(toList())
      );
  }
}