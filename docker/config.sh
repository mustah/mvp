#!/usr/bin/env bash
sed -i "s/#log_statement = 'none'/log_statement = 'all'/g" \
/var/lib/postgresql/data/postgresql.conf
