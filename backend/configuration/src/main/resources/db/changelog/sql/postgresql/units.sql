CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unit";

CREATE USER ${application_user} WITH ENCRYPTED PASSWORD '${application_password}';
GRANT CONNECT ON DATABASE ${application_database} TO ${application_user};
