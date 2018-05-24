[animator_summary]: animator_summary.png

## Android Animator 属性动画

##### 概述：
![animator_summary][animator_summary]

##### 原理：
指定开始的视觉效果（开始值）和结束的视觉效果（结束值），中间动画是通过一个插值器的东西，不断的修改元素的属性，使得在onDraw的时候绘制出不同的视觉效果，由此形成动画。

##### 动画对象类型

* ValueAnimator
* ObjectAnimator（简化版的ValueAnimator）
* AnimatorSet
* PropertyValuesHolder

##### xml与对象类型：

* animator（ValueAnimator）
* objectAnimator（ObjectAnimator）
* set（AnimatorSet）

> 属性动画，比较少通过xml创建。

##### 回调
1. AnimatorListenerAdapter
2. AnimatorListener

##### 优点：
1. 动画过程，可以影响到坐标，宽高，`点击区域`等View属性。
2. 颗粒度更小，可以精准控制动画效果。
3. 动画过程就是不断执行绘制的过程，所以动画过程可以支持layout，draw等操作。

##### 通过xml创建属性动画
1, 将xml放在`res/animator`目录下
2, 通过代码使用

```xml
// int_animator.xml
<animator xmlns:android="http://schemas.android.com/apk/res/android"  
    android:valueFrom="0"  
    android:valueTo="100"  
    android:valueType="intType"/>  
```

```xml
// alpha_animator.xml
<objectAnimator xmlns:android="http://schemas.android.com/apk/res/android"  
    android:valueFrom="1"  
    android:valueTo="0"  
    android:valueType="floatType"  
    android:propertyName="alpha"/>
```

```xml
// set_animator.xml
<set xmlns:android="http://schemas.android.com/apk/res/android"  
    android:ordering="sequentially" >  

    <objectAnimator  
        android:duration="2000"  
        android:propertyName="translationX"  
        android:valueFrom="-500"  
        android:valueTo="0"  
        android:valueType="floatType" >  
    </objectAnimator>  

    <set android:ordering="together" >  
        <objectAnimator  
            android:duration="3000"  
            android:propertyName="rotation"  
            android:valueFrom="0"  
            android:valueTo="360"  
            android:valueType="floatType" >  
        </objectAnimator>  

        <set android:ordering="sequentially" >  
            <objectAnimator  
                android:duration="1500"  
                android:propertyName="alpha"  
                android:valueFrom="1"  
                android:valueTo="0"  
                android:valueType="floatType" >  
            </objectAnimator>  
            <objectAnimator  
                android:duration="1500"  
                android:propertyName="alpha"  
                android:valueFrom="0"  
                android:valueTo="1"  
                android:valueType="floatType" >  
            </objectAnimator>  
        </set>  
    </set>  
</set>  
```

代码中使用Animator或AnimatorSet

```java
Animator animator = AnimatorInflater.loadAnimator(context, R.animator.anim_file);  
animator.setTarget(view);  
animator.start();  

AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(SetActivity.this, R.animator.setanimator);
animatorSet.setTarget(textView);
animatorSet.start();
```
通过`AnimatorInflater`来加载得到`Animator`对象，然后使用。

##### ValueAnimator

```java
private void startAnimator(View view) {
	final ValueAnimator animator = ValueAnimator.ofInt(0, 100);
	animator.setDuration(5000);
	animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
	   @Override
	   public void onAnimationUpdate(ValueAnimator animation) {
		   Integer value = (Integer) animation.getAnimatedValue();
		   view.setText(value + "");
	   }
	});
	animator.start();
}
```
见另一篇，此处不再累赘。


##### ObjectAnimator

支持属性：
* alpha
* scale／scaleX／scaleY
* rotation／rotationX／rotationY
* translation／translationX／translationY


例子，渐变从1到0再到1.

```java
public void start(View view){
	ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1,0,1);
	animator.setDuration(2000);
	animator.start();
}
```

例子，旋转从0到180度再回到0

```java
public void start(View view){
	ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "rotation", 0,180,0);
	animator.setDuration(2000);
	animator.start();
}
```

例子，绕x轴旋转。

```java
ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "rotationX", 0,180,360);
        animator.setDuration(2000);
        animator.start();
```

例子，动态变化的圆
1. 重写onDraw，根据自定义到半径属性绘制一个圆
2. 创建一个ObjectAnimator作用于半径属性
3. 在重绘的过程，圆就会因为半径的改变而改变。

```java
public static class Point {
    private float radius;

    public Point(float radius) {
        this.radius = radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }
}

public class MyPointView extends View{
    //创建圆形对象，半径为100
    private Point point = new Point(100);
    public MyPointView(Context context) {
        super(context);
    }
    public MyPointView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        //以300，300为圆心，以当前圆的半径为半径，画一个圆
        canvas.drawCircle(300,300, point.getRadius(), paint);
        super.onDraw(canvas);
    }
    public void setPointRadius(float radius){
        point.setRadius(radius);
        invalidate(); // 重绘
    }
}

public class MainActivity extends ActionBarActivity {
    private MyPointView pointView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    pointView = (MyPointView) findViewById(R.id.pointview);
    }
    public void start(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(pointView, "pointRadius", 0,300,100);
        animator.setDuration(2000);
        animator.start();
    }
}
```

例子，动态改变背景色
1. 创建一个背景色插值器
2. 使用插值器创建一个ObjectAnimator
3. 作用于背景属性

```java
public static class ArgbEvaluator implements TypeEvaluator {  
    public Object evaluate(float fraction, Object startValue, Object endValue) {  
        int startInt = (Integer) startValue;  
        int startA = (startInt >> 24);  
        int startR = (startInt >> 16) & 0xff;  
        int startG = (startInt >> 8) & 0xff;  
        int startB = startInt & 0xff;  

        int endInt = (Integer) endValue;  
        int endA = (endInt >> 24);  
        int endR = (endInt >> 16) & 0xff;  
        int endG = (endInt >> 8) & 0xff;  
        int endB = endInt & 0xff;  

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |  
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |  
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |  
                (int)((startB + (int)(fraction * (endB - startB))));  
    }  
}

public void start(view) {
	ObjectAnimator animator = ObjectAnimator.ofInt(view, "backgroundColor", 0xffff00ff, 0xffffff00, 0xffff00ff);
        animator.setDuration(2000);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();
}
```

##### AnimatorSet

```java
ObjectAnimator animatorA = ObjectAnimator.ofFloat(textView, "TranslationX", -300, 300, 0);
ObjectAnimator animatorB = ObjectAnimator.ofFloat(textView, "scaleY", 0.5f, 1.5f, 1f);
ObjectAnimator animatorC = ObjectAnimator.ofFloat(textView, "rotation", 0, 270, 90, 180, 0);

AnimatorSet animatorSet2 = new AnimatorSet();
animatorSet2.playTogether(animatorA, animatorB, animatorC);
animatorSet2.setDuration(3*1000);
animatorSet2.start();
```

##### PropertyValuesHolder

可变参数values确定了一组指定时刻的属性值，比如代码中的angels为 [0，20f],那么在事件duration内的初始值和终值为0何20度，如果中间插了一个15呢?就是在动画的前半段，角度由0变化到15度，后半段由15度变化到20。至于更多属性值的情况依次类推。由此可见，在时间维度上PropertyValuesHolder可以确定关键时间点的值，这样的好处是什么呢？举个例子：忽大忽小心跳动画,用PropertyValuesHolder就很方便实现，scales可以设置从数值波动的数组(eg:[1.0f, 1.2f,1.0f,0.8f])。

```java
public static void startStarAnimator(View view, int duration, boolean isShow ,Animator.AnimatorListener listener){
    PropertyValuesHolder phTranslationY = null;
    PropertyValuesHolder phAlpha = null;
    PropertyValuesHolder phScaleX = null;
    PropertyValuesHolder phScaleY = null;
    if(isShow){
        phTranslationY = PropertyValuesHolder.ofFloat("translationY",-view.getMeasuredHeight()*1.6f, 0);
        phAlpha = PropertyValuesHolder.ofFloat("alpha",0.2f,1.0f);
        phScaleX = PropertyValuesHolder.ofFloat("scaleX", 1.2f, 1.0f);
        phScaleY = PropertyValuesHolder.ofFloat("scaleY",1.2f,1.0f);
    }else{
        phTranslationY = PropertyValuesHolder.ofFloat("translationY", 0, -view.getMeasuredHeight()*1.6f);
        phAlpha = PropertyValuesHolder.ofFloat("alpha",1.0f,0f);
        phScaleX = PropertyValuesHolder.ofFloat("scaleX",1.0f,1.2f);
        phScaleY = PropertyValuesHolder.ofFloat("scaleY",1.0f,1.2f);
    }

    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, phTranslationY, phAlpha, phScaleX,phScaleY);
    if(listener != null){
        animator.addListener(listener);
    }
    animator.setDuration(duration).start();
}
```

```java
/**PropertyValuesHolder这个类可以先将动画属性和值暂时的存储起来，后一起执行，在有些时候可以使用替换掉AnimatorSet，减少代码量*/
private void showPropertyValuesHolderAnim() {
	 //keyframe
	 Keyframe keyframe1 = Keyframe.ofFloat(0.0f,0);
	 Keyframe keyframe2 = Keyframe.ofFloat(0.25f,-30);
	 Keyframe keyframe3 = Keyframe.ofFloat(0.5f,0);
	 Keyframe keyframe4 = Keyframe.ofFloat(0.75f, 30);
	 Keyframe keyframe5 = Keyframe.ofFloat(1.0f,0);
	 PropertyValuesHolder rotation = PropertyValuesHolder.ofKeyframe("rotation", keyframe1, keyframe2, keyframe3, keyframe4,keyframe5);

	 PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha",1.0f,0.2f,1.0f);
	 PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX",1.0f,0.2f,1.0f);
	 PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY",1.0f,0.2f,1.0f);
	 PropertyValuesHolder color = PropertyValuesHolder.ofInt("BackgroundColor", 0XFFFFFF00, 0XFF0000FF);

	 ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mButton, alpha, scaleX, scaleY,color,rotation);
	 animator.setInterpolator(new OvershootInterpolator());
	 animator.setDuration(5000).start();
}
```

```java
public void transAnimRun(final View view, final float width, int distance)
{
	int left = view.getLeft();
	PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", left, left+distance);
	PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f, 0);
	ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhZ);

	anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
	{
		@Override
		public void onAnimationUpdate(ValueAnimator animation)
		{
			view.setScaleX(width);
			view.setScaleY(width);
		}
	});
    anim.setDuration(CollapseAnimationDuration);
    anim.start();
}

public void scaleAnimRun(final View view, final float scale) {
	PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f, scale);
	PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1f, scale);
	PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
	ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ);

    anim.addListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
        }
        @Override
        public void onAnimationEnd(Animator animator) {
            if (mListener != null) {
                mListener.onCollapseAnimatonEnd();
            }
        }
        @Override
        public void onAnimationCancel(Animator animator) {
        }
        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    });
    anim.setDuration(CollapseAnimationDuration);
    anim.start();
}
```
