## Compile标记为过时
新版本 `Android Gradle plugin 3.0` 中 已经将 `compile` 标记为过时了，而取而代之的是 `implementation` 和 `api` 两个关键字。

`api` 关键字 其实等同于 `compile` 。

`implementation` ： 使用了该命令编译的依赖，它仅仅对当前的 Moudle 提供接口。例如我们当前项目结构如下：

```java
                      App
                     /    \
                  LibA    LibB
                   |        |
                  LibC    LibD
```

LibraryA 中引用了 LibraryC 的库，如果对 LibraryC 的依赖用的是 implementation 关键字。 如下：

```
dependencies {
    . . . . 
    implementation project（path：'：libraryC'）
}
```

那么 LibraryC 中的接口，仅仅只能给 LibraryA使用，而我们的 App Moudle 是无法访问到 LibraryC 提供的接口的，也就是将该依赖隐藏在内部，而不对外部公开。这就是`implementation`关键字的作用。

**那为什么要这么做呢？**
答案是： 1\. 加快编译速度。2\. 隐藏对外不必要的接口。

**为什么能加快编译速度呢？**
这对于大型项目含有多个 Moudle 模块的， 以上图为例，比如我们改动 LibraryC 接口的相关代码，这时候编译只需要单独编译 LibraryA 模块就行， 如果使用的是 api 或者旧时代的 compile，由于App Moudle 也可以访问到 LibraryC ,所以 App Moudle 部分也需要重新编译。当然这是在全编的情况下。

至于编译速度的对比，国外有个小哥已经做了简单的对比，效果还是不错。地址可点击原文跳转过去查看。

那么我们现有项目中的依赖如何修改呢? 答案是：将 compile 都修改为 implementation 然后尝试进行项目构建，如果构建成功那么恭喜你，如果构建不成功，则查看相关的依赖项，并将其修改为 api 关键字依赖。

作者：技术特工队
链接：https://juejin.im/post/5a0a71466fb9a045196918ab
来源：掘金

## Compile与Implementation在App依赖中体现

```java
App:
    compile(':librpcservice:R.0.1.29')
或
    implementation(':librpcservice:R.0.1.29')
```

**对于App而言，compile或implementation一样，都会加载aar/jar的依赖树。**
> App可以通过添加@aar来指定只使用aar，而不使用aar内部的依赖。

## Compile与Implementation在Module依赖中体现

`App -> :module:rpcservice -> librpcservice`

```java
App:
    compile project(path: ':rpcservice')
Module:
    api (':librpcservice:'+"${RPC_VERSION_CODE}") {
        exclude module: 'appcompat-v7'
        exclude group: 'com.android.support'
    }
或
    implementation (':librpcservice:'+"${RPC_VERSION_CODE}") {
        exclude module: 'appcompat-v7'
        exclude group: 'com.android.support'
    }
```

如上，
Module使用`compile/api`依赖librpcservice，dependencies依赖是：

```java
+--- project :rpcservice
|    \---:sideslipbar-sdk:D.3.1.240-SNAPSHOT
|         +--- com.google.code.gson:gson:2.8.2
|         \---:common-sdk:D.3.1.240-SNAPSHOT
|              +--- com.google.code.gson:gson:2.8.2
|              +---:libwsrpc:RC.3.3.2.43-SNAPSHOT
|              |    \--- com.google.code.gson:gson:2.8.2
|              \---:system-rpc-sdk:D.0.1.118-SNAPSHOT
|                   +--- com.google.code.gson:gson:2.8.2
|                   \---:library:1.6.0
```

Module使用`implementation`依赖librpcservice，dependencies依赖是：

```java
+--- project :rpcservice
```

（打印编译期依赖：`./gradlew app:dependencies --configuration "buildvariant+CompileClasspath"`）
（打印运行期依赖：`./gradlew app:dependencies --configuration "buildvariant+RuntimeClasspath"`）

**总结**：
Module层，可以通过compile/api使用aar的依赖树，也可以通过implementation来阻断aar的依赖树，使用@aar也可以阻断aar的依赖树。

## Compile与Implementation在aar中依赖的体现

如下：

`App -> librpcservice -> sideslipbar-sdk -> common-sdk`

其中，
App依赖librpcservice
librpcservice依赖sideslipbar-sdk
sideslipbar-sdk依赖common-sdk

其中，librpcservice使用`compile`依赖时，生成的pom文件是：

```java
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>com.xxx.jokin</groupId>
<artifactId>librpcservice</artifactId>
<version>R.0.1.29</version>
<packaging>aar</packaging>
<name>LibRpcService_master</name>
<dependencies>
<dependency>
<groupId>com.xxx.jokin</groupId>
<artifactId>sideslipbar-sdk</artifactId>
<version>D.3.1.240-SNAPSHOT</version>
<scope>compile</scope>
<exclusions>
<exclusion>
<artifactId>appcompat-v7</artifactId>
<groupId>*</groupId>
</exclusion>
<exclusion>
<artifactId>*</artifactId>
<groupId>com.android.support</groupId>
</exclusion>
</exclusions>
</dependency>
</dependencies>
</project>
```

1. 使用Compile依赖时，生成的pom文件中，包含有dependencies
2. 因为有dependencies属性，所以aar的依赖可以传递到App层
3. **虽然aar自身含有依赖，但如果Module层使用implementation，aar的依赖也会被阻断。**


librpcservice使用`Implementation／api`依赖时，生成的pom文件是：
```java
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>com.xxx.jokin</groupId>
<artifactId>librpcservice</artifactId>
<version>R.0.1.30</version>
<packaging>aar</packaging>
<name>LibRpcService_master</name>
</project>
```

1. 使用Implementation依赖时，生成的pom文件中，仅仅只有aar自身。
2. 由于没有任何依赖，对于App层，可见到只有当前的aar，至于aar所依赖的东西，App层不可见。
3. **即使App层使用Compile，也无法获取aar的依赖，因为aar自身没有包含依赖。**

按道理，compile与api一致，应该能够实现依赖传递，可能是pom文件生成插件没有更新，api还没支持，所以api输出也没有任何依赖（**这点，和上面module中api可以传递依赖不一致**）。

**总结：**
aar和module一样，可以通过使用compile／implementation来决定是否包含依赖。

## 总结
1. **对于App而言，`compile`和`implementation`一样**

2. 对于module／aar／jar而言，使用`compile`表示包含依赖，App访问它们时，也可以访问它们的依赖；使用`implementation`表示不暴露依赖，App访问它们时，不能访问它们的依赖。

2. 所以，要理清一个App编译／运行时对应的依赖树，需要层层分析module/aar/jar层是否有包含依赖。只有从上到下的路被打通，才能将底层的依赖暴露给上层。**（即每一层都使用compile才能将底层依赖暴露给上层）**

3. 除了从代码中分析，也可以通过命令行辅助：
如果编译时，找不到依赖而报错，可以使用`./gradlew app:dependencies --configuration "buildvariant+CompileClasspath"`查看编译时依赖树。
如果运行时，找不到依赖而崩溃，可以使用`./gradlew app:dependencies --configuration "buildvariant+RuntimeClasspath"`查看运行时依赖树。

4. 如果使用compile依赖一个aar，打印的依赖树中，该aar没有子依赖，说明该aar没有暴露任何依赖。

5. **使用@aar可以只包含aar而不包含aar的子依赖**
`compile ('com.xxx.jokin:librpcservice:R.0.1.30@aar')`
