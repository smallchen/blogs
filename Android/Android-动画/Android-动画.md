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
