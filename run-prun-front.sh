#!/bin/sh

usage() {
	echo "./$0 <proc_count> <xml_file> [options]" > /dev/stderr
	exit 1
}

if [ "x$1" == "x" -o "x$2" == "" -o "x$3" == "" ]; then
	usage
fi

# where is our working dir
WORKING_DIR=$2

# how many compute nodes do we want
NODES_COUNT=$1
NODES_COUNT=$[NODES_COUNT+1]

shift
shift
OPTIONS=$@

prun $OPTIONS -v -1 -np $NODES_COUNT -sge-script sge-front /bin/ls $WORKING_DIR 2>&1

