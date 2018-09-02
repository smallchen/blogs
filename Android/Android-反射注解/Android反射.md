## Android反射

1. 反射是可以访问私有属性和私有方法的！！！


判断a是否B的子类实例：

* bool bl = a instanceof B
* bool bl = b.getClass().isInstance(a) or cls.isInstance(a)

例子：
`view instanceof RemoteViewsAdapter.RemoteViewsFrameLayout`

`Class.forName("android.widget.RemoteViewsAdapter.RemoteViewsFrameLayout").isInstance(view)`


理解：

```
/*
* @param   obj the object to check
* @return  true if {@code obj} is an instance of this class
*
* @since JDK1.1
*/
public boolean isInstance(Object obj) {
   if (obj == null) {
       return false;
   }
   return isAssignableFrom(obj.getClass());
}
```

返回`true`，如果obj是当前class的一个实例。通常用于反射调用下，子类实例的判断。

```java
Class cls = null;
try {
    cls = Class.forName("android.widget.RemoteViewsAdapter");
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}
cls.isInstance(parent);
```

```
Class.isInstance does what you want.

if (Point.class.isInstance(someObj)){
    ...
}
Of course, you shouldn't use it if you could use instanceof instead, but for reflection scenarios it often comes in handy.
```

类型转换:

* B b = (B)a
* B b = b.getClass().cast(a)

会抛异常：`ClassCastException`!!


```java
/**
 * Casts an object to the class or interface represented
 * by this {@code Class} object.
 *
 * @param obj the object to be cast
 * @return the object after casting, or null if obj is null
 *
 * @throws ClassCastException if the object is not
 * null and is not assignable to the type T.
 *
 * @since 1.5
 */
@SuppressWarnings("unchecked")
public T cast(Object obj) {
    if (obj != null && !isInstance(obj))
        throw new ClassCastException(cannotCastMsg(obj));
    return (T) obj;
}
```


## 反射的参数

|     类型      | 参数类型         |
| :------------- | :------------- |
| 空参数         | 不需要第二个参数   |
| int           | int.class       |
| Integer       | Integer.class   |
| String        | String.class    |
| String[]      | String[].class  |

对应：

```java
public Method getMethod(String name, Class<?>... parameterTypes);

Target.class.getMethod("getX");
Target.class.getMethod("method_name", int.class);
Target.class.getMethod("method_name", Integer.class);
Target.class.getMethod("method_name", String[].class);

Target.class.getMethod("method_name", new Class<?>[]{int.class});
Target.class.getMethod("method_name", new Class<?>[]{Integer.class});
Target.class.getMethod("method_name", new Class<?>[]{String[].class});
```

java中，可变参数类型，既可以多个参数以逗号隔开，也可以使用数组，也可以不提供可变参数。

对应的调用：

```java
method.invoke(obj);

method.invoke(obj, 123);
method.invoke(obj, "abc");

private String[] STR_VAL = {"1", "2"};
method.invoke(obj, new Object[]{ STR_VAL });

method.invoke(obj, new Object[]{ new String[]{"1", "2"} });
```

如上，对于数组参数而言，有点区别，所有类型的数组，需要转换为`new Object[]`类型的数组。否则会有提示：

```java
Error:(325, 72) 警告: 最后一个参数使用了不准确的变量类型的 varargs 方法的非 varargs 调用;
对于 varargs 调用, 应使用 Object
对于非 varargs 调用, 应使用 Object[], 这样也可以抑制此警告
```

意思是：method.invoke的最后一个参数是varargs，是可变参数。对于Object类型的可变参数，可以是Object列表，也可以是Object[]数组。

看一下`Method.invoke`的原型，就知道怎么回事：

```java
public native Object invoke(Object obj, Object... args);
```

看到了吧，Method的第二个参数，是可变参数。对于可变参数而言，如何使用单个对象的方式，就是`objA, objB, objC`。如果使用数组的方式，就是`new Object[]{objA, objB, objC}`。所以如果是`String[]`等数组，必须进行`Objext[]`数组转换。

## 包含Declared与否

包含`Declared`，表示获取的是Class对象的类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法、属性等，**但不包括继承的方法、属性等**。简而言之，就是获取当前Class自身的所有方法、属性等。

不包含`Declared`，表示获取的是Class对象所表示的类或接口（包括那些由该类或接口声明的以及从超类和超接口继承的那些的类或接口）的公共方法、属性等。简而言之，就是 **在IDE中，使用点（.）能够访问到的所有公共方法、属性等**。

对于`Declared`方法，表示非`public方法`，所以需要添加`setAccessible(true)`，否则会报`NoSuchMethodException`.

## getClass()与XXX.class的区别

getClass()指的是当前的Class。而XXX.class则指的是XXX对应的Class，两者不一样。

举例：

```java
class MyPopupWindow extends PopupWindow {
    public MyPopupWindow() {
        getClass().getDeclaredMethods();
        PopupWindow.class.getDeclaredMethods();
    }
    public void init() {
        //
    }
}
```

如上，对于`MyPopupWindow`里面的`getClass().getDeclaredMethods()`，得到的只是当前`MyPopupWindow`类的所有方法。对于上例，就只有一个`init()`方法。而对于`PopupWindow.class.getDeclaredMethods()`，得到的是`PopupWindow`这个类的所有方法，它才是核心需要进行反射调用的类。

所以，需要注意以上这点。

在调用的时候，只能使用实例，所以也就只能是`MyPopupWindow`的实例。也就是说，反射获取方法属性的时候，可以使用当前类或父类，但调用的时候，只能为当前类的一个实例。

## 实例

反射是很不稳定的一种用法，如果在Android中使用反射，尽量使用少变的接口或属性来进行反射调用。否则当Android版本改变后，反射可能失效。

下面是打印一个类的所有属性，方法。

```java
for (Method method : getClass().getDeclaredMethods()) {
    method.setAccessible(true);
    Log.e(TAG, method.toGenericString());
}
Log.e(TAG, "-----------------");
for (Method method : PopupWindow.class.getDeclaredMethods()) {
    method.setAccessible(true);
    Log.e(TAG, method.toString());
}
Log.e(TAG, "------- Field --------");
for (Field field : getClass().getDeclaredFields()) {
    field.setAccessible(true);
    Log.e(TAG, field.toString());
}
Log.e(TAG, "-----------------");
for (Field field : PopupWindow.class.getDeclaredFields()) {
    field.setAccessible(true);
    Log.e(TAG, field.toString());
}
```

对于非public的方法和属性，需要使用 **`setAccessible(true)`** 来修改访问权。

使用反射修改PopupWindow的LayoutParams：

```java
try {
    Field fieldDecorView = PopupWindow.class.getDeclaredField("mDecorView");
    fieldDecorView.setAccessible(true);
    ViewGroup decorView = (ViewGroup) fieldDecorView.get(this);

    Field windowManager = PopupWindow.class.getDeclaredField("mWindowManager");
    windowManager.setAccessible(true);
    WindowManager wm = (WindowManager) windowManager.get(this);

    WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) decorView.getLayoutParams();
    if (bl) {
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    } else {
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
    Log.e(TAG, "layout:"+layoutParams);
    wm.updateViewLayout(decorView, layoutParams);
} catch (Exception e) {
    Log.e("SaveController", "############# Fatal Exception: android API changed! ###############", e);
}
```
