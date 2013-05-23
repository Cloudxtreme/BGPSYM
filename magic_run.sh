#!/bin/sh

usage() {
    echo "usage: $0 <hosts_count> <properties_file> <time>" > /dev/stderr
}

cd `dirname $0`

if [ "x$1" == "x" -o "x$2" == "x" -o "x$3" == "x" ]; then
    usage
    exit
fi

hosts=$1
properties=$2
time=$3


./run-prun.sh $hosts $properties -t $time 2>&1 | tee `./getLogFileName.sh $properties`
