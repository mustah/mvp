

create unique index if not exists measurement_physical_meter_id_created_quantity_value_idx
  on measurement (physical_meter_id, created, quantity, value);
