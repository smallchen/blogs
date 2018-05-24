
#### ValueAnimator


#### ValueAnimator的创建

```java
mAnimator = ValueAnimator.ofInt(0, 100);
mAnimator.setDuration(AnimationTime);
mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
mAnimator.addListener(mAnimatorListener);
mAnimator.addUpdateListener(mAnimatorUpdater);
```

动画报错：
`java.lang.NullPointerException: Attempt to get length of null array`

跟到代码是`mValues`为null。

```java
@CallSuper
void initAnimation() {
	if (!mInitialized) {
		int numValues = mValues.length;
		for (int i = 0; i < numValues; ++i) {
			mValues[i].init();
		}
		mInitialized = true;
	}
}
```

原因是，使用了以下代码，没有调用到setValue.

```java
mAnimator = ValueAnimator.();
mAnimator.ofInt(0, 100);
mAnimator.setDuration(AnimationTime);
```

以上`ofInt`是静态方法。本质是调用了`setIntValues`。由于没有设置参数，所以会导致访问越界。

```java
public static ValueAnimator ofInt(int... values) {
	ValueAnimator anim = new ValueAnimator();
	anim.setIntValues(values);
	return anim;
}
```

所以，归根到底，是接口使用错误，导致了NPE错误。


#### 例子

```java

private ValueAnimator mAnimator;
private AnimatorUpdater mAnimatorUpdater = new AnimatorUpdater();

private void initAnimator() {
	mAnimator = ValueAnimator.ofInt(0, 100);
	mAnimator.setDuration(AnimationTime);
	mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	mAnimator.addListener(mAnimatorListener);
	mAnimator.addUpdateListener(mAnimatorUpdater);
}

protected class AnimatorUpdater implements ValueAnimator.AnimatorUpdateListener {
    private int mStartWidth;
    private int mEndWidth;
    private float mStartAlpha;
    private float mEndAlpha;
    private boolean mExpand = false;
    private boolean mCollapse = false;
    private boolean mActivate = false;
    private boolean mUnactivate = false;

    public void setData(int fromWidth, int toWidth, float fromAlpha, float toAlpha) {
        mStartWidth = fromWidth;
        mEndWidth = toWidth;
        mStartAlpha = fromAlpha;
        mEndAlpha = toAlpha;
        setFlags();
    }

    public void setFlags() {
        mExpand = false;
        mCollapse = false;
        mActivate = false;
        mUnactivate = false;
        if (mStartWidth < mEndWidth) {
            mExpand = true;
        } else if (mStartWidth > mEndWidth) {
            mCollapse = true;
        }
        if (mStartAlpha < mEndAlpha) {
            mActivate = true;
        } else if (mStartAlpha > mEndAlpha) {
            mUnactivate = true;
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (! animation.isRunning()) {
            return;
        }
        Log.e("onAnimationUpdate", AnimationController.this.mToolbar.getToolbarSide()+"-"+animation.getAnimatedValue());
        Log.e("onAnimationUpdatelact", AnimationController.this.mToolbar.getToolbarSide()+"="+animation.getAnimatedFraction());

        float interpolatedTime = animation.getAnimatedFraction();
        int newWidth = mStartWidth + (int) ((mEndWidth - mStartWidth) * interpolatedTime);
        float newAlpha = mStartAlpha + (mEndAlpha - mStartAlpha) * interpolatedTime;
        if (newAlpha < mEndAlpha) {
            newAlpha = mEndAlpha;
        }
        if (mActivate || mUnactivate) {
            mToolbar.setAlpha(newAlpha);
            mExpandPanel.setAlpha(newAlpha);
        }

        // ExpandPanel跟随面板渐变，但展开或缩回时，渐变需要特殊处理。
        if (mExpand && mActivate) {
            // 0.3 - 0
            float expandAlpha = AlphaUnactivate + (0 - AlphaUnactivate) * interpolatedTime;
            mExpandPanel.setAlpha(expandAlpha);
        } else if (mCollapse && mUnactivate) {
            // 0.7 - 0.3 由于叠加关系，比1.0小一点更完美
            float expandAlpha = AlphaUnactivate + (0.7f - AlphaUnactivate) * interpolatedTime;
            mExpandPanel.setAlpha(expandAlpha);
        }

        if (mExpand || mCollapse) {
            ViewGroup.LayoutParams params = mMarkPanel.getLayoutParams();
            params.width = newWidth;
            mMarkPanel.setLayoutParams(params);
        }
    }
};
```
