# Staging

These steps needs to be taken in order to successfully deploy to a newly created staging server:

## In Gitlab

All staging server specific details, such as the SSH user, hostname and password, should be entered at [Gitlab settings for the MVP project](http://gitlab.elvaco.local/elvaco/mvp/settings/ci_cd).

You then refer to those variables in .gitlab-ci.yml just as if they were environment variables. Like this: `echo $WEB_STAGING_SSH_PRIVATE_KEY`, if `WEB_STAGING_SSH_PRIVATE_KEY` is a variable that you created.

Check the .gitlab-ci.yml file for currently used credential-variables. Hint: look at the `deploy_staging` job.

## On the staging server

### Preparing a user

If the user does not already exist on the staging server, these are the steps you should take:

SSH to the staging server (ask someone for the credentials if you do not know them)

    sudo -s
    mkdir -p /opt/elvaco
    useradd --create-home --home-dir /opt/elvaco/mvp --shell /bin/bash elvaco_mvp
    su -l elvaco_mvp
    ssh-keygen -b 4096 -f ~/.ssh/id_rsa -C "Elvaco MVP user" -o -a 500
    cat ~/.ssh/id_rsa

Take the output of the previous command and paste it as a value for the key *WEB_STAGING_SSH_PRIVATE_KEY* into the [Gitlab settings for the MVP project](http://gitlab.elvaco.local/elvaco/mvp/settings/ci_cd)

### Installing Java 8

1. SSH to the staging server (ask someone for the credentials if you do not know them)

    sudo -s
    apt-add-repository -y ppa:webupd8team/java
    apt-get update
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections
    apt-get install -y oracle-java8-installer
    java -version # should display "1.8...."

### Preparing systemd service

1. SSH to the staging server (ask someone for the credentials if you do not know them)
1. Login as root
1. Create `/etc/sudoers.d/elvaco_mvp` and fill it with this content:

    %elvaco_mvp ALL=NOPASSWD: /bin/systemctl daemon-reload
    %elvaco_mvp ALL=NOPASSWD: /bin/systemctl enable elvaco-mvp
    %elvaco_mvp ALL=NOPASSWD: /bin/systemctl stop elvaco-mvp
    %elvaco_mvp ALL=NOPASSWD: /bin/systemctl restart elvaco-mvp
    %elvaco_mvp ALL=NOPASSWD: /bin/systemctl start elvaco-mvp
    %elvaco_mvp ALL=NOPASSWD: /bin/mv /opt/elvaco/mvp/elvaco-mvp.service /etc/systemd/system

This will allow us to autostart the newly deployed artifact.

### Allowing our `elvaco_mvp` user to bind to port 443

1. As root on the server

    apt install authbind
    touch /etc/authbind/byport/443
    chmod 500 /etc/authbind/byport/443
    chown elvaco_mvp:elvaco_mvp /etc/authbind/byport/443
