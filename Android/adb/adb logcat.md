## adb logcat 输出

通过`logcat --help`查看：

```shell
Usage: logcat [options] [filterspecs]

其中options是：
options include:
  -s              Set default filter to silent.
                  Like specifying filterspec '*:S'
  -f <filename>   Log to file. Default is stdout

其中filterspecs是：
filterspecs are a series of
  <tag>[:priority]

<tag>是：log中的tag，*表示所有。
priority是：V for verbose D for debug I for info S for suppress（压制）黑名单.  

'*' by itself means '*:D' and <tag> by itself means <tag>:V.
If no '*' filterspec or -s on command line, all filter defaults to '*:V'.
eg: '*:S <tag>' prints only <tag>, '<tag>:S' suppresses all <tag> log messages.
```

根据手册可以得到：

1. 输出`Debug`,`Error`等级。

`logcat *:D` 仅输出所有Debug信息
`logcat *:E` 仅输出所有Error信息

2. 输出某个TAG的日志。

`logcat jokin:D` 输出TAG为jokin的Debug等级日志
`logcat jokin:E` 输出TAG为jokin的Error等级日志
`logcat jokin1:E jokin2:D jokin3:E jokin4:E` 输出或关系的TAG日志。

3. 黑名单S

`logcat blackTag:S` 不输出任何blackTag的日志。
`logcat blackTag1:S blackTag2:S blackTag3:S` 或关系的黑名单。

4. 白名单（正常等级的输出即为白名单）

`logcat whiteTag1:* whiteTag2:* whiteTag3:*` 或关系的白名单。

5. 输出到文件

`logcat -f ./error.log  *:E *:D *:I`

logcat的白名单+黑名单+输出到文件已经可以足够使用了。

#### 借助grep过滤。

adb logcat | grep -vE "^..MyApp|^..MyActivity"  #使用 egrep 无须转义符

显示某个package apk的日志输出：

```
#!/bin/bash
packageName=$1
pid=`adb shell ps | grep $packageName | awk '{print $2}'`
adb logcat | grep --color=auto $pid
```

#### sdcard里执行shell

在sdcard下执行shell，会提示权限不足。即使是root也不可以执行。这个时候，将shell脚本放到`/data/local/tmp`里面，就可以执行了。
