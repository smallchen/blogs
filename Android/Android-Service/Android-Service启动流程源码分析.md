## Android Service 启动流程详解

`onCreate()`流程

```java
// 正常启动
- ContextImpl:startService()
   - ActivityManagerService:startService()
   - mServices.startServiceLocked()
        // ActiveSerivces: 活动的Service
        @ ActiveSerivces:startServiceLocked()
        - ServiceMap smap = getServiceMapLocked(r.userId);
        - startServiceInnerLocked()
            - bringUpServiceLocked()
                - realStartServiceLocked()
                    - app.thread.scheduleCreateService()
                    @ ActivityThread:scheduleCreateService()
                    - sendMessage(H.CREATE_SERVICE, s);
                    - case CREATE_SERVICE
                        - handleCreateService((CreateServiceData)msg.obj);
                        @ ActivityThread:handleCreateService()
                            - service.onCreate();
                            - mServices.put(data.token, service);

// 重试！
@ ActiveSerivces
case MSG_BG_START_TIMEOUT:
    - rescheduleDelayedStartsLocked()
    @ ActiveSerivces:rescheduleDelayedStartsLocked()
        - startServiceInnerLocked()

@ ActiveSerivces:startServiceInnerLocked()
- bringUpServiceLocked()

@ ActiveSerivces:bringUpServiceLocked()
- app.addPackage(r.appInfo.packageName, r.appInfo.versionCode, mAm.mProcessStats);
- realStartServiceLocked(r, app, execInFg);

@ ActiveSerivces:realStartServiceLocked()
- app.thread.scheduleCreateService()
- r.postNotification();
// 就是`onStartCommand()`!!
- sendServiceArgsLocked(r, execInFg, true);



@ ActivityThread:scheduleCreateService()
- sendMessage(H.CREATE_SERVICE, s);

@ ActivityThread
- case CREATE_SERVICE
    - handleCreateService((CreateServiceData)msg.obj);
    @ ActivityThread:handleCreateService()

@ ActivityThread:handleCreateService()
- ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
- context.setOuterContext(service);
- Application app = packageInfo.makeApplication(false, mInstrumentation);
- service.attach(context, this, data.info.name, data.token, app)
- service.onCreate();
- mServices.put(data.token, service);

@ActivityThread:handleBindService()
- IBinder binder = s.onBind(data.intent);
- ActivityManager.getService().publishService(data.token, data.intent, binder);
```

`onStartCommand()`流程

```java

// 启动
@ ActiveSerivces:realStartServiceLocked()
- sendServiceArgsLocked(r, execInFg, true);

// 前置
@ ActiveSerivces:bringUpServiceLocked()
- sendServiceArgsLocked(r, execInFg, false);

// 清理
@ ActiveSerivces:cleanUpRemovedTaskLocked()
- sendServiceArgsLocked(sr, true, false);

@ ActiveSerivces:sendServiceArgsLocked()
    - r.app.thread.scheduleServiceArgs(r, slice);
    @ ActivityThread:scheduleServiceArgs()

@ ActivityThread:scheduleServiceArgs()
- sendMessage(H.SERVICE_ARGS, s);

@ ActivityThread
case SERVICE_ARGS:
    - handleServiceArgs((ServiceArgsData)msg.obj);
    @ ActivityThread:handleServiceArgs(ServiceArgsData data)
        - Service s = mServices.get(data.token);
        - onStartCommand()
```
