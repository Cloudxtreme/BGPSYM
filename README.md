# Installation

* Checkout source files from repository to DAS4
* Compile sources with ant

Possibly some warnings might appear: 

[javac] /home/$USER/bgpsym/src/nl/nlnetlabs/bgpsym01/primitives/types/ByteFIFO.java:12: warning: unmappable character for encoding ASCII


[javac] /home/$USER/bgpsym/src/nl/nlnetlabs/bgpsym01/primitives/types/ByteFIFO.java:99: warning: unmappable character for encoding ASCII

This is due to **LANG=en_US.UTF-8** in the settings or failing to set **LANG** properly. These warnings can be ignored.

# Running an experiment

Experiments are run on the DAS-4 cluster at the VU. To run experiments you would have to ssh to the DAS4, e.g., fs0.das4.cs.vu.nl, from within the VU and change directory to where you checked out BGPSYM.

After changing directory you can start an experiment by making use of the shell script **magic_run**:

`$ ./magic_run.sh <#nodes> ~/env/properties.xml <duration in hh:mm:ss>`

### Multiple experiments

The python script `experiments.py`, located in the main directory, assists in running multiple experiments on the DAS4:

`$ ./experiments.py -g <graph name> -e <event name> [-s <slaves used> -t <run time> -np <number of property files> -st <starting property>]`

This script expects the following directory in your home folder:

`/experiments/<graph name>/` containing a `topology.xml`, `prefixes.xml`, and two folders, `events` and `properties`. These are generated using tools from [BGPSYM-analysis](https://gitlab.nlnetlabs.nl/Rattleshirt/bgpsym-analysis/)

# Configuration

### Configuration files

The parameters for the simulator are specified in an XML formatted file, as mentioned above.
The properties file describes various parameter values that can be set for the simulation.
Each parameter has its own name and value and their meaning are quite straightforward. For example: number of slave nodes; working directory; scratch directory; etc.

Some specific simulator parameters (which defines the behaviour of the simulator) are described below:

* **timeScaler:** how much faster the simulator should run with respect to the real-time (defaults: 10 for 48 nodes, 30 for 64 nodes)
* **mraiProc:** how big percent of the ASes should use MRAI timers (30s) as opposed to no MRAI timers (0s) (default: 70[%])
* **mraiEmpty:** how big should be the value of MRAI for Juniper like hosts (default: 0 [ms]) 
* **flapDistribution:** how many routers should be CISCO-like (as opposed to Juniper-like) (default: 70 [%]) 
* **flapPercentage:** how many routers should use route flap damping (default: 70 [%]) 
* **iBGPMaxValue:** maximum convergence time of iBGP (with exponential distribution) (default: 30000 [ms]) 
* **iBGPMaxNeighbors:** for what amount of neighbors does iBGP reach the maximum value? (default: 750) 
* **noiseSleepTime:** how much time should elapse between sending two consecutive noise announcements (default: 61000 [ms])

### Hardcoded properties
Since the simulator is an experimental and "one-developer" software not all properties were extracted. Some of these un-extracted properties are very important and can still be changed.

### Callback logging
There is a callback mechanism implemented in the simulator.
Every event is given to a callback, and the callback decides what to do with it.
Since writing each event to a file is a very expensive process, the callback normally does nothing. 

It is sometimes desirable to be able to log events for some (or all hosts).
In order to change callback behavior changes are needed to be done in **nl.nlnetlabs.bgpsym01.primitives.factories.CallbackFactory**.
The behavior of **getCallback(ASIdentifier id)** has to be changed so that for particular hosts **FileCallbackFull** is created instead of **CallbackMock**.
**FileCallbackFull** logs events to file in log directory of the specific run.
Using those logs it is possible to trace all decisions made by a node, as well as all in and out BGP traffic. 

It has to be noted that turning callback on for all ASes slows the simulator down (it has to create and write to about 25k files on NFS).
It should be used only for debugging/tracing purposes and turned off when efficiency and precisions are an issue.
In order to keep simulator load low one should consider running simulator with full callback with very small time scale (even 2).

### Policy decision making
Although 2 parameters used in **PolicyImplRel** have been extracted, there are many functions for the policy making decisions.
In order to change the path comparison algorithm, one should change the implementation of **nl.nlnetlabs.bgpsym01.route.PolicyImplRel#isBetter** (description of the method and parameters is in the interface method: nl.nlnetlabs.bgpsym01.route.PolicyImplRel#isBetter=).

### Events

Simulation experiments are described by so-called scenarios, or commands. Scenarios are specified in the file **eventFileName**. This file could contain the following events:

* Announcement(s)
* Withdrawal(s)
* Connection(s)
* Disconnection(s)
* Log(s)

### Logging

Simulator outputs are done through apache's [log4j](http://logging.apache.org/log4j/).
