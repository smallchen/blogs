
## Android 估值器 TypeEvaluator

1. 插值器（Interpolator）决定值的变化规律（匀速、加速blabla），即决定的是变化趋势；而接下来的具体变化数值则交给估值器
2. 属性动画特有的属性

估值器是根据当前插值器系数，动画初始值，动画结束值来计算出当前时刻的值。

最核心的是系数。系数是从0-1的float值。有了系数，可以直接计算：
`当前值 = 开始值 +（结束值 - 开始值）* 系数`

ValueAnimator里面的onAnimationUpdate(ValueAnimator animator)可以通过animator获取到系数和值。可以忽略值，自己通过系数获取新的值。

```java
animation.getAnimatedFraction(); // 系数
animation.getAnimatedValue(); // 经过TypeEvaluator得到的值
```

#### 使用

```java
ObjectAnimator anim = ObjectAnimator.ofObject(myView2, "height", new Evaluator()，1，3);
// 在第4个参数中传入对应估值器类的对象
// 系统内置的估值器有3个：
// IntEvaluator：以整型的形式从初始值 - 结束值 进行过渡
// FloatEvaluator：以浮点型的形式从初始值 - 结束值 进行过渡
// ArgbEvaluator：以Argb类型的形式从初始值 - 结束值 进行过渡
```

#### 系统内置

```java
// FloatEvaluator
public class FloatEvaluator implements TypeEvaluator {  
// FloatEvaluator实现了TypeEvaluator接口

// 重写evaluate()
    public Object evaluate(float fraction, Object startValue, Object endValue) {  
// 参数说明
// fraction：表示动画完成度（根据它来计算当前动画的值）
// startValue、endValue：动画的初始值和结束值
        float startFloat = ((Number) startValue).floatValue();  

        return startFloat + fraction * (((Number) endValue).floatValue() - startFloat);  
        // 初始值 过渡 到结束值 的算法是：
        // 1. 用结束值减去初始值，算出它们之间的差值
        // 2. 用上述差值乘以fraction系数
        // 3. 再加上初始值，就得到当前动画的值
    }  
}  
```

#### 自定义

```java
public interface TypeEvaluator {  

    public Object evaluate(float fraction, Object startValue, Object endValue) {  
// 参数说明
// fraction：插值器getInterpolation（）的返回值
// startValue：动画的初始值
// endValue：动画的结束值

        ....// 估值器的计算逻辑

        return xxx；
        // 赋给动画属性的具体数值
        // 使用反射机制改变属性变化

// 特别注意
// 那么插值器的input值 和 估值器fraction有什么关系呢？
// 答：input的值决定了fraction的值：input值经过计算后传入到插值器的getInterpolation（），然后通过实现getInterpolation（）中的逻辑算法，根据input值来计算出一个返回值，而这个返回值就是fraction了
    }  
}  
```

```java
// 实现TypeEvaluator接口
public class PointEvaluator implements TypeEvaluator {

    // 复写evaluate（）
    // 在evaluate（）里写入对象动画过渡的逻辑
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {

        // 将动画初始值startValue 和 动画结束值endValue 强制类型转换成Point对象
        Point startPoint = (Point) startValue;
        Point endPoint = (Point) endValue;

        // 根据fraction来计算当前动画的x和y的值
        float x = startPoint.getX() + fraction * (endPoint.getX() - startPoint.getX());
        float y = startPoint.getY() + fraction * (endPoint.getY() - startPoint.getY());

        // 将计算后的坐标封装到一个新的Point对象中并返回
        Point point = new Point(x, y);
        return point;
    }

}
```

####
