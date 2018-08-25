## Android Failed to find Build Tools revision

```java
Error:Failed to find Build Tools revision 25.0.2

Consult IDE log for more details (Help | Show Log)
<a href="install.build.tools">Install Build Tools 25.0.2 and sync project</a>
```

原因：

当前Android Studio版本比较新，使用了更高的`Build Tools`，比如`Android 3.0.1`对应的`BuildTools`已经是`28.0.0`。

`BuildTools`会向后兼容（旧兼容），所以没必要按照提示，安装旧的`Build Tools`（25.0.2）。

升级`BuildTools`为当前`Android Studio IDE`对应的`BuildTools`版本即可解决问题！！！

其它sdkVesion的理解，参见另一篇《完全理解各种SdkVersion的意义》
