#!/bin/sh

echo $USER

echo -e "Removing logs"
cd /var/scratch/$USER/log/
rm *
echo -e "Removing data"
cd /var/scratch/$USER/data/
rm *
echo -e "Removing results"
cd /var/scratch/$USER/results/
rm -rf *
echo -e "Returning"
cd ~/MSC-SYM012
