package com.elvaco.mvp.database.repository.jpa;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementJpaRepository
  extends MeasurementJpaRepositoryCustom, JpaRepository<MeasurementEntity, MeasurementPk> {

  @Query(nativeQuery = true, value =
    "SELECT "
      + "  cast(unit_at(avg(value), :unit) AS TEXT) AS value, "
      + "  interval_start AS when "
      + "FROM ( "
      //Generate series with expected timestamps within the period
      + "  SELECT generate_series( "
      + "    date_trunc(:resolution, "
      + "      (cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC' - INTERVAL '1 day')), "
      + "    date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'), "
      + "    cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS interval_start "
      + "  ) date_serie "
      + "LEFT JOIN ( "
      + "  SELECT "
      //Diff against previous value with the selected interval.
      + "        value - ("
      + "        CASE WHEN (lag(value) OVER (PARTITION BY physical_meter_id ORDER BY created )) "
      + "          IS NOT NULL OR date_serie2.date <= :from THEN "
      + "            (lag(value) OVER (PARTITION BY physical_meter_id ORDER BY created )) "
      //Fallback, use first value within last X values matching interval
      + "        ELSE "
      + "          ( "
      + "            SELECT  "
      + "              value "
      + "            FROM ( "
      + "              SELECT "
      + "                generate_series( "
      + "                  date_trunc(:resolution, "
      + "                    ((cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC') - "
      + "                    cast(:fallbackMax || :resolution AS INTERVAL))), "
      + "                  date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'), "
      + "                  cast('1 ' || :resolution AS INTERVAL) "
      + "                ) AT TIME ZONE 'UTC' AS date "
      + "            ) inner_date_serie "
      + "            INNER JOIN measurement AS measurement_inner "
      + "              ON measurement_inner.created = inner_date_serie.date "
      + "              AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)  "
      + "              AND measurement_inner.physical_meter_id = measurement_outer"
      + ".physical_meter_id "
      + "              AND inner_date_serie.date < date_serie2.date "
      + "            ORDER BY inner_date_serie.date DESC "
      + "            LIMIT 1 "
      + "          ) "
      + "          END "
      + "        ) AS value, "
      + "    date_trunc(:resolution, created AT TIME ZONE 'UTC') AT TIME ZONE 'UTC' AS "
      + "interval_start"
      + "  FROM ( "
      + "    SELECT  "
      + "      generate_series( "
      + "        date_trunc(:resolution, "
      + "          ((cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC') - "
      + "cast('1 ' || :resolution AS INTERVAL))), "
      + "        date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'), "
      + "        cast('1 ' || :resolution AS INTERVAL) "
      + "      ) AT TIME ZONE 'UTC' AS date "
      + "  ) date_serie2 "
      + "  LEFT JOIN measurement AS measurement_outer "
      + "    ON date_serie2.date = measurement_outer.created "
      + "    AND physical_meter_id IN :meter_ids "
      + "    AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity) "
      + ") AS y "
      + "USING (interval_start) "
      + "WHERE"
      + "    interval_start >= cast(:from AS TIMESTAMPTZ)"
      + "    AND interval_start <= cast(:to AS TIMESTAMPTZ) "
      + "GROUP BY interval_start "
      + "ORDER BY interval_start "
  )
  List<MeasurementValueProjection> getAverageForPeriodConsumption(
    @Param("meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("fallbackMax") int fallbackMax
  );

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
    + "     AND quantity = (select id from quantity where quantity.name=:quantity)"
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
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  @Query(nativeQuery = true, value = "select"
    + "  cast (unit_at("
    + "      value - coalesce("
    // If we have a previous value, diff against that ...
    + "        lag(value) over (order by created ASC),"
    // ... Otherwise, pick the latest value in the series _before_ this period, and diff against
    // _that_, to avoid surprising null consumptions at the beginning of a period
    + "        (SELECT value "
    + "        FROM "
    + "          (SELECT "
    + "            generate_series( "
    + "              date_trunc(:resolution, "
    + "                ((cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC') -"
    + "                cast(:fallbackMax || :resolution AS INTERVAL))), "
    + "              date_trunc(:resolution, "
    + "                ((cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC') - "
    + "                cast('1 ' || :resolution AS INTERVAL))), "
    + "              cast('1 ' || :resolution AS INTERVAL)"
    + "            ) AT TIME ZONE 'UTC' AS date "
    + "          ) inner_date_serie "
    + "        inner join measurement on measurement.created = inner_date_serie.date "
    + "          and quantity = (SELECT id "
    + "                        FROM quantity "
    + "                        WHERE quantity.name = :quantity) "
    + "          AND physical_meter_id = :meter_id"
    + "          AND inner_date_serie.date < date_serie.date "
    + "        ORDER BY inner_date_serie.date DESC "
    + "        LIMIT 1)"
    + "      )"
    + "    , :unit) as TEXT) as value,"
    + "  date_serie.date as when "
    + "FROM (SELECT generate_series( "
    + " date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'), "
    + " date_trunc(:resolution, "
    + "   ((cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC') - "
    + "   cast('1 ' || :resolution AS INTERVAL))), "
    + " cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS date) as date_serie "
    + "LEFT JOIN measurement "
    + "  on date_serie.date = created "
    + "  AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity) "
    + "  AND physical_meter_id = :meter_id "
    + "WHERE "
    + "  date_serie.date >= cast(:from AS TIMESTAMPTZ) "
    + "  AND date_serie.date < cast(:to AS TIMESTAMPTZ) "
    + "ORDER BY date_serie.date ASC;"
  )
  List<MeasurementValueProjection> getSeriesForPeriodConsumption(
    @Param("meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution,
    @Param("fallbackMax") int fallbackMax
  );

  @Query(nativeQuery = true, value = "select"
    + "  cast (unit_at(value, :unit) as TEXT) as value,"
    + "  created as when"
    + "  from measurement"
    + "  where created >= cast(:from AS TIMESTAMPTZ)"
    + "  and quantity = (select id from quantity where quantity.name=:quantity)"
    + "  and created < cast(:to AS TIMESTAMPTZ)"
    + "  and created in ("
    + "   SELECT generate_series("
    + "     date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "     cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS interval_start"
    + "  )"
    + "  and physical_meter_id = :meter_id"
    + "  ORDER BY created ASC"
  )
  List<MeasurementValueProjection> getSeriesForPeriod(
    @Param("meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution
  );

  @Query(nativeQuery = true, value = "select"
    + "     measurement.created,"
    + "     unit_at(measurement.value, quantity.unit) as value,"
    + "     measurement.quantity,"
    + "     physical_meter_id"
    + " from logical_meter"
    + " inner join physical_meter"
    + "     on logical_meter.organisation_id = physical_meter.organisation_id and logical_meter.id"
    + "  = physical_meter.logical_meter_id"
    + " inner join measurement"
    + "     on physical_meter.id = measurement.physical_meter_id"
    + " inner join quantity"
    + "     on measurement.quantity = quantity.id"
    + " where logical_meter.id = :logical_meter_id"
    + " and logical_meter.organisation_id = :organisation_id"
    + " order by created desc"
    + " limit :limit"
    + " offset :offset")
  List<MeasurementEntity> latestForMeter(
    @Param("organisation_id") UUID organisationId,
    @Param("logical_meter_id") UUID logicalMeterId,
    @Param("limit") int limit,
    @Param("offset") long offset
  );

  /**
   * The limit 100 is arbitrary, just high enough to contain a few pages, but not high enough to
   * have to check all rows.
   */
  @Query(nativeQuery = true, value = "select count(1)"
    + " from ("
    + "   select distinct measurement.created"
    + "   from logical_meter"
    + "     inner join physical_meter"
    + "       on logical_meter.organisation_id = physical_meter.organisation_id and"
    + "          logical_meter.id = physical_meter.logical_meter_id"
    + "     inner join measurement"
    + "       on physical_meter.id = measurement.physical_meter_id"
    + "     inner join quantity"
    + "       on measurement.quantity = quantity.id"
    + "   where logical_meter.id = :logical_meter_id"
    + "   and logical_meter.organisation_id = :organisation_id"
    + "   limit 100"
    + " ) unused_alias")
  long countMeasurementsForMeter(
    @Param("organisation_id") UUID organisationId,
    @Param("logical_meter_id") UUID logicalMeterId
  );

  @Modifying
  @Query(nativeQuery = true, value =
    "INSERT INTO measurement (physical_meter_id, created, quantity, value)"
      + " VALUES "
      + "(:physical_meter_id, :created, :quantity, cast(concat(:value, ' ', :unit) as unit))"
      + " ON CONFLICT (physical_meter_id, created, quantity)"
      + "  DO UPDATE SET"
      + "    physical_meter_id = :physical_meter_id,"
      + "    created = :created,"
      + "    quantity = :quantity,"
      + "    value = cast(concat(:value, ' ', :unit) as unit)"
  )
  void createOrUpdate(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("created") ZonedDateTime created,
    @Param("quantity") Integer quantity,
    @Param("unit") String unit,
    @Param("value") double value
  );

  @Query(nativeQuery = true, value = "select"
    + "     measurement.created,"
    + "     unit_at(measurement.value, quantity.unit) as value,"
    + "     measurement.quantity,"
    + "     physical_meter_id"
    + " from measurement left join quantity on measurement.quantity = quantity.id"
    + " where physical_meter_id = :physical_meter_id"
    + " and created > :from"
    + " and created <= :to"
    + " order by created asc"
    + " limit 1"
  )
  Optional<MeasurementEntity> firstForPhysicalMeter(
    @Param("physical_meter_id") UUID logicalMeterId,
    @Param("from") ZonedDateTime after,
    @Param("to") ZonedDateTime beforeOrEquals
  );
}
