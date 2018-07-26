<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android获取另一个进程的资源](#android获取另一个进程的资源)
	- [参考RemoteView的跨进程View显示逻辑](#参考remoteview的跨进程view显示逻辑)
	- [照葫芦画瓢，实现跨进程layout显示](#照葫芦画瓢实现跨进程layout显示)
	- [createPackageContext解析](#createpackagecontext解析)
		- [createPackageContext的其它用法1:SharedPreferences](#createpackagecontext的其它用法1sharedpreferences)
		- [createPackageContext的其它用法2: 换肤](#createpackagecontext的其它用法2-换肤)
	- [利用资源名称反射得到资源的ID值](#利用资源名称反射得到资源的id值)
	- [跨越了沙盒机制？](#跨越了沙盒机制)

<!-- /TOC -->

## Android获取另一个进程的资源

### 参考RemoteView的跨进程View显示逻辑

```java
// RemoteView.java
public View apply(Context context, ViewGroup parent) {
	return apply(context, parent, null);
}

/** @hide */
public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
	RemoteViews rvToApply = getRemoteViewsToApply(context);

	View result;

	// 通过RemoteView中的packageName，得到packageName应用的一个Context。
	Context c = prepareContext(context);

    // 通过另一个应用的Context来得到LayoutInflater服务
	LayoutInflater inflater = (LayoutInflater)
			c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	// 把LayoutInflater对象中的Context重新设置为另一个应用的Context。
	//（既然LayoutInflater由另一个应用的Context构建，应该不需要再重新设置）
	inflater = inflater.cloneInContext(c);
	inflater.setFilter(this);

    // 使用另一个应用的LayoutInflater，和另一个应用的layout_id，
	// 创建一个View，并且挂到当前应用的View中
	result = inflater.inflate(rvToApply.getLayoutId(), parent, false);

	rvToApply.performApply(result, parent, handler);

	return result;
}

private Context prepareContext(Context context) {
	Context c;
	String packageName = mPackage;

	if (packageName != null) {
		try {
			c = context.createPackageContextAsUser(
					packageName, Context.CONTEXT_RESTRICTED, mUser);
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "Package name " + packageName + " not found");
			c = context;
		}
	} else {
		c = context;
	}
	return c;
}
```

照葫芦画瓢，最核心的应该是`prepareContext`，使用另一个应用的包名，构建另一个应用的`Context`。由于有了另一个应用的`Context`，那么这个`Context`就可以引用到另一个应用的`Resources`，继而得到另一个应用的所有资源！另一个应用的所有资源，包括：

* 资源ID和对应的数据。
* 图片／控件／布局／颜色／xml等等。

由于`资源ID`在打包后就不会改变，所以，`资源ID`可以跨进程，既可以在当前应用使用，也可以在另一个应用使用。

这也是，为什么`RemoteView`可以通过传递当前应用的资源ID值，在另一个应用里进行`findViewById()`查找View的原因!

当使用另一个应用的`Resources`的时候，使用另一个应用的`layout_id`进行的View创建的时候，`layout`和在另一个应用环境中创建是完全一样的。`layout`里面的View所使用的`ID值`也是一样的。

### 照葫芦画瓢，实现跨进程layout显示

```java
private void setLayout(int layoutID, String pkgName) {
	Log.d(TAG, "setLayout() called with: layoutID = [" + layoutID + "]");

	Context c = prepareContext(this, pkgName);
	LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	inflater = inflater.cloneInContext(c);
	inflater.inflate(layoutID, mRootView, true);
}

private Context prepareContext(Context context, String pkgName) {
	Context c;
	String packageName = pkgName;

	if (packageName != null) {
		try {
			c = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
			// c = context.createPackageContextAsUser(
			//         packageName, Context.CONTEXT_RESTRICTED, mUser);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name " + packageName + " not found");
			c = context;
		}
	} else {
		c = context;
	}
	return c;
}
```

虽然`RemoteView`中，`context.createPackageContextAsUser()`是隐藏方法，但可以发现，context有另一个方法也可以创建Context实例：`context.createPackageContext()`。

另一个思路是，虽然是隐藏方法，可以使用反射来实现。

以上代码可以实现，在另一个进程里，显示当前进程的一个layout。

### createPackageContext解析

```java
/**
 * Return a new Context object for the given application name.  This
 * Context is the same as what the named application gets when it is
 * launched, containing the same resources and class loader.  Each call to
 * this method returns a new instance of a Context object; Context objects
 * are not shared, however they share common state (Resources, ClassLoader,
 * etc) so the Context instance itself is fairly lightweight.
 *
 * <p>Throws {@link android.content.pm.PackageManager.NameNotFoundException} if there is no
 * application with the given package name.
 *
 * <p>Throws {@link java.lang.SecurityException} if the Context requested
 * can not be loaded into the caller's process for security reasons (see
 * {@link #CONTEXT_INCLUDE_CODE} for more information}.
 *
 * @param packageName Name of the application's package.
 * @param flags Option flags.
 *
 * @return A {@link Context} for the application.
 *
 * @throws SecurityException &nbsp;
 * @throws PackageManager.NameNotFoundException if there is no application with
 * the given package name.
 */
public abstract Context createPackageContext(String packageName,
		@CreatePackageOptions int flags) throws PackageManager.NameNotFoundException;
```

通过应用的包名，创建一个新的Context对象。这个Context对象和应用启动时的Context对象是一样的，包含了相同的`Resources`和`classloader`。
每调用一次，就返回一个新的Context对象。这些Context对象是不同的实例，但他们具有相同的应用环境，比如相同的`Resources`和`ClassLoader`等等。
（所以，Context对象其实是很轻量的）

可能跑出的异常：
`NameNotFoundException`：指定的应用不存在。
`SecurityException`：由于安全因素，指定的应用的Context不支持被加载到当前进程。

Flags:

* CONTEXT_INCLUDE_CODE: 可能会抛NameNotFoundException。如果使用此Flag，可以通过`getClassLoader()`得到另一个应用的类，可以构建另一个应用的对象。
* CONTEXT_IGNORE_SECURITY: 创建另一个应用的Context时，忽略安全限制。谨慎使用。忽略安全警告，如果**不加**这个标志的话，有些功能是用不了的，会出现安全警告。
* CONTEXT_RESTRICTED：创建一个受限的Context，会禁用特殊的特性。比如，如果一个View和受限的Context关联，会忽略部分xml属性（意思是部分属性丢失）。

// 下面这几个是@hide隐藏的。外部不可用。
* CONTEXT_DEVICE_PROTECTED_STORAGE：创建的Context，对应的文件操作API会指向设备保护的磁盘（大概是指向只读磁盘？）。
* CONTEXT_CREDENTIAL_PROTECTED_STORAGE：同上，此时文件操作API指向的是证书保护的磁盘。
* CONTEXT_REGISTER_PACKAGE：用于暗示，要通知activity-manager当前进程要加载代码。

#### createPackageContext的其它用法1:SharedPreferences

`createPackageContext`只是用来创建一个另一个应用的`Context`，可以做的事情还有很多。

比如：读取另一个应用的`SharedPreferences`数据。

1、首先，`SharedPreferences`创建的时候，可以有多种模式：

* MODE_PRIVATE：默认私有。
* MODE_WORLD_READABLE：全世界可读。
* MODE_WORLD_WRITEABLE：全世界可写。
* MODE_MULTI_PROCESS：多进程访问，保证多进程间变量的值即时变化。

创建的`SharedPreferences`必须为`MODE_WORLD_READABLE`，这样其它应用才能访问！

2、在其它应用内，使用`createPackageContext`创建一个当前应用的Context。
3、使用这个`Context`，在其它应用访问这个共享的`SharedPreferences`：

```java
try {
	Context context = createPackageContext("com.jokin.demo", Context.CONTEXT_IGNORE_SECURITY);
} catch (NameNotFoundException e) {
	e.printStackTrace();
}
SharedPreferences sharedPreferences = context.getSharedPreferences("demo.data", Context.MODE_WORLD_READABLE);
String name = sharedPreferences.getString("name", "");
```

如果使用上面的代码，当进程A和进程B都对同一个属性进行读写的时候，进程B先修改，进程A读取，会发现进程A读取的值还未更新。

这是因为默认情况下，SharedPreferences被一个进程加载后，就不会检测文件内容是否发生改变。

为了实现多进程的及时更新，需要将MODE设置为`MODE_MULTI_PROCESS`。这样，即时SharedPreferences已经被当前进程加载了，但每次访问SharedPreferences的时候，都会去检测文件是否发生改变。以此来实现多进程的读写同步。

#### createPackageContext的其它用法2: 换肤

换肤的基本原理，是上面说的，获取另一个进程的`Resources`资源。

既然`Resources`资源都获取了，那么就可以在当前应用使用另一个应用的`Resources`资源。

换肤有两个思路：

1. 主程序和换肤应用，约定一个共同的目录。换肤应用把资源解压到共同目录，然后主程序解析，以此实现换肤。

2. 主程序和换肤应用相互独立，主程序通过获取换肤应用的`Resources`资源，将资源加载到主程序使用。

方式2的注意点在于：

1. 多个换肤应用，资源包`Resources`中，同一个资源的ID值是不同的。比如都是背景，但不同应用中，背景资源可能都叫bg.png，但对应的ID值是不一样的。
2. 由于上面同一个资源不同ID的问题，主程序中不应该直接使用资源ID，而是使用资源名，或另外一种约定。比较常见的，是约定资源名，然后通过发射，得到资源的ID，然后使用。

### 利用资源名称反射得到资源的ID值

```java
/***
 * @param clazz 目标资源的R.java
 * @param className R.java的内部类，如layout,string,drawable...
 * @param name 资源名称
 * @return
 */
private int getResourseIdByName(Class clazz, String className, String name) {
    int id = 0;
    try {
        Class[] classes = clazz.getClasses(); // 获取R.java里的所有静态内部类
        Class desireClass = null;

        for (int i = 0; i < classes.length; i++) {
            if (classes[i].getName().split("\\$")[1].equals(className)) { // 查找指定的静态内部类
                desireClass = classes[i];
                break;
            }
        }
        if (desireClass != null)
            id = desireClass.getField(name).getInt(desireClass); // 从指定的静态内部类获得资源编号
    } catch (IllegalArgumentException e) {
        e.printStackTrace();
    } catch (SecurityException e) {
        e.printStackTrace();
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    } catch (NoSuchFieldException e) {
        e.printStackTrace();
    }
    return id;
}
```

使用例子：

```java
Context context;
try {
    context = createPackageContext("com.test.resource", Context.CONTEXT_INCLUDE_CODE
            | Context.CONTEXT_IGNORE_SECURITY);
    Class cls = context.getClassLoader().loadClass("com.test.resource.R"); // 获得目标apk的R类
    txvA.setText(context.getResources().getText(getResourseIdByName(cls, "string", "message")));
} catch (NameNotFoundException e) {
    e.printStackTrace();
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}
```

### 跨越了沙盒机制？

通常一个软件是不能创建其它程序的Context的，除非它们拥有相同的用户ID与签名。用户ID是一个字符串标识，在程序的AndroidManifest.xml文件的manifest标签中指定，格式为android:shareUserId="xx"。安装在设备中的每一个apk程序，Android系统会给其分配一个单独的用户空间,其中android:shareUserId就是对应一个Linux用户ID，并且为它创建一个沙箱，以防止与其它应用程序产生影响。用户ID 在应用程序被安装到设备中时分配。通过SharedUserid,拥有同一个Userid的多个APK可以配置成运行在同一个进程中，所以默认就是可以互相访问任意数据，也可以配置成运行在不同的进程中, 同时可以访问其APK的数据目录下的资源(图片，数据库和文件），就像访问本程序的数据一样。


android:sharedUserId="android.uid.system"

表示运行于系统用户，但并不是只是一个进程。
