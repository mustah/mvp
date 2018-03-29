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
