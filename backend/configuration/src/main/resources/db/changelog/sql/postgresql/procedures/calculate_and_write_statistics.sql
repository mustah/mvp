create or replace function calculate_and_write_statistics(quantity_id int,
                                                p_meter_id uuid,
                                                start_d date,
                                                stop_d date,
                                                current_tz text,
                                                read_interval int,
                                                consumption boolean) returns void as
$BODY$
declare
  cur_row measurement_stat_data%rowtype;
begin
  for cur_row in select * from calculate_statistics( quantity_id,
                              p_meter_id,
                              start_d,
                              stop_d,
                              current_tz,
                              read_interval,
                              consumption)
   loop
     if(cur_row.received_count is not null and cur_row.received_count >0) then
        insert into measurement_stat_data(stat_date,
                                          physical_meter_id,
                                          quantity,
                                          min,
                                          max,
                                          expected_count,
                                          received_count,
                                          average)
                                          values
                                          (cur_row.stat_date,
                                          cur_row.physical_meter_id,
                                          cur_row.quantity,
                                          cur_row.min,
                                          cur_row.max,
                                          cur_row.expected_count,
                                          cur_row.received_count,
                                          cur_row.average)
          on CONFLICT (stat_date,physical_meter_id,quantity) do update
          set min = excluded.min,
              max = excluded.max,
              expected_count = excluded.expected_count,
              received_count = excluded.received_count,
              average = excluded.average;

     else
        delete from measurement_stat_data
          where measurement_stat_data.stat_date=cur_row.stat_date and
                measurement_stat_data.physical_meter_id=cur_row.physical_meter_id and
                measurement_stat_data.quantity = cur_row.quantity;
     end if;

   end loop;
end;
$BODY$
language plpgsql;
