#!/bin/bash

NAME=$1

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

if [ "x$NAME" == "x" ]; then
    echo "usage: $0 <file_on_open>" > /dev/stderr
    exit 1
fi

DIR_ON_OPEN="/home/maciek/public_html/plots/$NAME"

echo "---+++ $NAME"

echo "<verbatim>"
rsh open "cat $DIR_ON_OPEN/logfile.summary" 
echo "</verbatim>"

echo
echo

echo "<img width=\"640\" alt=\"\" src=\"http://open.nlnetlabs.nl/~maciek/plots/$NAME/signal.jpg\" height=\"480\" />"
echo "<img width=\"640\" alt=\"\" src=\"http://open.nlnetlabs.nl/~maciek/plots/$NAME/conv.jpg\" height=\"480\" />"

echo
