CREATE CONSTRAINT TRIGGER reject_mixed_measurement_dimensions
  after insert or update
  on measurement
  for each row
execute procedure ensure_no_mixed_dimensions();
