package com.elvaco.mvp.api;

import java.util.Date;
import java.util.stream.Stream;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementControllerTest extends IntegrationTest {

  @Autowired
  private MeasurementRepository repository;

  @Autowired
  private PhysicalMeterRepository meterRepository;

  @Before
  public void setUp() {
    PhysicalMeterEntity physicalMeterEntity =
      new PhysicalMeterEntity(0L /*fixme: this should be an organisation entity*/,
                              "test-butter-meter-1",
                              "Butter"
      );
    meterRepository.save(physicalMeterEntity);
    Stream.of(
      new MeasurementEntity(
        new Date(),
        "Butter temperature",
        12.44,
        "°C",
        physicalMeterEntity
      ),
      new MeasurementEntity(
        new Date(),
        "Left to walk",
        500,
        "mi",
        physicalMeterEntity
      )
    ).forEach(repository::save);
  }

  @Test
  public void measurementsRetrievableAtEndpoint() {
    Page<MeasurementDto> page = apiService()
      .getPage("/measurements", MeasurementDto.class)
      .getBody()
      .newPage();

    assertThat(page.getContent().get(0).quantity).isEqualTo("Butter temperature");
  }

  @Test
  public void measurementRetrievableById() {
    MeasurementDto measurement = apiService().get("/measurements/1", MeasurementDto.class)
      .getBody();

    assertThat(measurement.id).isEqualTo(1L);
    assertThat(measurement.quantity).isEqualTo("Butter temperature");
    assertThat(measurement.value).isEqualTo(12.44);
  }

  @Test
  public void measurementUnitScaled() {
    Page<MeasurementDto> page = apiService()
      .getPage("/measurements?quantity=Butter temperature&scale=K", MeasurementDto.class)
      .getBody()
      .newPage();

    MeasurementDto measurementDto = page.getContent().get(0);
    assertThat(measurementDto.quantity).isEqualTo("Butter temperature");
    assertThat(measurementDto.unit).isEqualTo("K");
    assertThat(measurementDto.value).isEqualTo(285.59); // 12.44 Celsius = 285.59 Kelvin
  }

  @Test
  public void measurementLinksToItsPhysicalMeter() {
    String href = apiService()
      .get("/measurements/1", MeasurementDto.class)
      .getBody()
      .physicalMeter
      .getHref();

    assertThat(href).isEqualTo(restClient().getBaseUrl() + "/physical-meters/1");
  }

  private RestClient apiService() {
    return restClient().loginWith("user", "password");
  }
}
