## Android JNI 错误集锦

写在开头

很多错误很诡异，原因大部分是环境问题，而不是脚本执行问题。因为脚本执行时，都会有提示。如果按照提示修复还是失败，大概就是环境问题了。

* 路径错误。
* 脚本工具版本不一致。
* 其它环境不一致。

比如。
* 使用的`ndk-build`版本不一致，出现别人的能够构建，自己的构建失败。更新`NDK`就编译通过了。
* 脚本怎么修复都失败，结果发现，脚本中设置的相对路径错误，继而引发了脚本内部另一个错误。



1.`android.useDeprecatedNdk` is no longer supported.

> Error:Execution failed for task ':app:compileDebugNdk'.
> Error: Flag android.useDeprecatedNdk is no longer supported and will be removed in the next version of Android Studio.  Please switch to a supported build system.
  Consider using CMake or ndk-build integration. For more information, go to:
   https://d.android.com/r/studio-ui/add-native-code.html#ndkCompile
   To get started, you can use the sample ndk-build script the Android
   plugin generated for you at:
   /Users/jokinkuang/Documents/examples/JniDemo/app/build/intermediates/ndk/debug/Android.mk
  Alternatively, you can use the experimental plugin:
   https://developer.android.com/r/tools/experimental-plugin.html
  To continue using the deprecated NDK compile for another 60 days, set
  android.deprecatedNdkCompileLease=1528700258008 in gradle.properties

可以看到一个信息，在AndroidStudio环境下编译JNI，只能使用`CMake`或者`ndk-build`。

解决方案：

把`gradle.properties`里面的`android.useDeprecatedNdk=true`删除。

2.Your project contains C++ files but it is not using a supported native build system.

> Error:Execution failed for task ':app:compileDebugNdk'.
> Error: Your project contains C++ files but it is not using a supported native build system.
  Consider using CMake or ndk-build integration. For more information, go to:
   https://d.android.com/r/studio-ui/add-native-code.html
  Alternatively, you can use the experimental plugin:
   https://developer.android.com/r/tools/experimental-plugin.html

意思是，当前项目，既没有使用`CMake`也没有使用`ndk-build`，所以无法编译`C++`代码。可以参考以上链接，集成对应的构建环境。

解决方案1：

在`app.gradle`文件中，添加

```java
android {
	sourceSets {
		main.jni.srcDirs = []
	}
}
```

3.NDK integration is deprecated in the current plugin.

> Error:Execution failed for task ':app:compileDebugNdk'.
> Error: NDK integration is deprecated in the current plugin.  Consider trying the new experimental plugin.  For details, see http://tools.android.com/tech-docs/new-build-system/gradle-experimental.  Set "android.useDeprecatedNdk=true" in gradle.properties to continue using the current NDK integration.

可见，这是旧的NDK集成构建，在旧的gradle版本是支持的（2.0），在新的gradle版本已经不支持(3.0)。不支持会提示上面的错误(`android.useDeprecatedNdk` is no longer supported)。当然，如果你使用的是`gradle2.0`，那么可以使用`android.useDeprecatedNdk=true`来继续使用这个NDK集成构建环境。

4.Android NDK: The armeabi ABI is no longer supported. Use armeabi-v7a

> Android NDK: APP_PLATFORM not set. Defaulting to minimum supported version android-14.    
Android NDK: WARNING: APP_STL stlport_static is deprecated and will be removed in the next release. Please switch to either c++_static or c++_shared. See https://developer.android.com/ndk/guides/cpp-support.html for more information.    
Android NDK: The armeabi ABI is no longer supported. Use armeabi-v7a.    
Android NDK: NDK Application 'local' targets unknown ABI(s): armeabi    
Android NDK: Please fix the APP_ABI definition in /Users/jokinkuang/Documents/projects/jokin/src/main/jni/Application.mk    
/Users/jokinkuang/Library/Android/sdk/ndk-bundle/build/core/setup-app.mk:79: *** Android NDK: Aborting    .  Stop.

1、APP_STL `stlport_static` is deprecated，使用 `c++_static` or `c++_shared` 。

2、The `armeabi` ABI is no longer supported，使用`armeabi-v7a`.

5.add_library CMake Error: CMake can not determine linker language for target: FirstJni

> CMake Error at CMakeLists.txt:3 (add_library):
    src/main/jni/FirstJni.cpp
  Tried extensions .c .C .c++ .cc .cpp .cxx .m .M .mm .h .hh .h++ .hm .hpp
  .hxx .in .txx
CMake Error: CMake can not determine linker language for target: FirstJni
-- Generating done
-- Build files have been written to: /Users/jokinkuang/Documents/examples/CMakeJni/app/.externalNativeBuild/cmake/release/armeabi-v7a

先确保，`CMakeFiles.txt`中，源代码路径是否正确！！！！
然后Google吧。
