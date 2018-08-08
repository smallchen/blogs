## Android 通过字符串ID名获取对应的资源值

## 获取当前应用的资源：

```java
public int getDrawableId(String id) {
    return getResources().getIdentifier(id, "drawable", getPackageName());
}

public static Drawable GetImage(Context c, String ImageName) {
    return c.getResources().getDrawable(c.getResources().getIdentifier(ImageName, "drawable", c.getPackageName()));
}
```

## 获取`Android`内置的资源：

```java
public static int innerRId(String id) {
    return Resources.getSystem().getIdentifier(id, "id", "android");
}
public static int innerRStylableId(String id) {
    return Resources.getSystem().getIdentifier(id, "styleable", "android");
}
public static int innerRBoolId(String id) {
    return Resources.getSystem().getIdentifier(id, "bool", "android");
}
```

## 区别

获取当前应用，使用的是`context.getResources()`

获取系统内置资源，使用的是`Resources.getSystem()`

Resources其实就是`AssetManager`相关的操作。具体可以见《Resources构建流程详解》
