## JAVA强制进行类型转换会抛ClassCastException异常！！

举例：

```java
WindowManager.LayoutParams srcwparams = (WindowManager.LayoutParams) super.getLayoutParams();
if (srcwparams == null) {
   return srcwparams;
}
```

虽然，`WindowManager.LayoutParams`确实是`ViewGroup.LayoutParams`的子类，但由于`super.getLayoutParams()`的当前实例并不是`WindowManager.LayoutParams`的一个实例，所以，强制转换失败！抛出`ClassCastException`异常！！

```java
/AndroidRuntime: FATAL EXCEPTION: main
 Process: com.jokin.demo.demolayoutparams, PID: 10955
 java.lang.ClassCastException: android.view.ViewGroup$LayoutParams cannot be cast to android.view.WindowManager$LayoutParams
     at com.jokin.demo.demolayoutparams.WindowLayout.getLayoutParams(WindowLayout.java:87)
```

所以，正确的做法是进行类型判断后进行转换：

```java
ViewGroup.LayoutParams srcwparams = (ViewGroup.LayoutParams) super.getLayoutParams();
if (srcwparams == null) {
    return null;
}
if (!(srcwparams instanceof WindowManager.LayoutParams)) {
    return null;
}
return (WindowManager.LayoutParams) srcwparams;
```
