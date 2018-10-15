drop materialized view if exists missing_measurement; -- can't do this in liquibase xml, unfortunately :(

create materialized view missing_measurement
  as
    select expected.physical_meter_id, expected.expected_time
    from (select pm.id as physical_meter_id, t.expected_time as expected_time
          from physical_meter pm
                 join (select physical_meter.id,
                              generate_series(case
                                                when date_trunc('hour', lm.created :: timestamp) <
                                                     date_trunc('hour', (current_timestamp - '3 months' :: interval))
                                                        then date_trunc('hour', (current_timestamp - '3 months' :: interval))
                                                else date_trunc('hour', lm.created :: timestamp) end,
                                              date_trunc('day', current_timestamp),
                                              (physical_meter.read_interval_minutes || ' minutes') :: interval) as expected_time
                       from physical_meter
                              join logical_meter lm
                                on physical_meter.organisation_id = lm.organisation_id and
                                   physical_meter.logical_meter_id = lm.id
                       where read_interval_minutes > 0) t on pm.id = t.id) expected
           left join (select distinct physical_meter_id, created
                      from measurement
                      where created > current_timestamp - '3 months' :: interval
                        and created <= date_trunc('day', current_timestamp)) existing
             on (expected.physical_meter_id = existing.physical_meter_id and
                 expected.expected_time = existing.created)
    where existing.physical_meter_id is null
with no data;


create unique index missing_measurement_meter_idx
  on missing_measurement (expected_time, physical_meter_id);
