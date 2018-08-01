## Android WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)构造方法不生效！

使用下面的参数，构建一个`WindowManager`浮窗，发现浮窗是`MATCH_PARENT, MATCH_PARENT`!!，也即是说，`WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)`的构造方法不生效。

```java
WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
params.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
params.gravity = Gravity.LEFT | Gravity.TOP;
params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
params.format = PixelFormat.RGBA_8888;
```

你一定很奇怪，构造方法没有报错，为啥不生效？

请`Command+P`看一下对应的参数，或`Command+B`跳转源码看一下：

`public LayoutParams(int _type, int _flags)`

对应的是`type`和`flags`，并不是`width`和`height`!!!

这下逗逼了吧。

正确的应该是：

```java
// 最少参数
WindowManager.LayoutParams params = new WindowManager.LayoutParams();
params.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
// params.gravity = Gravity.LEFT | Gravity.TOP;
// params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
// params.format = PixelFormat.RGBA_8888;
// params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
// params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
```

`gravity`默认为`Gravity.CENTER`
`flags`默认为`FLAG_FOCUSABLE`，获取所有焦点和事件！！
`width,height`默认为`MATCH_PARENT`
