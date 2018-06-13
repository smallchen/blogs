## Application.mk语法

参考<https://developer.android.com/ndk/guides/application_mk>
<https://blog.csdn.net/zhou452840622/article/details/78135200>

#### 例子

```makefile
# Application.mk
APP_ABI := armeabi armeabi-v7a
APP_OPTIM := release
APP_STL := stlport_static
NDK_TOOLCHAIN_VERSION = 4.9
APP_CPPFLAGS := -frtti -std=c++11
```

本文档介绍 Application.mk 构建文件，此文件用于描述应用需要的原生模块。 模块可以是静态库、共享库或可执行文件。

建议在阅读本页之前先阅读概念和 Android.mk 页面。 这样有助于您最深入地了解本页的内容。

#### 概览

`Application.mk`文件实际上是定义要编译的多个变量的微小 GNU Makefile 片段。 它通常位于`$PROJECT/jni/`下，其中`$PROJECT` 指向应用的项目目录。 另一种方式是将其放在顶级`$NDK/apps/`目录的子目录下。 例如：

`$NDK/apps/<myapp>/Application.mk`

这里的 <myapp> 是用于向 NDK 构建系统描述应用的短名称。它不会实际进入生成的共享库或最终软件包。

#### 变量

##### APP_PROJECT_PATH
此变量用于存储应用项目根目录的绝对路径。构建系统使用此信息将生成的 JNI 共享库的简缩版放入 APK 生成工具已知的特定位置。

如果将 Application.mk 文件放在 $NDK/apps/<myapp>/ 下，则必须定义此变量。 如果将其放在 $PROJECT/jni/ 下，则此变量可选。

##### APP_OPTIM
将此可选变量定义为 release 或 debug。在构建应用的模块时可使用它来更改优化级别。

发行模式是默认模式，可生成高度优化的二进制文件。调试模式会生成未优化的二进制文件，更容易调试。

请注意，您可以调试发行或调试二进制文件。但发行二进制文件在调试时提供的信息较少。 例如，构建系统会选择某些合适的变量，您无需检查它们。 此外，代码重新排序可能增大单步调试代码的难度；堆叠追踪可能不可靠。

在应用清单的 <application> 标记中声明 android:debuggable 将导致此变量默认使用 debug而非 release。 将 APP_OPTIM 设置为 release 可替换此默认值。

##### APP_CFLAGS
此变量用于存储构建系统在为任何模块编译任何 C 或 C++ 源代码时传递到编译器的一组 C 编译器标志。 您可使用此变量根据需要它的应用更改指定模块的版本，而无需修改 Android.mk 文件本身。

这些标志中的所有路径应为顶级 NDK 目录的相对路径。例如，如果您有以下设置：

```java
sources/foo/Android.mk
sources/bar/Android.mk
```

要在`foo/Android.mk`中指定您在编译时要添加指向 bar 源文件的路径，应使用：

`APP_CFLAGS += -Isources/bar`
或者：
`APP_CFLAGS += -I$(LOCAL_PATH)/../bar`

`-I../bar`在其等于`-I$NDK_ROOT/../bar`后不会运行。

注：此变量仅适用于 android-ndk-1.5_r1 中的 C 源文件，而不适用于 C++ 源文件。 在该版本后的所有版本中，APP_CFLAGS 匹配整个 Android 构建系统。

##### APP_CPPFLAGS
此变量包含构建系统在仅构建 C++ 源文件时传递到编译器的一组 C++ 编译器标志。

注：在 android-ndk-1.5_r1 中，此变量适用于 C 和 C++ 源文件。 在 NDK 的所有后续版本中，APP_CPPFLAGS 现在匹配整个 Android 构建系统。 对于适用于 C 和 C++ 源文件的标志，请使用 APP_CFLAGS。

##### APP_LDFLAGS
构建系统在链接应用时传递的一组链接器标志。此变量仅在构建系统构建共享库和可执行文件时才相关。 当构建系统构建静态库时，会忽略这些标志。

##### APP_BUILD_SCRIPT
默认情况下，NDK 构建系统在 jni/ 下查找名称为 Android.mk 的文件。

如果要改写此行为，可以定义 APP_BUILD_SCRIPT 指向替代构建脚本。 构建系统始终将非绝对路径解释为 NDK 顶级目录的相对路径。

##### APP_ABI
默认情况下，NDK 构建系统为 armeabi ABI 生成机器代码。 此机器代码对应于基于 ARMv5TE、采用软件浮点运算的 CPU。 您可以使用 APP_ABI 选择不同的 ABI。 表 1 所示为不同指令集的 APP_ABI 设置。

表 1. APP_ABI 不同指令集的设置。

| 指令集 | 值     |
| :------------- | :------------- |
| 基于 ARMv7 的设备上的硬件 FPU 指令 | APP_ABI := armeabi-v7a       |
| ARMv8 AArch64	| APP_ABI := arm64-v8a
| IA-32	| APP_ABI := x86
| Intel64	| APP_ABI := x86_64
| MIPS32	| APP_ABI := mips
| MIPS64 (r6)	| APP_ABI := mips64
| 所有支持的指令集	| APP_ABI := all
> 注：all 从 NDKr7 开始可用。

您也可以指定多个值，将它们放在同一行上，中间用空格分隔。例如：

`APP_ABI := armeabi armeabi-v7a x86 mips`

如需了解所有支持的 ABI 列表及其用法和限制的详细信息，请参阅 ABI 管理。

##### APP_PLATFORM
此变量包含目标 Android 平台的名称。例如，android-3 指定 Android 1.5 系统映像。 如需平台名称和对应 Android 系统映像的完整列表，请参阅 Android NDK 原生 API。

##### APP_STL
默认情况下，NDK 构建系统为 Android 系统提供的最小 C++ 运行时库 (system/lib/libstdc++.so) 提供 C++ 标头。 此外，它随附您可以在自己的应用中使用或链接的替代 C++ 实现。请使用 APP_STL 选择其中一个。 如需了解有关支持的运行时及其功能的信息，请参阅 NDK 运行时和功能。

##### APP_SHORT_COMMANDS
相当于 Application.mk 中的 LOCAL_SHORT_COMMANDS，适用于整个项目。如需了解详细信息，请参阅 Android.mk 上此变量的相关文档。

##### NDK_TOOLCHAIN_VERSION
将此变量定义为 4.9 或 4.8 以选择 GCC 编译器的版本。 64 位 ABI 默认使用版本 4.9 ，32 位 ABI 默认使用版本 4.8。要选择 Clang 的版本，请将此变量定义为 clang3.4、clang3.5 或 clang。 指定 clang 会选择 Clang 的最新版本。

##### APP_PIE
从 Android 4.1（API 级别 16）开始，Android 的动态链接器支持位置独立的可执行文件 (PIE)。 从 Android 5.0（API 级别 21）开始，可执行文件需要 PIE。要使用 PIE 构建可执行文件，请设置 -fPIE 标志。 此标志增大了通过随机化代码位置来利用内存损坏缺陷的难度。 默认情况下，如果项目针对android-16 或更高版本，ndk-build 会自动将此值设置为 true。您可以手动将其设置为 true 或 false。

此标志仅适用于可执行文件。它在构建共享或静态库时没有影响。

> 注：PIE 可执行文件无法在 4.1 版之前的 Android 上运行。

此限制仅适用于可执行文件。它在构建共享或静态库时没有影响。

##### APP_THIN_ARCHIVE
在 Android.mk 文件中为此项目中的所有静态库模块设置 LOCAL_THIN_ARCHIVE 的默认值。 如需了解详细信息，请参阅 Android.mk 文档中的LOCAL_THIN_ARCHIVE。
