#!/bin/bash

TOFILE=0

if [ "x$2" != "x" ]; then
    TOFILE=1
fi

DEFAULT_START="/home0/mwi300/scratch/results_64/"
if [ "x$1" == "x" ]; then
	DIR=`rsh das3 "ls -t1 /home0/mwi300/scratch/results_64/ | head -1"`
	DIR="$DEFAULT_START$DIR/"

else
	DIR="$1/"
fi

echo "downloading $DIR"
rsync --delete -avz das3:$DIR msg_data/
if [ $? -ne 0 ]; then
    exit 1
fi

# we are interested in msg_1* msg_2* msg_3* and msg_4* (beacon1 and beacon3, announces and withdrawals)
nums="1 2 3 4 5 6"

# signal duration...
rm -f out* beacon*
for num in $nums; do
	if [ -f msg_data/msg_${num}000 ]; then
		cat msg_data/msg_${num}* | cut -d ";" -f 3 | cut -b 2- | sort -n > tmp$num
		cat tmp$num | python ccdf.py `wc -l tmp$num` > beacon$num
	fi
done


# relative convergence
for num in $nums; do
	if [ -f msg_data/msg_${num}000 ]; then
        MONITORS=`cat msg_data/msg_${num}* | grep -o "^[^ ]*" | sort | uniq`
        echo "mons: $MONITORS"
        for monitor in $MONITORS; do
            (for file in msg_data/msg_${num}*; do
                min=`grep $monitor $file | cut -d ";" -f 4 | cut -b 2- | sort -n | head -1`
                echo "for $file @ $monitor min=$min" > /dev/stderr
                for one in `grep $monitor $file | cut -d ";" -f 5 | cut -b 2-`; do
                    echo "$[one-min]"
                done
            done ) | sort -n > tmp$num
            cat tmp$num | python ccdf.py `wc -l tmp$num` > conv$num
        done
	fi
done

# get the filename...
FILENAME=`xmlstarlet sel -t -o "pol_" -v "properties/policyMulti" -o ",flapD=" -v "properties/flapDistribution" -o ",iValue=" -v "properties/iBgpMaxValue" -o ",iN=" -v "properties/iBgpMaxNeighbors" -o ",sleep=" -v "properties/noiseSleepTime" -o ",flapP=" -v "properties/flapPercentage" msg_data/properties.info`


# generate plots input files
rm -f input input2
if [ -f "msg_data/info" ]; then
    written=0
	num=0
	for line in `cat msg_data/info`; do
		if [ $num -eq 0 ]; then
            if [ "$TOFILE" != "0" ]; then
                echo "set output \"signal_$line_$FILENAME.jpg\"" >> input
                echo "set terminal jpeg" >> input
                echo "set output \"conv_$line_$FILENAME.jpg\"" >> input2
                echo "set terminal jpeg" >> input2
            fi
            GRID="set xtics 30\nset ytics 0.1\nset grid"
        
            echo -e "$GRID\n" >> input
            echo -e "$GRID\n" >> input2
			echo "set title \"signal $line\\n$FILENAME\"" >> input
			echo "set title \"conv $line\\n$FILENAME\"" >> input2
			echo -n "plot [0:180] [0:1] " >> input
			echo -n "plot [0:180] [0:1] " >> input2
			num=1
		else
			if [ -f beacon$num ]; then
				if [ $written -gt 0 ]; then
					echo -n ", " >> input
					echo -n ", " >> input2
				fi
				echo -n "\"beacon$num\" title \"$line\" " >> input
				echo -n "\"conv$num\" title \"$line\" " >> input2
                written=1
			fi
            num=$[num+1]
		fi
	done
	echo >> input
	echo >> input2
fi

#cat input

cat input2 | gnuplot
cat input | gnuplot

if [ "$TOFILE" == "0" ]; then
    cat input /dev/stdin | gnuplot &
    cat input2 /dev/stdin | gnuplot &
fi
