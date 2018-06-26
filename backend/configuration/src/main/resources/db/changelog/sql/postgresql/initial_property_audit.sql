insert into evoaudit.property_aud (rev, revtype, entity_id, organisation_id, property_key, property_value, value_mod)
  select
    (select max(id)
     from evoaudit.REVISION_ENTITY),
    0,
    entity_id,
    organisation_id,
    property_key,
    property_value,
    true
  from property;
