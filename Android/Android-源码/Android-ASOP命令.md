##

* jgrep            java文件
* resgrep
* godir         查找文件所在目录

build/setupenv.sh

lunch 显示构建版本

user：发行版
userdebug：内测版
eng：开发版

recorvery模式：bootable/recoverity/etc/init.rc
boot模式：system/core/rootdir/init.rc

32/64位的zygote。会根据CPU选择。

init.rc
zygote
system/bin/app_process
app_main.cpp::main()
AndroidRuntime.cpp::start()
ZygoteInit.java::main()
SystemServer.java::main()
RuntimeInit.java::zygoteInit()
SystemServer.java::systemReady()

sublime @Main

源码编译，直接编出odex。没有dex。减少转化时间。
Mac环境下不需要设置，默认就不开启odex优化。
