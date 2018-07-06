## ViewPager详解一，基本用法

### Demo1 不提供数据源的ViewPager

```java
public class EmptyPageAdapter extends PagerAdapter {
    private static final int Count = 2;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return position;
    }

    @Override
    public int getCount() {
        return Count;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}
```
结论：
1. 即使没有添加View，ViewPager还是可以滑动的，滑动的时候selected回调会被执行，滚动到第一页或最后一页，还会出现无法滚动到视觉效果。（监控ViewPager的事件就可以知道）

分析：
首先看instantiateItem，是在创建新Item时调用。

```java
    ItemInfo addNewItem(int position, int index) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = mAdapter.instantiateItem(this, position);
        ii.widthFactor = mAdapter.getPageWidth(position);	// 还没有重写
        if (index < 0 || index >= mItems.size()) {
            mItems.add(ii);
        } else {
            mItems.add(index, ii);
        }
        return ii;
    }
```

ViewPager是通过ItemInfo来表达某一页的信息，通过mItems（ArrayList<ItemInfo>）本地数组来存储ViewPager中真实存在的页面。看回ItemInfo结构，并没有存储实际的View引用，而是存储了与View一一对应的key。这也解释了，ViewPager不一定需要创建View。

ViewPager滑动过程，只是修改了position等数据，并产生selected回调，由于整个模型不是直接访问View，所以即使不提供View，ViewPager也可以正常工作。

我们看到的无法滚动的视觉效果，只是ViewPager在position已经无法改变时，在ViewPager容器产生的一个视觉效果。滑动过程，自始至终，ViewPager里面就只有容器自身。

很容易理解的视图与模型分离的设计。

### Demo2 使用List<View>静态视图数据源

```java
public class StaticPageAdapter extends PagerAdapter {
    private static final String TAG = "StaticPageAdapter";

    private List<View> mList = new ArrayList<>();
    private static final int Count = 10;

    public StaticPageAdapter(Context context) {
        for (int i = 0; i < Count; ++i) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            TextView textView = (TextView) view.findViewById(R.id.li_tv_value);
            textView.setText(String.valueOf("Position " + i));

            mList.add(view);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView( mList.get(position) );
        return position;    // return a key of the view
    }

    @Override
    public int getCount() {
        return Count;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == mList.get((Integer) object); // object is the key upper
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView( mList.get((Integer) object) );
        // @fix Following code cause error: The specified child already has a parent. You must call removeView() on the child's parent first.
        // container.removeViewAt(position);
    }
}
```
注意：
1. instantiateItem时，需要把使用addView把View加入到ViewPager中，否则页面空白一片。
2. 返回值或参数中的Object是key，用来与View一一绑定。
3. destroyItem时，不要使用removeViewAt，而尽量使用removeView，因为position与Item的下标并不总是相等。

分析：
1. ViewPager继承于ViewGroup，本身是一个容器，参数中的container其实就是ViewPager自身。所以要显示页面，必须把页面添加到container里。（这也是进行了视图和模型分离后，只能由用户去管理View）
2. ViewPager内部只管理与Item对应的ItemInfo结构，需要获取一个Item的其它信息时，都通过PageAdapter的hook函数来实现。比如:

```
// 要添加Item，通过adapter的instantiateItem来实现，用户需要自行添加View。
ItemInfo addNewItem(int position, int index) {
    ItemInfo ii = new ItemInfo();
    ii.position = position;
    ii.object = mAdapter.instantiateItem(this, position);
    ii.widthFactor = mAdapter.getPageWidth(position);
    if (index < 0 || index >= mItems.size()) {
        mItems.add(ii);
    } else {
        mItems.add(index, ii);
    }
    return ii;
}

// 判断child和Item的关系，通过adapter的isViewFromObject来实现。声明ItemInfo与Item的对应关系。
ItemInfo infoForChild(View child) {
    for (int i = 0; i < mItems.size(); i++) {
        ItemInfo ii = mItems.get(i);
        if (mAdapter.isViewFromObject(child, ii.object)) {
            return ii;
        }
    }
    return null;
}

// 要删除Item的时候，通过adapter的destroyItem来实现，用户需要自行删除View。
for (int i = 0; i < mItems.size(); i++) {
    final ItemInfo ii = mItems.get(i);
    mAdapter.destroyItem(this, ii.position, ii.object);
}    


```
3. destroyItem时，参数中的position是指Item在ViewPager中的逻辑位置，而实际上ViewPager中的Children只有缓存的那几个，要使用removeViewAt除非知道View在当前ViewPager的实际下标，否则只能使用removeView。

### Demo3 动态创建View视图

```java
public class DynamicPageAdapter extends PagerAdapter {
    public static final int ModelMin = 3;
    public static final int ModelNormal = 5;
    public static final int ModelMax = 7;

    private Context mContext;
    private int mCount = ModelMin;

    public DynamicPageAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.list_item, container, true);
        ViewGroup view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.list_item, container, false);
        container.addView(view);

        TextView textView = (TextView) view.findViewById(R.id.li_tv_value);
        textView.setText(String.valueOf("Position "+position));

//        return root.getChildAt(root.getChildCount()-1);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setModel(int model) {
        mCount = model;
    }
}

```
注意：
1. LayoutInflater的用法，这里很坑。返回的View并不一直是Inflate时所指定的xml生成的View。
2. 更改数据源后，一定要执行notifyDataSetChanged，否则抛异常。更改操作包括，修改了数据源的Count、内容等等。
3. 函数的调用是事件驱动，所以并不能保证如setAdapter后，就会立即调用Adapter的instantiateItem方法。事实上，setAdapter后只是记录数据源，ViewPager并未开始绘制，所以如果在setAdapter后立即访问instantiateItem里创建的View，得到是空的，因为instantiateItem还没开始执行呢。

分析：
1. LayoutInflater的返回值会根据参数发生变化。参数指定root时，返回值是root，否则返回值才是xml里的根view。不清楚这一点，误把root传入addView就会发生死循环。Inflater的参数中，root用于产生LayoutParams，boolean用于标识是否将生成的view加入root。
返回值：
- 如果指定root，且attachToRoot指定true，那么xml的根view会加入root，且返回值为root。
- 不管是否指定root，只要attachToRoot为false，那么返回值为xml的根view。
总结就是，Inflater始终返回根view，由于attachToRoot为false，并没有加入root，所以根view是xml自身，而如果attachToRoot为true，由于加入了root，所以根view是root而不再是xml本身。

2. ViewPager源码中，setAdapter里，会设置一个mExpectedAdapterCount，表示旧的Adapter的Count，每次操作前，ViewPager都会比较这个mExpectedAdapterCount和当前Adapter的Count是否相等，如果不等，说明Adapter发生了改变（至少从数量上发生了改变），而用户没有调用notifyDataSetChanged通知ViewPager刷新其它数据状态，这可能会导致内部错误，所以ViewPager抛出异常来阻止这个事件。（像ArrayList一样，不合理的操作会抛出ConcurrentModificationException异常，也是内部检测了不合理的操作后抛出的）。

3. Android大部分函数调用是事件驱动，这意味着，两个函数之间的前后调用关系是无法确定的。很多时候要确定两个函数间的前后调用关系，必须从源码去分析，除非那些已经以文档的形式说明，像Activity的onCreate总在onDestroy前调用。

为了分析，打印了从onCreate到ViewPager的instantiateItem调用过程：

D/Main LogViewPager: LogViewPager
D/Main LogViewPager: setAdapter
D/StaticPageAdapter: getCount
D/Main LogViewPager: addOnPageChangeListener
D/MainActivity: init finished!		# onCreate结束
D/Main LogViewPager: onAttachedToWindow  # View的onAttachedToWindow要比Activity的onCreate晚。
D/Main LogViewPager: drawableStateChanged
D/Main LogViewPager: onMeasure        # 第一次初始化是在这里调用populate方法
D/StaticPageAdapter: startUpdate
D/StaticPageAdapter: getCount
D/StaticPageAdapter: initItem position 0
D/StaticPageAdapter: initItem container children count 0
D/StaticPageAdapter: initItem view android.widget.FrameLayout{49d0a76 V.E...... ......I. 0,0-0,0}
D/Main LogViewPager: generateDefaultLayoutParams
D/Main LogViewPager: addView
D/Main LogViewPager: checkLayoutParams
D/Main LogViewPager: checkLayoutParams
D/StaticPageAdapter: initItem position 1
D/StaticPageAdapter: initItem container children count 1
D/StaticPageAdapter: initItem view android.widget.FrameLayout{d519877 V.E...... ......I. 0,0-0,0}
D/Main LogViewPager: generateDefaultLayoutParams
D/Main LogViewPager: addView
D/Main LogViewPager: checkLayoutParams
D/Main LogViewPager: checkLayoutParams
D/StaticPageAdapter: getCount
D/StaticPageAdapter: finishUpdate

可见，instantiateItem第一次是在onMeasure里进行初始化。是由事件回调触发的。可以看到ViewPager的onMeasure方法里，直接无条件调用了populate方法。

populate是ViewPager里进行Item管理的地方，对超出范围的Item进行删除，添加新的Item，对Item的宽高进行计算等等。也是唯一会调用instantiateItem的地方。

重新梳理流程，ViewPager的Item初始化，应该是：
1. setAdapter设置Adapter。
2. 由于是第一次初始化，private boolean mFirstLayout = true;所以调用的是requestLayout();虽然onAttachedToWindow里也会把mFirstLayout设置为true，但setAdapter要比onAttachedToWindow执行更早，onAttachedToWindow在Activity的onCreate完成后才会被调用。
3. ViewPager调用requestLayout()。当一个View主动调用requestLayout时，会通知DecorView进行View绘制过程，也即是分别调用onMeasure／onLayout／onDraw等方法。而View绘制是以事件形式通知执行的，Android每16ms绘制一次，所以执行时机其实是不太确定的。

Android每16ms的信号，是硬件产生的，不需要绘制时，不会产生，需要绘制时产生信号。Vsync的两个接收者，一个是SurfaceFlinger(负责合成各个Surface)，一个是Choreographer(负责控制视图的绘制)。


## ViewPager onPagedSelected 重复执行两次的源码分析

ViewFlipper 滑动时页面不可见，只是页面的动画而已
ViewPager 滑动时前后页面可见
