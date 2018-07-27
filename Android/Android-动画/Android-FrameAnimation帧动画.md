
## Android FrameAnimation 帧动画

##### 概述：
![animation_summary][animation_summary]

##### 原理：
逐帧显示图片，形成动画效果。

##### 动画对象类型

* AnimationDrawable

##### xml与代码类型：

* animation-list

##### 缺点

帧动画是一次性将所有图片加载到内容，如果图片资源比较大，比较多，很容易造成OOM。
所以，有一种方案是，自己实现帧动画，使用图片缓存来实现帧动画。

##### 使用

1. 把图片资源放在`drawable`目录下
2. `res/drawable`目录下，创建动画资源（也可能是`res/anim`下）
3. 赋值给layout中某个view的`background`，作为显示介质。
4. 代码中启动动画

```xml
// animatin_list.xml
<?xml version="1.0" encoding="utf-8"?>
<animation-list
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:oneshot="true"
    >
    <item android:drawable="@drawable/a0" android:duration="100"/>
    <item android:drawable="@drawable/a1" android:duration="100"/>
    <item android:drawable="@drawable/a2" android:duration="100"/>
    <item android:drawable="@drawable/a3" android:duration="100"/>
    <item android:drawable="@drawable/a4" android:duration="100"/>
</animation-list>
```

```xml
<ImageView  
    android:id="@+id/imageView"  
    android:layout_width="wrap_content"  
    android:layout_height="wrap_content"  
    android:background="@drawable/animation_list" />
```

```java
   animationDrawable = (AnimationDrawable) imageView.getBackground();
   animationDrawable.start();
```

方式2：代码中访问帧动画资源

```java
/**
 * 通过XML添加帧动画方法二
 */  
private void setXml2FrameAnim2() {  
    // 通过逐帧动画的资源文件获得AnimationDrawable示例  
    mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(  
            R.drawable.frame_anim);  
    imageView.setBackground(animationDrawable);
    // 设置给ImageView，作为显示介质。
}

private void start() {
    mAnimationDrawable.start();  
}
private void stop() {
    mAnimationDrawable.stop();
}
```

方式3：代码中创建和使用帧动画

```java
/**
 * 通过代码添加帧动画方法
 */  
private void setFrameAnimation() {  
    animationDrawable = new AnimationDrawable();  
    // 为AnimationDrawable添加动画帧  
    animationDrawable.addFrame(getResources().getDrawable(R.drawable.img00), 50);  
    animationDrawable.addFrame(getResources().getDrawable(R.drawable.img01), 50);  
    animationDrawable.addFrame(getResources().getDrawable(R.drawable.img02), 50);  
    // 设置为循环播放  
    animationDrawable.setOneShot(false);  
    imageView.setBackground(animationDrawable);
}
```
