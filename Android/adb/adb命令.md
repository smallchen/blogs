adb -s 172.18.101.170:5555 shell am startservice -n com.jokin.demo.calendar/com.jokin.demo.calendar.CalendarService

adb -s 192.168.54.1:5555 shell am startservice -n com.jokin.demo.calendar/.CalendarService

adb -s 172.18.99.140:5555 shell am startservice -n com.jokin.osservice/.OSService

安装包列表
adb shell pm list packages

AndroidStuido调试程序时的命令行
07/27 09:41:14: Launching testpcfiletrans
$ adb push /Users/jokinkuang/Documents/projects/510/510_master/testpcfiletrans/build/outputs/apk/testpcfiletrans-debug.apk /data/local/tmp/com.jokin.demo.TestPCFileTrans
$ adb shell pm install -r "/data/local/tmp/com.jokin.demo.TestPCFileTrans"
	pkg: /data/local/tmp/com.jokin.demo.TestPCFileTrans
Success

$ adb shell am start -n "com.jokin.demo.TestPCFileTrans/com.jokin.demo.TestPCFileTrans.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
Connected to process 31294 on device 172.18.141.72:5555

授权码 296495538
adb shell setprop persist.sys.boardsn.value 42170517000009

## mount root
adb xxx root
adb remount
adb shell
su
mount -o remount -w /system    // 3399
mount -o remount w /system     // 5508

分析GPU绘制
adb shell dumpsys gfxinfo com.jokin.demo.welcome framestats
adb shell dumpsys package com.examle.xx

chown -R media_rw:media_rw mindlinker/       

仅输出标记为“ActivityManager”且优先级大于等于“Info”和标记为“PowerManagerService”并且优先级大于等于“Debug”的日志：
adb logcat ActivityManager:I PowerManagerService:D *:S
注：*:S用于设置所有标记的日志优先级为S，这样可以确保仅输出符合条件的日志。
adb logcat *:W   //显示所有优先级大于等于“warning”的日志
adb logcat -s PowerManagerService   //显示PowerManagerService的日志信息
adb install -r ~/app-platform3399-1.0.0-000124-cb47e3d-platform3399Debug-signed.apk
// -r 是默认卸载重装，不加会报错无法安装

## APP版本号
dumpsys package co m .xx.xxx

## CPU架构
cat /proc/cpuinfo   
