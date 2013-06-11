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

CP="lib/xmlpull-1.1.3.1.jar:lib/xpp3_min-1.1.4c.jar:lib/xstream-1.4.4.jar:lib/log4j-1.2.15.jar:lib/compress.jar"
CP=".:src:$CP:lib/bgpsym.jar"

WORKING_DIR=`dirname $2`
LOG4J=""
if [ -f "$WORKING_DIR/log4j.properties" ]; then
	LOG4J="-Dlog4j.configuration=file://$WORKING_DIR/log4j.properties"
else
	echo "$WORKING_DIR/log4j.properties not found"
	exit 2
fi

PWD=`pwd`

DEBUG="-XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps -Xloggc:$WORKING_DIR/log/gc_logfile_coordinator"
#-XX:+UseConcMarkSweepGC
java $MEMORY -Xmx16g -Xms16g -XX:NewSize=700m -XX:MaxNewSize=700m -server $DEBUG -enableassertions  -cp $CP $LOG4J -XX:+UseConcMarkSweepGC nl.nlnetlabs.bgpsym01.coordinator.CoordinatorMain $1 $2 2> "logs/coordinator" &
