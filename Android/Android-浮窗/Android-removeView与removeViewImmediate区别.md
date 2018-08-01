<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android removeView与removeViewImmediate区别](#android-removeview与removeviewimmediate区别)
- [WindowManager中removeView与removeViewImmediate的区别](#windowmanager中removeview与removeviewimmediate的区别)
- [WindowManager: android.view.WindowLeaked](#windowmanager-androidviewwindowleaked)

<!-- /TOC -->
## Android removeView与removeViewImmediate区别

首先，看代码：

```java
// WindowManagerImpl.java
@Override
public void removeView(View view) {
    mGlobal.removeView(view, false);
}

@Override
public void removeViewImmediate(View view) {
    mGlobal.removeView(view, true);
}
```

* `removeView`方法来源于接口`ViewManager`
* `removeViewImmediate`方法来源于接口`WindowManager`。

`ViewManager`接口是`WindowManager`接口的父类。

> ViewManager定义了View管理的接口，通常是View容器实现该接口

关系如下：

ViewGroup <- ViewManager
WindowManagerImpl <- WindowManager <- ViewManager

`removeViewImmediate`只存在于`WindowManager`。

`removeView`既存在于普通的`ViewGroup`，也存在于`WindowManager`。

所以，要讨论removeView与removeViewImmediate的区别，只存在于`WindowManager`里面。

换句话：`WindowManager`中，removeView与removeViewImmediate的区别是什么？

## WindowManager中removeView与removeViewImmediate的区别

看源码：

```java
# WindowManagerImpl.java
- WindowManagerGlobal:removeView(View view, boolean immediate)
    - int index = findViewLocked(view, true);
    - removeViewLocked(index, immediate);
        - ViewRootImpl root = mRoots.get(index);
        - View view = root.getView();
        - boolean deferred = root.die(immediate)
            - ViewRootImpl.die(boolean immediate)
            - immediate ?
                T: doDie(); return false;
                    - dispatchDetachedFromWindow();
                    - WindowManagerGlobal.getInstance().doRemoveView(this);
                        - WindowManagerGlobal:doRemoveView(ViewRootImpl root)
                        - int index = mRoots.indexOf(root)
                        - mRoots.remove(index);
                F: mHandler.sendEmptyMessage(MSG_DIE); return true;
        - view.assignParent(null);
        - deferred ? mDyingViews.add(view)


# ViewRootImpl:handleMessage(Message msg)
- case MSG_DIE: doDie(); break;
```

从源码可以看出：

`removeViewImmediate`是直接调用`doDie()`将与`View`(就是添加到WindowManager中的View)绑定的`ViewRootImpl`（添加到WindowManager时，会为每个添加的View绑定到一个ViewRootImpl中）直接从`WindowManagerGlobal`中移除。也即是说，在当前主线程中，直接执行移除，移除后，`mRoots`数组里就不再存在已经删除的`ViewRootImpl`。

`removeView`则是在当前主线程中，发送一个`MSG_DIE`消息到主线程消息队列里。等待当前主线程执行完毕后，轮询到`MSG_DIE`消息时，才执行`doDie()`来自我销毁。

区别很明显：`removeViewImmediate`在当前消息循环中就进行了自我销毁；而`removeView`是等待下一次消息循环时，才有机会进行自我销毁。

乍一看，明白了也好像看不出有什么影响。下面是一个很好的例子来说明两者的影响。

## WindowManager: android.view.WindowLeaked

下面是一个添加View到WindowManager中显示，然后在Activity销毁时，移除View的例子。

```java
@Override
public void onClick(View v) {
    mWindowManager.addView(mView, new WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            0,
            0,
            WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.RGBA_8888
    ));
}

@Override
protected void onDestroy() {
    if (mView != null) {
        mWindowManager.removeView(mView);
        mView = null;
    }
    super.onDestroy();
}
```

以上是一个很常见的例子。但上面的代码会报`android.view.WindowLeaked`异常：

```java
WindowManager: android.view.WindowLeaked: Activity com.jokin.demo.demolayoutparams.MainActivity has leaked window android.widget.TextView{f0b5c64 V.ED..... ......ID 0,0-551,57} that was originally added here
    at android.view.ViewRootImpl.<init>(ViewRootImpl.java:485)
    at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:346)
    at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:93)
    at com.jokin.demo.demolayoutparams.MainActivity.onClick(MainActivity.java:102)
```

你一定很奇怪，明明已经提取对`View`进行移除了，为何会报错误？

如果对`WindowManager.removeView()`有所了解，就是上面分析的那个，你就知道，`WindowManager.removeView()`并不是立即对View进行移除，而是等到下一个消息循环。由于当前的消息循环已经是`Activity:onDestroy()`了，所以，当Activity销毁后，当前的View才会被销毁。销毁顺序有问题，所以报错。

把`WindowManager.removeView()`改为`removeViewImmediate()`就可以解决这个错误。

**知其然而知其所以然：android.view.WindowLeaked的出处**

Android中子窗口是由`WindowManagerGlobal`管理的，子窗口是依附在Activity之上。如果Activity已经销毁而子窗口还存在，则表明没有正确销毁子窗口，子窗口发生内存泄漏。

所以，这个`android.view.WindowLeaked`的异常是在Activity销毁时可能抛出的异常。

全局搜索`WindowLeaked`，可以发现只出现在`WindowManagerGlobal.java`里：

```java
// WindowManagerGlobal.java
public void closeAll(IBinder token, String who, String what) {
    closeAllExceptView(token, null /* view */, who, what);
}
public void closeAllExceptView(IBinder token, View view, String who, String what) {
    synchronized (mLock) {
        int count = mViews.size();
        // 如名称所示：除了View外关闭其它View。当View=null，则表示删除所有。
        // view=null token!=null 则表示删除指定token下所有view！
        for (int i = 0; i < count; i++) {
            if ((view == null || mViews.get(i) != view)
                    && (token == null || mParams.get(i).token == token)) {
                ViewRootImpl root = mRoots.get(i);

                if (who != null) {
                    WindowLeaked leak = new WindowLeaked(
                            what + " " + who + " has leaked window "
                            + root.getView() + " that was originally added here");
                    leak.setStackTrace(root.getLocation().getStackTrace());
                    // 异常只是打印出来，并没有抛出异常。。。
                    Log.e(TAG, "", leak);
                }

                // 等价于 removeView() 源代码就不贴了。
                removeViewLocked(i, false);
            }
        }
    }
}
```

1. closeAll通过参数token，view来控制删除操作。主要分为下面情况：

* view == null，token != null，表示删除指定token所关联的所有view，（mViews数组中，token为指定token的，所有view）。
* view != null, token != null，表示删除指定token下，除view外所有view，（mViews数组中，token为指定token的，除view外所有view）。
* view == null, token == null，表示删除mViews整个数组。


但，调用到这里进行删除已经太晚了！因为这个方法只在Activity销毁后，用于对绑定在Activity的窗口（View）进行清理。也就是说，到这里还能找到的窗口（View），都是没有正确销毁的，都是溢出的。

`WindowLeaked`异常只是用于警告用户，看上面源码，只是使用`Log.e(TAG, "", leak)`输出异常，并没有`throw`往上抛。**所以`WindowLeaked`异常虽然是打印出来了，但并不会导致应用崩溃。**

这个方法的调用在：

```java
// ActivityThread.java
private void handleDestroyActivity(IBinder token, boolean finishing,
        int configChanges, boolean getNonConfigInstance) {

    // 触发 Activity:onDestroy()
    ActivityClientRecord r = performDestroyActivity(token, finishing,
            configChanges, getNonConfigInstance);

    if (r != null) {
        cleanUpPendingRemoveWindows(r, finishing);
        WindowManager wm = r.activity.getWindowManager();
        View v = r.activity.mDecor;
        if (v != null) {
            if (r.activity.mVisibleFromServer) {
                mNumVisibleActivities--;
            }
            IBinder wtoken = v.getWindowToken();
            if (r.activity.mWindowAdded) {
                if (r.mPreserveWindow) {
                    // Hold off on removing this until the new activity's
                    // window is being added.
                    r.mPendingRemoveWindow = r.window;
                    r.mPendingRemoveWindowManager = wm;
                    // We can only keep the part of the view hierarchy that we control,
                    // everything else must be removed, because it might not be able to
                    // behave properly when activity is relaunching.
                    r.window.clearContentView();
                } else {
                    wm.removeViewImmediate(v);
                }
            }
            if (wtoken != null && r.mPendingRemoveWindow == null) {
                WindowManagerGlobal.getInstance().closeAll(wtoken,
                        r.activity.getClass().getName(), "Activity");
            } else if (r.mPendingRemoveWindow != null) {
                // We're preserving only one window, others should be closed so app views
                // will be detached before the final tear down. It should be done now because
                // some components (e.g. WebView) rely on detach callbacks to perform receiver
                // unregister and other cleanup.
                WindowManagerGlobal.getInstance().closeAllExceptView(token, v,
                        r.activity.getClass().getName(), "Activity");
            }
            r.activity.mDecor = null;
        }
        if (r.mPendingRemoveWindow == null) {
            // 》》》警告出自这里～～～
            // If we are delaying the removal of the activity window, then
            // we can't clean up all windows here.  Note that we can't do
            // so later either, which means any windows that aren't closed
            // by the app will leak.  Well we try to warning them a lot
            // about leaking windows, because that is a bug, so if they are
            // using this recreate facility then they get to live with leaks.
            WindowManagerGlobal.getInstance().closeAll(token,
                    r.activity.getClass().getName(), "Activity");
        }

        // Mocked out contexts won't be participating in the normal
        // process lifecycle, but if we're running with a proper
        // ApplicationContext we need to have it tear down things
        // cleanly.
        Context c = r.activity.getBaseContext();
        if (c instanceof ContextImpl) {
            ((ContextImpl) c).scheduleFinalCleanup(
                    r.activity.getClass().getName(), "Activity");
        }
    }
    if (finishing) {
        try {
            ActivityManager.getService().activityDestroyed(token);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }
    mSomeActivitiesChanged = true;
}
```

以上：

1. 触发Activity:onDestroy()。
2. clean up清理，清理与当前Activity绑定的子窗口，如果有，清理并打印错误。但不会造成崩溃。

`ActivityClientRecord r = performDestroyActivity()`的`Activity:onDestroy()`分发流程，可以见另一篇《Activity onDestroy流程详解》，有从Activity到接收到BACK按键，到onDestroy回调的流程分析。
