delete from physical_meter_status_log
where id in
      (select id
       from (select
               id,
               ROW_NUMBER()
               over (
                 partition by physical_meter_id, start, status
                 order by id ) as rownum
             from physical_meter_status_log) t
       where t.rownum > 1);

delete from gateway_status_log
where id in
      (select id
       from (select
               id,
               ROW_NUMBER()
               over (
                 partition by gateway_id, start, status
                 order by id ) as rownum
             from gateway_status_log) t
       where t.rownum > 1);
