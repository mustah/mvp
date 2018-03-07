package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeasurementJpaRepository extends JpaRepository<MeasurementEntity, Long> {

  @Query(nativeQuery = true, value = "SELECT "
    + "avg(value) as value,"
    + "interval_start as when"
    + " FROM ("
    + "   SELECT generate_series(date_trunc(:resolution, cast(:from as timestamptz)),"
    + "                          date_trunc(:resolution, cast(:to as timestamptz)),"
    + "                          cast('1 ' || :resolution as interval)) as interval_start) x"
    + " LEFT JOIN ("
    + "   SELECT value(value), date_trunc(:resolution, created) as interval_start from "
    + "measurement where physical_meter_id in :meter_ids and created >= :from and created <= :to) y"
    + "   using (interval_start)"
    + " GROUP BY interval_start"
    + " ORDER BY interval_start")
  List<MeasurementValueProjection> getAverageForPeriod(
    @Param("meter_ids") List<UUID> meterIds,
    @Param("resolution") String resolution,
    @Param("from") ZonedDateTime from,
    @Param("to") ZonedDateTime to
  );


  @Query(nativeQuery = true, value = "SELECT "
    + "avg(value) as value,"
    + "interval_start as when"
    + " FROM (SELECT value(value) as value, date_trunc('hour', created)  + cast(?2 || ' min' as "
    + "interval) "
    + "* ROUND(date_part('minute', created) / ?2) as interval_start"
    + " FROM measurement where physical_meter_id in ?1 and created >= ?3) x GROUP BY "
    + "interval_start, value")
  List<MeasurementValueProjection> getAverageForPeriod(
    List<UUID> meterIds,
    int intervalInMinutes,
    Date since,
    String unit
  );
}
