include:
  - postgresql.server
  - postgresql.db_users

create-geocode-database:
  postgres_database.present:
    - name: geocodedb
    - template: template0
    - lc_collate: "sv_SE.utf8"
    - encoding: "UTF8"

set-geocodedb-app-privileges:
  postgres_privileges.present:
    - name: geocodedb-app
    - object_name: geocodedb
    - object_type: database
    - privileges:
      - ALL
