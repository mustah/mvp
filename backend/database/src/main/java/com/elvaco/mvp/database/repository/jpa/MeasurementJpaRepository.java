package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementJpaRepository extends JpaRepository<MeasurementEntity, MeasurementPk> {

  @Modifying
  @Query(nativeQuery = true, value =
    "INSERT INTO measurement (physical_meter_id, created, quantity, value)"
      + " VALUES "
      + "(:physical_meter_id, :created, :quantity, :value)"
      + " ON CONFLICT (physical_meter_id, created, quantity)"
      + "  DO UPDATE SET"
      + "    physical_meter_id = :physical_meter_id,"
      + "    created = :created,"
      + "    quantity = :quantity,"
      + "    value = :value"
  )
  void createOrUpdate(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("created") ZonedDateTime created,
    @Param("quantity") Integer quantity,
    @Param("value") double value
  );

  @Query(nativeQuery = true, value = "SELECT"
    + "     measurement.created,"
    + "     measurement.value,"
    + "     measurement.quantity,"
    + "     physical_meter_id"
    + " FROM measurement"
    + " WHERE physical_meter_id = :physical_meter_id"
    + " AND created > :from"
    + " AND created <= :to"
    + " ORDER BY created ASC"
    + " LIMIT 1"
  )
  Optional<MeasurementEntity> firstForPhysicalMeter(
    @Param("physical_meter_id") UUID logicalMeterId,
    @Param("from") ZonedDateTime after,
    @Param("to") ZonedDateTime beforeOrEquals
  );
}
