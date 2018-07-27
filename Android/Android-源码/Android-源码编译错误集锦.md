## Android源码编译错误集锦

#### 找不到lunch命令

运行一下环境配置 `source build/envsetup.sh`

#### 找不到emulator命令

```java
source build/envsetup.sh
lunch (选择之前编译设置的目标版本，比如`1`)
emulator
```

mac下，`emulator`命令位于`prebuilts/android-emulator/`

```java
which emulator
/Volumes/diskD/android-8.1.0_r20/prebuilts/android-emulator/darwin-x86_64/emulator
```


```java
Error: could not find jdk tools.jar at /System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/../lib/tools.jar, please check if your JDK was installed correctly. Stop.
```


但在终端中却能够查询到JDK已经安装了。再看错误定位的文件是config.mk的第604行：
HOST_JDK_TOOLS_JAR:= $(shell $(BUILD_SYSTEM)/find-jdk-tools-jar.sh)
其中find-jdk-tools-jar.sh应该就是寻找本地JDK中tool.jar的执行脚本，在找到这个文件，发现：
原来必须在环境变量中指定ANDROID_JAVA_HOME才能顺利执行。

修改`~/.bash_profile`，然后新建一个终端标签。

```java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/
export PATH=$PATH:/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/bin/
export CLASSPATH=.:/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/jre/jdk/Contents/Home/lib/
export ANDROID_JAVA_HOME=$JAVA_HOME
```


```java
[ 99% 63858/64494] Building with Jack: out/target/common...LIBRARIES/framework_intermediates/with-local/classes.de
FAILED: out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/with-local/classes.dex
/bin/bash out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/with-local/classes.dex.rsp
Out of memory error (version 1.3-rc7 'Douarn' (445000 d7be3910514558d6715ce455ce0861ae2f56925a by android-jack-team@google.com)).
GC overhead limit exceeded.
Try increasing heap size with java option '-Xmx<size>'.
Warning: This may have produced partial or corrupted output.
```

Out of memory error (version 1.3-b2 'Douarn' (320400 bfc75939a2e32be4feddc59d656afb274397ed65 by android-jack-team@google.com)).
    Java heap space.
    Try increasing heap size with java option '-Xmx<size>'.
    Warning: This may have produced partial or corrupted output.


fix solution:

    1. vim ./prebuilts/sdk/tools/jack-admin ---->  JACK_VM_COMMAND=${JACK_VM_COMMAND:="java -Xmx4096m"}

    2.  export JACK_SERVER_VM_ARGUMENTS="-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4g"
        ./prebuilts/sdk/tools/jack-admin kill-server
        ./prebuilts/sdk/tools/jack-admin start-server

    1. vim `~/.bash_profile`
       `export JACK_SERVER_VM_ARGUMENTS="-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4096m"`

    2. stop the background jack-server
       `prebuilts/sdk/tools/jack-admin kill-server`
    3. start a new console.
    4. run `export` to see if the `JACK_SERVER_VM_ARGUMENTS` exist.
      or `echo $JACK_SERVER_VM_ARGUMENTS`
    5. run again.


```java
Launching Jack server java -XX:MaxJavaStackTraceDepth=-1 -Djava.io.tmpdir=/var/folders/z8/m7mfvs452dv23md7nq7gz7jw0000gp/T/ -Dfile.encoding=UTF-8 -XX:+TieredCompilation -cp /Users/jokin/.jack-server/launcher.jar com.android.jack.launcher.ServerLauncher
```


### 附录

<https://blog.csdn.net/h649305597/article/details/80322488>
<https://blog.csdn.net/l01142TS/rss/list>
<https://blog.csdn.net/brightming/article/details/49763515/>
搜索`android android-8.1.0 源码后没有emulator`
