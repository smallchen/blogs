## xml文件

```java
<set xmlns:android="http://schemas.android.com/apk/res/android"
     android:interpolator="@android:interpolator/decelerate_quad">
    <scale android:fromXScale="0" android:toXScale="1"
           android:fromYScale="0" android:toYScale="1"
           android:pivotX="0" android:pivotY="0.5"
           android:duration="200"/>
    <alpha android:fromAlpha="0.0" android:toAlpha="1.0"
            android:duration="200"/>
</set>
```
以上xml是错误的，几个问题：
1. set中，需要提供duration，否则兼容性差，在高版本的系统能够生效，在低版本的系统会认为整个动画的duration是0，然后动画不出现。
2. Scale中，android:pivotX是`0%-50%-100%`。如果设置为`0-0.5-1`不会生效！
3. set里面的<scale><alpha>，AS的输入提示没有duration（set已经提供了，AS就不会在scale里有提示）但仍旧可以写入，但好像不生效。如上，<scale duration=200>是不生效的，<alpha duration=200>也是不生效的（**低版本**）。

正确的：
```java
<set xmlns:android="http://schemas.android.com/apk/res/android"
     android:interpolator="@android:interpolator/decelerate_quad"
     android:duration="200">
    <scale android:fromXScale="0" android:toXScale="1"
           android:fromYScale="0" android:toYScale="1"
           android:pivotX="0%" android:pivotY="50%"/>
    <alpha android:fromAlpha="0.0" android:toAlpha="1.0"/>
</set>
```

## 窗口动画步骤

1. 定义xml动画，如上，放在`anim`目录下
2. 定义style，放在`values`目录下，命名`anims.xml`

```java
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Animation"/>
    <style name="Animation.LeftIN" parent="android:Animation">
        <item name="android:windowEnterAnimation">@anim/anim_left_enter</item>
        <item name="android:windowExitAnimation">@anim/anim_left_exit</item>
    </style>
    <style name="Animation.RightIN" parent="android:Animation">
        <item name="android:windowEnterAnimation">@anim/anim_right_enter</item>
        <item name="android:windowExitAnimation">@anim/anim_right_exit</item>
    </style>
</resources>
```
>
>关键点：
>1. 继承 android:Animation
>2. 自定义动画名称：Animation.LeftIN／Animation.RightIn
>2. 为自定义动画android:windowEnterAnimation和android:windowExitAnimation赋值

3. 调用setAnimationStyle将自定义动画设置到对应窗口，比如PopupWindow。

```java
PopupWindow mPopupWindow;
mPopupWindow.setAnimationStyle(R.style.Animation_LeftIN)
mPopupWindow.setAnimationStyle(R.style.Animation_RightIN)
```

**如果动画不生效**
1. 检查动画描述文件xml是否有错误。
2. 未知
