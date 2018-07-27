[animation_summary]: animation_summary.png

## Android Animation 补间动画

##### 概述：
![animation_summary][animation_summary]

##### 原理：
指定开始的视觉效果（开始值）和结束的视觉效果（结束值），中间动画变化过程由系统补全来确定一个动画。

##### 动画对象类型

* TranslateAnimation
* ScaleAnimation
* RotateAnimation
* AlphaAnimation
* AnimationSet

##### xml与代码类型：

* translate（TranslateAnimation）
* scale（ScaleAnimation）
* rotate（RotateAnimation）
* alpha（AlphaAnimation）
* set (AnimationSet)

##### 缺点：
1. 只能作用于View。
2. 不可以作用于View的属性，如：颜色／宽高／背景等等。
3. 动画过程不改变View的属性。如，动画过程：坐标，宽高，`可点击区域`等等不变。
4. 动画过程不能执行layout，否则动画失效。也即是说，动画过程不能调用requestLayout等。


> **如果以上缺点不能满足需求，那么就不能使用补间动画来实现！**

##### 应用场景：
1. Activity的切换效果
2. Fragment的切换效果
3. PopupWindow的切换效果
4. ViewGroup子元素的出场效果，比如：LinearLayout等布局中子元素的出场效果。（布局动画）

##### 性能
曾经试过将属性动画改为补间动画，但效率并没有明显提高。关键点始终是动画过程中Android的UI渲染，而不是动画过程中View属性的改变。

##### 使用
1. pivotX pivotY,可取值为数字，百分比，或者百分比p。

1. 设置为数字时（如50），轴点为View的左上角的原点在x方向和y方向加上50px的点。在Java代码里面设置这个参数的对应参数是`Animation.ABSOLUTE`。

2. 设置为百分比时（如50%），轴点为View的左上角的原点在x方向加上自身宽度50%和y方向自身高度50%的点。在Java代码里面设置这个参数的对应参数是`Animation.RELATIVE_TO_SELF`。

3. 设置为百分比p时（如50%p），轴点为View的左上角的原点在x方向加上父控件宽度50%和y方向父控件高度50%的点。在Java代码里面设置这个参数的对应参数是`Animation.RELATIVE_TO_PARENT`

4. 以上，xml里的50，50%，50%p，0.5大致可以明白并且区分了！

##### 回调
1. AnimationListenerAdapter
2. AnimationListener

##### xml里创建Animation

例子1，底部中心放大弹出，缩放到底部中心关闭的效果

1, res/anim/scale_pop_enter.xml
2, res/anim/scale_pop_exit.xml
3, res/values/anim_styles.xml
4, Activity/PopupWindow里面使用style

> **注意，xml里的值，部分是xx%，部分是0.xx，需要注意的是，不能乱用，否则动画可能失效，或者在版本不同的时候动画失效这类兼容性问题。**
> 比如，缩放0.xx表示缩放倍数。pivotX=50%表示相对于自身50%。alpha中0.x表示透明度。

```xml
// scale_pop_enter.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="250">
    <scale
        android:fromXScale="0"
        android:fromYScale="0"
        android:pivotX="50%"
        android:pivotY="100%"
        android:toXScale="1"
        android:toYScale="1" />

    <alpha
        android:fromAlpha="0.1"
        android:toAlpha="1.0" />
</set>
```

```xml
// scale_pop_exit.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="250">
    <scale
        android:fromXScale="1"
        android:fromYScale="1"
        android:pivotX="50%"
        android:pivotY="100%"
        android:toXScale="0"
        android:toYScale="0" />
    <alpha
        android:fromAlpha="1.0"
        android:toAlpha="0.1" />
</set>
```

```xml
// anim_styles.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="anim_pop_scale">
        <item name="android:windowEnterAnimation">@anim/scale_pop_enter</item>
        <item name="android:windowExitAnimation">@anim/scale_pop_exit</item>
    </style>
</resources>
```

```java
mPopupWindow = new PopupWindow();
mPopupWindow.setAnimationStyle(R.style.anim_pop_scale);
```

例子2，从下冒出，往下消失动画

```xml
// down2up.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="200"
    android:interpolator="@android:interpolator/accelerate_decelerate">
    <translate
        android:fromYDelta="50%"
        android:toYDelta="0" />
    <alpha android:fromAlpha="0.0" android:toAlpha="1.0"
           android:duration="@android:integer/config_shortAnimTime"/>
</set>
```

```xml
// up2down.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="200"
    android:interpolator="@android:interpolator/accelerate_decelerate">
    <translate
        android:fromYDelta="0"
        android:toYDelta="50%" />
    <alpha android:fromAlpha="1.0" android:toAlpha="0"
           android:duration="@android:integer/config_shortAnimTime"/>
</set>
```

例子3，从左冒出，往左消失动画

```xml
// left2right.xml
<set xmlns:android="http://schemas.android.com/apk/res/android"
        android:interpolator="@android:interpolator/decelerate_quad">
    <translate
        android:fromXDelta="-75%"
        android:toXDelta="0"
        android:duration="@android:integer/config_shortAnimTime"/>
    <alpha android:fromAlpha="0.0"
           android:toAlpha="1.0"
           android:duration="@android:integer/config_shortAnimTime"/>
</set>
```

```xml
// right2left.xml
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:fromXDelta="0%"
        android:toXDelta="-75%"
        android:duration="200"/>
    <alpha android:fromAlpha="1.0"
           android:toAlpha="0"
           android:duration="200"/>
</set>
```

##### 代码里使用xml中定义的动画

```xml
// translate_animation.xml
<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="3000"
    android:fromXDelta="0"
    android:toXDelta="500"
/>
```

```java
Button mButton = (Button) findViewById(R.id.Button);
Animation translateAnimation = AnimationUtils.loadAnimation(this, R.anim.translate_animation);
mButton.startAnimation(translateAnimation);
```

使用`AnimationUtils`加载动画资源，得到`Animation`对象。效果和在代码里创建`Animation`对象一样。

##### Activity启动／退出动画

```java
Intent intent = new Intent (this,Acvtivity.class);
startActivity(intent);
overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);
```
注意：overridePendingTransition()必须要在startActivity(intent)后被调用才能生效

```java
@Override
public void finish(){
    super.finish();
    overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);
}
```
注意：overridePendingTransition()必须要在finish()后被调用才能生效

##### Activity滑动启动／退出效果

```xml
// out_to_left.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="500"
        android:fromXDelta="0%p"
        android:toXDelta="-100%p"/>
</set>
```

```xml
// in_from_right.xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="500"
        android:fromXDelta="100%p"
        android:toXDelta="0%p"/>
</set>
```

```java
overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
```

##### Android系统原生动画资源

`android.R.anim.xxx`

比如，淡入淡出：
`overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);`

比如，从左向右滑动的效果：
`overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);`

##### Fragment动画切换效果

通过setTransition(int transit)进行设置。transit参数：

1. FragmentTransaction.TRANSIT_NONE：无动画
2. FragmentTransaction.TRANSIT_FRAGMENT_OPEN：标准的打开动画效果
3. FragmentTransaction.TRANSIT_FRAGMENT_CLOSE：标准的关闭动画效果

标准动画设置好后，在Fragment添加和移除的时候都会有。

```java
FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
fragmentTransaction.setTransition(int transit)；
```

```java
FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
fragmentTransaction.setCustomAnimations(
                R.anim.in_from_right,
                R.anim.out_to_left);
```


##### 视图组（ViewGroup）中子元素的出场效果

1. 创建一个view动画xml资源（普通Animation）
2. 创建一个布局动画xml资源（layoutAnimation）
3. 在layout的xml中使用动画资源（layout）
4. 或者在代码中指定动画资源

```xml
// item view动画
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="300">
    <alpha
        android:fromAlpha="1.0"
        android:toAlpha="0.0" />
    <translate
        android:fromXDelta="500"
        android:toXDelta="0"/>
</set>
```

```xml
// layout view动画
<?xml version="1.0" encoding="utf-8"?>
// 采用LayoutAnimation标签
<layoutAnimation xmlns:android="http://schemas.android.com/apk/res/android"
    android:delay="0.5"
    android:animationOrder="normal"
    android:animation="@anim/view_animation"
/>
```
其中，delay表示子元素动画延时。0.5表示占总动画时间的百分比。如上，第一个子元素延迟150ms播放入场效果；第二个延迟300ms，以此类推

animationOrder表示子元素的动画顺序。
1. normal ：顺序显示，即排在前面的子元素先播放入场动画
2. reverse：倒序显示，即排在后面的子元素先播放入场动画
3. random：随机播放入场动画

```xml
// layout_linear.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF"
    android:orientation="vertical" >
    <ListView
        android:id="@+id/listView1"
        android:layoutAnimation="@anim/anim_layout"
        // 指定layoutAnimation属性用以指定子元素的入场动画
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</LinearLayout>
```

代码中使用：

```java
ListView lv = (ListView) findViewById(R.id.listView1);

Animation animation = AnimationUtils.loadAnimation(this,R.anim.anim_item);
 // 加载子元素的出场动画

LayoutAnimationController controller = new LayoutAnimationController(animation);
controller.setDelay(0.5f);
controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
// 设置LayoutAnimation的属性

lv.setLayoutAnimation(controller);
// 为ListView设置LayoutAnimation的属性
```
1. 先获取一个Animation
2. 再通过这个Animation获取一个LayoutAnimationController
3. 将LayoutAnimationController设置到Layout里面

这个页面不错。
<https://blog.csdn.net/carson_ho/article/details/72827747>
