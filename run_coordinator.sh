#!/bin/bash

if [ -f "$HOME/.javarc" ]; then
	. "$HOME/.javarc"
fi
#JAVA_HOME=/opt/jdk1.6.0_04
#-XX:+UseConcMarkSweepGC 
#-XX:+UseParallelGC 
#-XX:+PrintGCDetails 

DEFAULT_XMX=24g
DEFAULT_NEW=512m

if [ "$MEMORY" == "" ]; then
	MEMORY="-Xmx$DEFAULT_XMX -Xms$DEFAULT_XMX -XX:NewSize=$DEFAULT_NEW -XX:MaxNewSize=$DEFAULT_NEW"
fi

CP="lib/xmlpull-1.1.3.1.jar:lib/xpp3_min-1.1.4c.jar:lib/xstream-1.4.4.jar:lib/log4j-1.2.15.jar:lib/compress.jar:lib/commons-compress-1.5.jar"
CP=".:src:$CP:lib/bgpsym.jar"

WORKING_DIR=`dirname $2`
LOG4J_PROPS="/home/$USER/experiments/log4j.properties"
LOG4J=""

if [ -f "$LOG4J_PROPS" ]; then
	LOG4J="-Dlog4j.configuration=file:$LOG4J_PROPS"
else
	echo "$LOG4J_PROPS not found"
	exit 2
fi

PWD=`pwd`

DEBUG="-XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps -Xloggc:/var/scratch/$USER/gc/gc_logfile_coordinator"
#-XX:+UseConcMarkSweepGC
java $MEMORY -server $DEBUG -enableassertions  -cp $CP $LOG4J -XX:+UseConcMarkSweepGC nl.nlnetlabs.bgpsym01.coordinator.CoordinatorMain $1 $2 2> "logs/coordinator" &
