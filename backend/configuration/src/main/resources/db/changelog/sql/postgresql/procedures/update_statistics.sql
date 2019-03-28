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
  if (TG_OP = 'DELETE')
  then
    rec := OLD;
  else
    rec := NEW;
  end if;
  -- Get measurement date, as meter's local time, because that's how we want to aggregate
  -- Note: Postgres uses posix style for timezone and we use ISO8601 style this is why we negate
  -- the tz using an interval.

  select (-(utc_offset || right('00:00:00', 9 - length(utc_offset)))::interval)::text,
         read_interval_minutes,
         display_quantity.display_mode = 2
         into measurement_tz,
           read_interval,
           consumption
  from logical_meter l
         inner join physical_meter p
                   on l.id = p.logical_meter_id
         left join meter_definition on meter_definition.id = l.meter_definition_id
         left join display_quantity on display_quantity.quantity_id = rec.quantity and
                                       meter_definition.id = display_quantity.meter_definition_id
  where p.id = rec.physical_meter_id;

  -- If there are no logical meter we can not get correct TZ an we will bail out
  if (measurement_tz is null)
  then
    return null;
  end if;

  measurement_date := (rec.created at time zone measurement_tz)::date;
  measurement_date_to := measurement_date;
  perform
  calculate_and_write_statistics(rec.quantity, rec.physical_meter_id, measurement_date,
                                 measurement_date_to, measurement_tz, read_interval, false);

  if (consumption)
  then
    --expand date-range for consumption and missing measurements situations
    measurement_date := ((rec.created - cast(
        (case when read_interval = 0 then 60 else read_interval end) ||
        ' minutes' as interval)) at time zone measurement_tz)::date;
    measurement_date_to := measurement_date;
    select coalesce((min(created at time zone measurement_tz)::date),
                    measurement_date_to) into measurement_date_to
    from measurement
    where physical_meter_id = rec.physical_meter_id and
          quantity = rec.quantity and
          created > rec.created;
    perform
    calculate_and_write_statistics(rec.quantity, rec.physical_meter_id, measurement_date,
                                   measurement_date_to, measurement_tz, read_interval, true);
  end if;

  return null;
end;
$BODY$
  language plpgsql;
