#!/bin/bash

EXC="--exclude=\"*.properties\""

HOST="das3"

if [ "x$1" != "x" ]; then
	HOST=$1
fi

#if [ "$1" == "all" ]; then
	#EXC=""
#fi

rsync --rsh=ssh -av --delete --exclude="log*" --exclude="*.properties" --exclude="*.swp" --exclude=".*" --exclude="*.class" --exclude="bgpsym.jar" . $HOST:bgpsym

ssh $HOST "cd bgpsym && ant"
date
