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

chown -R media_rw:media_rw mindlinker/    
