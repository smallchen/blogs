## MacOS High Sierra 10.13.5 编译 android-8.1.0_r20 源码

环境：

- macOS High Sierra 10.13.5（中文版）
- Xcode 9.4.1
- MacPorts 2.5.2
- Oracle JDK1.8

#### 环境配置

<https://source.android.com/setup/build/initializing>

1、创建区分大小写的磁盘映像

磁盘工具 - 容器`disk1` - 右键添加APFS宗卷 - 格式选择APFS(区分大小写) - 添加

不设置大小直接创建，会创建一个可以自动扩容的映像分区，不必担心容量过剩或不足。

当初选择了`大小选项`，分配了100G，结果限定了映像空间只有100G，无法扩容。而Android源码下载完就70G左右，官方说编译至少25G，所以100G是不够的。实际完成`android-8.1.0`的编译，占用空间125G。

> 注：Mac下编译Android源码，必须提供支持大小写的磁盘，编译脚本会对磁盘类型进行判断，不合法根部无法编译。

2、安装JDK

JDK必须是`Oracle`的，`OpenJDK`是不行的。会有编译提示，无需担心。

<http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html>

下载安装，然后设置环境变量到`~/.bash_profile`:

```java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/
export PATH=$PATH:/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/bin/
export CLASSPATH=.:/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/jre/jdk/Contents/Home/lib/
export ANDROID_JAVA_HOME=$JAVA_HOME
```

最重要的是`ANDROID_JAVA_HOME`，不设置编译会有错误，因为Android的编译是靠这个环境变量来指定`JDK`的，不设置会导致编译时使用了系统中旧的JDK，提示`could not find jdk tools.jar`。

3、安装Xcode

官方教程里没有说一定要安装Xcode，下载源码的时候，顺便安装了。直接在`app store`安装的Xcode。

无论Xcode是否安装，都需要执行：`xcode-select --install`。

4、安装MacPorts

MacPorts其实是Mac下的软件管理应用，和Homebrew类似，和Linux下的apt-get类似。官方教程应该简单说明一下，一开始是懵逼的。

提供了两种方式安装：

* mac下安装包`pkg`
* 源码安装

虽然有提供`pkg`的傻瓜式，但安装失败了，只好使用源码安装。

```java
下载源码 <https://distfiles.macports.org/MacPorts/MacPorts-2.5.2.tar.bz2>
tar xjvf MacPorts-2.5.2.tar.bz2
cd MacPorts-2.5.2
./configure && make && sudo make install
```

默认安装在`/opt/local`目录下。如果路径不在终端环境，自行添加`export PATH=/opt/local/bin:$PATH`到`~/.bash_profile`.

安装完后，第一次通常都要执行：`sudo port -v selfupdate`，算是初始化吧。

5、通过MacPorts安装编译环境

上面已经说了，MacPorts只是下载工具，所以需要继续安装其它工具。

通过MacPorts 获取 Make、Git 和 GPG 软件包:

`POSIXLY_CORRECT=1 sudo port install gmake libsdl git gnupg`

由于是新版本，所以`bison`，降级`make 3.82`也不需要了。

6、修改Mac下默认的文件描述符数量(默认的太少)

```java
# set the number of open files to be 1024
ulimit -S -n 1024
```

7、完成。

#### 源码下载

由于被墙或要翻墙，速度比较低，所以还是推荐`清华源`。

<https://mirrors.tuna.tsinghua.edu.cn/help/AOSP/>

1、下载repo

```java
curl https://mirrors.tuna.tsinghua.edu.cn/git/git-repo > ~/.repo
chmod a+x ~/.repo
```

下载到`~`目录，方便使用。将路径添加到`~/.bash_profile`。`export PATH=$PATH:~/`

2、配置git账号

```java
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

3、创建一个目录，比如`android-8.1.0_r20`

4、初始化

```java
repo init -u https://aosp.tuna.tsinghua.edu.cn/platform/manifest
repo init -u https://aosp.tuna.tsinghua.edu.cn/platform/manifest -b android-8.1.0_r20
repo sync
```
> android-8.1.0_r20 分支在

大概2小时。

#### 编译

##### 编译前准备

由于编译过程会有各种各样的问题，为了不遇到问题再解决，可以先解决。

1、准备工具环境

```java
source ./build/envsetup.sh
```

执行了这个命令，才有`lunch`等命令。

2、解决`macOS.sdk`不支持`10.13`问题。

<https://stackoverflow.com/questions/39960751/build-aosp-on-mac-10-12>

修改`build/soong/cc/config/x86_darwin_host.go`为：

```
darwinSupportedSdkVersions = []string{
    "10.8",
    "10.9",
    "10.10",
    "10.11",
    "10.12",
    "10.13", # 在最后面添加`10.13`
}
```

3、解决虚拟机`Out Of Memory`的问题。

添加`export JACK_SERVER_VM_ARGUMENTS="-Dfile.encoding=UTF-8 -XX:+TieredCompilation -Xmx4096m"`到`~/.bash_profile`中。

然后主动重启`jack-server`，jack-server是一个后台服务器，启动了编译时就不会启动:

```java
prebuilts/sdk/tools/jack-admin kill-server
prebuilts/sdk/tools/jack-admin start-server
```

像网上说的那样，把`-Xmx`在`prebuilts/sdk/tools/jack-admin`中直接修改参数，会导致`jack-server`无法启动（一直卡着），所以放弃了直接修改。

**如果不行**，将`prebuilts/sdk/tools/jack-admin`中，`start-server)`里面的`JACK_SERVER_COMMAND`改为：

`JACK_SERVER_COMMAND="java -Xmx:4096m -XX:MaxJavaStackTraceDepth=-1 -Djava.io.tmpdir=$TMPDIR $JACK_SERVER_VM_ARGUMENTS -cp $LAUNCHER_JAR $LAUNCHER_NAME"`

在`java命令`后面添加`-Xmx:4096m`。试过像网上那样，添加在`-cp`前后，或修改`$JACK_SERVER_VM_ARGUMENTS`都会导致`jack-server`启动不了。

> vim下，可以通过按键/xx来搜索xx

4、解决`bison`工具错误

<https://blog.csdn.net/h649305597/article/details/80322488>

4.1、定位到源码中`external/bison`文件夹。`cd external/bison`

4.2、在文件夹`创建`文本文件`patch-high-sierra.patch`并把下面代码复制进去。

```
With format string strictness, High Sierra also enforces that %n isn't used
in dynamic format strings, but we should just disable its use on darwin in
general.

--- lib/vasnprintf.c.orig   2017-06-22 15:19:15.000000000 -0700
+++ lib/vasnprintf.c    2017-06-22 15:20:20.000000000 -0700
@@ -4869,7 +4869,7 @@ VASNPRINTF (DCHAR_T *resultbuf, size_t *
 #endif
                   *fbp = dp->conversion;
 #if USE_SNPRINTF
-# if !(((__GLIBC__ > 2 || (__GLIBC__ == 2 && __GLIBC_MINOR__ >= 3)) && !defined __UCLIBC__) || ((defined _WIN32 || defined __WIN32__) && ! defined __CYGWIN__))
+# if !defined(__APPLE__) && !(((__GLIBC__ > 2 || (__GLIBC__ == 2 && __GLIBC_MINOR__ >= 3)) && !defined __UCLIBC__) || ((defined _WIN32 || defined __WIN32__) && ! defined __CYGWIN__))
                 fbp[1] = '%';
                 fbp[2] = 'n';
                 fbp[3] = '\0';
```

4.3、在控制台（保证当前目录是`external/bison`）执行

`patch -p0 < patch-high-sierra.patch  `

4.4、返回根目录 `cd ../..`  

4.5、编译bison `make bison`

4.6、复制bison到AOSP编译时寻找bison的位置

`cp ./out/host/darwin-x86/obj/EXECUTABLES/bison_intermediates/bison ./prebuilts/misc/darwin-x86/bison/bison`

经过他的验证在7.1以后的版本都需要使用这个来解决bison失效的问题，至少现在还是这样的。报的类似错误都可以用这个方式试试

5、解决编译到最后的`syntax error`

修改文件：`build/core/combo/HOST_darwin-x86.mk`

Mac需要把最后替换为：

```shell
# $(1): The file to check
define get-file-size
GSTAT=$(which gstat) ; \
if [ ! -z "$GSTAT" ]; then \
gstat -c "%s" $(1) ; \
else \
stat -f "%z" $(1) ; \
fi
endef
```

##### 开始编译

上面的操作，可以保证编译顺利进行。

```shell
source ./build/envsetup.sh
lunch
(选择1)
make -j8
```

输出的列表，选择1。`eng`表示是运行在模拟器中的版本。`user`表示发行版。`userdebug`表示内测版。
| 构建类型 | 用途     |
| :------------- | :------------- |
| user       | 有限的访问权限，主要用于发布正式产品，没有 root 跟调试权限       |
| userdebug       | 跟 user 类型差不多，但是多了 root 跟 debug 调试权限       |
| eng       | 拥有各种调试工具的开发版设置，拥有 root 跟 debug 权限       |


`make -j8`表示8个构建线程并发（2倍CPU核心）

漫长等待，睡觉了，所以不知道时间，算上解决错误的时间，大概5小时。

中途试过卡死几次，然后重启`jack-server`重试。来来回回5、6次，最后总算编译通过。

#### 运行

```java
source build/envsetup.sh
lunch (选择已经构建了的目标版本，比如之前选择的是1，则这里选择1)
emulator
```

或 `emulator -partition-size 4096 -memory 2048`

模拟器很慢。

<https://blog.csdn.net/dl6655/article/details/78869501/>
