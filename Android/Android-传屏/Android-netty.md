## Android netty

### 下载和引入netty

<http://netty.io/>
<https://dl.bintray.com/netty/downloads/netty-4.1.28.Final.tar.bz2>

下载并将`netty-4.1.28.Final.jar`放到`libs`目录下即可。

### 下载和引入log4j

引入netty还不足够，运行会发现log输出警告错误：

```
I/iletransferdem: Caused by: java.lang.ClassNotFoundException: Didn't find class "org.apache.logging.log4j.spi.ExtendedLoggerWrapper"
```

这是因为缺失了`log4j`。下载放到`libs`目录即可。

```java
// https://mvnrepository.com/artifact/log4j/log4j
compile group: 'log4j', name: 'log4j', version: '1.2.14'
```

<http://mvnrepository.com/artifact/log4j/log4j/1.2.14>

### Netty入门
