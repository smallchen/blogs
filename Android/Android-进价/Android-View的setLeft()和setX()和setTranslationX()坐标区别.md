<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android View的setLeft()和setX()和setTranslationX()坐标区别](#android-view的setleft和setx和settranslationx坐标区别)
- [setLeft()和setX()](#setleft和setx)
- [扩展](#扩展)
    - [setX()与offsetLeftAndRight()](#setx与offsetleftandright)
    - [offsetLeftAndRight()与layout(l,t,r,b)](#offsetleftandright与layoutltrb)
    - [Left,Right,Top,Bottom](#leftrighttopbottom)
    - [Left,Right与X,Y的相互影响](#leftright与xy的相互影响)
- [测试](#测试)

<!-- /TOC -->

## Android View的setLeft()和setX()和setTranslationX()坐标区别

## setLeft()和setX()

```java
// View.java
public final void setLeft(int left) {
    if (left != mLeft) {
        int oldWidth = mRight - mLeft;
        int height = mBottom - mTop;

        mLeft = left;
        mRenderNode.setLeft(left);
        sizeChange(mRight - mLeft, height, oldWidth, height);
    }
}
public void setX(float x) {
    setTranslationX(x - mLeft);
}
```

区别是：

* `setX`是永久性的，在下一次布局刷新时仍旧**生效**。
* `setLeft`是修改到mLeft属性，属于临时性修改，在下一次布局时**失效**。

效果是：

`setX(150)`与`setLeft(150)`设置前后对比：

```java
[helloworld A]
[hellowrold B]
```

```java
|<- 150 ->|
          [helloworld A]
          [he]
```

区别是：

* `setX`是整个控件，相对于父容器偏移`X`。控件的位置发生变化，内容没压缩。
* `setLeft`是控件内容，相对于控件自身偏移`X`。控件的位置其实没变，只是内容被压缩。**如果偏移量大于长度，控件会消失！**

`requestLayout()`刷新后区别：

```java
|<- 150 ->|
          [helloworld A]
[hellowrold B]
```

* `setX`设置的值仍旧生效。
* `setLeft`设置的值失效。恢复原来的样子。

## 扩展

### setX()与offsetLeftAndRight()

`setX()`和`offsetLeftAndRight()`看起来的效果是一样的。

比如：`setX(150)`与`offsetLeftAndRight(150)`效果如下：

```java
|<- 150 ->|
          [helloworld A]
          [helloworld B]
```

> B的内容左边压缩150，右边扩张150，所以看起来就是平移了150.

效果一样，区别仍旧是`requestLayout()`后，前者不变，后者还原。

### offsetLeftAndRight()与layout(l,t,r,b)

`offsetLeftAndRight()`和`layout(left, top, right, bottom)`其实也是一样的。

还有另一个`offsetTopAndBottom()`。

这些都是控制一个View的`Left, Top, Right, Bottom`值。

同样，这些值在`requestLayout()`后失效。

`offsetLeftAndRight`和`layout(l,t,r,b)`效果一样，类似`补间动画`，都是临时性的。
`setX()/setY()`和`属性动画`效果一样，是永久的。

### Left,Right,Top,Bottom

Left，Right，Top，Bottom是用于描述当前View的轮廓。当前View的宽度是`width = Right - Left`，当前View的高度是`height = Top - Bottom`。

### Left,Right与X,Y的相互影响

* 修改`X,Y`的属性动画不影响`Left, Right`的值！
* layout(l, t, r, b)布局时不影响`X, Y`的值！

简而言之，他们是描述不同的场景，并没有直接的关联。

## 测试

```java
setLeft(50);
setX(100);
requestLayout();

[helloworld A]
| 50 |
     [hellowo]
|  100  |
        [hellowo]
| 50 |
     [helloworld A]
```

这里比较难理解。原因是：`setX()`的偏移，需要减去`Left`的值。看源码：

```java
public void setX(float x) {
    setTranslationX(x - mLeft);
}
```

`setX()`是相对于`mLeft`的偏移。

所以，上面例子理解是：

行1: Left为50，导致View被压缩。
行2: X为100，实际偏移为`X - Left`即50，所以在Left基础上再偏移50。
行3: requestLayout后，Left失效，View恢复，所以剩余实际偏移50。


```java
setX(100);
setLeft(50);
requestLayout();

[helloworld A]
|  100  |
        [helloworld A]
        | 50 |
             [hellowo]
|  100  |    
        [helloworld A]
```

比较容易理解：

行1: X为100，实际偏移为`X - Left`即100。
行2: Left为50，导致View被压缩。
行3: requestLayout后，Left失效，View恢复。剩余间隔为100。
