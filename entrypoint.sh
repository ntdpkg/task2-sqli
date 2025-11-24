#!/bin/bash

# 1. Khởi động MySQL Service (Background)
echo "Starting MySQL..."
service mysql start

# Chờ MySQL khởi động
sleep 5

# 2. Cấu hình Database & User
# Vì chạy local, user root không cần pass, ta tạo user cho app
echo "Configuring Database..."
mysql -e "CREATE DATABASE IF NOT EXISTS todotask_db;"
mysql -e "CREATE USER IF NOT EXISTS 'todotask_user'@'localhost' IDENTIFIED BY 'todotask_passwd';"
mysql -e "GRANT ALL PRIVILEGES ON todotask_db.* TO 'todotask_user'@'localhost';"
mysql -e "FLUSH PRIVILEGES;"
# Quan trọng: Cấp quyền FILE cho user này để dùng được INTO OUTFILE
mysql -e "GRANT FILE ON *.* TO 'todotask_user'@'localhost';"

echo "MySQL started and configured."

# 3. Khởi động Tomcat (Foreground để giữ container chạy)
echo "Starting Tomcat..."
/usr/local/tomcat/bin/catalina.sh run