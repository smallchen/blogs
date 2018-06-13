## JNI构建方式二，CMake的构建方式

Using Android Studio 2.2 and higher, you can use the NDK and CMake to compile C and C++ code into a native library. Android Studio then packages your library into your APK using Gradle, the IDE's integrated build system.
> Android Studio 2.2以上，你就可以使用`CMake`来编译C／C++代码了！AndroidStudio会通过`gradle`将`so`库打包进apk。

参考<https://developer.android.com/ndk/guides/cmake?hl=zh-cn>

**CMake是Android Studio下默认的编译JNI的工具**

但不用灰心，使用了`NDK`工具的朋友，也无需惊扰。由于现有大部分项目都是使用`NDK`构建的，所以AndroidStudio还是会支持`ndk-build`的编译方式！

但是，如果是新的项目，建议使用`CMake`!!!!
> 以上是官方原话翻译。
> https://developer.android.com/studio/projects/add-native-code

### CMake的JNI构建方式入门

最简单的入门Demo，是`New` - `New Project` - 勾选`Include C++ support` - 下一步 - 勾选`EmptyActivity` - `Finish`。

这样由AndroidStudio生成的项目，就是一个使用`CMake`编译的JNI项目，输出`Hello from C++`。

如果现存的项目，要接入`CMake`怎么办？

1.删除`android.useDeprecatedNdk=true`。

2.在`Android-SDK`中`SDK Tools`下，安装`NDK／CMake／LLDB`这三个东西。（已安装忽略）

3.现存项目已经有完整的C++，直接创建编写项目的构建文件`CMakeList.txt`，放在`app/`目录下（**路径非常重要**）。

```java
cmake_minimum_required(VERSION 3.4.1)

add_library( # so文件名
             FirstJni
             # 动态还是静态链接库
             SHARED
             # 源代码
             src/main/jni/FirstJni.cpp )
```

4.修改`app/build.gradle`，引入`CMakeList.txt`：

```java
android {
    externalNativeBuild {
        cmake {
            path 'CMakeLists.txt'
        }
    }
}
```
> 注意，android.externalNativeBuild是配置`CMakeList.txt`的路径。android.defaultConfig.externalNativeBuild是配置`CMake`命令的参数。有两个externalNativeBuild，两者不一样！！！

这一步可以使用UI：左侧项目中`app`-右键-`Linked C++ Support`-选择`CMake`，然后浏览到对应的`CMakeLists.txt`即可。会自动生成上述代码。

5.`Sync`同步，运行即可。

以上修改，基于上一篇`JNI构建方式1`，在`ndk-build`基础上进行修改。

可见，`CMake`构建方式的接入并不困难，真正困难的是，`CMake`的语法、编写、调试和错误解决（有的错误，错误提示并不会任何信息！下面你就知道）。

### 错误集锦（见JNI错误集锦）

1.add_library CMake Error: CMake can not determine linker language for target: FirstJni
> CMake Error at CMakeLists.txt:3 (add_library):
    src/main/jni/FirstJni.cpp
  Tried extensions .c .C .c++ .cc .cpp .cxx .m .M .mm .h .hh .h++ .hm .hpp
  .hxx .in .txx
CMake Error: CMake can not determine linker language for target: FirstJni
-- Generating done
-- Build files have been written to: /Users/jokinkuang/Documents/examples/CMakeJni/app/.externalNativeBuild/cmake/release/armeabi-v7a

看回`CMakeList.txt`文件

```java
cmake_minimum_required(VERSION 3.4.1)

add_library( # so文件名
             FirstJni
             # 动态还是静态链接库
             SHARED
             # 源代码
             src/main/jni/FirstJni.cpp )
```
看起来并没有错。

看回`gradle`配置

```java
android {
    externalNativeBuild {
        cmake {
            path '../CMakeLists.txt'
        }
    }
}
```
正确引入了`CMakeList.txt`文件。

此时，无论怎么改，你会发觉，都是错误的，只不过错误日志各种各样，甚至中途就结束，让人摸不着头脑。

还是那句，不要太依赖错误日志。

这个错误，是由于`add_library`里，指定的`src/main/jni`目录，并不不存在于`CMakeList.txt`文件所在的目录。

也就是说，`src/main/jni`是找不到的源文件的，所以它报`can not determine linker language`。

**所以，CMakeList.txt里的路径，是相对于CMakeList.txt文件所在的目录，源文件或头文件的相对路径需要设置正确！！**

Demo见`CMakeJni`
<https://github.com/jokinkuang>
