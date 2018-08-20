<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android-在Service中使用Looper.loop()导致ServiceANR的问题分析](#android-在service中使用looperloop导致serviceanr的问题分析)
- [现象](#现象)
- [分析](#分析)
- [总结](#总结)
- [为何不用Thread.sleep或锁等待？](#为何不用threadsleep或锁等待)

<!-- /TOC -->

## Android-在Service中使用Looper.loop()导致ServiceANR的问题分析

## 现象

先看代码：

```java
private void init() {
    Intent intentService = new Intent(mContext);
    mContext.bindService(intentService, mServiceConnection, Context.BIND_AUTO_CREATE);
    Log.i(TAG, "## wait.");
    try {
        // 等待Service启动完毕
        Looper.loop();
    } catch (Exception e) {
        Log.i(TAG, "## pass.");
    }
}

private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "## ServiceConnected");
        mServer = IModuleServer.Stub.asInterface(service);
        // 让代码继续往下执行
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
};
```

以上，引入`Looper.loop()`来实现这样一个`主线程等待`的功能：

* 当前代码块阻塞，直到条件准备完毕才往下执行。
* 只阻塞当前代码块，不阻塞主线程。

效果相当于`模态对话框`、`阻塞式对话框`。

输出为：

```java
## wait.
## ServiceConnected
## pass.
```

原以为，这个实现可以行得通。但发现Service的`onStartCommand()`并没有被回调。然后等待`20s`后，就出现`ANR`，提示`Timeout executing service`。

## 分析

分析在另一篇《Service启动过程错误》已经有提及。以下有重复部分，只是为了更加清晰的理清这个问题。

全局搜索源码`Timeout executing service`，就能发现抛出错误的地方。

Service的启动流程，大致是这样：

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

1、首先，Service创建的时候，就已经先后发送了两个`Runnable`，分别为`H.CREATE_SERVICE`和`H.SERVICE_ARGS`，对应`onCreate()`和`onStartCommand()`例程。所以，执行`onCreate`的时候，`onStartCommand`已经在`MeesageQueue`消息队列里面。

2、在`onCreate`里面调用了`Looper.loop()`，阻塞的是`onCreate`这个Runnable，而不会阻塞整个消息队列。所以，相当于`onCreate`阻塞，`onStartCommand`照常执行。

3、通过查看`onStartCommand`源码，参见另一篇《Service启动过程错误源码详解》，入口方法为：

```java
// ActivityThread.java
private void handleServiceArgs(ServiceArgsData data) {
    Service s = mServices.get(data.token);
    if (s != null) {
        try {
            // 1. onStartCommand()
            res = s.onStartCommand(data.args, data.flags, data.startId);
            // 2. Service started finish
            ActivityManager.getService().serviceDoneExecuting(
                    data.token, SERVICE_DONE_EXECUTING_START, data.startId, res);
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
```

由于Service的`onCreate()`例程被阻塞，所以，`mServices.put(data.token, service)`未能执行。继而导致`mServices.get(data.token)`得到的是`null`，无法进入到`onStartCommand()`，导致Service的`onStartCommand()`不会调用。（mServices的作用：用来存储当前已创建的Services）

由于Service启动流程不完整，Android在一段时间后，监控`r.app.executingServices`数组，发现Service还在，就会报`Timeout executing service`异常，同时抛出`ANR`。（app.executingServices的作用：用来检测Service启动是否Timeout）

## 总结

回过头来，主要的点是：

* 消息队列 MessageQueue(Runnable)
* 消息循环 Looper
* 消息处理 Runnable

以上代码例子，可以简化为：

```java
// Looper
@ Looper.loop()
- for ;;
    - Runnable = MessageQueue.next() // 阻塞
    - Runnable.run()
        - MessageQueue.add(Runnable A)
        - MessageQueue.add(Runnable B)
        - Looper.loop()
            @ Looper.loop()
            - for ;;
                - Runnable = MessageQueue.next() // 阻塞
                - Runnable.run()
        - Do things C;
        - Do things D;
```

如上，

1. 在消息处理例程中，可以继续往MessageQueue消息队列里添加新的例程。
2. 在消息处理的例程中，消息例程自己也可以创建了一个内嵌的消息循环。由于消息队列是全局的，所以消息循环能够保持运行。但当前例程之后的代码将被阻塞。
3. 当MessageQueue消息队列没有消息，next()将被阻塞，此时，只能通过另一个线程添加新的Runnable例程。
4. Android中的ANR，其实只是检测Runnable例程的执行时间，并不是检测消息循环是否阻塞。

针对以上的消息队列先后问题：

最根本的原因，是Android四大组件的生命周期回调，基本都是通过提前往消息队列里添加生命周期的例程来实现。通俗的说，Android四大组件的生命周期基本不是直接调用，而是将例程丢到消息队列中，等待被调用。而有时候，Android甚至会一次性就把所有生命周期例程都先添加到消息队列中，然后由消息循环逐一调用。

一般情况下，消息队列里的消息，是先添加，先执行的（FIFO）。**Android也利用这一点来实现各大组件的生命周期的先后调用关系**。

所以，Android中，可以使用`post`来实现`尽快执行`的功能。其实就是将例程添加到消息队列尾部，在保证调用次序的前提下，尽可能快地执行。这种方式通常使用在Activity、View等组件的创建过程，用于保证在组件的生命周期执行完毕后才执行当前方法，保证方法可以安全访问到组件。

另一点，对于Android提供的生命周期的回调例程内，不要做影响到例程执行次序的操作，比如上面的`Looper.loop()`；也尽量不要做影响例程执行时间的操作，比如`Thread.sleep()`、`wait()`、`lock()`等等。（前者大部分会出问题；后者短暂阻塞还是问题不大）

最后，以上问题的解决方案？

说到底，不过是要了解Service启动过程，Android内部何时添加例程到消息队列。通过源码可以知道，`onStartCommand`后，消息队列里就没有Service相关的例程。所以，上面的`Looper.loop()`可以放在`onStartCommand`里，而不是放在`onCreate()`里。

把`Looper.loop()`放到生命周期最后的一个回调里，可以避免影响到Android原生的生命周期回调。

## 为何不用Thread.sleep或锁等待？

这个问题很简单。使用sleep或者锁，是阻塞当前线程。在主线程使用，就是阻塞主线程。而Android大部分组件的创建，都是在主线程中执行的。在启动Service后加锁，等待Service启动后解锁是不可能的。主线程加锁等待后，整个主线程就锁死了，Service等组件根本没办法启动。

当一个线程要等待同一个线程的信号，不能使用阻塞式等待方式。

所以，才需要保证主线程的消息循环活动的基础上，阻塞当前某个例程。

好了，不扯了。
