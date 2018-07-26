## Android Context

基于Android 8.1.0源码。

基类源码
/frameworks/base/core/java/android/content/Context.java
/frameworks/base/core/java/android/content/ContextWrapper.java
/frameworks/base/core/java/android/view/ContextThemeWrapper.java

```java
public abstract class Context {
	// APP基本信息接口
	public abstract Display getDisplay();
	public abstract Context getApplicationContext();
	public abstract ClassLoader getClassLoader();
	public abstract ApplicationInfo getApplicationInfo();
	public IBinder getActivityToken() {};

	// APP四大组件接口
	public abstract void startActivity(@RequiresPermission Intent intent);
	public abstract ComponentName startService(Intent service);
	public abstract boolean bindService(@RequiresPermission Intent service, @NonNull ServiceConnection conn, @BindServiceFlags int flags);
    public abstract Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter);
    public abstract void sendBroadcast(Intent intent, String receiverPermission, int appOp);

	// APP基本信息创建接口
    public abstract Context createApplicationContext(ApplicationInfo application,
            @CreatePackageOptions int flags) throws PackageManager.NameNotFoundException;
    public abstract Context createDisplayContext(@NonNull Display display);

    // 一些文件操作接口，比如 Data Dir / ExternalFilesDir／SharedReference 等等
    public abstract FileInputStream openFileInput(String name) throws FileNotFoundException;
    public abstract boolean deleteFile(String name);
    public abstract File getDataDir();
    public abstract File getExternalFilesDir(@Nullable String type);

    // 一些Intent Uri权限校验接口
    public abstract int checkUriPermission(Uri uri, int pid, int uid, @Intent.AccessUriMode int modeFlags, IBinder callerToken);
}
```

Context是一个很重要的基类，正如上面所看到的，提供了与APP相关的非常重要的接口。还提供了Android里四大组件的调用接口。

```java
public class ContextWrapper extends Context {
    Context mBase;

    public ContextWrapper(Context base) {
        mBase = base;
    }

    protected void attachBaseContext(Context base) {
        if (mBase != null) {
            throw new IllegalStateException("Base context already set");
        }
        mBase = base;
    }

    public Context getBaseContext() {
        return mBase;
    }

    @Override
    public Context getApplicationContext() {
        return mBase.getApplicationContext();
    }

    // ......重写Context所有接口
}
```

ContextWrapper很简单，就提供了一个mBase属性。可以通过构造器ContextWrapper(Context base)和attachBaseContext(Context base)来设置或修改mBase这个属性。剩下的所有方法和接口，都是直接调用mBase这个context实例来提供的。名称中Wrapper已经说明了一切，只是对mBase进行了包装，ContextWrapper真正调用的是mBase这个Context实例。

有两点：
1. mBase是核心；
2. mBase可以有两个设置入口，一是构造方法，二是attach。

```java
public class ContextThemeWrapper extends ContextWrapper {
	private int mThemeResource;
    private Resources.Theme mTheme;
    private LayoutInflater mInflater;
    private Configuration mOverrideConfiguration;
    private Resources mResources;

    public ContextThemeWrapper() {
        super(null);
    }
    public ContextThemeWrapper(Context base, @StyleRes int themeResId) {
        super(base);
        mThemeResource = themeResId;
    }
    public ContextThemeWrapper(Context base, Resources.Theme theme) {
        super(base);
        mTheme = theme;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
}
```
ContextThemeWrapper也很简单，只有几个属性，都是和Theme／Resources／Configuration这些与Theme相关的属性，所以ThemeWrapper也很形象，是对Theme的包装，ContextThemeWrapper中与Context相关操作，都是父类ContextWrapper提供的，最后也是调用到mBase这个Context实例的接口。

以上三个Context类总结：

1. 继承关系: Context <- ContextWrapper <- ContextThemeWrapper
2. 从包路径可以看出，ContextThemeWrapper已经是更高层次了。ContextWrapper和Context才是最底层。
3. Context是一个抽象类，主要是起接口作用。真正的实现是更上层的类。
4. 正如其名，ContextWrapper是对Context实例的Wrapper封装。**最最核心的是，ContextWrapper里面的mBase实例**。
5. ContextWrapper并不提供实质的服务，它只是调用mBase的方法来提供服务。所以严格上不算是Context接口的实现。


继承于Context还有：
/frameworks/base/core/java/android/app/ContextImpl.java
/frameworks/base/core/java/com/android/internal/policy/DecorContext.java

```java
class ContextImpl extends Context {
    static ContextImpl createActivityContext(ActivityThread mainThread,
            LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId,
            Configuration overrideConfiguration)

    @Override
    public Context createApplicationContext(ApplicationInfo application, int flags)
            throws NameNotFoundException {
    }

    @Override
    public Context createPackageContext(String packageName, int flags)
            throws NameNotFoundException {
        return createPackageContextAsUser(packageName, flags,
                mUser != null ? mUser : Process.myUserHandle());
    }

    /// 创建AppContext！！！！
    static ContextImpl createAppContext(ActivityThread mainThread, LoadedApk packageInfo) {
        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, null, null, null, 0, null);
        ...
        return context;
    }

    /// 创建ActivityContext！！！！
    static ContextImpl createActivityContext(ActivityThread mainThread,
            LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId, Configuration overrideConfiguration) {

        /// 对于Activity，ContextImpl container参数是null

        ContextImpl context = new ContextImpl(null, mainThread, packageInfo, activityInfo.splitName, activityToken, null, 0, classLoader);
        ...
        return context;
    }

    private ContextImpl(@Nullable ContextImpl container, @NonNull ActivityThread mainThread,
            @NonNull LoadedApk packageInfo, @Nullable String splitName,
            @Nullable IBinder activityToken, @Nullable UserHandle user, int flags,
            @Nullable ClassLoader classLoader) {
    }

    final void setOuterContext(Context context) {
        mOuterContext = context;
    }
    // .... 省略了很多代码
}
```

ContextImpl实现了Context定义的所有方法，**是Context最核心的唯一的那个实现**。
提供了创建ActivityContext接口。
提供了创建AppContext接口。
提供了创建SystemContext接口。
提供了创建SystemUiContext接口。
提供了创建PackageContext接口。
。。。等等
这也意味着，以上这些context创建接口得到的都是ContextImpl实例。
*ContextImpl还有一个setOuterContext接口。下面会说到*。

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

DecorContext很简单，就全部源码都放上来。只提供一个构造方法。
DecorContext持有了PhoneWindow和自己的WindowManager。
它重写了getSystemService，使得获取WindowManager时，不是返回WindowManagerImpl，而是返回一个WindowManagerImpl::createLocalWindowManager。可以说是它自己的WindowManager（这里不深入，**只是需要知道，不同的Context实例，getSystemService(Context.WINDOW_SERVICE)得到的WindowManager是有些许区别的**）。


简单总结一下：
Android里面Context相关的类就这么几个。
/frameworks/base/core/java/android/content/Context.java
/frameworks/base/core/java/android/content/ContextWrapper.java
/frameworks/base/core/java/android/view/ContextThemeWrapper.java
/frameworks/base/core/java/android/app/ContextImpl.java
/frameworks/base/core/java/com/android/internal/policy/DecorContext.java

继承关系是:

```
Context <- ContextWrapper <- ContextThemeWrapper <- DecorContext
      \
       ContextImpl
```

这些是比较底层的，应用层不会直接接触到的Context。在应用层，接触到的基本都是Application和Activity这两种Context。

### Application与Activity Context

/frameworks/base/core/java/android/app/Application.java
/frameworks/base/core/java/android/app/Activity.java

```java
public class Application extends ContextWrapper {
	public LoadedApk mLoadedApk;

	public Application() {
        super(null);
    }

    @CallSuper
    public void onCreate() {
    }

    @CallSuper
    public void onTerminate() {
    }

    final void attach(Context context) {
        attachBaseContext(context);
        mLoadedApk = ContextImpl.getImpl(context).mPackageInfo;
    }
}
```

Application也很简单，持有LoadedApk属性，这个属性封装了APP安装信息，比如包名／路径等等，通常变量名是mPackageInfo。

Application的构造方法，调用了super(null)，也就是ContextWrapper(Context base)中传递了null参数，所以，Application构造时mBase是null。但ContextWrapper还有另一个attachBaseContext(Context base)入口可以提供修改mBase的机会。（这里的构造方法有点多余。默认就是这样的构造器。）

通过`getApplication().getBaseContext()`打印可以发现，mBase并不是null，所以可以推测，Application构造时，mBase为空，但在attach时绑定了另一个ContextImpl。所以可以肯定，Application.attach才是Application真正用来绑定Context的入口。

所以要跟踪Application包装的那个mBase实例，需要跟踪到Application的创建。Application在ActivityThread里面创建（下面会说到）。


```java
public class Activity extends ContextThemeWrapper {
	// Activity没有提供构造器！

	final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            NonConfigurationInstances lastNonConfigurationInstances,
            Configuration config, String referrer, IVoiceInteractor voiceInteractor,
            Window window, ActivityConfigCallback activityConfigCallback) {

        attachBaseContext(context);
    }
}
```

Activity也没有提供构造器，但同样，它也是在attach中通过attachBaseContext(Context base)来设置mBase。
所以要跟踪Activity包装的那个的mBase实例，同样需要跟踪到Activity的创建。Activity也是在ActivityThread里面创建（下面会说到）。

**综上**，Application和Activity是很相似的，都是在attach方法对context进行绑定。虽然他们都是Context对象，但真正提供Context服务的，是与他们绑定的那个Context mBase实例。

### 各类Context的总结。

=======================
简单总结一下，所有Context的继承关系是:

```
Context <- ContextWrapper <- ContextThemeWrapper <- DecorContext
      \              \                    \
       ContextImpl  Application          Activity
```

1. 对比下扩展的属性。

```java
Context 			{ 接口而已 }

ContextWrapper      { Context mBase; }

Application         { Context mBase; LoadedApk mLoadedApk; }

ContextThemeWrapper { Context mBase; Resources.Theme mTheme; Resources mResources; }

DecorContex         { Context mBase; PhoneWindow mPhoneWindow; WindowManager mWindowManager; }

Activity            { Context mBase; Application mApplication; 等等，扩展得非常复杂 }

ContextImpl         { 没有Context mBase，但有Context mOuterContext; 实现了所有Context接口，提供了创建AppContext／ActivityContext等等 }
```
2. Application/Activity/DecorContext都是ContextWrapper，核心是mBase。

3. ContextWrapper继承于Context，又包含一个Context，意味着ContextWrapper有两个Context实例。所以，Application／Activity等，自身是一个context实例，又是对另一个核心context实例的封装。

4. ContextImpl通常是被封装的那个核心context。而ContextImpl里的outerContext属性存储了作为容器的context。

> 为了方便，下面将对Application／Activity等继承于ContextWrapper的对象不认为是Context，所说的Application Context／Activity Context／Decor Context 指的是它们封装的，那个核心的，负责提供服务的mBase context对象。


### 各类Context的创建时机

```
Context <- ContextWrapper <- ContextThemeWrapper <- DecorContext
      \              \                    \
       ContextImpl  Application          Activity
```

Context <- ContextWrapper <- ContextThemeWrapper 是对象封装，是结构层级，不会实例化；**真正会实例化的，是ContextImpl／DecorContext／Application／Activity这四类Context**。

#### Activity Context的创建。

ActivityThread源码
/frameworks/base/core/java/android/app/ActivityThread.java

```java
public final class ActivityThread {
	/// 打开App
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
		/// 创建的时候，把PackageInfo（即LoadedApk）传递到ContextImpl
        ContextImpl appContext = ContextImpl.createActivityContext(
                this, r.packageInfo, r.activityInfo, r.token, displayId, r.overrideConfig);

        final DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();

        return appContext;
    }
}
```

可见，Activity的context，是通过ContextImpl的静态方法createActivityContext()来创建的。
1. 使用ContextImpl.createActivityContext创建一个ContextImpl实例（变量appContext）。
2. 使用Instrumentation.newActivity创建一个Activity实例（变量activity）。
3. 调用appContext.setOuterContext(activity)，绑定Activity实例到ContextImpl实例中的mOuterContext。
4. 调用activity.attach(appContext)，绑定ContextImpl实例到Activity实例中的mBase。

所以，Activity和ContextImpl是相互绑定的关系。通过Activity::getBaseContext()可以得到ContextImpl实例，通过Activity::getBaseContext()::getOuterContext()可以得到Activity实例自身。

综上，**Activty这个ContextWrapper的mBase是ContextImpl**。

#### Application Context的创建。

ActivityThread源码

```java
public final class ActivityThread {
	/// 打开APP
   	private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        /// 通过Instrumentation的newActivity创建Activity实例。
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = mInstrumentation.newActivity(
            cl, component.getClassName(), r.intent);
        appContext.setOuterContext(activity);
        activity.attach(appContext, this, getInstrumentation(), r.token,
                r.ident, app, r.intent, r.activityInfo, title, r.parent,
                r.embeddedID, r.lastNonConfigurationInstances, config,
                r.referrer, r.voiceInteractor, window, r.configCallback);
        try {
        	/// 重点。 通过Instrumentation的makeApplication创建Application实例。这里的packageInfo.makeApplication只是简单封装。
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);

            /// 不知为何再绑定一次Activity
            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                Configuration config = new Configuration(mCompatConfiguration);
                if (r.overrideConfig != null) {
                    config.updateFrom(r.overrideConfig);
                }
                if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                        + r.activityInfo.name + " with config " + config);
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
                appContext.setOuterContext(activity);
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);
            }
	}
}
```

PackageInfo其实是LoadedApk，源码
/frameworks/base/core/java/android/app/LoadedApk.java

```java
public final class LoadedApk {
	public Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation) {
        if (mApplication != null) {
            return mApplication;
        }
        Application app = null;

        String appClass = mApplicationInfo.className;
        if (forceDefaultAppClass || (appClass == null)) {
            appClass = "android.app.Application";
        }

        try {
            java.lang.ClassLoader cl = getClassLoader();
            if (!mPackageName.equals("android")) {
                Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
                        "initializeJavaContextClassLoader");
                initializeJavaContextClassLoader();
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            }

            /// 重点。 创建App
            ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
            app = mActivityThread.mInstrumentation.newApplication(
                    cl, appClass, appContext);
            appContext.setOuterContext(app);

        } catch (Exception e) {
            if (!mActivityThread.mInstrumentation.onException(app, e)) {
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                throw new RuntimeException(
                    "Unable to instantiate application " + appClass
                    + ": " + e.toString(), e);
            }
        }
        /// 加入Application列表
        mActivityThread.mAllApplications.add(app);
        mApplication = app;
        return app;
    }
}

Instrumentation源码
/frameworks/base/core/java/android/app/Instrumentation.java

public class Instrumentation {
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        return newApplication(cl.loadClass(className), context);
    }

    static public Application newApplication(Class<?> clazz, Context context)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        /// 使用反射创建Application
        Application app = (Application)clazz.newInstance();
        app.attach(context);
        return app;
    }
}
```

可见，Application的context，是通过ContextImpl的静态方法createAppContext()来创建的。

1. 使用ContextImpl.createAppContext创建一个ContextImpl实例（变量appContext）。
2. 使用Instrumentation.newApplication创建一个Application实例（变量app）。
3. 调用app.attach(appContext)，绑定ContextImpl实例到Application实例中的mBase。
4. 调用appContext.setOuterContext(app)，绑定Application实例到ContextImpl实例中的mOuterContext。

所以，Application和ContextImpl也是相互绑定的关系。通过Application::getBaseContext()可以得到ContextImpl实例，通过Application::getBaseContext()::getOuterContext()可以得到Application实例自身。

综上，**Application这个ContextWrapper的mBase是ContextImpl**。

#### Decor Context的创建。

DecorContext是DecorView的Context。也算是View的Context，只是DecorView是一个特殊的View。DecorView只在Window里使用。对于手机，主要是PhoneWindow。

PhoneWindow源码
/frameworks/base/core/java/com/android/internal/policy/PhoneWindow.java

```java
public class PhoneWindow extends Window implements MenuBuilder.Callback {
    /**
     * Constructor for main window of an activity.
     */
    public PhoneWindow(Context context, Window preservedWindow,
            ActivityConfigCallback activityConfigCallback) {
        this(context);

        // Only main activity windows use decor context, all the other windows depend on whatever context that was given to them.
        mUseDecorContext = true;      
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
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                /// 重点
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

DecorView源码
/frameworks/base/core/java/com/android/internal/policy/DecorView.java

public class DecorView extends FrameLayout {

}
```
DecorContext在PhoneWindow中创建，主要用于创建DecorView。DecorView是一个FrameLayout。

1. 在PhoneWindow.setContentView时，创建DecorContext（变量context）。
2. DecorContext的mBase不是通过attach设置，而是通过构造器设置（还记得吧，mBase有两个设置入口，一是构造器，二是attach）。
3. DecorContext构造器传递的是applicationContext。

综上，**DecorContext这个ContextWrapper，它的mBase是applicationContext，也即是Application实例**。


#### ContextImpl的创建。

ContextImpl的创建无处不在。尤其在ActivityThread里面。ActivityThread创建Activity／Service等等时，都会创建ContextImpl。只需要知道ContextImpl是Context唯一的实现。最后提供服务的，通常都是ContextImpl。


### 各类Context创建总结

各类Context的mBase对比。

```java
Context - ContextImpl 直接创建
ContextWrapper - Application  mBase是ContextImpl
ContextWrapper - Activity     mBase是ContextImpl
ContextWrapper - DecorContext mBase是Application
```

从上面的分析可以看出，Application和Activity的mBase虽然都是ContextImpl，但是是不同方法构建的不同实例。DecorContext的mBase是当前Application实例。下面的打印可以验证。

```java
Log.e("Application  instance", ""+getApplication());
Log.e("Activity     instance", ""+this);
Log.e("DecorContext instance", ""+getWindow().getDecorView().getContext());

Log.e("Application's  mBase", ""+getApplication().getBaseContext());
Log.e("Activity's     mBase", ""+this.getBaseContext());
Log.e("DecorContext's mBase", ""+((ContextWrapper)getWindow().getDecorView().getContext()).getBaseContext());
```
输出为

```
E/Application  instance: android.app.Application@9d6f046
E/Activity     instance: com.jokin.MainActivity@9ddd207
E/DecorContext instance: com.android.internal.policy.DecorContext@1dabf34

E/Application's  mBase: android.app.ContextImpl@69d715d
E/Activity's     mBase: android.app.ContextImpl@481cad2
E/DecorContext's mBase: android.app.Application@9d6f046
```


## Android Context总结

1. Context能够实例化的基本只有ContextImpl／DecorContext／Activity 和 Application。

2. 开发过程，context的创建并不需要开发者关心，context都是系统负责创建的，创建时机就是上面分析的那些。

3. 之所以分析上面这些context的创建，是因为，开发者需要知道当前使用的context实例是哪个。**只有知道了是哪个context实例，才能跟踪到真正调用的方法和接口**。
比如，使用Activity这个context时，对应的context.XXX方法跟踪的入口是Activity这个类，而不是ContextWrapper，也不是ContextThemeWrapper。在Activity内调用mBase.XXX方法时，跟踪的不是Context，也不是ContextWrapper，而是它真正的实例，是ContextImpl。
再比如，跟踪DecorView里面的context，跟踪的是DecorContext这个实例，而不是ContextWrapper，也不是ContextThemeWrapper，也不是Activity，也不是Application。而在DecorContext里跟踪mBase时，不是跟踪ContextImpl，而是Application。

4. Context的使用，除了四大组件，还有View／ViewGroup／PopWindow／Dialog／AlertDialog／Toast等等等等。**这些对象使用的Context，跟踪到最后，肯定是ContextImpl／DecorContext／Activity／Application中的某一实例**。这里不再展开分析，是因为没有必要。
比如，上述这些上层对象的创建，入口基本都需要传递一个Context，而无论使用的是view.getContext()，还是window.getContext()，还是getBaseContext()，还是getOuterContext()等等等等，最后get到的这个context实例通常不是Activity就是Application，如果是控件内部，顶多就是ContextImpl或DecorContext。
再比如，跟踪DecorView这个FrameLayout的Context，可以看DecorView的创建，上面已经看到，new DecorView(context, featureId, this, getAttributes());这里传递的context是DecorContext，所以DecorView里面使用的context，其实就是DecorContext。再接下来的跟踪，就是上面对DecorContext的分析。

5. Wrapper这种设计，android里面比较多。
```
Context <- ContextWrapper <- ContextThemeWrapper <- DecorContext
      \              \                    \
       ContextImpl  Application          Activity
```
每一个继承自Context的类，本身是Context，也可以被Wrapper包含，比如DecorContext里面，Wrapper封装的是Application这个Context。而Application本身也是Wrapper。所以Wrapper这种设计，可以层层嵌套，形如链表，最后产生自顶向下的调用链。(Rxjava也有类似设计)。

```java
调用链入口
   |
- WrapperContext
- mBase : WrapperContext
           - mBase : WrapperContext
                      - mBase : WrapperContext
                                 - mBase : Context
```
