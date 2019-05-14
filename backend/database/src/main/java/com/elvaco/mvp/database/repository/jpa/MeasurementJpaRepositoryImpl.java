package com.elvaco.mvp.database.repository.jpa;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.tables.Measurement.MEASUREMENT;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseJooqRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  private final DSLContext dsl;

  @Autowired
  MeasurementJpaRepositoryImpl(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, MeasurementEntity.class);
    this.dsl = dsl;
  }

  @Modifying
  @Override
  public void createOrUpdate(
    UUID organisationId,
    UUID physicalMeterId,
    ZonedDateTime readoutTime,
    ZonedDateTime receivedTime,
    ZonedDateTime expectedTime,
    Integer quantity,
    double value
  ) {
    OffsetDateTime received = receivedTime != null ? receivedTime.toOffsetDateTime() : null;
    OffsetDateTime expected = expectedTime != null ? expectedTime.toOffsetDateTime() : null;
    var query = dsl.insertInto(MEASUREMENT).columns(
      MEASUREMENT.ORGANISATION_ID,
      MEASUREMENT.PHYSICAL_METER_ID,
      MEASUREMENT.READOUT_TIME,
      MEASUREMENT.RECEIVED_TIME,
      MEASUREMENT.EXPECTED_TIME,
      MEASUREMENT.QUANTITY_ID,
      MEASUREMENT.VALUE
    ).values(
      DSL.val(organisationId),
      DSL.val(physicalMeterId, MEASUREMENT.PHYSICAL_METER_ID),
      DSL.val(readoutTime.toOffsetDateTime(), MEASUREMENT.READOUT_TIME),
      received == null ? DSL.castNull(MEASUREMENT.RECEIVED_TIME) :
        DSL.val(received, MEASUREMENT.RECEIVED_TIME),
      expected == null ? DSL.castNull(MEASUREMENT.EXPECTED_TIME) :
        DSL.val(expected, MEASUREMENT.EXPECTED_TIME),
      DSL.val(quantity, Integer.class),
      DSL.val(value, Double.class)
    )
      .onConflict(
        MEASUREMENT.ORGANISATION_ID,
        MEASUREMENT.PHYSICAL_METER_ID,
        MEASUREMENT.READOUT_TIME,
        MEASUREMENT.QUANTITY_ID
      )
      .doUpdate()
      .set(MEASUREMENT.PHYSICAL_METER_ID, physicalMeterId)
      .set(MEASUREMENT.READOUT_TIME, readoutTime.toOffsetDateTime())
      .set(MEASUREMENT.RECEIVED_TIME, received == null ? DSL.castNull(MEASUREMENT.RECEIVED_TIME) :
        DSL.val(received, MEASUREMENT.RECEIVED_TIME))
      .set(MEASUREMENT.EXPECTED_TIME, expected == null ? DSL.castNull(MEASUREMENT.EXPECTED_TIME) :
        DSL.val(expected, MEASUREMENT.EXPECTED_TIME))
      .set(MEASUREMENT.QUANTITY_ID, quantity)
      .set(MEASUREMENT.VALUE, value);

    executeUpdate(query);
  }
}
