

import sys

argc = len(sys.argv)

size = float(sys.argv[1])
count = 0.0

last = -1
multi = 1000.0

for i in sys.stdin:
	value = int(i)
	count += 1
	if value != last and last != -1:
		print "%d %f" % (last / multi, count / size)
	last = value
		
print "%d %f" % (last / multi, count / size)
