DELETE
FROM meter_alarm_log
WHERE id IN
      (SELECT id
       FROM (SELECT id, ROW_NUMBER()
         OVER(PARTITION BY physical_meter_id, mask ORDER BY id DESC) AS row_num
             FROM meter_alarm_log) t
       WHERE t.row_num > 1);
