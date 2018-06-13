## Android JNI构建总结

#### 三种JNI构建方式

* ndkCompile
* CMake
* ndkBuild

其中，ndkCompile是比较旧的使用`NDK`构建的方式；新的`NDK`构建方式是`ndkBuild`，需要指定`Android.mk`和`Application.mk`来进行构建。

CMake是Google推荐的构建JNI的方式。只需要指定一个`CMakeList.txt`描述文件即可。CMake语法是通用的。

#### so的Release版本

默认就是release。
添加`APP_OPTIM := debug`启用debug，或者`Application的manifest`启用了`android:debuggable=true`，这时默认是debug，这个时候release才要显式设置。

#### so文件优化

默认release就是优化的，不需要额外进行优化。
> APP_OPTIM
> A 'release' mode is the default, and will generate highly optimized binaries.

参考<https://stackoverflow.com/questions/14564918/android-ndk-release-build/14579929#14579929>
