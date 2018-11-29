alter table gateway_status_log
  drop constraint gateway_status_log_gateway_id_fkey;

alter table gateway_status_log
  add constraint gateway_status_log_gateway_id_organisation_id_fkey
    foreign key (gateway_id, organisation_id)
      references gateway(id, organisation_id);
