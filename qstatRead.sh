#!/bin/bash

queueState=`qstat -f`
queueLines=`echo -e "$queueState"|wc -l`
queueLines=$[$queueLines-1]
queueState=`echo -e "$queueState" |tail -$queueLines`
firstChar='c'
freeTotal=0
freeAll=0
freeGPU=0
freeFat=0
freeDAS3=0
fatLine=""
while [[ "$firstChar" != "#" && $queueLines -gt 1 ]]; do
  line=`echo -e "$queueState"|head -1`
  firstChar=`echo $line|cut -c1-1`
  case "$firstChar" in
    a)
      state=`echo $line|cut -d " " -f 3|cut -d "/" -f 2`
      if [ "$state" -lt 1 ]; then
        state=`echo $line|cut -d " " -f 6`
        if [[ "$state" != "d" && "$state" != "au" && "$state" != "adu" ]]; then
          freeTotal=$[$freeTotal+1]
          freeAll=$[$freeAll+1]
        fi
      fi
      ;;
    g)
      state=`echo $line|cut -d " " -f 3|cut -d "/" -f 2`
      if [ "$state" -lt 1 ]; then
        state=`echo $line|cut -d " " -f 6`
        if [[ "$state" != "d" && "$state" != "au" && "$state" != "adu" ]]; then
          freeTotal=$[$freeTotal+1]
          freeGPU=$[$freeGPU+1]
        fi
      fi
      ;;
    f)
      state=`echo $line|cut -d " " -f 3|cut -d "/" -f 2`
      if [ "$state" -lt 1 ]; then
        state=`echo $line|cut -d " " -f 6`
        if [[ "$state" != "d" && "$state" != "au" && "$state" != "adu" ]]; then
          freeTotal=$[$freeTotal+1]
          freeFat=$[$freeFat+1]
          machine=`echo $line|cut -d " " -f 1|cut -d "@" -f 2|cut -d "." -f 1`
          fatLine=`echo "${fatLine}${machine},"`
        fi
      fi
      ;;
    f)
      state=`echo $line|cut -d " " -f 3|cut -d "/" -f 2`
      if [ "$state" -lt 1 ]; then
        state=`echo $line|cut -d " " -f 6`
        if [[ "$state" != "d" && "$state" != "au" && "$state" != "adu" ]]; then
          freeTotal=$[$freeTotal+1]
          freeFat=$[$freeFat+1]
          machine=`echo $line|cut -d " " -f 1|cut -d "@" -f 2|cut -d "." -f 1`
          fatLine=`echo "${fatLine}${machine},"`
        fi
      fi
      ;;
    d)
      state=`echo $line|cut -d " " -f 3|cut -d "/" -f 2`
      if [ "$state" -lt 1 ]; then
        state=`echo $line|cut -d " " -f 6`
        if [[ "$state" != "d" && "$state" != "au" && "$state" != "adu" ]]; then
          freeTotal=$[$freeTotal+1]
          freeDAS3=$[$freeDAS3+1]
        fi
      fi
      ;;
  esac
  queueLines=$[$queueLines-1]
  queueState=`echo -e "$queueState" |tail -$queueLines`
done

#echo There are $freeTotal total free nodes.
#echo There are $freeAll free regular nodes.
#echo There are $freeGPU free GPU nodes.
#echo -n "There are $freeFat free Fat nodes"
fatChars=`echo $fatLine|wc -c`
if [ $fatChars -gt 2 ]; then
  fatChars=$[$fatChars-2]
  fatLine=${fatLine:0:$fatChars}
#  echo ": $fatLine"
fi
#echo There are $freeDAS3 free DAS3 nodes.
echo $freeTotal $freeAll $freeGPU $freeDAS3 $freeFat $fatLine

