mvp_group:
  group.present:
    - name: mvp
    - gid: 4000

mvp_user:
  user.present:
    - name: mvp
    - fullname: MVP User
    - shell: /bin/bash
    - home:  /home/elvaco
    - uid: 4000
    - gid: 4000
    - groups:
      - mvp
      - docker
    - password: $1$1Nvnb.no$TfN/WL9yt8zfCD1.WDBBt/
    - hash_password: True
    - require:
      - mvp_group
