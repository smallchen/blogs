<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android 内存泄漏检测实战](#android-内存泄漏检测实战)
    - [先明白什么是内存泄漏](#先明白什么是内存泄漏)
    - [持有引用不代表泄漏](#持有引用不代表泄漏)
    - [MAT内存分析步骤](#mat内存分析步骤)
        - [从对象数量开始检测](#从对象数量开始检测)
        - [从大对象开始检测](#从大对象开始检测)
        - [从整体开始检测](#从整体开始检测)
    - [MAT内存分析总结](#mat内存分析总结)
        - [非泄漏的正常引用](#非泄漏的正常引用)
        - [线程里的GC-Root](#线程里的gc-root)
        - [$符号](#符号)

<!-- /TOC -->

[leak-filter]: imgs/leak-filter.png
[leak-objects]: imgs/leak-objects.png
[leak-objects-tree]: imgs/leak-objects-tree.png
[leak-objects-root]: imgs/leak-objects-root.png
[leak-objects-all-root]: imgs/leak-objects-all-root.png
[leak-points]: imgs/leak-points.png
[leak-top-consumers]: imgs/leak-top-consumers.png
[leak-biggest-objects]: imgs/leak-biggest-objects.png
[leak-biggest-objects-ref]: imgs/leak-biggest-objects-ref.png
[leak-byte]: imgs/leak-byte.png
[leak-histogram]: imgs/leak-histogram.png
[leak-detail]: imgs/leak-detail.png
[leak-byte-analyze]: imgs/leak-byte-analyze.png
[leak-basemark]: imgs/leak-basemark.png
[leak-application]: imgs/leak-application.png

## Android 内存泄漏检测实战

### 先明白什么是内存泄漏

面试的时候，也会经常被问到，什么是内存泄漏。

扯再多的匿名内部类，静态类，引用泄漏也没说明白本质。

内存泄漏，说白了，就是对象的生命周期没管理好，让其跑出了生命管理之外。本来应当结束的对象，在本该结束的场景下，仍旧好好地活着，而且还活着好几个。

内存泄漏也分大小，有小对象泄漏也有大对象泄漏。通常大对象泄漏比较容易找到，小对象的泄漏就比较困难。

更多，参见另一篇《内存如何泄漏》

### 持有引用不代表泄漏

一个对象被持有引用，并不代表这个对象就是泄漏的。尤其是通过`hprof`文件分析时，看到很多大对象都被引用着没能释放，但这并不一定是泄漏，可能是这个`hprof`正好是在对象生存期内的一个内存快照。

所以，内存分析时需要鉴别，哪些是正常的引用，哪些是跑偏了的对象。

### MAT内存分析步骤

Android自带的`Memory Profiler`老是抓不到数据，没办法，只好用MAT顶上，MAT分析是很好的工具，就是采集`hprof`文件太浪费体力。

说回MAT，MAT分析内存泄漏，通常步骤为：

1. 运行APP，多操作，到达一个场景。记住这个场景。
2. 运行一下GC。
3. dump出`hprof`文件
4. 使用`sdk/platform-tools/hprof-conv`转换为MAT格式
5. 使用`Eclipse`打开文件
6. 过滤出自己应用的包`com.xxx.xxx`
7. 从对象数量开始检测
8. 从大对象开始检测
9. 从整体开始检测

简化就是：

1. 先从数据中找到泄漏的对象
2. 然后查看泄漏的对象被什么引用着
3. 分析为什么对象还被引用着，肯定是哪里没有进行释放。

#### 从对象数量开始检测

分析内存泄漏，最快捷的一种方式是，查看目标对象的数量是否增大了。

![leak-filter][leak-filter]

如上，在`Overview - Histogram`里面过滤出当前应用的对象类。（顺便，前面绿色的`C`表示是类，白色的文件图标表示是对象）。

重点看`MultiPageMark`，这是一个很核心的类，原本在当前场景下，只会存在一个。但现在发现，数量已经达到7个，属于严重泄漏。

> 所谓严重，指的是泄漏的对象随着重复操作泄漏步骤，泄漏的对象可能达到无数个。而轻微泄漏，指的是，泄漏的对象固定在某个数量，不会继续增长，常见于赋值操作，使用新值替换旧值时，使得旧的引用可以被释放。

所以，从对象数量上，我们可以很轻易知道，`MultiPageMark`发生泄漏。

接下来，我们要看这些对象是被哪些活着的对象引用才导致无法释放。也就是通常说的GC-ROOT。

执行`右键` - `List Objects` - `With incoming references` 查看持有当前对象的对象，即哪些对象引用着这个对象。(outgoing基本不用看，它查看的是当前对象内部的引用，而我们需要的是引用着这个对象的引用)

![leak-objects][leak-objects]

如上，`incoming references`列出了7个对象。这7个对象的地址是不一样的，所以是7个不同的对象实例。以上列表，展开，得到的是引用着MultiPageMark这个对象的父对象。展开：

![leak-objects-tree][leak-objects-tree]

如上，你发现很多对象引用着`MultiPageMark`，但这些对象并不都是GC-ROOT对象，所以这个时候，你需要进行过滤。

执行`右键` - `Merge Shortest Paths to GC Roots` - `exclude all references`，过滤掉所有弱引用，查看对象到GC-ROOT之间的强引用链。

![leak-objects-root][leak-objects-root]

如上，过滤后只剩一条到GC-ROOT的引用链。

`MarkApplication`的`mLoadedApk`的`mReceivers`数组里引用着这个`MultiPageMark`。由于`MarkApplication`是全局的生命周期，所以导致它里面的数组元素，也是全局的生命周期。所以`MultiPageMark`得不到释放而泄漏。

`mReceivers`是广播接收器，说明`MultiPageMark`里注册了广播，但没有进行释放。

图中`MultiPageMark`是被一个`MultiPageMark@10`引用着，继而被`MarkApplication`持有。

`MultiPageMark@10`表示是匿名内部类，说明是MultiPageMark里的匿名内部类持有了MultiPageMark的引用，然后注册到广播里但没有进行反注册，继而引发了泄漏。

`MultiPageMark@10`表示MultiPageMark类里面，第10个匿名内部类。认真数数，不要放过方法里面的匿名内部类，数到第10个，那么这个匿名内部类就是泄漏的源头。如果你发现它是一个`BroadcastReceiver`，就可以更加肯定，就是这个匿名内部类广播注册了没释放。

> 在这个实例里，释放广播在`Destroy()`方法里，但调用者直接将对象赋值为null，而不是先调用`Destroy()`，所以导致广播没有反注册。

以上，展示了其中一个`MultiPageMark`的泄漏分析定位。以此类推，可以对剩余的6个对象依次进行。

但要注意，其中有一个是正常的，因为当前场景下，是存在一个MultiPageMark引用的。至于哪个位置的引用才是正常的，需要根据场景分析。比如，一个广播没有被释放，并不一定是泄漏，有可能，当前场景就是存在着一个广播，只是还没到destroy的场景，所以广播存在是正常的。

除了以上一个一个分析定位，还有一个更方便快捷的方式。就是在第一幅图，`Histogram`里面，在`MultiPageMark`那个类，执行`右键` - `Merge Shortest Paths to GC Roots` - `exclude all references`。然后就可以一次性列出所有MultiPageMark对象的GC-ROOTS。

![leak-objects-all-root][leak-objects-all-root]

为什么泄漏的对象有7个，但这里显示的GC-ROOTS却只有4个？这是因为有些对象的GC-ROOTS是一样的，也即是说同一处泄漏，泄漏了多个`MultiPageMark`对象。这里显示4个，表示`MultiPageMark`泄漏的地方有3处（其中有一个是正常的）。

展开就可以看到详细。

![leak-points][leak-points]

如图，`INSTANCE`是正常的那个引用。其余3处，分别是广播泄漏、线程泄漏、全局静态变量泄漏。

广播泄漏上面已经分析了，线程泄漏是指引用被线程持有，而线程还没结束。对于这里主要是由于线程是TCP线程，引用着MultiPageMark，但TCP线程是阻塞等待的，所以造成了泄漏。最后的全局静态变量泄漏，是指把一个匿名内部类设置到了全局静态变量里面了，导致静态变量持有了MultiPageMark的引用。从`MultiPageMark@15`可以看出，这个匿名内部类是MultiPageMark里第15个匿名内部类。

至此，MultiPageMark泄漏的所有点都被分析定位到了。

顺便说一下，最右侧的`retained heap`表示对象释放后能够释放的空间。

#### 从大对象开始检测

除了一开始就去查看大类、核心类的数量来判断是否发生内存泄漏，还有另一个方式。就是从应用的大对象入手。

切换到`OverView - Top Consumers`视图：

![leak-top-consumers][leak-top-consumers]

如图，应用的内存已经高达196MB了，当前设备单个应用最大可用的内存是256MB，内存已经很紧张。其中最大的那部分，是`android.graphics.Bitmap`。

> 查看当前设备单个应用最大内存限制，见另一篇《应用可用最大内存》

这个页面有一个`Biggest Objects`来查看大对象列表。

![leak-biggest-objects][leak-biggest-objects]

如图，同样对每个对象，执行`单击` - `Merge Shortest Paths to GC Roots` - `exclude all references`来查看对象到GC-ROOT的路径。

![leak-biggest-objects-ref][leak-biggest-objects-ref]

如上，图片被`MultiPageMark`引用着，占用了`8MB`，而`MultiPageMark`被`MarkApplication`的`mReceivers`引用着。所以，图片得不到释放。

对比上面那种方式，定位`MultiPageMark`的时候，并没有看到`Bitmap`泄漏了，只是确认了`MultiPageMark`没有被释放。但既然`MultiPageMark`没有释放，自然它里面的引用也得不到释放。

从大对象入手，可以解决应用的燃眉之急，毕竟大对象的泄漏才容易导致OOM，小对象即使泄漏，泄漏几千几百个也并不是太碍事。

最下面有个`byte[8232323]`的对象，它没有到`GC-ROOT`的路径，虽然不知道为何（可能是对象已经可以释放了，只是dump hprof前，没有执行一次GC），但有另一种方式，就是查看其被什么对象引用着。`点击 - ListObjects - with incoming references`，然后看到

![leak-byte][leak-byte]

如图，列出了引用这个`byte[]`数组的对象，你可以大致猜想，是哪个引用着它，导致它没有释放。（或许，所有引用都是非强引用）

同样，`Overview - DominatorTree`也是可以看到大对象列表，基本上都是Bitmap。

从大对象入手，可以大致理解应用当前内存的分布，有利于进行内存优化。所谓内存优化，是指优化内存空间。比如，降低图片质量来缩小Bitmap的内存。

#### 从整体开始检测

所谓从整体，就是从内存角度来整体分析应用。

切换`Overview - Histogram`视图，不添加任何Filter：

![leak-histogram][leak-histogram]

如上，`Shallow Heap`表示对象自身占用的内存大小，不包含子引用。这个和`Memory-Profiler`里的一样。

上面已经看到，应用占用内存是`196MB`。而`byte[]`数组也是`196MB`。可见，所有内存对象，在最后都是引用`byte[]`数组，也就是字节流（char[]也是引用byte[]，Bitmap里面也是引用byte[]来存放图片数据）。

所以，查看`byte[]`到GC-ROOTS路径就可以看到当前场景，内存引用关系：执行`右键` - `Merge Shortest Paths to GC Roots` - `exclude all references`。

![leak-detail][leak-detail]

如图：

* Shallow Heap: 对象自身内存占用，不包括子引用
* Ref. Shallow Heap: 自身加子引用链一共占用的内存
* Retained Heap: 对象释放时可以释放的内存空间

其中，对于大部分，`Ref Shallow Heap`与`Retained Heap`并不相等，这意味着，对象部分子引用还被别的对象引用着，所以当前对象释放时，那部分子引用不会跟着释放（常见于引用了单例、引用了外部传入的引用等等）。而对于`byte[]`而言，三者是相等的，因为它没有子引用。

对于上图，主要看`Ref.Shallow Heap`也即是，某个对象总共占用的内存。

上面这幅图，有利于我们分析应用的内存布局。

比如排在开头的`java.lang.Thread`，究竟这个线程干了什么，竟然占用了`100MB`。

![leak-byte-analyze][leak-byte-analyze]

如上，可以看到`Thread`主要由3部分内存构成：MultiPageMark的60MB，HashMap里的33MB，Bitmap里的8MB。细分下来，MultiPageMark主要是由于一个帧动画，帧动画就占用了50MB，并且有一个绘制图层Bitmap占用了8MB。HashMap里面主要是两张图片缓存，每张为8MB，共16MB。而最后的Bitmap是单独存在的。

由于子线程Thread自身就是一个GC-ROOT，所以没办法再跟下去。只是知道是线程里引用了`MultiPageMark`和`一个存储Bitmap的HashMap`和`一个独立的Bitmap`。通过代码，应该可以轻易定位到符合这些条件的那条线程。（对于本例，就是TCP线程）

再看排在第2位的`BaseMark`

![leak-basemark][leak-basemark]

如上，BaseMark占用了`33MB`，主要是因为一个`INSTANCE`的静态变量持有着`MultiPageMark`。`MultiPageMark`之所以占用33MB，主要是因为一个`HashMap`的`mDrawBitmapCache`，存储着2张缓存图片，共16MB，和`mDrawBitmap`的绘图Bitmap，以及一个`mSynthesisDrawBitmap`。4张图片共计32MB。

不难猜想，上面byte分析中的缓存和单独的Bitmap，应该就是这里的缓存和Bitmap了。

再往下看，TvApiApplication占用了25MB。

![leak-application][leak-application]

如上，可以看出，主要是由于`MultiPageMark`泄漏，导致了24MB占用。每个`MultiPageMark`里面都引用着一张8MB的Bitmap，当前泄漏了3个，所以占用了24MB。

依此类推，可以逐一分析出，应用中主要占用内存的地方，也可以粗略看到，是否是内存泄漏导致了内存占用如此大（如图中，发现了MultiPageMark有多个不同的实例，说明内存泄漏了）。即使没有内存泄漏，这个分析也有利于进行内存优化（比如，帧动画的50MB是否可以压缩）。

再结合`incoming references`，可以追溯到对象的父引用，依次找到根源。

像这样，全局分析不失为一种内存检测的方式。

### MAT内存分析总结

总结以上的分析，要定位大对象的泄漏还是比较容易的，但对于小对象，由于泄漏的内存比较少，上面的方式就不太有效。

小对象的泄漏，通常要按模块细分来检测，并且结合查看对象数量来判断是否发生泄漏。

最后，灵活利用`incoming references`和`merge path to gc root`来查看对象的父引用链，更容易分析问题。

#### 非泄漏的正常引用

* Bitmap被线程引用
* Bitmap被广播引用
* Bitmap被SidebarMenu引用着
* Bitmap被Toolbar引用着
* Bitmap被定时器引用着

因为捕获hprof时，状态是运行时，所以很多资源还未来得及释放，所以有一大部分Bitmap被引用着是很正常的！

我们需要知道当前状态下，哪些场景会引用到大Bitmap，对于这些场景，存在引用是正常的；而如果发现Bitmap被非运行中的场景引用着，则才说明该Bitmap泄漏了。

所以，排查方法很简单粗暴：**对每个Bitmap对象逐一排查**。

对每个Bitmap执行`Merge path to gc`-`exclude all references`，马上就能看到Bitmap被哪个GC-Root引用着。

如果你发现，所有对象都是合法引用的，那么你得关注下，内存利用是否合理，是否有优化的空间。因为OOM也有可能是出现在非泄漏情况下，当内存使用不合理，盲目使用内存，也会导致OOM。

#### 线程里的GC-Root

如果对象是线程内创建初始化的，比如一个`http server`，那么线程中的这个对象，GC-ROOT是`Thread`。

所以，如果看到GC-ROOT是`Thread`，说明是一个子线程，且这个子线程还没有结束，仍旧在运行中，所以才形成GC-ROOT。

#### $符号

美元符号，表示内部类。

如果是`$N`，则表示匿名内部类。
如果是`$MyHandler`，则表示是`MyHandler`内部类。

比如`SingleMark$1`和`SingleMark$2`表示SingleMark类内的两个匿名内部类，分别是`SingleMark`中第一个和第二个匿名内部类。

`Class$1 - Class$N`表示从Class类文件开头，包括属性、方法体里引用到的匿名内部类中第1～N个匿名内部类。说白了就是代码中，第N个匿名内部类，认真数就可以了。

如果`%N`难识别，可以改为命名内部类。
