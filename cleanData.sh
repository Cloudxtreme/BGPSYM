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
echo -e "Removing gc files"
cd /var/scratch/$USER/gc/
rm gc_*
echo -e "Remove java log files"
cd ~/BGPSYM/logs/
rm log_*
rm gzip_*
rm rsync_*
echo -e "Done"
