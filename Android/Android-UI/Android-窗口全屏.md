<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android窗口全屏](#android窗口全屏)
- [通过代码设置](#通过代码设置)
- [真正全屏](#真正全屏)

<!-- /TOC -->

## Android窗口全屏

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="WhiteboardTheme" parent="Theme.AppCompat.Light.NoActionBar" >
        <item name="android:windowBackground">@color/default_whiteboard_bg_color</item>
    </style>
</resources>
```

```xml
<resources>

    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

</resources>
```

以上，只能实现隐藏`ActionBar`，并不会隐藏`系统状态栏`，`系统虚拟按键`

## 通过代码设置

## 真正全屏

可以新建Android的FullScreenActivity来实现。
