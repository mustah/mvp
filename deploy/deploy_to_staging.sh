#!/bin/bash -e

# This script should support both staging and production in the future

# To use it locally, add contents of your ~/.ssh/id_rsa.pub to the server's user's ~/.ssh/authorized_keys

SSH_USER=$1
SSH_HOST=$2
STAGING_KEYSTORE_BASE64=$3
STAGING_KEYSTORE_PASSWORD_BASE64=$4
STAGING_KEYSTORE_ALIAS_BASE64=$5

# some variables have default values
: ${GIT_VERSION:=`git describe --abbrev=7`}
: ${DIST_FOLDER:="/opt/elvaco/mvp/dists"}
: ${DEPLOYED_DIST:="/opt/elvaco/mvp/current"}
: ${CONFIG_FOLDER:="$DEPLOYED_DIST/config"}
: ${KEYSTORE_FILENAME:="keystore_staging.p12"}
: ${SERVICE_FILE:="/opt/elvaco/mvp/elvaco-mvp.service"}
: ${PROPERTIES_FILENAME:="application-staging.properties"}
: ${MOCKUP_BASE_FOLDER:="/opt/elvaco/mvp-mockup"}
: ${MOCKUP_DIST_FOLDER:="$MOCKUP_BASE_FOLDER/dist"}
: ${MOCKUP_ARCHIVE:="/tmp/mvp-mockup.tar.gz"}

LOCAL_TAR=backend/build/distributions/mvp-$GIT_VERSION.tar

function p {
  echo "deploy: $1"
}

if [ ! -f "$LOCAL_TAR" ]; then
  p "The local artifact does not exist: '$LOCAL_TAR'"
  exit 1
fi


p "This SSH command acts as a gatekeeper with an almost no-op command"
p "If it fails, check:"
p " - is the staging server ($SSH_HOST) up?"
p " - do we have matching keys added for the $SSH_USER in Gitlab, and on the staging server?"

set -x
ssh "$SSH_USER@$SSH_HOST" mkdir -p $DIST_FOLDER
scp $LOCAL_TAR "$SSH_USER@$SSH_HOST:$DIST_FOLDER"
scp deploy/elvaco-mvp.service "$SSH_USER@$SSH_HOST:$SERVICE_FILE"
ssh "$SSH_USER@$SSH_HOST" << END
  set -ex
  tar -C $DIST_FOLDER -xf $DIST_FOLDER/mvp-$GIT_VERSION.tar
  rm -f $DEPLOYED_DIST
  ln -s $DIST_FOLDER/mvp-$GIT_VERSION $DEPLOYED_DIST
  mkdir -p $CONFIG_FOLDER
  cat <(base64 -d <(echo "$STAGING_KEYSTORE_BASE64")) > $DEPLOYED_DIST/$KEYSTORE_FILENAME
  printf "server.port=443\nserver.ssl.key-store=%s\nserver.ssl.key-store-password=%s\nserver.ssl.key-alias=%s\nserver.ssl.key-store-type=%s\n" "$KEYSTORE_FILENAME" $(base64 -d <(echo "$STAGING_KEYSTORE_PASSWORD_BASE64")) $(base64 -d <(echo "$STAGING_KEYSTORE_ALIAS_BASE64")) "PKCS12" > $CONFIG_FOLDER/$PROPERTIES_FILENAME
  sudo /bin/mv $SERVICE_FILE /etc/systemd/system
  sudo /bin/systemctl daemon-reload
  sudo /bin/systemctl enable elvaco-mvp
  sudo /bin/systemctl restart elvaco-mvp
END

function deploy_mockup {
scp frontend/mockup/elvaco-mvp-mockup*.tar.gz "$SSH_USER@$SSH_HOST:$MOCKUP_ARCHIVE"
ssh "$SSH_USER@$SSH_HOST" << END
  set -ex
  rm -rf $MOCKUP_DIST_FOLDER
  mkdir $MOCKUP_DIST_FOLDER
  cd $MOCKUP_DIST_FOLDER
  tar zxvf $MOCKUP_ARCHIVE
  sudo /bin/mv $MOCKUP_DIST_FOLDER/elvaco-mvp-mockup.service /etc/systemd/system
  sudo /bin/systemctl daemon-reload
  sudo /bin/systemctl enable elvaco-mvp-mockup
  sudo /bin/systemctl restart elvaco-mvp-mockup
  rm -rf $MOCKUP_ARCHIVE
END
}

deploy_mockup
