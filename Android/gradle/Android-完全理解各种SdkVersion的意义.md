## Android 完成理解各种SdkVersion的意义

如下，一个常见的配置：

```java
android {
    compileSdkVersion 25
    buildToolsVersion "28.0.0"
    defaultConfig {
        applicationId "com.osanwen.nettydemo"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    compile 'com.android.support:appcompat-v7:25.3.1'
}
```

* buildToolsVersion IDE版本。升级到和AS对应的版本号即可，不需要降级。
* compileSdkVersion 当前项目使用的编译版本。代码里Android SDK使用的版本。还取决于`targetSdkVersion`，编译API版本一定要比目标API版本高。通常相等即可。（你不用更高的API编译，怎么能够编译出目标版本？）
* minSdkVersion    当前项目最低支持的API，目前基本是21（Android5.0）
* targetSdkVersion 当前项目最高支持的API，取决于项目自身，通常就是最新API版本。

解释一下上面的配置：

由于当前系统环境使用的是`Android Studio 3.0.1`，对应的`Build Tools`是`28.0.0`，而BuildTools是向后兼容的（向旧兼容）。所以，对于本机来说，直接升级`BuildTools`到`28.0.0`是最理想的，马上就能解决`BuildTools`不存在的问题。

但这样做会引起另一个问题：协同工作的其它电脑，安装的AS版本也必须高于`3.0.1`，否则低版本的AS是没有且不支持BuildTools 28.0.0的。所以，这个项目配置到了另一台机很可能就不能工作了。

所以，`BuildTools`需要共同协商好，要基于协作的所有AS版本中，使用的最低的AS版本对应的`BuildTools`版本才是合理的。更高的AS版本，则使用`Install Missing Build Tools`来修复。这样就可以解决，同一个项目在更低的AS版本中都可以完成编译构建。

这样，就可以理解，`buildToolsVersion`其实指定的是，项目支持的最低的`Android Studio`版本。如果你使用了更低的版本，则需要先升级AS。

另外发现，`BuildTools`是可以删除的，如果不存在，AS会使用默认的对应的`BuildTools`，你可以看到，`BuildTools`是字符串参数，而其它几个是数值参数。`BuildTools`要大于或等于`compileSdkVersion`，相当于，只有更高版本的IDE，才能支持对应的编译版本。

> 注：gradle:3.0.1才会默认添加buildTools，旧的gradle版本会提示缺失buildTools！！

接下来，我们先理解`minSdkVersion`和`targetSdkVersion`。

这两个参数，指定的是当前项目，兼容的Android系统版本。从`minSdk ~ targetSdk`之间的Android系统，都可以安装和运行当前项目应用。所以，这两个参数应该比较容易理解。也很容易理解，为什么`minSdkVersion`必定要小于`targetSdkVersion`。

最后，再理解`compileSdkVersion`就不费劲了。

由于项目目标是`min ~ target`之间的系统，要编译这样一个应用，当然需要使用相等或更高的API编译环境。所以，`compileSdkVersion`要等于或者大于`minSdkVersion`和`targetSdkVersion`（minSdkVersion必定要小于targetSdkVersion，所以只需大于或等于targetSdkVersion）。通常，我们取相等的版本即可。

最后的最后，理解下`dependencies`里的`compile`版本。

由于编译的API版本是`compileSdkVersion`指定的版本。所以项目依赖的其它编译组件，自然要大于或等于`compileSdkVersion`。否则，你就是在使用旧的编译工具来编译新的目标，很容易报错。

所以，你会看到，上面的`compileSdkVersion`为`25`时，依赖的Android组件，要么也为`25`，要么就大于`25`。如果小于`25`，AndroidStudio就会提示：`the support library should be 25 or higher`。

至此，终于完全理解，gradle里配置的各个`sdk version`的意义，和依赖中`compile version`的意义。
