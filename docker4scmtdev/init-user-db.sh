#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE USER scmt WITH PASSWORD 'scmtsupersecret';
  CREATE DATABASE scmt WITH OWNER scmt;
EOSQL