include:
  - postgresql.server
  - postgresql.conf
  - postgresql.db_users

drop-mvp-database:
  postgres_database.absent:
    - name: mvpdb

drop-mvp-app-role:
  postgres_user.absent:
    - name: mvpdb-app
