## Android Memory Profiler 查看内存

内存计数中的类别如下所示：

* Java：从 Java 或 Kotlin 代码分配的对象内存。
* Native：从 C 或 C++ 代码分配的对象内存。(比如：Bitmap)

即使您的应用中不使用 C++，您也可能会看到此处使用的一些原生内存，因为 Android 框架使用原生内存代表您处理各种任务，如处理图像资源和其他图形时，即使您编写的代码采用 Java 或 Kotlin 语言。

* Graphics：图形缓冲区队列向屏幕显示像素（包括 GL 表面、GL 纹理等等）所使用的内存。 （请注意，这是与 CPU 共享的内存，不是 GPU 专用内存。）

* Stack： 您的应用中的原生堆栈和 Java 堆栈使用的内存。 这通常与您的应用运行多少线程有关。

* Code：您的应用用于处理代码和资源（如 dex 字节码、已优化或已编译的 dex 码、.so 库和字体）的内存。

* Other：您的应用使用的系统不确定如何分类的内存。

* Allocated：您的应用分配的 Java/Kotlin 对象数。 它没有计入 C 或 C++ 中分配的对象。

当连接至运行 Android 7.1 及更低版本的设备时，此分配仅在 Memory Profiler 连接至您运行的应用时才开始计数。 因此，您开始分析之前分配的任何对象都不会被计入。 不过，Android 8.0 附带一个设备内置分析工具，该工具可记录所有分配，因此，在 Android 8.0 及更高版本上，此数字始终表示您的应用中待处理的 Java 对象总数。

## 参考
<https://developer.android.com/studio/profile/memory-profiler?hl=zh-cn>
