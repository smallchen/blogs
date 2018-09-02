## Android 应用可用最大内存

`/system/build.prop`


`dalvik.vm.heapstartsize=8m` // 它表示堆分配的初始大小，它会影响到整个系统对RAM的使用程度，和第一次使用应用时的流畅程度。它值越小，系统ram消耗越慢，但一些较大应用一开始不够用，需要调用gc和堆调整策略，导致应用反应较慢。它值越大，这个值越大系统ram消耗越快，但是应用更流畅。

`dalvik.vm.heapgrowthlimit=64m` // 单个应用可用最大内存，主要对应的是这个值,它表示单个进程内存被限定在64m,即程序运行过程中实际只能使用64m内存，超出就会报OOM。（仅仅针对dalvik堆，不包括native堆），所以合理使用native可以增大应用可用内存。

`dalvik.vm.heapsize=384m `// heapsize参数表示单个进程可用的最大内存，但如果存在heapgrowthlimit参数，则以heapgrowthlimit为准.
heapsize表示不受控情况下的极限堆，表示单个虚拟机或单个进程可用的最大内存。而android上的应用是带有独立虚拟机的，也就是每开一个应用就会打开一个独立的虚拟机（这样设计就会在单个程序崩溃的情况下不会导致整个系统的崩溃）。

**注意：在设置了heapgrowthlimit的情况下，单个进程可用最大内存为heapgrowthlimit值** 。在android开发中，如果要使用大堆，需要在manifest中指定android:largeHeap为true，这样dvm heap最大可达heapsize。

不同设备，这些个值可以不一样。一般地，厂家针对设备的配置情况都会适当的修改/system/build.prop文件来调高这个值。随着设备硬件性能的不断提升，从最早的16M限制（G1手机）到后来的24m,32m，64m等，都遵循Android框架对每个应用的最小内存大小限制，参考http://source.android.com/compatibility/downloads.html 3.7节。


通过代码查看每个进程可用的最大内存，即heapgrowthlimit值：
ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
int memClass = activityManager.getMemoryClass(); // 64，以m为单位

或：

`$adb shell getprop dalvik.vm.heapgrowthlimit`

256m（OOM的大小）

`$adb shell getprop dalvik.vm.heapsize`

384m（虚拟机极限内存）

`$adb shell getprop dalvik.vm.heapstartsize`
5m（应用启动时立即分配的内存）

## 参考
<https://www.cnblogs.com/onelikeone/p/7112184.html>
