-- We accidentally set the wrong time zone for measurements. This corrects that.
INSERT INTO new_measurement (physical_meter_id, created, quantity, value)
    (SELECT physical_meter_id,
            created  /* ex: '2018-03-25 16:00:00+00'*/
              at time zone 'Europe/Stockholm' /* to local time in Europe/Stockholm (aka CET/CEST: which is what we accidentally inserted the timestamp as), respecting DST:'2018-03-25 18:00:00' (no UTC offset) */
              at time zone 'UTC' /* interpreted as a UTC timestamp: '2018-03-25 18:00:00+00' (casting to timestamptz using cast () or ::timestamptz uses the session time zone for the local timestamp, and we don't want that) */
              at time zone 'UTC+1' /* to local time in UTC+1 (which is what we *intended* to insert the timestamp as): '2018-03-25 17:00:00' */
              at time zone 'UTC', /* back to UTC: '2018-03-25 17:00:00+00' */
            quantity,
            value
     FROM measurement);
