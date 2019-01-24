#!/bin/bash

if [ -z "$1" ]; then
 printf "Please supply param with path to a file, containing the facilities to delete"
else
 organisation_id='c57fed6d-ad3b-4560-962b-dc3151134bb3'
 auth='mvpadmin@elvaco.se:changeme'
 evo_url='http://localhost:8080/'

 while read facility
 do
  printf "\nLooking up meter with facility-id ${facility}\n"

  id=$(curl -s -u $auth -XGET "${evo_url}api/v1/meters?facility=${facility}&organisation=${organisation_id}" | jq '.["content"][0]["id"]')
  id=$(printf "$id" | sed -e 's/"//g')
  if [ -z "$id" ]; then
   printf "\nMeter with facility-id ${facility} not found"
  else
   printf "\nDeleting meter with id ${id}\n"
   curl -s -u $auth -XDELETE "${evo_url}api/v1/meters/${id}"
  fi
  printf "\n-------- NEXT ------"
 done <$1
fi
