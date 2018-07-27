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

3. 当ADIL服务断开，客户端会有`onServiceDisconnected`通知。即使由于意外，服务端进程挂掉。**除非**，客户端也同时挂掉。

4. 
