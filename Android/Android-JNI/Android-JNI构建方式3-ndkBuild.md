## JNI构建方式三，NDK Build的构建方式

If you're using the deprecated `ndkCompile`, you should migrate to using either `CMake` or `ndk-build`.

Because `ndkCompile` generates an intermediate `Android.mk` file for you, migrating to `ndk-build` may be a simpler choice.

意思是，旧版JNI构建方式，叫`ndkCompile`，如果是旧版的构建方式，那么你要升级。要么升级为`CMake`，要么升级为`ndk-build`。

由于旧版的`ndkCompile`会在`build/intermediates/ndk/debug`目录下生成了`Android.mk`，所以转化为`ndk-build`的方式是非常简单的。（因为两者都是通过mk文件编译，且`Android.mk`已经自动生成了。）

这里的`ndk-build`的方式和旧版的`ndk集成`区别在于：

旧版不需要编写任何脚本，自动编译构建。新版需要自行编写`Android.mk`和`Application.mk`文件，并且整合到`gralde`来进行编译。

> **如果是新项目，建议直接使用CMake了**

### ndk-build的JNI构建方式入门

1.删除`android.useDeprecatedNdk=true`。

2.在`Android-SDK`中`SDK Tools`下，安装`NDK／CMake／LLDB`这三个东西。（已安装忽略）

3.现存项目已经有完整的C++。那么可以编写`Android.mk`和`Application.mk`。放在`src/main/jni`目录下。

```java
# Android.mk
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := FirstJni
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := FirstJni.cpp

LOCAL_C_INCLUDES += ./

include $(BUILD_SHARED_LIBRARY)
```

`Application.mk`是配置构建环境。简单的可以不配置。

另：如果当前项目已经是使用旧的`ndkCompile`的方式构建的，那么`make`一下，在`build/intermediates/ndk/debug`下就已经存在一个自动生成的`Android.mk`。直接拷贝这个也是可以的！！


4.打开命令行，在`src/main/jni`目录下执行：

`app/src/main/jni$ ~/Library/Android/sdk/ndk-bundle/ndk-build`

大功告成，so文件生成成功。

5.整合到`gradle`脚本。修改`app/build.gradle`，引入`Android.mk`：

```java
android {
    externalNativeBuild {
        ndkBuild {
            path 'src/main/jni/Android.mk'
        }
    }
}
```

这一步也可以使用UI：左侧项目中`app`-右键-`Linked C++ Support`-选择`ndk-build`，然后浏览到对应的`Android.mk`即可。会自动生成上述代码。

5.`Sync`同步，运行即可。

以上修改，基于上一篇`JNI构建方式1`，在`ndk-build`基础上进行修改。


#### Gradle整合Application.mk

Gradle中不支持直接指定`Application.mk`，需要将`Application.mk`配置转换为`Gradle配置`。

```makefile
# Application.mk
APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions -std=gnu++11 -Wno-format-contains-nul -g -Wno-deprecated-declarations
APP_CPPFLAGS +=-fpermissive
APP_PLATFORM := android-14
APP_ABI := armeabi-v7a
```

```java
android {
defaultConfig {
    // ...
    externalNativeBuild {
        ndkBuild {
            arguments 'APP_STL=gnustl_static',
                'APP_PLATFORM=android-14',
                'NDK_TOOLCHAIN_VERSION=4.9'
            cppFlags '-frtti',
                '-fexceptions',
                '-std=gnu++11',
                '-Wno-format-contains-nul',
                '-g',
                '-Wno-deprecated-declarations',
                '-fpermissive'
        }
    }
    ndk {
        abiFilters 'armeabi-v7a'
    }
}
}
```

### 附录

Demo见`NDKJni`
<https://github.com/jokinkuang>
