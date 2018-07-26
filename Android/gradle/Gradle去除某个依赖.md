
## Gradle 去除某个依赖

```java
    configurations {
        compile.exclude module: 'commons'
        all*.exclude group: 'org.gradle.test.excludes', module: 'reports'
    }

  // Project去除依赖
    p551Compile(project(path: ':output')) {
        exclude group: 'org.jetbrains'
    }

  // aar去除依赖
    p551Implementation (':librpcservice:R.0.1.29') {
        exclude module: 'appcompat-v7'
        exclude group: 'com.android.support'
        exclude group: 'com.alibaba.fastjson'
        exclude module: 'fastjson'
        // exclude 'com.android.support:support-annotations'
        exclude group: 'com.android.support', module: 'support-annotations'
        // exclude 'org.json: json'
        exclude group: 'org.json', module: 'json'
    }

   // 加上force = true表明的意思就是即使在有依赖库版本冲突的情况下坚持使用被标注的这个依赖库版。
   compile('com.squareup.okhttp:okhttp-mt:2.5.0') {
        force = true
   }
```

### exclude group 和 exclude module的区别

以`com.android.support:support-annotations`为例：

上面的理解是，

* exclude group是去除包名`com.android.support:*`。
* exclude module是去除模块名`*:support-annotations`。
* exclude group, module则是去除确定的`com.android.support:support-annotations`包名以及模块。

就类似于：

* 包匹配过滤
* 模块匹配过滤
* 包+模块匹配过滤

其中，包匹配，模块匹配都测试通过，包+模块都测试通过。

**如果你发现不生效，那么请一个字母一个字母对一下，肯定包或模块名写错了！**

另外，如果有多个同名依赖，默认会使用最新版本！你会看到`->`箭头指向新的版本。

```java
android {
        packagingOptions {
            pickFirst 'lib/armeabi-v7a/libc++_shared.so'
            pickFirst 'lib/arm64-v8a/libc++_shared.so'
            exclude 'META-INF/DEPENDENCIES'
            exclude 'META-INF/NOTICE'
            exclude 'META-INF/LICENSE'
            exclude 'META-INF/LICENSE.txt'
            exclude 'META-INF/NOTICE.txt'
        }
}
```

只使用Application的AndroidManifest.xml

```java
android{
    sourceSets {
            main {
                manifest.srcFile 'src/main/AndroidManifest.xml'
            }
    }
}
```
