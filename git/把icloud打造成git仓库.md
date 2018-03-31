# 把icloud打造成git仓库

icloud并不能直接作为远程git仓库使用，因为icloud上的文件并不可以直接访问，但可以作为本地git仓库使用。

大致思路是这样：
本地目录 《- git -》 本地git仓库（icloud目录） 《- icloud -》 icloud备份服务器

### Step1

```shell
$ cd ~
$ ln -s Library/Mobile\ Documents/com~apple~CloudDocs/ icloud
$ cd icloud
```

创建icloud目录本地的快捷访问路径（软链接）

### Step2

```shell
$ cd ~/icloud
$ mkdir repos && cd $_
$ git init --bare demo.git
```

在icloud中创建一个共享的git仓库（--bare相当于svn中的远程仓库概念）
以上，访问`~/icloud/repos/demo.git`就相当于访问了远程git仓库。

### Step3

```shell
$ cd ~
$ mkdir localDemo
$ cd localDemo
$ echo ".DS_Store" > .gitignore
$ echo "Demo" > README

$ git init
$ git add .
$ git commit -m "initial commit"
$ git remote add origin ~/icloud/repos/demo.git
$ git push -u origin master
```

在任意目录下，初始化本地的git项目，配置远程git仓库，然后把本地的所有分支push到远程仓库。

### Step4

```shell
$ cd ~
$ git clone ~/icloud/repos/demo.git
```

从远程仓库中拉取项目。

## 总结
类似本地svn仓库服务器一样，其实是搭建了本地的git仓库服务器。
由于是本地访问，所以通过`本地路径/project.git`来访问。
因为本地仓库是建立在icloud自动备份的目录下，所以icloud会自动备份这个git仓库服务器。

如果本地机器有对外的IP，那么这个过程相当于建立了远程的git服务器，并对服务器上的git仓库进行了icloud的备份。

## 关于git init --bare作用
个人理解：
--bare就是建立一个git仓库服务器。既然是git仓库服务器，就没必要存储实际的workspace，只需要存储能生成workspace的文件即可。而对于git而言，`.git`目录才是仓库的核心。所以，--bare相当于只存储`.git`目录。

查看服务器中的`XXXX.git`目录和本地的`.git`目录，就能发现两者结构几乎一致。（正规的说法并不是这样）

## 参考
<http://winterbe.com/posts/2014/11/27/setup-icloud-git-repository/>
