## adb remount命令

方式1

```shell
adb root
adb remount
```

方式2

```shell
adb shell
su
mount -o remount -w /system    // 3399
mount -o remount w /system     // 5508
```

以上情况，可能会发生以下错误：

```java
mount: '/system' not in /proc/mounts
```

最后，**最牛逼** 的命令解决了：`mount -o rw,remount /;`

```shell
Probable cause that remount fails is you are not running adb as root.

Shell Script should be as follow.

# Script to mount Android Device as read/write.
# List the Devices.
adb devices;

# Run adb as root (Needs root access).
adb root;

# Since you're running as root su is not required
adb shell mount -o rw,remount /;
If this fails, you could try the below:

# List the Devices.
adb devices;

# Run adb as root
adb root;

adb remount;
adb shell su -c "mount -o rw,remount /";
To find which user you are:

$ adb shell whoami  
```

chown -R media_rw:media_rw mindlinker/    
