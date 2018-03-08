CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unit";

CREATE USER ${application_user} WITH ENCRYPTED PASSWORD '${application_password}';
GRANT CONNECT ON DATABASE mvp TO ${application_user};
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
  type INTEGER PRIMARY KEY,
  medium TEXT NOT NULL,
  system_owned BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS meter_definition_quantities (
  quantity_id BIGINT REFERENCES quantity NOT NULL,
  meter_definition_type BIGINT REFERENCES meter_definition NOT NULL
);

CREATE TABLE IF NOT EXISTS organisation (
  id UUID PRIMARY KEY,
  name VARCHAR(255),
  code VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS mvp_user (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  organisation_id UUID REFERENCES organisation
);

CREATE TABLE IF NOT EXISTS role (
  role VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS users_roles (
  user_id UUID REFERENCES mvp_user,
  role_id VARCHAR(255) REFERENCES role
);

CREATE TABLE IF NOT EXISTS logical_meter (
  id UUID PRIMARY KEY,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  meter_definition_type BIGINT REFERENCES meter_definition,
  organisation_id UUID NOT NULL REFERENCES organisation,
  external_id TEXT NOT NULL,
  UNIQUE (organisation_id, external_id)
);

CREATE TABLE IF NOT EXISTS location (
  logical_meter_id UUID REFERENCES logical_meter ON DELETE CASCADE PRIMARY KEY,
  country TEXT,
  city TEXT,
  street_address TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  confidence DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS physical_meter (
  id UUID PRIMARY KEY,
  organisation_id UUID REFERENCES organisation,
  address VARCHAR(255) NOT NULL,
  external_id TEXT NOT NULL,
  medium TEXT,
  manufacturer VARCHAR(255),
  logical_meter_id UUID REFERENCES logical_meter,
  UNIQUE (organisation_id, external_id, address)
);

CREATE TABLE IF NOT EXISTS gateway (
  id UUID PRIMARY KEY,
  serial TEXT NOT NULL UNIQUE,
  product_model TEXT NOT NULL,
  organisation_id UUID REFERENCES organisation,
  UNIQUE (organisation_id, serial, product_model)
);

CREATE TABLE IF NOT EXISTS gateways_meters (
  logical_meter_id UUID REFERENCES logical_meter,
  gateway_id UUID REFERENCES gateway
);

CREATE TABLE IF NOT EXISTS measurement (
  id BIGSERIAL PRIMARY KEY,
  physical_meter_id UUID NOT NULL REFERENCES physical_meter (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  quantity VARCHAR(255) NOT NULL,
  value UNIT NOT NULL,
  UNIQUE (physical_meter_id, created, quantity, value)
);

CREATE TABLE IF NOT EXISTS mvp_setting (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS physical_meter_status (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS physical_meter_status_log (
  id BIGSERIAL PRIMARY KEY,
  start TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  stop TIMESTAMP WITHOUT TIME ZONE,
  status_id BIGINT REFERENCES physical_meter_status (id),
  physical_meter_id UUID REFERENCES physical_meter (id)
);

GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA public TO ${application_user};
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO ${application_user};
