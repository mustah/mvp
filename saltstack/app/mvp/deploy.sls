{% set mvp_systemd_unit = "elvaco-mvp.service" %}
{% set mvp_branch = salt['pillar.get']('mvp-branch', 'master') %}
{% set mvp_version = salt['pillar.get']('mvp_version', 'UNKNOWN') %}

include:
  - mvp.openjdk-8-jre
  - mvp.app.user

fetch_mvp_archive:
  file.managed:
    - name: /tmp/mvp-{{ mvp_version }}.tar
    - source: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar
    - source_hash: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar.sha1

create_new_mvp_dir:
  file.directory:
    - name: /opt/elvaco/mvp-{{ mvp_version }}/config
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
    - source_hash: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar.sha1
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

deploy_mvp_config:
  file.managed:
    - name: /opt/elvaco/mvp-{{ mvp_version }}/config/application.properties
    - source: salt://mvp/app/files/mvp/application.properties
    - require:
        - deploy_mvp

deploy_mvp_db_config:
  file.managed:
    - name: /opt/elvaco/mvp-{{ mvp_version }}/config/application-postgresql.properties
    - source: salt://mvp/app/files/mvp/application-postgresql.properties.jinja
    - template: jinja
    - require:
        - deploy_mvp

deploy_mvp_systemd:
  file.managed:
    - name: /lib/systemd/system/{{ mvp_systemd_unit }}
    - source: salt://mvp/app/files/mvp/{{ mvp_systemd_unit }}
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /lib/systemd/system/{{ mvp_systemd_unit }}
  service.running:
    - name: {{ mvp_systemd_unit }}
    - enable: True
    - require:
      - create_mvp_symlink
      - deploy_mvp_config
      - deploy_mvp_db_config
    - watch:
      - file: /lib/systemd/system/{{ mvp_systemd_unit }}
      - file: /opt/elvaco/mvp-{{ mvp_version }}/config/application.properties
      - file: /opt/elvaco/mvp-{{ mvp_version }}/config/application-postgresql.properties

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
