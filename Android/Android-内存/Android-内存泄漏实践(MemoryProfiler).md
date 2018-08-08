[memory-dump]: ./imgs/memory-dump.png
[memory-grahpy]: ./imgs/memory-graphy.png

## Android 内存泄漏实践（Memory Profiler版）

最近在编写一个多进程的窗口/View模块。

主要功能是，子进程A在主进程中显示一个UI界面。

其中一种View的实现方式是`RemoteViews`。

为了测试，写了个Demo：子进程A显示一个ImageView在主进程，并且每33ms设置一张图片，实现帧动画。

测试性能的第一点是，先看一下有没有内存泄漏。所以打开了`Memory Profiler`观察内存。

![memory-graphy][memory-grahpy]

对`Memory Profiler`不了解的，看另一篇《Memory Profiler》

## Bitmap泄漏

33ms的动画很快，没一会就发现`native memory`蹭蹭往上涨，竟然高达`500M`。

通过捕获某一段内存分配信息，可以发现，主要是`Bitmap`对象导致的。

![memory-dump][memory-dump]

> Bitmap的泄漏，主要体现在`native`层的内存泄漏。

选中Bitmap后，通过右侧的引用，可以追溯到对应的泄漏位置。

泄漏原因是：客户端的`RemoteViews.setImageViewBitmap()`。

为什么原生的RemoteViews的普通的方法会导致内存泄漏？主要是由于使用不当！！

`RemoteViews`的设计，是每个`set`方法，都会产生一个`Action对象`然后`Add`到RemoteViews对象的数组中，然后序列化到服务端使用。这个过程并没有进行去重。

所以，每33ms就调用一次`setImageViewBitmap()`，就相当于每33ms往数组里添加一个`Action`对象。不用多久，服务端收到的`RemoteViews`就会包含大量Bitmap对象。

要解决这个问题，只需要每次设置图片时，创建一个新的`RemoteViews`对象，保证其没有旧数据影响。

对于Android的`RemoteViews`，本质和`Map／List／Data`等实体类一样，只是一个数据对象。这个数据对象可以被作用于一个普通的View，这就是`RemoteViews`的本质。

## 数组泄漏

服务端得到客户端的`RemoteViews`，会存储在本地的一个`HashMap`中，用来标记管理。

在IPC通信中，要标记一个对象，唯一的办法是给对象赋予一个key。

这很容易理解。进程A中的对象A序列化到服务端B，服务端B得到的对象A虽然长得和A一样，但一定是不同的对象实例。因为对象是归属于进程的，进程不一样，对象就一定不同。

所谓的跨进程通信，传递的都只有数据。

所以，要标记两个对象是否相同，必须使用对象中的数据来标记。引入key是其中一个方法。

> 上面说key是唯一办法，泛指要产生一个唯一的键值作为标记

回到正题。

修复上面的问题后，`Memory Profiler`中，native内存一栏，终于不再上涨。但跑了几十分钟，发现`Java Memory`一栏，内存从最先的8M，到了后来的30M。

捕获某一段内存分配信息，发现比较靠前的是`HashMap`数组的结点。通过点击，查看右边的引用。结果发现数组长度比实际的要长。

追溯了代码，发现创建新的`RemoteViews`对象时，对象的key没有进行复制，而且也没有进行序列化。也即是说，发送到服务端的对象，一直被当作是新的对象看待。

所以，原本只有1个对象的客户端，变成了不断创建对象的客户端。而且这些对象被添加到HashMap管理，运行期间无法被GC，所以产生了JAVA对象的泄漏，最终体现在`JAVA Memory`上涨。

修复后，跑了数小时，各栏内存稳定。

## 总结

第一次尝试使用`Memory Profiler`，然后发现可以进行基本的内存泄漏检测，并且可以很方便的跟踪引用，定位泄漏的位置。

最后，`Memory`的分栏也很有用。如果是`JAVA Memory`层发生泄漏，就是JAVA层的对象生命周期管理失误，可以重点分析JAVA层的模块。如果是`Native`层发生泄漏，可以重点分析JNI调用或者Android原生对象的生命周期。
