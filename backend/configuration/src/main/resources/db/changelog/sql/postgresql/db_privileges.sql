--SHOULD ALWAYS BE EXECUTED LAST
GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA public TO ${application_user};
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO ${application_user};

GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA evoaudit TO ${application_user};
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA evoaudit TO ${application_user};
GRANT USAGE ON SCHEMA evoaudit TO ${application_user};
