package com.elvaco.mvp.web.mapper;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementMapperTest {

  private MeasurementMapper mapper;

  @Before
  public void setUp() {
    mapper = new MeasurementMapper();
  }

  @Test
  public void emtpySeries() {
    assertThat(mapper.toSeries(emptyList())).isEqualTo(emptyList());
  }

  @Test
  public void oneMeasurementMappedToSeries() {
    UUID physicalMeterId = UUID.randomUUID();
    Measurement measurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));

    List<MeasurementSeriesDto> expected = singletonList(
      new MeasurementSeriesDto(
        "Cheese",
        "pcs",
        physicalMeterId.toString(),
        singletonList(
          new MeasurementValueDto(measurement.created.toInstant(), 3.0)
        )
      )
    );

    assertThat(mapper.toSeries(singletonList(
      LabeledMeasurementValue.from(measurement)
    ))).isEqualTo(expected);
  }

  @Test
  public void twoMeasurementsMappedToSameSeries() {
    UUID physicalMeterId = UUID.randomUUID();
    Measurement firstMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));

    List<MeasurementSeriesDto> expected = singletonList(
      new MeasurementSeriesDto(
        "Cheese",
        "pcs",
        physicalMeterId.toString(),
        asList(
          new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0),
          new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
        )
      )
    );

    assertThat(mapper.toSeries(asList(
      LabeledMeasurementValue.from(firstMeasurement),
      LabeledMeasurementValue.from(secondMeasurement)
    ))).isEqualTo(expected);
  }

  @Test
  public void twoSeriesFromSameMeter() {
    UUID physicalMeterId = UUID.randomUUID();
    Measurement firstMeasurement = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondMeasurement = newMeasurement(physicalMeterId, new Quantity("Milk", "l"));

    assertThat(mapper.toSeries(asList(
      LabeledMeasurementValue.from(firstMeasurement),
      LabeledMeasurementValue.from(secondMeasurement)
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          "Cheese",
          "pcs",
          physicalMeterId.toString(),
          singletonList(
            new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          "Milk",
          "l",
          physicalMeterId.toString(),
          singletonList(
            new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
          )
        )
      )
    );
  }

  @Test
  public void sameQuantityDifferentMeters() {
    UUID firstPhysicalMeterId = UUID.randomUUID();
    UUID secondPhysicalMeterId = UUID.randomUUID();
    Measurement firstMeasurement = newMeasurement(
      firstPhysicalMeterId,
      new Quantity("Cheese", "pcs")
    );
    Measurement secondMeasurement = newMeasurement(
      secondPhysicalMeterId,
      new Quantity("Cheese", "pcs")
    );

    assertThat(mapper.toSeries(asList(
      LabeledMeasurementValue.from(firstMeasurement),
      LabeledMeasurementValue.from(secondMeasurement)
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          "Cheese",
          "pcs",
          firstPhysicalMeterId.toString(),
          singletonList(
            new MeasurementValueDto(firstMeasurement.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          "Cheese",
          "pcs",
          secondPhysicalMeterId.toString(),
          singletonList(
            new MeasurementValueDto(secondMeasurement.created.toInstant(), 3.0)
          )
        )
      ));
  }

  @Test
  public void differentlySizedSeries() {
    UUID physicalMeterId = UUID.randomUUID();
    Measurement firstCheese = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement secondCheese = newMeasurement(physicalMeterId, new Quantity("Cheese", "pcs"));
    Measurement firstMilk = newMeasurement(physicalMeterId, new Quantity("Milk", "l"));

    assertThat(mapper.toSeries(asList(
      LabeledMeasurementValue.from(firstCheese),
      LabeledMeasurementValue.from(secondCheese),
      LabeledMeasurementValue.from(firstMilk)
    ))).isEqualTo(
      asList(
        new MeasurementSeriesDto(
          "Cheese",
          "pcs",
          physicalMeterId.toString(),
          asList(
            new MeasurementValueDto(firstCheese.created.toInstant(), 3.0),
            new MeasurementValueDto(secondCheese.created.toInstant(), 3.0)
          )
        ),
        new MeasurementSeriesDto(
          "Milk",
          "l",
          physicalMeterId.toString(),
          singletonList(
            new MeasurementValueDto(firstMilk.created.toInstant(), 3.0)
          )
        )
      ));
  }

  private Measurement newMeasurement(
    UUID physicalMeterId,
    Quantity quantity
  ) {
    return new Measurement(
      quantity,
      3.0,
      newPhysicalMeter(physicalMeterId)
    );
  }

  private PhysicalMeter newPhysicalMeter(UUID physicalMeterId) {
    return new PhysicalMeter(
      physicalMeterId,
      new Organisation(
        UUID.randomUUID(),
        "An organisation",
        "an-organisation"
      ),
      "1234",
      physicalMeterId.toString(),
      "a medium",
      "a manufacturer",
      0
    );
  }
}
