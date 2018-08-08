## Android Bugly接入

<https://bugly.qq.com/docs/user-guide/instruction-manual-android/?v=20180713114028#_4>

参考官网。

比较烦的是，看到了引入的库，但总是提示错误：找不到类`CrashReport`。

后来使用最新版：
`compile 'com.tencent.bugly:crashreport:latest.release'`

同步完成后才找到。旧的出错的配置是:

`compile 'com.tencent.bugly:crashreport:2.6.6'`

应该这样是拉去了错误的版本，使用最新版看到的版本号为`2.6.6.1`。

`compile 'com.tencent.bugly:crashreport:2.6.6.1'`

接入第三方，使用固定版本号可以避免下次编译由于最新版不兼容旧接口而报错。
