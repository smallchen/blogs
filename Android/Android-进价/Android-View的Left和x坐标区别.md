## Android View的Left和x坐标区别

修改Left，Right，在RequestLayout后会重新布局。
修改X，Y，是真正修改View属性。

offsetLeftAndRight 和 layout(l,t,r,b)效果一样。
setX()和属性动画效果一样。

属性动画不影响Left Right。
layout布局不影响X Y。
