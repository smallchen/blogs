## Android Service启动过程错误源码详解

`Waited long enough for`

全局搜索：

```java
// ActiveSerivces.java
void rescheduleDelayedStartsLocked() {
    removeMessages(MSG_BG_START_TIMEOUT);
    final long now = SystemClock.uptimeMillis();
    for (int i=0, N=mStartingBackground.size(); i<N; i++) {
        ServiceRecord r = mStartingBackground.get(i);
        if (r.startingBgTimeout <= now) {
            Slog.i(TAG, "Waited long enough for: " + r);
            mStartingBackground.remove(i);
            N--;
            i--;
        }
    }
}
```

主要靠`MSG_BG_START_TIMEOUT`来识别。

主要列表是`mStartingBackground`。

```java
@ ActiveSerivces:startServiceInnerLocked()
// Post message 来启动 Service
- bringUpServiceLocked()
// 添加 ServiceRecord 到 ArrayList<ServiceRecord> 列表中（全局唯一一处添加！！）
- smap.mStartingBackground.add(r);
```



`Timeout executing service`

```java
// ActiveSerivces.java
void serviceTimeout(ProcessRecord proc) {
    ServiceRecord timeout = null;
    long nextTime = 0;
    // 遍历`executingServices`
    for (int i=proc.executingServices.size()-1; i>=0; i--) {
        ServiceRecord sr = proc.executingServices.valueAt(i);
        if (sr.executingStart < maxTime) {
            timeout = sr;
            break;
        }
        if (sr.executingStart > nextTime) {
            nextTime = sr.executingStart;
        }
    }
    if (timeout != null && mAm.mLruProcesses.contains(proc)) {
        Slog.w(TAG, "Timeout executing service: " + timeout);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
        pw.println(timeout);
        timeout.dump(pw, "    ");
        pw.close();
        mLastAnrDump = sw.toString();
        mAm.mHandler.removeCallbacks(mLastAnrDumpClearer);
        mAm.mHandler.postDelayed(mLastAnrDumpClearer, LAST_ANR_LIFETIME_DURATION_MSECS);
        anrMessage = "executing service " + timeout.shortName;
    }
}
```

必须调用到`serviceDoneExecutingLocked()`才能删除`executingServices`里的元素，才表示`Service`启动完毕。

添加的时候：当Service启动时，会添加到`executingServices`数组里。

```java
@ ActionService:realStartServiceLocked()
- bumpServiceExecutingLocked(r, execInFg, "create");
- app.thread.scheduleCreateService()
    - sendMessage(H.CREATE_SERVICE, s);
- sendServiceArgsLocked(r, execInFg, true);
    - r.app.thread.scheduleServiceArgs(r, slice);
    - - sendMessage(H.SERVICE_ARGS, s);

@ ActiveSerivces:bumpServiceExecutingLocked(ServiceRecord r)
// 全局唯一
- r.app.executingServices.add(r);
```

删除的时候，必须调用上面的`serviceDoneExecutingLocked()`。`serviceDoneExecutingLocked()`有很多处调用。但启动Service过程，是由`ActivityManager.getService().serviceDoneExecuting()`来完成启动的。

流程如下：

```java
@ ActivityThread:
- sendMessage(H.CREATE_SERVICE, s);
- sendMessage(H.SERVICE_ARGS, s);

// 添加
- r.app.executingServices.add(r);
@ ActivityThread:handleCreateService()
    - service.onCreate();
    - mServices.put(data.token, service);

@ ActivityThread:handleServiceArgs(ServiceArgsData data)
    - Service service = mServices.get(data.token);
    - service != null ?
    T service.onStartCommand()
        - ActivityManager.getService().serviceDoneExecuting()
            - mServices.serviceDoneExecutingLocked((ServiceRecord)token)
            - @ ActiveServices:serviceDoneExecutingLocked((ServiceRecord))
                // 删除
                - r.app.executingServices.remove(r);
```

如果`onStartCommand()`没有顺利执行下去，那么，就会报`Timeout executing service: `，然后抛出`ANR`。


```java
// ActivityThread.java
private void handleServiceArgs(ServiceArgsData data) {
    Service s = mServices.get(data.token);
    if (s != null) {
        try {
            if (data.args != null) {
                data.args.setExtrasClassLoader(s.getClassLoader());
                data.args.prepareToEnterProcess();
            }
            int res;
            if (!data.taskRemoved) {
                res = s.onStartCommand(data.args, data.flags, data.startId);
            } else {
                s.onTaskRemoved(data.args);
                res = Service.START_TASK_REMOVED_COMPLETE;
            }

            QueuedWork.waitToFinish();

            try {
                ActivityManager.getService().serviceDoneExecuting(
                        data.token, SERVICE_DONE_EXECUTING_START, data.startId, res);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
            ensureJitEnabled();
        } catch (Exception e) {
            if (!mInstrumentation.onException(s, e)) {
                throw new RuntimeException(
                        "Unable to start service " + s
                        + " with " + data.args + ": " + e.toString(), e);
            }
        }
    }
}

// ActivityManagerService.java
public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
    synchronized(this) {
        if (!(token instanceof ServiceRecord)) {
            Slog.e(TAG, "serviceDoneExecuting: Invalid service token=" + token);
            throw new IllegalArgumentException("Invalid service token");
        }
        mServices.serviceDoneExecutingLocked((ServiceRecord)token, type, startId, res);
    }
}
```


## Records

以上这些数组，都在`ProcessRecord`里面。其中`Service`就有`services数组`和`executingServices数组`。

```java
// ProcessRecord.java

// all activities running in the process
final ArrayList<ActivityRecord> activities = new ArrayList<>();
// all ServiceRecord running in this process
final ArraySet<ServiceRecord> services = new ArraySet<>();
// services that are currently executing code (need to remain foreground).
final ArraySet<ServiceRecord> executingServices = new ArraySet<>();
// All ConnectionRecord this process holds
final ArraySet<ConnectionRecord> connections = new ArraySet<>();
// all IIntentReceivers that are registered from this process.
final ArraySet<ReceiverList> receivers = new ArraySet<>();
// class (String) -> ContentProviderRecord
final ArrayMap<String, ContentProviderRecord> pubProviders = new ArrayMap<>();
// All ContentProviderRecord process is using
final ArrayList<ContentProviderConnection> conProviders = new ArrayList<>();
```
