select calculate_and_write_statistics(
  quantity,
  physical_meter_id,
  stat_date,
  stat_date,
  '-01',
  read_intervall_minutes,
  is_consumption
)
from
  (select distinct quantity,
          physical_meter_id,
          stat_date,
          (24*60/expected_count)::int as read_intervall_minutes,
          is_consumption
from measurement_stat_data 
where stat_date = :DATE and 
      physical_meter_id in (select id from physical_meter where organisation_id=:ORG))
as foo;
