create or replace function update_statistics()
  returns trigger as
$BODY$
declare
  current_stat        measurement_stat_data%rowtype;
  measurement_tz      text;
  measurement_date    date;
  measurement_date_to date;
  do_update           boolean := true;
  read_interval       int;
  rec                 measurement%rowtype;
  consumption         boolean := false;
begin
  if(TG_OP = 'DELETE') then
    rec := OLD;
  else
    rec := NEW;
  end if;
  -- Get measurement date, as meter's local time, because that's how we want to aggregate
  -- Note: Postgres uses posix style for timezone and we use ISO8601 style this is why we negate
  -- the tz using an interval.

  select (-(utc_offset || right('00:00:00',9-length(utc_offset)))::interval)::text,
         read_interval_minutes
         into measurement_tz,
              read_interval
         from logical_meter l
           left join physical_meter p
           on l.id=p.logical_meter_id
         where p.id=rec.physical_meter_id;

  select (q.series_display_mode = 2) into consumption
    from quantity q
    where q.id = rec.quantity;


  -- If there are no logical meter we can not get correct TZ an we will bail out
  if(measurement_tz is null) then
    return null;
  end if;

  if(consumption) then
    measurement_date := ((rec.created - cast((case when read_interval=0 then 60 else read_interval end)||' minutes' AS INTERVAL)) at time zone measurement_tz)::date;
  else
    measurement_date := (rec.created at time zone measurement_tz)::date;
  end if;
  measurement_date_to := measurement_date;


  if (TG_OP = 'INSERT')then
    if( not consumption) then
      select * into current_stat
        from measurement_stat_data
          where measurement_stat_data.physical_meter_id = rec.physical_meter_id and
                              measurement_stat_data.stat_date = measurement_date and
                              measurement_stat_data.quantity = rec.quantity;


      if current_stat.stat_date is null then
      -- No stats for this meter/date, create it
        current_stat := (select (measurement_date,
            rec.physical_meter_id,
            rec.quantity,
            rec.value,
            -- min
            rec.value,
            -- max
            coalesce(60 * 24 / nullif(read_interval, 0), 0),
            --expected_count
            0,
            -- actual_count
            0
            -- average
             ));
        do_update := false;
      end if;
      current_stat.min := least(current_stat.min, rec.value);
      current_stat.max := greatest(current_stat.max, rec.value);
      current_stat.average =
      (current_stat.average * current_stat.received_count + rec.value) / (current_stat.received_count +
        1);
      current_stat.received_count := current_stat.received_count + 1;
      if (do_update) then
         update measurement_stat_data set min = current_stat.min,
                                     max = current_stat.max,
                                     average = current_stat.average,
                                     expected_count = current_stat.expected_count,
                                     received_count = current_stat.received_count
            where stat_date = measurement_date and
              quantity = rec.quantity and
              physical_meter_id = rec.physical_meter_id;

      else
         current_stat.stat_date := measurement_date;
         --skip if there are no rows.
         if(current_stat.received_count>0) then
           insert into measurement_stat_data(stat_date,
                                 physical_meter_id,
                                 quantity,
                                 min,
                                 max,
                                 average,
                                 expected_count,
                                 received_count)
             values (current_stat.stat_date,
               current_stat.physical_meter_id,
               current_stat.quantity,
               current_stat.min,
               current_stat.max,
               current_stat.average,
               current_stat.expected_count,
               current_stat.received_count);
         end if;
       end if;
       return null;
     end if;
  end if;
    --expand date-range for consumption and missing measurements situations
    if( consumption) then
      select coalesce((min(created at time zone measurement_tz)::date),measurement_date_to)
               into measurement_date_to
         from measurement
         where physical_meter_id=rec.physical_meter_id
           and quantity=rec.quantity
           and created > rec.created;
    end if;
    perform calculate_and_write_statistics(rec.quantity, rec.physical_meter_id, measurement_date, measurement_date_to, measurement_tz, read_interval,consumption);

  return null;
end;
$BODY$
language plpgsql;
