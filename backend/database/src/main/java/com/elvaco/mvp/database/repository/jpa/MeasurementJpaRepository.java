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
    + "     case when :mode = 'consumption'"
    + "     then"
    + "       value - lag(value) over (partition by physical_meter_id order by created)"
    + "     else value"
    + "     end as value,"
    + "     date_trunc(:resolution, created AT TIME ZONE 'UTC') AT TIME ZONE 'UTC' AS "
    + "interval_start FROM "
    + "measurement WHERE"
    + "     physical_meter_id IN :meter_ids"
    + "     AND quantity = :quantity"
    + "     AND created >= cast(:from AS TIMESTAMPTZ) - cast('1 ' || :resolution as interval)"
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
    @Param("mode") String mode,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  Optional<MeasurementEntity> findByPhysicalMeterIdAndQuantityAndCreated(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  );

  @Query(nativeQuery = true, value = "select"
    + "  cast (unit_at("
    + "    case when :mode = 'consumption'"
    + "    then"
    + "      value - coalesce("
    // If we have a previous value, diff against that ...
    + "        lag(value) over (order by created),"
    // ... Otherwise, pick the latest value in the series _before_ this period, and diff against
    // _that_, to avoid surprising null consumptions at the beginning of a period
    + "        (select value from measurement"
    + "          where quantity = :quantity and"
    + "          physical_meter_id = :meter_id and"
    + "          created < cast(:from AS TIMESTAMPTZ)"
    + "          order by created desc limit 1))"
    + "    else value"
    + "    end, :unit) as TEXT) as value,"
    + "  created as when"
    + "  from measurement"
    + "  where created >= cast(:from AS TIMESTAMPTZ)"
    + "  and created <= cast(:to AS TIMESTAMPTZ)"
    + "  and created in ("
    + "   SELECT generate_series("
    + "     date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS interval_start"
    + "  )"
    + "  and physical_meter_id = :meter_id"
    + "  and quantity = :quantity"
    + "  ORDER BY created ASC"
  )
  List<MeasurementValueProjection> getSeriesForPeriod(
    @Param("meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("mode") String mode,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution
  );

  @Query(nativeQuery = true, value = "select "
    + " id, physical_meter_id, created, quantity,"
    + " unit_at(value, :unit) as value"
    + " from measurement"
    + " where physical_meter_id = :meter_id"
    + " and quantity = :quantity"
    + " order by created desc"
    + " limit 1")
  Optional<MeasurementEntity> findLatestReadout(
    @Param("meter_id") UUID meterId,
    @Param("quantity") String quantity,
    @Param("unit") String unit
  );
}
