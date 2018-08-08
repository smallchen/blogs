## Android实现aidl的一些技巧

1. 使用AIDL的接口`类名`作为`ACTION`，不需要额外定义`ACTION`字符串。

```java
@Override
 public IBinder onBind(Intent intent) {
     // Select the interface to return.  If your service only implements
     // a single interface, you can just return it here without checking
     // the Intent.
     if (IRemoteService.class.getName().equals(intent.getAction())) {
         return mBinder;
     }
     if (ISecondary.class.getName().equals(intent.getAction())) {
         return mSecondaryBinder;
     }
     return null;
 }

 bindService(new Intent(IRemoteService.class.getName()),
                    mConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(ISecondary.class.getName()),
                    mSecondaryConnection, Context.BIND_AUTO_CREATE);
```

2. `android.os.DeadObjectException`也是`android.os.RemoteException`的一个子类。

ADIL的接口都需要捕获`android.os.RemoteException`，通常是指ADIL连接断开。

表现在：

* 客户端的调用接口。
* 服务端的回调接口。

##### AIDL服务断开

AIDL服务断开，主要有以下情况：

* 客户端主动`unbindService`关闭。
* 服务端挂掉。

其中，客户端主动`unbindService`断开连接，不会有`onServiceDisconnected`回调。只有服务端意外挂掉，才会有`onServiceDisconnected`通知。

从最近任务中删除，走的是正常的`Destroy`流程。

4. 同上，处理AIDL连接时，需要在两个途径释放IPC连接：



两种情况，都需要调用`unbindService`，否则下次连接时，会出现`Leaked: was originally bound`的泄漏错误。而如果多次调用`unbindService`，则会出现`IllegalArgumentException：Service not registered`错误。

所以`unbindService`的处理需要很谨慎。**最好，对其捕获异常！！**

```java
public void onServiceDisconnected(ComponentName name) {
    Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
    Log.i(TAG, "## ServiceDisconnected");

    /** When disconnected, no more unregister through IPC */

    // 1. unbind and mark IPC has broken
    mContext.unbindService(mServiceConnection);
    mModuleServer = null;
    // 2. notify upwards
    IClientModule[] modules = mClientModules.values().toArray(new IClientModule[mClientModules.size()]);
    for (IClientModule module : modules) {
        module.onDestroy();
    }
    // 3. clear
    mClientModules.clear();
}

public void destroy() {
    Log.i(TAG, "## Destroy()");
    Log.i(TAG, "## modules size: "+mClientModules.size());
    Log.i(TAG, "## ##");
    if (mModuleServer != null) {
        // 1. unregister by IPC
        IClientModule[] modules = mClientModules.values().toArray(new IClientModule[mClientModules.size()]);
        for (IClientModule module : modules) {
            unregisterModule(module);
        }
        // 2. real broken the IPC and mark IPC has broken
        mContext.unbindService(mServiceConnection);
        mModuleServer = null;
        // 4. clear
        mClientModules.clear();
    }
}
```

5.
