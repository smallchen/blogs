
<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [atom markdown toc 插件源码修改](#atom-markdown-toc-插件源码修改)
	- [修复非H1开头的标题转化为CodeBlock的错误](#修复非h1开头的标题转化为codeblock的错误)
	- [修复代码块中注释成为TOC标题的错误](#修复代码块中注释成为toc标题的错误)
- [扩展：atom插件的修改](#扩展atom插件的修改)

<!-- /TOC -->

## atom markdown toc 插件源码修改

### 修复非H1开头的标题转化为CodeBlock的错误

markdown toc对于非以H1开头的标题转化为了`Code block`，有两个方式可以修改：

1、修改markdown toc的options：

```java
<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->
// 将depthFrom改为2，保存一下，就能自动更新。
```

2、修改插件：

`atom - packages - toc - source code`。打开源码。

源码目录通常在`~/.atom/package`下。

修改`Toc.coffee`里面的`__createList()`方法为：

```javascript
__createList: () ->
  list = []
  depthFrom = if @options.depthFrom isnt undefined then @options.depthFrom else 1
  depthFirst = -1
  depthTo = if @options.depthTo isnt undefined then @options.depthTo else 6
  indicesOfDepth = Array.apply(null, new Array(depthTo - depthFrom)).map(Number.prototype.valueOf, 0);

  for own i, item of @list
	row = []
	for tab in [depthFrom..item.depth] when tab > depthFirst
	  if depthFirst isnt -1
		row.push "\t"
	if @options.orderedList is 1
	  row.push ++indicesOfDepth[item.depth-1] + ". "
	  indicesOfDepth = indicesOfDepth.map((value, index) -> if index < item.depth then value else 0)
	else
	  row.push "- "
	  # jokin add
	  if depthFirst is -1
		depthFirst = item.depth
	  # --

	line = item.line.substr item.depth
	line = line.trim()
	if @options.withLinks is 1
	  row.push @___createLink line
	else
	  row.push line

	list.push row.join ""
  if list.length > 0
	return list
  return false
```

思路是以第一个出现的标题作为TOC顶级，不添加过多的`\t`。

修改完毕，需要重启atom插件的修改才能生效。


### 修复代码块中注释成为TOC标题的错误

在`Toc.coffee`里面，修改`__updateList()`上半部分为：

```java
  # parse all lines and find markdown headlines
  __updateList: () ->
    @___updateLines()
    @list = []
    parsingCodeBlock = false
    for i of @lines
      line = @lines[i]

      if line.indexOf('```') > -1
          if not parsingCodeBlock
             parsingCodeBlock = true
          else if parsingCodeBlock
             parsingCodeBlock = false
      if parsingCodeBlock
          continue
      ... ...
```

这个Bug最新版的`markdown-toc`已经解决。它是这样解决的：

```java
# parse all lines and find markdown headlines
  __updateList: () ->
    @___updateLines()
    @list = []
    isInCodeBlock = false
    for i of @lines
      line = @lines[i]
      isInCodeBlock = !isInCodeBlock if line.match /^```/
      continue if isInCodeBlock
      ... ...
```

直接`clone`<https://github.com/jokinkuang/markdown-toc.git>到`~/.atom/packages/`下面，重启atom即可完成atom插件的安装和更新。

这个仓库在最新版本基础上，修复了H1标题的问题。


## 扩展：atom插件的修改

atom插件的语法，是`CoffeeScript`，和`javascript`很像。

简单修改以下还是比较容易的。
