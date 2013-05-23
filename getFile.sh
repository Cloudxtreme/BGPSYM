#!/bin/sh
if [ "x$1" == "x" ]; then
    FILE=`ls -t1 log* | head -1`
else
    FILE=$1
fi
echo $FILE

grep -i -e "<mraiProc" -e "<flap" -e "<noise" -e "<iBGP" -e "<policyMulti" -e "<time" $FILE

grep -o -i -e "ANN" -e " overloading" -e "UNoverloading" -e "WARN" -e "ERROR" -e "finished" -e closed -e "second for msg_[123456]..." -e "flap.*msg_[123456]..." $FILE | sort | uniq -c

