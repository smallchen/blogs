## Android JNI

一入JNI，深似海。

### JNI 与 NDK 与 ABI

JNI（Java Native Interface）即Java本地接口

NDK（Native Development Kit）Android的一个开发JNI的工具包

**JNI是属于JAVA语言的**，和Android平台无关。而NDK是属于Android平台的工具。

NDK是android为了更高效的开发jni，并且更方便的集成到apk中而开发的一个android ide工具。

NDK还提供平台库（`platform library`）！支持访问native层的Activity，支持访问硬件，比如感应器，触摸输入设备等。

### 为什么需要JNI

在JAVA环境下调用c／c++的代码，或c／c++中访问JAVA环境，JNI提供了JAVA和C／C++相互交互的能力。

* 可以直接使用现存C／C++现行的开源库，引入C++庞大的支持库。
* 可以在某些场景下，提高代码的执行效率。C／C++要比JAVA运行效率高。比如，音视频，编解码，运算要求高等方面。
* JAVA容易被反编译，C／C++层可以更好的保护代码安全。
* 复用性更高，可支持环境比JAVA更广泛。

### JNI实例

#### 准备环境

1.下载 NDK 和构建工具，要为您的应用编译和调试原生代码，您需要以下组件：

`Android 原生开发工具包 (NDK)`：这套工具集允许您为 Android 使用 C 和 C++ 代码，并提供众多平台库，让您可以管理原生 Activity 和访问物理设备组件，例如传感器和触摸输入。

`CMake`：一款外部构建工具，可与 Gradle 搭配使用来构建原生库。如果您只计划使用 ndk-build，则不需要此组件。

`LLDB`：一种调试程序，Android Studio 使用它来调试原生代码。
您可以使用 SDK 管理器安装这些组件：

2.在打开的项目中，从菜单栏选择 Tools > Android > SDK Manager。

3.点击 SDK Tools 标签。

4.选中`LLDB`、`CMake` 和 `NDK` 旁的复选框，安装。

#### 新建项目

1.New项目

2.勾选`Include C++ Support`

3.配置

`C++ Standard`：使用下拉列表选择您希望使用哪种 C++ 标准。选择 Toolchain Default 会使用默认的 CMake 设置。

`Exceptions Support`：如果您希望启用对 C++ 异常处理的支持，请选中此复选框。如果启用此复选框，Android Studio 会将`-fexceptions`标志添加到模块级 build.gradle 文件的`cppFlags`中，Gradle 会将其传递到 CMake。

`Runtime Type Information Support`：如果您希望支持 RTTI，请选中此复选框。如果启用此复选框，Android Studio 会将`-frtti`标志添加到模块级 build.gradle 文件的`cppFlags`中，Gradle 会将其传递到 CMake。

如下：

```java
android {
	defaultConfig {
		externalNativeBuild {
			cmake {
				cppFlags "-frtti -fexceptions"
			}
		}
	}
}
```

```java
defaultConfig {
	externalNativeBuild {
			ndkBuild {
				arguments "NDK_APPLICATION_MK:=src/main/jni/Application.mk"
				cFlags "-DTEST_C_FLAG1", "-DTEST_C_FLAG2"
				cppFlags "-DTEST_CPP_FLAG2", "-DTEST_CPP_FLAG2"
				abiFilters "armeabi-v7a", "armeabi"
			}
		}
}
```

4.可选择性的，在`gradle.properties`添加：

```java
android.useDeprecatedNdk=true
```



1.在某个包名下，创建一个包含Native方法的java类。

```java
package com.jokin.demo.jni;

public class JNativeString {
    public native String getStringFromC();
}
```

2.命令行下，切换到`src/main/java`下，即上面包名`com.jokin.demo.jni`的父目录(注意是java目录，不是main目录)。运行下面的命令，生成`.h`头文件。

```java
[/JniDemo/app/src/main/java$] javah -jni com.jokin.demo.jni.JNativeString
```

3.运行后，会发现`src/main/java`目录下，即包名的父目录下，产生了一个头文件。
`com_jokin_demo_jni_JNativeString.h`

4.


### JNI编译

* cmake
* ndk-build

### 错误修复

1.如果编译失败，如何找原因？

`ndk-build NDK_LOG=1`       
打印出内部的NDK日志信息，可以看到编译过程很多信息。



参考<https://developer.android.com/studio/projects/add-native-code?utm_source=android-studio>
<https://developer.android.com/ndk/guides/ndk-build?hl=zh-cn>
<https://developer.android.com/ndk/guides/cpp-support?hl=zh-cn>
