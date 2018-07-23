## Android WindowManager.LayoutParams与ViewGroup.LayoutParams区别

WindowManager源码里：

`public static class LayoutParams extends ViewGroup.LayoutParams implements Parcelable`

可见，`WindowManager.LayoutParams`是`ViewGroup.LayoutParams`的子类。

同样：**几乎所有LayoutParams都继承自ViewGroup.LayoutParams**。

也即是说，当我们使用`public ViewGroup.LayoutParams getLayoutParams()`获取当前View的`LayoutParams`实例时，这个`LayoutParams`实例可能是`ViewGroup.LayoutParams`也可能是`WindowManager.LayoutParams`。

## public ViewGroup.LayoutParams getLayoutParams()

`getLayoutParams()`和`setLayoutParams()`是View提供的方法。

查看文档，比较重要的几点：

1. This method may return null if this View is not attached to a parent
2. When a View is attached to a parent ViewGroup, this method must not return null
3. There are many subclasses of ViewGroup.LayoutParams, and these correspond to the different subclasses of ViewGroup

1. 如果View没有添加到任何parent中，getLayoutParams()返回null。
2. 如果View已经添加到parent中，getLayoutParams()一定返回非null。
3. 得到的是ViewGroup.LayoutParams的子类实现（取决于你添加到的是那个容器的LayoutParams）。

以上，举例：

当一个`View`被添加到`WindowManager`，这个`View`的`getLayoutParams()`得到的其实是`WindowManager.LayoutParams`!!!

## public void setLayoutParams()

对于普通ViewGroup.LayoutParams，`setLayoutParams()`是可以设置新的`LayoutParams`并且刷新视图。
但是对于浮窗，WindowManager.LayoutParams，`setLayoutParams()`并不能导致浮窗刷新布局，而必须使用`WindowManager.updateViewLayout`!

## 打印

2. 打印：

`layoutParams:WM.LayoutParams{(0,0)(wrapxwrap) gr=#33 ty=2002 fl=#1000008 fmt=1}`

表示：这个LayoutParams是`WM`（WindowManager），`(0,0)(wrapxwrap)`坐标是(0,0)，内容是(wrap_content, wrap_content)，`gr=#33`gravity是`#33`，`ty=2002`type是2002，`fl=#1000008`flag是`#1000008`。

`layoutParams:WM.LayoutParams{(0,0)(270x340) gr=#33 ty=2002 fl=#8 fmt=1}`

表示：宽高是`270x340`。
