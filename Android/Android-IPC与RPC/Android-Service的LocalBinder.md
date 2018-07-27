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

### LocalBinder使用在单项目多进程的Service如何？

上面例子，把`LocalService`放到子进程中，也就是有3中情况：

1. 同一个项目，同一个进程
2. 同一个项目，公共子进程(com.local)
3. 同一个项目，私有子进程(:local)

情况1为上面例子，LocalBinder工作正常。`onServiceConnected`中的`IBinder`是`com.jokin.demo.aidl.server.LocalService$LocalBinder`

情况2，崩溃。因为`onServiceConnected`中的`IBinder`不再是LocalBinder对象，而是`android.os.BinderProxy`。

情况3，崩溃，同上，对象不是`LocalBinder`，而是`BinderProxy`。

凡是跨进程通信，IBinder得到的都是`BinderProxy`，是一个`remote`的`Binder`。不可能得到一个完整的`LocalBinder`对象，更不可能直接通过`LocalBinder`提供的公共方法访问到在另一个进程中的`Service`!!!

### 总结

1. Binder对象只是起了桥梁的作用。通过Binder这个桥梁。客户端可以访问服务端，甚至可以拿到服务端整个实例。

2. 本地Binder的线程问题，和本地调用一样。在UI线程调用则在UI线程执行。在多线程中调用，则在多线程环境下执行。

3. **不能用于多进程Service**

参考：<https://developer.android.com/guide/components/bound-services?hl=zh-cn#Binder>
