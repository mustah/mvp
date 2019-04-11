package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementJpaRepository extends JpaRepository<MeasurementEntity, MeasurementPk> {

  @Query(nativeQuery = true, value = "SELECT"
    + "     measurement.organisation_id,"
    + "     measurement.readout_time,"
    + "     measurement.received_time,"
    + "     measurement.expected_time,"
    + "     measurement.value,"
    + "     measurement.quantity_id,"
    + "     physical_meter_id"
    + " FROM measurement"
    + " WHERE physical_meter_id = :physical_meter_id"
    + " AND readout_time > :from"
    + " AND readout_time <= :to"
    + " ORDER BY readout_time ASC"
    + " LIMIT 1"
  )
  Optional<MeasurementEntity> firstForPhysicalMeter(
    @Param("physical_meter_id") UUID logicalMeterId,
    @Param("from") ZonedDateTime after,
    @Param("to") ZonedDateTime beforeOrEquals
  );
}
