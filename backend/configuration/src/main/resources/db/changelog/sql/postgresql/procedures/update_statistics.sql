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


  --TODO remove the rest

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
         left join display_quantity on display_quantity.quantity_id = rec.quantity_id and
                                       meter_definition.id = display_quantity.meter_definition_id
  where p.id = rec.physical_meter_id;

  -- If there are no logical meter we can not get correct TZ an we will bail out
  if (measurement_tz is null)
  then
    return null;
  end if;
insert into measurement_stat_job(organisation_id,
                                          stat_date,
                                          physical_meter_id,
                                          quantity_id,
                                          read_interval_minutes,
                                          posix_offset,
                                          is_consumption,
                                          modified,
                                          shard_key)
                                          values
                                          ( rec.organisation_id,
                                          (rec.readout_time at time zone measurement_tz)::date,
                                          rec.physical_meter_id,
                                          rec.quantity_id,
                                          read_interval,
                                          measurement_tz,
                                          COALESCE (consumption,false),
                                          now(),
                                          ('x' || substring(rec.physical_meter_id::text from 1 for 1)
                                          )::bit(4)::int
                                          )
                                      ON CONFLICT (organisation_id,physical_meter_id, stat_date, quantity_id)
      DO UPDATE SET modified = now();

  return null;
end;
$BODY$
  language plpgsql;
