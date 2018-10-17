include:
  - mvp

Install JRE:
  pkg.removed:
    - name: openjdk-8-jre-headless

remove_geoservice_systemd:
  file.absent:
    - name: /lib/systemd/system/elvaco-geoservice.service

remove_mvp_systemd:
  file.absent:
    - name: /lib/systemd/system/elvaco-mvp.service

reload_systemd:
  module.run:
  - name: service.systemctl_reload
  - require:
    - remove_mvp_systemd
    - remove_geoservice_systemd
