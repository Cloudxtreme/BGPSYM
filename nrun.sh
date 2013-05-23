#!/bin/sh

JAVA_HOME=/opt/jdk1.6.0_04
#-XX:+UseConcMarkSweepGC 
#-XX:+UseParallelGC 
#-XX:+PrintGCDetails 

CP=".:output:lib/xpp3_min-1.1.3.4.O.jar:lib/xstream-1.2.2.jar:lib/log4j-1.2.15.jar"

$JAVA_HOME/bin/java -server -Xmx3100m -enableassertions -XX:+UseConcMarkSweepGC \
	-Djava.rmi.server.codebase=file:///home/wojciech/MSC-SYM01/output/ -cp $CP nl.nlnetlabs.bgpsym01.main.rmi.RMIStart $@

