#!/bin/bash

DIR=$1
RUNDIR=$2
HOSTS=$3
TIME=$4

if [ "x$DIR" == "x" -o "x$RUNDIR" == "x" -o "x$HOSTS" == "x" -o "x$TIME" == "x" -o ! -d "$DIR" ]; then
    echo "$0 <dir> <run_dir> <hosts> <time>"
    exit 1
fi

LOCAL_FILE="$RUNDIR/properties_rerun.xml"
SCRIPT="./magic_run.sh"
SLEEP_TIME=100
INFO=properties.info

for dir in $DIR/*; do
    file=$dir/$INFO
    if [ ! -f "$file" ]; then
        echo "file $file does not exist..."
        continue
    fi

    cp $file $LOCAL_FILE
    echo "running $SCRIPT $HOSTS $file $TIME"
    $SCRIPT $HOSTS $LOCAL_FILE $TIME
    echo "sleeeping"
    sleep $SLEEP_TIME
done
