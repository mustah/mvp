--
-- create materialized view with no data and populate it later.
--
create materialized view missing_measurement
  as
    select physical_meter_id,
           expected_time,
           meter_definition_type
    from (select pm.id as physical_meter_id,
                 t.expected_time as expected_time,
                 lm.meter_definition_type meter_definition_type
          from physical_meter pm
                 join logical_meter lm
                 on pm.organisation_id = lm.organisation_id and pm.logical_meter_id = lm.id
                 join (select physical_meter.id,
                              generate_series(
                                date_trunc('hour', lm2.created),
                                date_trunc('hour', now()),
                                (physical_meter.read_interval_minutes || ' minutes') :: interval) as expected_time
                       from physical_meter
                              join logical_meter lm2
                              on physical_meter.organisation_id = lm2.organisation_id and
                                 physical_meter.logical_meter_id = lm2.id
                       where read_interval_minutes > 0) t on pm.id = t.id
          where read_interval_minutes > 0) as expected_measurements
    where not exists(select 1
                     from measurement
                     where physical_meter_id = expected_measurements.physical_meter_id and
                             created = expected_measurements.expected_time at time zone 'UTC' and
                             meter_definition_type = expected_measurements.meter_definition_type)
with no data;

create unique index missing_measurement_meter_idx
  on missing_measurement (expected_time, physical_meter_id, meter_definition_type);

--
-- populate view for the first time
--
refresh materialized view missing_measurement;

--
-- refresh view without blocking
--
--refresh materialized view concurrently missing_measurement;
