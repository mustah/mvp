-- allow gist to create index on columns of the type UUID
create extension btree_gist;

alter table physical_meter
add constraint non_overlapping_active_periods exclude
using gist (active_period with &&, logical_meter_id with =);
