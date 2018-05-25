#!/bin/sh

set -ex
APP_USER=appuser
ADMIN_USER=mvp
APPDB=mvp

echo "CREATE USER $APP_USER WITH ENCRYPTED PASSWORD 'RWZ8tKUzDdbncg2J';"
echo "GRANT CONNECT ON DATABASE $APPDB TO $APP_USER;"
cat $1 | sed -e "s/mvpdb-app/${APP_USER}/g" | sed -e "s/mvpdb-admin/${ADMIN_USER}/g" | sed -e "s/mvpdb/${APPDB}/g"
echo "update mvp_user set password='da20c7f55c151330cfcf04e8e74fe6d5f848ab09a1415d0e39d655ab0bc7f75d239dbbf54b5c6718'"
