## WindowFramework设计

### 统一接口

举例：普通的ViewGroup与添加到WindowManager中的ViewGroup可以使用统一的接口，实现`Move/Scale/Min/Max`等操作。

思路有两种：

**思路1**：将`WindowManager`中的`ViewGroup`装饰一下，使其与普通`ViewGroup`一样。调用`ViewGroup`的接口，就能控制视图在`WindowManager`中进行刷新。

这种方法，已经实现了覆盖`setLayoutParams()`来对`WindowManager`中的ViewGroup进行刷新。

**不足** 的是：

1.1 没办法覆盖重写`setLeft()/setRight()`等`final`方法。

1.2 ViewGroup关于UI布局的接口有很多，比如`setAlpha()，setScaleX()`等等，如果没进行覆盖，则会导致同样的接口，在`WindowManager`中不能刷新布局。

> 关于这点，其实WindowManager中，关注的只有x, y, gravity, alpha, width, height。

1.3 如果使用`ViewGroup.setX()`来控制`WindowManager.LayoutParams.x`，那么两者的效果是不一致的。因为普通的`ViewGroup.setX()`在刷新后会失效；而`WindowManager.LayoutParams.x`则在刷新后仍旧生效。而且，通过`setX()`来改变`WindowManager.LayoutParams.x`时，不能调用`super.setX()`，因为它不能改变到View的x坐标。

1.4 扩展性不足。因为内部不知道哪些接口是有效的，安全的，毕竟并没有实现`setLeft()/setRight()`等`final`方法。

总结：不可行。

**思路2**：统一成接口`IView`，然后两种类型的ViewGroup都实现`IView`接口来实现统一接口。

**优点** 是：

2.1 扩展性好。后续需要新的特性，增加新接口即可。

2.2 接入成本低。无论使用View还是ViewGroup还是其他View，只需要实现对应的接口，就可以接入。

这种方式简单易用。但个人觉得，没有方式一优雅，可惜方式一由于Android的限制，没法完美实现。

总结：使用思路2的方式来统一接口。然后可以结合思路1来实现接口。



## RemoteViews



```java
private static Context s_c;
public static LayoutInflater from(Context localContext, String remotePackageName) {
    Context c = prepareContext(localContext, remotePackageName);
    LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    return inflater.cloneInContext(c);
}

private static Context prepareContext(Context context, String pkgName) {
    if (s_c != null) {
        return s_c;
    }
    Context c;
    String packageName = pkgName;
    if (packageName != null) {
        try {
            c = context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
            // c = context.createPackageContextAsUser(
            //         packageName, Context.CONTEXT_RESTRICTED, mUser);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name " + packageName + " not found");
            c = context;
        }
    } else {
        c = context;
    }
    s_c = c;
    return c;
}

View realView = RemoteLayoutInflater.from(mContext, view.getPackage()).inflate(view.getLayoutId(), mRootView, false);
```

通过主动创建，可以直接创建xml里面的自定义对象。

这个Context是对应xml所在的apk的。比如，上面得到的View，通过`view.getContext().getCacheDir()`得到的是`/data/user/0/com.jokin.framework.moduleb/cache/`，并不是宿主的私有路径。

由于Android7.0以上，所有权限操作都要运行时申请，所以虽然宿主获得了权限，但这个远程创建的本地View，它使用的Context是远程的apk的Context，所以需要远程apk也动态申请权限。

本来以为远程apk也申请权限就可以了。但结果不是。原因是，宿主中的获取远程apk的context，和远程apk的context是两个完全不同的沙盒。虽然都是访问同一个apk包，但两者运行环境不一致。

```java
08-24 14:27:26.309 11063-11063/com.jokin.framework.cwindowframework E: java.io.FileNotFoundException: /data/user/0/com.jokin.framework.moduleb/cache/a (Permission denied)
   at java.io.FileOutputStream.open0(Native Method)
   at java.io.FileOutputStream.open(FileOutputStream.java:287)
   at java.io.FileOutputStream.<init>(FileOutputStream.java:223)
   at java.io.FileOutputStream.<init>(FileOutputStream.java:110)
   at com.jokin.framework.modulesdk.view.CViewWindow.onClick(CViewWindow.java:320)
   at android.view.View.performClick(View.java:6294)
   at android.view.View$PerformClick.run(View.java:24770)
```

android 目录 `/data/data/` 跟 `/data/user/0/` 差别

测试了两台手机一台 4.1.2 一台 6.0 。调用 Context.getFilesDir.getAbsolutePath 方法。
4.1.2 返回 /data/data/package/files
6.0 返回 /data/user/0/package/files

6.0 的 /data/user/0/跟 data/data 有区别，如果我想获取 /data/data 改怎么弄
6.0 支持多用户的，之前不知道有没有。

单用户：/data/user/0/package/files
另一个用户：/data/user/1/package/files
旧系统，是/data/data目录。

/data/user/0/com.jokin.framework.moduleb/cache/a

```java
View realView = view.apply(mContext, mRootView);
```

通过`RemoteViews`默认的apply方法，默认是不支持访问`CONTEXT_INCLUDE_CODE`的。见源码：

```java
return context.createApplicationContext(mApplication, Context.CONTEXT_RESTRICTED);
```

所以会报错：`failNotAllowed`。（如下）

```shell
14:30:30.071 11204-11204/com.jokin.framework.cwindowframework E: Plugin Dead!
   android.view.InflateException: Binary XML file line #0: Binary XML file line #0: Error inflating class com.jokin.framework.modulesdk.view.CViewWindow
   Caused by: android.view.InflateException: Binary XML file line #0: Error inflating class com.jokin.framework.modulesdk.view.CViewWindow
   Caused by: android.view.InflateException: Binary XML file line #0: Class not allowed to be inflated com.jokin.framework.modulesdk.view.CViewWindow
       at android.view.LayoutInflater.failNotAllowed(LayoutInflater.java:686)
       at android.view.LayoutInflater.createView(LayoutInflater.java:612)
       at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:790)
       at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:730)
       at android.view.LayoutInflater.inflate(LayoutInflater.java:492)
       at android.view.LayoutInflater.inflate(LayoutInflater.java:423)
       at android.widget.RemoteViews.inflateView(RemoteViews.java:3498)
       at android.widget.RemoteViews.apply(RemoteViews.java:3475)
       at android.widget.RemoteViews.apply(RemoteViews.java:3468)
```
