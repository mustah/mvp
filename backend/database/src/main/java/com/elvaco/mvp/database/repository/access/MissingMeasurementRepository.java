package com.elvaco.mvp.database.repository.access;

import com.elvaco.mvp.core.spi.repository.MissingMeasurements;
import com.elvaco.mvp.database.repository.jpa.MissingMeasurementJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;

@RequiredArgsConstructor
@Slf4j
public class MissingMeasurementRepository implements MissingMeasurements {

  private final MissingMeasurementJpaRepository missingMeasurementJpaRepository;

  @Override
  public boolean refresh() {
    try {
      missingMeasurementJpaRepository.refresh();
    } catch (PessimisticLockingFailureException ex) {
      log.info("Unable to retrieve lock when refreshing missing measurements");
      return false;
    }
    return true;
  }
}
