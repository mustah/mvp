package com.elvaco.mvp.api;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.repository.jpa.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.fixture.Entities.WAYNE_INDUSTRIES_ENTITY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;

  @Autowired
  private PhysicalMeterRepository physicalMeterRepository;

  private Long id;
  private List<MeasurementEntity> saved;

  @Before
  public void setUp() {
    PhysicalMeterEntity forceMeter = new PhysicalMeterEntity(
      WAYNE_INDUSTRIES_ENTITY,
      String.valueOf(Math.random()),
      "vacum"
    );
    id = physicalMeterRepository.save(forceMeter).id;

    // What are midichlorians measured in?
    // https://scifi.stackexchange.com/a/28354
    saved = measurementJpaRepository.save(
      asList(
        new MeasurementEntity(
          new Date(),
          "Heat",
          150,
          "°C",
          forceMeter
        ),
        new MeasurementEntity(
          //https://scifi.stackexchange.com/a/28354
          new Date(),
          "LightsaberPower",
          28,
          "kW",
          forceMeter
        ),
        new MeasurementEntity(
          Date.from(Instant.parse("1983-05-24T12:00:01Z")),
          "LightsaberPower",
          0,
          "kW",
          forceMeter
        )
      ));
  }

  @Test
  public void fetchMeasurementsForMeter() {
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/physical-meters/" + id + "/measurements")
        .getContent();

    assertThat(contents).hasSize(3);
  }

  @Test
  public void fetchMeasurementsForHeatMeter() {
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/physical-meters/" + id + "/measurements/Heat")
        .getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("Heat");

    // TODO[!must!] fix this when we have a PhysicalModelMapper in place!
    /*String valueAndUnit = dto.value + " " + dto.unit;
    assertThat(toMeasurementUnit(valueAndUnit, "K").toString()).isEqualTo("423.15 K");*/
  }

  @Test
  public void fetchMeasurementsForMeterByQuantity() {
    Long id1 = saved.get(0).id;
    Long id2 = saved.get(1).id;
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/physical-meters/" + id + "/measurements?id=" + id1 + "&id=" + id2)
        .getContent();

    assertThat(contents).hasSize(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityBeforeTime() {
    String date = "1990-01-01T08:00:00Z";
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/physical-meters/" + id + "/measurements/LightsaberPower?before=" + date)
        .getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    String date = "1990-01-01T08:00:00Z";
    List<MeasurementDto> contents =
      getPageAsSuperAdmin("/physical-meters/" + id + "/measurements/LightsaberPower?after=" + date)
        .getContent();

    MeasurementDto dto = contents.get(0);
    assertThat(contents).hasSize(1);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(28);
  }

  private Page<MeasurementDto> getPageAsSuperAdmin(String url) {
    return asSuperAdmin()
      .getPage(url, MeasurementDto.class);
  }
}
