#!/bin/sh

JAVA_HOME=/opt/jdk1.6.0_04
#-XX:+UseConcMarkSweepGC 
#-XX:+UseParallelGC 
#-XX:+PrintGCDetails 
$JAVA_HOME/bin/java -server -Xmx3100m -enableassertions -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -cp .:output:lib/log4j-1.2.15.jar nl.nlnetlabs.bgpsym01.main.ThreadStart

