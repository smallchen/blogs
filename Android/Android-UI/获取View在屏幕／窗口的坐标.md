相对于屏幕的坐标

```java
int[] location = new int[2];
view.getLocationOnScreen(location);
```

相对于父窗口的坐标

```java
int[] location = new int[2];
view.getLocationInWindow(location);
```

可见区域范围

```java
Rect rect = new Rect();
view.getLocalVisibleRect(rect);
```

附录

```java

int[] location = new int[2];
mView.getLocationOnScreen(location);

Rect srect = new Rect(-1, -1, -1, -1);
srect.left = location[0];
srect.top = location[1];
srect.right = srect.left + mView.getWidth();
srect.bottom = srect.top + mView.getHeight();
NLog.d(TAG, " Screen Rect:"+srect.toString());

mView.getLocationInWindow(location);
Rect wrect = new Rect(-1, -1, -1, -1);
wrect.left = location[0];
wrect.top = location[1];
wrect.right = wrect.left + mView.getWidth();
wrect.bottom = wrect.top + mView.getHeight();
```
