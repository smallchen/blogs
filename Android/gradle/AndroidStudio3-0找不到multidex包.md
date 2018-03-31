Android Studio 3.0 引入multidex报找不到pom错误。
```java
Unable to resolve dependency for ':app@platform551Debug/compileClasspath': Could not resolve com.android.support:multidex:1.0.3.

Could not resolve com.android.support:multidex:1.0.3.
Required by:
    project :app
 > Could not resolve com.android.support:multidex:1.0.3.
    > Could not get resource 'http://mvn.gz.jokin.cn/nexus/content/repositories/snapshots/com/android/support/multidex/1.0.3/multidex-1.0.3.pom'.
          > Could not HEAD 'http://mvn.gz.jokin.cn/nexus/content/repositories/snapshots/com/android/support/multidex/1.0.3/multidex-1.0.3.pom'. Received status code 400 from server: Repository version policy: SNAPSHOT does not allow version: 1.0.3
```

如下，添加google的maven即可。

```java
        maven {
            url 'https://maven.google.com'
        }
```

```java
allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            url 'https://maven.google.com'
        }
}
```
