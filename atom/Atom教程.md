https://www.w3cschool.cn/atom/zyhq1hqn.html

Ait + shift + T 插入日期

### 全局hard tab与soft tab转换

* Hard Tab 使用Tab符号(\t)
* Soft Tab 使用空格代替Tab

`command + shift + f` 全局查找，勾选`.*`开启`Regex`，然后输入`\t`，替换为4个空格。

反过来，则是soft tab替换为hard tab。

另一种方式：据说atom的Lines-AutoIntent也可以进行转换。但应该针对当前文件。

**tabs to spaces插件**

使用`tabs to spaces`插件，可以实现`Tab`和`Space`的转换：

* Tabify 转换为Tab符号
* Untabify 转换内容为`@editor.getTabLength()`空格
* Untabify All 转换当前行。

插件的配置页，可以配置，`onSave`操作时自动转换。

这个插件目前只支持当前文件，如果要操作整个项目，无力。
