## 你真的懂Android中各种对象的生命周期吗

* Application
* Service
* Broadcast
* Activity
* Fragment
* View


### Application

> Application源码很少，可以直接阅读相关注释。

启动Service／Broadcast／Activaty等时，如果Application还没启动，都会先启动Application。

关闭Service／Broadcast／Activity等时，没有任何组件，Application还不一定销毁。Application的销毁时机由系统管理。所以，一些放在Application里的对象，它的生命周期是异常长寿的。

重要回调：
* onCreate() 整个APP最先执行的回调，先于四大组件的创建。
* onTerminate() 进程被终结回调；只在虚拟机环境下生效；真机环境下，进程被终结时，不会有任何回调就直接退出。
* onLowMemory()
* onTrimMemory(int level)

### Service

> Service源码很少，可以直接阅读相关注释。

类型：
* 前台Service 受系统保护，可以一直存活
* 后台Service	不受保护，很容易被杀死

实现：
* 一次性Service
* 自定义Service

重要回调：
* onCreate() 整个Service最先执行的回调。
* onStartCommand(Intent, int, int)
* onDestroy()
* onBind(Intent intent)
* onUnbind(Intent intent)
* onRebind(Intent intent)
* onLowMemory()
* onTrimMemory(int level)

重要接口：
* stopSelf()
* stopSelfResult(int startId)
* startForeground(int id, Notification notification)
* stopForeground(boolean removeNotification)

### BroadcastReceiver

类型：
* 全局广播（跨应用）
  * 动态注册
  * 静态注册
* 本地广播（应用内）
  * 动态注册
* 顺序广播

重要回调：
* onReceive(Context context, Intent intent)

### Activity

重要回调：
* onCreate()
* onStart()
* onResume()
* onRestart()
* onPause()
* onStop()
* onDestroy()

* onNewIntent(Intent intent)
* onAttachFragment(Fragment fragment)

* onLowMemory()
* onTrimMemory(int level)
* onCreateThumbnail(Bitmap outBitmap, Canvas canvas) 自定义进程列表中的快照截图。

重要接口：
* finish()
* isFinishing()	用于在onPause时，判断是正常的onPause还是由于finish引起的onPause流程。

#### ActivityA -> B切换过程生命周期回调

1. A的`onPause()`一定先于B的`onCreate()`.
> B will not be created until A's {@link #onPause} returns,so be sure to not do anything lengthy here. (javadoc of onPause())

2.


### Service
#### Service Lifecycle

There are two reasons that a service can be run by the system. If someone calls Context.startService() then the system will retrieve the service (creating it and calling its onCreate() method if needed) and then call its onStartCommand(Intent, int, int) method with the arguments supplied by the client. The service will at this point continue running until Context.stopService() or stopSelf() is called. Note that multiple calls to Context.startService() do not nest (though they do result in multiple corresponding calls to onStartCommand()), so no matter how many times it is started a service will be stopped once Context.stopService() or stopSelf() is called; however, services can use their stopSelf(int) method to ensure the service is not stopped until started intents have been processed.

For started services, there are two additional major modes of operation they can decide to run in, depending on the value they return from onStartCommand(): START_STICKY is used for services that are explicitly started and stopped as needed, while START_NOT_STICKY or START_REDELIVER_INTENT are used for services that should only remain running while processing any commands sent to them. See the linked documentation for more detail on the semantics.

Clients can also use Context.bindService() to obtain a persistent connection to a service. This likewise creates the service if it is not already running (calling onCreate() while doing so), but does not call onStartCommand(). The client will receive the IBinder object that the service returns from its onBind(Intent) method, allowing the client to then make calls back to the service. The service will remain running as long as the connection is established (whether or not the client retains a reference on the service's IBinder). Usually the IBinder returned is for a complex interface that has been written in aidl.

A service can be both started and have connections bound to it. In such a case, the system will keep the service running as long as either it is started or there are one or more connections to it with the Context.BIND_AUTO_CREATE flag. Once neither of these situations hold, the service's onDestroy() method is called and the service is effectively terminated. All cleanup (stopping threads, unregistering receivers) should be complete upon returning from onDestroy().


#### Process Lifecycle
The Android system will attempt to keep the process hosting a service around as long as the service has been started or has clients bound to it. When running low on memory and needing to kill existing processes, the priority of a process hosting the service will be the higher of the following possibilities:

If the service is currently executing code in its onCreate(), onStartCommand(), or onDestroy() methods, then the hosting process will be a foreground process to ensure this code can execute without being killed.

If the service has been started, then its hosting process is considered to be less important than any processes that are currently visible to the user on-screen, but more important than any process not visible. Because only a few processes are generally visible to the user, this means that the service should not be killed except in low memory conditions. However, since the user is not directly aware of a background service, in that state it is considered a valid candidate to kill, and you should be prepared for this to happen. In particular, long-running services will be increasingly likely to kill and are guaranteed to be killed (and restarted if appropriate) if they remain started long enough.

If there are clients bound to the service, then the service's hosting process is never less important than the most important client. That is, if one of its clients is visible to the user, then the service itself is considered to be visible. The way a client's importance impacts the service's importance can be adjusted through BIND_ABOVE_CLIENT, BIND_ALLOW_OOM_MANAGEMENT, BIND_WAIVE_PRIORITY, BIND_IMPORTANT, and BIND_ADJUST_WITH_ACTIVITY.

A started service can use the startForeground(int, Notification) API to put the service in a foreground state, where the system considers it to be something the user is actively aware of and thus not a candidate for killing when low on memory. (It is still theoretically possible for the service to be killed under extreme memory pressure from the current foreground application, but in practice this should not be a concern.)

Note this means that most of the time your service is running, it may be killed by the system if it is under heavy memory pressure. If this happens, the system will later try to restart the service. An important consequence of this is that if you implement onStartCommand() to schedule work to be done asynchronously or in another thread, then you may want to use START_FLAG_REDELIVERY to have the system re-deliver an Intent for you so that it does not get lost if your service is killed while processing it.

Other application components running in the same process as the service (such as an Activity) can, of course, increase the importance of the overall process beyond just the importance of the service itself.
