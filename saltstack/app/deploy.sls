{% set mvp_version = "v0.0.1-1997-g1d4c116" %}
{% set mvp_hash = "0deb796dcda77bd5072442d49437d59f" %}
{% set mvp_systemd_unit = "elvaco-mvp.service" %}

include:
  - mvp.openjdk-8-jre
  - mvp.app.user

fetch_mvp_archive:
  cmd.run:
    - name: curl -O http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar
    - cwd: /tmp
    - runas: mvp

create_new_mvp_dir:
  file.directory:
    - name: /opt/elvaco/mvp-{{ mvp_version }}
    - user: mvp
    - group: mvp
    - mode: 755
    - makedirs: True
    - require:
      - fetch_mvp_archive

deploy_mvp:
  archive.extracted:
    - name: /opt/elvaco
    - archive_format: tar
    - source: /tmp/mvp-{{ mvp_version }}.tar
    - source_hash: {{ mvp_hash }}
    - user: mvp
    - group: mvp
    - require:
      - create_new_mvp_dir

create_mvp_symlink:
  file.symlink:
    - name: /opt/elvaco/mvp-current
    - target: /opt/elvaco/mvp-{{ mvp_version }}
    - require:
      - deploy_mvp

deploy_mvp_systemd:
  file.managed:
    - name: /lib/systemd/system/{{ mvp_systemd_unit }}
    - source: salt://mvp/app/files/{{ mvp_systemd_unit }}
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /lib/systemd/system/{{ mvp_systemd_unit }}
  service.running:
    - name: {{ mvp_systemd_unit }}
    - enable: True
    - require:
      - create_mvp_symlink
    - watch:
      - file: /lib/systemd/system/{{ mvp_systemd_unit }}

remove_mvp_archive:
  file.absent:
    - name: /tmp/mvp-{{ mvp_version }}.tar
    - require:
      - deploy_mvp_systemd

mvp_version:
  grains.present:
    - value: {{ mvp_version }}
    - require:
      - deploy_mvp_systemd
