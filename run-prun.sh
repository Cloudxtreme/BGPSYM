#!/bin/sh

usage() {
	echo "./$0 <proc_count> <xml_file> [options]" > /dev/stderr
	exit 1
}

if [ "x$1" == "x" -o "x$2" == "" -o "x$3" == "" ]; then
	usage
fi

# where is our working dir
PROPERTIES_FILE=$2

# how many compute nodes do we want
NODES_COUNT=$1
NODES_COUNT=$[NODES_COUNT+1]

shift
shift
OPTIONS=$@

echo -e "Starting BGPSIM"
echo -e "Properties file: $WORKING_DIR"
echo -e "Number of nodes: $NODES_COUNT"
echo -e "Options: $OPTIONS\n\n"

prun $OPTIONS -v -1 -np $NODES_COUNT -sge-script sge-script /bin/ls $PROPERTIES_FILE 2>&1
