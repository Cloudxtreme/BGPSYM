#!/usr/bin/python
import sys

def getNext(value):
    if value == 0:
        return 5
    else:
        return value + 10

def getLast(value):
    if value == 0:
        return 0
    else:
        return value + 5

last = 0
next = getNext(last)
count = 0

echo "x" "x"

for i in sys.stdin:
    value = int(i) / 1000
    if value > next:
        print "%d %d" % (getLast(last), count)
        count = 0
        last = next
        next = getNext(last)
    count += 1

print "%d %d" % (getLast(last), count)
