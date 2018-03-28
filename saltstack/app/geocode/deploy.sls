{% set module = "geocode" %}
{% set systemd_unit = "elvaco-" + module + ".service" %}
{% set mvp_branch = salt['pillar.get']('mvp-branch', 'master') %}
{% set remote_git_describe = '/usr/bin/remote-git-describe.sh' %}
{% set git_repository = 'git@gitlab.elvaco.se:elvaco/mvp.git' %}
{% set module_version = salt['cmd.run'](remote_git_describe + " " + git_repository  + " " + mvp_branch) %}
{% set artifact = module + "-" + module_version + ".tar" %}

include:
  - mvp.openjdk-8-jre
  - mvp.app.user

fetch_{{module}}_archive:
  file.managed:
    - name: /tmp/{{artifact}}
    - source: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/{{artifact}}
    - source_hash: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/{{artifact}}.sha1

create_new_{{module}}_dir:
  file.directory:
    - name: /opt/elvaco/{{module}}-{{ module_version }}/config
    - user: mvp
    - group: mvp
    - mode: 755
    - makedirs: True
    - require:
      - fetch_{{module}}_archive

deploy_{{module}}:
  archive.extracted:
    - name: /opt/elvaco
    - archive_format: tar
    - source: /tmp/{{artifact}}
    - source_hash: http://artifactory2.elvaco.local/artifactory/Elvaco/MVP/{{artifact}}.sha1
    - user: mvp
    - group: mvp
    - require:
      - create_new_{{module}}_dir

create_{{module}}_symlink:
  file.symlink:
    - name: /opt/elvaco/{{module}}-current
    - target: /opt/elvaco/{{module}}-{{ module_version }}
    - require:
      - deploy_{{module}}

deploy_{{module}}_config:
  file.managed:
    - name: /opt/elvaco/{{module}}-{{ module_version }}/config/application.properties
    - source: salt://mvp/app/files/{{module}}/application.properties
    - require:
        - deploy_{{module}}

deploy_{{module}}_db_config:
  file.managed:
    - name: /opt/elvaco/{{module}}-{{ module_version }}/config/application-postgresql.properties
    - source: salt://mvp/app/files/{{module}}/application-postgresql.properties.jinja
    - template: jinja
    - require:
        - deploy_{{module}}

deploy_{{module}}_systemd:
  file.managed:
    - name: /lib/systemd/system/{{ systemd_unit }}
    - source: salt://mvp/app/files/{{module}}/{{ systemd_unit }}
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /lib/systemd/system/{{ systemd_unit }}
  service.running:
    - name: {{ systemd_unit }}
    - enable: True
    - require:
      - create_{{module}}_symlink
      - deploy_{{module}}_config
      - deploy_{{module}}_db_config
    - watch:
      - file: /lib/systemd/system/{{ systemd_unit }}
      - file: /opt/elvaco/{{module}}-{{ module_version }}/config/application.properties
      - file: /opt/elvaco/{{module}}-{{ module_version }}/config/application-postgresql.properties

remove_{{module}}_archive:
  file.absent:
    - name: /tmp/{{artifact}}
    - require:
      - deploy_{{module}}_systemd

{{module}}_version:
  grains.present:
    - value: {{ module_version }}
    - require:
      - deploy_{{module}}_systemd
