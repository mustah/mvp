drop-geocode-database:
  postgres_database.absent:
    - name: geocodedb

drop-geocode-app-role:
  postgres_user.absent:
    - name: geocodedb-app
