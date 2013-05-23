import sys

pause = 6000 * 1000	# logical seconds
#propagationTime = 7200 * 1000 # this is in logical ms
propagationTime = 120 * 60 * 1000 # this is in logical ms

#initialPause = 60 * 60 * 1000 # start propagation after 60 minutes
initialPause = 25 * 60 * 1000 # start propagation after 10 minutes

#pause = 20000	# logical seconds
#propagationTime = 3600 * 1000 # this is in logical ms

beacon = 1
withsize = 0

if len(sys.argv) > 1:
	beacon = int(sys.argv[1])
if len(sys.argv) > 2:
	withsize = int(sys.argv[2])


print "<object-stream>"

asList = []
asList.append((15174, "AS3927")) # AS3130, this is beacon2 now
asList.append((2667, "AS5637")) # AS3130, this is beacon2 now
asList.append((3419, "AS1221")) # AS1221, this is beacon1 now

#asList.append(2667) # AS5637

count = len(asList)

def getAS(i):
	(as, _) =  asList[i % len(asList)]
	return as

def getPrefix(i):
	return i / count + 1000 * ((i % count) + 1)

def getWithdrawal(i):
	return getPrefix(i) + 3000 	# this will get us msg_4000 and msg_5000 and msg_6000

def getASesList(asList):
	out = ""
	for (as, name) in asList:
		out += "<string>%s</string>\n" % (name, )
	return out


def getPrefixXML(prefix):
	return """<prefix>%d</prefix>""" % (prefix, )

def getPrefixListXML(prefix):
	return """<prefixList>%s</prefixList>""" % (getPrefixXML(prefix), )

def getWithdrawalListXML(prefix):
	return """<withdrawals>%s</withdrawals>""" % (getPrefixXML(prefix), )


def getPrefixListXMLFromRange(rg):
	out = "<prefixList>"
	for i in rg:
		for j in range(0, len(asList)):
			out += getPrefixXML(getWithdrawal(i+j))
	out += "</prefixList>"
	return out

def getAnn(as, prefix, time):
	return """
		<ann showOnScreen="true">
			<asId>%d</asId>
			%s
			<schedule class="explicit" launchTime="%d"/>
		</ann>""" % (as, getPrefixListXML(prefix), time)

def getWith(as, prefix, time):
	return """
		<ann showOnScreen="true">
			<asId>%d</asId>
			%s
			<schedule class="explicit" launchTime="%d"/>
		</ann>""" % (as, getWithdrawalListXML(prefix), time)

def getResetXML(withsize, time):
	return """<resetStats><schedule class="explicit" launchTime="%d"/>%s</resetStats>""" % (time, getPrefixListXMLFromRange(range(0, withsize * count, len(asList))))
	

print """
<noise showOnScreen="true" asesCount="16000">
		<schedule class="explicit" launchTime="0"/>
</noise>
"""

time = 0

# send initial prefixes for withdrawing
if withsize > 0:
	for i in range(0, withsize * count, len(asList)):
		for j in range(0, len(asList)):
			print getAnn(getAS(i + j), getWithdrawal(i + j), time)
	time += initialPause
time += initialPause

# start announcing
for i in range(0, beacon * count, len(asList)):
	for j in range(0, len(asList)):
		print getAnn(getAS(i+j), getPrefix(i+j), time)
	time += pause
time -= pause

time += propagationTime

if withsize > 0:
	# we need to reset info about prefixes that we want to withdraw
	print getResetXML(withsize, time)
	# start withdrawing
	for i in range(0, withsize * count, len(asList)):
		for j in range(0, len(asList)):
			print getWith(getAS(i+j), getWithdrawal(i+j), time)
		time += pause
	

print """
<info showOnScreen="true">
	<schedule class="explicit" launchTime="%d"/>
	<ases>
%s
%s
	</ases>
</info>
	<lastSeen showOnScreen="true">
                <schedule class="explicit" launchTime="%d"/>
                <prefixes>""" % (time, getASesList(asList), getASesList(asList), time)



for i in range(0, max(beacon, withsize)):
	for j in range(count):
		print """ <prefix>%d</prefix>""" % (getPrefix(i * count + j), )
		print """ <prefix>%d</prefix>""" % (getWithdrawal(i * count + j), )



print"""
                </prefixes>
        </lastSeen>
"""

print "</object-stream>"


if len(sys.argv) > 3:
	scale = int(sys.argv[3])
	sys.stderr.write("sim time: %d seconds\n" % (time / 1000 / scale, ))
