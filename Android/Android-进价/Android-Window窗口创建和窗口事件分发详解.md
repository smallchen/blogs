Window窗口事件

PopupWindow源码

```java
public class PopupWindow {
    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }
}
```

context.getSystemService与ApplicationContext.getSystemService不一样。
**这里的context是Activity实例，看Activity::getSystemService()**

Activity源码

```java
public class Activity extends ContextThemeWrapper
        implements LayoutInflater.Factory2,
        Window.Callback, KeyEvent.Callback,
        OnCreateContextMenuListener, ComponentCallbacks2,
        Window.OnWindowDismissedCallback, WindowControllerCallback,
        AutofillManager.AutofillClient {
    private static final String TAG = "Activity";

    @Override
    public Object getSystemService(@ServiceName @NonNull String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException(
                    "System services not available to Activities before onCreate()");
        }

        ///=> 如果context是Activity，那么直接返回Activity自身的mWindowManager。

        if (WINDOW_SERVICE.equals(name)) {
            return mWindowManager;
        } else if (SEARCH_SERVICE.equals(name)) {
            ensureSearchManager();
            return mSearchManager;
        }
        return super.getSystemService(name);
    }

    final void attach(.........) {
        ///=> Activity绑定的Window是PhoneWindow。
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        mWindow.setWindowControllerCallback(this);

        ///=> Activity关注Window.Callback接口。即关注Window的dispatchTouchEvent，优先截获了Window的事件分发。
        mWindow.setCallback(this);

        ///=> PhoneWindow中的WindowManager是由Activity传递进去。Activity设置了WindowManager
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
        if (mParent != null) {
            mWindow.setContainer(mParent.getWindow());
        }

        mWindow.setOnWindowDismissedCallback(this);
        mWindow.getLayoutInflater().setPrivateFactory(this);

        ///=> Activity里的mWindowManager是PhoneWindow的WindowManager。
        ///=> Activity将WindowManager再次get出来。
        mWindowManager = mWindow.getWindowManager();
    }
}
```

```java
    @Override
    public void setContentView(int layoutResID) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }

    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            mDecor = generateDecor(-1);
            mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mDecor.setIsRootNamespace(true);
            if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
                mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
            }
        } else {
            mDecor.setWindow(this);
        }
    }

    protected DecorView generateDecor(int featureId) {
        // System process doesn't have application context and in that case we need to directly use
        // the context we have. Otherwise we want the application context, so we don't cling to the
        // activity.
        Context context;
        if (mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }
```

// Applicstion(ApplicationContext) -> Activity(ContextImpl) -> ContentView(DecorContext)

```java
public abstract class Window {
     /**
     * Set the window manager for use by this Window to, for example,
     * display panels.  This is <em>not</em> used for displaying the
     * Window itself -- that must be done by the client.
     *
     * @param wm The window manager for adding new windows.
     */
    public void setWindowManager(WindowManager wm, IBinder appToken, String appName,
            boolean hardwareAccelerated) {
        mAppToken = appToken;
        mAppName = appName;
        mHardwareAccelerated = hardwareAccelerated
                || SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
        if (wm == null) {
            wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        mWindowManager = ((WindowManagerImpl)wm).createLocalWindowManager(this);
    }


    /**
     * Return the window manager allowing this Window to display its own
     * windows.
     *
     * @return WindowManager The ViewManager.
     */
    public WindowManager getWindowManager() {
        return mWindowManager;
    }
}
```

1. Activity里的getSystemService直接返回mWindowManager。从Activity构造可见，这个WindowManager是PhoneWindow里的WindowManager。

2. Activity关注了PhoneWindow中的Window.Callback接口，所以Activity里的dispatchTouchEvent其实是Window.Callback接口里面的回调。

3. PhoneWindow里面的getWindowManager是`Window.java`里面提供的，直接返回。而`Window.java`里面的mWindowManager是外部设置进来的。对于Activity而言，先将WindowManager设置到PhoneWindow里面，然后从PhoneWindow里get出来保存。

4. 转了一圈，最后还是调用了context.getSystemService(Context.WINDOW_SERVICE)。但这个context实例是从外部传递入Activity，所以需要跟踪到Activity创建流程。

ActivityThread源码
/frameworks/base/core/java/android/app/ActivityThread.java

```java
public final class ActivityThread {
    private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
                ContextImpl appContext = createBaseContextForActivity(r);

                Activity activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);

                appContext.setOuterContext(activity);

                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);
    }

    private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {       
        /// 创建的时候，把PackageInfo传递到ContextImpl，而PacakgeInfo就包含了ApplicationContext。
        ContextImpl appContext = ContextImpl.createActivityContext(
                this, r.packageInfo, r.activityInfo, r.token, displayId, r.overrideConfig);

        final DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();

        return appContext;
    }
}

public class Activity extends ContextThemeWrapper {
    final void attach(.....) {
        attachBaseContext(context);
        mWindow = new PhoneWindow(this, window, activityConfigCallback);
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        newBase.setAutofillClient(this);
    }

}

public class ContextWrapper extends Context {
    Context mBase;

    protected void attachBaseContext(Context base) {
        if (mBase != null) {
            throw new IllegalStateException("Base context already set");
        }
        mBase = base;
    }

    @Override
    public Context getApplicationContext() {
        return mBase.getApplicationContext();
    }
}

```
Activity的创建流程是：
1. ActivityThread::performLaunchActivity()开始创建Activity。创建Activity前，需要先创建Activity的context。

2. 通过ContextImpl.createActivityContext静态方法，创建Activity的context，是一个ContextImpl实例。

3. 创建ContextImpl实例，需要穿入ApplicationContext和mainThead，ApplicationContext封装在ActivityClientRecord里面。所以，创建ContextImpl传递一个ActivityClientRecord足够了。ActivityClientRecord是一个非常重要的类，包含了当前进程几乎所有信息，比如：LoadedApk对象，token，ClassLoader等等。ActivityThread就是ContextImpl里面的mainThread。

4. ContextImpl创建完成，就可以通过它访问：ActivityThread／LoadedApk／ActivityInfo／token／其它大部分实例。

5. ContextImpl创建完成，意味着Activity的context创建完成。开始创建Activity。

6. mInstrumentation.newActivity()创建对应的Activity后，执行attach。

7. Activity在attach时才正式绑定context。这个context是Activity中的mBase，即是mBase就是ContextImpl。

8. Activity在attach时，创建PhoneWindow，传递到Window里的context是Activity自身，也就是PhoneWindow里的context是Activity context.

9. Activity里的context是ContextImpl，所以Activity里调用的getApplicationContext()是调用ContextImpl的getApplicationContext。Activity里调用getSystemService也是ContextImpl的getSystemService（如下）

8. 于是，使用Activity的context执行：context.getApplicationContext()是拿LoadedApk实例。context.getSystemService拿的是WindowManagerImpl实例。（都在ContextImpl可以看到相关代码）

9. 至此，ApplicationContext是LoadedApk实例里面的Application实例；ActivityContext是ContextImpl实例。

10. 然后，还有DecorContext；在PhoneWindow里面，调用setContentView(也即是Activity的setContentView)，会创建一个DecorContext实例传递给DecorView。这个DecorContex是DecorView实例里的context实例。换言之，DecorView‘s context是DecorContext实例。（见下面PhoneWindow源码）

```java
public class PhoneWindow extends Window implements MenuBuilder.Callback {
    public PhoneWindow(Context context) {
        super(context);
        mLayoutInflater = LayoutInflater.from(context);
    }
    @Override
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
    }
    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            mDecor = generateDecor(-1);
        } else {
            mDecor.setWindow(this);
        }
    }
    protected DecorView generateDecor(int featureId) {
        Context context;
        if (mUseDecorContext) {
            /// 如果applicationContext为null，则使用Window自身的context，也就是Activity context。
            /// 不为空，则创建DecorContext。
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }
}

public abstract class Window {
    public Window(Context context) {
        mContext = context;
        mFeatures = mLocalFeatures = getDefaultFeatures(context);
    }
    public final Context getContext() {
        return mContext;
    }
}
```

1. 还记得吧，传到PhoneWindow里的context是Activity context。

2. 从DecorView创建可知，如果applicationContext是null，使用Activity context创建DecorView；否则创意一个DecorContext来创建DecorView。

3. 所以，DecorView里面的context，可能是ApplicationContext，也可能是ActivityContext。具体看mUseDecorContext。

4. 不管怎样，我们知道了，Activity是context，base是ContextImpl实例；DecorContext是context，base是Application实例。


所以Activity里的context是`ContextImpl`.
/frameworks/base/core/java/android/app/

```java
class ContextImpl extends Context {
    final @NonNull ActivityThread mMainThread;
    final @NonNull LoadedApk mPackageInfo;

    @Override
    public Object getSystemService(String name) {
        return SystemServiceRegistry.getSystemService(this, name);
    }

    @Override
    public Context getApplicationContext() {
        return (mPackageInfo != null) ?
                mPackageInfo.getApplication() : mMainThread.getApplication();
    }

    static ContextImpl createActivityContext(ActivityThread mainThread,
            LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId,
            Configuration overrideConfiguration) {
        if (packageInfo == null) throw new IllegalArgumentException("packageInfo");

        String[] splitDirs = packageInfo.getSplitResDirs();
        ClassLoader classLoader = packageInfo.getClassLoader();

        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, activityInfo.splitName,
                activityToken, null, 0, classLoader);

        return context;
    }

    private ContextImpl(@Nullable ContextImpl container, @NonNull ActivityThread mainThread,
            @NonNull LoadedApk packageInfo, @Nullable String splitName,
            @Nullable IBinder activityToken, @Nullable UserHandle user, int flags,
            @Nullable ClassLoader classLoader) {
        mOuterContext = this;

        mMainThread = mainThread;
        mActivityToken = activityToken;
        mFlags = flags;

        if (user == null) {
            user = Process.myUserHandle();
        }
        mUser = user;

        /// PackageInfo是创建ContextImpl时传递进来的。
        mPackageInfo = packageInfo;

        mSplitName = splitName;
        mClassLoader = classLoader;
        mResourcesManager = ResourcesManager.getInstance();
    }
}

public final class LoadedApk {
    private final ActivityThread mActivityThread;
    final String mPackageName;
    private ApplicationInfo mApplicationInfo;
    private ClassLoader mClassLoader;
    private Application mApplication;

    Application getApplication() {
        return mApplication;
    }
}

final class SystemServiceRegistry {
    /**
     * Gets a system service from a given context.
     */
    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
        return fetcher != null ? fetcher.getService(ctx) : null;
    }


    // 静态初始化列表。所以，Context.WINDOW_SERVICE 对应new WindowManagerImpl(ctx)。
    static {
        registerService(Context.MIDI_SERVICE, MidiManager.class,
                    new CachedServiceFetcher<MidiManager>() {
                @Override
                public MidiManager createService(ContextImpl ctx) throws ServiceNotFoundException {
                    IBinder b = ServiceManager.getServiceOrThrow(Context.MIDI_SERVICE);
                    return new MidiManager(IMidiManager.Stub.asInterface(b));
                }});
        ///。。。。
        registerService(Context.WINDOW_SERVICE, WindowManager.class,
                new CachedServiceFetcher<WindowManager>() {
            @Override
            public WindowManager createService(ContextImpl ctx) {
                return new WindowManagerImpl(ctx);
            }});
    }
    private static <T> void registerService(String serviceName, Class<T> serviceClass,
            ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
    }
}

```

从上可以看到：
1. Activity里的context是ContextImpl。
2. Activity里的context.getSystemService(Context.WINDOW_SERVICE)得到的是WindowManagerImpl。



这里的context是DecorContext实例。
DecorContext源码
/frameworks/base/core/java/com/android/internal/policy/DecorContext.java

```java
class DecorContext extends ContextThemeWrapper {
    private PhoneWindow mPhoneWindow;
    private WindowManager mWindowManager;
    private Resources mActivityResources;

    public DecorContext(Context context, Resources activityResources) {
        super(context, null);
        mActivityResources = activityResources;
    }

    void setPhoneWindow(PhoneWindow phoneWindow) {
        mPhoneWindow = phoneWindow;
        mWindowManager = null;
    }

    @Override
    public Object getSystemService(String name) {
        if (Context.WINDOW_SERVICE.equals(name)) {
            if (mWindowManager == null) {
                WindowManagerImpl wm =
                        (WindowManagerImpl) super.getSystemService(Context.WINDOW_SERVICE);
                mWindowManager = wm.createLocalWindowManager(mPhoneWindow);
            }
            return mWindowManager;
        }
        return super.getSystemService(name);
    }

    @Override
    public Resources getResources() {
        return mActivityResources;
    }

    @Override
    public AssetManager getAssets() {
        return mActivityResources.getAssets();
    }
}
```


WindowManagerImpl源码
/frameworks/base/core/java/android/view/WindowManagerImpl.java

```java
public final class WindowManagerImpl implements WindowManager {
    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        return new WindowManagerImpl(mContext, parentWindow);
    }

    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        return new WindowManagerImpl(displayContext, mParentWindow);
    }
}
```
1. WindowManager只是一个接口。
2.

```java
/** Window flag: if you have set {@link #FLAG_NOT_TOUCH_MODAL}, you
 * can set this flag to receive a single special MotionEvent with
 * the action
 * {@link MotionEvent#ACTION_OUTSIDE MotionEvent.ACTION_OUTSIDE} for
 * touches that occur outside of your window.  Note that you will not
 * receive the full down/move/up gesture, only the location of the
 * first down as an ACTION_OUTSIDE.
 */
public static final int FLAG_WATCH_OUTSIDE_TOUCH = 0x00040000;
```
1. 只有Window的Flag设置了FLAG_WATCH_OUTSIDE_TOUCH，才能收到ACTION_OUTSIDE。
2. 一次点击事件，ACTION_OUTSIDE只有一次。也即是说，ACTION_OUTSIDE相当于是一个通知。

MotionEvent里对ACTION_OUTSIDE的描述

```java
/**
 * Constant for {@link #getActionMasked}: A movement has happened outside of the
 * normal bounds of the UI element.  This does not provide a full gesture,
 * but only the initial location of the movement/touch.
 * <p>
 * Note: Because the location of any event will be outside the
 * bounds of the view hierarchy, it will not get dispatched to
 * any children of a ViewGroup by default. Therefore,
 * movements with ACTION_OUTSIDE should be handled in either the
 * root {@link View} or in the appropriate {@link Window.Callback}
 * (e.g. {@link android.app.Activity} or {@link android.app.Dialog}).
 * </p>
 */
public static final int ACTION_OUTSIDE          = 4;
```
1. ACTION_OUTSIDE和ACTION_DOWN一样，是MotionEvent里的事件类型。
2. 由于ACTION_OUTSIDE发生在view树之外，所以默认情况下ACTION_OUTSIDE不会分发到子view。
3. ACTION_OUTSIDE只会分发到root view（常见的DecorView），或者窗口Window或者对话框Dialog的回调接口（Window.Callback接口）。

Window.Callback接口源码

```java
/**
 * API from a Window back to its caller.  This allows the client to
 * intercept key dispatching, panels and menus, etc.
 */
public interface Callback {
    /**
     * Called to process touch screen events.  At the very least your
     * implementation must call
     * {@link android.view.Window#superDispatchTouchEvent} to do the
     * standard touch screen processing.
     *
     * @param event The touch screen event.
     *
     * @return boolean Return true if this event was consumed.
     */
    public boolean dispatchTouchEvent(MotionEvent event);

    /**
     * Called to process trackball events.  At the very least your
     * implementation must call
     * {@link android.view.Window#superDispatchTrackballEvent} to do the
     * standard trackball processing.
     *
     * @param event The trackball event.
     *
     * @return boolean Return true if this event was consumed.
     */
    public boolean dispatchTrackballEvent(MotionEvent event);

    /**
     * Called to process generic motion events.  At the very least your
     * implementation must call
     * {@link android.view.Window#superDispatchGenericMotionEvent} to do the
     * standard processing.
     *
     * @param event The generic motion event.
     *
     * @return boolean Return true if this event was consumed.
     */
    public boolean dispatchGenericMotionEvent(MotionEvent event);

    /**
     * Called to process population of {@link AccessibilityEvent}s.
     *
     * @param event The event.
     *
     * @return boolean Return true if event population was completed.
     */
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);


    /**
     * Called when pointer capture is enabled or disabled for the current window.
     *
     * @param hasCapture True if the window has pointer capture.
     */
    default public void onPointerCaptureChanged(boolean hasCapture) { };
}

```

事件分发分为：
1. 将事件分发到窗口，叫窗口事件分发。比如Activity／PopupWindow／Dialog等等窗口。Activity是一个包含了窗口的特殊窗口。
2. 将事件分发到View，叫View事件分发。比如ViewGroup／View。

窗口之间的事件分发由WindowManager决定，通常互不影响。除非设置了事件透传。默认，一个事件会被传递到多个窗口。
View之间的事件分发，就是一般讨论的布局事件分发。默认，一个事件只会被一个View处理。


WindowManagerImpl源码
/frameworks/base/core/java/android/view/WindowManagerImpl.java

```java
public final class WindowManagerImpl implements WindowManager {
      private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();

    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
}
```
1. WindowManager只是一个接口。


WindowManagerGlobal源码
/frameworks/base/core/java/android/view/WindowManagerGlobal.java

```java
public final class WindowManagerGlobal {
    private static final String TAG = "WindowManager";

      public void addView(View view, ViewGroup.LayoutParams params,
              Display display, Window parentWindow) {

                   /// 对于PopupWindow这种SUB_WINDOW类型，是支持一个窗口弹出多个PopupWindow的。所以需要遍历。

               // If this is a panel window, then find the window it is being
            // attached to for future reference.
            if (wparams.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                    wparams.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                final int count = mViews.size();
                for (int i = 0; i < count; i++) {
                    if (mRoots.get(i).mWindow.asBinder() == wparams.token) {
                        panelParentView = mViews.get(i);
                    }
                }
            }

             ViewRootImpl root;
             root = new ViewRootImpl(view.getContext(), display);

            // do this last because it fires off messages to start doing things
            try {
                /// 将View添加到ViewRootImpl，并且将LayoutParams里面的flags传递给ViewRootImpl里面的mWindowAttributes。
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
                // BadTokenException or InvalidDisplayException, clean up.
                if (index >= 0) {
                    removeViewLocked(index, true);
                }
                throw e;
            }
    }
}
```


ViewRootImpl源码
/frameworks/base/core/java/android/view/ViewRootImpl.java

```java
public final class ViewRootImpl implements ViewParent,
        View.AttachInfo.Callbacks, ThreadedRenderer.DrawCallbacks {
    private static final String TAG = "ViewRootImpl";

    View mView;
    final View.AttachInfo mAttachInfo;

    public ViewRootImpl(Context context, Display display) {
        mContext = context;
        mWindowSession = WindowManagerGlobal.getWindowSession();
        mDisplay = display;
        mBasePackageName = context.getBasePackageName();
        mWindow = new W(this);

        mAttachInfo = new View.AttachInfo(mWindowSession, mWindow, display, this, mHandler, this, context);
    }

     /**
     * We have one child
     */
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        /// 只绑定一次，且绑定一个View。
        synchronized (this) {
            if (mView == null) {

                /// attrs即是WindowManager::addView传入的LayoutParams，这里将其当作attrs使用。
                  mWindowAttributes.copyFrom(attrs);
                mAttachInfo.mDisplayState = mDisplay.getState();
                mDisplayManager.registerDisplayListener(mDisplayListener, mHandler);

                /// 重点：从attrs复制一份到本地保存为mWindowAttributes。然后通过getWindowFlags来获取Flags。
                mWindowAttributes.copyFrom(attrs);

                attrs = mWindowAttributes;

                /// attrs.flags即是PopupWindow设置的一系列Flags。通过mClientWindowLayoutFlags保存。
                // Keep track of the actual window flags supplied by the client.
                mClientWindowLayoutFlags = attrs.flags;

                setAccessibilityFocus(null, null);

                mView = view;
                mAttachInfo.mRootView = view;



                // Schedule the first layout -before- adding to the window
                // manager, to make sure we do the relayout before receiving
                // any other events from the system.
                requestLayout();

                /// 事件接收模块（从InputChannel()那里接收事件）

                  if ((mWindowAttributes.inputFeatures
                        & WindowManager.LayoutParams.INPUT_FEATURE_NO_INPUT_CHANNEL) == 0) {
                    mInputChannel = new InputChannel();
                }
                if (mInputChannel != null) {

                    mInputEventReceiver = new WindowInputEventReceiver(mInputChannel,
                            Looper.myLooper());
                }


                // Set up the input pipeline.
                /// 事件处理模块
                CharSequence counterSuffix = attrs.getTitle();
                mSyntheticInputStage = new SyntheticInputStage();
                InputStage viewPostImeStage = new ViewPostImeInputStage(mSyntheticInputStage);
                InputStage nativePostImeStage = new NativePostImeInputStage(viewPostImeStage,
                        "aq:native-post-ime:" + counterSuffix);
                InputStage earlyPostImeStage = new EarlyPostImeInputStage(nativePostImeStage);
                InputStage imeStage = new ImeInputStage(earlyPostImeStage,
                        "aq:ime:" + counterSuffix);
                InputStage viewPreImeStage = new ViewPreImeInputStage(imeStage);
                InputStage nativePreImeStage = new NativePreImeInputStage(viewPreImeStage,
                        "aq:native-pre-ime:" + counterSuffix);
            }
        }
    }


    public int getWindowFlags() {
        return mWindowAttributes.flags;
    }


    WindowInputEventReceiver mInputEventReceiver;
    final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override
        public void onInputEvent(InputEvent event) {
            enqueueInputEvent(event, this, 0, true); // 源码在下面👇
        }

        @Override
        public void onBatchedInputEventPending() {
            if (mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                scheduleConsumeBatchedInput();
            }
        }

        @Override
        public void dispose() {
            unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

    // enqueueInputEvent
    void enqueueInputEvent(InputEvent event,
            InputEventReceiver receiver, int flags, boolean processImmediately) {
        adjustInputEventForCompatibility(event);
        QueuedInputEvent q = obtainQueuedInputEvent(event, receiver, flags);

        // Always enqueue the input event in order, regardless of its time stamp.
        // We do this because the application or the IME may inject key events
        // in response to touch events and we want to ensure that the injected keys
        // are processed in the order they were received and we cannot trust that
        // the time stamp of injected events are monotonic.
        QueuedInputEvent last = mPendingInputEventTail;
        if (last == null) {
            mPendingInputEventHead = q;
            mPendingInputEventTail = q;
        } else {
            last.mNext = q;
            mPendingInputEventTail = q;
        }
        mPendingInputEventCount += 1;
        Trace.traceCounter(Trace.TRACE_TAG_INPUT, mPendingInputEventQueueLengthCounterName,
                mPendingInputEventCount);

        if (processImmediately) {
            doProcessInputEvents();    // 源码在下面
        } else {
            scheduleProcessInputEvents();
        }
    }

    // doProcessInputEvents
    void doProcessInputEvents() {
        // Deliver all pending input events in the queue.
        while (mPendingInputEventHead != null) {
            QueuedInputEvent q = mPendingInputEventHead;
            mPendingInputEventHead = q.mNext;
            if (mPendingInputEventHead == null) {
                mPendingInputEventTail = null;
            }
            q.mNext = null;

            mPendingInputEventCount -= 1;
            Trace.traceCounter(Trace.TRACE_TAG_INPUT, mPendingInputEventQueueLengthCounterName,
                    mPendingInputEventCount);

            long eventTime = q.mEvent.getEventTimeNano();
            long oldestEventTime = eventTime;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent me = (MotionEvent)q.mEvent;
                if (me.getHistorySize() > 0) {
                    oldestEventTime = me.getHistoricalEventTimeNano(0);
                }
            }
            mChoreographer.mFrameInfo.updateInputEventTime(eventTime, oldestEventTime);

            ///
            deliverInputEvent(q);
        }

        // We are done processing all input events that we can process right now
        // so we can clear the pending flag immediately.
        if (mProcessInputEventsScheduled) {
            mProcessInputEventsScheduled = false;
            mHandler.removeMessages(MSG_PROCESS_INPUT_EVENTS);
        }
    }

    // deliverInputEvent
    private void deliverInputEvent(QueuedInputEvent q) {
        Trace.asyncTraceBegin(Trace.TRACE_TAG_VIEW, "deliverInputEvent",
                q.mEvent.getSequenceNumber());
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }

        // 根据不同的输入设备，分发到不同的处理方法，就是上面##事件处理模块##
        InputStage stage;
        if (q.shouldSendToSynthesizer()) {
            stage = mSyntheticInputStage;
        } else {
            stage = q.shouldSkipIme() ? mFirstPostImeInputStage : mFirstInputStage;
        }

        if (stage != null) {
            stage.deliver(q);
        } else {
            finishInputEvent(q);
        }
    }

    // InputStage
    /**
    /**
     * Delivers post-ime input events to the view hierarchy.
     */
    final class ViewPostImeInputStage extends InputStage {
        public ViewPostImeInputStage(InputStage next) {
            super(next);
        }

        @Override
        protected int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {

                /// processKeyEvent
                return processKeyEvent(q);
            } else {
                final int source = q.mEvent.getSource();
                if ((source & InputDevice.SOURCE_CLASS_POINTER) != 0) {

                    /// processPointerEvent
                    return processPointerEvent(q);
                } else if ((source & InputDevice.SOURCE_CLASS_TRACKBALL) != 0) {

                    /// processTrackballEvent
                    return processTrackballEvent(q);
                } else {
                    return processGenericMotionEvent(q);
                }
            }
        }

        private int processPointerEvent(QueuedInputEvent q) {
            final MotionEvent event = (MotionEvent)q.mEvent;

            mAttachInfo.mUnbufferedDispatchRequested = false;
            mAttachInfo.mHandlingPointerEvent = true;

            /// 事件分发
            boolean handled = mView.dispatchPointerEvent(event);

            maybeUpdatePointerIcon(event);
            maybeUpdateTooltip(event);
            mAttachInfo.mHandlingPointerEvent = false;
            if (mAttachInfo.mUnbufferedDispatchRequested && !mUnbufferedInputDispatch) {
                mUnbufferedInputDispatch = true;
                if (mConsumeBatchedInputScheduled) {
                    scheduleConsumeBatchedInputImmediately();
                }
            }
            return handled ? FINISH_HANDLED : FORWARD;
        }

    }
}
```

1. ViewRootImpl只会绑定一次，且只绑定一个View，通过setView设置。
2. ViewRootImpl会把View也设置到mAttachInfo。
3. 事件通过processPointerEvent(QueuedInputEvent q)，分发到与ViewRootImpl绑定到View（其实就是root view，或，decorView）。


InputEventReceiver源码
```java
/**
 * Provides a low-level mechanism for an application to receive input events.
 * @hide
 */
public abstract class InputEventReceiver {
    // Called from native code.
    @SuppressWarnings("unused")
    private void dispatchInputEvent(int seq, InputEvent event) {
        mSeqMap.put(event.getSequenceNumber(), seq);
        onInputEvent(event);
    }
}
```

1. 从native层调用dispatchInputEvent，分发输入事件。
2. WindowInputEventReceiver继承自InputEventReceiver，在ViewRootImpl中负责分发输入事件。




InputDispatcher源码
/frameworks/native/services/inputflinger/InputDispatcher.cpp

```java
sp<InputWindowHandle> InputDispatcher::findTouchedWindowAtLocked(int32_t displayId,
        int32_t x, int32_t y) {
    // Traverse windows from front to back to find touched window.
    size_t numWindows = mWindowHandles.size();
    for (size_t i = 0; i < numWindows; i++) {
        sp<InputWindowHandle> windowHandle = mWindowHandles.itemAt(i);
        const InputWindowInfo* windowInfo = windowHandle->getInfo();
        if (windowInfo->displayId == displayId) {
            int32_t flags = windowInfo->layoutParamsFlags;

            if (windowInfo->visible) {
                if (!(flags & InputWindowInfo::FLAG_NOT_TOUCHABLE)) {
                    bool isTouchModal = (flags & (InputWindowInfo::FLAG_NOT_FOCUSABLE
                            | InputWindowInfo::FLAG_NOT_TOUCH_MODAL)) == 0;
                    if (isTouchModal || windowInfo->touchableRegionContainsPoint(x, y)) {
                        // Found window.
                        return windowHandle;
                    }
                }
            }
        }
    }
    return NULL;
}

```


Window触摸事件过滤
DisplayContent源码
/frameworks/base/services/core/java/com/android/server/wm/DisplayContent.java
/frameworks/base/services/core/java/com/android/server/wm/WindowContainer.java

```java
class DisplayContent extends WindowContainer<DisplayContent.DisplayChildWindowContainer> {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "DisplayContent" : TAG_WM;

    /// 找到对应的点下，能够接收触摸事件的Window的WindowState，WindowState包含了Window的Flags。

    /** Find the visible, touch-deliverable window under the given point */
    WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        final int x = (int) xf;
        final int y = (int) yf;
        final WindowState touchedWin = getWindow(w -> {
            final int flags = w.mAttrs.flags;
            if (!w.isVisibleLw()) {
                return false;
            }
            if ((flags & FLAG_NOT_TOUCHABLE) != 0) {
                return false;
            }

            w.getVisibleBounds(mTmpRect);
            if (!mTmpRect.contains(x, y)) {
                return false;
            }

            w.getTouchableRegion(mTmpRegion);

            final int touchFlags = flags & (FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL);
            return mTmpRegion.contains(x, y) || touchFlags == 0;
        });

        return touchedWin;
    }
}

getWindow()是父类提供的。
class WindowContainer<E extends WindowContainer> implements Comparable<WindowContainer> {
    protected final WindowList<E> mChildren = new WindowList<E>();

    WindowState getWindow(Predicate<WindowState> callback) {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final WindowState w = mChildren.get(i).getWindow(callback);
            if (w != null) {
                return w;
            }
        }
        return null;
    }
}

class WindowList<E> extends ArrayList<E> {
    void addFirst(E e) {
        add(0, e);
    }

    E peekLast() {
        return size() > 0 ? get(size() - 1) : null;
    }

    E peekFirst() {
        return size() > 0 ? get(0) : null;
    }
}
```

1. 如果flags设置了FLAG_NOT_TOUCHABLE，返回false，表示不是当前遍历的这个Window。
2. WindowState存储了窗口的状态。
