package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeterAlarmLogJpaRepository extends JpaRepository<MeterAlarmLogEntity, Long> {

  @Modifying
  @Query(nativeQuery = true, value =
    "INSERT INTO meter_alarm_log (physical_meter_id, mask, start, last_seen, description)"
      + " VALUES "
      + "(:physical_meter_id, :mask, :start, :last_seen, :description)"
      + " ON CONFLICT (physical_meter_id, mask, start)"
      + "  DO UPDATE SET"
      + "    physical_meter_id = :physical_meter_id,"
      + "    mask = :mask,"
      + "    start = :start,"
      + "    last_seen = :last_seen,"
      + "    description = :description"
  )
  void createOrUpdate(
    @Param("physical_meter_id") UUID physicalMeterId,
    @Param("mask") int mask,
    @Param("start") ZonedDateTime start,
    @Param("last_seen") ZonedDateTime lastSeen,
    @Param("description") String description
  );
}
