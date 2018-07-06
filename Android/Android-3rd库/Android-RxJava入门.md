## Android RxJava

#### 例子

```java
Observable.empty()
		.observeOn(Schedulers.single())
		.doOnComplete(new Action() {
			@Override
			public void run() throws Exception {
				// TODO in the single Thread.
			}
		}).subscribe();
```

```java
Observable
		.just(init()) // just is Run on current Thread.
		.subscribeOn(Schedulers.single()) // subscribe is Run on the Single Thread.
		.timeout(5000, TimeUnit.MILLISECONDS)
		.subscribe(new Observer<Boolean>() {
			@Override
			public void onSubscribe(Disposable d) {
				// TODO
			}

			@Override
			public void onNext(Boolean result) {
				// TODO
			}

			@Override
			public void onError(Throwable e) {
				// TODO
			}

			@Override
			public void onComplete() {
				// TODO
			}
		});
```

#### 几个注意点

1. subscribeOn指定subscribe的线程。如果在Observable链中调用了多个subscribeOn，无论调用点在哪里，Observable链只会使用第一个subscribeOn指定的调度器。

2. observeOn指定observe的线程。可以中途改变Observable链的线程。比如，控制下一个onNext的执行线程。

3. 无论怎样，Observable链中，onNext还是顺序执行的。

4. 正因为onNext是顺序的，所以注意任意过程不能阻塞，否则会影响接下来的流程。

5.

#### 预设线程池Schedulers

预设线程池不应该乱用，因为线程的级别不一样。比如，不应该将IO操作放在computation，可能会导致IO操作抢占CPU。

##### Schedulers.from()

定制线程池。

```java
ExecutorService es = Executors.newFixedThreadPool(200, new ThreadFactoryBuilder().setNameFormat("SubscribeOn-%d").build());
Schedulers.from(es)
```

##### Schedulers.single()

拥有一个线程单例，所有的任务都在这一个线程中执行，当此线程中有任务执行时，其他任务将会按照先进先出的顺序依次执行。

##### AndroidSchedulers.mainThread()

在Android UI线程中执行任务，为Android开发定制。

##### Schedulers.io()

用于处理IO，使用`CachedThreadScheduler`实现，是可以增长或缩减的线程池。由于这个线程池是**无限的**，所以注意线程太多会引发`OOM`。

##### Schedulers.computation()

用于计算。**固定的** 线程池（大小为CPU的核数）。例如xml，json文件的解析，Bitmap图片的压缩取样等。

是以下方法默认的调度器。

* buffer()
* debounce()
* delay()
* interval()
* sample()
* skip()

因为以上方法已经预设了`Schedulers.computation()`调度器，所以你再调用`subscribeOn`是无效的。

```java
Observable.just(1,2,3)
			.delay(1, TimeUnit.SECONDS)
			.subscribeOn(Schedulers.newThread())
			.map(i -> {
			    System.out.println("map: " + Thread.currentThread().getName());
			    return i;
			})
			.subscribe(i -> {});
```
以上，`Schedulers.newThead()`是不生效的。

##### Schedulers.immediate()

立即在当前线程执行。如果当前线程有任务在执行，则会将其暂停，等插入进来的任务执行完之后，再将未完成的任务接着执行。

是以下方法默认的调度器：

* timeout()
* timeInterval()
* timestamp()

##### Schedulers.newThread()

创建新的线程。

##### Schedulers.trampoline()

为当前线程建立一个队列，将当前任务加入到队列中依次执行。
