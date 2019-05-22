create or replace function read_interval_interval(read_interval int)
  returns interval as
$BODY$
begin
  return ((case when read_interval = 0 then 60 else read_interval end) || ' minutes')::interval;
end;
$BODY$
  language plpgsql;

drop function if exists calculate_statistics;
create or replace function calculate_statistics(p_organisation_id uuid,
                                                p_quantity_id int,
                                                p_meter_id uuid,
                                                p_stat_date date,
                                                stop_date date,
                                                current_tz text,
                                                read_interval int,
                                                consumption boolean) returns
  table (
    organisation_id uuid,
    stat_date date,
    physical_meter_id uuid,
    quantity_id int,
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
      select
             p_organisation_id,
             gen.d2,
             p_meter_id,
             p_quantity_id,
             mg.min,
             mg.max,
             mg.expected_count,
             mg.received_count,
             mg.average,
             false
      from (select cast(generate_series(p_stat_date::timestamp at time zone current_tz,
                                        stop_date::timestamp at time zone current_tz,
                                        '1 day'::interval) at time zone
                        current_tz as date) as d2) as gen
             left join
           (select (m.expected_time at time zone current_tz)::date as date1,
                   min(m.value),
                   max(m.value),
                   coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected_count,
                   count(m.value)::int as received_count,
                   avg(m.value) as average
            from measurement m
            where m.physical_meter_id = p_meter_id and
                  m.organisation_id = p_organisation_id and
                  m.quantity_id = p_quantity_id and
                  m.expected_time is not null and
                  m.expected_time >= p_stat_date::timestamp
                    at time zone current_tz and
                  m.expected_time < (stop_date::timestamp + interval '1 day')
                    at time zone current_tz
            group by date1, m.physical_meter_id, m.quantity_id) as mg on mg.date1 = gen.d2);
  else
    return query (
      select p_organisation_id,
             (m.readout_time at time zone current_tz)::date as date,
             p_meter_id as physical_meter_id,
             p_quantity_id as quantity_id,
             min(m.consumption),
             max(m.consumption),
             coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected,
             count(m.consumption)::int,
             avg(m.consumption),
             true
      from (select readout_time,
                   value,
                   case
                     when lead(last_known_partition.value) over
                       (order by readout_time asc) is null then
                       null
                     else lead(last_known_partition.value) over
                       (order by readout_time asc) - first_value
                     end as consumption
            from (select part.readout_time,
                         value_partition,
                         part.value,
                         first_value(value)
                                     over (partition by value_partition order by readout_time) as first_value
                  from (select measurements.readout_time,
                               measurements.value,
                               sum(case when measurements.value is null then 0 else 1 end)
                                   over (order by readout_time) as value_partition
                        from (select measurement_serie.when as readout_time,
                                     measurement_serie.value as value
                              from (select value, date_serie.date as when
                                    from (select generate_series(
                                                     p_stat_date::timestamp - cast('2 day' as interval),
                                                     stop_date::timestamp + cast('2 day' as interval),
                                                     read_interval_interval(read_interval)
                                                   ) at time zone current_tz as date) as date_serie
                                           left join measurement on date_serie.date = readout_time
                                      and measurement.quantity_id = p_quantity_id
                                      and measurement.physical_meter_id = p_meter_id
                                      and measurement.organisation_id = p_organisation_id
                                   ) as measurement_serie
                              where measurement_serie.when >=
                                    p_stat_date::timestamp at time zone current_tz
                                      - cast('2 day' as interval) and
                                    measurement_serie.when <=
                                    stop_date::timestamp at time zone current_tz
                                      + cast('1 day' as interval)
                              order by measurement_serie.when asc
                             ) as measurements
                       ) as part
                 ) as last_known_partition
            where last_known_partition.readout_time >=
                  p_stat_date::timestamp at time zone current_tz and
                  last_known_partition.readout_time <=
                  stop_date::timestamp at time zone current_tz + cast('1 day' as interval)
           ) m
      where m.readout_time >=
            p_stat_date::timestamp at time zone current_tz and
            m.readout_time < (stop_date::timestamp + interval '1 day') at time zone current_tz
      group by date
    );
  end if;
end;
$BODY$
  language plpgsql;



