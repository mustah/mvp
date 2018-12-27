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
    + "   avg(consumption) AS value,"
    + "   interval_start AS when"
    + " FROM"
    + "   (SELECT"
    + "      lead(value) over (PARTITION BY physical_meter_id order by created ASC) "
    + "            - value as consumption,"
    + "      date_serie.date as interval_start"
    + "    FROM"
    + "      (SELECT generate_series("
    + "                cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "                cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC' + cast(:resolution AS INTERVAL),"
    + "                cast(:resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS date) as date_serie"
    + "       LEFT JOIN measurement"
    + "          ON physical_meter_id IN :physical_meter_ids"
    + "         AND date_serie.date = created"
    + "         AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"

    + "   ) as consumption_values"
    + " WHERE"
    + "   interval_start <= cast(:to AS TIMESTAMPTZ)"
    + " GROUP BY interval_start"
    + " ORDER BY interval_start"
  )
  List<MeasurementValueProjection> getAverageForPeriodConsumption(
    @Param("physical_meter_ids") List<UUID> physicalMeterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  @Query(nativeQuery = true, value = ""
    + " SELECT"
    + "   avg(value) as value,"
    + "   date_serie.date as when"
    + " FROM"
    + "     (SELECT generate_series("
    + "                  cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "                  cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "                  cast(:resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
    + "     ) AS date_serie"
    + "     LEFT JOIN measurement"
    + "            ON physical_meter_id IN :physical_meter_ids"
    + "           AND date_serie.date = created"
    + "           AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
    + " GROUP BY date_serie.date"
    + " ORDER BY date_serie.date")
  List<MeasurementValueProjection> getAverageForPeriod(
    @Param("physical_meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("quantity") String quantity,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to
  );

  @Query(nativeQuery = true, value = ""
    + "   SELECT measurement_serie.value, measurement_serie.when from "
    + "   (SELECT"
    + "     lead(value) over (ORDER BY created ASC) - value AS value,"
    + "     date_serie.date AS when"
    + "   FROM"
    + "     ("
    + "       SELECT"
    //          Series of all expected measurements from startTime up to (stopTime + 1) intervals.
    //          Consider stopTime as inclusive, hence +1 to the stopTime
    + "         generate_series("
    + "           cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "           cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC' +"
    + "             cast(:resolution AS INTERVAL),"
    + "           cast(:resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
    + "     ) AS date_serie"
    + "     LEFT JOIN measurement"
    + "        ON physical_meter_id = :physical_meter_id"
    + "       AND date_serie.date = created"
    + "       AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"

    + "   ) as measurement_serie"
    + " WHERE"
    + "   measurement_serie.when >= CAST(:from AS TIMESTAMPTZ)"
    + "   AND measurement_serie.when <= CAST(:to AS TIMESTAMPTZ)"
    + " ORDER BY"
    + "   measurement_serie.when ASC;"
  )
  List<MeasurementValueProjection> getSeriesForPeriodConsumption(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution
  );

  @Query(nativeQuery = true, value = ""
    + " SELECT"
    + "   value,"
    + "   date_serie.date as when"
    + " FROM"
    + "   (SELECT generate_series("
    + "                  cast(:from AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "                  cast(:to AS TIMESTAMPTZ) AT TIME ZONE 'UTC',"
    + "                  cast(:resolution AS INTERVAL)) AT TIME ZONE 'UTC' AS DATE"
    + "   ) AS date_serie"
    + "   LEFT JOIN measurement"
    + "          ON physical_meter_id = :physical_meter_id"
    + "         AND date_serie.date = created"
    + "         AND quantity = (SELECT id FROM quantity WHERE quantity.name = :quantity)"
    + " ORDER BY date_serie.date ASC"
  )
  List<MeasurementValueProjection> getSeriesForPeriod(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("quantity") String quantity,
    @Param("from") OffsetDateTime from,
    @Param("to") OffsetDateTime to,
    @Param("resolution") String resolution
  );

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
