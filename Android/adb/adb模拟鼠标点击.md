
```shell
#!/bin/bash

while true
do
    adb shell input tap 301 921
    sleep 1
done
exit


for i in {1..200}
do
    adb shell input tap 301 921
      sleep 3
    adb shell input tap 301 921
done
exit
```
