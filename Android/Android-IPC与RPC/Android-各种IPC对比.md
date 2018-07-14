## Android 各种IPC对比

### Android的IPC方式

* 四大组件(Bundle)
* Binder
* Messenger
* AIDL
* SOCKET

以下的单进程，既包含`单个项目单进程`，也包含`多个项目单进程`（通过android:sharedUserId)。
以下的跨进程，既包含`单个项目多进程`（通过android:process），也包含`多个项目多进程`。

很容易忽略，**Android四大组件天然支持跨进程通信！！**，

### Bound Service的三种方式（跨Service）

* Binder（本地Binder）
* Messenger
* AIDL

对之前的笔记看过一遍，就可以知道这三者区别：

单进程：
* 都支持

跨进程：
* Messenger（只支持跨进程消息，不支持跨进程调用）
* AIDL（都支持）

并发：
* Binder（需要在多线程中调用）
* AIDL（天然支持）

同步：
* Binder
* Messenger

数据：
* 都支持

调用：
* Binder
* AIDL

这里的区别很简单，但确实就这么简单。
