#!/bin/sh

NAME="log"

if [ "x$1" != "x" ]; then
    BASE=`basename $1`
    NAME="${NAME}_${BASE}"
fi

DATE=`date "+%Y%d%m_%H:%M:%S"`

NAME="logs/${NAME}_${DATE}.log"

echo $NAME
