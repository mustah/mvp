create or replace function calculate_statistics(quantity_id int,
                                                p_meter_id uuid,
                                                stat_date date,
                                                stop_date date,
                                                current_tz text,
                                                read_interval int,
                                                consumption boolean) returns
                                                  table(stat_d date,
                                                        physical_meter_id uuid,
                                                        quantity int,
                                                        min double precision,
                                                        max double precision,
                                                        expected_count int,
                                                        received_count int,
                                                        average double precision) as
$BODY$
begin
  if( not consumption ) then
    return query(
    select (gen.d2::date at time zone current_tz)::date,
                         p_meter_id,
                         quantity_id,
                         mg.min,
                         mg.max,
                         mg.expected_count,
                         mg.received_count,
                         mg.average from
  (select  cast(generate_series(stat_date::timestamp at time zone current_tz,
                                stop_date::timestamp at time zone current_tz,
                                '1 day'::interval) AT TIME ZONE current_tz as date) as d2 ) as gen
  left join
  (select (m.created at time zone current_tz)::date as d,
     m.physical_meter_id,
     m.quantity,
     min(value),
     max(value),
     coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected_count,
     count(value)::int as received_count,
     avg(value) as average
   from measurement m
   where m.physical_meter_id = p_meter_id and
         m.quantity=quantity_id and
         m.created >= stat_date::timestamp
                      at time zone current_tz and
         m.created < (stop_date::timestamp + interval '1 day')
                     at time zone current_tz
   group by d, m.physical_meter_id, m.quantity) as mg on mg.d=gen.d2);
  else
    return query (select (m.created at time zone current_tz)::date as d,
  m.physical_meter_id,
  m.quantity,
  min(m.consumption),
  max(m.consumption),
  coalesce(60 * 24 / nullif(read_interval, 0), 0) as expected,
  count(m.consumption)::int,
  avg(m.consumption)
from (select a.physical_meter_id,
     quantity_id as quantity,
     created,
     value,
     first_value as last_known,
     lead(value) over (
       order by created asc) as next_value,
     case
     when lead(value) over (
       order by created asc) is null then null
     else lead(value) over (
       order by created asc)-first_value
     end as consumption
   from
     (select q.physical_meter_id,
        quantity_id as quantity,
        q.created,
        value_partition,
        q.value,
        first_value(value) over (partition by value_partition
          order by created) from
        (select p.physical_meter_id, created, value, sum(case
                                                       when value is null then 0
                                                       else 1
                                                       end) over(
          order by created) as value_partition
         from
           (select p_meter_id as physical_meter_id, measurement_serie.when as created, measurement_serie.value
            from
              (select value, date_serie.date as when
               from
                 ( select generate_series( stat_date::timestamp at time zone current_tz - cast('2 day' as INTERVAL),
                                           stop_date::timestamp at time zone current_tz + cast('2 day' as INTERVAL),
                                           cast((case when read_interval=0 then 60 else read_interval end)||' minutes' as INTERVAL)) as date) as date_serie
                 left join measurement on date_serie.date = created
                                          and measurement.quantity = quantity_id
                                          and measurement.physical_meter_id = p_meter_id ) as measurement_serie
            where measurement_serie.when >= cast(stat_date as timestamp) at time zone current_tz - cast('2 day' as INTERVAL)
                  and measurement_serie.when <= cast(stop_date as timestamp) at time zone current_tz + cast('1 day' as INTERVAL)
            order by measurement_serie.when asc) as p) as q ) as a
   where a.created >= cast(stat_date as timestamp) at time zone current_tz
         and a.created <= cast(stop_date as timestamp) at time zone current_tz+ cast('1 day' as INTERVAL) ) m
where m.physical_meter_id = p_meter_id and
      m.quantity=quantity_id and
      m.created >= stat_date::timestamp
                   at time zone current_tz and
      m.created < (stop_date::timestamp + interval '1 day')
                  at time zone current_tz
group by d, m.physical_meter_id, m.quantity);
  end if;
end;
$BODY$
language plpgsql;



