```java
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
    }

// exclude 'com.android.support:support-annotations'
     exclude group: 'com.android.support', module: 'support-annotations'

// exclude 'org.json: json'
     exclude group: 'org.json', module: 'json'
```

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
