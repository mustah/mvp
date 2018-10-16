{% set module = "mvp" %}
{% set systemd_unit = "elvaco-" + module + ".service" %}
{% set mvp_branch = salt['pillar.get']('mvp-branch', 'master') %}
{% set module_version = salt['pillar.get']('mvp_version', 'UNKNOWN') %}

include:
  - docker
  - mvp.app.user

create_{{module}}_log_dir:
  file.directory:
    - name: /var/log/elvaco/{{module}}
    - user: mvp
    - group: mvp
    - mode: 755
    - makedirs: True

create_{{module}}_dir:
  file.directory:
    - name: /opt/elvaco/{{module}}-{{module_version}}/config
    - user: mvp
    - group: mvp
    - mode: 755
    - makedirs: True

create_{{module}}_symlink:
  file.symlink:
    - name: /opt/elvaco/{{module}}-current
    - target: /opt/elvaco/{{module}}-{{module_version}}
    - require:
        - create_{{module}}_dir

deploy_{{module}}_config:
  file.managed:
    - name: /opt/elvaco/{{module}}-{{module_version}}/config/application.properties
    - source: salt://mvp/app/files/{{module}}/application.properties
    - require:
        - create_{{module}}_dir

deploy_{{module}}_log_config:
  file.managed:
    - name: /opt/elvaco/{{module}}-{{module_version}}/config/logback-spring.xml
    - source: salt://mvp/app/files/{{module}}/logback-spring.xml
    - require:
        - create_{{module}}_log_dir

deploy_{{module}}_db_config:
  file.managed:
    - name: /opt/elvaco/{{module}}-{{module_version}}/config/application-postgresql.properties
    - source: salt://mvp/app/files/{{module}}/application-postgresql.properties.jinja
    - template: jinja
    - require:
        - create_{{module}}_dir

download_{{module}}_image:
  docker_image.present:
    - name: gitlab.elvaco.se:4567/elvaco/mvp/{{module}}:{{module_version}}

docker_{{module}}:
  docker_container.running:
    - name: evo
    - user: mvp
    - image: gitlab.elvaco.se:4567/elvaco/mvp/{{module}}:{{module_version}}
    - links: geoservice:geoservice
    - detach: True
    - dns: 10.120.1.10
    - dns_search: elvaco.local
    - ports: 8080/tcp
    - port_bindings:
      - 8080:8080
    - restart_policy: always
    - log_driver: journald
    - binds:
      - /opt/elvaco/{{module}}-current/config/:/app/config:ro
      - /var/log/elvaco/{{module}}/:/var/log/elvaco/{{module}}:rw

#deploy_mvp_systemd:
#  file.managed:
#    - name: /lib/systemd/system/{{ systemd_unit }}
#    - source: salt://mvp/app/files/mvp/{{ systemd_unit }}
#  module.wait:
#    - name: service.systemctl_reload
#    - watch:
#      - file: /lib/systemd/system/{{ systemd_unit }}
#  service.running:
#    - name: {{ systemd_unit }}
#    - enable: True
#    - require:
#      - create_mvp_symlink
#      - deploy_mvp_config
#      - deploy_mvp_log_config
#      - deploy_mvp_db_config
#    - watch:
#      - file: /lib/systemd/system/{{ systemd_unit }}
#      - file: /opt/elvaco/mvp-{{ module_version }}/config/application.properties
#      - file: /opt/elvaco/mvp-{{ module_version }}/config/application-postgresql.properties

{{module}}_version:
  grains.present:
    - value: {{ module_version }}
    - require:
      - docker_{{module}}
