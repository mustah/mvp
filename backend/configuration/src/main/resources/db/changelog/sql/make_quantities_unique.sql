update meter_definition_quantities m
set quantity_id = (select max(id)
                   from quantity q
                   where q.name = (
                     select name
                     from quantity
                     where id = m.quantity_id)
)
where exists(select *
             from quantity q
             where q.id = m.quantity_id);

-- delete all duplicate quantities, that are not referred to by the newly updated meter_definition join table
DELETE FROM quantity
WHERE quantity.id NOT IN (
  SELECT quantity_id
  FROM meter_definition_quantities
);

-- fixup a messy history in envers' audit logs
DELETE FROM evoaudit.quantity_aud
WHERE evoaudit.quantity_aud.id NOT IN (
  SELECT id
  FROM quantity
);

DELETE FROM evoaudit.meter_definition_quantities_aud
WHERE quantity_id NOT IN (
  SELECT id
  FROM quantity
);
