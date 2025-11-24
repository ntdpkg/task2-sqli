#!/bin/bash

echo "Starting MySQL..."
service mysql start

sleep 5

echo "Configuring Database..."
mysql -e "CREATE DATABASE IF NOT EXISTS todotask_db;"
mysql -e "CREATE USER IF NOT EXISTS 'todotask_user'@'localhost' IDENTIFIED BY 'todotask_passwd';"
mysql -e "GRANT ALL PRIVILEGES ON todotask_db.* TO 'todotask_user'@'localhost';"
mysql -e "FLUSH PRIVILEGES;"
mysql -e "GRANT FILE ON *.* TO 'todotask_user'@'localhost';"

echo "MySQL started and configured."

echo "Starting Tomcat..."
/usr/local/tomcat/bin/catalina.sh run