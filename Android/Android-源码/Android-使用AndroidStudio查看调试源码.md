<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android 使用Android Studio查看调试源码](#android-使用android-studio查看调试源码)
- [参考:](#参考)

<!-- /TOC -->

## Android 使用Android Studio查看调试源码

1. 编译`idegen`模块：

```shell
soruce build/envsetup.sh
mmm development/tools/idegen/
# 或者
# cd development/tools/idegen/
# mm
```

> mmm和mm具体查看另一篇《源码编译命令详解》

2. 根目录运行脚本`development/tools/idegen/idegen.sh`

> 非根目录会提示：Run from the root of the tree.

3. 根目录会生成`android.ipr、android.iml、android.iws`

* android.ipr: 通常是保存工程相关的设置,比如编译器配置,入口,相关的libraries等
* android.iml: 则是主要是描述了modules,比如modules的路径,依赖关系等.
* android.iws: 则主要是包含了一些个人工作区的设置.

4. 在进行项目导入时，需要先扩大`AndroidStudio`的内存和IDEA的设置。`AndroidStudio` - `Help` - `Edit Custom VM Options`。或者在路径`/Applications/Android Studio.app/Contents/bin`下。

`idea.properties`文件，修改为（这个也可以不改）:

* `idea.max.content.load.filesize`为`40000`

`studio.vmoptions`文件，修改为：

```java
-Xms512m
-Xmx4096m
-XX:MaxPermSize=1024m
-XX:ReservedCodeCacheSize=512m

// 下面这些可选（我没填，只是用于记录）
-ea
-Dsun.io.useCanonCaches=false
-Djava.net.preferIPv4Stack=true
-Djna.nosys=true
-Djna.boot.library.path=

-Djna.debug_load=true
-Djna.debug_load.jna=true
-Djsse.enableSNIExtension=false
-XX:+UseCodeCacheFlushing
-XX:+UseConcMarkSweepGC
-XX:SoftRefLRUPolicyMSPerMB=50
-Dawt.useSystemAAFontSettings=lcd
```

> 然后发现，没修改任何参数，1246M的内存也成功导入了Android源码项目。

4. 执行命令`open android.ipr`。或者使用`AndroidStudio`打开`android.ipr`导入项目。

导入过程比较漫长，耐心等待，大概25分钟。

这边导入后，就直接可以`Command+O`和`Command+B`来跳转源码了，并不需要进行任何配置。唯独不太好的就是，`Command+O`会找到多个`View类`，包含了`out/target`目录下SDK输出的`View`。

```java
类1:
/Volumes/diskD/android-8.1.0_r20/frameworks/base/core/java/android/view/View.java
类2:
/Volumes/diskD/android-8.1.0_r20/out/target/common/obj/JAVA_LIBRARIES/android_system_stubs_current_intermediates/src/android/view/View.java
类3:
/Volumes/diskD/android-8.1.0_r20/out/target/common/obj/JAVA_LIBRARIES/android_test_stubs_current_intermediates/src/android/view/View.java
```

把后面两个路径从项目中删除即可。

要实现`Android Framework`的调试环境，还需要：

* 配置Module。这边直接使用原始配置生成的Module，也就是包含了所有Module。

* 配置SDK。这边直接选择AndroidStudio自带的`API 8.1`。

配置好SDK后，打开`attach debugger to android process`，勾选`Show All Process`，就可以断点调试`Settings`系统应用了。

我的环境是：

```
源码版本: 8.1.0_r20
AndroidStudio 3.0.1
MacOS High Sierra 10.13.5
```

并不需要像网上那么复杂。以下是网上教程里的步骤：

* 在`module`中挂载上我们自己的`Android Framework`
* 将我们自己生成的`SDK`设置成`AndroidStudio`的默认`SDK`
* 运行APP后，使用`attach debugger to android process`来断点调试

注：一定要清晰的知道所修改的源码所在进程，不然无法调试。如果修改的代码中有AIDL文件，那么一定要手动将这些文件路径导入到framework目录下的Android.mk中。

## 参考:
Android源码编译以及调试相关记录
<http://lib.csdn.net/article/android/58610>
<https://www.cnblogs.com/Lefter/p/4176991.html>
<https://www.jianshu.com/p/4ab864caefb2>
官方
<https://source.android.com/source/initializing.html>
