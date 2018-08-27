DELETE
FROM physical_meter_status_log USING (SELECT physical_meter_id, min(start) AS start
                                      FROM physical_meter_status_log
                                      WHERE status = 'OK' AND stop IS NULL
                                      GROUP BY physical_meter_id
                                      HAVING count(*) > 1) AS meters
WHERE meters.physical_meter_id = physical_meter_status_log.physical_meter_id AND
        physical_meter_status_log.start > meters.start AND physical_meter_status_log.status = 'OK';
