
create unique index on measurement_stat_data(physical_meter_id, stat_date, quantity)
  where is_consumption = false;
