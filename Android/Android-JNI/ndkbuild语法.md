## Android ndk-build 用法详解

### 详细命令

1.ndk-build NDK_LOG=1

用于配置LOG级别，打印ndk编译时的详细输出信息

2.ndk-build NDK_PROJECT_PATH=.

指定NDK编译的代码路径为当前目录，如果不配置，则必须把工程代码放到Android工程的jni目录下

3.ndk-build APP_BUILD_SCRIPT=./Android.mk

指定NDK编译使用的Android.mk文件

4.ndk-build NDK_APP_APPLICATION_MK=./Application.mk

指定NDK编译使用的application.mk文件

5.ndk-build clean

清除所有编译出来的临时文件和目标文件

6.ndk-build -B

强制重新编译已经编译完成的代码

7.ndk-build NDK_DEBUG=1

执行 debug build

8.ndk-build NDK_DEBUG=0

执行 release build

9.ndk-build NDK_OUT=./mydir

指定编译生成的文件的存放位置

10.ndk-build -C /opt/myTest/

到指定目录编译native代码
