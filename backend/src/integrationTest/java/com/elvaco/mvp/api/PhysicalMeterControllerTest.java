package com.elvaco.mvp.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.testdata.RestResponsePage;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;


public class PhysicalMeterControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementRepository measurementRepository;

  @Autowired
  private PhysicalMeterRepository physicalMeterRepository;

  @Before
  public void setUp() {
    PhysicalMeterEntity forceMeter = new PhysicalMeterEntity(1L,
        "force-meter", "Jedi aptitude");
    physicalMeterRepository.save(forceMeter);
    measurementRepository.save(
        Stream.of(
            new MeasurementEntity(
                new Date(),
                "Midi-chlorians",
                20001,
                "one", //What are midichlorians measured in?
                forceMeter),
            new MeasurementEntity(
                //https://scifi.stackexchange.com/a/28354
                new Date(),
                "LightsaberPower",
                28,
                "kW",
                forceMeter),
            new MeasurementEntity(
                Date.from(Instant.parse("1983-05-24T12:00:01Z")),
                "LightsaberPower",
                0,
                "kW",
                forceMeter

            )
        ).collect(Collectors.toList())
    );
  }

  @Test
  public void fetchMeasurementsForMeter() {
    ResponseEntity<RestResponsePage<MeasurementDto>> responseEntity =
        rest().getPage("/physical-meters/1/measurements", MeasurementDto.class);

    RestResponsePage<MeasurementDto> measurementsPage = responseEntity.getBody();
    Page<MeasurementDto> page = measurementsPage.pageImpl();
    List<MeasurementDto> contents = page.getContent();
    assertThat(contents).isNotEmpty();
    MeasurementDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("Midi-chlorians");
    // "The readings are off the chart. Over 20,000. Even Master Yoda
    //       doesn't have a midi-chlorian count that high."
    assertThat(dto.value).isEqualTo(20001);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantity() {
    ResponseEntity<RestResponsePage<MeasurementDto>> responseEntity =
        rest().getPage(
            "/physical-meters/1/measurements/LightsaberPower",
            MeasurementDto.class);

    RestResponsePage<MeasurementDto> measurementsPage = responseEntity.getBody();
    Page<MeasurementDto> page = measurementsPage.pageImpl();
    List<MeasurementDto> contents = page.getContent();
    assertThat(contents).isNotEmpty();
    MeasurementDto dto = contents.get(0);
    assertThat(contents.size()).isEqualTo(2);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(28);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantity2() {
    ResponseEntity<RestResponsePage<MeasurementDto>> responseEntity =
        rest().getPage(
            "/physical-meters/1/measurements?id=1&id=2",
            MeasurementDto.class);

    RestResponsePage<MeasurementDto> measurementsPage = responseEntity.getBody();
    Page<MeasurementDto> page = measurementsPage.pageImpl();
    List<MeasurementDto> contents = page.getContent();
    assertThat(contents).isNotEmpty();
    assertThat(contents.size()).isEqualTo(2);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityBeforeTime() {
    ResponseEntity<RestResponsePage<MeasurementDto>> responseEntity =
        rest().getPage(
            "/physical-meters/1/measurements/LightsaberPower?before=1990-01-01T08:00:00Z",
            MeasurementDto.class);

    RestResponsePage<MeasurementDto> measurementsPage = responseEntity.getBody();
    Page<MeasurementDto> page = measurementsPage.pageImpl();
    List<MeasurementDto> contents = page.getContent();
    assertThat(contents).size().isEqualTo(1);
    MeasurementDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(0);
  }

  @Test
  public void fetchMeasurementsForMeterByQuantityAfterTime() {
    ResponseEntity<RestResponsePage<MeasurementDto>> responseEntity =
        rest().getPage(
            "/physical-meters/1/measurements/LightsaberPower?after=1990-01-01T08:00:00Z",
            MeasurementDto.class);

    RestResponsePage<MeasurementDto> measurementsPage = responseEntity.getBody();
    Page<MeasurementDto> page = measurementsPage.pageImpl();
    List<MeasurementDto> contents = page.getContent();
    assertThat(contents).size().isEqualTo(1);
    MeasurementDto dto = contents.get(0);
    assertThat(dto.quantity).isEqualTo("LightsaberPower");
    assertThat(dto.value).isEqualTo(28);
  }

  private RestClient rest() {
    return restClient().loginWith("user", "password");
  }

}
