#!/bin/bash

#
#
#
for file in `ls $1`; do
    basename=${file%.*}
    suffix=${file: -4}
    
    param1="$1/$file"
    param2="$1/$basename$suffix.dat"

    java netflix.memreader.MemReader $param1 $param2

done
