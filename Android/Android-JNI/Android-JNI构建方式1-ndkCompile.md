## JNI构建方式一，旧版的构建方式

为啥叫旧版，因为的确有点旧，以至于在新的AndroidStudio中工作失效。（其实旧版也是基于`ndk-build`的方式构建的。后面会提到）。



**旧版整合步骤：**

1.gradle版本为`2.0.0`和`2.10-all`

`build.gralde`修改为：

```
classpath 'com.android.tools.build:gradle:2.0.0'
```

`gradle-wrapper.properties`修改为：

```java
#Mon Dec 28 10:00:20 PST 2015
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-2.10-all.zip
```

2.`gradle.properties`中，设置

`android.useDeprecatedNdk=true`

如果不设置，会提示：

> Error:Execution failed for task ':app:compileDebugNdk'.
> Error: NDK integration is deprecated in the current plugin.  Consider trying the new experimental plugin.  For details, see http://tools.android.com/tech-docs/new-build-system/gradle-experimental.  Set "android.useDeprecatedNdk=true" in gradle.properties to continue using the current NDK integration.

意思是：这个NDK集成构建方式已经比较旧了，请选择新的集成方式；你也可以设置`android.useDeprecatedNdk=true`来继续使用这种方式。

3.安装NDK构建环境。

这个很普遍了，在插件那，安装勾选`Android NDK Support`。
如果不行，安装`Android-SDK`中`SDK Tools`下，NDK／CMake／LLDB这三个东西。（这可能是新的NDK构建方式，见另一篇，入门）

4.环境准备就绪。

5.创建一个`JAVA类`，包含`native`方法。

```java
package com.example.administrator.jni;
public class FirstJni {
    static {
        System.loadLibrary("FirstJni");
    }
    public native int get();
    public native void set(int val);
}
```
> loadLibrary里的模块，是后面才指定的。

6.在`src/main/java`目录下(注意是java目录，不是main目录)，执行

`javah com.example.administrator.jni.FirstJni`
或
`javah -jni com.example.administrator.jni.FirstJni`

7.得到头文件`com_example_administrator_jni_FirstJni.h`

8.新建`jni`源文件目录，通常是`src/main/jni`。
> main/java是JAVA，main/jni是JNI

9.将头文件复制到`src/main/jni`目录下。

10.实现头文件，实现文件为`FirstJni.cpp`，放在目录`src/main/jni`下。

```java
#include "com_example_administrator_jni_FirstJni.h"

int value;

JNIEXPORT jint JNICALL Java_com_example_administrator_jni_FirstJni_get
        (JNIEnv *jenv, jobject jobj) {
    return value;
}

JNIEXPORT void JNICALL Java_com_example_administrator_jni_FirstJni_set
        (JNIEnv *jenv, jobject jobj, jint a) {
    value = a + 2222;
}
```
勿忘记，include头文件。

11.NDK环境，C++源文件都准备完毕，开始配置构建。

12.在`build.gradle`里面，配置`so`文件输出配置。

```java
buildTypes {
    release {
        ndk{
            moduleName "FirstJni"             //生成的so名字
            abiFilters "armeabi-v7a", "x86"  //输出指定三种abi体系结构下的so库。
        }
    }

    debug{
        ndk{
            moduleName "FirstJni"             //生成的so名字
            abiFilters  "armeabi-v7a", "x86"  //输出指定三种abi体系结构下的so库。
        }
    }
}
```
其中，
`moduleName`是，最终生成的`so`库文件名。
`abiFilters`是，只生成指定的芯片体系结构下的`so`库。如果不填，表示全部。

全部有：
* armeabi
* armeabi-v7a
* x86
* mips64
* mips
* x86_64
* arm64-v8a

> 怎么得到的？不填，然后构建出apk，`analyzeAPK`打开就可以看到`lib`目录下列表。

13.使用包含native方法的类，调用`native`方法。

```java
FirstJni jni = new FirstJni();
jni.set(11111);
Log.e(TAG, "from native:" + jni.get());
```

14.直接构建运行即可。

### 总结

1.整合的方式，是不能直接输出`so`文件的。output目录下并不会有任何`so`文件。

2.那么怎么得到`so`文件呢？
`Build`-`Rebuild Project`，
然后在`output/intermediates/ndk`下，可以看到所有的NDK构建相关文件。

* lib目录下，有所有的`so`文件。
* obj目录下，有所有的中间产物。
* `Android.mk`文件，是`ndk-build`的脚本文件!!!!!

于是，你可以将这个`Android.mk`文件，拷贝到`src/main/jni`目录下。然后在命令行中执行。

`~/Library/Android/sdk/ndk-bundle/ndk-build`

你会发现，成功构建出`libs`和`obj`目录，其中`libs`目录就包含所有的`so`文件。

附上这个由IDE产生的`Android.mk`文件。

```java
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := FirstJni
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
    Documents/examples/jni/app/src/main/jni/FirstJni.cpp \

LOCAL_C_INCLUDES += Documents/examples/jni/app/src/main/jni
LOCAL_C_INCLUDES += Documents/examples/jni/app/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
```

3.这个方式，配置还是蛮简单的，不知道为何被废弃。

Demo见`OldJni`
<https://github.com/jokinkuang>
