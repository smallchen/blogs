## Android 打印调试消息队列信息

`Looper.myLooper().dump(new LogPrinter(Log.VERBOSE, TAG), "--");`
或
`Looper.myLooper().setMessageLogging(new LogPrinter(Log.VERBOSE, TAG));`


输出样例：

```java
V: --Looper (main, tid 2) {2272640}
V: --  Message 0: { when=-237ms what=115 obj=ServiceArgsData{token=android.os.BinderProxy@af3d0d4 startId=1 args=Intent { cmp=com.jokin.framework.moduleb/.ModuleBService }} target=android.app.ActivityThread$H }
V: --  Message 1: { when=-231ms barrier=2 }
V: --  Message 2: { when=-229ms what=109 arg1=1 obj=android.os.BinderProxy@74e9e36 target=android.app.ActivityThread$H }
V: --  Message 3: { when=-208ms callback=android.view.Choreographer$FrameDisplayEventReceiver target=android.view.Choreographer$FrameHandler }
V: --  Message 4: { when=-207ms callback=android.app.ActivityThread$StopInfo target=android.app.ActivityThread$H }
V: --  Message 5: { when=-147ms what=2 target=android.hardware.display.DisplayManagerGlobal$DisplayListenerDelegate }
V: --  Message 6: { when=-94ms what=118 obj={1.0 310mcc260mnc [en_US] ldltr sw360dp w592dp h336dp 480dpi nrml land finger qwerty/v/v -nav/h appBounds=Rect(0, 0 - 1776, 1080) s.73} target=android.app.ActivityThread$H }
V: --  Message 7: { when=-10ms callback=android.app.LoadedApk$ServiceDispatcher$RunConnection target=android.app.ActivityThread$H }
V: --  Message 8: { when=+8s264ms what=132 target=android.app.ActivityThread$H }
V: --  (Total messages: 9, polling=false, quitting=false)
```

```java
>>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 104
D: onStop() called
I: Skipped 33 frames!  The application may be doing too much work on its main thread.
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} null
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 140
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} null
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 114
D: onCreate() called
I: ## Init()
D: getExplicitIntent() called with: implicitIntent = Intent { act=framework.server.service.main }, explicitIntent = Intent { act=framework.server.service.main cmp=com.jokin.framework.cwindowframework/com.jokin.framework.moduleserver.ModuleCenterService }
D: onBind() called with: remote = [Intent { act=framework.server.service.main cmp=com.jokin.framework.cwindowframework/com.jokin.framework.moduleserver.ModuleCenterService (has extras) }]
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 115
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} null
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 109
D: gralloc_alloc: Creating ashmem region of size 622592
D: eglMakeCurrent: 0x94c65ce0: ver 3 0 (tinfo 0x94c74830)
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} null
V: >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {e1e2341} android.view.Choreographer$FrameDisplayEventReceiver@6d900e6: 0
V: <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {e1e2341} android.view.Choreographer$FrameDisplayEventReceiver@6d900e6
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} android.app.ActivityThread$StopInfo@dd2f979: 0
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} android.app.ActivityThread$StopInfo@dd2f979
D: eglMakeCurrent: 0x94c65ce0: ver 3 0 (tinfo 0x94c74830)
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} android.app.LoadedApk$ServiceDispatcher$RunConnection@76d46be: 0
D: onServiceConnected() called with: name = [ComponentInfo{com.jokin.framework.cwindowframework/com.jokin.framework.moduleserver.ModuleCenterService}], service = [android.os.BinderProxy@a09331f]
I: ## ServiceConnected
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} android.app.LoadedApk$ServiceDispatcher$RunConnection@76d46be
V: >>>>> Dispatching to Handler (android.app.ActivityThread$H) {9534fc5} null: 119
V: <<<<< Finished to Handler (android.app.ActivityThread$H) {9534fc5} null
```

```java
public static final int CREATE_SERVICE          = 114;  // onCreate()
public static final int SERVICE_ARGS            = 115;  // onStartCommand()
public static final int STOP_SERVICE            = 116;  // onDestroy()
```

其中，`114`表示`CREATE_SERVICE`，即`onCreate`例程；`115`表示`SERVICE_ARGS`，即`onStartCommand`例程。
