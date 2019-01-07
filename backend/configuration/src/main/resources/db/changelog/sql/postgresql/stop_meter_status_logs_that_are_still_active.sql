update physical_meter_status_log
set stop = (select min(start)
            from physical_meter_status_log pmsl
            where pmsl.physical_meter_id = physical_meter_status_log.physical_meter_id and
                  physical_meter_status_log.organisation_id = pmsl.organisation_id and
                  pmsl.start > physical_meter_status_log.start)
where stop is null;
