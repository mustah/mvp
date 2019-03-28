#!/bin/bash

## usage
## cat organisations.txt |xargs -n 1 -P 16 ./update_measurement_stat.sh 2019-02-24 5
## mvpdb-app mvpdb

export PGPASSWORD=mvp-password

for i in `seq 1 $2`;
do
 A=$(date -d "$1 $i days" +%Y-%m-%d);
 echo "Running: $A - $3"
 psql -h localhost -U mvpdb-app -d mvpdb -f update_one_org_one_day.sql -v DATE="'$A'::date" -v ORG="'$3'::uuid" > /dev/nu$
 RET=$?

 if [ $RET -gt 0 ]; then
   echo "$A - $3 FAILED"
 else
   echo "$A - $3 completed"
 fi

done

