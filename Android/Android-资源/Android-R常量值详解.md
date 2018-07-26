<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android R常量值（R.xxx）详解](#android-r常量值rxxx详解)
	- [R常量值的实现原理](#r常量值的实现原理)
		- [Resource Id 值的改变原则：](#resource-id-值的改变原则)
		- [其他ID的改变原则](#其他id的改变原则)
		- [简单总结](#简单总结)
	- [R常量值的使用原理](#r常量值的使用原理)
	- [从源码角度理解资源和R常量](#从源码角度理解资源和r常量)
			- [Resources（ResourcesImpl）的创建过程](#resourcesresourcesimpl的创建过程)
			- [AssetManager的构建流程](#assetmanager的构建流程)
			- [Resources.getLayout](#resourcesgetlayout)

<!-- /TOC -->

## Android R常量值（R.xxx）详解

Android `R常量`是指`build/generated/source/r/debug/`目录下的`R.java`文件中，对各种资源的**整形常量值**定义。

R常量值是一个比较迷糊的值，因为，虽然平常用的非常多，但很多人并不非常理解R常量。

### R常量值的实现原理

#### Resource Id 值的改变原则：

1. 重新编译项目，`ResourceID`的值一定不变。
2. 重命名`id`，`ResourceID`的值可能不变，可能变，取决于如何重命名。不修改首字母，`ResourceID`的值一定不变。
3. 添加新的`id`，`ResourceID`的值可能变，可能不变。

看到以上，可能不理解，但贴一下`R.java`中对`id`的常量定义，就非常清楚：

```java
public static final class id {
  public static final int barrier=0x7f040000;
  public static final int bottom=0x7f040001;
  public static final int btnCallback=0x7f040002; // btn1
  public static final int btnLayout=0x7f040003; // btn2
  public static final int btnText=0x7f040004; // btn3
  public static final int chains=0x7f040005;
  public static final int dimensions=0x7f040006;
  public static final int direct=0x7f040007;
  public static final int end=0x7f040008;
  public static final int gone=0x7f040009;
  public static final int invisible=0x7f04000a;
  public static final int left=0x7f04000b;
  public static final int none=0x7f04000c;
  public static final int packed=0x7f04000d;
  public static final int parent=0x7f04000e;
  public static final int percent=0x7f04000f;
  public static final int right=0x7f040010;
  public static final int sext2=0x7f040011;
  public static final int spread=0x7f040012;
  public static final int spread_inside=0x7f040013;
  public static final int standard=0x7f040014;
  public static final int start=0x7f040015;
  public static final int text1=0x7f040016; // text
  public static final int top=0x7f040017;
  public static final int wrap=0x7f040018;
}
```

`ResourceID`就是按照**字母表**顺序，从`0x7f040000`(2130968576)开始，依次加1的结果。至于修改是否影响到`ResourceID`的值，取决于是否影响到这个字母表顺序的位置。

#### 其他ID的改变原则

同理，其它`R.layout`，`R.string`，`R.style`，`R.color`，`R.drawable`等等，也是按照字母表顺序定义的常量。

```java
public static final class color {
  public static final int colorAccent=0x7f020000;
  public static final int colorPrimary=0x7f020001;
  public static final int colorPrimaryDark=0x7f020002;
}
public static final class drawable {
  public static final int ic_launcher_background=0x7f030001;
  public static final int ic_launcher_foreground=0x7f030002;
}
```

```java
public static final class layout {
  public static final int activity_main=0x7f050000;
  public static final int layout_remote=0x7f050001;
}
```

```java
public static final class mipmap {
  public static final int ic_launcher=0x7f060000;
  public static final int ic_launcher_round=0x7f060001;
}
public static final class string {
  public static final int app_name=0x7f070000;
}
public static final class style {
  public static final int AppTheme=0x7f080000;
}
```

如上，从`0x7f020000`、`0x7f030001`到`0x7f060000`，分别表示一类资源的ID。

#### 简单总结

总结，R常量值的实现原理是，对int数值范围，按照资源类型进行分段，每段int数值范围代表不同类型的资源，然后各分段按照字母表顺序，依次对具体的资源进行int常量定义，最终使得所有资源有一个与之唯一对应的int数值常量。

通过这个int数值常量，可以找到与之唯一对应的资源。包括控件(View)、颜色(Color)、样式(Style)、布局(layout)等等。

但，int常量与资源之间的对应关系并不是一成不变的，一旦改动影响到字母表顺序，旧的int常量就会对应新的资源。

所以，必须使用`R.xxx`来引用常量，而不能直接使用具体的数值！

### R常量值的使用原理

R常量值是如何使用的呢？以一段常见的代码为例：

```java
View rootView = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null, false);
TextView textView = rootView.findViewById(R.id.text);
textView.setText(R.string.app_name);
```

`R.layout.activity_main`和`R.id.text`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:layout_width="wrap_content"
             android:layout_height="wrap_content">
	<TextView
		android:id="@+id/text"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="null"/>
</FrameLayout>
```

`R.string.app_name`：

```xml
<resources>
    <string name="app_name">AppDemo</string>
</resources>
```

以上，使用到的`R`常量有，`R.layout.activity_main`和`R.id.text`和`R.string.app_name`，分别表示`layout`布局资源，`id`控件，`string`字符串资源。

1、首先，代码第一句是通过xml生成一个View。生成的View会变成：

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:layout_width="wrap_content"
             android:layout_height="wrap_content">
	<TextView
		android:id="0x7f040016"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:text="null"/>
</FrameLayout>
```

`TextView`的id会变成具体的int数值，就是`R`常量里定义的`R.id.text`常量值（0x7f040016）。

2、其次，代码第二句，通过`findViewById`查找`R.id.text`常量的控件。相当于`rootView.findViewById(0x7f040016)`。

3、由于`rootView`里面，确实存在一个数值为`0x7f040016`的TextView，所以可以查找出来。

4、设置`TextView`的字符串为`R.string.app_name`，系统会找到int数值对应的字符串，然后设置到TextView，下面会详解。

以上，大致可以理解，`R`常量在Android应用中的使用原理。尤其是布局中`R.id`常量的使用。

逆向思维一下，以上`TextView`的id，只要保证id在字母表的顺序不会变化，那么，在代码中可以完全写死：

`rootView.findViewById(0x7f040016)`

但不要忘记，一旦字母表顺序改变，0x7f040016就不再表示`TextView`控件了，可能是另一个控件。

### 从源码角度理解资源和R常量

阅读源码前，大致说一下，Android每个应用，都有与之对应的`Resources`对象，表示应用的资源。

应用可以通过`Resources`对象访问应用内部的资源。

由于`R`是一个常量定义，所以编译后运行的应用中，`R定义的常量`是存在在应用的apk中的！

通俗的说，如果在release版本中打印出来`R.id.text`的值为`0x7f040015`，那么就可以通过`0x7f040015`来直接访问。

你也可以类比成，你知道服务器的ip，然后就写死ip来访问服务器。

源码的解析，以上面的代码块为例：

```java
View rootView = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null, false);
TextView textView = rootView.findViewById(R.id.text);
textView.setText(R.string.app_name);
```

第一句：`LayoutInflater.from(mContext)`，从当前应用的`context`得到应用的`LayoutInflater`实例。其实就是一个`SystemService`，叫`LAYOUT_INFLATER_SERVICE`。

使用过Android的都知道，不同应用的`Context`，得到的`SystemService`只是针对当前应用的Service。

```java
/**
 * Obtains the LayoutInflater from the given context.
 */
public static LayoutInflater from(Context context) {
	LayoutInflater LayoutInflater =
			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	if (LayoutInflater == null) {
		throw new AssertionError("LayoutInflater not found.");
	}
	return LayoutInflater;
}
```

紧接着的`inflate(R.layout.activity_main, null, false)`比较常见。这是一个比较复杂的方法，参数对inflate的影响非常大，这里就是单纯通过`R.layout.xxx`来创建一个View。

```java
public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
	final Resources res = getContext().getResources();
	if (DEBUG) {
		Log.d(TAG, "INFLATING from resource: \"" + res.getResourceName(resource) + "\" ("
				+ Integer.toHexString(resource) + ")");
	}

	final XmlResourceParser parser = res.getLayout(resource);
	try {
		return inflate(parser, root, attachToRoot);
	} finally {
		parser.close();
	}
}
```

上面，`final Resources res = getContext().getResources();`就是通过当前应用的`Context`，得到应用的`Resources`对象。

**这个Resources对象就是代表当前应用的资源，对应用资源的创建不感兴趣的，可以略过下面的追溯。**

要追溯`getContext().getResources()`，其实是`ContextWrapper:getResources()`中`mBase.getResources()`。

`mBase`其实是`Activity:attach()`时传入的`Context`。

依次追溯，`mBase`是 `ActivityThread:createBaseContextForActivity`和`ContextImpl.createActivityContext`。

> 这部分追溯过程可以见另一篇`Context详解`

```java
// ActivityThread:createBaseContextForActivity
private ContextImpl createBaseContextForActivity(ActivityClientRecord r) {       
	/// 创建的时候，把PackageInfo（即LoadedApk）传递到ContextImpl
	ContextImpl appContext = ContextImpl.createActivityContext(
			this, r.packageInfo, r.activityInfo, r.token, displayId, r.overrideConfig);

	final DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();

	return appContext;
}
```

```java
// ContextImpl:createActivityContext
static ContextImpl createActivityContext(ActivityThread mainThread,
		LoadedApk packageInfo, ActivityInfo activityInfo, IBinder activityToken, int displayId,
		Configuration overrideConfiguration) {

	final ResourcesManager resourcesManager = ResourcesManager.getInstance();

	// Create the base resources for which all configuration contexts for this Activity
	// will be rebased upon.
	context.setResources(resourcesManager.createBaseActivityResources(activityToken,
			packageInfo.getResDir(),
			splitDirs,
			packageInfo.getOverlayDirs(),
			packageInfo.getApplicationInfo().sharedLibraryFiles,
			displayId,
			overrideConfiguration,
			compatInfo,
			classLoader));
	context.mDisplay = resourcesManager.getAdjustedDisplay(displayId,
			context.getResources());
	return context;
}
```

通过`ResourcesManager.getInstance()`得到`ResourcesManager对象`，然后通过包的资源路径（ResDir）来加载应用的资源，返回一个`Resources对象`设置到`Context`里面。

##### Resources（ResourcesImpl）的创建过程

1、`Activity`创建时，会创建`Activity`对应的`Resources`。在`createActivityContext`中，调用`createBaseActivityResources`创建`Resources`。

2、首先，创建了一个`ResourcesKey`，如果参数不变，得到的`ResourcesKey`是一样的。换言之，通常一个应用，就只有一个key（猜测同一个应用参数不变，具体没确认）。

3、通过key创建一个`ResourcesImpl`加入到map中，如果map已经存在key，则从map中返回key对应的`ResourcesImpl`。

4、最后通过`ResourcesManager:getOrCreateResourcesForActivityLocked`创建对应的`Resources`对象。

```java
// ResourcesManager:getOrCreateResourcesForActivityLocked
/**
 * Gets an existing Resources object tied to this Activity, or creates one if it doesn't exist
 * or the class loader is different.
 */
 private @NonNull Resources getOrCreateResourcesForActivityLocked(@NonNull IBinder activityToken,
		@NonNull ClassLoader classLoader, @NonNull ResourcesImpl impl,
		@NonNull CompatibilityInfo compatInfo) {
	// A.
	final ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(
			activityToken);

	// B.
	final int refCount = activityResources.activityResources.size();
	for (int i = 0; i < refCount; i++) {
		WeakReference<Resources> weakResourceRef = activityResources.activityResources.get(i);
		Resources resources = weakResourceRef.get();

		if (resources != null
				&& Objects.equals(resources.getClassLoader(), classLoader)
				&& resources.getImpl() == impl) {
			return resources;
		}
	}

    // C.
	Resources resources = compatInfo.needsCompatResources() ? new CompatResources(classLoader)
			: new Resources(classLoader);
	resources.setImpl(impl);

	// D.
	activityResources.activityResources.add(new WeakReference<>(resources));
	return resources;
}
```
文档描述：Gets an existing Resources object tied to this Activity, or creates one if it doesn't exist or the class loader is different.（获取一个已经存在的，与当前Activity绑定的Resources对象；如果不存在，或classloader发生改变，则重新创建一个）

A. 通过`activityToken`获得一个`ActivityResources`结构。文档描述是，Resources associated with an Activity。

B. 遍历绑定在Activity中的Resources。如果能够找到一个一样的，那么直接返回那个Resources。

C. 都没能找到，则通过`classLoader`新建一个`Resources对象`。然后和`ResourcesImpl实例`绑定。（没找到的对象，仍旧留在集合里，没清理）

D. 把这个新的`Resources`继续丢到与当前Activity绑定的`ActivityResources`列表里。然后返回这个新的`Resources`对象。

**总结：**

```java
  // 创建ActivityContext的时候，同时创建对应的Resources。
- ContextImpl:createActivityContext()
  // 创建Resources。
- ResourcesManager:createBaseActivityResources()
  // 创建一个key
  - new ResourcesKey()
  // 确保创建了一个ActivityResources数组结构（借助token）
  - getOrCreateActivityResourcesStructLocked(activityToken)
     - 创建(activityToken, new ActivityResources())
  // 更新Resources资源
  - updateResourcesForActivity()
     - 由于刚创建，ActivityResources数组长度是空的，所以基本什么没做。
  // 确保创建一个Resources
  - return getOrCreateResources();
     // 真正创建一个ResourceImpl
     - resourcesImpl = createResourcesImpl(key);
	     // 创建一个AssetManager
         - assets = new AssetManager();
		 - assets.addAssetPath(key.mResDir)
		 - assets.addOverlayPath(idmapPath)
		     - assets.ensureStringBlocks()
			 // 这里，开始正式加载资源表！具体见`AssetManager详解`
		 // 通过AssetManager创建ResourcesImpl
		 - return new ResourcesImpl(assets)
		     - assets.ensureStringBlocks()
     // 丢到一个key-value中
     - mResourceImpls.put(key, new WeakReference<>(resourcesImpl));
     // 使用ResourceImpl创建Resources并返回
     - resources = getOrCreateResourcesForActivityLocked()
        - resources = new Resources(classLoader)；
        - resources.setImpl(impl);
             - newThemeImpl()
        // 创建一个Resources，然后丢到activityResources数组内。
        - activityResources.add(new WeakReference<>(resources));
     - return resources
```
> 以上，是Resource的创建流程，注释才是最重要的！

Resources的创建过程，有多个缓存。首先，`ResourcesImpl`使用`ResourcesKey`作为缓存；其次，在与Activity绑定的`Resources`有一个缓存，缓存了与当前Activity绑定的多个`Resources`；最后，才会真正创建一个`Resources对象`和`ResourcesImpl`绑定。在`Resources`中，真正核心的是`ResourcesImpl`。

所以，对于一般应用，这个`Resources`是相同的。

`Resources`对象在Activity创建时，就一同创建了。创建的同时，会通过`AssetManager`添加各种资源表（ResTable）。

也即是说，**ActivityContext创建的时候，各类Resources资源就已经创建完毕！！！**

以上说了一大片，其实只是讲述了`getContext().getResources()`的代码

紧接着，是`final XmlResourceParser parser = res.getLayout(resource)`.

```java
// Resources.getLayout()
public XmlResourceParser getLayout(@LayoutRes int id) throws NotFoundException {
	return loadXmlResourceParser(id, "layout");
}

// Resources.loadXmlResourceParser()
@NonNull
XmlResourceParser loadXmlResourceParser(@AnyRes int id, @NonNull String type) throws NotFoundException {
   final TypedValue value = obtainTempTypedValue();
   try {
	   final ResourcesImpl impl = mResourcesImpl;
	   impl.getValue(id, value, true);
	   if (value.type == TypedValue.TYPE_STRING) {
		   return impl.loadXmlResourceParser(value.string.toString(), id,
				   value.assetCookie, type);
	   }
	   throw new NotFoundException("Resource ID is not valid");
   } finally {
	   releaseTempTypedValue(value);
   }
}
```

`Resources`通过内部的`ResourcesImpl`对象来实现真正的操作。所以，`ResourcesImpl`才是重点。

```java
// ResourcesImpl:getValue()
void getValue(@AnyRes int id, TypedValue outValue, boolean resolveRefs)
        throws NotFoundException {
    boolean found = mAssets.getResourceValue(id, 0, outValue, resolveRefs);
    if (found) {
        return;
    }
    throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
}
```

以上，可见，ResourcesImpl里面，依赖的是`Assets`；同理，impl.loadXmlResourceParser也是依赖`Assets`。

这个`Assets`其实是`AssetManager`对象。通过`ResourcesImpl(AssetManager assets)`构造方法传入的。

##### AssetManager的构建流程

通过查看`ResourcesImpl`的构建过程：

```java
// ResourcesManager:createResourcesImpl()
private @Nullable ResourcesImpl createResourcesImpl(@NonNull ResourcesKey key) {
    final DisplayAdjustments daj = new DisplayAdjustments(key.mOverrideConfiguration);
    daj.setCompatibilityInfo(key.mCompatInfo);

    final AssetManager assets = createAssetManager(key);
    if (assets == null) {
        return null;
    }

    final DisplayMetrics dm = getDisplayMetrics(key.mDisplayId, daj);
    final Configuration config = generateConfig(key, dm);
    final ResourcesImpl impl = new ResourcesImpl(assets, dm, config, daj);

    if (DEBUG) {
        Slog.d(TAG, "- creating impl=" + impl + " with key: " + key);
    }
    return impl;
}
```

`AssetManager`同样是通过同样的`ResourcesKey`构建的。但它并没有将key作为键值，而是读取key里面的路径。

```java
// ResourcesManager:createAssetManager()
protected @Nullable AssetManager createAssetManager(@NonNull final ResourcesKey key) {
    AssetManager assets = new AssetManager();
    assets.addAssetPath(key.mResDir);
    for (final String splitResDir : key.mSplitResDirs) {
        if (assets.addAssetPath(splitResDir) == 0) {
            Log.e(TAG, "failed to add split asset path " + splitResDir);
            return null;
        }
    }
    for (final String idmapPath : key.mOverlayDirs) {
        assets.addOverlayPath(idmapPath);
    }
    for (final String libDir : key.mLibDirs) {
        if (libDir.endsWith(".apk")) {
            // Avoid opening files we know do not have resources,
            // like code-only .jar files.
            if (assets.addAssetPathAsSharedLibrary(libDir) == 0) {
                Log.w(TAG, "Asset path '" + libDir +
                        "' does not exist or contains no resources.");
            }
        }
    }
    return assets;
}
```

1、直接`new`构建一个`AssetManager`。文档描述为(Create a new AssetManager containing only the basic system assets.)。构建AssetManager时，只是构建了一个包含系统资源的对象。后期才会绑定应用，通过Resources#getAssets绑定。

2、把`mResDir`，`mSplitResDirs`，`mLibDirs`目录列表，添加到`AssetManager`里面。**在这个过程，AssetManager完成了整个应用资源表的构建。**

3、构建完成。

相对而言，`AssetManager`的构建非常简单清晰。和构建一个普通对象差不多。

`AssetManager`里面，添加路径这些操作，最终会调用到各种`native`方法，比如`addAssetPathNative`。

最终会调用到`AssetManager.cpp`。具体见另一篇《AssetManager详解》

##### Resources.getLayout

再次回过来，调用到`impl.getValue(id, value, true)`，最终调用到`mAssets.getResourceValue(id, 0, outValue, resolveRefs)`.

```java
// AssetManager:getResourceValue()
final boolean getResourceValue(@AnyRes int resId, int densityDpi, @NonNull TypedValue outValue,
        boolean resolveRefs) {
    synchronized (this) {
        final int block = loadResourceValue(resId, (short) densityDpi, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }

        // Convert the changing configurations flags populated by native code.
        outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(
                outValue.changingConfigurations);

        if (outValue.type == TypedValue.TYPE_STRING) {
            outValue.string = mStringBlocks[block].get(outValue.data);
        }
        return true;
    }
}
```

loadResourceValue是native方法，对应`android_util_AssetManager:xxx_loadResourceValue`。

```java
// android_util_AssetManager.cpp
static jint android_content_AssetManager_loadResourceValue(JNIEnv* env, jobject clazz,jint ident,jshort density,jobject outValue,jboolean resolve)
{
    if (outValue == NULL) {
         jniThrowNullPointerException(env, "outValue");
         return 0;
    }
    AssetManager* am = assetManagerForJavaObject(env, clazz);
    if (am == NULL) {
        return 0;
    }
    const ResTable& res(am->getResources());

    Res_value value;
    ResTable_config config;
    uint32_t typeSpecFlags;
    ssize_t block = res.getResource(ident, &value, false, density, &typeSpecFlags, &config);
    if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
            jniThrowException(env, "java/lang/IllegalStateException", "Bad resource!");
            return 0;
        }
    }
    uint32_t ref = ident;
    if (resolve) {
        block = res.resolveReference(&value, block, &ref, &typeSpecFlags, &config);
        if (kThrowOnBadId) {
            if (block == BAD_INDEX) {
                jniThrowException(env, "java/lang/IllegalStateException", "Bad resource!");
                return 0;
            }
        }
    }
    if (block >= 0) {
        return copyValue(env, outValue, &res, value, ref, block, typeSpecFlags, &config);
    }

    return static_cast<jint>(block);
}
```

主要是：

a. res.getResource(ident, &value, false, density, &typeSpecFlags, &config);
查询信息，填充到&value, &typeSpecFlags, &config里面。

b. copyValue(env, outValue, &res, value, ref, block, typeSpecFlags, &config);
复制信息，复制到jobject outValue对象里面，将信息返回到上层。

其中，copyValue可以关注以下，因为可以跟踪java层属性对应关系：

```java
jint copyValue(JNIEnv* env, jobject outValue, const ResTable* table,
               const Res_value& value, uint32_t ref, ssize_t block,
               uint32_t typeSpecFlags, ResTable_config* config)
{
    env->SetIntField(outValue, gTypedValueOffsets.mType, value.dataType);
    env->SetIntField(outValue, gTypedValueOffsets.mAssetCookie,
                     static_cast<jint>(table->getTableCookie(block)));
    env->SetIntField(outValue, gTypedValueOffsets.mData, value.data);
    env->SetObjectField(outValue, gTypedValueOffsets.mString, NULL);
    env->SetIntField(outValue, gTypedValueOffsets.mResourceId, ref);
    env->SetIntField(outValue, gTypedValueOffsets.mChangingConfigurations,
            typeSpecFlags);
    if (config != NULL) {
        env->SetIntField(outValue, gTypedValueOffsets.mDensity, config->density);
    }
    return block;
}
```

java层TypedValue对应的属性关系为：

```java
TypedValue.mType = Res_value.dataType
TypedValue.mAssetCookie = ResTable.getTableCookie(block)
TypedValue.mData = Res_value.data
TypedValue.mString = Null
TypedValue.mResourceId = ref
TypedValue.mChangingConfigurations = typeSpecFlags
TypedValue.mDensity = ResTable_config.density
```

然后，你会注意到，`AssetManager.java`里面，有比较多以下用法:

```java
// AssetManager:getResourceValue()
if (outValue.type == TypedValue.TYPE_STRING) {
	outValue.string = mStringBlocks[block].get(outValue.data);
}
```

其实，就是指，这个资源的值如果是字符串，那么直接将数据段(data)转化为`String`类型。如果不是字符串，那么数据段(data)可能是一个二进制流，二进制流使用`TypedValue.TYPE_REFERENCE`引用表示。
