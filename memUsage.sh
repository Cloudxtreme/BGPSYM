#!/bin/bash

usage() {
	echo "usage: $0 <nodes>"
}

NODES=$1

if [ "x$NODES" == "x" ]; then
	usage
	exit
fi

for node in $NODES; do
	MEM_INFO=`ssh $node head -2 /proc/meminfo | grep -o '[0-9]*'`
	MEM_TOTAL=`echo $MEM_INFO | awk '{ print $1 }'`
	MEM_FREE=`echo $MEM_INFO | awk '{ print $2 }'`
	MEM_USED=`expr $MEM_TOTAL - $MEM_FREE`
	USEPCT=`echo "scale=3; ($MEM_USED / $MEM_TOTAL) * 100" | bc -l`
	echo "$node: $USEPCT % $MEM_USED kb/$MEM_TOTAL kb"
done
