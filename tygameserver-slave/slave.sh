#!/bin/sh
echo start to run slave
nohup java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8700,suspend=n -jar target/tygameserver-slave-0.0.1-SNAPSHOT.jar > slave.log 2>&1 &
PID=$!
echo $PID > master.pid
tail -f slave.log
