## Android OpenGrok搭建源码阅读环境

1. brew install tomcat

`/usr/local/Cellar/tomcat/`

2. brew install ctags

3. brew install wget

4. download opengrok：<https://github.com/oracle/opengrok/releases>

`wget -O - https://github.com/oracle/opengrok/releases/download/1.1-rc31/opengrok-1.1-rc31.tar.gz | tar xvz`

5. 配置一些必要的环境，可以配置在`～/.bash_profile`中

```shell
export PATH=$PATH:~/Applications/opengrok-1.1-rc31/bin/
export JAVA_HOME=/opt/java
export OPENGROK_TOMCAT_BASE=/usr/local/Cellar/tomcat/9.0.10/libexec
export OPENGROK_APP_SERVER=Tomcat // 这个可以不用
export OPENGROK_INSTANCE_BASE=~/Applications/opengrok-1.1-rc31
```

添加`PATH`是为了方便执行`OpenGrok`命令。
添加`OPENGROK_TOMCAT_BASE`是指定`Tomcat`的目录
添加`OPENGROK_INSTANCE_BASE`是指定`opengrok`的根目录

6. 在OpenGrok根目录下执行

```shell
mkdir data
mkdir etc
```

之所以要在`opengrok`目录下执行创建目录的操作，是因为上面的操作，指定了新的配置目录为`~/Applications/opengrok-1.1-rc31`。

默认情况下：
`data`目录为：`/private/var/opengrok/data`
`etc`目录为：`/private/var/opengrok/etc`

所以，如果看到目录输出为`/private/var`或`/var`就说明，没有重新配置`opengrok`目录，或者配置失败。如果使用默认目录，需要使用`sudo`权限才能成功执行下面的命令。

这两个目录先创建，下面会有讲解。

7. 在`opengrok/bin`目录下执行`./OpenGrok deploy`

命令执行后，就会在`OPENGROK_TOMCAT_BASE`环境变量路径下的`webapps`下（即`/usr/local/Cellar/tomcat/9.0.10/libexec/webapps`目录下），会产生一个`source.war`和一个`source`目录。

这两个文件就是`http://localhost:8080/source`站点的源文件和配置。

`./OpenGrok deploy`只是一个脚本，帮助生成上面那两个文件。也可以手动解压`opengrok/lib/source.war`（unzip命令)。

注：如果`source.war`和`source目录`已经存在，那么必须先删除再执行`Deploy`，否则不会覆盖更新！！或者手动更改下面配置里面的路径也可。

最后，看到`webapps/source/WEB-INF/web.xml`变成下面这样，包含环境变量`OPENGROK_INSTANCE_BASE`指定的路径时，就表示配置成功（在文件头部第10行左右）。

```xml
<context-param>
	<description>Full path to the configuration file where OpenGrok can read its configuration</description>
	<param-name>CONFIGURATION</param-name>
	<param-value>/Users/jokin/Applications/opengrok-1.1-rc31/etc/configuration.xml</param-value>
</context-param>
```

此时，查看`opengrok目录/etc`和`opengrok目录/data`，还是空的。

8. 建立源代码索引。

在需要建立索引的源码路径执行：

`~/Applications/opengrok-1.1-rc31/bin/OpenGrok index .` (.表示当前路径)

你可以使用`opengrok`目录做测试：

`cd ~/Applications/opengrok-1.1-rc31/; bin/OpenGrok index .`

生成的索引会存储在`opengrok目录/data`里面。（这时，你也知道了，上面创建data目录的作用了）

同时，`opengrok目录/etc`下，会产生一个`configuration.xml`文件。（这时，你也知道了，上面创建etc目录的作用了）

如果要重建，删除这两个目录，重新执行即可。

建立源代码索引，取决于源代码数量，数量越大等待越久。比如`AOSP`的`framework目录`，大概需要15分钟。

9. 启动/暂停 Tomcat

`catalina start/stop`

10. 完成！打开<http://localhost:8080/source>就可以看源码了！

**错误集锦：**

1. Unable to determine Deployment Directory for Tomcat

```java
Loading the default instance configuration ...

FATAL ERROR: Unable to determine Deployment Directory for  Tomcat - Aborting!
```

找不到`Tomcat`，按照上面环境变量配置，配置好后新建一个终端重新`Deploy`。正确的输出为：

```java
Loading the default instance configuration ...
Installing /Users/jokin/Downloads/opengrok-1.1-rc31/bin/../lib/source.war to /usr/local/Cellar/tomcat/9.0.10/libexec/webapps ...

Start your application server (Tomcat),  if it is not already
running, or wait until it loads the just installed web  application.

OpenGrok should be available on <HOST>:<PORT>/source
  where HOST and PORT are configured in Tomcat.
```

2. index命令，../lib/opengrok.jar error

```java
OpenGrok Error: Invalid or corrupt jarfile /Users ../lib/opengrok.jar error
```

不好意思，版本太新，出问题了。降级到`opengrok-1.1-rc31`就没问题。

3. index命令，mkdir: /var/opengrok/data: Permission denied

```java
Loading the default instance configuration ...
WARNING: OpenGrok generated data path /var/opengrok/data doesn't exist
  Attempting to create generated data directory ...
mkdir: /var/opengrok/data: Permission denied

FATAL ERROR: OpenGrok data path /var/opengrok/data doesn't exist - Aborting!
```

说明配置的路径没生效，定位到了默认的`/var/opengrok/data`，而由于没有使用`sudo`执行，所以执行失败。

4. Unable to determine source root path.Missing configuration?

```java
There was an error!
CONFIGURATION parameter has not been configured in web.xml! Please configure your webapp.

Unable to determine source root path. Missing configuration?
```

原因是，还没有建立源代码索引。

参考
<https://github.com/oracle/opengrok/wiki/How-to-setup-OpenGrok>
<https://github.com/oracle/opengrok/wiki/Webapp-configuration>
