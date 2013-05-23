# Installation

* Checkout source files from repository to DAS4
* Compile sources with ant

Possibly some warnings might appear: 

[javac] /home/$USER/bgpsym/src/nl/nlnetlabs/bgpsym01/primitives/types/ByteFIFO.java:12: warning: unmappable character for encoding ASCII
...
[javac] /home/$USER/bgpsym/src/nl/nlnetlabs/bgpsym01/primitives/types/ByteFIFO.java:99: warning: unmappable character for encoding ASCII

This is due to **LANG=en_US.UTF-8** in the settings or failing to set **LANG** properly. These warnings can be ignored.

# Running an experiment

Experiments are run on the DAS-4 cluster at the VU. To run experiments you would have to ssh to the DAS4, e.g., fs0.das4.cs.vu.nl, from within the VU and change directory to where you checked out BGPSYM.

After changing directory you can start an experiment by making use of the shell script **magic_run**:

`$ ./magic_run.sh <#nodes> ~/env/properties.xml <duration in hh:mm:ss>`

# Configuration
