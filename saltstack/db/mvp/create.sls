include:
  - mvp.db.mvp.users

create-mvp-database:
  postgres_database.present:
    - name: mvpdb
    - template: template0
    - lc_collate: "sv_SE.utf8"
    - encoding: "UTF8"
