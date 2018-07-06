## Mac配置Subline查看源码

1. 安装sublime
2. 打开sublime的终端Console，到https://packagecontrol.io/installation选择对应的版本粘贴命令到终端下载。
3. 安装完毕，在Preferences - PackageControl - 下载ctags
4. Open-选择源码根目录
5. sublime中，右键源码根目录，Ctags：Rebuild Tags，重新生成索引

### 修复错误

**/Library/Developer/CommandLineTools/usr/bin/ctags: illegal option -- R
usage: ctags** [**-BFadtuwvx**]**** [**-f tagsfile**] **file ...**

原因：使用的是系统默认的ctags，没有-R选项。

修复：使用homebrew安装ctags。
1. brew install ctags
2. which ctags 查看ctags命令是否指向新的ctags
3. 将上面路径的ctags替换成新的ctags

```shell
cd /Library/Developer/CommandLineTools/usr/bin/
sudo mv ctags ctags-old
ln -s /usr/local/bin/ctags ./ctags
```

### 按键冲突
Preferences - PackageSettings - CTags
