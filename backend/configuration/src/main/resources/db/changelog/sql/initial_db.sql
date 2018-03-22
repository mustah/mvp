CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unit";

CREATE USER ${application_user} WITH ENCRYPTED PASSWORD '${application_password}';
GRANT CONNECT ON DATABASE ${application_database} TO ${application_user};

CREATE TABLE IF NOT EXISTS status (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

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
  name TEXT,
  slug TEXT NOT NULL UNIQUE,
  external_id TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS mvp_user (
  id UUID,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  organisation_id UUID REFERENCES organisation,
  PRIMARY KEY (organisation_id, id)
);

CREATE TABLE IF NOT EXISTS role (
  role TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS users_roles (
  organisation_id UUID,
  user_id UUID,
  role_id TEXT REFERENCES role,
  FOREIGN KEY (organisation_id, user_id) REFERENCES mvp_user
);

CREATE TABLE IF NOT EXISTS logical_meter (
  id UUID UNIQUE,
  created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  meter_definition_type BIGINT REFERENCES meter_definition,
  organisation_id UUID NOT NULL REFERENCES organisation,
  external_id TEXT NOT NULL,
  PRIMARY KEY (organisation_id, id),
  UNIQUE (organisation_id, external_id)
);

CREATE TABLE IF NOT EXISTS location (
  logical_meter_id UUID PRIMARY KEY REFERENCES logical_meter(id) ON DELETE CASCADE,
  country TEXT,
  city TEXT,
  street_address TEXT,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  confidence DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS physical_meter (
  id UUID UNIQUE,
  organisation_id UUID REFERENCES organisation,
  address VARCHAR(255) NOT NULL,
  external_id TEXT NOT NULL,
  medium TEXT,
  manufacturer VARCHAR(255),
  logical_meter_id UUID,
  read_interval_minutes BIGINT NOT NULL,
  FOREIGN KEY (organisation_id, logical_meter_id) REFERENCES logical_meter,
  PRIMARY KEY (organisation_id, id),
  UNIQUE (organisation_id, external_id, address)
);

CREATE TABLE IF NOT EXISTS gateway (
  id UUID,
  organisation_id UUID REFERENCES organisation,
  serial TEXT NOT NULL UNIQUE,
  product_model TEXT NOT NULL,
  PRIMARY KEY (organisation_id, id),
  UNIQUE (organisation_id, serial, product_model)
);

CREATE TABLE IF NOT EXISTS gateways_meters (
  organisation_id UUID,
  logical_meter_id UUID,
  gateway_id UUID,
  FOREIGN KEY (organisation_id, logical_meter_id) REFERENCES logical_meter,
  FOREIGN KEY (organisation_id, gateway_id) REFERENCES gateway
);

CREATE TABLE IF NOT EXISTS gateway_status_log (
  id BIGSERIAL PRIMARY KEY,
  start TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  stop TIMESTAMP WITH TIME ZONE,
  status_id BIGINT REFERENCES status (id),
  gateway_id UUID,
  organisation_id UUID REFERENCES organisation,
  FOREIGN KEY (organisation_id, gateway_id) REFERENCES gateway
);

CREATE TABLE IF NOT EXISTS measurement (
  id BIGSERIAL PRIMARY KEY,
  physical_meter_id UUID NOT NULL REFERENCES physical_meter (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  quantity TEXT NOT NULL,
  value UNIT NOT NULL,
  UNIQUE (physical_meter_id, created, quantity, value)
);

CREATE TABLE IF NOT EXISTS mvp_setting (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS physical_meter_status_log (
  id BIGSERIAL PRIMARY KEY,
  -- FIXME: These should be tzranges
  start TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  stop TIMESTAMP WITH TIME ZONE,
  status_id BIGINT REFERENCES status (id),
  physical_meter_id UUID REFERENCES physical_meter (id)
);

GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA public TO ${application_user};
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO ${application_user};
