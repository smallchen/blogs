# Git流图

## 基本Commit
改善[improve]
修改 [modify]
新特性 [feature]
Bug修复 [bugfix]

## 问题

1. git pull 或 git push 提示没有分支
原因：当前分支没有关联远程分支，这个是需要设置的，使用了`git checkout origin/develop -b develop`是自动关联。但`git checkout develop -b new_develop`中，new_develop就不会自动关联。

`git config -e` 查看

```
[branch "develop"]
        remote = origin
        merge = refs/heads/develop
[branch "master"]
        remote = origin
        merge = refs/heads/master
```
自己添加即可。


## 基本命令
0. 帮助
git xxx --help

1. 更新
git fetch origin 更新远程所有分支
git pull origin

2. 合并分支
git rebase develop  // 合并，并重整当前分支
git merge develop
git merge develop --no-ff  // 合并但不会将合并作为提交。
git merge develop --no-submit // 合并，然后产生cache，不会直接提交，需要用户自己提交
git merget develop --squash  // 合并，将develop的多个差异作为一个提交。之前develop的提交信息会丢失。


3. 仅合并某个提交
git cherry-pick -x 6b8108
git cherry-pick <start-commit-id>..<end-commit-id>  合并区间：(start end]
git cherry-pick <start-commit-id>^..<end-commit-id> 合并区间：[start end]

3. 恢复
git reset 6b8108/FETCH_HEAD  远程与本地的差异会进入cache
git reset --hard FETCH_HEAD  直接舍弃本地，找不回来
git reset --hard origin/HEAD  同上

4. 暂存和提取
git stash            // 暂存
git stash pop        // 提取并出栈
git stash save XXX   // 暂存
git stash apply XXX  // 提取不出栈
git stash list -v    // 列表

6. 提交
git commit --amend  // 修改上一个的commit的消息内容（如果觉得上一个提交信息不完善）
git push origin master                   // 等同于 master:master
git push origin master:master_1    // 提交本地master到远程master_1分支

7. 添加远程仓库
git remote add origin https://xxx.xxx.xxx
git remote add origin_1  https://xxx.xxx.xxx   // 添加
git remote set-url origin https://xxx.xxx.xxx  // 修改

8. 分支
git branch      //  查看本地分支
git branch -a   // 查看所有分支
git remote prune origin  //  删除本地中远程仓库不复存在的分支（刷新本地分支）
git branch -D xxx. // 删除分支

9. 显示
git rev-list --max-count=1 --pretty HEAD  // 显示最新的提交
git log --author=xxx // 显示xxx的提交

10. 高级格式化显示（可用于gradle等脚本）
git rev-list --max-count=10 --format=%an_%ar_%ai_%n%B%n  HEAD
输出：
commit 40e2b400726c1d2b24ec6d4f3e566fb32b8f0ac6
yangmeng_24 hours ago_2018-01-04 15:04:22 +0800_
[bugfix] "converity bugfix"
参考：git log --help
短hash：--abbrev-commit
部分格式：
```shell
           o   %ad: author date (format respects --date= option)
           o   %aD: author date, RFC2822 style
           o   %ar: author date, relative
           o   %at: author date, UNIX timestamp
           o   %ai: author date, ISO 8601-like format
           o   %aI: author date, strict ISO 8601 format
           o   %D: ref names without the " (", ")" wrapping.
           o   %e: encoding
           o   %s: subject
           o   %f: sanitized subject line, suitable for a filename
           o   %b: body
           o   %B: raw body (unwrapped subject and body)
           o   %N: commit notes
           o   %GG: raw verification message from GPG for a signed commit
           o   %n: newline
```

## Graph
gitlab和github的graph

1. 简单的图
![QQ20171205-101232@2x.png](http://upload-images.jianshu.io/upload_images/2166887-b0bce05ab347948b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图。
A是初始化的点。后来在A点上`checkout`了一个分支，修复了一个Bug，然后`merge`回去成为了B点。
B点后在原来分支进行了一个提交。
然后再`checkout`一个分支修复了焦点的Bug，再`merge`回去成为C点。
C点再`checkout`一个分支，进行了一系列提交。原来分支上也进行了一系列提交。**当前差异性代码在C点后面**

2. 复杂的图
![QQ20171205-102132@2x.png](http://upload-images.jianshu.io/upload_images/2166887-bc79845ced09eb07.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图。复杂在于分支多，且可以看出**合并后保留了原来的分支**（灰色标签是分支名），个人习惯保留分支名，可以直观看到那个分支干了什么事。

A点。A点合并到B点，意味着，B点包含了A点前所有内容。反过来则不然，A点并不包含B点前的内容，因为B点没有`merge`到A点。
C点。C点包含了B点，同时也包含了A点。
D点。D点包含了A点。虽然B和D都是从A点延伸，但B和D点的差异并不仅仅是从A点开始，**B除了A点外，还有自己的提交，这部分A是不具备的，也是D不具备的**。
F点。F点是合并了ABCD所有点的提交，所以F点是最终合并的内容。

**Graph非常重要**，可以看到两个分支的起源和差异，并且看到各自的commit。大部分没有提供graph的工具，在合并代码时会异常困难。

> 通过Graph可以看出哪里可以安全使用git rebase。如图，在一条线上的点，都可以安全使用rebase。例如。D点3399_develop分支可以使用rebase同步到F点而不需要做任何修复。B点可以rebase到C点。dev_new_mark可以rebase到merge_develop。merge_develop可以rebase到master。等等。

3. 合理的图
一般情况下，一个项目只需一个develop和release分支。总体图看起来应该像下面这样。
![QQ20171205-111255@2x.png](http://upload-images.jianshu.io/upload_images/2166887-066e3c28863cfac2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图。
A点项目初始。当一定量的提交完成后，标志着第一个版本可以进行测试。此时从develop分支中A点`merge`到release分支，生为B点，在B点构建一个测试版release1.0.0_snapshot。
测试过程发生的Bug，则从B点`checkout`一个bugfix分支，修复后要同步合并到develop和release，也即是E点和C点（在release上进行修复的分支，可以统一命名为hotfix，意为需要合并到所有分支）。
C点同样打包一个测试版release1.0.1_snapshot，测试通过，在C点打个Tag，标示着release1.0正式版发布。

当从A点`merge`到release，意味着develop已经解锁，可以继续并行开发。比如feature分支。
从F点`merge`到release生成G点，构建2.0版本的测试版。同理发现Bug则`checkout`一个bugfix分支进行修复，然后合并回develop和release。

总体的约定：develop和release只能进行merge，而不能直接提交。
分支的命名：develop上切出来的，可以为bugfix/xxx，feature/xxx，也可简写为b/xxx与f/xxx，release上切出来的，可以为hotfix/xxx，分支加`/`是因为有的git工具可以自动生成目录结构。

看回图。release分支上，2.0版本的H点，包含着之前的所有测试版，以及1.0正式版。develop分支一直走在前面，包含着release的所有点。
![QQ20180111-151007@2x.png](http://upload-images.jianshu.io/upload_images/2166887-b19db186aecdfbe7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## Revert
单独把revert抽出来说，是因为revert操作对内容的影响是直接的。**一个没有进行revert的分支，和一个进行了revert的分支合并，revert了的那部分提交是存在还是消失？？**

首先，revert某个提交，是把提交中增加的内容删除，删除了的内容添加回来。然后作为新的提交commit。
反过来，把revert的那个提交进行一次revert，就是把那个提交又恢复过来。

明白这一点，虽然不知道git如何处理上面的问题，但**只要在合并前或合并后，对revert的那个提交再revert一次，就可以将内容恢复过来**。这操作和cherry-pick类似，是对某个提交的变更进行操作。
> 曾经进行过上面的合并，revert掉的那段内容有部分消失，部分留了下来！！这诡异的行为主要因为是git的自动合并，如果手动合并则可以全部保留。但相对于手动合并，更建议使用revert恢复。

## 单项目多仓库
一个项目有多个远程仓库，仓库的项目是大致相同的，比如是不同的厂商之类的。当前有一个功能，需要同步到两个仓库。
比较省事的办法是：仓库A中修改完毕，cherry-pick到仓库B。

1. 把仓库B拉取到本地。git clone B
2. 把仓库A也绑定到仓库B。git remote add A xxx
3. 拉取仓库A的最新代码。git fetch A
4. 切换到要合并的分支。git checkout b-master B/master
5. 将代码同步到仓库B。git cherry-pick -x <commit-id>
