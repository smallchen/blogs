**项目结构**：
```java
    flavorDimensions "platform"
    productFlavors {
        platform3399 {
            dimension "platform"
            if (!isBuildInJenkins()) {
                signingConfig signingConfigs.debug3399
            }
        }
        platform551 {
            dimension "platform"
            if (!isBuildInJenkins()) {
                signingConfig signingConfigs.debug3399
            }
        }
        platform5508 {
            dimension "platform"
            applicationId "com.jokin"
            minSdkVersion 19
            if (!isBuildInJenkins()) {
                signingConfig signingConfigs.debug5508
            }
        }
    }
```
如上，产生`1*3*2`个variants。（variant由`Dimensions * Flavor * 2`产生）

```java
platform3399Debug
platform3399Release
platform551Debug
platform551Release
platform5508Debug
platform5508Release
```

需要查看的依赖，主要是以上variants的依赖。

**步骤**

1. ./gradlew :app:dependencies 查看app项目所有配置的依赖（**app前缀必须有！否则出错！**）（打印的是--configuration参数的所有配置的依赖）

```java
platform3399DebugCompileClasspath - Resolved configuration for compilation for variant: platform3399Debug
// 后面是配置的解释。表示是什么配置。
// 如上，platform3399DebugCompileClasspath 表示的是platform3399Debug
```

2. ./gradlew :app:dependencies  --configuration compile 查看compile选项的依赖（**不准确!，因为只是main flavor的依赖**）

```java
compile - Compile dependencies for 'main' sources (deprecated: use 'implementation' instead).
+--- :recordsdk-debug:
+---:libcvtouchcontrol:R.0.2.4
|    +---:libappcontract:R.0.2+ -> R.0.2.9
|    |    \---:libsystemcontract:latest.release -> R.0.2.11
|    \---:libcvtouchhelper:R.0.2+ -> R.0.2.26
|         +--- com.tencent.bugly:crashreport:2.6.5
|         +---:libvaidl:R.0.1.3
|         +---:friday-sdk-android:0.1.9-SNAPSHOT
|         \---:libsystemcontract:latest.release -> R.0.2.11
```

3. ./gradlew :app:dependencies  --configuration platform3399DebugCompileClasspath (**正确！platform3399Debug variant/flavor**)

```java
platform3399DebugCompileClasspath - Resolved configuration for compilation for variant: platform3399Debug
+--- :recordsdk-debug:
+---:libcvtouchcontrol:R.0.2.4
|    +---:libappcontract:R.0.2+ -> R.0.2.9
|    |    \---:libsystemcontract:latest.release -> R.0.2.11
|    \---:libcvtouchhelper:R.0.2+ -> R.0.2.26
|         +--- com.tencent.bugly:crashreport:2.6.5
|         +---:libvaidl:R.0.1.3
|         +---:friday-sdk-android:0.1.9-SNAPSHOT
|         \---:libsystemcontract:latest.release -> R.0.2.11
```

总结：
1. ./gradlew :app:dependencies 查看app模块下所有配置的依赖。
`其中，app为模块`
2. ./gradlew :app:dependencies --configuration compile 查看compile配置的依赖，配置后面有一串字符串解释当前配置（**compile不准确**）。
`其中，通过--configuration指定配置项`

3. ./gradlew :app:dependencies --configuration platform3399DebugCompileClasspath （**准确的方式**）
`其中，platform3399Debug为variant。所以查看某个variant的依赖，指定的配置为variant+CompileClasspath`

4. 比较有用的是，variant+CompileClasspath，variant+RuntimeClasspath。两者的依赖树不一样。**编译期找不到依赖，查看variant+CompileClasspath；运行时找不到依赖，查看variant+RuntimeClasspath**。

5. variant指AndroidStudio里的Build Variant选项值。
