## Android浮窗缩放的实现

1.以LEFT,TOP为坐标系的缩放实现。

```java
public void handleEvent(MotionEvent event) {
    int x = (int) event.getRawX();
    int y = (int) event.getRawY();
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mTouchLastX = x;
            mTouchLastY = y;
            onStart(x, y);
            break;
        case MotionEvent.ACTION_MOVE:
            int offsetX = x - mTouchLastX;
            int offsetY = y - mTouchLastY;
            onContinue(offsetX, offsetY);
            // a new move with new width.
            mTouchLastX = x;
            mTouchLastY = y;
            break;
        case MotionEvent.ACTION_UP:
        default:
            break;
    }
}
```

其中，`onContinue(offsetX, offsetY)`要改变窗口的宽高。
改变后，要重新设置`mTouchLastX`和`mTouchLastY`是因为，属性`mParam`是一个引用，会在onContinue的时候发生改变！

1. `mParam`是一个引用，当WindowManager的窗口的`LayoutParams`改变时，可能会联动改变。
2. `onContinue`里面，使用的是`mParams += offset`意味着，mParams每次都对基数进行了改变！此时应该使用局部变量。
