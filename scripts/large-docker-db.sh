#!/bin/bash
set -ex
interval="1 weeks"
if [ -n "$1" ]; then
  interval="$1"
fi

d=$(date --iso-8601)
prefix="/tmp/staging-dump-${d}"
container_name="postgresql-staging-backup-1w"
postgres_port="2345"
PGPASS_TEMPFILE=$(mktemp)

# Run command inside docker container
function run_sql_in_container() {
  time docker exec -i ${container_name} psql -h localhost -U mvp -c "$1" mvp
}

# Dump remote table to file and stream it over ssh
# You need ssh keys to staging environment for this to work.
# Arg1 = table name
# Arg2 = the column to do date compare on
function dump_table() {
  table=$1
  column=$2
  if [ ! -f "${prefix}-${table}.csv" ]; then
    time ssh 'administrator@elvsealidb02' bash <<EOSSH |
      echo "\copy (select * from ${table} where ${column} < '${d}' and ${column} > ('${d}'::timestamptz - '${interval}'::interval)) to stdout with csv" | psql -h localhost -U mvpdb-app mvpdb | gzip --stdout
EOSSH
      gunzip > "${prefix}-${table}.csv"
  else
    echo "Using cached file: ${prefix}-${table}.csv"
  fi
}

# Load csv export into local docker db.
function load_to_database() {
  table=$1
  echo "SET session_replication_role = replica; \copy ${table} from '${prefix}-${table}.csv' delimiter ',' csv" | \
   PGPASSFILE=${PGPASS_TEMPFILE} time psql -h localhost -p ${postgres_port} mvp mvp
}

# Dump remote db to file and stream it over ssh
# You need ssh keys to staging environment for this to work.
function dump_structure() {
  if [ ! -f "${prefix}.gz" ]; then
    #requires ssh keys to be deployed on target host
  time ssh 'administrator@elvsealidb02' 'pg_dump -v -Fc --exclude-table-data "measurement_stat_data" --exclude-table-data "measurement" --exclude-table-data "missing_measurement" -h localhost -U mvpdb-app mvpdb' > "${prefix}.gz"
  else
    echo "Using cached file: ${prefix}.gz"
  fi
}

# Wrapper function to start docker container
function start_container() {
  time docker run -d --name ${container_name} -p ${postgres_port}:5432 gitlab.elvaco.se:4567/elvaco/mvp/postgresql:latest postgres
  sleep 5 # :face_with_rolling_eyes:0
}

# Wrapper function to restore db dump
function restore_structure() {
  time docker exec -i "${container_name}" pg_restore -v -d mvp -U mvp < "${prefix}.gz"
}

# Wrapper function to create a postgresql password file.
function create_pgpassfile() {
  echo "localhost:${postgres_port}:mvp:mvp:mvp" > "${PGPASS_TEMPFILE}"
}

# == Main ==
create_pgpassfile
dump_structure
start_container

# Pre fetch data stuff
dump_table measurement readout_time
dump_table measurement_stat_data stat_date

# Setup initial users and db
run_sql_in_container 'CREATE ROLE "mvpdb-admin";CREATE ROLE "mvpdb-app"';
restore_structure

# load data to db
load_to_database  measurement
load_to_database  measurement_stat_data
run_sql_in_container "CREATE USER appuser WITH ENCRYPTED PASSWORD 'RWZ8tKUzDdbncg2J';"
run_sql_in_container "update mvp_user set password = 'da20c7f55c151330cfcf04e8e74fe6d5f848ab09a1415d0e39d655ab0bc7f75d239dbbf54b5c6718';"
run_sql_in_container 'SET session_replication_role = DEFAULT';
