[animator_interpolator]:animator_interpolator.png

## Android插值器Interpolator

1. 插值器（Interpolator）决定值的变化规律（匀速、加速blabla），即决定的是变化趋势；而接下来的具体变化数值则交给估值器（TypeEvaluator）。
2. 属性动画特有的属性

#### 使用
* xml里设置
* 代码中设置

```xml
<?xml version="1.0" encoding="utf-8"?>
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:interpolator="@android:anim/overshoot_interpolator"
    // 通过资源ID设置插值器
/>
```

```java
Animation alphaAnimation = new AlphaAnimation(1,0);
alphaAnimation.setDuration(3000);
Interpolator overshootInterpolator = new OvershootInterpolator();
alphaAnimation.setInterpolator(overshootInterpolator);
```


#### 内置插值器

![animator_interpolator][animator_interpolator]

#### 自定义插值器

```java
// Interpolator接口
public interface Interpolator {  

    // 内部只有一个方法
     float getInterpolation(float input) {  
         // 参数说明
         // input值值变化范围是0-1，且随着动画进度（0% - 100% ）均匀变化
        // 即动画开始时，input值 = 0；动画结束时input = 1
        // 而中间的值则是随着动画的进度（0% - 100%）在0到1之间均匀增加

      ...// 插值器的计算逻辑

      return xxx；
      // 返回的值就是用于估值器继续计算的fraction值，下面会详细说明
    }  

// TimeInterpolator接口
// 同上
public interface TimeInterpolator {  

    float getInterpolation(float input);  

}
```

系统的插值器

```java
// 匀速差值器：LinearInterpolator
@HasNativeInterpolator  
public class LinearInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {  
   // 仅贴出关键代码
  ...
    public float getInterpolation(float input) {  
        return input;  
        // 没有对input值进行任何逻辑处理，直接返回
        // 即input值 = fraction值
        // 因为input值是匀速增加的，因此fraction值也是匀速增加的，所以动画的运动情况也是匀速的，所以是匀速插值器
    }  


// 先加速再减速 差值器：AccelerateDecelerateInterpolator
@HasNativeInterpolator  
public class AccelerateDecelerateInterpolator implements Interpolator, NativeInterpolatorFactory {  
      // 仅贴出关键代码
  ...
    public float getInterpolation(float input) {  
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        // input的运算逻辑如下：
        // 使用了余弦函数，因input的取值范围是0到1，那么cos函数中的取值范围就是π到2π。
        // 而cos(π)的结果是-1，cos(2π)的结果是1
        // 所以该值除以2加上0.5后，getInterpolation()方法最终返回的结果值还是在0到1之间。只不过经过了余弦运算之后，最终的结果不再是匀速增加的了，而是经历了一个先加速后减速的过程
        // 所以最终，fraction值 = 运算后的值 = 先加速后减速
        // 所以该差值器是先加速再减速的
    }
}
```

自定义

```java
public class DecelerateAccelerateInterpolator implements TimeInterpolator {

    @Override
    public float getInterpolation(float input) {
        float result;
        if (input <= 0.5) {
            result = (float) (Math.sin(Math.PI * input)) / 2;
            // 使用正弦函数来实现先减速后加速的功能，逻辑如下：
            // 因为正弦函数初始弧度变化值非常大，刚好和余弦函数是相反的
            // 随着弧度的增加，正弦函数的变化值也会逐渐变小，这样也就实现了减速的效果。
            // 当弧度大于π/2之后，整个过程相反了过来，现在正弦函数的弧度变化值非常小，渐渐随着弧度继续增加，变化值越来越大，弧度到π时结束，这样从0过度到π，也就实现了先减速后加速的效果
        } else {
            result = (float) (2 - Math.sin(Math.PI * input)) / 2;
        }
        return result;
        // 返回的result值 = 随着动画进度呈先减速后加速的变化趋势
	}
}
```
