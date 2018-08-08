## Android Flavors And Dimensions

```gradle
android {

  flavorDimensions "client", "server"
  productFlavors {
    client1 {
      dimension "client"
      applicationIdSuffix ".client1"
    }
    client2 {
      dimension "client"
      applicationIdSuffix ".client2"
    }
    dev {
      dimension "server"
    }
    staging {
      dimension "server"
    }
    production {
      dimension "server"
    }
}
```

以上，除了`DEBUG`和`RELEASE`，产生的构建组合为：

| client | server     | client + server     |
| :------------- | :------------- | :------------- |
| client1       | dev             | client1Dev     |
| client1       | staging         | client1Staging |
| client1       | production      | client1Production     |
| -------       | ---------       | ---------      |
| client2       | dev             | client2Dev     |
| client2       | staging         | client2Staging |
| client2       | production      | client2Production     |

代码目录为：

```
YourAndroidProject/
  app/
    src/
      main/
       <part of all builds>
      client1/
       <included in client1Dev/client1Staging/client1Production>
      client2/
       <included in client2Dev/client2Staging/client2Production>
      dev/
       <included in client1Dev/client2Dev>
      staging/
       <included in client1Staging/client2Staging>
      production/
       <included in client1Production/client2Production>
      client1Production/
       <included only in client1Production>
```

如果某几个`Flavor`或路径，包含同样的代码，而另外几个`Flavor`或路径又包含别的代码。除了可以新增一个`纬度(flavorDimensions)`，也可以通过配置，指定某几个`Flavor`的源代码目录为同一个，以此实现包含共同代码：

```
sourceSets {
    client1 {
        java.srcDirs = ['src/client/java']
    }

    client2 {
        java.srcDirs = ['src/client/java']
    }

    dev {
        java.srcDirs = ['src/dev/java']
    }
    staging {
        java.srcDirs = ['src/dev/java']
    }
    production {
        java.srcDirs = ['src/production/java']
    }
}
```

如上，表示`client1`和`client2`是同一块代码。`dev`和`staging`是同一块代码。只有`production`有自己的差异。

### 理解

`flavorDimensions N1, N2 ...`用来定义纬度:

`N1 N2 N3 ...`

`productFlavors A1 dimension N1`用来定义各纬度的值：

`N1: A1 A2 ...`
`N2: B1 B2 ...`
`N3: C1 C2 ...`
`Nn: DEBUG RELEASE`

加上默认的`DEBUG`和`RELEASE`产生的构建组合矩阵为：

`N1 * N2 * N3 * ...`

```
A1  B1  C1 ... DEBUG
A2  B2  C2 ... RELEASE
..  ..  .. ...
```

结果为：

`A1 B1 C1 ... DEBUG`
`A1 B1 C1 ... RELEASE`
`A2 B1 C1 ... DEBUG`
`A2 B1 C1 ... RELEASE`

`A1 B2 C1 ... DEBUG`
`A1 B2 C1 ... RELEASE`
`A2 B2 C1 ... DEBUG`
`A2 B2 C1 ... RELEASE`

... ... ，依次类推。

## 代码管理

组合已经理解了，怎么定义某个组合的代码？怎样实现差异？

由于上面例子纬度太多，所以以下面这个简单的例子为例：

```gradle
flavorDimensions "platform"
productFlavors {
    platform3399 {
        dimension "platform"
    }
    platform551 {
        dimension "platform"
    }
}
```

加上默认的`DEBUG/RELEASE`，其实这个例子有两个纬度，产生的构建为：

`platform3399Debug`
`platform3399Release`
`platform551Debug`
`platform551Release`

默认情况下，
`3399`对应的源代码目录为：`src/platform3399`
`551`对应的源代码目录为：`src/platform551`

`3399Debug`对应`src/platform3399/debug`
`3399Release`对应`src/platform3399/release`

## 参考

<https://proandroiddev.com/advanced-android-flavors-part-2-enter-flavor-dimensions-4ad7f486f6>
