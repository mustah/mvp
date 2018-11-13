-- @@ is a magic unit operator to get only the value in the given unit
INSERT INTO new_measurement (physical_meter_id, created, quantity, value)
    (SELECT physical_meter_id,
            created,
            quantity,
            value @@ (select storage_unit from quantity where quantity.id=quantity) as value
     FROM measurement);
