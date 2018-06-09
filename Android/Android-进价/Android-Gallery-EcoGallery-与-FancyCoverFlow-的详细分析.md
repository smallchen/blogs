[cgallery]:cgallery.png

# Android Gallery EcoGallery 与 FancyCoverFlow 的详细分析

## Gallery被标记为过时
一切的起源源于Gallery被标记为过时，所以才有本文的出现。

过时的原因，可以参考下面链接。

<https://stackoverflow.com/questions/11868503/the-type-gallery-is-deprecated-whats-the-best-alternative>

I suspect that Gallery was deprecated because it did not properly use convertView with its adapter. Which meant that it had to create a new view for every item which was a drain on performance.

Another option you have is to use the 3rd party created EcoGallery which Joseph Earl created to overcome the issue, this version does recycle its views properly.

## 关于RecycleBin
在分析Gallery控件时，需要先了解RecycleBin。RecycleBin是一个容器类，用来管理可回收的View。

不像Adapter／ArrayList等，RecycleBin有很多同名类，它作为内部类，存在于多个不同的类里面，有着不同的实现方式。(目前只发现下面两个类里面有RecycleBin内部类）

比如，AbsSpinner里面的RecycleBin：

```java
	class RecycleBin {
        private final SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }

        View get(int position) {
            // System.out.print("Looking for " + position);
            View result = mScrapHeap.get(position);
            if (result != null) {
                // System.out.println(" HIT");
                mScrapHeap.delete(position);
            } else {
                // System.out.println(" MISS");
            }
            return result;
        }

        void clear() {
            final SparseArray<View> scrapHeap = mScrapHeap;
            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.valueAt(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }
            scrapHeap.clear();
        }
    }
```

比如，AbsListView里面的RecycleBin：

```
class RecycleBin {
        private RecyclerListener mRecyclerListener;

        /**
         * The position of the first view stored in mActiveViews.
         */
        private int mFirstActivePosition;

        /**
         * Views that were on screen at the start of layout. This array is populated at the start of
         * layout, and at the end of layout all view in mActiveViews are moved to mScrapViews.
         * Views in mActiveViews represent a contiguous range of Views, with position of the first
         * view store in mFirstActivePosition.
         */
        private View[] mActiveViews = new View[0];

        /**
         * Unsorted views that can be used by the adapter as a convert view.
         */
        private ArrayList<View>[] mScrapViews;

        private int mViewTypeCount;

        private ArrayList<View> mCurrentScrap;

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            //noinspection unchecked
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList<View>();
            }
            mViewTypeCount = viewTypeCount;
            mCurrentScrap = scrapViews[0];
            mScrapViews = scrapViews;
        }

        public void markChildrenDirty() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[I];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).forceLayout();
                    }
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }
        // 代码太多，省略
    }
```

RecycleBin主要用于列表视图，比如GridView／ListView等，这些列表视图通常需要维护一个和可见窗口同样大小的View列表，当列表滑动时，不是不断创建新的View，而是重用滑出显示区域的那些Views。RecycleBin容器相当于回收区，用于管理滑出这个显示区域的View，表示可以重用的Views列表。（从命名字面意思也可以想到）

不同的列表控件，有不同的回收机制，所以RecycleBin的实现也不同。反过来，如果使用了不当的RecycleBin，会导致列表控件不能真正的利用到回收区的View，或者说，不当的RecycleBin导致列表控件无法从回收区“命中”重用的View，导致每次都创建新的View。（从这个角度看，RecycleBin更像是一个Cache）

所以，RecycleBin扮演着一个回收区和缓存的角色。

## Gallery控件的缺陷
Gallery控件就是这么回事，RecycleBin回收区设置不合理，导致View的利用率很低。从代码看，回收区以position为key，所以只有往回滑动才能命中，往前滑动position是递增的，每次都需要创建新的View。

实际上，经过测试，Gallery控件是每次都创建新的View，所以连往回滑动都没有“命中”。通过代码查看，除了最致命的选择position作为回收区key外，在每次layout都对回收区进行了clear，是导致连往回滚动都无法命中的原因。反正，这应该是Gallery控件的一个Bug，重用View的机制没有生效。（测试Demo见附录）

Gallery控件另一个Bug是，层叠效果。

Gallery控件通过重写ViewGroup的`getChildDrawingOrder`来使得绘制从两边到中间，从而使列表项有层叠的效果。然而，重写的这个方法并不正确。重叠效果并不是越靠近中心的层级越高。如果你需要越靠近中心的层级越高，越靠近边界的层级越低，那么你需要重写这个方法。

```java
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = mSelectedPosition - mFirstPosition;

        // Just to be safe
        if (selectedIndex < 0) return I;

        if (i == childCount - 1) {
            // Draw the selected child last
            return selectedIndex;
        } else if (i >= selectedIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return I;
        }
    }
```
假设3是选中的，childCount是5，那么上面这个方法的绘制顺序是：1 2 4 5 3
而通常我们需要的是下面的绘制顺序：1 2 5 4 3

```java
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = mSelectedPosition - mFirstPosition;
		if (i < selectedIndex) {
			return I;
		} else if (i >= selectedIndex) {
			return childCount - 1 - i + selectedIndex;
		} else {
			return I;
		}
	}
```

## EcoGallery替代Gallery
Gallery已经被标记为过时，至于过时的原因，据说就是上面的View没有重用导致效率低下。虽然Gallery控件有效率问题，但它的展示效果还是比较独特的：

1. 选中居中效果
2. 触摸滑动切换效果
3. 多页面Flip的效果（ViewPager只有3页可见）
4. 页面层叠效果（间距与层级的控制）

想快速完成上述效果，使用Gallery还是比较合适的，相比官方建议的HorizontalScrollView and ViewPager，还需要进行一定量的修改。

EcoGallery的诞生就是因为Gallery被过时，但仍旧需要一个替代Gallery的控件。
EcoGallery并不神秘，它其实是Gallery源码的一个拷贝。作者将Gallery源码从Android原生Widget中抽离出来，并对上述View重用的缺陷进行了改进，实现了另一个RecycleBin。关于如何抽离Android原生控件，可以参考附录。

```java
	class RecycleBin {
        private SparseArray<View> mScrapHeap = new SparseArray<View>();

        public void put(int position, View v) {
            mScrapHeap.put(position, v);
        }

        public void add(int position, View v) {
            mScrapHeap.put(mScrapHeap.size(), v);
        }
        public View get() {
            if (mScrapHeap.size() < 1) return null;

            View result = mScrapHeap.valueAt(0);
            int key = mScrapHeap.keyAt(0);

            if (result != null) {
                    mScrapHeap.delete(key);
            }
            return result;
        }
        // 剩下的是没用的
    }
```
这个RecycleBin回收区是不管position，只要回收区有View，就拿出来重用。可以认为，这里仅仅将RecycleBin当作是回收区，而没有作为缓存存在（缓存更接近于拿出来直接用，回收区重用会进行一次初始化）。

Gallery另一个层叠的问题，EcoGallery也存在。

## FancyCoverFlow酷炫效果
如果想要下面3D Gallery的效果，那么就需要这个控件。FancyCoverFlow继承于Gallery，而Gallery已经过时，所以可以修改使其继承EcoGallery。

[图片上传失败...(image-7619da-1512009763589)]

![FancyCoverFlow.png][cgallery]

附录：
分析Demo <https://github.com/jokinkuang/GalleryRecycle.git>
EcoGallery <https://github.com/falnatsheh/EcoGallery>
FancyCoverFlow <https://github.com/davidschreiber/FancyCoverFlow>

关于抽离Android原生控件，搜索抽离Android原生控件的方法。
关于更深入的理解分析，搜索CGallery控件。
