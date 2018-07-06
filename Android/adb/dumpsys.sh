#!/bin/bash

while true
do
    adb shell dumpsys meminfo | grep $1
    sleep 1
done
exit
