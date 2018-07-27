#### PopupWindow坐标问题

```java
showAtLocation(parent, Gravity.BOTTOM|Gravity.END, left, top);
```

showAtLocation是相对于，当前parent所在的WindowManager中的根View布局。

比如，如果在屏幕右下角创建一个`200*200`区域的View加入WindowManager，下面代码，
`showAtLocation(parent, Gravity.BOTTOM|Gravity.START, 0, 0)`虽然设置的坐标是（0，0），但显示的位置是相对于右下角`200*200`的区域。



#### 点击外部按钮，不会关闭问题。

有个案例，往WindowManager里添加两片ViewGroup，分别位于屏幕左下边的A，和右下边的B。高度wrap_content。

```java
final WindowManager.LayoutParams paramsLeft = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SEARCH_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);
        paramsLeft.gravity = Gravity.START|Gravity.BOTTOM;
mLeftToolbarPanel = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.toolbar_left_panel, null);
mRightToolbarPanel = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.toolbar_right_panel, null);


mWindowManager.addView(mLeftToolbarPanel, paramsLeft);
mWindowManager.addView(mRightToolbarPanel, paramsRight);
```

A和B上各有一个按钮弹出一个PopupWindow。

奇怪的现象出现了。

A先添加，B后添加。A弹出的窗口，点击B的按钮并不会令A窗口关闭。反过来，B弹出的窗口，点击A的按钮是可以让窗口关闭的。

这与添加到WindowManager顺序相关。因为反过来。先添加B，后添加A，情况就反过来。

两边弹出的窗口，点击屏幕上面的其它既不属于A也不属于B的区域，都可以关闭。

记录一下。可能是最顶层的窗口才能够点击任何地方都关闭。
