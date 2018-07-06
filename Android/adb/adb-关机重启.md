## adb 关机或重启

adb reboot 重启

adb reboot -p 关机


### Android系统重启，不是Linux系统重启，会比较快。

adb shell;
stop;start;

### 进入bootloader模式

adb reboot bootloader

### fastboot烧写

adb fastboot
