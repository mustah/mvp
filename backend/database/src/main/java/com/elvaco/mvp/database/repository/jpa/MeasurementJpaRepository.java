package com.elvaco.mvp.database.repository.jpa;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementJpaRepository extends JpaRepository<MeasurementEntity, Long>,
  MeasurementJpaRepositoryCustom {

  @Query(nativeQuery = true, value = "SELECT "
    + "cast(unit_at(avg(value), :unit) AS TEXT) AS value,"
    + "interval_start AS when"
    + " FROM ("
    + "   SELECT generate_series("
    + "     date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS interval_start"
    + " ) x"
    + " LEFT JOIN ("
    + "   SELECT"
    + "     value,"
    + "     date_trunc(:resolution, created AT TIME ZONE 'UTC') AT TIME ZONE 'UTC' AS "
    + "interval_start FROM "
    + "measurement WHERE"
    + "     physical_meter_id IN :meter_ids"
    + "     AND quantity = :quantity"
    + "     AND created >= cast(:from AS TIMESTAMPTZ)"
    + "     AND created <= cast(:to AS TIMESTAMPTZ)"
    + ") y"
    + "   USING (interval_start)"
    + " GROUP BY interval_start"
    + " ORDER BY interval_start")
  List<MeasurementValueProjection> getAverageForPeriod(
    @Param("meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  Optional<MeasurementEntity> findByPhysicalMeterIdAndQuantityAndCreated(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  );

  @Query(nativeQuery = true, value = "SELECT m1.*"
    + " FROM measurement m1"
    + " INNER JOIN ("
    + "     SELECT"
    + "       MAX(created) as latest,"
    + "       quantity,"
    + "       physical_meter_id"
    + "     FROM measurement"
    + "     WHERE physical_meter_id = :physical_meter_id"
    + "     GROUP by physical_meter_id, quantity"
    + " ) m2"
    + " ON m1.created = latest"
    + " AND m1.physical_meter_id = m2.physical_meter_id"
    + " AND m1.quantity = m2.quantity")
  List<MeasurementEntity> findLatestForPhysicalMeter(@Param("physical_meter_id") UUID id);

}
