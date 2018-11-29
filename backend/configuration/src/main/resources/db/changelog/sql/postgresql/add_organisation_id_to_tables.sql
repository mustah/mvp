update gateways_meters
set organisation_id = (
  select organisation_id
  from logical_meter
  where logical_meter.id = gateways_meters.logical_meter_id
);

alter table gateway_status_log
  add column organisation_id UUID;

update gateway_status_log
set organisation_id =
  (select organisation.id
   from organisation
          join gateway g on (organisation.id = g.organisation_id)
   where g.id = gateway_status_log.gateway_id);

alter table gateway_status_log
  alter column organisation_id set not null;


alter table location
  add column organisation_id UUID;

update location
set organisation_id = (
  select organisation_id
  from logical_meter
  where logical_meter.id = location.logical_meter_id
);

alter table location
  alter column organisation_id set not null;

alter table location
  drop constraint location_pkey;

alter table location
  add constraint location_pkey primary key (logical_meter_id, organisation_id);

alter table evoaudit.location_aud
  add column organisation_id UUID;

update evoaudit.location_aud
set organisation_id = (
  select organisation_id
  from evoaudit.logical_meter_aud
  where evoaudit.logical_meter_aud.id = evoaudit.location_aud.logical_meter_id
  limit 1
);

alter table evoaudit.location_aud
  alter column organisation_id set not null;
