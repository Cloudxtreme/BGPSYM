import sys
from prop_gen import getDefDict, printXML

params=[("timeScaler", (40, )),
        ("mraiProc", (60, 20)),
        ("mraiEmpty", (0, )),
        ("flapDistribution", (80, )),
        ("flapPercentage", (20, 80)),
        ("noise", (11000, 31000, 61000, )),
        ("policyMulti", (30, )), ("iBgpMaxValue", (30000, 10000)),
        ("iBgpMaxNeighbors", (1250, 750, ))]

def writeXML(dict, num):
    f = open("V23_properties_%s.xml" % (num, ), "w")
    f.write(printXML(dict))
    f.close()

def useDefaults():
    num = 0
    for name, list in params:
        for value in list:
            dict = getDefDict()
            dict[name] = value
            writeXML(dict, num)
            num += 1

def arb(num, timeScaler, mraiProc, mraiEmpty, flapDistribution, flapPercentage, noise, policyMulti, iBgpMaxValue, iBgpMaxNeighbors):
    d=dict(timeScaler=timeScaler, mraiProc=mraiProc, mraiEmpty=mraiEmpty, flapDistribution=flapDistribution, flapPercentage=flapPercentage, noise=noise, policyMulti=policyMulti, iBgpMaxValue=iBgpMaxValue, iBgpMaxNeighbors=iBgpMaxNeighbors, hostCount=sys.argv[1], iBGPLog="true")
    print "%s -> %s" % (num, d)
    writeXML(d, num)

def allPossibilites():
    class Helper:

        def __init__(self):
            self.num = 0
            
        def helper(self, n, accu):
            if n == len(params):
                print accu
                writeXML(accu, self.num)
                self.num += 1
            else:
                (name, list) = params[n]
                for value in list:
                    accu[name] = value
                    self.helper(n+1, accu)

    d=getDefDict()
    d['hostCount'] = sys.argv[1]
    Helper().helper(0, d)


#allPossibilites()
def genArb():
    num = 0
    #list = [(20000, 750), (20000, 1000), (20000, 500), (10000, 350), (3000, 50)]
    #for flap in (20, 10):
        #for iValue, iN in list:
    flap=65
    mrai=65
    scaler=15
    policy=50
    noise=121000
    iValue=10000
    iN=1500
    arb("%d" % (8,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,     iValue,     iN)
    arb("%d" % (9,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      20000,     iN)
    arb("%d" % (10,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      20000,     2500)
    arb("%d" % (11,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      20000,     3500)
    arb("%d" % (12,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      2000,     500)
    arb("%d" % (13,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      200,     500)
    arb("%d" % (14,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,      200,     50)
    
    arb("%d" % (15,  ),    scaler,     mrai,       0,      80,     flap,      noise,   70    ,     iValue,     iN)
    arb("%d" % (16,  ),    scaler,      90,       0,      80,     flap,      noise,   70    ,     iValue,     iN)
    arb("%d" % (17,  ),    scaler,      20,       0,      80,     flap,      noise,   70    ,     iValue,     iN)
    arb("%d" % (18,  ),    scaler,      100,       0,      80,     flap,      noise,   70    ,     iValue,     iN)
    arb("%d" % (19,  ),    scaler,      5,       0,      80,     flap,      noise,   70    ,     iValue,     iN)



genArb()
#allPossibilites()
