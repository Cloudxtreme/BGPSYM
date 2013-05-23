#!/bin/bash

PORT=$1
NODE=`find $HOME/.gmpi | tail -1 | xargs head -1 | grep -o "node..."`

echo "NODE=$NODE, port=$PORT"

telnet $NODE $PORT
