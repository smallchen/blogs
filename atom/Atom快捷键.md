## Atom快捷键



<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

    - [Atom快捷键](#atom快捷键)
    - [Atom 插件](#atom-插件)

<!-- /TOC -->

## Atom快捷键



Command Shift P ，非常有用，弹出Command Palette（命令面板），不需要记各种快捷键！！比如，增删TOC。

Control Shift M ，markdown预览

需要了解atom，搜索可能效率低，因为你可能不知道搜索的东西具体叫什么，比如上面的命令面板。通过直接查看官网的文档，稍微看一下，就可以了解atom的一些便捷的使用。
<https://flight-manual.atom.io/getting-started/sections/atom-basics/>


## Atom 插件

sync-settings
atom-terminal

markdown-preview
markdown-toc

## Atom 编辑器设置、快捷键与必备插件
 2016-03-15  0 条评论  8602 次阅读  被赞了 7 次
目录
[隐藏]
Atom 设置
Atom 常用快捷键概览
文件切换
导航
目录树操作
书签
选取
编辑和删除文本
基本操作
删除和剪切
多光标和多处选取
括号跳转
编码方式
查找和替换
代码片段
自动补全
折叠
文件语法高亮
使用Atom进行写作
git操作
推荐一些好用的插件
Atom 编辑器界面比 sublime text 更好看，随着不断的升级优化，卡顿情况也大有改善，喜欢折腾的你快来试一试吧！

打造好用的 Atom 编辑器，让你的 Atom 更好用，一些必要的设置、好用的插件、以及记住常用快捷键是必须的。

Atom 设置
推荐几个设置选项：

显示空格、换行等符号： 选中 Show Invisibles
鼠标滚轮滚屏太小：设置 Scroll Sensitivity 参数，如 100（默认为 40）
允许到尾部还可以向上滚屏： 选中 Scroll Past End
Atom 常用快捷键概览
英文    中文    快捷键    功能
New Window    新建界面窗口    Ctrl + Shift + N    如中文意思
New File    新建文件    Ctrl + N    如中文意思
Open File    打开文件    Ctrl + O    如中文意思
Open Folder    打开文件夹    Ctrl + Shift + O    如中文意思
Add Project Folder    加载项目目录    Ctrl + Alt + O    如中文意思
Reopen Last Item    重新加载上次项目    Ctrl + Shift + T    如中文意思
Save    保存文件    Ctrl + S    如中文意思
Save As    另存为    Ctrl + Shift +S    如中文意思
Close Tab    关闭当前编辑文档    Ctrl + W    如中文意思
Close Window    关闭编辑器    Ctrl + Shift + W    如中文意思
Undo    撤销    Ctrl + Z    如中文意思
Redo    重做    Ctrl + Y    如中文意思
Cut    剪切    Shift + Delete    如中文意思
Copy    复制    Ctrl + Insert    如中文意思
Copy Path    复制文档路径    Ctrl + Shift + C    如中文意思
Paste    粘贴    Shift + Insert    如中文意思
Select All    全选    Ctrl + A    如中文意思
Select Encoding    选择编码    Ctrl + Shift +U    就是设置文件的编码
Go to Line    跳转到某行    Ctrl + G    支持行列搜索,Row:Column
Slect Grammar    语法选择    Ctrl + Shift + L    和Sublime的Syntax设置功能一样
Reload    重载    Ctrl+ Alt +R    重新载入当前编辑的文档
Toggle Full Screen    F11    全屏    如中文意思
Increase Font Size    增大字体    Ctrl + Shift + “+”    Sublime的Ctrl + 也能生效
Decrease Font Size    减小字体    Ctrl + Shift + “-“    Sublime的Ctrl – 也能生效
Toggle Tree View    展示隐藏目录树    Ctrl + |Sublime的Ctrl+K,+B这里也可以生效
Toggle Commadn palette    全局搜索面板    Ctrl + Shift + P    和Sublime的大同小异
Select Line    选定一行    Ctrl + L    如中文意思
Select First Character of Line    选定光标至行首    Shift + Home    如中文意思
Slect End of Line    选定光标至行尾    Shift + End    如中文意思
Select to Top    选定光标处至文档首行    Ctrl + Shift + Home    就是光标处作为分割线,取文档上部分
Select to Bottom    选定光标处至文档尾行    Ctrl + Shfit + End    就是光标处作为分割线,取文档下部分
Find in Buffer    从缓存器搜索    Ctrl + F    与Sublime一致
Replace in Buffer    高级替换    Ctrl + Shift + F    与Sublime一致
Select Next    匹配选定下一个    Ctrl + D    和Sublime一模一样有木有
Select All    匹配选定所有    Alt + F3    和Sublime一模一样有木有
Find File    查询文件,选定打开    Ctrl + P    与Sublime不一样
Delte End of Word    删除光标处至词尾    Ctrl + Del    如中文意思
Duplicate Line    Ctrl + Shift + D    复制当前行追加到后面    如中文意思
Delete Line    删除一行    Ctrl + Shift + K    如中文意思
Toggle Comment    启用注释    Ctrl + /    与Sublime一致
Toggle developer tools    打开Chrome调试器    Ctrl + Alt + I    神奇啊
Indent    增加缩进    Ctrl + [    向右缩进
Outdent    减少缩进    Ctrl + ]    向左缩进
Move Line Up    行向上移动    Ctrl + up    如字面意思
Move Line Down    行向下移动    Ctrl + Down    如字面意思
Join Lines    行链接    Ctrl + J    追加
newline-below    光标之下增加一行    Ctrl + Enter    与sublime 一致
editor:newline-above    光标之上增加一行    Ctrl + Shift + Enter    与sublime 一致
pane:show-next-item    切换编辑的标签页    Ctrl + Tab    如中文意思
Fuzzy Finder    文件跳转面板    Ctrl + T    如字面意思
Select Line Move above    选中行上移    Ctrl + up    如中文意思
Select Line Move below    选中行下移    Ctrl + down    如中文意思
Symbol-view    进入变量、函数跳转面板。    Ctrl + R    如中文意思
文件切换
ctrl-shift-s 保存所有打开的文件
cmd-shift-o 打开目录
cmd-\ 显示或隐藏目录树
ctrl-0 焦点移到目录树
目录树下，使用a，m，delete来增加，修改和删除
cmd-t或cmd-p 查找文件
cmd-b 在打开的文件之间切换
cmd-shift-b 只搜索从上次git commit后修改或者新增的文件

导航
（等价于上下左右）
ctrl-p 前一行
ctrl-n 后一行
ctrl-f 前一个字符
ctrl-b 后一个字符

alt-B, alt-left 移动到单词开始
alt-F, alt-right 移动到单词末尾

cmd-right, ctrl-E 移动到一行结束
cmd-left, ctrl-A 移动到一行开始

cmd-up 移动到文件开始
cmd-down 移动到文件结束

ctrl-g 移动到指定行 row:column 处

cmd-r 在方法之间跳转

目录树操作
cmd-\ 或者 cmd-k cmd-b 显示(隐藏)目录树
ctrl-0 焦点切换到目录树(再按一次或者Esc退出目录树)
a 添加文件
d 将当前文件另存为(duplicate)
i 显示(隐藏)版本控制忽略的文件
alt-right 和 alt-left 展开(隐藏)所有目录
ctrl-al-] 和 ctrl-al-[ 同上
ctrl-[ 和 ctrl-] 展开(隐藏)当前目录
ctrl-f 和 ctrl-b 同上
cmd-k h 或者 cmd-k left 在左半视图中打开文件
cmd-k j 或者 cmd-k down 在下半视图中打开文件
cmd-k k 或者 cmd-k up 在上半视图中打开文件
cmd-k l 或者 cmd-k right 在右半视图中打开文件
ctrl-shift-C 复制当前文件绝对路径

书签
cmd-F2 在本行增加书签
F2 跳到当前文件的下一条书签
shift-F2 跳到当前文件的上一条书签
ctrl-F2 列出当前工程所有书签

选取
大部分和导航一致，只不过加上shift

ctrl-shift-P 选取至上一行
ctrl-shift-N 选取至下一样
ctrl-shift-B 选取至前一个字符
ctrl-shift-F 选取至后一个字符
alt-shift-B, alt-shift-left 选取至字符开始
alt-shift-F, alt-shift-right 选取至字符结束
ctrl-shift-E, cmd-shift-right 选取至本行结束
ctrl-shift-A, cmd-shift-left 选取至本行开始
cmd-shift-up 选取至文件开始
cmd-shift-down 选取至文件结尾
cmd-A 全选
cmd-L 选取一行，继续按回选取下一行
ctrl-shift-W 选取当前单词

编辑和删除文本
基本操作
ctrl-T 使光标前后字符交换
cmd-J 将下一行与当前行合并
ctrl-cmd-up, ctrl-cmd-down 使当前行向上或者向下移动
cmd-shift-D 复制当前行到下一行
cmd-K, cmd-U 使当前字符大写
cmd-K, cmd-L 使当前字符小写

删除和剪切
ctrl-shift-K 删除当前行
cmd-backspace 删除到当前行开始
cmd-fn-backspace 删除到当前行结束
ctrl-K 剪切到当前行结束
alt-backspace 或 alt-H 删除到当前单词开始
alt-delete 或 alt-D 删除到当前单词结束

多光标和多处选取
cmd-click 增加新光标
cmd-shift-L 将多行选取改为多行光标
ctrl-shift-up, ctrl-shift-down 增加上（下）一行光标
cmd-D 选取文档中和当前单词相同的下一处
ctrl-cmd-G 选取文档中所有和当前光标单词相同的位置

括号跳转
ctrl-m 相应括号之间，html tag之间等跳转
ctrl-cmd-m 括号(tag)之间文本选取
alt-cmd-. 关闭当前XML/HTML tag

编码方式
ctrl-shift-U 调出切换编码选项

查找和替换
cmd-F 在buffer中查找
cmd-shift-f 在整个工程中查找

代码片段
alt-shift-S 查看当前可用代码片段

在~/.atom目录下snippets.cson文件中存放了你定制的snippets

定制说明

自动补全
ctrl-space 提示补全信息

折叠
alt-cmd-[ 折叠
alt-cmd-] 展开
alt-cmd-shift-{ 折叠全部
alt-cmd-shift-} 展开全部
cmd-k cmd-N 指定折叠层级 N为层级数

文件语法高亮
ctrl-shift-L 选择文本类型

使用Atom进行写作
ctrl-shift-M Markdown预览
可用代码片段

b, legal, img, l, i, code, t, table

git操作
cmd-alt-Z checkout HEAD 版本
cmd-shift-B 弹出untracked 和 modified文件列表
alt-g down alt-g up 在修改处跳转
alt-G D 弹出diff列表
alt-G O 在github上打开文件
alt-G G 在github上打开项目地址
alt-G B 在github上打开文件blame
alt-G H 在github上打开文件history
alt-G I 在github上打开issues
alt-G R 在github打开分支比较
alt-G C 拷贝当前文件在gihub上的网址

推荐一些好用的插件
主题
atom-material-ui 好看到爆
atom-material-syntax
美化
atom-beautify 一键代码美化
file-icons 给文件加上好看的图标
atom-minimap 方便美观的缩略滚动图
git
atomatigit 可视化git操作
代码提示
emmet 这个不用介绍了吧
atom-ternjs js代码提示很强大，高度定制化
docblockr jsdoc 给js添加注释
autoclose-html 闭合html标签
color-picker 取色器 必备插件
pigments 颜色显示插件 必装
terminal-panel 直接在atom里面写命令了
svg-preview svg预览
便捷操作
advanced-open-file 快速打开、切换文件
whitespace 保存代码时清除不必要的空格和制表符
参考

http://blog.csdn.net/crper/article/details/45674649

https://github.com/futantan/atom
