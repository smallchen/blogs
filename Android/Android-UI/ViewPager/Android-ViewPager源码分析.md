## ViewPage源码分析

ViewPager数据源切换，必须调用notifyDataSetChanged。否则会抛出异常。
ViewPager内部对旧数据进行了保存，如果新数据和旧数据的数量不等，则抛出异常。
1.mExpectedAdapterCount仅在两处进行设置，第一次，setAdapter时初始化。第二次，notifyDataSetChanged时更新。在每次选择Item时，会对Adapter的Count和mExpectedAdapterCount进行比较，如果不等，则抛出异常，提示必须调用NotifyDataSetChanged来通知ViewPager，DataSet发生了变化。

2.populate
仅在populate函数里对mItems进行add操作！见addNewItem


ViewPager：View视图
PagerAdapter：DataSet
流程：

1 新建Adapter
1 新建ViewPager
1 调用setAdapter把Adapter对象注入ViewPager，进行绑定。此时ViewPager还不会进行绘制。
1 调用Adapter的DataSetChanged方法，通知ViewPager数据源已经改变，会触发ViewPager对Adapter的观察者函数onChanged，继而触发ViewPager的dataSetChanged方法，dataSetChanged才是真正进行绘制的地方。
1 由于ViewPager初始化时，mItems数组为空，所以needPopulate会为true。
1 对mItems进行遍历，是对旧数据进行遍历处理。
1 needPopulate表示Item已经发生了改变，需要更新。
1 Collections.*sort*(**mItems**, ***COMPARATOR***);是对Items的position进行排序。
1 由于初始化，会进入needPopulate的处理。
1 然后调用setCurrentItemInternal对默认的Item进行设置。也即是说，每次dataSetChanged时，都会设置一次CurrentItem。
设置CurrentItem的过程，会对每个Item分发当前选择的item，然后Item自己来根据是否选中的是自己来处理。
1 如果是第一次Layout，不需要调用populate方法。非第一次Layout，则在populate方法里对oldSelectItem和newSelectItem进行处理。
1 **private boolean mFirstLayout** = **true**;是表示是否第一次布局。仅两处设置：setAdapter会重置为true，onAttachToWindow会设置为true。

```java

    void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if (mAdapter == null || mAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        if (!always && mCurItem == item && mItems.size() != 0) {
            setScrollingCacheEnabled(false);
            return;
        }

// 这里，选中的item如果比Adapter的容量要大，则选中最后一个
        if (item < 0) {
            item = 0;
        } else if (item >= mAdapter.getCount()) {
            item = mAdapter.getCount() - 1;
        }
        final int pageLimit = mOffscreenPageLimit;
        if (item > (mCurItem + pageLimit) || item < (mCurItem - pageLimit)) {
            // We are doing a jump by more than one page.  To avoid
            // glitches, we want to keep all current pages in the view
            // until the scroll ends.
            for (int i = 0; i < mItems.size(); i++) {
                mItems.get(i).scrolling = true;
            }
        }
// 是否触发Selected，由mCurItem和item决定
        final boolean dispatchSelected = mCurItem != item;

        if (mFirstLayout) {
            // We don't have any idea how big we are yet and shouldn't have any pages either.
            // Just set things up and let the pending layout handle things.
            mCurItem = item;
            if (dispatchSelected) {
                dispatchOnPageSelected(item);
            }
            requestLayout();
        } else {
            populate(item);
            scrollToItem(item, smoothScroll, velocity, dispatchSelected);
        }
    }

```

dataSetChanged

```java
    void dataSetChanged() {
        // This method only gets called if our observer is attached, so mAdapter is non-null.

        final int adapterCount = mAdapter.getCount();
        mExpectedAdapterCount = adapterCount;
        boolean needPopulate = mItems.size() < mOffscreenPageLimit * 2 + 1
                && mItems.size() < adapterCount;
        int newCurrItem = mCurItem;

        boolean isUpdating = false;
        for (int i = 0; i < mItems.size(); i++) {
            final ItemInfo ii = mItems.get(i);
            final int newPos = mAdapter.getItemPosition(ii.object); // getItemPosition的影响

// 如果是UNCHANGED，不需额外处理，也不会触发needPopulate（重新填充）
            if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                continue;
            }
// 如果是NONE，则需要移除
            if (newPos == PagerAdapter.POSITION_NONE) {
                mItems.remove(i);
                i--;

                if (!isUpdating) {
                    mAdapter.startUpdate(this);
                    isUpdating = true;
                }

                mAdapter.destroyItem(this, ii.position, ii.object);
                needPopulate = true;

                if (mCurItem == ii.position) {
                    // Keep the current item in the valid range
                    newCurrItem = Math.max(0, Math.min(mCurItem, adapterCount - 1));
                    needPopulate = true;
                }
                continue;
            }
// 新Position既不是UNCHANGED，也不是NONE，而是指定的具体数值，那么才会执行下面的代码
            if (ii.position != newPos) {
                if (ii.position == mCurItem) {
                    // Our current item changed position. Follow it.
                    newCurrItem = newPos;
                }

                ii.position = newPos;
                needPopulate = true;
            }
        }

        if (isUpdating) {
            mAdapter.finishUpdate(this);
        }
// 重排剩余的Item
        Collections.sort(mItems, COMPARATOR);

        if (needPopulate) {
            // Reset our known page widths; populate will recompute them.
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!lp.isDecor) {
                    lp.widthFactor = 0.f;
                }
            }

            setCurrentItemInternal(newCurrItem, false, true);
            requestLayout();
        }
    }
```

由于每个Item都为PagerAdapter.POSITION_NONE，所以DataSetChanged时，都会被销毁，剩余的Items为空，所以即使需要重新填充，界面也没有任何效果。
但setCurrentItemInternal还是会执行，触发一次onPageSelected(int position)，这个触发和mItems是不是空没有关系。（怀疑这里是个Bug，没有Item还触发）
