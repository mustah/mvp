update measurement_stat_data
set is_consumption = (msd.display_mode = 2)
from (
       select quantity.series_display_mode as display_mode,
              physical_meter_id,
              measurement_stat_data.quantity
       from measurement_stat_data
              join quantity on measurement_stat_data.quantity = quantity.id
     ) msd
where measurement_stat_data.quantity = msd.quantity and
      measurement_stat_data.physical_meter_id = msd.physical_meter_id;
