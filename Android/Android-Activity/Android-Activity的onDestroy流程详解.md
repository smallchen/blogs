<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android Activity onDestroy流程详解](#android-activity-ondestroy流程详解)
- [总结](#总结)

<!-- /TOC -->

## Android Activity onDestroy流程详解

具体见另一篇《removeView与removeViewImmediate》。

当时提到了`performDestroyActivity`，顺便理一下`destroy`分发流程。

```java
@ Activity:onKeyUp()
- onKeyUp(int keyCode, KeyEvent event)
    - KeyEvent.KEYCODE_BACK ?
    T: onBackPressed();
        - finishAfterTransition();
            - finish();
            @ Activity:finish()

@ Activity:finish()
- ActivityManager.getService().finishActivity(mToken, resultCode, resultData, finishTask))
@ ActivityManagerService:finishActivity()
    - tr.getStack().requestFinishActivityLocked(token, resultCode,resultData, "app-request", true);
    @ ActivityStack:requestFinishActivityLocked()
        - finishActivityLocked()
            - finishCurrentActivityLocked()
                - destroyActivityLocked()
                    // ActivityRecord.ProcessRecord.ActivityThread。
                    - r.app.thread.scheduleDestroyActivity()
                    @ ActivityThread:scheduleDestroyActivity()

@ ActivityThread:scheduleDestroyActivity()
- sendMessage(H.DESTROY_ACTIVITY);
-
- void handleMessage(Message msg):
    - DESTROY_ACTIVITY ?
    T: handleDestroyActivity()
        - performDestroyActivity()
            - mInstrumentation.callActivityOnDestroy
            @ Instrumentation:callActivityOnDestroy()
                - activity.performDestroy();
                @ Activity:performDestroy()
                    - onDestroy();
                        - getApplication().dispatchActivityDestroyed(this);
                        @ Application:dispatchActivityDestroyed()

@ Application:dispatchActivityDestroyed()
- for callbacks:
    - callbacks[i].onActivityDestroyed(activity);
    @ ActivityLifecycleCallbacks:onActivityDestroyed()
```

以上就是`BACK`按键，到`Activity:onDestroy()`的执行流程，
并且`Activity:onDestroy()`后，会通知`Application`的`ActivityLifecycleCallbacks`当前Activity已销毁。
Application中`ActivityLifecycleCallbacks`全局生命周期回调是这样工作的。

下面是源代码版本。

```java
Activity.java (这个不是用于释放的，用于后台Activity自毁，当回到前台，会恢复。所以Back键不是这个入口)
// Activity.java
public boolean releaseInstance() {
    try {
        return ActivityManager.getService().releaseActivityInstance(mToken);
    } catch (RemoteException e) {
        // Empty
    }
    return false;
}

当按下BACK键，Activity接收到事件，调用finish()进行自我销毁。
///////////////////////////////////////////////////////////////
// Activity.java
public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (getApplicationInfo().targetSdkVersion
            >= Build.VERSION_CODES.ECLAIR) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            onBackPressed();
            return true;
        }
    }
    return false;
}
public void onBackPressed() {
    if (mActionBar != null && mActionBar.collapseActionView()) {
        return;
    }

    FragmentManager fragmentManager = mFragments.getFragmentManager();

    if (fragmentManager.isStateSaved() || !fragmentManager.popBackStackImmediate()) {
        finishAfterTransition();
    }
}
public void finishAfterTransition() {
    if (!mActivityTransitionState.startExitBackTransition(this)) {
        finish();
    }
}
// Activity.java
private void finish(int finishTask) {
    if (mParent == null) {
        try {
            if (resultData != null) {
                resultData.prepareToLeaveProcess(this);
            }
            // 从Activity的finish()开始销毁。
            // ActivityManager.getService().finishActivity()
            if (ActivityManager.getService()
                    .finishActivity(mToken, resultCode, resultData, finishTask)) {
                mFinished = true;
            }
        } catch (RemoteException e) {
            // Empty
        }
    } else {
        mParent.finishFromChild(this);
    }
}
////////////////////////////////////////////////////////

然后调用到：ActivityManager.getService().finishActivity()
即：ActivityManagerService:finishActivity()
///////////////////////////////////////////////////

// ActivityManagerService.java
@Override
public final boolean finishActivity(IBinder token, int resultCode, Intent resultData,
        int finishTask) {
    res = tr.getStack().requestFinishActivityLocked(token, resultCode,resultData, "app-request", true);
    if (!res) {
        Slog.i(TAG, "Failed to finish by app-request");
    }
}

// ActivityStack.java
final boolean requestFinishActivityLocked(IBinder token, int resultCode,
        Intent resultData, String reason, boolean oomAdj) {
    ActivityRecord r = isInStackLocked(token);
    if (DEBUG_RESULTS || DEBUG_STATES) Slog.v(TAG_STATES,
            "Finishing activity token=" + token + " r="
            + ", result=" + resultCode + ", data=" + resultData
            + ", reason=" + reason);
    if (r == null) {
        return false;
    }

    finishActivityLocked(r, resultCode, resultData, reason, oomAdj);
    return true;
}

// ActivityStack.java
final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData,
         String reason, boolean oomAdj, boolean pauseImmediately) {
             final boolean removedActivity = finishCurrentActivityLocked(r, finishMode, oomAdj)
                        == null;
}

// ActivityStack.java
final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj) {
    boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm");
}

// ActivityStack.java
final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
    r.app.thread.scheduleDestroyActivity(r.appToken, r.finishing,
           r.configChangeFlags);
}
// ActivityThread.java
public final void scheduleDestroyActivity(IBinder token, boolean finishing,
        int configChanges) {
    sendMessage(H.DESTROY_ACTIVITY, token, finishing ? 1 : 0,
            configChanges);
}

// ActivityThread.java
public void handleMessage(Message msg) {
    case DESTROY_ACTIVITY:
        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityDestroy");
        handleDestroyActivity((IBinder)msg.obj, msg.arg1 != 0,
                msg.arg2, false);
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        break;
}

// ActivityThread.java
private void handleDestroyActivity(IBinder token, boolean finishing,
        int configChanges, boolean getNonConfigInstance) {
    // 触发 Activity:onDestroy()
    ActivityClientRecord r = performDestroyActivity(token, finishing,
            configChanges, getNonConfigInstance);
}

// ActivityThread.java
private ActivityClientRecord performDestroyActivity(IBinder token, boolean finishing,int configChanges, boolean getNonConfigInstance) {
    mInstrumentation.callActivityOnDestroy(r.activity);
}

// Instrumentation.java
public void callActivityOnDestroy(Activity activity) {
  activity.performDestroy();
}

// Activity.java
final void performDestroy() {
    mDestroyed = true;
    mWindow.destroy();
    mFragments.dispatchDestroy();
    onDestroy();
    mFragments.doLoaderDestroy();
    if (mVoiceInteractor != null) {
        mVoiceInteractor.detachActivity();
    }
}

@CallSuper
protected void onDestroy() {
    // dismiss any dialogs we are managing.
    // 关闭对话框

    // close any cursors we are managing.
    // 关闭所有cursors

    // Close any open search dialog
    // 关闭搜索对话框和ActionBar。
    if (mSearchManager != null) {
        mSearchManager.stopSearch();
    }
    if (mActionBar != null) {
        mActionBar.onDestroy();
    }
    // 分发ActivityDestroyed消息
    getApplication().dispatchActivityDestroyed(this);
}

// Application.java
void dispatchActivityDestroyed(Activity activity) {
    Object[] callbacks = collectActivityLifecycleCallbacks();
    if (callbacks != null) {
        for (int i=0; i<callbacks.length; i++) {
            ((ActivityLifecycleCallbacks)callbacks[i]).onActivityDestroyed(activity);
        }
    }
}
```

## 总结

1. Activity的销毁，从Activity.finish()开始。

2. 流程为：`Activity` - `ActivityManagerService` - `ActivityStack` - `ActivityThread` - `Instrumentation` - 回到`Activity` - 通知`Application`生命周期回调。

2. 如果Activity在后台被销毁，回到前台时，ActivateRecord会自动恢复。
