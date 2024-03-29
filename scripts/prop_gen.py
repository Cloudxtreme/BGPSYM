#!/usr/bin/python

import getopt, sys
print sys.version
from string import Template

def usage():
    print """
-h --help
-u --user x
-h --hostCount x
-t --timeScaler x
-p --mraiProc x
-e --mraiEmpty x
-f --flapDistribution x
-m --policyMulti x
-i --iBgpMaxValue x
-n --iBgpMaxNeighbors x
"""

def getDefDict():
    return dict(timeScaler=30, mraiProc=70, mraiEmpty=0, flapDistribution=70, policyMulti=10, iBgpMaxValue=120000, iBgpMaxNeighbors=750, iBGPLog="true", hostCount=64, flapPercentage=70, noise=121000)

def main():
    dict = getDefDict()
    try:
        opts, args = getopt.getopt(sys.argv[1:], "t:p:e:f:m:i:n:h", ["help", "user=", "hostCount=", "timeScaler=", "mraiProc=", "mraiEmpty=", "flapDistribution=", "--policyMulti", "--iBgpMaxValue", "--iBgpMaxNeighbors"])
    except getopt.GetoptError, err:
# print help information and exit:
        print str(err) # will print something like "option -a not recognized"
        usage()
        sys.exit(2)
    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()

	elif o in ("-u", "--user"):
	    dict['user'] = a
	elif o in ("-h", "--hostCount"):
	    dict['hostCount'] = a
        elif o in ("-t", "--timeScaler"):
            dict['timeScaler'] = a
        elif o in ("-p", "--mraiProc"):
            dict['mraiProc'] = a
        elif o in ('-e', '--mraiEmpty'):
            dict['mraiEmpty'] = a
        elif o in ('-f', '--flapDistribution'):
            dict['flapDistribution'] = a
        elif o in ('-m', '--policyMulti'):
            dict['policyMulti'] = a
        elif o in ('-i', '--iBgpMaxValue'):
            dict['iBgpMaxValue'] = a
        elif o in ('-n', '--iBgpMaxNeighbors'):
            dict['iBgpMaxNeighbors'] = a
        else:
            assert False, "unhandled option"

    print printXML(dict)
    
"""
    <!-- do we want to use events ? -->
        <useEventsFile>true</useEventsFile>
    <!--    <eventsFileName>/home0/$user/runEnv/small_$hostCount/events_flap.xml</eventsFileName>-->
        <eventsFileName>/home0/$user/runEnv/small_$hostCount/events_scenario3.xml</eventsFileName>
"""

def printXML(dict):
    xml=Template("""
    <properties>
        <workingDir>/home/$user/large_$hostCount/</workingDir>
        <nodesFileName>nodes_caida_$hostCount.xml</nodesFileName>
        <diskCacheDir>/local/$user/data/</diskCacheDir>
        <prefixesFile>prefixes.xml</prefixesFile>
        <storageDir>/var/scratch/$user/large_storage_$hostCount/</storageDir>
        <hostCount>$hostCount</hostCount>
        <prefixCount>100000</prefixCount>
        <prefixArraySize>100000</prefixArraySize>
        <diagnosticThreadSleep>2500</diagnosticThreadSleep>
        <prefixCacheSize>7000</prefixCacheSize>
        <sleepingTime>40</sleepingTime>
    <!-- do we want to use neighbors caching ??? -->
        <neighborsContainerCaching>false</neighborsContainerCaching>

    <!-- logging info -->
        <createdLoggingInterval>10</createdLoggingInterval>
        <introducedLoggingInterval>10</introducedLoggingInterval>

    <!-- where to put the results? -->
        <resultDirectory>/var/scratch/jlf200/results_100k/</resultDirectory>

        <timeScaler>$timeScaler</timeScaler>
        <mraiProc>$mraiProc</mraiProc>
        <mraiEmpty>$mraiEmpty</mraiEmpty>
        <flapDistribution>$flapDistribution</flapDistribution>
        <policyMulti>$policyMulti</policyMulti>
        <iBgpMaxValue>$iBgpMaxValue</iBgpMaxValue>
        <iBgpMaxNeighbors>$iBgpMaxNeighbors</iBgpMaxNeighbors>
        <iBGPLog>$iBGPLog</iBGPLog>

        <noiseSleepTime>$noise</noiseSleepTime>
        <flapPercentage>$flapPercentage</flapPercentage>

    </properties>
    """)


    return xml.substitute(dict)


if __name__ == "__main__":
    main()
