#!/bin/bash

# 1. check parameters <log_file> <results_dir> <open_dir>
# 2. run ./do.sh
# 3. scp results to open

check() {
    code=$1
    msg=$2
    if [ $code -ne 0 ]; then
        echo -n "code: $code" > /dev/stderr
        if [ "x$msg" == "x" ]; then
            echo > /dev/stderr
        else
            echo ", msg=$msg" > /dev/stderr
        fi
        exit $code
    fi
}


# constants
RESULTS_PREFIX="/var/scratch/mwi300/results_64"
LOGFILE_PREFIX="/home0/mwi300/bgpsym"
OPEN_PREFIX="/home/maciek/public_html/plots"
SERVER="open"
DATA_DIR="data"
LOGFILE_TMP="logfile.tmp"
LOGFILE_SUMMARY="logfile.summary"


LOGFILE=$1
RESULTS_DIR=$2
OPEN_DIR=$3

if [ "x$LOGFILE" == "x" -o "x$RESULTS_DIR" == "x" -o "x$OPEN_DIR" == "x" ]; then
    echo "$0 <log_file> <results_dir> <open_dir>" > /dev/stderr
    exit 1
fi


# if results dir is not an absolute path, add the prefix
if [ `basename $RESULTS_DIR` == $RESULTS_DIR ]; then
    RESULTS_DIR=$RESULTS_PREFIX/$RESULTS_DIR
fi
# the same for logfile
if [ `basename $LOGFILE` == $LOGFILE ]; then
    LOGFILE=$LOGFILE_PREFIX/$LOGFILE
fi
# the same for open
if [ `basename $OPEN_DIR` == $OPEN_DIR ]; then
    OPEN_DIR=$OPEN_PREFIX/$OPEN_DIR
fi

# generate the plots
. ./do.sh $RESULTS_DIR 1 || check $1 "unable to generate plots"

# get the log file and generate xml info
scp das3:$LOGFILE $LOGFILE_TMP || check $? "unable to get logfile"
grep -i -e "<mraiProc" -e "<flap" -e "<noise" -e "<iBGP" -e "<policyMulti" -e "<time" $LOGFILE_TMP > $LOGFILE_SUMMARY || check $? "unable to grep logfile"

# good jpges are the last ones
SIGNAL_JPG=`ls -t1 signal*jpg | head -1`
CONV_JPG=`ls -t1 conv*jpg | head -1`

if [ "x$SIGNAL_JPG" == "x" -o "x$CONV_JPG" == "x" ]; then
    check 12 "unable to find jpgs"
fi

echo "signal: $SIGNAL_JPG"
echo "conv: $CONV_JPG"

rsh $SERVER "mkdir -p $OPEN_DIR/$DATA/" || check $? "unable to create directories"
scp $SIGNAL_JPG $SERVER:$OPEN_DIR/signal.jpg || check $? "unable to scp signal.jpg"
scp $CONV_JPG $SERVER:$OPEN_DIR/conv.jpg || check $? "unable to scp conv.jpg"
scp -r "msg_data/" "$SERVER:$OPEN_DIR/$DATA/" || check $? "unable to scp data dir"



