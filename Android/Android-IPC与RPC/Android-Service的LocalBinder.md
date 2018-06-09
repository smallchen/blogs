## Service Local Binder

看过aidl入门后，一定会疑惑，之前Service也有一个`Binder`，且不需要繁杂的`aidl`定义文件，就可以轻松实现与本地Service的通信。

### Local Binder实现

#### 服务端

1.创建一个Service。(比如`LocalService`)

```java
public class LocalService extends Service {
    private static final String TAG = "LocalService";
}
```

2.创建一个继承了`Binder`的自定义类。放哪都行。（比如`LocalBinder`)

```java
public class LocalService extends Service {
    private static final String TAG = "LocalService";

    public class LocalBinder extends Binder {
        LocalService getLocalService() {
            return LocalService.this;
        }
    }
}
```
> 内部类，可以直接访问`LocalService`对象。

3.通过`onBind()`将`LocalBinder`返回给客户端。（这里每次new一个Binder，AndroidDemo里，是只创建一个，每次返回同一个对象）

```java
@Override
public IBinder onBind(Intent intent) {
	// TODO: Return the communication channel to the service.
	return new LocalBinder();
}
```

#### 客户端

1.使用`Intent`绑定服务。

```java
Intent intent = new Intent(this, LocalService.class);
bindService(intent, mLocalServiceConnection, BIND_AUTO_CREATE);
```

或
```java
private static final String PACKAGE_NAME = "com.jokin.demo.aidl.server";
private static final String LOCAL_ACTION_SERVICE = PACKAGE_NAME+".LocalService";

Intent intentService = new Intent();
intentService.setComponent(new ComponentName(PACKAGE_NAME, LOCAL_ACTION_SERVICE));
bindService(intentService, mLocalServiceConnection, BIND_AUTO_CREATE);
```

2.关键的`ServiceConnection`回调。其中，参数中的`IBinder`指向的就是`LocalService`中`onBind()`返回的Binder对象。（这里直接强转）

```java
private LocalService mLocalService;
private ServiceConnection mLocalServiceConnection = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		LocalService.LocalBinder localBinder = (LocalService.LocalBinder) service;
		mLocalService = localBinder.getLocalService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mLocalService = null;
	}
};
```

3.得到`Binder实例`后，就可以通过实例的方法，得到整个`LocalService`对象。整个对象都有了，接下来想干什么都可以。

4.为`LocalService`添加全局方法。

```java
public class LocalService extends Service {
    private static final String TAG = "LocalService";

    public void open(int color, int size) {
        Log.e(TAG, String.format("open with color=%d size=%d", color, size));
    }

    public void close(String tip) {
        Log.e(TAG, "close with tip:" + tip);
    }
}
```

5.在客户端直接访问。

```java
findViewById(R.id.closeActionLocal).setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View v) {
		if (mLocalService == null) {
			return;
		}
		mLocalService.close("本地要关闭拉！");
	}
});
```

### 总结

1. Binder对象只是起了桥梁的作用。通过Binder这个桥梁。客户端可以访问服务端，甚至可以拿到服务端整个实例。

2. 本地Binder的线程问题，和本地调用一样。在UI线程调用则在UI线程执行。在多线程中调用，则在多线程环境下执行。

参考：<https://developer.android.com/guide/components/bound-services?hl=zh-cn#Binder>
