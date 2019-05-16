sysctl_keepalive:
  file.managed:
    - name: /etc/sysctl.d/15-keepalive.conf
    - source: salt://mvp/db/files/15-keepalive.conf
  cmd.run:
     - name: sysctl --load=/etc/sysctl.d/15-keepalive.conf
     - onchanges:
       - file: /etc/sysctl.d/15-keepalive.conf

postgresql-configuration:
  file.managed:
    - name: /etc/postgresql/10/main/postgresql.conf
    - source: salt://mvp/db/files/postgresql.conf
  cmd.run:
     - name: systemctl restart postgresql
     - onchanges:
       - file: /etc/postgresql/10/main/postgresql.conf
     - require:
       - sysctl_keepalive
