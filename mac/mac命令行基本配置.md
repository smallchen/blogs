## Mac命令行基本配置

#### 终端的配置
1. 安装homebrew

`/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`

1. git命令自动补全，这个也真麻烦。

```shell
1. brew install bash-completion
2. 下载https://raw.github.com/git/git/master/contrib/completion/git-completion.bash 因为默认没有git自动补全
3. 复制bash文件到 usr/local/etc/bash_completion.d 目录下
4. 修改~/.bash_profile，添加内容
   [ -f /usr/local/etc/bash_completion ] && . /usr/local/etc/bash_completion
5. 执行 brew unlink bash-completion && brew link bash-completion 更新一下
6. 重启终端
```

2. shell路径显示文件路径、git分支

```shell
1. 下载git-prompt.sh
2. 参考下面的完整例子
```

最后，完整的~/.bash_profile

```
source ~/demoInitShell/git-prompt.sh
[ -f /usr/local/etc/bash_completion ] && . /usr/local/etc/bash_completion
export PATH=$PATH:~/demoInitShell/:/Users/jokinkuang/Library/Android/sdk/platform-tools/
export PS1='\h:\u:\w$(__git_ps1 "(%s)")\$ '
```

参考文档
<http://wppurking.github.io/2013/03/03/wei-mac-os-tian-jia-bash-completion.html>
<http://www.cnblogs.com/redcreen/archive/2011/05/04/2037057.html>
<http://www.cnblogs.com/redcreen/archive/2011/05/05/2038331.html>


#### AndroidStudio设置
1. 查看分配的内存。
AndroidStudio - Preferences - Appearance - ShowMemoryIndicator

2. 更改虚拟机设置，否则很卡！
Help - EditCustomVMOptions

```
# custom Android Studio VM options, see http://tools.android.com/tech-docs/configuration
-Xms2048m
-Xmx409m            # 最大虚拟内存
-XX:PermSize=256m
-XX:MaxPermSize=512m # 永久代空间
```
