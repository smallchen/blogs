## Android setLayoutParams与getLayoutParams死循环解决

### View设置LayoutParams的三种方式：

通常，为了易用，View的LayoutParams布局，可以通过三种方式设置或修改：

1、先设置View的LayoutParams，然后添加到Parent中：

```java
child.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
parent.addView(child);
```

2、添加View到Parent的同时，设置新的LayoutParams：

```java
parent.addView(child, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
```

此时，child view原先里面的`LayoutParams`将被覆盖。

3、当View已经被添加到Parent中，可以直接通过`View.setLayoutParams(LayoutParams params)`来更新View的布局：

```java
LayoutParams params = child.getLayoutParams();
params.width = WRAP_PARENT;
child.setLayoutParams(params);
```

以上三种方式，设置或修改结束，`View.getLayoutParams()`就是当前设置或修改的`LayoutParams`。

### `view.setLayoutParams()`和`child.requestLayout()`区别：

上面第三种：

```java
LayoutParams params = child.getLayoutParams();
params.width = WRAP_PARENT;
child.setLayoutParams(params);
```

此时，params是child view里面的`LayoutParams`的一个 **引用**，修改的时候，其实已经更新了view里面的`LayoutParams`;调用`setLayoutParams`只是为了通知更新UI！！

也可以写成：

```java
LayoutParams params = child.getLayoutParams();
params.width = WRAP_PARENT;
child.requestLayout();
```

`child.setLayoutParams()`和`child.requestLayout()`的区别是，`setLayoutParams()`可以触发`resolveLayoutDirection`，`onSetLayoutParams`等回调，具体看源码：

```java
public void setLayoutParams(ViewGroup.LayoutParams params) {
    if (params == null) {
        throw new NullPointerException("Layout parameters cannot be null");
    }
    mLayoutParams = params;
    // 触发`resolveLayoutDirection()`
    resolveLayoutParams();
    if (mParent instanceof ViewGroup) {
        // 触发`onSetLayoutParams()`
        ((ViewGroup) mParent).onSetLayoutParams(this, params);
    }
    // 最终，还是调用`requestLayout()`而已！！！
    requestLayout();
}
```

### WindowManager的改造

```java
- WindowManager.java
    - WindowManagerImpl.java
        - WindowManagerGlobal.java
            - view.setLayoutParams(params);
                - ViewRootImpl.setLayoutParams()
                - // 不管params是新对象，还是旧对象，都是和ViewRootImpl里面的mWindowAttributes对比
                - // 根据差异来决定是否进行更新。所以上层可以放心使用同一个，或不同的LayoutParams。
                - mWindowAttributesChangesFlag = mWindowAttributes.copyFrom(params);
```

最重要，是要解决这个问题：

**从View里面获取一个`LayoutParams`，修改后设置回去可以对UI进行更新，但不会导致死循环**

```java
WindowManager.LayoutParams params = (WindowManager.LayoutParams)child.getLayoutParams();
params.width = WRAP_PARENT;
child.setLayoutParams(params);
```



如果使用`WindowManager`将一个`View`添加显示，并不符合上面的`LayoutParams`的三种方式。`WindowManager`的添加，只符合

为了做到这点，重写`setLayoutParams`，如下：

```java
@Override
public void setLayoutParams(ViewGroup.LayoutParams params) {
    WindowManager.updateByLayoutParams(this, params);
    super.setLayoutParams(params);
}

public void updateByLayoutParams(View view, ViewGroup.LayoutParams params) {
    mWindowManager.updateLayout(view, params);
}
```

如果像上面那样使用，就会发现，在设置`LayoutParams`时，就会产生死循环。

因为更新LayoutParams时，需要更新UI，更新UI完毕，又需要将新的LayoutParams设置到View里面。

影响死循环的步骤有：

* `view.getLayoutParams()`得到的是一个引用。修改当前引用，将直接影响`view`里面的属性`mLayoutParams`。所以，导致无法通过判断值是否改变，来决定是否需要进行更新。

* `view.setLayoutParams()`可以更新UI布局，而更新完UI布局，亦需要将`LayoutParams`设置回`view`。原因是，更新UI布局有两种方式：一是通过Parent的`updateViewLayout()`，一是通过View自身的`setLayoutParams()`。

### ViewGroup的实现
