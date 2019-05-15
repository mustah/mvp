drop function if exists calculate_and_write_statistics;
create or replace function calculate_and_write_statistics(p_organisation_id uuid,
                                                p_quantity_id int,
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
  for cur_row in select organisation_id,
                        stat_date,
                        physical_meter_id,
                        quantity_id,
                        min,
                        max,
                        expected_count,
                        received_count,
                        average,
                        is_consumption
                   from calculate_statistics(
                          p_organisation_id,
                          p_quantity_id,
                          p_meter_id,
                          start_d,
                          stop_d,
                          current_tz,
                          read_interval,
                          consumption)
   loop
     if(cur_row.received_count is not null and cur_row.received_count >0) then
        insert into measurement_stat_data(organisation_id,
                                          stat_date,
                                          physical_meter_id,
                                          quantity_id,
                                          min,
                                          max,
                                          expected_count,
                                          received_count,
                                          average,
                                          is_consumption)
                                          values
                                          ( cur_row.organisation_id,
                                          cur_row.stat_date,
                                          cur_row.physical_meter_id,
                                          cur_row.quantity_id,
                                          cur_row.min,
                                          cur_row.max,
                                          cur_row.expected_count,
                                          cur_row.received_count,
                                          cur_row.average,
                                          cur_row.is_consumption)
          on CONFLICT (organisation_id,stat_date,physical_meter_id,quantity_id,is_consumption) do update
          set min = excluded.min,
              max = excluded.max,
              expected_count = excluded.expected_count,
              received_count = excluded.received_count,
              average = excluded.average;

     else
        delete from measurement_stat_data
          where measurement_stat_data.organisation_id=cur_row.organisation_id and
                measurement_stat_data.stat_date=cur_row.stat_date and
                measurement_stat_data.physical_meter_id=cur_row.physical_meter_id and
                measurement_stat_data.quantity_id = cur_row.quantity_id and
                measurement_stat_data.is_consumption = cur_row.is_consumption;
     end if;

   end loop;
end;
$BODY$
language plpgsql;
