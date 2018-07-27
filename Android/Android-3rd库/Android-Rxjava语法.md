## Rxjava语法

```
compile 'io.reactivex:rxjava:1.0.14'
compile 'io.reactivex:rxandroid:1.0.1'
```
以下，基于`Rxjava1`。

#### Rxjava基本概念：

* `Observable` 可观察者，被观察者。 类比`Button`
* `Observer` 观察者。 类比`Activity`
* `subscribe` 订阅。 类比`setOnClickListener()`
* `事件` 包括`onNext`,`onCompleted`,`onError`。 类比`onClick()/onTouch()`


事件：

* onCompleted(): 事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做一个队列。RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() 方法作为标志。

* onError(): 事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列自动终止，不允许再有事件发出。

* 在一个正确运行的事件序列中, onCompleted() 和 onError() 有且只有一个，并且是事件序列中的最后一个。需要注意的是，onCompleted() 和 onError() 二者也是互斥的，即在队列中调用了其中一个，就不应该再调用另一个。


#### 一个完整的Rxjava创建流程

1、创建`Observable`，可观察者。类比`Button`。

```java
Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
    @Override
    public void call(Subscriber<? super String> subscriber) {
        subscriber.onNext("Hello");
        subscriber.onNext("Hi");
        subscriber.onNext("Aloha");
        subscriber.onCompleted();
    }
});
```

使用`create()`来创建一个`Observable`。

create传入一个`OnSubsribe`对象。这个对象会被存储在返回的`Observable`对象中。当`Observable`被订阅的时候，`OnSubsribe`对象的`call()`方法会被调用。

如上，当这个对象被订阅的时候，这个对象会触发三次`onNext()`事件，和一次`onComplete()`事件。（类比于，触发`onClick()`和`onTouch()`。）

**`OnSubsribe对象是被观察者的事件分发中心`**

扩展。

RxJava还提供额外的方法，便捷的创建`OnSubscribe`对象，并产生对应的事件分发。

* `just(T...)` 将传入的参数依次触发`onNext()`，最后触发`onComplete()`。
* `from(T[]) / from(Iterable<? extends T>)` 将传入的数组或迭代器拆分为对象，依次触发`onNext()`，最后触发`onComplete()`。

> just()/from()都只是创建了一个`Observable`，只不过这个`Observable`是执行遍历的，将参数一一遍历，发送给订阅者的`onNext()`回调。

上面例子，等同于：

```java
Observable observable = Observable.just("Hello", "Hi", "Aloha");
```
和
```java
String[] words = {"Hello", "Hi", "Aloha"};
Observable observable = Observable.from(words);
```

**相当于**，一个内置的默认的`Observable.OnSubscribe`。


2、创建`Observer`，观察者，订阅者。类比`Activity`。描述谁对被观察者感兴趣。

```java
Observer<String> observer = new Observer<String>() {
    @Override
    public void onNext(String s) {
        Log.d(tag, "Item: " + s);
    }

    @Override
    public void onCompleted() {
        Log.d(tag, "Completed!");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(tag, "Error!");
    }
};
```

`Subscriber`是内置的一个实现了`Observer`的抽象类。只是做了扩展。

```java
Subscriber<String> subscriber = new Subscriber<String>() {
    @Override
    public void onNext(String s) {
        Log.d(tag, "Item: " + s);
    }

    @Override
    public void onCompleted() {
        Log.d(tag, "Completed!");
    }

    @Override
    public void onError(Throwable e) {
        Log.d(tag, "Error!");
    }
};
```

**Subscriber也是Observer，观察者**

在Rxjava的订阅过程`subsribe`中，`Observer`也总是转化为`Subscriber`再使用。

> `Observable`可观察者，可订阅者。`Observer`观察者，`Subscriber`订阅者，
> `Observer`和`Subscriber`是等价的。Rxjava里面，通常使用`Subscriber`。

两者区别是：

* `onStart()`: 这是 Subscriber 增加的方法。它会在 subscribe 刚开始，而事件还未发送之前被调用，可以用于做一些准备工作，例如数据的清零或重置。这是一个可选方法，默认情况下它的实现为空。需要注意的是，如果对准备工作的线程有要求（例如弹出一个显示进度的对话框，这必须在主线程执行）， onStart() 就不适用了，因为它总是在 subscribe 所发生的线程被调用，而不能指定线程。要在指定的线程来做准备工作，可以使用 doOnSubscribe() 方法，具体可以在后面的文中看到。

* `unsubscribe()`: 这是 Subscriber 所实现的另一个接口 Subscription 的方法，用于取消订阅。在这个方法被调用后，Subscriber 将不再接收事件。一般在这个方法调用前，可以使用 isUnsubscribed() 先判断一下状态。 unsubscribe() 这个方法很重要，因为在 subscribe() 之后， Observable 会持有 Subscriber 的引用，这个引用如果不能及时被释放，将有内存泄露的风险。所以最好保持一个原则：要在不再使用的时候尽快在合适的地方（例如 onPause() onStop() 等方法中）调用 unsubscribe() 来解除引用关系，以避免内存泄露的发生。

3、`Observable`被观察者，和`Observer`观察者都齐全了，接下来是`subsribe`订阅。

`observable.subscribe(observer);`
或
`observable.subscribe(subscriber);`

上面还记得吧，`subscriber`是`observer`的一个实现。

类比：`Button.setOnClickListener(Activity.onClickListener)`


内部简化源码：

```java
public Subscription subscribe(Subscriber subscriber) {
    subscriber.onStart();
    onSubscribe.call(subscriber);
    return subscriber;
}
```

回顾，`subscriber`是观察者，`onStart()`表示准备注册，`onSubscribe`对象是被观察者的事件分发中心。调用了`onSubscribe.call(subscriber)`，表示从事件分发中心中分发事件到观察者`subscriber`。


1. 调用 Subscriber.onStart() 。这个方法在前面已经介绍过，是一个可选的准备方法。

2. 调用 Observable 中的 OnSubscribe.call(Subscriber) 。在这里，事件发送的逻辑开始运行。从这也可以看出，在 RxJava 中， Observable 并不是在创建的时候就立即开始发送事件，而是在它被订阅的时候，即当 subscribe() 方法执行的时候。

3. 将传入的 Subscriber 作为 Subscription 返回。这是为了方便 unsubscribe().

#### 不完整的订阅方式

上面呈现的是，完整的被观察者和观察者订阅方式。除了这种完整的定义方式，还具备`不完整`的方式。可以类比，使用了`DefaultListenerAdapter`则可以只实现感兴趣的接口，而无需全部接口都实现。

```java
Action1<String> onNextAction = new Action1<String>() {
    // onNext()
    @Override
    public void call(String s) {
        Log.d(tag, s);
    }
};
Action1<Throwable> onErrorAction = new Action1<Throwable>() {
    // onError()
    @Override
    public void call(Throwable throwable) {
        // Error handling
    }
};
Action0 onCompletedAction = new Action0() {
    // onCompleted()
    @Override
    public void call() {
        Log.d(tag, "completed");
    }
};
```

订阅：

```java
observable.subscribe(onNextAction);
observable.subscribe(onNextAction, onErrorAction);
observable.subscribe(onNextAction, onErrorAction, onCompletedAction);
```

**注：subscribe(Action ...)参数中，第一个是Next事件，第二个是Error事件，第三个是Complete事件。大多数我们看到的都是只有一个参数的形式，就是设置的Next事件。**

可见，这种用法就是，只实现其中某个接口，而不是像`Observer`对象一样，需要同时实现三个接口。

没用过的同学可能会奇怪，`Action0`,`Action1`这类用法。

```java
public interface Action0 extends Action {
    void call();
}
public interface Action1<T> extends Action {
    void call(T t);
}
public interface Action2<T1, T2> extends Action {
    void call(T1 t1, T2 t2);
}
public interface Action3<T1, T2, T3> extends Action {
    void call(T1 t1, T2 t2, T3 t3);
}
```

通常，这种用法用来表示**同一个方法，不同的参数个数**的表达。所以，

`Action0` 表示无参的`call()`
`Action1` 表示含有1个参数的`call(T t)`
`Action2` 表示含有2个不同参数的`call(T1 t1, T2 t2)`
`Action3` 表示含有3个不同参数的`call(T1 t1, T2 t2, T3 t3)`

同理，会有`Action4`，`Action5`，`ActionN`。
**这里不使用可变参数，是因为编译期可以做参数类型检测，降低错误。**

回过头。由于`Subscriber`的回调中，`onCompleted`是无参的，`onNext`是一个参数，`onError`也是一个参数。所以可以分别用`Action0`和`Action1`表示。


问题：如何做到，传入同一个参数类型，却可以分别调用`Action0`、`Action1`等等不同的参数个数的回调？？
答案：取决于subscribe发起时，使用多少个参数。那么回调中就使用ActionN作为回调。任务的发起方决定了参数类型和个数。


#### 异步线程控制

从上面的`被观察者`，`观察者/订阅者`可以看到，有两个时机可以切换线程。

1. `被观察者`的事件分发时，即`OnSubscribe`时。可以控制分发到哪个线程。
2. `观察者/订阅者`的事件响应时，即`Subscriber`回调。可以控制响应在哪个线程。

默认情况下，Rxjava不会做线程切换。但如果用到了Rxjava内置的一些方法，可能默认会切到异步线程！

* `Schedulers.immediate()`: 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。

* `Schedulers.newThread()`: 总是启用新线程，并在新线程执行操作。

* `Schedulers.io()`: I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler。行为模式和 newThread() 差不多，区别在于 io() 的内部实现是是用一个无数量上限的线程池，可以重用空闲的线程，因此多数情况下 io() 比 newThread() 更有效率。不要把计算工作放在 io() 中，可以避免创建不必要的线程。

* `Schedulers.computation()`: 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使用的固定的线程池，大小为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。

* `Schedulers.single()`: 单线程的线程池。

* 另外， Android 还有一个专用的`AndroidSchedulers.mainThread()`，它指定的操作将在 Android 主线程运行。

上面已经说了，可以切换线程的时机。Rxjava已经提供了两个方法用来切换。

* `subscribeOn()` 指定 `subscribe()` 所发生的线程，即事件分发过程的线程。
* `observeOn()` 指定 `Subscriber` 所运行在的线程，即事件响应的线程。

**说实在的，觉得这命名很容易混淆。**

> `subscribeOn()`订阅在XXX线程。用于指定`onSubscribe`订阅操作的执行线程。
> `observeOn()` 观察在XXX线程。用于指定`Subscriber`订阅者的执行线程。


线程例子：

```java
Observable.create(new Observable.OnSubscribe<String>() {
    @Override
    public void call(Subscriber<? super String> subscriber) {
        Log.e(TAG, "OnSubscribe() called on thread " + Thread.currentThread().getName());
        subscriber.onNext("Hello");
        subscriber.onNext("Hi");
        subscriber.onNext("Aloha");
        subscriber.onCompleted();
    }
}).subscribeOn(Schedulers.io())    // 被订阅者的订阅操作运行于IO线程。
  .observeOn(Schedulers.computation()) // 事件响应在computation线程。
  .subscribe(new Observer() {
    @Override
    public void onCompleted() {
        Log.e(TAG, "onCompleted() called on thread " + Thread.currentThread().getName());
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError() called on thread " + Thread.currentThread().getName());
    }

    @Override
    public void onNext(Object o) {
        Log.e(TAG, "onNext() called on thread " + Thread.currentThread().getName());
    }
});
```

输出

```java
4729-4837 E/MainActivity: OnSubscribe() called on thread RxCachedThreadScheduler-1
4729-4835 E/MainActivity: onNext() called on thread RxComputationThreadPool-3
4729-4835 E/MainActivity: onNext() called on thread RxComputationThreadPool-3
4729-4835 E/MainActivity: onNext() called on thread RxComputationThreadPool-3
4729-4835 E/MainActivity: onCompleted() called on thread RxComputationThreadPool-3
```

线程例子2:

```java
Observable.from(getIntegers())
         .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
         .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
         .subscribe(new Action1<Integer>() {
             @Override
             public void call(Integer number) {
                 Log.e(TAG, "subscriber thread in " + Thread.currentThread());
                 Log.d(TAG, "number:" + number);
             }
         });

private Integer[] getIntegers() {
    Log.e(TAG, "getIntegers thread in " + Thread.currentThread());
    return new Integer[]{1, 2, 3, 4};
}         
```

如上，不少同学误解了`just()`和`from()`，认为`subscribeOn()`可以控制`just()/from()`里面`getIntegers()`方法的执行线程。或者，认为上面`subsribeOn()`的设置没有生效。

首先，`just()`和`from()`是创建`Observable`的参数。意味着什么？当执行`just()/from()`时，`Observable`还没开始创建，所以`subscribeOn`怎么能控制`just()/from()`的调用线程，以及里面`getIntegers()`的调用线程呢。

上面的代码，其实是：

```java
Integer[] intes = getIntegers();
Observable.from(intes).subsribeOn(Sechedulers.io());
```

所以，`just()/from()`的操作，是在当前线程执行的。

另外，这里`subsribeOn()`的设置并非没有生效。只是整个事件分发过程在Rxjava内部，是默认的，并没有输出log，所以没办法直接知道是哪个线程。但其实分发是在`IO`线程内执行的。

线程例子3. 加载图片。

```java
int drawableRes = ...;
ImageView imageView = ...;
Observable.create(new OnSubscribe<Drawable>() {
    @Override
    public void call(Subscriber<? super Drawable> subscriber) {
        Drawable drawable = getTheme().getDrawable(drawableRes));
        subscriber.onNext(drawable);
        subscriber.onCompleted();
    }
})
.subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
.observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
.subscribe(new Observer<Drawable>() {
    @Override
    public void onNext(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show();
    }
});
```

#### Rxjava变换

Rxjava牛逼的地方。

```java
Observable.just("images/logo.png") // 输入类型 String
    .map(new Func1<String, Bitmap>() {
        @Override
        public Bitmap call(String filePath) { // 参数类型 String
            return getBitmapFromPath(filePath); // 返回类型 Bitmap
        }
    })
    .subscribe(new Action1<Bitmap>() {
        @Override
        public void call(Bitmap bitmap) { // 参数类型 Bitmap
            showBitmap(bitmap);
        }
    });
```

`Func1`的用法，可以参照上面`Action1`的用法。

```java
public interface Func0<R> extends Function, Callable<R> {
    @Override
    R call();
}
public interface Func1<T, R> extends Function {
    R call(T t);
}
public interface Func2<T1, T2, R> extends Function {
    R call(T1 t1, T2 t2);
}
public interface Func3<T1, T2, T3, R> extends Function {
    R call(T1 t1, T2 t2, T3 t3);
}
```
和`Action`的区别是，提供了函数返回值`R`。(**唯一的区别**)

所以，可以认为，`Func1`不过是`Action1`提供了返回值的版本。

另外注意，**指定返回值类型** 的是参数中最后一个。

##### map()

事件对象的直接变换。比如上例，就是将参数中的`String`变换为`Bitmap`，在接下来的响应事件中（订阅操作用于指定被观察者的订阅、分发，所以使用响应来避免混淆），参数将由原来的`String`变为`Bitmap`。

`map()`是比较容易理解的，就是将一个对象转化为另一个对象，再提交给订阅者。相当于改变了订阅者的事件。

##### flatMap()

将一个对象转化为一个`Observable`，并将这个`Observable`的事件交给订阅者。相当于改变了订阅者的观察对象。

如下

```java
Student[] students = ...;
Subscriber<Course> subscriber = new Subscriber<Course>() {
    @Override
    public void onNext(Course course) {
        Log.d(tag, course.getName());
    }
    ...
};
Observable.from(students)
    .flatMap(new Func1<Student, Observable<Course>>() {
        @Override
        public Observable<Course> call(Student student) {
            return Observable.from(student.getCourses());
        }
    })
    .subscribe(subscriber);
```
`flatMap()`将`Student`对象，转化为一个新的`Observable`，而订阅者的事件则是来源于这个新的`Observable`。

新的`Observable`参数类型是`Course`，所以订阅者的事件响应中，参数是`Course`。

扩展：由于可以在嵌套的 Observable 中添加异步代码， flatMap() 也常用于嵌套的异步操作，例如嵌套的网络请求。示例代码（Retrofit + RxJava）：

```java
networkClient.token() // 返回 Observable<String>，在订阅时请求 token，并在响应后发送 token
    .flatMap(new Func1<String, Observable<Messages>>() {
        @Override
        public Observable<Messages> call(String token) {
            // 返回 Observable<Messages>，在订阅时请求消息列表，并在响应后发送请求到的消息列表
            return networkClient.messages();
        }
    })
    .subscribe(new Action1<Messages>() {
        @Override
        public void call(Messages messages) {
            // 处理显示消息列表
            showMessages(messages);
        }
    });
```

##### .throttleFirst(500, TimeUnit.MILLISECONDS)**

500毫秒内，丢弃新的事件。用来做防抖动。就是防止用户短时间内重复点击进入。

##### lift()变换

```java
public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
    return lift(new OperatorMap<T, R>(func));
}
```
看源码，map底层，其实是调用`lift()`做的变换。

`lift()`其实和`wrapper`或者代理类似。就是创建一个新的`Observable`包含这个旧的`Observable`，然后先执行新的`Observable`中的订阅方法，再执行旧的`Observable`中的订阅方法，最后将结果发送给`订阅者`。

所以，lift其实是在中间插入一个调用，形成一个新的调用链。

```java
public <R> Observable<R> lift(Operator<? extends R, ? super T> operator) {
    return Observable.create(new OnSubscribe<R>() {
        @Override
        public void call(Subscriber subscriber) {
            Subscriber newSubscriber = operator.call(subscriber);
            newSubscriber.onStart();
            onSubscribe.call(newSubscriber);
        }
    });
}
```
大致是：

```java
- oldObservable.call
       - newObservable1.call
             - newObservable2.call
                 ...
             - newObservable2.end
       - newObservable1.end
- oldObservable.end
- notifyEvent()
- completed()
```
和`Android的ViewGroup事件分发有点类似`。事件的响应回调，最终还是在最外层的`Observable`负责触发的。

至于`notifyEvent()`和`completed()`在哪个链路执行，取决于不同的`Observable`。上面只是其中一个流程的例子。

自定义lift变换方法：

```java
observable.lift(new Observable.Operator<String, Integer>() {
    @Override
    public Subscriber<? super Integer> call(final Subscriber<? super String> subscriber) {
        // 将事件序列中的 Integer 对象转换为 String 对象
        return new Subscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                subscriber.onNext("" + integer);
            }

            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }
        };
    }
});
```
> 讲述 lift() 的原理只是为了让你更好地了解 RxJava ，从而可以更好地使用它。然而不管你是否理解了 lift() 的原理，RxJava 都不建议开发者自定义 Operator 来直接使用 lift()，而是建议尽量使用已有的 lift() 包装方法（如 map() flatMap() 等）进行组合来实现需求，因为直接使用 lift() 非常容易发生一些难以发现的错误。

##### compose()变换

除了 lift() 之外， Observable 还有一个变换方法叫做 compose(Transformer)。它和 lift() 的区别在于， lift() 是针对**事件项和事件序列**的，而 compose() 是针对 Observable **自身** 进行变换。

比较简单，其实就是，对当前`Observable`进行一系列操作，用于同一系列的操作要针对多个`Observable`。相当于一个局部方法。

```java
public class LiftAllTransformer implements Observable.Transformer<Integer, String> {
    @Override
    public Observable<String> call(Observable<Integer> observable) {
        return observable
            .lift1()
            .lift2()
            .lift3()
            .lift4();
    }
}
...
Transformer liftAll = new LiftAllTransformer();
observable1.compose(liftAll).subscribe(subscriber1);
observable2.compose(liftAll).subscribe(subscriber2);
observable3.compose(liftAll).subscribe(subscriber3);
observable4.compose(liftAll).subscribe(subscriber4);
```

以上，表示`observable1～observable4`，都执行`liftAll`方法。将通用的方法放在`Observable`内部，而不是使用以下外部方法：

```java
private Observable liftAll(Observable observable) {
    return observable
        .lift1()
        .lift2()
        .lift3()
        .lift4();
}
liftAll(observable1).subscribe(subscriber1);
liftAll(observable2).subscribe(subscriber2);
```

这种外部方法，打破了`Observable`的链式调用。

#### 线程控制

上面已经涉及到线程的切换。再回顾以下。

* `subscribeOn()`订阅在XXX线程。用于指定`onSubscribe`订阅操作的执行线程。
* `observeOn()` 观察在XXX线程。用于指定`Subscriber`订阅者／观察者的执行线程。

```java
Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.newThread())
    .map(mapOperator) // 新线程，由 observeOn() 指定
    .observeOn(Schedulers.io())
    .map(mapOperator2) // IO 线程，由 observeOn() 指定
    .observeOn(AndroidSchedulers.mainThread)
    .subscribe(subscriber);  // Android 主线程，由 observeOn() 指定
```

如上，`observeOn()`可以设置多次，分别指定紧接着的下一次`onNext()/onError()/onComplete()`的执行线程。

也就是说，可以多次指定下一次事件响应的执行线程。

但是，`subscribeOn()`的位置无论放在哪，有多少个，都是使用第一个。

看源码：

```java
public <R> Observable<R> lift(Operator<? extends R, ? super T> operator) {
    return Observable.create(new OnSubscribe<R>() {
        @Override
        public void call(Subscriber subscriber) {
            Subscriber newSubscriber = operator.call(subscriber);
            newSubscriber.onStart();
            onSubscribe.call(newSubscriber);
        }
    });
}
```

任意链式的`Observable`，`OnSubscribe`这个执行订阅／分发的`call()`的调用源头是，第一个`Observable`的`OnSubsribe`中的`call()`，是由这个`call()`发起的调用链。所以分发调用链都执行于源头的这个`Observable`的线程。

虽然超过一个的 subscribeOn() 对事件处理的流程没有影响，但在流程之前却是可以利用的。

例子。

```java
Observable.create(onSubscribe)
    .subscribeOn(Schedulers.io())
    .doOnSubscribe(new Action0() {
        @Override
        public void call() {
            progressBar.setVisibility(View.VISIBLE); // 需要在主线程执行
        }
    })
    .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(subscriber);
```
与 Subscriber.onStart() 相对应的，有一个方法 Observable.doOnSubscribe()，表示订阅前。

默认情况下， doOnSubscribe() 执行在 subscribe() 发生的线程；而如果在 doOnSubscribe() 之后有 subscribeOn() 的话，它将执行在离它最近的 subscribeOn() 所指定的线程。

> 注意，它只能影响doOnSubscribe的执行线程，仍旧不能影响订阅后，分发流程的执行线程。

线程问题：默认，observer执行线程和subscribe线程一致。意味着：修改observers的线程的方式有两种：

* subscribe时就切换线程。
* observers时切换线程。


#### 扩展，同步改异步

Rxjava中，被观察者的执行，都在单线程。如何让被观察者可以在多线程中并发？

方式1: 创建多个`Observable`，但结果需要额外的同步锁。

方式2：使用flatMap

通过`flatMap`可以将`源Observable的元素项`转成`n个Observable`,生成的每个Observable可以使用线程池并发的执行，同时`flatMap`还会将这n个Observable merge成一个Observable。最终产生多个`onNext()`和一个`onComplete()`，可以在`onComplete()`里对并发结果进行处理。



参考
<http://gank.io/post/560e15be2dca930e00da1083#toc_1>
