## Android ViewRootImpl与View的关系

```java
ViewRootImpl root;
View view;
WindowManager.LayoutParams wparams;

root = new ViewRootImpl(view.getContext(), display);
root.setView(view, wparams, panelParentView);
```

1. `ViewRootImpl`只会绑定一次，且只绑定一个`View`，通过`setView()`绑定。
2. `ViewRootImpl`会把`View`也设置到`mAttachInfo`。
3. 事件通过`processPointerEvent(QueuedInputEvent q)`，分发到与`ViewRootImpl`绑定的`View`（其实就是root view，或，decorView）。
4. 绘制通过`ViewRootImpl.performDraw()`开始分发，分发到与`ViewRootImpl`绑定的`View`。调用`mView.draw(canvas);`分发到与之绑定的`View`。（具体看另一篇《View绘制详解》


总结：

`ViewRootImpl`并不是一个View；可以将其理解成`View`的逻辑层`Presenter`，主要用于与系统层进行交互，然后通知与之绑定的`View`层。

```java
View <-> ViewRootImpl <-> Config(Translator/Location/)/Input/Display(Measure/Layout/Draw)/
```
