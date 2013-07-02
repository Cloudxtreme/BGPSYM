#!/usr/bin/python
import os, os.path
import sys
import argparse
import time

__author__ = 'Jeffrey'

# Constants
DEFAULT_TOTAL_SLAVES = 48
DEFAULT_RUNTIME = "00:15:00"
DEFAULT_NUMBER_OF_PROPERTIES = 100


# Main program
parser = argparse.ArgumentParser(description='Perform experiments with BGPSYM')
parser.add_argument('-n', '--name', type=str, required=True, help="Name of graph")
parser.add_argument('-e', '--event', type=str, required=True, help="Name of event")
parser.add_argument('-s', '--slaves', type=int, default=DEFAULT_TOTAL_SLAVES, help="Number of compute nodes")
parser.add_argument('-t', '--time', type=str, default=DEFAULT_RUNTIME, help="Runtime of simulation")
parser.add_argument('-np', '--properties', type=int, default=DEFAULT_NUMBER_OF_PROPERTIES, help="Total number of property files")
args=parser.parse_args()

graphName = args.name
eventName = args.event
slaves = args.slaves
time = args.time
props = args.properties

directory = "~/experiments/%s/properties/%s/properties_" % (graphName, eventName)

#./magic_run.sh 48 ~/experiments/1k/properties/disconnect_link/properties_0.xml 00:15:00
for i in range(0,props):
  cmd = "~/BGPSYM/magic_run.sh %d %s%d.xml %s" % (slaves, directory, i, time)
  print(cmd)
  os.system(cmd)
