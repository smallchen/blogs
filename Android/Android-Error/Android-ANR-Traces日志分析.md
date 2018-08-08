## Android anr traces 日志记录

当Android设备出现ANR，可以在以下目录找到`traces.txt`日志记录。

`/data/anr/traces.txt`

## 历史ANR记录

由于`traces.txt`只保存最近的ANR日志，其余ANR日志会添加到数据库中。

要查看历史，可以在`/data/system/dropbox`DB文件存放位置.

## ANR Log分析

引起ANR问题的根本原因，总的来说可以归纳为两类：

1. 应用进程自身引起的，例如：

* 主线程阻塞、挂起、死循环
* 应用进程的其他线程的CPU占用率高，使得主线程无法抢占到CPU时间片

2. 其他进程间接引起的，例如：

* 当前应用进程进行进程间通信请求其他进程，其他进程的操作长时间没有反馈
* 其他进程的CPU占用率高，使得当前应用进程无法抢占到CPU时间片

分析ANR问题时，以上述可能的2种原因为线索，通过分析各种日志信息，大多数情况下你就可以很容易找到问题所在了。

ANR一般有三种类型：

1. KeyDispatchTimeout(5 seconds) --主要类型按键或触摸事件在特定时间内无响应

2. BroadcastTimeout(10 seconds) --BroadcastReceiver在特定时间内无法处理完成

3. ServiceTimeout(20 seconds) --小概率类型 Service在特定的时间内无法处理完成

### 线程状态

```java
ThreadState (defined at “dalvik/vm/thread.h “)

THREAD_UNDEFINED = -1, /* makes enum compatible with int32_t */

THREAD_ZOMBIE = 0, /* TERMINATED */

THREAD_RUNNING = 1, /* RUNNABLE or running now */

THREAD_TIMED_WAIT = 2, /* TIMED_WAITING in Object.wait() */

THREAD_MONITOR = 3, /* BLOCKED on a monitor */

THREAD_WAIT = 4, /* WAITING in Object.wait() */

THREAD_INITIALIZING= 5, /* allocated, not yet running */

THREAD_STARTING = 6, /* started, not yet on thread list */

THREAD_NATIVE = 7, /* off in a JNI native method */

THREAD_VMWAIT = 8, /* waiting on a VM resource */

THREAD_SUSPENDED = 9, /* suspended, usually by GC or debugger */
```

参考：
<http://www.cnblogs.com/purediy/p/3225060.html>
