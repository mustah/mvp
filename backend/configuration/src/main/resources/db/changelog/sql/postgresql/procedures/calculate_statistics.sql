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
                                                p_consumption boolean) returns
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
  if (not p_consumption)
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
        true
    from
    (select cast(generate_series(p_stat_date::timestamp at time zone current_tz,
                                 stop_date::timestamp at time zone current_tz,
                                 '1 day'::interval) at time zone
                 current_tz as date) as d2) as gen
    left join
    (select
       date as stat_date,
       min(consumption),
       max(consumption),
       min(ec)::int as expected_count, --could be separate query?
       count(consumption )::int as received_count,
       avg(consumption) as average
       from (
         select
           (expected_time at time zone current_tz)::date as date,
           expected_time,
           value,
           lead(value) over (partition by m.physical_meter_id order by expected_time),
           case when
               extract(epoch from (lead(expected_time) over (partition by m.physical_meter_id order by expected_time)
               - expected_time))/60 = read_interval
           then
               lead(value) over (partition by m.physical_meter_id order by expected_time) - value
           else
               null
           end as consumption,
           (extract(epoch from (
              upper(p.active_period * tstzrange((expected_time at time zone current_tz)::date,
                                               ((expected_time at time zone current_tz)+('1 day'::interval))::date,'[)'))
              -lower(p.active_period * tstzrange((expected_time at time zone current_tz)::date,
                                                ((expected_time at time zone current_tz)+('1 day'::interval))::date,'[)'))))/60)/read_interval_minutes as ec

         from  measurement m
         join physical_meter p on m.physical_meter_id = p.id and m.organisation_id = p.organisation_id
         where m.physical_meter_id = p_meter_id
          and m.organisation_id = p_organisation_id
          and m.expected_time>=(((p_stat_date::timestamp at time zone current_tz)-
               cast(
                  (case when read_interval = 0 then
                    60
                   else
                     read_interval
                    end)
                  ||' minutes' as interval)) - interval '1 day' )
          and m.expected_time<=(stop_date::timestamp at time zone current_tz + interval '1 day')
          and m.quantity_id = p_quantity_id
        ) as a
        group by date
      ) as mg on mg.stat_date = gen.d2
    );
  end if;
end;
$BODY$
  language plpgsql;



