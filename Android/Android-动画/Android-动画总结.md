## Android动画总结

### 动画种类

* 帧动画
    * animation-list (AnimationDrawable)
    * 目录`res/drawable`

* 补间动画
    * translate（TranslateAnimation）
    * scale（ScaleAnimation）
    * rotate（RotateAnimation）
    * alpha（AlphaAnimation）
    * set (AnimationSet)
    * 有`ABSOLUTE/RELATIVE_TO_SELF/RELATIVE_TO_PARENT`
    * 目录`res/anim`

* 属性动画
    * animator（ValueAnimator）
    * objectAnimator（ObjectAnimator）
    * set（AnimatorSet）
    * 无 (PropertyValuesHolder)
    * 目录`res/animator`

### 动画的选择

属性动画
1. 动画过程还需要处理触摸事件，那么只能使用属性动画。
2. 动画过程还需要刷新布局，那么只能使用属性动画。
3. 动画过程还需要精细的动画控制，那么只能使用属性动画。
4. 根据某个自定义属性进行动画，那么只能使用属性动画。

补间动画
5. Activity／Fragment／ViewGroup／LinearLayout等启动／退出／添加／删除等动画，通常使用补间动画。

帧动画
6. 视图复杂变换，内容复杂改变的动画，通常使用帧动画实现。


### 动画的创建和使用

1. 帧动画通常使用xml创建，在代码中加载并使用
2. 补间动画也通常使用xml创建，在代码中加载并使用
3. 属性动画通常直接在代码中创建和使用
