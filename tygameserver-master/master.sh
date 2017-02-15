#!/bin/sh
echo start to run master
nohup java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8600,suspend=n -jar target/tygameserver-master-0.0.1-SNAPSHOT.jar > master.log 2>&1 &
PID=$!
echo $PID > master.pid
tail -f master.log
