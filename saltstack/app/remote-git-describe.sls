install_remote_git_describe:
  file.managed:
    - name: /usr/bin/remote-git-describe.sh
    - user: root
    - group: root
    - mode: 755
