## JAVA覆盖方法时，使用子类返回值

举例：

```java
// View.java
public ViewGroup.LayoutParams getLayoutParams() {
    return mLayoutParams;
}
```

覆盖View的方法为：

```java
@Override
public WindowManager.LayoutParams getLayoutParams() {
    ViewGroup.LayoutParams srcwparams = (ViewGroup.LayoutParams) super.getLayoutParams();
    if (srcwparams == null) {
        return null;
    }
    if (!(srcwparams instanceof WindowManager.LayoutParams)) {
        return null;
    }
    return (WindowManager.LayoutParams) srcwparams;
}
```

其中，父类方法返回的`ViewGroup.LayoutParams`是子类覆盖方法的返回类型`WindowManager.LayoutParams`的父类。


以上覆盖是可行的。

```java
setLayoutParams(new WindowManager.LayoutParams(0,0));
mWindowLayout.getLayoutParams();
((ViewGroup)mWindowLayout).getLayoutParams();
```

以上代码：

1. 首先，无论使用当前扩展类的`getLayoutParams()`，还是使用父类`ViewGroup.getLayoutParams()`，得到的都是一个`WindowManager.LayoutParams`实例。

2. 如果使用当前扩展类的`getLayoutParams()`，那么不需要进行类型转换，就可以得到`WindowManager.LayoutParams`类型的实例。如果使用父类的`ViewGroup.getLayoutParams()`方法，则得到的是`ViewGroup.LayoutParams`的类型，但是内在是一个`WindowManager.LayoutParams`实例。需要进行类型转换才能使用`WindowManager.LayoutParams`类型的方法。

3. 进行强制类型转换可能会产生`ClassCastException`。

上面的例子:

```java
@Override
public WindowManager.LayoutParams getLayoutParams() {
    ViewGroup.LayoutParams srcwparams = (ViewGroup.LayoutParams) super.getLayoutParams();
    if (srcwparams == null) {
        return null;
    }
    if (!(srcwparams instanceof WindowManager.LayoutParams)) {
        return null;
    }
    return (WindowManager.LayoutParams) srcwparams;
}
```

有一个问题是，如果`super.getLayoutParams();`得到的是`ViewGroup.LayoutParams`，那么这个`ViewGroup.LayoutParams`是无法获取的。因为无论当前扩展类`getLayoutParams()`还是使用父类`ViewGroup.getLayoutParams()`，实际上，都是调用了上面的覆盖的方法。由于覆盖的方法就是将`ViewGroup.LayoutParams`转换为`WindowManager.LayoutParams`返回，如果类型不对，就返回null。所以，如果你set进去一个`ViewGroup.LayoutParams`，则无论调用哪个`getLayoutParams`得到的都是null。
