UPDATE logical_meter
SET meter_definition_type = 5
WHERE meter_definition_type = 6;

UPDATE evoaudit.logical_meter_aud
SET meter_definition_type = 5
WHERE meter_definition_type = 6;

UPDATE physical_meter
SET medium = 'Water'
WHERE lower(medium) = 'cold water';

UPDATE evoaudit.physical_meter_aud
SET medium = 'Water'
WHERE lower(medium) = 'cold water';

-- Remove cold water.
-- This is safe to delete  here since the there more quantities that uses water quantities
-- such as hot water and water.
DELETE
FROM meter_definition_quantities
WHERE meter_definition_type = 6;

DELETE
FROM evoaudit.meter_definition_quantities_aud
WHERE meter_definition_type = 6;

DELETE
FROM meter_definition
WHERE type = 6;

DELETE
FROM evoaudit.meter_definition_aud
WHERE type = 6;

