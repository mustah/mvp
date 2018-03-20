drop-mvp-database:
  postgres_database.absent:
    - name: mvpdb

drop-mvp-app-role:
  postgres_user.absent:
    - name: mvpdb-app
