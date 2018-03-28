drop-geoservice-database:
  postgres_database.absent:
    - name: geoservicedb

drop-geoservice-app-role:
  postgres_user.absent:
    - name: geoservicedb-app
