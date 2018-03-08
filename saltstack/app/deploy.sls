{% set mvp_systemd_unit = "elvaco-mvp.service" %}

include:
  - mvp.openjdk-8-jre
  - mvp.app.user

install_remote_git_describe:
  file.managed:
    - name: /usr/bin/remote-git-describe.sh
    - user: root
    - group: root
    - mode: 755

{% set mvp_version = salt['cmd.run']('/usr/bin/remote-git-describe.sh git@gitlab.elvaco.se:elvaco/mvp.git mr-salt-install-tar') %}

fetch_mvp_archive:
  cmd.run:
    - name: curl -O http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar
    - cwd: /tmp
    - runas: mvp
    - require:
      - install_remote_git_describe

fetch_mvp_sha1:
  cmd.run:
    - name: curl -O http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/mvp-{{ mvp_version }}.tar.sha1
    - cwd: /tmp
    - runas: mvp
    - require:
      - install_remote_git_describe

create_new_mvp_dir:
  file.directory:
    - name: /opt/elvaco/mvp-{{ mvp_version }}/config
    - user: mvp
    - group: mvp
    - mode: 755
    - makedirs: True
    - require:
      - fetch_mvp_archive
      - fetch_mvp_sha1

deploy_mvp:
  archive.extracted:
    - name: /opt/elvaco
    - archive_format: tar
    - source: /tmp/mvp-{{ mvp_version }}.tar
    - source_hash: /tmp/mvp-{{ mvp_version }}.tar.sha1
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
    - source: salt://mvp/app/files/application.properties
    - require:
        - deploy_mvp

deploy_mvp_db_config:
  file.managed:
    - name: /opt/elvaco/mvp-{{ mvp_version }}/config/application-postgresql.properties
    - source: salt://mvp/app/files/application-staging.properties
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

remove_mvp_sha1:
  file.absent:
    - name: /tmp/mvp-{{ mvp_version }}.tar.sha1
    - require:
      - deploy_mvp_systemd

mvp_version:
  grains.present:
    - value: {{ mvp_version }}
    - require:
      - deploy_mvp_systemd
