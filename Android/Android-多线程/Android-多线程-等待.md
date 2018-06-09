## Android多线程等待

1. 创建一个锁
2. 调用wait等待锁
3. 释放锁的时候，调用锁的notifyAll()

```java
private static final Object LOCK = new Object();
private static final byte[] LOCK = new byte[0];

// 异步加载mPrintData。
synchronized (LOCK) {
	try {
		LOCK.wait();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}

synchronized (LOCK) {
	mPrintData = new Pair<>(path, colorful);
	// mPrintData加载完后，通知主线程继续
	LOCK.notifyAll();
}
```
