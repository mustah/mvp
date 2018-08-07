--
-- create materialized view with no data and populate it later.
--
--DROP MATERIALIZED VIEW missing_measurement;
CREATE MATERIALIZED VIEW missing_measurement
  AS
    SELECT
      physical_meter_id,
      expected_time,
      meter_definition_type
    FROM (SELECT
            pm.id           AS       physical_meter_id,
            t.expected_time AS       expected_time,
            lm.meter_definition_type meter_definition_type
          FROM physical_meter pm
            JOIN logical_meter lm
              ON pm.organisation_id = lm.organisation_id AND pm.logical_meter_id = lm.id
            JOIN (SELECT
                    physical_meter.id,
                    generate_series(date_trunc('hour' :: text, lm2.created),
                                    ((CURRENT_TIMESTAMP) :: timestamp WITHOUT TIME ZONE) :: timestamp WITH TIME ZONE,
                                    (concat(physical_meter.read_interval_minutes,
                                            ' minutes')) :: interval) AS expected_time
                  FROM physical_meter
                    JOIN logical_meter lm2
                      ON physical_meter.organisation_id = lm2.organisation_id AND
                         physical_meter.logical_meter_id = lm2.id
                  WHERE read_interval_minutes > 0) t ON pm.id = t.id
          WHERE read_interval_minutes > 0) AS expected_measurements
    WHERE NOT EXISTS(SELECT 1
                     FROM measurement
                     WHERE physical_meter_id = expected_measurements.physical_meter_id AND
                           created = expected_measurements.expected_time AND
                           meter_definition_type = expected_measurements.meter_definition_type)
WITH NO DATA;

CREATE UNIQUE INDEX missing_measurement_meter_idx
  ON missing_measurement (expected_time, physical_meter_id, meter_definition_type);

--
-- populate view for the first time
--
REFRESH MATERIALIZED VIEW missing_measurement;

--
-- refresh view without blocking
--
--refresh materialized view concurrently missing_measurement;
