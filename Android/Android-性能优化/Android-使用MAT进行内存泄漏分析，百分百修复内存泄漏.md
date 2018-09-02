[mat1-overview]:imgs/mat1-overview.png
[mat2-leak-suspect]:imgs/mat2-leak-suspect.png
[mat3-leak-problem]:imgs/mat3-leak-problem.png
[mat4-leak-detail]:imgs/mat4-leak-detail.png
[mat5-income]:imgs/mat5-income.png
[mat6-baidu]:imgs/mat6-baidu.png
[mat7-histogram]:imgs/mat7-histogram.png
[mat8-dominator]:imgs/mat8-dominator.png
[mat9-merge-path]:imgs/mat9-merge-path.png
[mat10-path2gc]:imgs/mat10-path2gc.png
[mat11-path2gc2]:imgs/mat11-path2gc2.png
[mat12-outgoing]:imgs/mat12-outgoing.png
[mat13-outgoing2]:imgs/mat13-outgoing2.png
[mat14-all]:imgs/mat14-all.png
[mat15-objects]:imgs/mat15-objects.png
[mat16]:imgs/mat16.png
[mat17]:imgs/mat17.png
[mat18]:imgs/mat18.png
[mat19]:imgs/mat19.png

本文是使用Eclipse Memory Analyzer Tool (MAT)进行内存泄漏分析的笔记。遇到大大小小的内存泄漏，都可以通过MAT分析出来。
这一篇写得不够好，参考另一篇《内存泄漏检测实战》

## 安装
MAT官网 <https://www.eclipse.org/mat/>

> To install the Memory Analyzer **into an Eclipse IDE** use the update site URL provided below. The *Memory Analyzer (Chart)* feature is optional. The chart feature requires the [BIRT Chart Engine](https://www.eclipse.org/birt) (Version 2.3.0 or greater).

总体有3种方式。

1. 直接下载独立安装包
2. 下载Eclipse插件包，作为Eclipse插件安装（Archived Update）
3. 在线安装Eclipse插件包，同样作为插件安装（Update Site地址）

在Mac下面，单独安装失败，使用Eclipse自带插件服务器安装失败（太慢），改为UpdateSize的地址后，在线安装成功。

##  分析
MAT的文件后缀是hprof，从AndroidStudio中得到的hprof需要经过转换（网上有教程）。这里废话不多说，直接进入主题。

MAT界面：
* Overview（概述）
* Histogram（直方图）
* Leak Suspects（泄漏猜测）

**Overview**
![QQ20170929-154814@2x.png][mat1-overview]

**Leak Suspects**
![QQ20170929-155719@2x.png][mat2-leak-suspect]

大部分LeakSuspects都会检测到一些问题。下图是一个内存泄漏到检测信息。
![QQ20170929-155931@2x.png][mat3-leak-problem]

点击`Details »`查看详情，得到一个引用链。

![QQ20170929-160233@2x.png][mat4-leak-detail]

这张引用链表示，一张大图片没有被释放 -> 被Drawable引用 -> 被CameraPopWindow引用 -> 被CBarrageView引用 -> 即CBarrageView在退出的时候没能被释放，出现了内存泄漏。

再往下看，CBarrageView没能被释放，是因为被一个叫HandlerAction引用，这个HandlerAction是在一个数组中，这个数组由ViewRootImpl.RunQueue持有，最后RunQueue是被Thread引用了。

查看最后的Thread，选择`ListObjects - with incoming references`表示查看引用了这个Thread的对象：

![QQ20171127-094245@2x.png][mat5-income]

可以看到，Thread作为mUiThread被DisplayActivity引用着。布局层次上，DisplayActivity -> EditAndPreviewActivity(CBarrageView容器)，所以，CBarrageView跨越了它的生命周期，是因为DisplayActivity里的mUiThread在持有它，为什么CBarrageView会被DisplayActivity的mUiThread持有呢？

此时，通常有两种方式确认泄漏的位置。

**方式1**，上网查关键字。
![QQ20171127-095205@2x.png][mat6-baidu]

可以看到，大致和View的post相关。可以查看CBarrageView里post相关是否使用得当。

**方式2**，继续跟进源码。

查看源码：跟进Activity源码。
```java
private Thread mUiThread;

mUiThread = Thread.currentThread();  // 主线程

    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
```
猜想可能和post相关，想起CBarrageView里面使用了postDelayed()，

```java
    public boolean postDelayed(Runnable action, long delayMillis) {
        final AttachInfo attachInfo = mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler.postDelayed(action, delayMillis);
        }
        // Assume that post will succeed later
        ViewRootImpl.getRunQueue().postDelayed(action, delayMillis);
        return true;
    }
```
这里，ViewRootImpl，RunQueue和内存泄漏检测的引用链很接近。所以可以继续猜想，是由于CBarrageView的postDelayed导致的。

跟到CBarrageView内部，可以发现下面的代码，Runnable并没有主动释放！这就是泄漏的原因：

```java
    private void initView() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                checkRowIdle();
                postDelayed(this, 50);
            }
        }, 50);
    }
```
这里为什么泄漏呢？
1. 匿名内部类Runnable持有外部类CBarrageView的引用
2. Runnable超出当前类的生命周期（Runnable是丢在主线程的消息队列，这个是View的postDelayed接口的问题了，没有做到自动释放）
3. 当前类生命周期结束的时候，没有主动释放Runnable（CBarrageView生命周期结束，但主线程生命周期还健在）

本来假想View释放的时候会把附带的Runnable释放掉，但这个Runnable并不是依附到当前View，而是主线程。所以不确定的地方切记使用最安全的方式，否则就相当于挖了个坑。

修改后：

```java
   private void initView() {
        postDelayed(mCheckRowIdleTask, 50);
    }

    private Runnable mCheckRowIdleTask = new Runnable() {
        @Override
        public void run() {
            checkRowIdle();
            postDelayed(this, 50);
        }
    };

    public void release() {
        removeCallbacks(mCheckRowIdleTask);
    }
```

**Histogram**
![QQ20170929-155630@2x.png][mat7-histogram]

**Dominator Tree**
显示大对象列表

![QQ20171127-111424@2x.png][mat8-dominator]

## 内存泄漏检测与OOM定位

1. 如果是OOM崩溃，可以直接拿到对应hprof文件进行分析，通常通过LeakSuspects就可以定位到OOM的位置。

2. 内存泄漏的检测步骤：
2.1 确定要检测的页面（功能／模块）
2.2 进入对应的页面，操作
2.3 退出页面，主动执行一次GC（不执行也行）
2.4 抓取hprof文件使用MAT进行分析。
2.5 跳到Histogram界面，使用过滤器（图中灰色<Regex>位置）找到对应的页面。没有找到，恭喜，生命周期正常，没有内存泄漏。找到了，则根据下面的步骤继续定位。

## MAT定位

![QQ20171127-113347@2x.png][mat9-merge-path]

最重要的几个选项，优先级从高到低：

1. Path To GC Roots
    列出当前对象到GCRoot的引用链（自底向上）
![QQ20171127-114404@2x.png][mat10-path2gc]

自底向上，从当前对象到GCRoot。很容易看出，当前对象没能被释放，是因为ImageView引用着，继而被CBarrageView引用着，继而被Thread的RunQueue引用着。

2. Merge Shortest Paths to GC Roots
   列出当前对象到GCRoot的引用链（自顶向下）
 ![QQ20171127-113804@2x.png][mat11-path2gc2]

自顶向下，从GCRoot到当前对象。如果要看CBarrageView为何没能被释放，则从下往上看。如果要看当前对象真正被引用的地方，就是最底部的ImageView。换句话，对象之所以没有被释放，是因为被ImageView引用着，被CBarrageView引用着，被更上层的Thread引用着。

3. With incoming references 和 With outgoing references
    With incoming references 是列出引用当前对象的对象。
    With outgoing references 是列出当前对象引用的对象。

如果当前对象有泄漏，则只需看 incoming references即可，即找出它被什么对象引用了导致了生命周期异常。根本无需关心它引用了哪些对象！！

**在内存泄漏方面，outgoing references并没有什么用，切记不要点，只会混淆视听**

4. List Objects 和 Show Objects By Class
    List Objects是按照类的实例（对象）来显示。
    Show Objects By Class是按照类名来显示。

![QQ20171127-115126@2x.png][mat12-outgoing]

![QQ20171127-115109@2x.png][mat13-outgoing2]

差异自行感知。

5. references的过滤器
5.1 with all references  即所有引用都显示出来
5.2 exclude weak reference  即不显示弱引用
5.3 exclude soft reference 即不显示软引用
5.4 exclude phantom reference 即不显示幽灵引用

由于weak／soft／phantom引用都可以被GC回收，所以三者都可以不显示。通常使用`exclude weak/soft reference`。（weak是引用可回收时立即被回收变为null，soft是引用可回收但会等到内存不足时才回收，phantom是用来跟踪引用释放用的，本身不会产生强引用）因为有时候，选择exclude all反而没有内容可显示。

![QQ20171127-113640@2x.png][mat14-all]


**回到实例**

如上面所述，怀疑CBarrageView有内存泄漏，在退出了CBarrageView后捕获hprof文件。

1. 选择Histogram（Dominator Tree可以忽略了，后面会说）
2. 按照包名过滤：com.xxx.xxx
![QQ20171127-153205@2x.png][mat15-objects]

3. Objects那里，只有一个的对象，如果发现不止一个，则表示内存发生泄露。

4. 如图，选择含有CBarrageView（如CBarrageView$CRecycleBin)，或者在CBarrageView里引用的对象（如CBarrageItem）都可以。因为CBarrageView没有释放，其内部引用的对象也不会释放，最后到GCRoot的引用链是一致的。这里要注意不要使用对象数为0的来分析，因为这种无法生成引用链。**同时，建议使用更底层的对象**，因为当前对象不能释放很可能是由于内部b（见另一个案例）。

5. 右键，选择Merge Shortest Paths to GC Roots，**过滤掉weak和soft引用**，然后就生成下面的引用链（自顶向下）。
![QQ20171127-154629@2x.png][mat16]

注：自顶向下，黑色部分是变量，变量的类型是上一条。如图，localValues变量的类型并不是Values，而是上面的Thread；mActions变量并不是ArrayList而是上面那条RunQueue。

6. 如果习惯自底向上分析，可以按照*步骤4*再选择一个对象，右键选择Path To GC Roots，同样**过滤掉weak和soft引用**，然后就生成下面的引用链（自底向上）。
![QQ20171127-154647@2x.png][mat17]

注：自底向上，黑体部分是变量，变量的类型紧随其后，和自顶向下不同！如图，mActions变量类型是RunQueue，是下面table变量的一个元素。

7. 过滤掉weak和soft引用可以减少不必要的分析，因为误分析了weak和soft的引用其实一点帮助都没有，只会浪费时间。

8. 根据引用链猜想或定位问题。可以通过源码，或者通过搜索关键字。

9. 找到泄露的根本原因。比如，根据泄露定位到Activity还挂在广播服务中，通过查看发现onDestroy里是有反注册的，那么说明是Activity泄露（没有执行到Destroy）才导致了没能反注册广播，则下一步是跟进为何Activity没有泄露，而不是死磕广播服务。

最后，回答为什么不使用Dominator Tree。因为这个界面并不能百分百生成引用链。如下图，CBarrageView相关的对象，几乎有一半没能正确生成引用链。这会误导，模块没有发生内存泄漏，所以不要再使用Dominator Tree界面来进行内存泄漏分析。这个界面就只是单纯看大对象就算了！！
![QQ20171127-160738@2x.png][mat18]
![QQ20171127-154131@2x.png][mat19]

## 一些内存泄漏例子

1. PostRunnable导致的内存泄漏：
    android.view.ViewRootImpl$RunQueue

    使用了View的post或postDelayed没有进行Runnable的主动释放。导致Runnable泄漏到主线程。

2. 广播接收器导致的内存泄漏：           android.app.LoadedApk$ReceiverDispatcher$InnerReceiver
    mDispatcher java.lang.ref.WeakReference
    mContext android.app.LoadedApk$ReceiverDispatcher

    使用了广播，注册了没有进行反注册，导致泄漏到广播分发队列中。

3. PopupWindow等setListener传入匿名类导致的内存泄漏：

    PopupWindow传入了外部的匿名类，如果PopupWindow没关闭就会导致外部的类泄漏。
