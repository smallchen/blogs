看到有这样的代码块，好奇这种用法。

```java
Thread lShowToastThread = new Thread() {
    @Override
    public void run() {
        Looper.prepare(); 
        String toast = " program has crashed";
        Toast.makeText(WelcomeApplication.this, toast, Toast.LENGTH_LONG).show();
        Looper.loop();
    }
};
lShowToastThread.start();
```

如果在Thread里不加Looper，则会报错：

```java
01-10 19:25:45.091 4769-4811/? E/AndroidRuntime: FATAL EXCEPTION: Thread-213
                                                 Process:, PID: 4769
                                                 java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                                                     at android.os.Handler.<init>(Handler.java:200)
                                                     at android.os.Handler.<init>(Handler.java:114)
                                                     at android.widget.Toast$TN.<init>(Toast.java:345)
                                                     at android.widget.Toast.<init>(Toast.java:101)
                                                     at android.widget.Toast.makeText(Toast.java:259)
```

这主要是因为，Handler是Android的东西，必须要有Looper（消息循环），而Thread仅仅是线程概念，并不要求有消息循环。**所以，在android里使用Thread要小心，不要在调用链过程中，创建Handler！**

为了能够顺利执行，估计后面在代码块中添加了Looper.prepare和Looper.loop。但这是正确的么？Looper.loop会导致消息循环，线程无法退出，如果是应用期间一直存在没关系，那么问题也不算大。如果是可重入的子模块，则Thread会不断创建，造成泄漏。

总结一下：

1. Thread里面不能new Handler，因为缺失Looper。使用Thread时避免在调用链中创建Handler（通常是在对象属性初始化Handler，然后不小心在Thread中new对象）。

1. 在Android里面，Thread不能被多次start，否则会报错（不知道java是不是如此）。如果要持有一个线程多次开启，使用Thread是无法实现的，但可以使用HandlerThread代替。

1. Looper.loop是阻塞的。像上面Thread里面的Looper用法，线程总是一直存在没能退出。

```java
for (;;) {
    Message msg = queue.next(); // might block（可能阻塞）
    if (msg == null) {
        // No message indicates that the message queue is quitting.
        return;
    }
    ......
}
```

4. Android中尽量避免使用Thread，就是尽力少埋坑。

