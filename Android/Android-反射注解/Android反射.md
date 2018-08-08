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
