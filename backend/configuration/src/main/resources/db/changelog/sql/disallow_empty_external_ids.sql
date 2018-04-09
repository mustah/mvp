DELETE FROM physical_meter WHERE char_length(external_id) = 0;
ALTER TABLE physical_meter ADD CONSTRAINT external_id_empty_check CHECK (char_length(external_id) > 0);

DELETE FROM logical_meter WHERE char_length(external_id) = 0;
ALTER TABLE logical_meter ADD CONSTRAINT external_id_empty_check CHECK (char_length(external_id) > 0);

DELETE FROM organisation WHERE char_length(external_id) = 0;
ALTER TABLE organisation ADD CONSTRAINT external_id_empty_check CHECK (char_length(external_id) > 0);
