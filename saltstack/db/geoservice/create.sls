include:
  - postgresql.server
  - postgresql.db_users

create-geoservice-database:
  postgres_database.present:
    - name: geoservicedb
    - template: template0
    - lc_collate: "sv_SE.utf8"
    - encoding: "UTF8"

set-geoservicedb-app-privileges:
  postgres_privileges.present:
    - name: geoservicedb-app
    - object_name: geoservicedb
    - object_type: database
    - privileges:
      - ALL
