sysctl_keepalive:
  file.managed:
    - name: /etc/sysctl.d/15-keepalive.conf
    - source: salt://mvp/db/files/15-keepalive.conf
  cmd.run:
     - name: sysctl --load=/etc/sysctl.d/15-keepalive.conf
     - onchange:
       - file: /etc/sysctl.d/15-keepalive.conf
