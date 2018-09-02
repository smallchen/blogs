
<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android源码编译命令详解](#android源码编译命令详解)
    - [m、mm、mmm](#mmmmmm)
        - [m例子](#m例子)
        - [mm例子](#mm例子)
        - [mmm例子](#mmm例子)
    - [mma、mmma](#mmammma)
    - [条件](#条件)
    - [make -j4](#make-j4)
    - [编译SDK（修改后的SDK）](#编译sdk修改后的sdk)
    - [make update-api](#make-update-api)

<!-- /TOC -->
## Android源码编译命令详解

### m、mm、mmm

- m:       Makes from the top of the tree.
- mm:      Builds all of the modules in the current directory.
- mmm:     Builds all of the modules in the supplied directories.

* m：编译所有的模块
* mm：编译当前目录下的模块，当前目录下要有Android.mk文件
* mmm：编译指定路径下的模块，指定路径下要有Android.mk文件

参数可以为`-B -j2`：

* -B 表示编译所有文件
* -j2 表示2个线程执行

#### m例子

```shell
source build/envsetup.sh
m
```

通常，编译过一次系统后，不需要编译所有模块。单独编译某个模块即可。

#### mm例子

```shell
source build/envsetup.sh
cd development/tools/idegen/
mm
```
表示编译`development/tools/idegen`模块。

其中：必须在模块所在目录。

#### mmm例子

```shell
source build/envsetup.sh
mmm development/tools/idegen/
```

同上，也表示编译`development/tools/idegen`模块。

其中：必须为源码根目录。

### mma、mmma

和上面的差不多，只是强调，指定目录下新增或删除了文件，而进行的重新编译。


### 条件

```java
No rule to make target `out/target/product/generic_arm64/obj/SHARED_LIBRARIES/libbinder_intermediates/export_includes', needed by `out/target/product/generic_arm64/obj/SHARED_LIBRARIES/libinputflinger_intermediates/import_includes'
```

如上错误。要构建的模块，所依赖的模块不存在。

所以通常，在对模块进行构建前，需要对系统进行一次全面的编译。主要是因为模块之间会有依赖问题。如果不进行构建，很容易出现引入的so或者lib不存在而导致模块编译失败。

当然，有一小部分是例外的。比如构建AndroidStudio的项目模块。

### make -j4

编译整个Android系统源码。-j4表示启用4个线程编译（核心数*2）。

### 编译SDK（修改后的SDK）

```shell
source ./build/envsetup.sh
lunch sdk-eng
make sdk
```

### make update-api

添加系统API或者修改`@hide`的API后，需要执行`make update-api`，然后再`make`。

修改公共api后，需要`make update-api`

比如，修改了`Intent.java`、`KeyEvent.java`等等，编译源码时会提示：

```java
see build/core/apicheck_msg_current.txt
******************************
You have tried to change the API from what has been previously approved.

To make these errors go away, you have two choices:
   1) You can add "@hide" javadoc comments to the methods, etc. listed in the
      errors above.

   2) You can update current.txt by executing the following command:
         make update-api

      To submit the revised current.txt to the main Android repository,
      you will need approval.
******************************
```

谷歌对于所有的类和API，分为`开放`和`非开放`两种，而开放的类和API，可以通过`Javadoc标签`与源码同步生成`程序的开发文档`；当我们修改或者添加一个新的API时，我们有两种方案可以避免出现上述错误.

* 一是将该接口加上 非公开的标签：`/*{@hide}/`；
* 二是可以在修改后执行：`make update-api(公开)`，将修改内容与API的doc文件更新到一致。

修改相应API文件后，`make update-api`后，在`base`库下面会产生`.current.txt`文件的差异，提交时将该差异一并提交审核即可(frameworks/base/api/.current.txt)。
