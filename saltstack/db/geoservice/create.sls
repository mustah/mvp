geoservicedb-admin:
  postgres_user.present:
    - name: geoservicedb-admin
    - password: dCU0WkunRIGDrTfDdPoK5QcyvPqWCe88
    - login: True
    - superuser: True

geoservicedb-app:
  postgres_user.present:
    - name: geoservicedb-app
    - password: ftypTI52S66jcbLwQ072AlrCndsgC4Qe
    - login: True

create-geoservice-database:
  postgres_database.present:
    - name: geoservicedb
    - template: template0
    - lc_collate: "sv_SE.utf8"
    - encoding: "UTF8"

geoservicedb-app-privilegies:
  postgres_privileges.present:
    - name: geoservicedb-app
    - object_name: ALL
    - object_type: table
    - privileges:
      - ALL
    - maintenance_db: geoservicedb
    - require:
      - create-geoservice-database
