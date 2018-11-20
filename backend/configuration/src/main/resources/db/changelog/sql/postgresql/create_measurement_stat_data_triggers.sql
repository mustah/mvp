create or replace function update_statistics()
  returns trigger as
$BODY$
declare
  current_stat     measurement_stat_data%rowtype;
  measurement_tz   text;
  measurement_date date;
  do_update        boolean := true;
  read_interval int;
  rec             measurement%rowtype;
begin
  if(TG_OP = 'DELETE') then
    rec := OLD;
  else
    rec := NEW;
  end if;
  -- Get measurement date, as meter's local time, because that's how we want to aggregate
  -- Note: Postgres uses posix style for timezone and we use ISO8601 style this is why we negate
  -- the tz using an interval.
  select (-(utc_offset || ':00:00')::interval)::text,
         read_interval_minutes
         into measurement_tz,
                read_interval
         from logical_meter l
           left join physical_meter p
             on l.id=p.logical_meter_id
          where p.id=rec.physical_meter_id;

  -- If there are no logical meter we can not get correct TZ an we will bail out
  if(measurement_tz is null) then
    return null;
  end if;

  measurement_date := (rec.created at time zone measurement_tz)::date;
  select * into current_stat
  from measurement_stat_data
  where measurement_stat_data.physical_meter_id = rec.physical_meter_id and
        measurement_stat_data.stat_date = measurement_date and
        measurement_stat_data.quantity = rec.quantity;


  if current_stat.stat_date is null
  then
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

  if (TG_OP = 'INSERT')
  then
    current_stat.min := least(current_stat.min, rec.value);
    current_stat.max := greatest(current_stat.max, rec.value);
    current_stat.average =
    (current_stat.average * current_stat.received_count + rec.value) / (current_stat.received_count +
    1);
    current_stat.received_count := current_stat.received_count + 1;
  elsif (TG_OP = 'UPDATE')
  then
    current_stat := calculate_statistics(rec.quantity, rec.physical_meter_id, measurement_date, measurement_tz, read_interval);
  elsif (TG_OP = 'DELETE')
  then
    current_stat := calculate_statistics(rec.quantity, rec.physical_meter_id, measurement_date, measurement_tz, read_interval);
    if (current_stat.stat_date is null) then
      delete from measurement_stat_data where physical_meter_id=rec.physical_meter_id and
                                              quantity = rec.quantity and
                                              stat_date = measurement_date;
      return null;
    else
      do_update = true;
    end if;

  end if;

  if (do_update)
  then
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
  return null;
end;
$BODY$
language plpgsql;

drop trigger if exists update_statistics_trigger on measurement;
create trigger update_statistics_trigger
  after insert or update or delete
  on measurement
  for each row execute procedure update_statistics();


create or replace function calculate_statistics(quantity_id int,
                                                p_meter_id uuid,
                                                stat_date date,
                                                current_tz text,
                                                read_interval int) returns setof measurement_stat_data as
$BODY$
begin
  return query (select stat_date,
                       m.physical_meter_id,
                       quantity,
                       min(value),
                       max(value),
                       coalesce(60 * 24 / nullif(read_interval, 0), 0),
                       count(*)::int,
                       avg(value)
                from measurement m
                where m.physical_meter_id = p_meter_id and
                      m.quantity=quantity_id and
                      m.created >= stat_date::timestamp
                                    at time zone current_tz and
                      m.created < (stat_date::timestamp + interval '1 day')
                                   at time zone current_tz
                group by stat_date, physical_meter_id, m.quantity);
end;
$BODY$
language plpgsql;
