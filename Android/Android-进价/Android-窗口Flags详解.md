## Android 窗口Flags详解

这里主要探讨Touchable，Focusable，OutsideTouchable，TouchModal这四个混合使用的效果。

```java
public static final int FLAG_NOT_FOCUSABLE      = 0x00000008;
public static final int FLAG_NOT_TOUCHABLE      = 0x00000010;
public static final int FLAG_NOT_TOUCH_MODAL    = 0x00000020;
public static final int FLAG_WATCH_OUTSIDE_TOUCH = 0x00040000;

private int computeFlags(int curFlags) {
    boolean mTouchable = true;
    if (!mTouchable) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    } else {
        curFlags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    boolean mFocusable = false;
    if (!mFocusable) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    } else {
        curFlags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    boolean mTouchModal = true;
    if (!mTouchModal) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    } else {
        curFlags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    }

    boolean mOutsideTouchable = true;
    if (mOutsideTouchable) {
        curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    } else {
        curFlags &= ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    }

    return curFlags;
}
```

1. Touchable （默认为true）

最简单的Touchable，
为false，表示窗口不接受触摸事件；
为true，表示窗口接受触摸事件；

要窗口接收事件，必须为true。窗口不接受事件，意味着事件会透传到下一个窗口。这里的窗口事件是指DOWN-UP，窗口是指窗口自身范围，窗口外的ACTION_OUTSIDE与此设置无关。

2. OutsideTouchable（默认为false）

为false，表示对ACTION_OUTSIDE事件不感兴趣。
为true，表示对ACTION_OUTSIDE事件感兴趣，此时，**如果新事件被另一个窗口消化**，则会发送ACTION_OUTSIDE给该窗口。包括：

2.1. 如果窗口设置了Touchable为false。即使触摸事件在窗口内，由于不处理事件，导致事件透传，被另一个窗口消化，此时该窗口也会收到ACTION_OUTSIDE。

2.2 触摸事件在窗口外面触发，导致事件被另一个窗口消化，此时该窗口也会收到ACTION_OUTSIDE。

2.3 如果窗口设置了TouchModal和Focusable，导致窗口内外的事件被当前窗口截获，由于不是被另一个窗口消化，所以即使设置了OutsideTouchable，也不会有ACTION_OUTSIDE。

3. Focusable（默认为true）

为false，表示不会聚焦，所以不会有软键盘。同时它的z-order可以在软键盘之上，覆盖软键盘。如果你在不聚焦的情况下，还需要软键盘，可以使用FLAG_ALT_FOCUSABLE_IM来修改。**如果为false，会放弃TouchModal原来的值，强制设置TouchModal为false**

为true，表示窗口可以聚焦。

> Focusable生效，通常需要Touchable为true。如果窗口Touchable为false，窗口可以聚焦顶多可以弹出键盘，窗口自身不会收到Touch事件。

4. TouchModal（默认为true）

**TouchModal的设置，只有在Focusable为true时才有效，Focusable为false，会忽略TouchModal的值**

为true，当窗口Focusable为true时，无论窗口内外，事件都被当前窗口接收。
为false，当窗口Focusable为true时，只有窗口内的事件被当前窗口接收。窗口外，OutsideTouchable的设置决定了是否有ACTION_OUTSIDE事件。

为true，当窗口Focusable为false时，设置不生效。
为false，当窗口Focusable为false时，设置不生效。

## 总结：
1. 如何知道这些Flag的默认值？

FLAG_NOT_TOUCHABLE，意味着，默认是TOUCHABLE，必要时，才使用这个Flag关闭。
FLAG_NOT_FOCUSABLE，意味着，默认是FOCUSABLE，必要时，才使用这个Flag关闭。
FLAG_WATCH_OUTSIDE_TOUCH，意味着，默认是不关心，必要时，才使用这个Flag开启。
FLAG_NOT_TOUCH_MODAL，意味着，默认是TOUCH_MODAL，必要时，才使用这个Flag关闭。

2. 如何看Flag注释里的enable／disable：

```java
/** Window flag: this window won't ever get key input focus, so the
 * user can not send key or other button events to it.  Those will
 * instead go to whatever focusable window is behind it.  This flag
 * will also enable {@link #FLAG_NOT_TOUCH_MODAL} whether or not that
 * is explicitly set.
 *
 * <p>Setting this flag also implies that the window will not need to
 * interact with
 * a soft input method, so it will be Z-ordered and positioned
 * independently of any active input method (typically this means it
 * gets Z-ordered on top of the input method, so it can use the full
 * screen for its content and cover the input method if needed.  You
 * can use {@link #FLAG_ALT_FOCUSABLE_IM} to modify this behavior. */
public static final int FLAG_NOT_FOCUSABLE      = 0x00000008;
```
FLAG_NOT_FOCUSABLE的注释是这样的。This flag will also enable FLAG_NOT_TOUCH_MODAL whether or not that is explicitly set. 

**所谓的enable，就是使用这个Flag**。直接翻译就是，使用了FLAG_NOT_FOCUSABLE这个Flag，就会同时使用FLAG_NOT_TOUCH_MODAL这个flag。

使用FLAG_NOT_FOCUSABLE就是关闭FOCUSABLE，使用FLAG_NOT_TOUCH_MODAL就是关闭TOUCH_MODAL。简述就是，关闭了FOCUSABLE，会同时关闭TOUCH_MODAL。

3. 除了注释中有说明，否则这些Flag都是单独生效的。

比如，Touchable，当一个事件确实派发到窗口到时候，就看这个Flag，为true就是接受事件，为false就是不接受事件。你可以提前截获不让事件派发到窗口，但一旦派发到窗口，就是这个Flag来决定窗口是否接受事件。

比如，OutsideTouchable，你可以截获事件不让事件派发到另一个窗口，但一旦事件派发到另一个窗口，就是这个Flag决定当前窗口是否接受ACTION_OUTSIDE事件。


## 实例分析：

```java
mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
final WindowManager.LayoutParams p = new WindowManager.LayoutParams();
p.width = WindowManager.LayoutParams.WRAP_CONTENT;
p.height = WindowManager.LayoutParams.WRAP_CONTENT;
mWindowManager.addView(view, p);
```
以下，窗口内外是指，触点在窗口内／外。完整事件是指DOWN to UP事件流。ACTION_OUTSIDE事件只有一次。

1. WindowManager.LayoutParams使用默认设置。
意味着，都使用默认值：Touchable=true，OutsideTouchable=false，Focusable=true，TouchModal=true。
结果是：窗口内外事件都被当前弹窗截获。

2. 上面参数把OutsideTouchable=true。即
Touchable=true，OutsideTouchable=true，Focusable=true，TouchModal=true。
结果是：同上，窗口内外事件都被当前弹窗截获。没有事件透传到下一个窗口，所以内外不会有ACTION_OUTSIDE事件。

3. Touchable=false，OutsideTouchable=false，Focusable=true，TouchModal=true。
结果是：内外的事件都透传，窗口收不到任何事件。（窗口内不接受事件，事件透传。窗口外由OutsideTouchable=false控制，不接收ACTION_OUTSIDE事件，所以窗口收不到任何事件）

4. 上面参数把OutsideTouchable=true。即
Touchable=false，OutsideTouchable=true，Focusable=true，TouchModal=true。
结果是：窗口内外都收到ACTION_OUTSIDE事件。（窗口内外事件都透传到下一个窗口，相当于对于窗口而言，都是窗口外，所以点击窗口内外，窗口都能收到ACTION_OUTSIDE事件）

总结，**Touchable和OutsideTouchable分别控制了窗口内外的事件**。

5. Touchable=true，OutsideTouchable=true，Focusable=true，TouchModal=true。
结果是：窗口内外事件都被当前弹窗截获，是完整事件。内外都不会有ACTION_OUTSIDE事件。

6. Touchable=true，OutsideTouchable=true，Focusable=true，TouchModal=false。
结果是：窗口内有完整事件；窗口外有ACTION_OUTSIDE事件。如果将OutsideTouchable设为false，则窗口外没有ACTION_OUTSIDE事件。

7. Touchable=true，OutsideTouchable=true，Focusable=false，TouchModal=false/true。
结果是：同上。窗口内有完整事件；窗口外有ACTION_OUTSIDE事件。如果将OutsideTouchable设为false，窗口外没有ACTION_OUTSIDE事件。

总结，**当且仅当Focusable为true，TouchModal为true情况下，窗口内外事件才被当前窗口截获；否则，都是窗口内才有完整事件，窗口外才有ACTION_OUTSIDE事件**。

>注意，当前窗口收到的完整事件和ACTION_OUTSIDE事件，都是先分发到窗口的DecorView，即WindowManager.addView(view, p);里面的view实例。
当事件分发到View，就进入View的事件分发流程。
