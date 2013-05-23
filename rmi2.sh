#!/bin/bash

if [ -f "$HOME/.javarc" ]; then
	. "$HOME/.javarc"
fi
#JAVA_HOME=/opt/jdk1.6.0_04
#-XX:+UseConcMarkSweepGC 
#-XX:+UseParallelGC 
#-XX:+PrintGCDetails 

if [ "$MEMORY" == "" ]; then
	MEMORY="-Xmx3000m"
fi

CP="lib/xpp3_min-1.1.3.4.O.jar:lib/xstream-1.2.2.jar:lib/log4j-1.2.15.jar"
if [ "$2" == "ant" ]; then
	CP=".:src:$CP:lib/bgpsym.jar"
else
	CP=".:output:$CP"
fi

pids=""

pids="$!"

size=$1
size=$[size-1]
seq=`seq 1100 $[1100+size]`
for i in $seq; do
	rmiregistry $i &
	pids="$! $pids"
done

for i in `seq 0 $size`; do
	$JAVA_HOME/bin/java $MEMORY -server -Djava.rmi.server.codebase=file:///home/wojciech/MSC-SYM01/output/ -enableassertions -XX:+UseConcMarkSweepGC -cp $CP  nl.nlnetlabs.bgpsym01.main.rmi.RMIStart $i /dev/shm/registries.xml /dev/shm/nodes.xml &
	pids="$! $pids"
done

function cleanup {
	for i in $pids; do
		echo "killing $i"
		kill $i
	done
	exit 0
}

trap "cleanup" SIGINT SIGTERM

while true; do
	sleep 3600
done

