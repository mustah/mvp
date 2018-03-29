include:
  - web.nginx

Install ssl cert:
  file.managed:
    - name: /etc/nginx/cert.crt
    - source: salt://certs/files/wildcard.elvaco.se.crt

Install ssl key:
  file.managed:
    - name: /etc/nginx/cert.key
    - source: salt://certs/files/wildcard.elvaco.se.key

Install nginx config:
  file.managed:
    - name: /etc/nginx/sites-available/default
    - source: salt://mvp/app/files/mvp/mvpsite.elvaco.se.conf.jinja
    - template: jinja
    - require:
      - pkg: nginx
  cmd.run:
    - name: systemctl restart nginx.service
    - onchange:
      - file: /etc/nginx/sites-available/default
