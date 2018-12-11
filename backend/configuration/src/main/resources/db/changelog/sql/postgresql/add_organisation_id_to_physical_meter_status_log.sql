alter table physical_meter_status_log
  add column organisation_id UUID;

update physical_meter_status_log
set organisation_id =
  (select organisation.id
   from organisation
          join physical_meter pm on (organisation.id = pm.organisation_id)
   where pm.id = physical_meter_status_log.physical_meter_id);

alter table physical_meter_status_log alter column organisation_id set not null;

alter table physical_meter_status_log
  add constraint physical_meter_status_log_physical_meter_id_organisation_id_fkey
    foreign key (organisation_id, physical_meter_id)
      references physical_meter(organisation_id, id);
