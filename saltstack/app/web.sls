include:
  - web.nginx

Install evo-502-error-page:
  file.managed:
    - name: /usr/share/nginx/html/evo-502.html
    - source: salt://mvp/app/files/mvp/evo-502.html

Install evo-502-error-page-image:
  file.managed:
    - name: /usr/share/nginx/html/evo_maintenance.jpg
    - source: salt://mvp/app/files/mvp/evo_maintenance.jpg

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
