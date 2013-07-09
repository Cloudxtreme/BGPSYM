#!/usr/bin/python
import os, os.path
import sys
import argparse
import time
from time import sleep

__author__ = 'Jeffrey'

# Constants
DEFAULT_TOTAL_SLAVES = 48
DEFAULT_RUNTIME = "00:15:00"
DEFAULT_NUMBER_OF_PROPERTIES = 100
SCR_RESULTS_DIR = "/var/scratch/jlf200/results/"
BGPSYM_PATH = "/home/jlf200/BGPSYM/"
REMOTE_RESULTS_DIR = "/home/jeffrey/data"

# Main program
parser = argparse.ArgumentParser(description='Perform experiments with BGPSYM')
parser.add_argument('-g', '--graph', type=str, required=True, help="Name of graph")
parser.add_argument('-e', '--event', type=str, required=True, help="Name of event")
parser.add_argument('-s', '--slaves', type=int, default=DEFAULT_TOTAL_SLAVES, help="Number of compute nodes")
parser.add_argument('-t', '--time', type=str, default=DEFAULT_RUNTIME, help="Runtime of simulation")
parser.add_argument('-np', '--properties', type=int, default=DEFAULT_NUMBER_OF_PROPERTIES, help="Total number of property files")
args=parser.parse_args()

graphName = args.graph
eventName = args.event
slaves = args.slaves
timeDuration = args.time
props = args.properties

directory = "~/experiments/%s/properties/%s/properties_" % (graphName, eventName)

def getLatestDir (path):
  print(os.listdir(path))
  all_subdirs = [d for d in os.listdir(path) if os.path.isdir(d)]
  return max(all_subdirs, key=os.path.getmtime)

#./magic_run.sh 48 ~/experiments/1k/properties/disconnect_link/properties_0.xml 00:15:00
for i in range(0,props):
  cmd = "%smagic_run.sh %d %s%d.xml %s" % (BGPSYM_PATH, slaves, directory, i, timeDuration)
  print(cmd)
  os.system(cmd)

  print("Results directory: %s" % (SCR_RESULTS_DIR))
   
  os.chdir(SCR_RESULTS_DIR)
  latestDir = getLatestDir(".")
  localDir = "%s%s" % (SCR_RESULTS_DIR, latestDir)
  remoteDir = "%s/%s/%s/%s" % (REMOTE_RESULTS_DIR, graphName, eventName, latestDir)
  cmd = "rsync -av --rsh=ssh %s/tars/ area51:%s > %s/logs/rsync_%d 2>&1" % (localDir, remoteDir, BGPSYM_PATH, i)

  print("Local directory: %s" % localDir)
  print("Remote directory: %s" % remoteDir)
  print("Sync command: %s" % cmd)

  os.chdir(localDir)
  os.mkdir('tars')
  os.system("find . -type f | while read filename; do tar -czvf \"tars/$filename\".tar.gz \"$filename\" && rm \"$filename\"; done > %s/logs/gzip_%d 2>&1" % (BGPSYM_PATH, i))

  os.system("ssh area51 'mkdir -p %s'" % remoteDir)
  os.system(cmd)
  os.system("rm -rf %s" % (localDir))
  os.chdir(BGPSYM_PATH)
  sleep(60)
