## macOS High Sierra下虚拟机开启HAXM加速（不是吐槽，arm的虚拟机真心慢！基本不能用！）

macOS High Sierra,Intel HAXM is required to run this AVD...

mac 升级到10.13版本后，Android Studio的模拟器就运行不了了，报错 Intel HAXM is required to run this AVD,VT-x is disabled in BIOS，网上搜索解决办法是安装HAXM，但我尝试安装后还是没有解决。最后在微软的官网上看到HAMX还不支持10.13。

最近想起来这个问题，看了一下微软的更新，发现已经更新到了v7.0.0。我尝试下载直接安装.dmg，但还是报错。尝试用.sh脚本卸载提示没有安装。最后尝试使用脚本安装，终于模拟器可以正常运行不报错了。

### 解决步骤：

官网下载HAMX点这里<https://software.intel.com/en-us/articles/intel-hardware-accelerated-execution-manager-intel-haxm>。然后下载macOS版本的zip包。

> 额。。。额。。。发现百度搜索`HAXM`第一个就是。

如果慢的话，可以在这下载<http://files.bughub.top/haxm-macosx_v7_0_0.zip>。

然后解压，切换到解压的目录。

执行`sudo sh silent_install.sh`，这时要记得看一下安全和隐私里面是不是有需要你允许的操作。

如果安装成功，提示`Silent installation Pass!`。

这时安卓模拟器就可以正常启动了。

此时，执行`kextstat | grep intel`就能有输出

```shell
  166    0 0xffffff7f83758000 0x1e000    0x1e000    com.intel.kext.intelhaxm (7.0.0) 823315F2-1275-377E-AFF9-E909857C3B2B <7 5 4 3 1>
```

##### 开启

`sudo kextload –b com.intel.kext.intelhaxm`

##### 暂停

`sudo kextunload –b com.intel.kext.intelhaxm`

### 额外要求

Intel® Hardware Accelerated Execution Manager (Intel® HAXM) is a hardware-assisted virtualization engine (hypervisor) that uses Intel® Virtualization Technology (Intel® VT) to speed up Android* app emulation on a host machine.

HAXM硬件加速只支持`x86`CPU。所以需要构建`x86`的Android版本。比如`aosp_x86-eng`和`aosp_x86_64-eng`。

x86的虚拟机，要比arm的虚拟机快至少5倍。arm的虚拟机根本用不了。

当然，x86虚拟机的缺点也是有的，就是不能安装市面上的应用。因为市面上的应用大部分是arm abi（就是so没有包含x86版本）的apk。

### 参考
<https://blog.csdn.net/song279811799/article/details/79133756>
