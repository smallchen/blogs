## Android PopupWindow详解

源码
`/frameworks/base/core/java/android/widget/PopupWindow.java（Android 8.0）`

启动一个PopupWindow很简单，两行代码。

```java
PopupWindow window = new PopupWindow(this, R.layout.popup_window);
window.showAtLocation(findViewById(R.id.showWindowTv), Gravity.LEFT|Gravity.TOP, 0, 0);
```

PopupWindow的构造方法

```java
/**
    /**
     * <p>Create a new empty, non focusable popup window of dimension (0,0).</p>
     *
     * <p>The popup does provide a background.</p>
     */
    public PopupWindow(Context context) {
        this(context, null);
    }

    public PopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.popupWindowStyle);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * <p>Create a new, empty, non focusable popup window of dimension (0,0).</p>
     *
     * <p>The popup does not provide a background.</p>
     */
    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    /**
     * <p>Create a new empty, non focusable popup window of dimension (0,0).</p>
     *
     * <p>The popup does not provide any background. This should be handled
     * by the content view.</p>
     */
    public PopupWindow() {
        this(null, 0, 0);
    }

    /**
     * <p>Create a new non focusable popup window which can display the
     * <tt>contentView</tt>. The dimension of the window are (0,0).</p>
     *
     * <p>The popup does not provide any background. This should be handled
     * by the content view.</p>
     *
     * @param contentView the popup's content
     */
    public PopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    /**
     * <p>Create a new empty, non focusable popup window. The dimension of the
     * window must be passed to this constructor.</p>
     *
     * <p>The popup does not provide any background. This should be handled
     * by the content view.</p>
     */
    public PopupWindow(int width, int height) {
        this(null, width, height);
    }

    /**
     * <p>Create a new non focusable popup window which can display the
     * <tt>contentView</tt>. The dimension of the window must be passed to
     * this constructor.</p>
     *
     * <p>The popup does not provide any background. This should be handled
     * by the content view.</p>
     *
     */
    public PopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    /**
     * <p>Create a new popup window which can display the <tt>contentView</tt>.
     * The dimension of the window must be passed to this constructor.</p>
     *
     * <p>The popup does not provide any background. This should be handled
     * by the content view.</p>
     */
    public PopupWindow(View contentView, int width, int height, boolean focusable) {

    }
```

众多构造器，重要的设置有：

1. 是否提供background。有的does provide，有的does not provide。

2. 是否focusable，所有构造器，默认都是not focusable（下面会说focusable的重要作用）。

3. 除非参数指定，所有构造器，宽高默认都是0.


提供background的构造器：

```java
 public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        final Drawable bg = a.getDrawable(R.styleable.PopupWindow_popupBackground);
        mElevation = a.getDimension(R.styleable.PopupWindow_popupElevation, 0);
        mOverlapAnchor = a.getBoolean(R.styleable.PopupWindow_overlapAnchor, false);

        // Preserve default behavior from Gingerbread. If the animation is
        // undefined or explicitly specifies the Gingerbread animation style,
        // use a sentinel value.
        if (a.hasValueOrEmpty(R.styleable.PopupWindow_popupAnimationStyle)) {
            final int animStyle = a.getResourceId(R.styleable.PopupWindow_popupAnimationStyle, 0);
            if (animStyle == R.style.Animation_PopupWindow) {
                mAnimationStyle = ANIMATION_STYLE_DEFAULT;
            } else {
                mAnimationStyle = animStyle;
            }
        } else {
            mAnimationStyle = ANIMATION_STYLE_DEFAULT;
        }

        final Transition enterTransition = getTransition(a.getResourceId(
                R.styleable.PopupWindow_popupEnterTransition, 0));
        final Transition exitTransition;
        if (a.hasValueOrEmpty(R.styleable.PopupWindow_popupExitTransition)) {
            exitTransition = getTransition(a.getResourceId(
                    R.styleable.PopupWindow_popupExitTransition, 0));
        } else {
            exitTransition = enterTransition == null ? null : enterTransition.clone();
        }

        a.recycle();

        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
        setBackgroundDrawable(bg);
}
```
1. 使用context.getSystemService(Context.WINDOW_SERVICE)获取WindowManager。

2. 获取背景资源Drawable bg。

3. 获取动画Transition enterTransition／exitTransition。

4. 设置EnterTransition／ExitTransition／BackgroundDrawable

还是比较简单的。

不提供background的构造器：

```java
public PopupWindow(View contentView, int width, int height, boolean focusable) {
    if (contentView != null) {
        mContext = contentView.getContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    setContentView(contentView);
    setWidth(width);
    setHeight(height);
    setFocusable(focusable);
}
```

1. 使用context.getSystemService(Context.WINDOW_SERVICE)获取WindowManager。

2. 设置ContentView

3. 设置宽高

4. 设置Focusable

综上，PopupWindow构造时，主要是进行实例属性初始化，比如mContentView／mWindowManager／mBackground／mFocusable／mWidth／mHight等等。

PopupWindow的显示。

```java
public void showAtLocation(IBinder token, int gravity, int x, int y) {
    if (isShowing() || mContentView == null) {
        return;
    }

    TransitionManager.endTransitions(mDecorView);

    detachFromAnchor();

    mIsShowing = true;
    mIsDropdown = false;
    mGravity = gravity;

    final WindowManager.LayoutParams p = createPopupLayoutParams(token);
    preparePopup(p);

    p.x = x;
    p.y = y;

    invokePopup(p);
}
```

1. 如果正在显示或者没有设置ContentView，直接返回，不显示。

2. 结束上一个动画（上一个动画可能还没结束）。

3. 创建LayoutParams。

4. 显示。

```java
protected final WindowManager.LayoutParams createPopupLayoutParams(IBinder token) {
    final WindowManager.LayoutParams p = new WindowManager.LayoutParams();

    // These gravity settings put the view at the top left corner of the
    // screen. The view is then positioned to the appropriate location by
    // setting the x and y offsets to match the anchor's bottom-left
    // corner.
    p.gravity = computeGravity();
    p.flags = computeFlags(p.flags);
    p.type = mWindowLayoutType;
    p.token = token;
    p.softInputMode = mSoftInputMode;
    p.windowAnimations = computeAnimationResource();

    if (mBackground != null) {
        p.format = mBackground.getOpacity();
    } else {
        p.format = PixelFormat.TRANSLUCENT;
    }

    if (mHeightMode < 0) {
        p.height = mLastHeight = mHeightMode;
    } else {
        p.height = mLastHeight = mHeight;
    }

    if (mWidthMode < 0) {
        p.width = mLastWidth = mWidthMode;
    } else {
        p.width = mLastWidth = mWidth;
    }

    p.privateFlags = PRIVATE_FLAG_WILL_NOT_REPLACE_ON_RELAUNCH
            | PRIVATE_FLAG_LAYOUT_CHILD_WINDOW_IN_PARENT_FRAME;

    // Used for debugging.
    p.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));

    return p;
}
```
1. new WindowManager.LayoutParams().

2. 填充gravity／flags／token等等。

3. 如果有背景，使用背景的Opacity，如果没有，使用透明格式PixelFormat.TRANSLUCENT。

4. 如果没设置WRAP_CONTENT／MATCH_PARENT，则使用设定的宽高。

5. 设置privateFlags。


```java
private void preparePopup(WindowManager.LayoutParams p) {
    if (mContentView == null || mContext == null || mWindowManager == null) {
        throw new IllegalStateException("You must specify a valid content view by "
                + "calling setContentView() before attempting to show the popup.");
    }

    if (p.accessibilityTitle == null) {
        p.accessibilityTitle = mContext.getString(R.string.popup_window_default_title);
    }

    // The old decor view may be transitioning out. Make sure it finishes
    // and cleans up before we try to create another one.
    if (mDecorView != null) {
        mDecorView.cancelTransitions();
    }

    // When a background is available, we embed the content view within
    // another view that owns the background drawable.
    if (mBackground != null) {
        mBackgroundView = createBackgroundView(mContentView);
        mBackgroundView.setBackground(mBackground);
    } else {
        mBackgroundView = mContentView;
    }

    mDecorView = createDecorView(mBackgroundView);

    // The background owner should be elevated so that it casts a shadow.
    mBackgroundView.setElevation(mElevation);

    // We may wrap that in another view, so we'll need to manually specify
    // the surface insets.
    p.setSurfaceInsets(mBackgroundView, true /*manual*/, true /*preservePrevious*/);

    mPopupViewInitialLayoutDirectionInherited =
            (mContentView.getRawLayoutDirection() == View.LAYOUT_DIRECTION_INHERIT);
}
```

1. 如果没有设置ContentView等，条件不符合，抛出异常。

2. 设置accessibilityTitle。

3. 取消旧的DecorView的动画。

4. 构造器里是否提供background派上用场了，如果没有提供，则使用ContentView作为背景（即没有背景层）；如果有提供背景层，则将ContentView嵌入到背景层，作为背景层的子View。

5. 创建DecorView，子View是背景层。


通过查看createBackgroundView和createDecorView来看布局是如何。

```java
// Wraps a content view in a PopupViewContainer.
private PopupBackgroundView createBackgroundView(View contentView) {
    final ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
    final int height;
    if (layoutParams != null && layoutParams.height == WRAP_CONTENT) {
        height = WRAP_CONTENT;
    } else {
        height = MATCH_PARENT;
    }

    final PopupBackgroundView backgroundView = new PopupBackgroundView(mContext);
    final PopupBackgroundView.LayoutParams listParams = new PopupBackgroundView.LayoutParams(
            MATCH_PARENT, height);
    backgroundView.addView(contentView, listParams);

    return backgroundView;
}

// Wraps a content view in a FrameLayout.
private PopupDecorView createDecorView(View contentView) {
    final ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
    final int height;
    if (layoutParams != null && layoutParams.height == WRAP_CONTENT) {
        height = WRAP_CONTENT;
    } else {
        height = MATCH_PARENT;
    }

    final PopupDecorView decorView = new PopupDecorView(mContext);
    decorView.addView(contentView, MATCH_PARENT, height);
    decorView.setClipChildren(false);
    decorView.setClipToPadding(false);

    return decorView;
}
```
1. 注释很清晰，就是将ContentView包装到BackgroundView或DecorView。

2. 包装时，获取ContentView的LayoutParams，如果设置了WRAP_CONTENT，则使用WRAP_CONTENT，否则都使用MATCH_PARENT。也即是说，指定宽高时，BackgroundView和DecorView都是MATCH_PARENT。

3. BackgroundView 和 DecorView 都是FrameLayout。

目前为止，都只是停留在ViewGroup相关的操作。

```java
private void invokePopup(WindowManager.LayoutParams p) {
    if (mContext != null) {
        p.packageName = mContext.getPackageName();
    }

    final PopupDecorView decorView = mDecorView;
    decorView.setFitsSystemWindows(mLayoutInsetDecor);

    setLayoutDirectionFromAnchor();

    mWindowManager.addView(decorView, p);

    if (mEnterTransition != null) {
        decorView.requestEnterTransition(mEnterTransition);
    }
}
```

PopupWindow显示时，只是调用了mWindowManager.addView(decorView, p);

以上，就是那两行代码的流程分析。
1. 根据传递进来的ContentView构建好DecorView。
2. 使用WindowManager显示DecorView。

问题：**PopupWindow点击窗口外消失是如何实现的？**

从头开过来，发现唯一可能和触摸事件相关的，就只有创建WindowManager.LayoutParams时，在computeFlags里，根据Touchable，Focusable，OutsideTouchable设置了一系列Flags。其中包含FLAG_NOT_FOCUSABLE，FLAG_NOT_TOUCHABLE，FLAG_WATCH_OUTSIDE_TOUCH。

```java
private int computeFlags(int curFlags) {
    curFlags &= ~(
            WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH);
    if(mIgnoreCheekPress) {
        curFlags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
    }
    if (!mFocusable) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (mInputMethodMode == INPUT_METHOD_NEEDED) {
            curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
    } else if (mInputMethodMode == INPUT_METHOD_NOT_NEEDED) {
        curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
    }
    if (!mTouchable) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }
    if (mOutsideTouchable) {
        curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    }
    if (!mClippingEnabled || mClipToScreen) {
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
    }
    if (isSplitTouchEnabled()) {
        curFlags |= WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
    }
    if (mLayoutInScreen) {
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
    }
    if (mLayoutInsetDecor) {
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
    }
    if (mNotTouchModal) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    }
    if (mAttachedInDecor) {
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR;
    }
    return curFlags;
}
```

所以可以猜测，mWindowManager.addView(decorView, p);中，WindowManager.LayoutParams p的设置，影响着decorView的事件。

看回DecorView：

```java
private class PopupDecorView extends FrameLayout {
    /** Runnable used to clean up listeners after exit transition. */
    private Runnable mCleanupAfterExit;

    public PopupDecorView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mTouchInterceptor != null && mTouchInterceptor.onTouch(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        if ((event.getAction() == MotionEvent.ACTION_DOWN)
                && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
            /// 重点
            dismiss();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            /// 重点
            dismiss();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }
```

点击事件，被分发到PopupWindow最底层DecorView。DecorView就是一个ViewGroup。触摸事件的分发，从dispatchTouchEvent开始。

1. 事件到达时，先查看是否有设置mTouchInterceptor，先执行事件截获器。

2. 如果截获了则结束，否则调用super.dispatchTouchEvent。

3. ViewGroup的dispatchTouchEvent默认会调用onTouch和onTouchEvent。这里没有关注onTouch，所以会执行到onTouchEvent。

4. onTouchEvent中，
1. 如果是ACTION_DOWN且触摸点在DecorView范围外，则dismiss。
2. 如果是ACTION_OUTSIDE，则dismiss。
换句话，触点在PopupWindow外，或收到ACTION_OUTSIDE，PopupWindow就会关闭。

**至此，PopupWindow的构建，弹出，消失结束**

相信很多人都会奇怪，为啥会这样，PopupWindow中最关键的，Touchable，Focusable，OutsideTouchable，TouchModal好像一点也没用上，就只是用于设置了Flags，然后就收到了事件。

## 这就是本文重点

非常简单，WindowManager.addView(view, params)，就是可以通过params参数来指定添加的view的窗口样式，触摸事件的传递也是通过params参数设置的。即使是PhoneWindow，最后也是使用WindowManager.addView来将DecorView来显示出来。

具体看另一篇，Window Flags详解。要明白下面，必须先清楚Window Flags作用。

## 如何让PopupWindow点击外部窗口不关闭。

先分析PopupWindow关闭的时机：ACTION_DOWN且触摸点在窗口范围外；收到ACTION_OUTSIDE事件。

思路1:
让PopupWindow不接收ACTION_DOWN且不接收ACTION_OUTSIDE事件。设置FLAG_NOT_TOUCHABLE；并且去掉FLAG_WATCH_OUTSIDE_TOUCH。也即是，

```java
setTouchable(false);
setOutsideTouchable(false);
```
然后，你发现，点击PopupWindow外部确实不关闭弹窗，但PopupWindow也收不到任何事件。所以，Touchable必须为true。

思路2:
既然事件必须接受，所以可以重写事件分发，不让PopupWindow处理外部的ACTION_DOWN和ACTION_OUTSIDE。

回顾Touchable必须为true。PopupWindow默认TouchModal为true。就剩下OutsideTouchable和Focusable。

2.1 如果Focusable为true，因为TouchModal也为true，那么所有事件将被PopupWindow截获，在窗口外的事件，默认会分发到PopupWindow的onTouchEvent里面。所以无法实现，除非重写PopupWindow的dispatchTouchEvent方法，截获外部的ACTION_DOWN事件。**这种场景没办法实现，因为窗口外的事件不能透传到下一个窗口，要在这种场景下实现，要自实现PopupWindow，需要改动的地方比较多，相对复杂**

2.2 虽然Focusable为true，但如果使TouchModal为false，还是可以实现。但setTouchModal接口是隐藏的，如果能够实现下面代码，一样可以使得点击窗口外面不关闭，窗口内有完整事件，且窗口是Focusable。

```java
setTouchable(true);
setFocusable(true);
setTouchModal(false);
setOutsideTouchable(false);
```
> setTouchModal是隐藏方法，需要使用反射来调用

```java
private void setTouchModal(boolean bl) {
    try {
        Class cls = new PopupWindow().getClass();
        Method method = cls.getMethod("setTouchModal", boolean.class);
        method.setAccessible(true);
        method.invoke(this, bl);
    } catch (Exception e) {
        Log.e("", "setTouchModal", e);
    }
}
```

2.3 如果Focusable为false，窗口内有完整事件，窗口外可能有ACTION_OUTSIDE事件，如果设置OutsideTouchable为false，可以不接收ACTION_OUTSIDE事件。所以，

```java
setTouchable(true);
setFocusable(false);
setOutsideTouchable(false);
```
这样就可以实现，窗口外不关闭PopupWindow弹窗。

但这里有个缺点，Focusable为false，意味着，软键盘输入可能会是问题。如果Focusable（这里不深入，具体可查看Touchable为false时如何打开软键盘这类）。


2.4 如果启用了点击外部关闭弹窗，但发现窗口并没有关闭。如下。

```java
setTouchable(true);
setFocusable(false);
setOutsideTouchable(true);
```

原因：PopupWindow的父窗口，是WindowManager那层View。比如，如果使用WindowManager添加了一个小区域，那么PopupWindow的坐标是相对于这个小区域的坐标系，而不是相对于整个屏幕。所以，对于PopupWindow，所有事件的来源，先来源于这个WindowManager上的根View。

这个例子，是因为WindowManager添加了一个小区域。如下。

```java
final WindowManager.LayoutParams paramsBottom = new WindowManager.LayoutParams(
		WindowManager.LayoutParams.MATCH_PARENT,
		10,
		WindowManager.LayoutParams.TYPE_SEARCH_BAR,
		WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
		PixelFormat.TRANSPARENT
);
paramsBottom.gravity = Gravity.START|Gravity.BOTTOM;
mWindowManager.addView(mBottomPanel, paramsBottom);
```
这个WindowManager添加的View中，并没有`WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH`标记。所以，它本身不能捕获`ACTION_OUTSIDE`，所以就无法将事件传递给PopupWindow，导致PopupWindow无法响应外部事件关闭窗口。

本来以为是这样，结果打脸了。上面的代码并不能解决。需要将Type升级为`WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG 或 TYPE_SYSTEM_ALERT`才能捕获到`ACTION_OUTSIDE`。
