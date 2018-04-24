delete from measurement where quantity = 'Volume' and dimension(value) = '1 m^3/s';

create or replace function ensure_no_mixed_dimensions()
  returns trigger as $$
declare
  new_dimension      unit;
  existing_dimension unit;
begin
  new_dimension := dimension(NEW.value);
  -- Find any existing value in the (meter/quantity) series and compare against that value's dimension
  existing_dimension := (select dimension(value)
                         from measurement
                         where physical_meter_id = NEW.physical_meter_id
                               and quantity = NEW.quantity
                         order by created
                         limit 1);
  if (existing_dimension is not null and new_dimension != existing_dimension)
  then
    raise exception 'Mixed dimensions for same quantity/meter combination is not allowed (have %, got %)', existing_dimension, new_dimension;
  end if;
  return NEW;
end
$$
language plpgsql;

CREATE constraint trigger reject_mixed_measurement_dimensions
  after insert or update
  on measurement
  for each row
execute procedure ensure_no_mixed_dimensions();
