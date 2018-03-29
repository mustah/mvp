geoservicedb-app:
  postgres_user.present:
    - name: geoservicedb-app
    - password: ftypTI52S66jcbLwQ072AlrCndsgC4Qe
    - login: True

geoservicedb-admin:
  postgres_user.present:
    - name: geoservicedb-admin
    - password: dCU0WkunRIGDrTfDdPoK5QcyvPqWCe88
    - login: True
    - superuser: True

geoservicedb-app-privilegies:
  postgres_privileges.present:
    - name: geoservicedb-app
    - object_name: ALL
    - object_type: table
    - privileges:
      - ALL
    - maintenance_db: geoservicedb
