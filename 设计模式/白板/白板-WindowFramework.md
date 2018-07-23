## 白板WindowFramework层实现

### 实现多个进程，一个recent窗口

1. 主Activity的launchmode设置为`singleInstance`。
2. 副Activity设置`excludeFromRecents`为true。

这样，从副Activity启动主Activity，只有一个主Activity出现在Recent列表中。
唯一的缺陷是，Recent列表中的截图，没有副Activity产生的内容。
