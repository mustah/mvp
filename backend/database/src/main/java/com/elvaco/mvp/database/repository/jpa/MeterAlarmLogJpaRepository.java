package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeterAlarmLogJpaRepository extends JpaRepository<MeterAlarmLogEntity, Long> {

  String UPDATE_START = "CASE WHEN EXCLUDED.start < meter_alarm_log.start THEN "
    + ":timestamp ELSE meter_alarm_log.start END";

  String UPDATE_LAST_SEEN = "CASE WHEN EXCLUDED.last_seen > meter_alarm_log.last_seen THEN"
    + " :timestamp ELSE meter_alarm_log.last_seen END";

  @Modifying
  @Query(nativeQuery = true, value =
    "INSERT INTO meter_alarm_log (physical_meter_id, mask, start, last_seen, description)"
      + " VALUES "
      + "(:physical_meter_id, :mask, :timestamp, :timestamp, :description)"
      + " ON CONFLICT (physical_meter_id, mask)"
      + "  DO UPDATE SET"
      + "    START = " + UPDATE_START + ", "
      + "    last_seen = " + UPDATE_LAST_SEEN + ", "
      + "    description = :description"
      + "  WHERE meter_alarm_log.stop IS NULL"
  )
  void createOrUpdate(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("mask") int mask,
    @Param("timestamp") ZonedDateTime timestamp,
    @Param("description") String description
  );

  @Query("SELECT a FROM MeterAlarmLogEntity a WHERE a.stop IS NULL AND a.start <= :timestamp ")
  List<MeterAlarmLogEntity> findActiveAlamsOlderThan(@Param("timestamp")ZonedDateTime timestamp);

}
