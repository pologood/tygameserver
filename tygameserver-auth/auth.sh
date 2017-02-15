#!/bin/sh
echo start to run auth
nohup java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8500,suspend=n -jar target/tygameserver-auth-0.0.1-SNAPSHOT.jar > auth.log 2>&1 &
PID=$!
echo $PID > auth.pid
tail -f auth.log