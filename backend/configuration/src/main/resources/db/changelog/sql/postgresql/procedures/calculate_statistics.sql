create or replace function read_interval_interval(read_interval int)
  returns interval as
$BODY$
begin
  return ((case when read_interval = 0 then 60 else read_interval end) || ' minutes')::interval;
end;
$BODY$
  language plpgsql;

drop function if exists calculate_statistics;
create or replace function calculate_statistics(quantity_id int,
                                                p_meter_id uuid,
                                                stat_date date,
                                                stop_date date,
                                                current_tz text,
                                                read_interval int,
                                                consumption boolean) returns
  table (
    stat_d date,
    physical_meter_id uuid,
    quantity int,
    min double precision,
    max double precision,
    expected_count int,
    received_count int,
    average double precision,
    is_consumption boolean
  ) as
$BODY$
begin
  if (not consumption)
  then
    return query (
      select gen.d2,
             p_meter_id,
             quantity_id,
             mg.min,
             mg.max,
             mg.expected_count,
             mg.received_count,
             mg.average,
             false
      from (select cast(generate_series(stat_date::timestamp at time zone current_tz,
                                        stop_date::timestamp at time zone current_tz,
                                        '1 day'::interval) at time zone
                        current_tz as date) as d2) as gen
             left join
           (select (m.created at time zone current_tz)::date as date1,
                   min(value),
                   max(value),
                   coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected_count,
                   count(value)::int as received_count,
                   avg(value) as average
            from generate_series(stat_date::timestamp at time zone current_tz,
                                 (stop_date::timestamp + interval '1 day') at time zone current_tz,
                                 read_interval_interval(read_interval)) as date_serie
                   join measurement m on (m.created = date_serie)
            where m.physical_meter_id = p_meter_id and
                  m.quantity = quantity_id and
                  m.created >= stat_date::timestamp
                    at time zone current_tz and
                  m.created < (stop_date::timestamp + interval '1 day')
                    at time zone current_tz
            group by date1, m.physical_meter_id, m.quantity) as mg on mg.date1 = gen.d2);
  else
    return query (
      select (m.created at time zone current_tz)::date as date,
             p_meter_id as physical_meter_id,
             quantity_id as quantity,
             min(m.consumption),
             max(m.consumption),
             coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected,
             count(m.consumption)::int,
             avg(m.consumption),
             true
      from (select created,
                   value,
                   case
                     when lead(last_known_partition.value) over
                       (order by created asc) is null then
                       null
                     else lead(last_known_partition.value) over
                       (order by created asc) - first_value
                     end as consumption
            from (select part.created,
                         value_partition,
                         part.value,
                         first_value(value)
                                     over (partition by value_partition order by created) as first_value
                  from (select measurements.created,
                               measurements.value,
                               sum(case when measurements.value is null then 0 else 1 end)
                                   over (order by created) as value_partition
                        from (select measurement_serie.when as created,
                                     measurement_serie.value as value
                              from (select value, date_serie.date as when
                                    from (select generate_series(
                                                     stat_date::timestamp - cast('2 day' as interval),
                                                     stop_date::timestamp + cast('2 day' as interval),
                                                     read_interval_interval(read_interval)
                                                   ) at time zone current_tz as date) as date_serie
                                           left join measurement on date_serie.date = created
                                      and measurement.quantity = quantity_id
                                      and measurement.physical_meter_id = p_meter_id
                                   ) as measurement_serie
                              where measurement_serie.when >=
                                    stat_date::timestamp at time zone current_tz
                                      - cast('2 day' as interval) and
                                    measurement_serie.when <=
                                    stop_date::timestamp at time zone current_tz
                                      + cast('1 day' as interval)
                              order by measurement_serie.when asc
                             ) as measurements
                       ) as part
                 ) as last_known_partition
            where last_known_partition.created >=
                  stat_date::timestamp at time zone current_tz and
                  last_known_partition.created <=
                  stop_date::timestamp at time zone current_tz + cast('1 day' as interval)
           ) m
      where m.created >=
            stat_date::timestamp at time zone current_tz and
            m.created < (stop_date::timestamp + interval '1 day') at time zone current_tz
      group by date
    );
  end if;
end;
$BODY$
  language plpgsql;



