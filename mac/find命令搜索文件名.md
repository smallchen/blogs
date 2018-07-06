## find命令搜索文件名

find 基本用法
find path -name "(字符，可以用wildcard)"

默认情况下搜寻path以及其所有子目录下的文件。

举例
```shell
find . -name "*abc*"
# 找出当前目录以及其所有子目录下所有名字中包含“abc”三字的文件

find . -name "*.txt" -maxdepth 1
# 找出当前目录（不包括子目录）下所有名字中后缀为".txt"的文件
```
