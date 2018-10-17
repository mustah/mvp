{% set module = "geoservice" %}
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

shutdown_{{module}}_systemd:
  service.dead:
    - name: {{systemd_unit}}
    - require:
      - download_{{module}}_image

docker_{{module}}:
  docker_container.running:
    - name: {{module}}
    - user: mvp
    - image: gitlab.elvaco.se:4567/elvaco/mvp/{{module}}:{{module_version}}
    - detach: True
    - dns: 10.120.1.10
    - dns_search: elvaco.local
    - ports: 8081/tcp
    - port_bindings:
      - 8081:8081
    - restart_policy: always
    - log_driver: journald
    - log_opt: tag={{module}}
    - binds:
      - /opt/elvaco/{{module}}-current/config/:/app/config:ro

{{module}}_version:
  grains.present:
    - value: {{ module_version }}
    - require:
      - docker_{{module}}
