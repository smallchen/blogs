## Android查看当前源码的版本号

如果当前有一份Android的源码，如何查看当前源码的版本号？

1. `build/core/version_defaults.mk` 查看 `PLATFORM_VERSION`的值。

2. `.repo/manifest.xml`查看。

3. 编译的时候可以看到打印输出`PLATFORM_VERSION`。

4. 如果源码编译过，则查看`out/XXX/system/build.prop`，查看`ro.build.version.release`的值。
