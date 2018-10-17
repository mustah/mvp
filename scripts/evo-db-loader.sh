#!/bin/bash
pg_dump_path='./dump.gz'
container_name='prod-restore'
container_postgresql_port='5432'
dump_host=''
function print_vars() {
  echo "Dump path:        ${pg_dump_path}"
  echo "Container name:   ${container_name}"
  echo "Container port:   ${container_postgresql_port}"
  if [ -n "$dump_host" ] && [ ! -f ${pg_dump_path} ]; then
    echo "Taking dump from: ${dump_host}"
  fi
}

function wait_for_postgresql() {
  RETRIES=30
  until docker exec -i $container_name psql -h localhost -U mvp -c 'select 1;' mvp > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do
    echo -n "."
    RETRIES=$((RETRIES--))
    sleep 1
  done
}

function run_sql_in_container() {
  docker exec -i $container_name psql -h localhost -U mvp -c "$1" mvp
}
function print_help() {
  echo "Usage:" >&2
  echo "$0 [-h] [-d dump_path] [-H [user@]hostname] [-n container_name] [-p container_postgresql_port]" >&2
}

function cleanup_docker_and_bail() {
  echo "Bailing out." >&2
  docker stop "$container_name" >/dev/null
  docker rm "$container_name" >/dev/null
  exit 1
}

while getopts ":hd:H:n:p:" opt; do
  case $opt in
    d)
      pg_dump_path=$OPTARG
      ;;
    H)
      dump_host=$OPTARG
      ;;
    n)
      container_name=$OPTARG
      ;;
    p)
      container_postgresql_port=$OPTARG
      ;;
    h)
      print_help
      exit 1
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      print_help
      exit 1
      ;;
  esac
done
print_vars

if [ ! -f "$pg_dump_path" ] && [ -n "$dump_host" ]; then
  echo "Running pg_dump on '$dump_host'... First enter any SSH credentials for '$dump_host', then enter database password for mvpdb-app:mvp"
  dump_time_start=$(date +%s)
  ssh "$dump_host" 'pg_dump -v -Fc -h localhost -U mvpdb-app mvpdb' > "$pg_dump_path"
  ret=$?
  dump_time_stop=$(date +%s)
  dump_runtime=$((dump_time_stop - dump_time_start))
  echo "Finished in ${dump_runtime}s."
  if [ $ret -ne 0 ]; then
    echo "Failed to retrieve database dump. Bailing out." >&2
    rm "$pg_dump_path"
    exit 1
  fi
fi
pg_dump_dir=$(dirname "$pg_dump_path")
pg_dump_filename=$(basename "$pg_dump_path")
image='gitlab.elvaco.se:4567/elvaco/mvp/postgresql:latest'

echo "Starting up docker container ${container_name} ..."
if ! docker run -d --name "$container_name" -e POSTGRES_USER=mvp -e POSTGRES_PASSWORD=mvp -p "$container_postgresql_port":5432 -v "$pg_dump_dir":/dump "$image" postgres -c 'archive_mode=off' -c 'wal_level=minimal' -c 'max_wal_senders=0'; then
  echo "Failed to start docker container" >&2
  docker rm "$container_name" >/dev/null
  exit 1
fi

echo "Waiting for PostgreSQL to accept connections"
wait_for_postgresql

echo "Setting up database roles ..."

if ! run_sql_in_container 'CREATE ROLE "mvpdb-admin";CREATE ROLE "mvpdb-app";'; then
  cleanup_docker_and_bail
fi

echo "Restoring dump ..."

restore_time_start=$(date +%s)
if ! docker exec -i "$container_name" pg_restore -v -d mvp -U mvp -j 4 "/dump/$pg_dump_filename"; then
  cleanup_docker_and_bail
fi
restore_time_stop=$(date +%s)
restore_runtime=$((restore_time_stop - restore_time_start))
echo "Finished in ${restore_runtime}s"

echo "Creating database user..."
run_sql_in_container "CREATE USER appuser WITH ENCRYPTED PASSWORD 'RWZ8tKUzDdbncg2J';"
echo "Resetting passwords of any mvp_user(s)..."
run_sql_in_container "update mvp_user set password = 'da20c7f55c151330cfcf04e8e74fe6d5f848ab09a1415d0e39d655ab0bc7f75d239dbbf54b5c6718';"
