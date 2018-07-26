<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android AssetManager源码详解](#android-assetmanager源码详解)
	- [AssetManager的构建](#assetmanager的构建)
	- [AssetManager内部源码](#assetmanager内部源码)
		- [Native接口的查找与定位](#native接口的查找与定位)
		- [AssetManager的初始化过程](#assetmanager的初始化过程)
		- [应用资源的加载时机](#应用资源的加载时机)
		- [盒子模型](#盒子模型)
		- [AssetManager与resources.arsc](#assetmanager与resourcesarsc)

<!-- /TOC -->

## Android AssetManager源码详解

基于Android 8.1.0源码。

### AssetManager的构建

从`ResourcesManager:createAssetManager`开始。

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

AssetManager可以直接构建。

```java
AssetManager assets = new AssetManager();
```

然后为AssetManager添加各种资源目录。

```java
assets.addAssetPath(key.mResDir);
assets.addOverlayPath(idmapPath);
assets.addAssetPathAsSharedLibrary(libDir);
```

完成AssetManager的构建。

### AssetManager内部源码

```java
/**
 Create a new AssetManager containing only the basic system assets.
 Applications will not generally use this method, instead retrieving the appropriate asset manager with {@link Resources#getAssets}.
 */
public AssetManager() {
	synchronized (this) {
		if (DEBUG_REFS) {
			mNumRefs = 0;
			incRefsLocked(this.hashCode());
		}
		init(false);
		if (localLOGV) Log.v(TAG, "New asset manager: " + this);
		ensureSystemAssets();
	}
}
```

空构造方法的构造器。

文档说：创建一个包含基本系统资源的AssetManager。Applications通常不使用这个方法来得到一个AssetManager，而是通过`Resources.getAssets()`来得到这个AssetManager。

简而言之，这个构造方法是给`Resources`用的。你要使用`AssetManager`，直接从`Resources`里拿就行了。

#### Native接口的查找与定位

AssetManager的init方法，通过跳转是无法找到的。通常JNI的命名是`com_android_xxx_xxx_init()`这种。但通过这种方式也是无法找到对应的JNI入口。

这是因为，这部分JNI使用了注册的方式，使用函数指针绑定，所以只能找到对应的注册列表，否则无法找到真正的实现。

AssetManager的jni的实现在`android_util_AssetManager.cpp`里面。

```java
// android_util_AssetManager.cpp
static void android_content_AssetManager_init(JNIEnv* env, jobject clazz, jboolean isSystem)
{
    if (isSystem) {
        verifySystemIdmaps();
    }
    AssetManager* am = new AssetManager();
    if (am == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "");
        return;
    }

    am->addDefaultAssets();

    ALOGV("Created AssetManager %p for Java object %p\n", am, clazz);
    env->SetLongField(clazz, gAssetManagerOffsets.mObject, reinterpret_cast<jlong>(am));
}
```

jni注册绑定也在同一个文件：

```c++
// android_util_AssetManager.cpp
/*
 * JNI registration.
 */
static const JNINativeMethod gAssetManagerMethods[] = {
/* name, signature, funcPtr */
   // Bookkeeping.
   { "init",           "(Z)V",
       (void*) android_content_AssetManager_init },
   { "destroy",        "()V",
       (void*) android_content_AssetManager_destroy },
   { "getGlobalAssetCount", "()I",
       (void*) android_content_AssetManager_getGlobalAssetCount },
   { "getAssetAllocations", "()Ljava/lang/String;",
       (void*) android_content_AssetManager_getAssetAllocations },
   { "getGlobalAssetManagerCount", "()I",
       (void*) android_content_AssetManager_getGlobalAssetManagerCount },
};
```

可以看到：`init`方法对应的实现是`android_content_AssetManager_init`。以此类推，`AssetManager`其它native方法都在这里。

```c++
// android_util_AssetManager.cpp
int register_android_content_AssetManager(JNIEnv* env)
{
	jclass typedValue = FindClassOrDie(env, "android/util/TypedValue");
	jclass assetManager = FindClassOrDie(env, "android/content/res/AssetManager");
	jclass stringClass = FindClassOrDie(env, "java/lang/String");
	return RegisterMethodsOrDie(env, "android/content/res/AssetManager", gAssetManagerMethods,
                                NELEM(gAssetManagerMethods));
}
```

以上是注册入口，通过`AndroidRuntime.cpp`进行注册。

```java
// AndroidRuntime.cpp
namespace android {
	/*
	 * JNI-based registration functions.  Note these are properly contained in
	 * namespace android.
	 */
	extern int register_android_app_admin_SecurityLog(JNIEnv* env);

	extern int register_android_content_AssetManager(JNIEnv* env);

	extern int register_android_util_Log(JNIEnv* env);
	extern int register_android_graphics_Canvas(JNIEnv* env);
	extern int register_android_view_Surface(JNIEnv* env);
	extern int register_android_database_SQLiteConnection(JNIEnv* env);
	extern int register_android_os_SystemProperties(JNIEnv *env);
	extern int register_android_net_NetworkUtils(JNIEnv* env);
	extern int register_android_text_AndroidCharacter(JNIEnv *env);
}
```

可见，`AndroidRuntime.cpp`是非常核心的，负责注册一系列的native服务。包括：

* android.content.*
* android.util.*
* android.graphics.*
* android.view.*
* android.os.*
* 等等

> 如果要找一个JAVA层的jni实现，可以在`AndroidRuntime.cpp`里搜索。其次，搜索`RegisterMethodsOrDie`找到注册入口。

#### AssetManager的初始化过程

看回`AssetManager`的初始化：

```java
// android_util_AssetManager.cpp
static void android_content_AssetManager_init(JNIEnv* env, jobject clazz, jboolean isSystem)
{
    if (isSystem) {
        verifySystemIdmaps();
    }
	// A.
    AssetManager* am = new AssetManager();
    if (am == NULL) {
        jniThrowException(env, "java/lang/OutOfMemoryError", "");
        return;
    }

    // B.
    am->addDefaultAssets();

    ALOGV("Created AssetManager %p for Java object %p\n", am, clazz);

	// C.
    env->SetLongField(clazz, gAssetManagerOffsets.mObject, reinterpret_cast<jlong>(am));
}
```

A. 构建一个c++层的`AssetManager对象`。对应`AssetManager.cpp`。
B. 为c++层的`AssetManager对象`添加默认的assets。
C. **把c++层的AssetManager对象指针，存储到java层的AssetManager.mObject属性当中。** 这是native编程常见的策略。

```java
// AssetManager.java
// For communication with native code.
// 为了和native层通信
private long mObject;
```

然后在native层，将c++的AssetManager对象指针取出来：

```java
// this guy is exported to other jni routines
AssetManager* assetManagerForJavaObject(JNIEnv* env, jobject obj)
{
    jlong amHandle = env->GetLongField(obj, gAssetManagerOffsets.mObject);
	// 将long型转化为C++对象指针。
    AssetManager* am = reinterpret_cast<AssetManager*>(amHandle);
    if (am != NULL) {
        return am;
    }
    jniThrowException(env, "java/lang/IllegalStateException", "AssetManager has been finalized!");
    return NULL;
}
```

> c++层，主要是操作c++层的`AssetManager对象`，完成一系列资源管理。

C++层AssetManager对象的构造:

```java
// AssetManager.cpp
AssetManager::AssetManager() :
        mLocale(NULL), mResources(NULL), mConfig(new ResTable_config) {
    int count = android_atomic_inc(&gCount) + 1;
    if (kIsDebug) {
        ALOGI("Creating AssetManager %p #%d\n", this, count);
    }
    memset(mConfig, 0, sizeof(ResTable_config));
}
```

c++层的AssetManager，主要有`Locale`,`Resources`,`Config`：

* char* mLocale;
* ResTable* mResources;
* ResTable_config* mConfig;

构建很简单，不需要累赘。接下来是`am->addDefaultAssets();`添加默认的资源。

```java
// AssetManager.cpp

// A.
static const char* kSystemAssets = "framework/framework-res.apk";

bool AssetManager::addDefaultAssets()
{
    const char* root = getenv("ANDROID_ROOT");
    LOG_ALWAYS_FATAL_IF(root == NULL, "ANDROID_ROOT not set");

    String8 path(root);
    path.appendPath(kSystemAssets);

    return addAssetPath(path, NULL, false /* appAsLib */, true /* isSystemAsset */);
}

// B.
bool AssetManager::addAssetPath() {
	if (mResources != NULL) {
		appendPathToResTable(ap, appAsLib);
	}
	return true;
}

```

A. SystemAssets系统资源是`framework/framework-res.apk`(系统内置的颜色、图片、文字等等)。

B. 添加系统资源的时候，如果`mResources`不为空，则添加。很可惜，构建的时候，`mResources=NULL`。所以不会执行。

以上分析，也可以得到和文档注释一致的描述：构建AssetManager时，只是带上了系统资源，并不会有应用相关的资源。

#### 应用资源的加载时机

上面分析得到，AssetManager空构造方法时，只是带上系统资源，不会带上应用资源。

为了分析应用资源何时加载，需要清楚，上面代码中`mResources`何时进行初始化。

跟踪`mResources`的构建，是在`AssetManager::getResTable`，且如果一旦构建了，就不再构建。其中，`mResources`是`ResTable`对象（资源表）。

```java
// AssetManager.cpp
const ResTable* AssetManager::getResTable(bool required) const
{
    ResTable* rt = mResources;
    if (rt) {
        return rt;
    }
	mResources = new ResTable();
	return mResources;
}
```

`getResTable`的唯一调用在`AssetManager::getResources`：

```java
// AssetManager.cpp
const ResTable& AssetManager::getResources(bool required) const
{
    const ResTable* rt = getResTable(required);
    return *rt;
}
```

而`AssetManager::getResources`的所有调用都在`android_util_AssetManager.cpp`里面：

```java
static jstring android_content_AssetManager_getResourcePackageName()
{
	AssetManager* am = assetManagerForJavaObject(env, clazz);
	if (am == NULL) {
		return NULL;
	}
	ResTable::resource_name name;
	if (!am->getResources().getResourceName(resid, true, &name)) {
		return NULL;
	}
	return NULL;
}
```

`am->getResources()`是c++层的`AssetManager::getResources()`。

以上，可以总结，`mResources`的构建，是用时初始化。只要外部的接口，用到`mResources`，就会构建。

从较上面的分析已经知道，`android_util_AssetManager.cpp`，是java层`AssetManager.java`的native层接口实现。

总结调用过程：

A. java层创建`AssetManager`对象。创建的同时，调用`android_util_AssetManager`里面的native方法，创建一个c++层的`AssetManager`对象。

B. c++层`AssetManager`构建时，会加载系统内置的资源`framework/framework-res.apk`。

C. java层调用`AssetManager`的接口时，才会调用到c++层的`getResources`方法，这时才创建`ResTable对象`（资源表），即`mResources`成员。

D. java层通过`AssetManager`修改或访问c++层的`ResTable`资源表，实现上层的资源管理。

所以，如果关注点不在native层，可以只看java层的`AssetManager`就足够了，底层只是一个数据管理。

所以，我们的关注点又回到上层，看上层是如何操作`AssetManager`的。或者更更上层，看是如何操作`ResourcesManager`的。

#### 盒子模型

像`AssetManager`这种，只是提供接口，主要由外部调用它的接口来完成一系列操作的模型，个人称之为`盒子模型`。

理解`盒子模型`，可以用容器，独立的模块封装，独立的数据管理模块，扯线木偶等等一系列来理解。这些都可以称为`盒子模型`。

`盒子模型`的特点：

* 高度模块化，没有和外部复杂的联系。
* 接口单一，对外提供对盒子内部增删改查的接口。
* 高度复用性和可塑性，取决于外部对盒子的定制。
* 不直接提供服务，由外部包装提供服务（因为它需要定制后才能提供服务）。
* 被动提供服务，非主动。

类比`Resources`的创建流程：

1. `ResourcesManager`构建一个`ResourcesImpl`。

2. `ResourcesImpl`构建时，内部会构建`AssetManager`这个盒子。

3. 当获取`Activity:getResources()`时，创建一个`Resources`，并绑定`ResourcesImpl`，然后返回。（延长初始化）

4. 使用`Resources:xxx`时，调用的是`ResourcesImpl`，`ResourcesImpl`则在内部操作`AssetManager`这个盒子来产生结果。

5. `AssetManager`就像扯线木偶一样，被动提供服务。

#### AssetManager与resources.arsc

AssetManager的构造过程。

```java
// ResourcesManager.java
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
	return impl;
}
```

以上，就是`AssetManager`的构造过程。大致流程如下：

```java
- new AssetManager()
  - android_util_AssetManager:init()
     - am = new C++ AssetManager()
	     - 空构造，空操作
	 - am.addDefaultAssets();
	     - root = getenv("ANDROID_ROOT")
		 - path = path(root).appendPath("framework/framework-res.apk")
		 - addAssetPath(path)
		     - mAssetPaths.add(asset_path)
			 // 注：这里只是修改了mAssetPaths数组，并没有进行加载！！因为初始化时
			 // mResources为空，不需要执行appendPathToResTable。
	 - AssetManager:mObject = am;
	 - 完成初始化。
```

上面已经分析过了，
C++ AssetManager中，`mResources`不为空，只出现在`getResTable(bool required)`，
而`getResTable`只在`AssetManager::getResources(bool required)`中调用，
而`AssetManager::getResources(bool required)`只在`android_util_AssetManager.cpp`中调用到，这里几乎每个方法都会调用到`getResources`。
而`android_util_AssetManager.cpp`是java层`AssetManager`的native实现。所以跟踪java层`AssetManager`在构建后（构建的时候并不会加载），首次调用的是那个接口，就是第一次真正加载资源表的时机。

所以，又回到java层的`AssetManager`。

由于`AssetManager`主要通过`ResourcesImpl`，所以，看一下`ResourcesImpl`是何时首次调用`AssetManager`，什么时候第一次调用，就是首次加载资源表。

跟踪上面创建完`AssetManager`的代码，接下来就是创建`ResourcesImpl`

```java
- new ResourcesImpl(assets, dm, config, daj);
   - mAssets = assets;
   - mAssets.ensureStringBlocks();
        - makeStringBlocks()
		     // 初始化字符串Block数组，对应AssetManager.mStringBlocks[]属性。
		     - mStringBlocks = new StringBlock[num];
			 - mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
			       - native getNativeStringBlock(i)
				   - 执行到android_util_AssetManager.cpp:getNativeStringBlock
				        - am->getResources().getTableStringBlock(block)
						- getResTable(true)
						    - 第一次初始化c++层的mResources资源表。
						    - mResources = new ResTable();
							- empty如果为true，则表示初始化出错了，resources.arsc找不到。
							- 或者表示，是系统资源。
						    - empty为true，会设置mResources = NULL
							- bool empty = appendPathToResTable()
							      - 如果isSystemOverlay为true，则直接返回true，不进行加载。
							- 由于第一次是系统资源，所以为emtpy，所以会重新设置mResources = NULL。
							- 其实没有进行初始化！空欢喜一场。

```

> 注：AssetManager.cpp里面，getResources(bool required = true)，是一个包含默认值的函数。getResources()表示required为true。

通过上面的分析，发现要真正初始化，必须对`mAssetPaths`添加`isSystemOverlay=false`的`asset_path`才行！！

然后发现，`AssetManager`以下方法，才可能添加到`isSystemOverlay=false`的资源：

* AssetManager::openDir
* AssetManager::openNonAssetDir
* AssetManager::open
* AssetManager::openNonAsset
* AssetManager::addAssetPath
* AssetManager::addOverlayPath

因为以上方法，没有对`isSystemOverlay`进行赋值。
而像`AssetManager::addSystemOverlays`则在里面强制对`oap.isSystemOverlay = true`强制赋值为true。

最后，兜兜转转，发现创建`AssetManager`时，就调用了`AssetManager`的一系列添加方法，添加资源！！！

```java
// ResourcesManager.java
protected @Nullable AssetManager createAssetManager(@NonNull final ResourcesKey key) {
	AssetManager assets = new AssetManager();

    // A.
	// resDir can be null if the 'android' package is creating a new Resources object.
	// This is fine, since each AssetManager automatically loads the 'android' package
	// already.
	if (key.mResDir != null) {
		// B.
		if (assets.addAssetPath(key.mResDir) == 0) {
			Log.e(TAG, "failed to add asset path " + key.mResDir);
			return null;
		}
	}

	if (key.mSplitResDirs != null) {
		for (final String splitResDir : key.mSplitResDirs) {
			if (assets.addAssetPath(splitResDir) == 0) {
				Log.e(TAG, "failed to add split asset path " + splitResDir);
				return null;
			}
		}
	}

	if (key.mOverlayDirs != null) {
		for (final String idmapPath : key.mOverlayDirs) {
			assets.addOverlayPath(idmapPath);
		}
	}

	if (key.mLibDirs != null) {
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
	}
	return assets;
}
```

A. resDir可能为null，当`android`这个包构建Resources对象时，但没关系，因为每个AssetManager已经自动加载`android`这个包。（大致意思是，这是一个通用的方法，如果当前package刚好是系统的package，那么resDir会为空。而如果非系统的包，则会默认加载系统默认的资源，所以没关系）

B. 调用`assets.addAssetPath(key.mResDir)`等添加应用的资源目录。以上代码流程为：

```java
- AssetManager.addAssetPath(path)
   - addAssetPathInternal(path, false)
   - native final int addAssetPathNative(String path, boolean appAsLib)
       - android_util_AssetManager.addAssetPath(jstring path, jboolean appAsLib)
	   - am->addAssetPath(String8(path8.c_str()), &cookie, appAsLib)
	   - AssetManager::addAssetPath(path, int32_t* cookie, appAsLib, isSystemAsset)
	   // 原型是addAssetPath(path, int32_t* cookie, bool appAsLib=false, bool isSystemAsset=false)
	   // 默认是isSystemAsset=false！！！这次可以进行初始化了！！！！
	        - Skip if we have it already. 如果路径已经存在于mAssetPaths，则直接返回true，否则添加到列表。
			- mAssetPaths.add(ap)
			- if (mResources != NULL) appendPathToResTable()
			// 非常遗憾，虽然添加成功，但由于mResources还没初始化，所以没有进行加载！！！
- AssetManager.addOverlayPath(idmapPath)
   - addOverlayPathNative(idmapPath)
   - native final int addOverlayPathNative(String idmapPath)
        - android_util_AssetManager.addOverlayPath(jstring idmapPath)
		- am->addOverlayPath(String8(idmapPath8.c_str()), &cookie);
		- AssetManager::addOverlayPath(const String8& packagePath, int32_t* cookie)
		     - 这里和上面大同小异。如果已经存在，则直接返回true，否则添加到列表中。
			 - 同样，如果mResources为NULL，仍旧不进行加载！！！
   - makeStringBlocks(mStringBlocks);
   - 这里不知道为何调用了makeStringBlocks，但得益于这个调用，mResources终于要初始化了！！！
	   - native getNativeStringBlock(i)
	   - android_util_AssetManager.cpp:getNativeStringBlock
	   	 - am->getResources().getTableStringBlock(block)
	   	 - getResTable(true)
	   		 - 第一次初始化c++层的mResources资源表。
			 - mResources = new ResTable();
			 - 遍历mAssetPaths，逐一添加到 appendPathToResTable(mAssetPaths.itemAt(i));
			 - bool empty = appendPathToResTable(asset_path)
			 	  - 由于，这次存在isSystemOverlay=false，所以可以真正初始化！
				  - 创建一个共享资源表`ResTable* sharedRes`
				  - 读取`resources.arsc`的资源，添加到`sharedRes`
				  - 将`sharedRes`添加到`mResources`
				  - mResources包含应用的各种资源了。
			 - onlyEmptyResources = onlyEmptyResources && empty;
			 - 讲解一下这个属性，只要有一个empty=false，那么onlyEmptyResources就一定为false。
			 - 也就是说，只要有一个empty=false，那么mResources就不会被设置为NULL。
			 - mResources初始化完毕！！
- 由于mResources已经初始化完毕，所以，接下来的所有添加，
  就不会因为if (mResources != NULL)而不进行真正的加载了。

```

#### 其它获取`Resources`的方法

`ApplicationPackageManager`继承自`PackageManager`实现了多个获取`Resources`的方法：

* getResourcesForActivity()
* getResourcesForApplication()

```java
// ApplicationPackageManager.java extends PackageManager.java
@Override
public Resources getResourcesForActivity(ComponentName activityName)
		throws NameNotFoundException {
	return getResourcesForApplication(
		getActivityInfo(activityName, sDefaultFlags).applicationInfo);
}

@Override
public Resources getResourcesForApplication(@NonNull ApplicationInfo app)
		throws NameNotFoundException {
	if (app.packageName.equals("system")) {
		return mContext.mMainThread.getSystemUiContext().getResources();
	}
	final boolean sameUid = (app.uid == Process.myUid());
	final Resources r = mContext.mMainThread.getTopLevelResources(
				sameUid ? app.sourceDir : app.publicSourceDir,
				sameUid ? app.splitSourceDirs : app.splitPublicSourceDirs,
				app.resourceDirs, app.sharedLibraryFiles, Display.DEFAULT_DISPLAY,
				mContext.mPackageInfo);
	if (r != null) {
		return r;
	}
	throw new NameNotFoundException("Unable to open " + app.publicSourceDir);

}

@Override
public Resources getResourcesForApplication(String appPackageName)
		throws NameNotFoundException {
	return getResourcesForApplication(
		getApplicationInfo(appPackageName, sDefaultFlags));
}
```

```java
/**
 * Creates the top level resources for the given package. Will return an existing
 * Resources if one has already been created.
 */
Resources getTopLevelResources(String resDir, String[] splitResDirs, String[] overlayDirs,
		String[] libDirs, int displayId, LoadedApk pkgInfo) {
	return mResourcesManager.getResources(null, resDir, splitResDirs, overlayDirs, libDirs,
			displayId, null, pkgInfo.getCompatibilityInfo(), pkgInfo.getClassLoader());
}
```

为当前`Package`创建最顶层的`Resources`，如果已经创建，则返回已创建的。



```java
bool AssetManager::appendPathToResTable(const asset_path& ap, bool appAsLib) const {
    // skip those ap's that correspond to system overlays
    if (ap.isSystemOverlay) {
        return true;
    }

    Asset* ass = NULL;
    ResTable* sharedRes = NULL;
    bool shared = true;
    bool onlyEmptyResources = true;
    ATRACE_NAME(ap.path.string());
    Asset* idmap = openIdmapLocked(ap);
    size_t nextEntryIdx = mResources->getTableCount();
    ALOGV("Looking for resource asset in '%s'\n", ap.path.string());
    if (ap.type != kFileTypeDirectory) {
        if (nextEntryIdx == 0) {
            // The first item is typically the framework resources,
            // which we want to avoid parsing every time.
            sharedRes = const_cast<AssetManager*>(this)->
                mZipSet.getZipResourceTable(ap.path);
            if (sharedRes != NULL) {
                // skip ahead the number of system overlay packages preloaded
                nextEntryIdx = sharedRes->getTableCount();
            }
        }
        if (sharedRes == NULL) {
            ass = const_cast<AssetManager*>(this)->
                mZipSet.getZipResourceTableAsset(ap.path);
            if (ass == NULL) {
                ALOGV("loading resource table %s\n", ap.path.string());
                ass = const_cast<AssetManager*>(this)->
                    openNonAssetInPathLocked("resources.arsc",
                                             Asset::ACCESS_BUFFER,
                                             ap);
                if (ass != NULL && ass != kExcludedAsset) {
                    ass = const_cast<AssetManager*>(this)->
                        mZipSet.setZipResourceTableAsset(ap.path, ass);
                }
            }

            if (nextEntryIdx == 0 && ass != NULL) {
                // If this is the first resource table in the asset
                // manager, then we are going to cache it so that we
                // can quickly copy it out for others.
                ALOGV("Creating shared resources for %s", ap.path.string());
                sharedRes = new ResTable();
                sharedRes->add(ass, idmap, nextEntryIdx + 1, false);
#ifdef __ANDROID__
                const char* data = getenv("ANDROID_DATA");
                LOG_ALWAYS_FATAL_IF(data == NULL, "ANDROID_DATA not set");
                String8 overlaysListPath(data);
                overlaysListPath.appendPath(kResourceCache);
                overlaysListPath.appendPath("overlays.list");
                addSystemOverlays(overlaysListPath.string(), ap.path, sharedRes, nextEntryIdx);
#endif
                sharedRes = const_cast<AssetManager*>(this)->
                    mZipSet.setZipResourceTable(ap.path, sharedRes);
            }
        }
    } else {
        ALOGV("loading resource table %s\n", ap.path.string());
        ass = const_cast<AssetManager*>(this)->
            openNonAssetInPathLocked("resources.arsc",
                                     Asset::ACCESS_BUFFER,
                                     ap);
        shared = false;
    }

    if ((ass != NULL || sharedRes != NULL) && ass != kExcludedAsset) {
        ALOGV("Installing resource asset %p in to table %p\n", ass, mResources);
        if (sharedRes != NULL) {
            ALOGV("Copying existing resources for %s", ap.path.string());
            mResources->add(sharedRes, ap.isSystemAsset);
        } else {
            ALOGV("Parsing resources for %s", ap.path.string());
            mResources->add(ass, idmap, nextEntryIdx + 1, !shared, appAsLib, ap.isSystemAsset);
        }
        onlyEmptyResources = false;

        if (!shared) {
            delete ass;
        }
    } else {
        ALOGV("Installing empty resources in to table %p\n", mResources);
        mResources->addEmpty(nextEntryIdx + 1);
    }

    if (idmap != NULL) {
        delete idmap;
    }
    return onlyEmptyResources;
}
```
