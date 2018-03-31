## Android 滑动删除实现
滑动删除，需要在View的基础上，实现擦除。效果是擦除View的一部分。既可以理解成，隐藏擦除的部分，也可以理解成裁剪掉擦除的部分。

## 滑动删除实现步骤

1. 继承ImageView。
2. 重写ImageView的onDraw方法，重写其绘制过程。（注：onDraw只影响ImageView的src，不会影响ImageView的background）

```java
@Override
protected void onDraw(Canvas canvas) {
    Drawable drawable = getDrawable();
    if (drawable == null) {
        return; // couldn't resolve the URI
    }

    if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
        return;     // nothing to draw (empty bounds)
    }

    // 保存现场（canvas是外部传进来的，不要改之）
    int saveCount = canvas.getSaveCount();
    canvas.save();

    // 固定大小，防止图片按照实际尺寸放大。
    drawable.setBounds(0, 0, getWidth(), getHeight());
    // 裁剪
    canvas.clipRect(getScrollX() + getPaddingLeft(), getScrollY() + getPaddingTop(),
            getScrollX() + getRight() - getLeft() - getPaddingRight(),
            getScrollY() + getBottom() - getTop() - getPaddingBottom());
    drawable.draw(canvas);
    
    // 恢复现场
    canvas.restoreToCount(saveCount);
}
```

原ImageView::draw方法：

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas); // 空实现

    if (mDrawable == null) {
        return; // couldn't resolve the URI
    }

    if (mDrawableWidth == 0 || mDrawableHeight == 0) {
        return;     // nothing to draw (empty bounds)
    }

    if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
        mDrawable.draw(canvas);
    } else {
        final int saveCount = canvas.getSaveCount();
        canvas.save();

        if (mCropToPadding) {
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;
            canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                    scrollX + mRight - mLeft - mPaddingRight,
                    scrollY + mBottom - mTop - mPaddingBottom);
        }

        // 画布移动 
        canvas.translate(mPaddingLeft, mPaddingTop);

        // 图形缩放
        if (mDrawMatrix != null) {
            canvas.concat(mDrawMatrix);
        }
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
```

关键点：

1. ImageView的onDraw方法可以访问私有变量，即使覆盖重写，也未必能够访问，好在，ImageView有提供public方法来获取对应的私有属性，如下。
```java
mDrawable == getDrawable()
mDrawableWidth == getDrawable().getIntrinsicWidth()
mDrawableHeight == getDrawable().getIntrinsicHeight()
mDrawMatrix == getImageMatrix()

mLeft  == getLeft()
mRight == getRight()
mScrollX == getScrollX()
mScrollY == getScrollY()
mPaddingLeft == getPaddingLeft()
mPaddingTop == getPaddingTop()
```

2. canvas.clipRect裁剪区域
3. canvas.translate移动画布，看到图片随着布局偏移，就是这里
4. canvas.concat缩放，看到图片随着区域变小而变小，或者scaleType就是这里的作用
