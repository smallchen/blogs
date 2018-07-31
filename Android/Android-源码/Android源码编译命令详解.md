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
