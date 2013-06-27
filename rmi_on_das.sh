#!/bin/bash
if [ -f "$HOME/.javarc" ]; then
	. "$HOME/.javarc"
fi
#JAVA_HOME=/opt/jdk1.6.0_04
#-XX:+UseConcMarkSweepGC 
#-XX:+UseParallelGC 
#-XX:+PrintGCDetails 


STORAGE_DIR="/local/$USER/data/*"
rm -rf $STORAGE_DIR

INST_COUNT=1
DEFAULT_XMX=16g
DEFAULT_NEW=64m
DEFAULT_FRACTION=60

WORKING_DIR=`dirname $3`

if [ "$MEMORY" == "" ]; then
	MEMORY="-Xmx$DEFAULT_XMX -Xms$DEFAULT_XMX -XX:NewSize=$DEFAULT_NEW -XX:MaxNewSize=$DEFAULT_NEW"
fi

CP="lib/xmlpull-1.1.3.1.jar:lib/xpp3_min-1.1.4c.jar:lib/xstream-1.4.4.jar:lib/log4j-1.2.15.jar:lib/compress.jar:lib/commons-compress-1.5.jar"
CP=".:src:$CP:lib/bgpsym.jar"

PWD=`pwd`
LOG4J_PROPS="/home/$USER/experiments/log4j.properties"

#-XX:+UseConcMarkSweepGC
LOG4J=""
if [ -f "$LOG4J_PROPS" ]; then
	LOG4J="-Dlog4j.configuration=file:$LOG4J_PROPS"
else
	echo "$LOG4J_PROPS not found"
	exit 2
fi

debug=1
DEBUG=""


#COLLECTOR="-XX:+UseParallelGC -XX:MaxGCPauseMillis=500"
#COLLECTOR="-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10"
#COLLECTOR="-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSIncrementalSafetyFactor=25 -XX:CMSIncrementalDutyCycleMin=25 -XX:-CMSIncrementalPacing -XX:CMSIncrementalDutyCycle=40"
COLLECTOR="-XX:+UseConcMarkSweepGC -XX:SurvivorRatio=16 -XX:CMSInitiatingOccupancyFraction=$DEFAULT_FRACTION -XX:+UseCMSInitiatingOccupancyOnly -XX:MaxTenuringThreshold=0 -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+HeapDumpOnOutOfMemoryError" 

MYNUM=$1

for i in `seq 1 $INST_COUNT`; do
	NUM=$[MYNUM+i-1]
	if [ "x$debug" == "x1" ]; then
		DEBUG="-XX:+PrintGCDetails -verbose:gc -XX:+PrintGCTimeStamps -Xloggc:/var/scratch/$USER/gc/gc_logfile_$NUM"
	fi
	java $MEMORY -server -enableassertions  -cp $CP $LOG4J -XX:+DisableExplicitGC $DEBUG $COLLECTOR  nl.nlnetlabs.bgpsym01.main.tcp.TCPStart $NUM $2 $3 2> "logs/log_$NUM" &
	sleep 0.5
done
