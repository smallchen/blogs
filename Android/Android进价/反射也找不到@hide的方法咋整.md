## java反射

1. Class

```java
1. 
Class cls = Class.forName("android.support.v4.widget.PopupWindowCompat");
2. 
Class cls = new PopupWindow().getClass();
```
2. Constructor

```java
1.
cls.newInstance();
cls.getConstructor().newInstance();
2.
Constructor constructor = cls.getConstructor(String.class, Integer.class);
constructor.newInstance("A", 1);
3. 
Constructor constructor = cls.getDeclaredConstructor(new Class[]{String.class, Integer.class});
constructor.newInstance("A", 1);
4.
cls.getConstructors();
5.
cls.getDeclaredConstructors();
```

3. Field

```java
1.
cls.getField("width");
2.
cls.getDeclaredField("width");
3.
cls.getFields();
4.
cls.getDeclaredFields();
```

4. Method

```java
1.
cls.getMethod("getArg");
cls.getMethod("getArg", new Class[]{});
2.
cls.getMethod("setArg", Integer.class, String.class);
cls.getMethod("setArg", new Class[]{Integer.class, String.class});
3.
cls.getDeclaredMethod("getArg");
4.
cls.getMethods();
5.
cls.getDeclaredMethods();
```

5. Annotations

```java
1.
cls.getAnnotation(Annotation.class);
2.
cls.getDeclaredAnnotation(Annotation.class);
3.
cls.getAnnotations();
4.
cls.getDeclaredAnnotations();
```

Declared有无的区别：
1. 含有Declared的方法，返回的是所有public／protected／default／private关键字的构造器／属性／方法／注解。**但不包括继承！**（是切片）

2. 非Declared的方法，返回的是该类所有public方法，**包括继承而来的。**（是纵深）

## android @hide注解

简而言之，就是内部使用的接口。android的解释是：

1. 不稳定的接口，不同android版本接口的功能可能不一样，也可能压根不存在；即可能在旧android版本上存在，在新android版本被删除，可能在旧android版本不存在，在新android版本才添加进去。这类接口和android版本强绑定。

2. 不建议外部使用，会导致兼容性变差。

## @hide注解的方法使用反射提示找不到方法

在AndroidStudio里查看源码，PopupWindow里面是有以下这些@hide接口的。

```java
/** @hide */
protected boolean hasContentView() {
    return mContentView != null;
}

/** @hide */
protected boolean hasDecorView() {
    return mDecorView != null;
}

/** @hide */
protected WindowManager.LayoutParams getDecorViewLayoutParams() {
    return (WindowManager.LayoutParams) mDecorView.getLayoutParams();
}
```

为了验证一个问题，通过反射获取getDecorViewLayoutParams()，但提示找不到方法。尝试了其它@hide的方法，结果也是一样找不到。打印所有方法也没能找到。

在模拟器和真机上测试均是如此。

为什么呢？
1. 可能反射用错，Declare方法不包含继承而来的方法，所以找不到父类的方法。
2. Android系统版本不对，接口已被删改。

可能性1，PopupWindow没有继承。
可能性2：**发现这些方法是API26，android 8.0才引入的，模拟器和真机还是android 7.0的系统，所以自然找不到方法！**

正如android官方所说，@hide的方法不建议使用，因为使用后会导致APP的兼容性不好，在不同的android系统版本中，可能有不同的行为，也可能根本没有此接口。**越踩坑就越会发现android文档里的用语非常精确，比如说不保证XXX，就说明总会有出错的时候。从没遇到过不代表不会发生！**
