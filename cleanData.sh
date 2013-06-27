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
rm -rf 2013*
echo -e "Remove gc files"
cd ~/env/log
rm gc_*
echo -e "Remove java log files"
cd ~/BGPSYM/logs/
rm log_*
echo -e "Done"
