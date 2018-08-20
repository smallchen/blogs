[memory-dump]: ./imgs/memory-dump.png
[memory-grahpy]: ./imgs/memory-graphy.png
[memory-drawable]: ./imgs/memory-drawable.png
[memory-detail]: ./imgs/memory-detail.png
[memory-bitmap-detail]: ./imgs/memory-bitmap-detail.png
[memory-bitmap-preview]: ./imgs/memory-bitmap-preview.png
[memory-view-detail]: ./imgs/memory-view-detail.png

<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android 内存泄漏实践（Memory Profiler版）](#android-内存泄漏实践memory-profiler版)
- [Bitmap泄漏](#bitmap泄漏)
- [数组泄漏](#数组泄漏)
- [Drawable图片占用内存过大](#drawable图片占用内存过大)
- [内存统计细节](#内存统计细节)
- [总结](#总结)
- [参考](#参考)

<!-- /TOC -->

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

## Drawable图片占用内存过大

在另一个测试Demo中，也跑了一下进行内存检测，发现`Native`内存在Activity启动完成后，就占用了80M！

![memory-drawable][memory-drawable]

如上图，通过查看引用，看到这个`Native`内存主要由`Bitmap`引起。而这个`Bitmap`指向的是`Drawable`。

然后在`res/drawable`下发现了一张几MB的图片。由于Activity启动时，加载了`R.layout.activity_main`，这个布局里面使用到了那张`Drawable`大图，所以直接导致native内存膨胀。

解决方案是，图片不宜过大。当前业务场景不需要这么大的图片，所以裁剪图片来实现。如果业务场景需要大图，那么也没有办法。

可行的办法？：将native的内存占用往java层移动，把图片数据放到自定义数组里，直接显示二进制流。如果android view在显示时，一定需要创建Bitmap，那么这个方法也不可行。

所以，图片占用内存可能无法避免，唯一的办法是，尽可能使用和场景适应的图片，不乱使用大图。

由于是测试Demo，所以直接改变了图片的大小，使其变成200kb左右（730*400），这样情况下，native占用大概20M。

注：**在Drawable中的图片，不会立即被加载到内存，只有在使用时，才会被解压成Bitmap。**

## 内存统计细节

![memory-detail][memory-detail]

如上图，关键的信息有：

1. 右上角的内存统计
2. 列表中的内存细节

右上角的内存统计就不多说了，主要是如何看列表：

| Class Name | Alloc Count | Native Size | Shallow Count | Retained Size |
| :--------- | :---------- | :---------- | :---------- | :---------- |
| byte[]     | 1350       | 0       | 13402086       | 13402086       |
| Bitmap     | 28       | 896       | 1204       | 10010040       |
| DisplayActivity(com.xxx)     | 1       | 0       | 308       | 1184402       |
| LoadingDialog     | 1       | 0       | 61       | 1183213       |


* `Alloc Count` 中文（分配数量）指的是内存中对象的个数。比如：上图Bitmap对象有28个，DisplayActivity有1个，LoadingDialog只有1个。如果发现DisplayActivity有2个，那么很可能发生了泄漏。DisplayActivity旁边是对应的包路径。

* `Shallow Size` 中文（浅的大小）指的是对象本身占用内存的大小，不包含其引用的对象。常规对象（非数组）的Shallow size由其成员变量的数量和类型决定。数组的shallow size由数组元素的类型（对象类型、基本类型）和数组长度决定。可以理解成，是对象在内存中的存储大小。（对于基本类型，就是字节，对于引用类型，只计算引用类型自身的字节）。比如：上图DisplayActivity，本身对象只占用308字节，所以对DisplayActivity进行浅复制，只需要308个字节。

* `Retained Size`中文（遗留的大小）指的是对象的shallow size，加上从该对象能直接或间接访问到对象的shallow size之和。通俗的说，是表示对象释放后，会释放的内存。如果当前引用树释放，释放后应当释放等于`Retained Size`的内存，如果释放后并没有完全释放`Retained Size`，说明这个树结点发生了内存泄漏。比如：上图DisplayActivity释放后，应当释放1184402内存，如果没释放完全，说明DisplayActivity发生了泄漏（不懂也没关系，下面会解释）。

> Retained size of set of objects is the memory that will be freed by garbage collector if all the objects of the set are collected.

> Please note that, unlike shallow size, retained size of set of objects is not always the sum of retained sizes of each object.

注意上表：`byte[]`的`shallow count`和`retained size`是一样大小！所以说，字节数组，本身占用的字节空间，就是其二进制数据的大小，由于没有子引用，所以retained size也不会增大，和对象本身内存大小一样。

* `Native Size` 中文（底层大小）指的是对象引用的Native层的内存大小。比如：DisplayActivity和LoadingDialog都没有使用Native层，所以Native Size为0，而Bitmap的图片会用到Native加载，所以Native Size不为0.

最后看左侧的`Class Name`一列，无论是`byte[]`、`Bitmap`、`View[]`还是`DisplayActivity`，都是指内存中的一种对象类型的数量。比如，`View[]`指的是内存中View数组的数量和相关信息。点击选中，会出现一个列表，列表中的某一项，可能来自不同的模块，当前的`View[]`只是内存中的一个对象快照。

![memory-bitmap-detail][memory-bitmap-detail]

选中左边的Bitmap，就可以看到右边所有Bitmap实例。

如上图，Bitmap实例上的箭头，可以查看当前Bitmap实例向下引用的对象。`Retained Size`指的就是这些向下引用的对象，合计持有的内存大小。比如，上面的Bitmap，`Retained Size`大小为`1066KB`，主要是由于内部持有的`byte[] mBuffer`持有了`1066KB`，加上一些`基本类型`的属性占用的大小，总共`1066KB`。这个`byte[]`会出现在上面提及的`byte[]`类型的对象集合中。byte[]通常不需理会，因为它主要是二进制数据了。从中也可以知道，Bitmap中的图像数据，主要使用byte[]存储。

> 因此，`byte[]`实例列表是非常巨大的，它的意义不大，主要是数据内容。除了需要从`byte[]`往上索引，找到泄漏的地方，否则不建议点开，因为会卡卡卡卡卡卡死。

上面的这种统计，并不是每次都相等。因为一个对象中的子引用，有可能还被另一个对象引用着。换句话，`Retained Size`少于或等于以当前实例向下引用的所有对象的`Retained Size`总和。当当前对象是独立的，此时`Retained Size`等于往下引用的统计之和。如果当前对象引用了一个计数大于1的引用，当前对象释放时，就不能释放那个引用对象，此时`Retained Size`就少于往下引用的统计之和。总而言之，`Retained Size`只是表示，当前对象GC后，可以释放的内存大小。唯一缺陷是，为啥不标记一下，哪些引用是可以跟随释放（用于统计），哪些是不跟随释放呢（不在统计范围内）？

在某个对象实例右键，可以跳转到对应对象的实例，可以对那个对象进行往下索引，进一步跟踪。此处不累赘。

上面看的是以Bitmap为根往下的引用。接下来看Bitmap的往上索引，即以Bitmap为叶子，查看哪里引用到了这个Bitmap。

下面的`reference`可以查看当前Bitmap往上的索引。

`reference`里的`Retained Size`就意义不大了，因为你看到的只是父引用的内存大小，不具有统计意义，要统计，需要使用上面的从实例往下的引用计算方式。

如上，引用到Bitmap主要是`LoadingDialog`。可以看`Depth`（Depth从7到0往上索引），Bitmap的深度为7，往上索引的过程，就可以发现是由`LoadingDialog`引用的。

再说一遍，`reference`里面的`Retained Size`不具有统计意义，但能粗略看出内存变化，并且根据内存变化来追踪父类：

Bitmap(占用1066KB) - 父mBitmap占用1066KB - 父mBitmapState占用1066KB - 父mBackground占用1181KB - 以此类推。

可以看出，内存占用依次递增（不递增则说明路径是错的），原理是父结点总比子结点占用内存。

另外，mBackground相对于Bitmap来说，多了100kb左右，这100kb就是除Bitmap数据外，额外的属性多出的内存空间，要优化时，可以尽量压缩，使得mBackground和Bitmap大小差不多，这就是**内存优化**的过程，和内存可优化的空间。

**对于Bitmap来说，最方便的就是，reference旁边有个preview预览图片的功能，可以直接知道当前Bitmap对应哪张图片！**

![memory-bitmap-preview][memory-bitmap-preview]

最后，以view来重新验证一下：

![memory-view-detail][memory-view-detail]

1. View往下的引用中，View自身引用了8295KB，mContext引用了1184KB，而mBackground引用了8294KB，mContext是不跟随View释放的，所以View的`Retained Size`并没有统计mContext。此时View的Retained Size小于统计之和。View释放时，必定释放8295KB，否则就是内存泄漏。

2. View往上索引，可以看到View主要是DisplayActivity里面的RecyclerView。只有它比View的内存大，所以它才是View的父结点。

3. 没有了。

## 总结

第一次尝试使用`Memory Profiler`，然后发现可以进行基本的内存泄漏检测，并且可以很方便的跟踪引用，定位泄漏的位置。

最后，`Memory`的分栏也很有用。如果是`JAVA Memory`层发生泄漏，就是JAVA层的对象生命周期管理失误，可以重点分析JAVA层的模块。如果是`Native`层发生泄漏，可以重点分析JNI调用或者Android原生对象的生命周期。

## 参考
<http://toolkit.globus.org/toolkit/testing/tools/docs/help/sizes.html>
<https://developer.android.com/studio/profile/memory-profiler?hl=zh-cn>
