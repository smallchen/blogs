<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [抽离Android原生控件的方法](#抽离android原生控件的方法)
    - [Android源码](#android源码)
    - [Android原生控件的源码](#android原生控件的源码)
    - [为什么需要抽离原生控件](#为什么需要抽离原生控件)
    - [抽离原生Gallery控件](#抽离原生gallery控件)

<!-- /TOC -->

# 抽离Android原生控件的方法

## Android源码
在线源码：<http://androidxref.com/>
这个网站很有用，除了可以搜索某个类的源码，还可以定位到源码所在的目录。

## Android原生控件的源码
通过上面网站搜索，可以看到Android大部分Widget都在以下目录。
Widget源码目录：`/frameworks/base/core/java/android/widget/`
View源码目录：`/frameworks/base/core/java/android/view/`

## 为什么需要抽离原生控件
理由很简单，Android对原生控件进行了封装保护，有时候，仅仅通过继承并不能解决问题，一些私有变量，私有方法仍旧无法被访问。明明只需要对控件内部进行一些小改造就能完成任务，但由于访问限制，我们只能自定义控件，在实现原生控件的大部分功能基础上，再进行一些需求的定制。如果此时可以直接将原生控件从Android中抽离，那么工作量就会少很多。（反过来，即使不抽离原生控件，也需要自实现一个类似原生的自定义控件，除去工作量，还比不上原生的稳定性，综合起来，抽离是不错的思路）

## 抽离原生Gallery控件
Android原生的Gallery控件被标记为过时，如果需要使用，可以将其抽离。EcoGallery就是这样做的。

原生Widget都在同一个package里面，一些访问权限可以直接访问，而抽离后，需要进行特别处理。

有需要的可以使用`Beyong Compare`比较EcoGallery和原生Gallery的源码，就可以看到额外的处理。

**1. 对父类的Field属性访问替代为Method接口访问**

比如，Gallery里对布局属性的访问，是直接访问父类的属性。

```java
mLeft
mRight
mPaddingLeft
mPaddingRight
```

抽离时，可以替代为父类的方法访问。前提是父类有提供对应的访问接口。

```java
getLeft();
getRight();
getPaddingLeft();
getPandingRight();
```

这里其实不必要，既然Gallery里能够直接访问mPaddingLeft，意味着父类的该属性肯定不是private，所以可以不作修改。当且仅当父类的属性是private时，才需要转换为Method方式访问。出现这种情况，通常是新的SDK中，将访问权限修改了。

更新：
上面的理解是错误的，EcoGallery之所以需要使用方法访问，是因为Android SDK源码里，部分属性和接口会添加`@hide`注释。该注释的作用是在生成SDK时会将对应的属性和接口删除，所以我们的APP在使用SDK时，是无法找到标记为`@hide`的那些属性和接口，导致我们的APP无法通过编译。仅仅是无法通过编译而已，使用反射是可以访问的。这才是EcoGallery需要改变访问方式的真正原因。

如果想编译成功，可以自行编译原始的 `android.jar`替代SDK目录下对应的`android.jar`。

@hide的属性和方法意味着不稳定，在新版本可能被修改，所以直接访问@hide的属性和方法的APP的稳定性在新版本得不到保障。

另外，如果控件开源给别人使用，就不能使用替代android.jar的方式，因为别人拿到源码却编译不过就尴尬了。

**2. 私有的访问可以使用反射来修复**

如果新的SDK中，将访问权限关闭了，那么是不是没辙了。也不是，可以通过反射来访问。

```java
mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;


    /**
     * When set, the drawing method will call {@link #getChildDrawingOrder(int, int)}
     * to get the index of the child to draw for that iteration.
     *
     * @hide
     * ViewGroup源码
     */
    protected static final int FLAG_USE_CHILD_DRAWING_ORDER = 0x400;

     /**
     * When set, this ViewGroup supports static transformations on children; this causes
     * {@link #getChildStaticTransformation(View, android.view.animation.Transformation)} to be
     * invoked when a child is drawn.
     * ViewGroup源码
     */
    protected static final int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 0x800;
```


mGroupFlags在这里可能相当于Windows下的窗口样式，可以改变控件的行为。
以上操作，大致是指启用`自定义子View的绘制顺序`和`绘制前调用TRANSFORMATIONS`的特性。应该是分别对应以下的重写方法：

```java
    @Override
    protected int getChildDrawingOrder(int childCount, int i)

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t)
```

mGroupFlags是ViewGroup里面的属性，新的SDK中，通过继承是可以直接访问到的。

```java
     /**
     * Internal flags.
     *
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int mGroupFlags;
```

可能旧的SDK这个属性是private的，所以EcoGallery里是使用反射来访问和设置私有属性mGroupFlags的值。但反过来想觉得不是。因为原生Gallery里都可以直接访问mGroupFlags，说明它没有限制访问权限。所以，EcoGallery里这样操作，可能是防范于未然。

```java
        // We draw the selected item last (because otherwise the item to the
        // right overlaps it)
        int FLAG_USE_CHILD_DRAWING_ORDER = 0x400;
        int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 0x800;
        Class<ViewGroup> vgClass = ViewGroup.class;

        try {
            Field childDrawingOrder = vgClass.getDeclaredField("FLAG_USE_CHILD_DRAWING_ORDER");
            Field supportStaticTrans = vgClass.getDeclaredField("FLAG_SUPPORT_STATIC_TRANSFORMATIONS");

            childDrawingOrder.setAccessible(true);
            supportStaticTrans.setAccessible(true);

            FLAG_USE_CHILD_DRAWING_ORDER = childDrawingOrder.getInt(this);
            FLAG_SUPPORT_STATIC_TRANSFORMATIONS = supportStaticTrans.getInt(this);
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        try {
            // set new group flags
            Field groupFlags = vgClass.getDeclaredField("mGroupFlags");
            groupFlags.setAccessible(true);
            int groupFlagsValue = groupFlags.getInt(this);

            groupFlagsValue |= FLAG_USE_CHILD_DRAWING_ORDER;
            groupFlagsValue |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;

            groupFlags.set(this, groupFlagsValue);

            // working!
            mBroken = false;
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
```

具体如何使用反射来访问和设置私有属性，可以google。

代码中的`mBroken`标识，用来表示控件初始化是否完整，如果通过反射设置失败，意味着控件初始化是不完整的，外部可以通过这个标识来判断控件是否可用。

> 从这里想，或者，不需要完全抽离原生控件，继承原生控件，然后使用反射来访问私有权限，完成对原生控件内部的小修改，理论上应该也是可行的。

**3. 简化不必要的代码**

原生Gallery里，支持RTL特性，进行了额外的处理。EcoGallery抽离时，删掉了对RTL的支持。在抽离过程，可以对不需要使用的特性进行精简。

附录：
@hide详解 <http://blog.csdn.net/linghu_java/article/details/8283042>

**4 R改为com.android.internal.R**

android内部资源，访问为`com.android.internal.R`。

**5 注解使用support.annotations**

注解：
`implementation 'com.android.support:support-annotations:28.0.0-alpha3'
`
