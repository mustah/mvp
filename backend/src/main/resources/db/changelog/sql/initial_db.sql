create extension if not exists "uuid-ossp";
create extension if not exists "unit";
-- unit aliases, do we really need these? Could we change the units at the source (Metering) instead?
insert into unit_units values ('Celsius', '1 K'::unit, 273.15, 'K');
insert into unit_units values ('Kelvin', '1 K'::unit, default, 'K');
insert into unit_units values ('m3', 'm^3'::unit, default, 'm');

create table if not exists meter_definition (
	id bigserial primary key
);

create table if not exists organisation (
	id bigserial primary key,
	name varchar(255)
);

create table if not exists metering_point (
	id bigserial primary key,
	property_collection jsonb
	-- meter_definition_id bigserial references meter_definition
);

create table if not exists physical_meter (
	id bigserial primary key,
	organisation_id bigserial references organisation,
	identity varchar(255),
	medium varchar(255),
	metering_point_id bigserial references metering_point,
	unique (organisation_id, identity)
);

-- TODO: add gateway

create table if not exists measurement (
	id bigserial primary key,
	physical_meter_id bigserial references physical_meter (id) on update cascade on delete cascade,
	created timestamp without time zone not null default now(),
	quantity varchar(255),
	value unit, -- if this is a proper measurement, the value will be here
	unique (physical_meter_id, created, quantity, value)
);

/*create or replace function get_measurements_for_logical_meter(_logical_meter_id logical_meter.uuid%type, _from measurement.created%type, _to measurement.created%type) returns setof measurement as $$
begin
	return select * from physical_meter, measurement where physical_meter.logical_meter_id = _logical_meter_id and measurement.created >= _from and measurement.created <= _to;
end
$$ language plpgsql;*/

create or replace function add_measurement(organisation_name organisation.name%type,
	_identity physical_meter.identity%type,
	_medium physical_meter.medium%type,
	measurement_quantity measurement.quantity%type,
	measurement_unit varchar(255),
	measurement_created measurement.created%type,
	measurement_value double precision) returns physical_meter.id%type as $$
declare
	physical_meter_id physical_meter.id%type;
	organisation_id organisation.id%type;
begin

	select id from organisation where name = organisation_name into organisation_id;
		if organisation_id is null then
			insert into organisation values(default, organisation_name) returning id into organisation_id;
		end if;

	select physical_meter.id
	from organisation, physical_meter
				where
					physical_meter.organisation_id = organisation_id
					and physical_meter.identity = _identity
					and physical_meter.medium = _medium
					into physical_meter_id;

	if physical_meter_id is null then
		-- New physical meter!
		insert into physical_meter values(default, organisation_id, _identity, _medium) returning id into physical_meter_id;
	end if;
	insert into measurement values(default, physical_meter_id, measurement_created,  measurement_quantity, (measurement_value || measurement_unit)::unit);
	return physical_meter_id;
end;
$$ language plpgsql;
