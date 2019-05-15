package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;

public interface MeasurementJpaRepositoryCustom {

  @Modifying
  void createOrUpdate(
    UUID organisationId,
    UUID physicalMeterId,
    ZonedDateTime readoutTime,
    ZonedDateTime receivedTime,
    ZonedDateTime expectedTime,
    Integer quantity,
    double value
  );
}
