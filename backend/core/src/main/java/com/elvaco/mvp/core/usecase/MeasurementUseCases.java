package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.Measurements;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeasurementUseCases {

  private final Measurements measurements;

  public void createOrUpdate(Measurement m) {
    measurements.createOrUpdate(
      m.physicalMeter,
      m.created,
      m.quantity,
      m.unit,
      m.value
    );
  }

  public Page<Measurement> findAllBy(UUID organisationId, UUID logicalMeterId, Pageable pageable) {
    return measurements.findAllBy(
      organisationId,
      logicalMeterId,
      pageable
    );
  }

  public List<MeasurementValue> averageForPeriod(
    List<UUID> meterIds,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurements.findAverageForPeriod(meterIds, seriesQuantity, from, to, resolution);
  }

  public List<MeasurementValue> seriesForPeriod(
    UUID meterId,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return measurements.findSeriesForPeriod(meterId, seriesQuantity, from, to, resolution);
  }
}
