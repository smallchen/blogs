####

* Pressed
* Enabled
* Checked
* Focus
* Selected
* Clickable


#### Clickable

`view.setClickable(false);`

表示view不响应手势。手势开始的`Down`不会被view所消化。所以，事件会向上冒泡。

无论怎么冒泡，都是`onTouch`然后到`onTouchEvent`。如果`onTouch`返回了`true`，那么就不会执行`onTouchEvent`，继而不会有任何`click`事件。

默认的`click`事件是由`onTouchEvent`触发的，没有执行到`onTouchEvent`就不会有事件。

如果某个`view`的`onTouch`可以打印出`Down`事件，那么说明事件已经派发到当前`view`。

#### Pressed

Pressed的状态不会保持，UI的触摸可以直接改变一个view的`pressed`状态。


#### Selected

同上，UI的触摸可以直接改变一个view的`selected`状态。

#### Checked

可以持久保留。
