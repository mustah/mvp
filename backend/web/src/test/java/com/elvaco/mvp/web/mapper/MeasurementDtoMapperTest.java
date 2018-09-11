package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementDtoMapperTest {

  @Test
  public void emtpySeries() {
    assertThat(MeasurementDtoMapper.toSeries(emptyList())).isEqualTo(emptyList());
  }

  @Test
  public void oneMeasurementMappedToSeries() {
    UUID logicalMeterId = randomUUID();
    UUID physicalMeterId = randomUUID();
    Measurement measurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));

    List<MeasurementSeriesDto> expected = singletonList(
      new MeasurementSeriesDto(
        logicalMeterId.toString(),
        "Cheese",
        "pcs",
        physicalMeterId.toString(),
        "city",
        "address",
        "medium",
        singletonList(
          new MeasurementValueDto(measurement.created.toInstant(), 3.0)
        )
      )
    );

    assertThat(MeasurementDtoMapper.toSeries(singletonList(
      LabeledMeasurementValue.of(measurement, logicalMeterId, "city", "address", "medium")
    ))).isEqualTo(expected);
  }

  @Test
  public void twoMeasurementsMappedToSameSeries() {
    UUID logicalMeterId = randomUUID();
    UUID physicalMeterId = randomUUID();
    Measurement firstMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));

    List<MeasurementSeriesDto> expected = singletonList(
      new MeasurementSeriesDto(
        logicalMeterId.toString(),
        "Cheese",
        "pcs",
        physicalMeterId.toString(),
        "city",
        "address",
        "medium",
        asList(
          new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0),
          new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
        )
      )
    );

    assertThat(MeasurementDtoMapper.toSeries(asList(
      LabeledMeasurementValue.of(firstMeasurement, logicalMeterId, "city", "address", "medium"),
      LabeledMeasurementValue.of(secondMeasurement, logicalMeterId, "city", "address", "medium")
    ))).isEqualTo(expected);
  }

  @Test
  public void twoSeriesFromSameMeter() {
    UUID logicalMeterId = randomUUID();
    UUID physicalMeterId = randomUUID();
    Measurement firstMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondMeasurement = newMeasurement(physicalMeterId, new Quantity("Milk", "l"));

    assertThat(MeasurementDtoMapper.toSeries(asList(
      LabeledMeasurementValue.of(firstMeasurement, logicalMeterId, "city", "address", "medium"),
      LabeledMeasurementValue.of(secondMeasurement, logicalMeterId, "city", "address", "medium")
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          logicalMeterId.toString(),
          "Cheese",
          "pcs",
          physicalMeterId.toString(),
          "city",
          "address",
          "medium",
          singletonList(
            new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          logicalMeterId.toString(),
          "Milk",
          "l",
          physicalMeterId.toString(),
          "city",
          "address",
          "medium",
          singletonList(
            new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
          )
        )
      )
    );
  }

  @Test
  public void sameQuantityDifferentMeters() {
    UUID firstLogicalMeterId = randomUUID();
    UUID secondLogicalMeterId = randomUUID();
    UUID firstPhysicalMeterId = randomUUID();
    UUID secondPhysicalMeterId = randomUUID();
    Measurement firstMeasurement = newMeasurement(
      firstPhysicalMeterId,
      new Quantity("Cheese", "pcs")
    );
    Measurement secondMeasurement = newMeasurement(
      secondPhysicalMeterId,
      new Quantity("Cheese", "pcs")
    );

    assertThat(MeasurementDtoMapper.toSeries(asList(
      LabeledMeasurementValue.of(
        firstMeasurement,
        firstLogicalMeterId,
        "city",
        "address",
        "medium"
      ),
      LabeledMeasurementValue.of(
        secondMeasurement,
        secondLogicalMeterId,
        "city",
        "address",
        "medium"
      )
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          firstLogicalMeterId.toString(),
          "Cheese",
          "pcs",
          firstPhysicalMeterId.toString(),
          "city",
          "address",
          "medium",
          singletonList(
            new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          secondLogicalMeterId.toString(),
          "Cheese",
          "pcs",
          secondPhysicalMeterId.toString(),
          "city",
          "address",
          "medium",
          singletonList(
            new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
          )
        )
      ));
  }

  @Test
  public void differentlySizedSeries() {
    UUID logicalMeterId = randomUUID();
    UUID physicalMeterId = randomUUID();
    Measurement firstCheese = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondCheese = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement firstMilk = newMeasurement(physicalMeterId, new Quantity("Milk", "l"));

    assertThat(MeasurementDtoMapper.toSeries(asList(
      LabeledMeasurementValue.of(firstCheese, logicalMeterId, "city", "address", "medium"),
      LabeledMeasurementValue.of(secondCheese, logicalMeterId, "city", "address", "medium"),
      LabeledMeasurementValue.of(firstMilk, logicalMeterId, "city", "address", "medium")
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          logicalMeterId.toString(),
          "Cheese",
          "pcs",
          physicalMeterId.toString(),
          "city",
          "address",
          "medium",
          asList(
            new MeasurementValueDto(firstCheese.created.toInstant(), 3.0),
            new MeasurementValueDto(secondCheese.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          logicalMeterId.toString(),
          "Milk",
          "l",
          physicalMeterId.toString(),
          "city",
          "address",
          "medium",
          singletonList(
            new MeasurementValueDto(firstMilk.created.toInstant(), 3.0)
          )
        )
      ));
  }

  private Measurement newMeasurement(UUID physicalMeterId, Quantity quantity) {
    return Measurement.builder()
      .created(ZonedDateTime.now())
      .quantity(quantity.name)
      .unit(quantity.presentationUnit())
      .value(3.0)
      .physicalMeter(PhysicalMeter.builder()
        .id(physicalMeterId)
        .organisation(new Organisation(
          randomUUID(),
          "An organisation",
          "an-organisation",
          "An organisation"
        ))
        .externalId(physicalMeterId.toString())
        .address("1234")
        .medium("Heat")
        .manufacturer("ELV")
        .readIntervalMinutes(15)
        .build()
      )
      .build();
  }
}
