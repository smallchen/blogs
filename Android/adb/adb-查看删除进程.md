## adb 查看进程

### 将Service转为远程进程。

```xml
<service
    android:name=".ActionService"
    android:enabled="true"
    android:exported="true"
    android:process="com.jokin.demo.aidl.remote"
    >
```

### 查看进程

```java
odin:/ $ ps | grep jokin                                                                                                                                    
u0_a268   5723  594   1734932 107248 SyS_epoll_ 0000000000 S com.jokin.demo.aidl.server
u0_a268   5843  594   1670660 79980 SyS_epoll_ 0000000000 S com.jokin.demo.aidl.remote
```

### 杀掉进程

```java
kill -9 5723
```

通常，真机拿不到root权限，也就无法kill进程。
