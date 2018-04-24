delete from measurement
where id in
      (select id
       from (select
               id,
               ROW_NUMBER()
               over (
                 partition by physical_meter_id, created, quantity
                 order by id ) as rownum
             from measurement) t
       where t.rownum > 1);
