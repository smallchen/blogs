
## AIDL的调用线程问题

AIDL可以用在`本地进程`，也可以用于`跨进程`。区别如下：

* 本地进程调用（同一个应用内）

AIDL在本地进程中调用。AIDL的调用线程和执行线程是一致的。调用在UI线程，则执行也在UI线程；调用在子线程，则执行也在子线程。（官网也强调了，如果是这种情况，根本不应该使用AIDL，而应该使用本地Binder）

* 跨进程调用（不同应用内）

AIDL在跨进程中调用。调用方的线程不管。服务端的执行是分配在一个线程池中执行的。**换句话，跨进程中，AIDL的接口实现必须是线程安全的！**

* oneway调用

这个比较重要，之前也忽略了。默认情况下，AIDL跨进程调用是**阻塞的**，和普通函数调用一样，只是隐藏了跨进程的细节。如果需要调用立即返回，而不等待执行过程，那么可以使用`oneway`关键字。

`oneway`关键字只有在跨进程调用中生效；对于本地进程调用，仍旧是同步的。


## 例子
在另一个进程里调用：

调用6次open：

```java
06-06 15:09:17.793 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@f3ddc7e]
06-06 15:09:17.794 15299-15442/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
06-06 15:09:18.462 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@df975df]
06-06 15:09:18.463 15299-15312/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
06-06 15:09:18.922 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@e9b522c]
06-06 15:09:18.922 15299-15313/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
06-06 15:09:19.498 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@53394f5]
06-06 15:09:19.498 15299-15442/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
06-06 15:09:20.049 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@8d7428a]
06-06 15:09:20.049 15299-15312/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
06-06 15:09:20.646 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [open], action = [com.jokin.demo.aidl.sdk.IAction@aa990fb]
06-06 15:09:20.646 15299-15313/com.jokin.demo.aidl.server E/ActionService: open with penColor=-16777216 penSize=10
```

调用6次close：

```java
06-06 15:09:58.103 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@1816518]
06-06 15:09:58.103 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:58.328 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@9758b71]
06-06 15:09:58.328 15299-15312/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:58.609 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@1eefd56]
06-06 15:09:58.610 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:58.797 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@a7641d7]
06-06 15:09:58.797 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:59.058 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@fb3bac4]
06-06 15:09:59.058 15299-15312/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:59.376 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@a066dad]
06-06 15:09:59.376 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:09:59.648 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@af598e2]
06-06 15:09:59.649 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
```

发现：

1. 对于客户端，无论是在主线程还是子线程中调用，到了服务端，都是在子线程中执行。
2. 服务端的子线程池看起来有3个。为15312，15313，15442。

为了看服务端线程池的特性，将客户端并发20个线程调用：

```java
findViewById(R.id.closeAction).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        for (int i = 0; i < 20; ++i) {
            Thread thread = new Thread(closeRunnable, "thread-"+i);
            thread.start();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }
});

private Object lock = new Object();
private Runnable closeRunnable = new Runnable() {
    @Override
    public void run() {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Log.e(TAG, Thread.currentThread().getName()+" call close action!!!");
            mIsdk.doAction("close", new IAction(new CloseAction("要关闭拉！")));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
};
```

输出：

```java
06-06 15:25:35.747 10854-10920/com.jokin.demo.aidlclient E/MainActivity: thread-4 call close action!!!
06-06 15:25:35.747 10854-10919/com.jokin.demo.aidlclient E/MainActivity: thread-3 call close action!!!
06-06 15:25:35.747 10854-10924/com.jokin.demo.aidlclient E/MainActivity: thread-8 call close action!!!
06-06 15:25:35.747 10854-10925/com.jokin.demo.aidlclient E/MainActivity: thread-9 call close action!!!
06-06 15:25:35.747 10854-10931/com.jokin.demo.aidlclient E/MainActivity: thread-15 call close action!!!
06-06 15:25:35.747 10854-10926/com.jokin.demo.aidlclient E/MainActivity: thread-10 call close action!!!
06-06 15:25:35.747 10854-10918/com.jokin.demo.aidlclient E/MainActivity: thread-2 call close action!!!
06-06 15:25:35.747 10854-10933/com.jokin.demo.aidlclient E/MainActivity: thread-17 call close action!!!
06-06 15:25:35.747 10854-10935/com.jokin.demo.aidlclient E/MainActivity: thread-19 call close action!!!
06-06 15:25:35.747 10854-10916/com.jokin.demo.aidlclient E/MainActivity: thread-0 call close action!!!
06-06 15:25:35.747 10854-10921/com.jokin.demo.aidlclient E/MainActivity: thread-5 call close action!!!
06-06 15:25:35.747 10854-10917/com.jokin.demo.aidlclient E/MainActivity: thread-1 call close action!!!
06-06 15:25:35.747 10854-10929/com.jokin.demo.aidlclient E/MainActivity: thread-13 call close action!!!
06-06 15:25:35.748 10854-10934/com.jokin.demo.aidlclient E/MainActivity: thread-18 call close action!!!
06-06 15:25:35.748 10854-10927/com.jokin.demo.aidlclient E/MainActivity: thread-11 call close action!!!
06-06 15:25:35.749 10854-10923/com.jokin.demo.aidlclient E/MainActivity: thread-7 call close action!!!
06-06 15:25:35.749 10854-10928/com.jokin.demo.aidlclient E/MainActivity: thread-12 call close action!!!
06-06 15:25:35.749 10854-10932/com.jokin.demo.aidlclient E/MainActivity: thread-16 call close action!!!
06-06 15:25:35.749 10854-10930/com.jokin.demo.aidlclient E/MainActivity: thread-14 call close action!!!
06-06 15:25:35.749 10854-10922/com.jokin.demo.aidlclient E/MainActivity: thread-6 call close action!!!
06-06 15:25:35.762 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@87b3042]
06-06 15:25:35.762 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@a33d853]
06-06 15:25:35.762 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.762 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.763 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@72e9590]
06-06 15:25:35.763 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@dc7be89]
06-06 15:25:35.763 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@1100e8e]
06-06 15:25:35.764 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@819a6af]
06-06 15:25:35.765 15299-5307/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@e0f7abc]
06-06 15:25:35.767 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.767 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.767 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@d64ba45]
06-06 15:25:35.768 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.768 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.768 15299-5307/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.769 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@b02859a]
06-06 15:25:35.769 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.770 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@3e3fecb]
06-06 15:25:35.770 15299-15312/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.770 15299-5307/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@bb786a8]
06-06 15:25:35.770 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@61bb5c1]
06-06 15:25:35.770 15299-5307/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@f736166]
06-06 15:25:35.771 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-15312/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@6b1bca7]
06-06 15:25:35.771 15299-15313/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@1526554]
06-06 15:25:35.771 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-15313/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-5307/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@dececfd]
06-06 15:25:35.771 15299-5307/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.771 15299-5306/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@6c42df2]
06-06 15:25:35.771 15299-5306/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.772 15299-10938/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@2b37c43]
06-06 15:25:35.772 15299-15442/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@51a82c0]
06-06 15:25:35.772 15299-10938/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.772 15299-15442/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
06-06 15:25:35.772 15299-15312/com.jokin.demo.aidl.server E/ActionService: doAction() called with: key = [close], action = [com.jokin.demo.aidl.sdk.IAction@a05bf9]
06-06 15:25:35.773 15299-15312/com.jokin.demo.aidl.server E/ActionService: close with tip=要关闭拉！
```

发现：

1. 服务端的线程池是会扩大的。应该不会排队。
