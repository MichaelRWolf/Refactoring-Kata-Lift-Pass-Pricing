#! /bin/bash

../runLocalDatabases.sh
mysql -u root -p mysql < ./database/initDatabase.sql


