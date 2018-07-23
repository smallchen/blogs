<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android IntentFilter](#android-intentfilter)
	- [Activity使用IntentFilter跳转](#activity使用intentfilter跳转)
		- [声明](#声明)
		- [启动](#启动)
	- [Service使用IntentFilter跳转](#service使用intentfilter跳转)
		- [声明](#声明)
		- [启动](#启动)

<!-- /TOC -->

## Android IntentFilter

### Activity使用IntentFilter跳转

#### 声明

```java
<intent-filter>
	<action android:name="framework.server.activity.main"/>
	<category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

1. 必须添加`category`DEFAULT。否则，会报`ActivityNotFoundException`。
2. Activity必须为`android:exported="true" android:enabled="true"`。

#### 启动

```java
Intent intent = new Intent("framework.server.activity.main");
// intent.addCategory("android.intent.category.DEFAULT");
// intent.addCategory(Intent.CATEGORY_DEFAULT);
startActivity(intent);
```

1. 只提供Action就可以启动。

### Service使用IntentFilter跳转

#### 声明

```java
<intent-filter>
	<action android:name="framework.server.service.main"/>
</intent-filter>
```

1. Service必须为`android:exported="true" android:enabled="true"`。

#### 启动

由于Android5.0（API22）以上，启动Service必须显式启动，所以需要借助下面方法：

```java
//将隐式启动转换为显式启动,兼容编译sdk5.0以后版本
private static Intent getExplicitIntent(Context context, Intent implicitIntent) {
    PackageManager pm = context.getPackageManager();

    List<ResolveInfo> resolveInfos = pm.queryIntentServices(implicitIntent, 0);
    if (resolveInfos == null || resolveInfos.size() != 1) {
        Log.d(TAG, "getExplicitIntent: ResolveInfos is empty!");
        return null;
    }

    ResolveInfo info = resolveInfos.get(0);
    String packageName = info.serviceInfo.packageName;
    String className = info.serviceInfo.name;

    Intent explicitIntent = new Intent(implicitIntent);
    ComponentName component = new ComponentName(packageName, className);
    explicitIntent.setComponent(component);
    Log.d(TAG, "getExplicitIntent() called with: implicitIntent = " + implicitIntent
            + ", explicitIntent = " + explicitIntent);
    return explicitIntent;
}
```

1. 由于上面方法使用的是`queryIntentServices`，所以，只是对于`Service`的隐式转显式。同理，`Activity`的隐式转显式也可以参考实现。
2. 缺陷是：以上对Intent默认使用第一个是不严谨的！！由于ACTION是暴露出去的，如果有多个支持同一个ACTION的服务（比如别人冒充），那么默认使用第一个是错误的，可能会启动了别人冒充的服务，而不是自己的服务。

但：不是必要，不建议查询。因为会多了查询操作。交给Android默认处理也一样。
