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

  @Query(nativeQuery = true, value = ""
    + " SELECT"
    + "   cast(unit_at(avg(consumption), :unit) AS TEXT) AS value,"
    + "   interval_start AS when"
    + " FROM"
    + "   (SELECT"
    + "      lead(value) over (PARTITION BY physical_meter_id order by created ASC) "
    + "            - value as consumption,"
    + "      serie.date as interval_start"
    + "    FROM"
    + "      (SELECT generate_series("
    + "                 date_trunc(:resolution,"
    + "                            cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "                 date_trunc(:resolution,"
    + "                            cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC') + "
    + "                            cast('1 ' || :resolution AS INTERVAL),"
    + "                 cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS date) as serie"
    + "       LEFT JOIN measurement"
    + "         on serie.date = created"
    + "         AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
    + "         AND physical_meter_id IN :meter_ids"
    + "   ) as consumption_values"
    + " WHERE"
    + "   interval_start <= cast(:to AS TIMESTAMPTZ)"
    + " GROUP BY interval_start"
    + " ORDER BY interval_start"
  )
  List<MeasurementValueProjection> getAverageForPeriodConsumption(
    @Param("meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  @Query(nativeQuery = true, value = ""
    + " SELECT"
    + "   cast(unit_at(avg(value), :unit) as TEXT) as value,"
    + "   date_serie.date as when"
    + " FROM"
    + "     (SELECT generate_series("
    + "                  date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "                  date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "                  cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
    + "     ) AS date_serie"
    + "     LEFT JOIN measurement"
    + "            ON date_serie.date = created"
    + "           AND physical_meter_id IN :meter_ids"
    + "           AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
    + " GROUP BY date_serie.date"
    + " ORDER BY date_serie.date")
  List<MeasurementValueProjection> getAverageForPeriod(
    @Param("meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  @Query(nativeQuery = true, value =
    "     SELECT measurement_serie.value, measurement_serie.when from "
      + "   (SELECT"
      + "     CAST (unit_at( lead(value) over (ORDER BY created ASC) - value , :unit) AS TEXT)"
      + "     AS value,"
      + "     date_serie.date AS when"
      + "   FROM"
      + "     ("
      + "       SELECT"
      //          Series of all expected measurements from startTime up to (stopTime + 1) intervals.
      //          Consider stopTime as inclusive, hence +1 to the stopTime
      + "         generate_series("
      + "           date_trunc(:resolution,"
      + "                      CAST(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
      + "           date_trunc(:resolution,"
      + "                      (CAST(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC') + "
      + "                       CAST('1 ' || :resolution AS INTERVAL)),"
      + "           CAST('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
      + "     ) AS date_serie"
      + "     LEFT JOIN measurement"
      + "     ON"
      + "       date_serie.date = created"
      + "       AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
      + "       AND physical_meter_id = :meter_id"
      + "   ) as measurement_serie"
      + " WHERE"
      + "   measurement_serie.when >= CAST(:from AS TIMESTAMPTZ)"
      + "   AND measurement_serie.when <= CAST(:to AS TIMESTAMPTZ)"
      + " ORDER BY"
      + "   measurement_serie.when ASC;"
  )
  List<MeasurementValueProjection> getSeriesForPeriodConsumption(
    @Param("meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("unit") String unit,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution
  );

  @Query(nativeQuery = true, value = ""
    + " SELECT"
    + "   cast(unit_at(value, :unit) as TEXT) as value,"
    + "   date_serie.date as when"
    + " FROM"
    + "   (SELECT generate_series("
    + "                  date_trunc(:resolution, cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "                  date_trunc(:resolution, cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC'),"
    + "                  cast('1 ' || :resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
    + "   ) AS date_serie"
    + "   LEFT JOIN measurement"
    + "          ON date_serie.date = created"
    + "         AND physical_meter_id = :meter_id"
    + "         AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
    + " ORDER BY date_serie.date ASC"
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
