CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unit";
-- unit aliases, do we really need these? Could we change the units at the source (Metering) instead?
INSERT INTO unit_units VALUES ('Celsius', '1 K' :: UNIT, 273.15, 'K');
INSERT INTO unit_units VALUES ('Kelvin', '1 K' :: UNIT, default, 'K');
INSERT INTO unit_units VALUES ('m3', 'm^3' :: UNIT, default, 'm');

CREATE TABLE IF NOT EXISTS quantity (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  unit VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS meter_definition (
  id BIGSERIAL PRIMARY KEY,
  medium TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS meter_definition_quantities (
  quantity_id BIGINT REFERENCES quantity NOT NULL,
  meter_definition_id BIGINT REFERENCES meter_definition NOT NULL
);

CREATE TABLE IF NOT EXISTS organisation (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  code VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS mvp_user (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  organisation_id BIGINT REFERENCES organisation
);

CREATE TABLE IF NOT EXISTS role (
  role VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS users_roles (
  user_id BIGINT REFERENCES mvp_user,
  role_id VARCHAR(255) REFERENCES role
);

CREATE TABLE IF NOT EXISTS logical_meter (
  id BIGSERIAL PRIMARY KEY,
  status VARCHAR(255),
  medium VARCHAR(255),
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  property_collection JSONB,
  meter_definition_id BIGINT REFERENCES meter_definition
);

CREATE TABLE IF NOT EXISTS location (
  meter_id BIGINT REFERENCES logical_meter ON DELETE CASCADE PRIMARY KEY,
  country TEXT,
  city TEXT,
  street_address TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  confidence DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS physical_meter (
  id BIGSERIAL PRIMARY KEY,
  organisation_id BIGINT REFERENCES organisation,
  identity VARCHAR(255),
  medium VARCHAR(255),
  logical_meter_id BIGINT REFERENCES logical_meter,
  UNIQUE (organisation_id, identity)
);

CREATE TABLE IF NOT EXISTS gateway (
  id BIGSERIAL PRIMARY KEY,
  serial VARCHAR(255) NOT NULL,
  product_model TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS gateways_meters (
  meter_id BIGINT REFERENCES logical_meter,
  gateway_id BIGINT REFERENCES gateway
);

CREATE TABLE IF NOT EXISTS measurement (
  id BIGSERIAL PRIMARY KEY,
  physical_meter_id BIGINT NOT NULL REFERENCES physical_meter (id) ON UPDATE CASCADE ON DELETE CASCADE,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  quantity VARCHAR(255) NOT NULL,
  value UNIT NOT NULL,
  UNIQUE (physical_meter_id, created, quantity, value)
);

CREATE TABLE IF NOT EXISTS mvp_setting (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  value TEXT NOT NULL
);

CREATE OR REPLACE FUNCTION add_measurement(organisation_name    organisation.name%TYPE,
                                           _identity            physical_meter.identity%TYPE,
                                           _medium              physical_meter.medium%TYPE,
                                           measurement_quantity measurement.quantity%TYPE,
                                           measurement_unit     VARCHAR(255),
                                           measurement_created  measurement.created%TYPE,
                                           measurement_value    DOUBLE PRECISION)
  RETURNS physical_meter.id%TYPE AS $$
DECLARE
  physical_meter_id physical_meter.id%TYPE;
  organisation_id   organisation.id%TYPE;
BEGIN

  SELECT id
  FROM organisation
  WHERE name = organisation_name
  INTO organisation_id;
  IF organisation_id IS NULL
  THEN
    INSERT INTO organisation VALUES (default, organisation_name)
    RETURNING id
      INTO organisation_id;
  END IF;

  SELECT physical_meter.id
  FROM organisation, physical_meter
  WHERE
    physical_meter.organisation_id = organisation_id
    AND physical_meter.identity = _identity
    AND physical_meter.medium = _medium
  INTO physical_meter_id;

  IF physical_meter_id IS NULL
  THEN
    -- New physical meter!
    INSERT INTO physical_meter VALUES (default, organisation_id, _identity, _medium)
    RETURNING id
      INTO physical_meter_id;
  END IF;
  INSERT INTO measurement
  VALUES (default, physical_meter_id, measurement_created, measurement_quantity,
          (measurement_value || measurement_unit) :: UNIT);
  RETURN physical_meter_id;
END;
$$
LANGUAGE plpgsql;
