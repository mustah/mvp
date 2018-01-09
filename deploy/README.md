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

### During mockup/design phase

*Temporary solution, before November demo*

We want to run JSON server because it's easier to mock demo data with the live reload environment the front end requires.

Neither the "review app" logic nor the https://mvpstaging.elvaco.se services are working during this phase.

On the staging server, install Node and Yarn, as described by [frontend/README.md](../frontend/README.md).

Make sure that a port (any port) that is neither 8080 nor 443 is open, so that we can start a separate web server.

#### Steps to take when/if we EOL the mockup phase

- In the code
  - Remove the mockup related targets in *frontend/build.gradle*
- Communication
  - Email johanness.magnusson@unibase.se and tell him to close the port that was used for the JSON server.

### Installing Java 8

SSH to the staging server (ask someone for the credentials if you do not know them)

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
1. Create `/etc/sudoers.d/elvaco_mvp` and fill it with the content of [elvaco_mvp](elvaco_mvp).

This will allow us to autostart the newly deployed artifact.

### Allowing our `elvaco_mvp` user to bind to port 443

As root on the server

    apt install authbind
    touch /etc/authbind/byport/443
    chmod 500 /etc/authbind/byport/443
    chown elvaco_mvp:elvaco_mvp /etc/authbind/byport/443
