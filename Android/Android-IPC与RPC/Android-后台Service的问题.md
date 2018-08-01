## Android 后台Service的问题

`app is in background`错误

当`ServiceB`属于第三方APP，而第三方APP进程已经结束，此时，在主APP中，使用`startService`尝试启动第三方APP的`ServiceB`，则会报`app is in background`错误！

E/NotificationService: No Channel found for pkg=com.jokin.framework.moduleb, channelId=null, id=1, tag=null, opPkg=com.jokin.framework.moduleb, callingUid=10086, userId=0, incomingUserId=0, notificationUid=10086, notification=Notification(channel=null pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x40 color=0xff607d8b vis=PRIVATE)



```java
E/AndroidRuntime: FATAL EXCEPTION: main
                  Process: com.jokin.framework.cwindowframework, PID: 1471
                  java.lang.IllegalStateException: Not allowed to start service Intent { cmp=com.jokin.framework.moduleb/.ModuleBService }: app is in background uid UidRecord{b0dbcb4 u0a86 CEM  bg:+4m2s725ms idle change:idle procs:1 seq(0,0,0)}
                      at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1521)
                      at android.app.ContextImpl.startService(ContextImpl.java:1477)
                      at android.content.ContextWrapper.startService(ContextWrapper.java:650)
                      at com.jokin.framework.cwindowframework.FullscreenActivity$2.onClick(FullscreenActivity.java:50)
                      at android.view.View.performClick(View.java:6294)
                      at android.view.View$PerformClick.run(View.java:24770)
                      at android.os.Handler.handleCallback(Handler.java:790)
                      at android.os.Handler.dispatchMessage(Handler.java:99)
                      at android.os.Looper.loop(Looper.java:164)
                      at android.app.ActivityThread.main(ActivityThread.java:6494)
                      at java.lang.reflect.Method.invoke(Native Method)
                      at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:438)
                      at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:807)
```

解决：分`Android 8.0`和`Android 8.0`以前：

I got solution. For pre-8.0 devices, you have to just use startService(), but for post-7.0 devices, you have to use startForgroundService(). Here is sample for code to start service.

```java

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    context.startForegroundService(new Intent(context, ServedService.class));
} else {
    context.startService(new Intent(context, ServedService.class));
}
```
And in service class, please add the code below for notification:

```java
@Override
public void onCreate() {
    super.onCreate();
    startForeground(1,new Notification());
}
```

如果不加`startForeground`，则会崩溃：

```java
E/ActivityManager: ANR in com.jokin.framework.moduleb
                   PID: 3488
                   Reason: Context.startForegroundService() did not then call Service.startForeground()
                   Load: 0.78 / 0.34 / 0.28
```

但上面的代码，在`Android 8.1`会有一个Toast提示：

`Developer warning for package "xxx.xxx.xxx" Failed to post notification on channel "null" See log for more details`.

Log如下：

```java
E/NotificationService: No Channel found for pkg=com.jokin.framework.moduleb, channelId=null, id=1, tag=null, opPkg=com.jokin.framework.moduleb, callingUid=10086, userId=0, incomingUserId=0, notificationUid=10086, notification=Notification(channel=null pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x40 color=0xff607d8b vis=PRIVATE);
```

这是因为，`ChannelID`没有设置。


参考：
<https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten>
<https://stackoverflow.com/questions/44489657/android-o-reporting-notification-not-posted-to-channel-but-it-is>
