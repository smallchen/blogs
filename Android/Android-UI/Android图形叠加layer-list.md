[shape_circle]: shape_circle.png
[archive_save_as_pressed]:archive_save_as_pressed.png
[archive_tab_bg_pressed]:archive_tab_bg_pressed.png
[shape_save_as]:shape_save_as.png

## Android图形叠加layer-list

### 圆环的实现

shape_pen_circle.xml:
```java
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <shape android:shape="oval">
            <solid android:color="@color/pen_color_selected"/>
            <padding android:bottom="@dimen/pen_circle_stroke_size"
                android:left="@dimen/pen_circle_stroke_size"
                android:right="@dimen/pen_circle_stroke_size"
                android:top="@dimen/pen_circle_stroke_size"/>
        </shape>
    </item>
    <item>
        <shape android:shape="oval">
            <solid android:color="@color/pen_color_selected_inner"/>
            <padding android:bottom="@dimen/pen_circle_stroke_gap_size"
                android:left="@dimen/pen_circle_stroke_gap_size"
                android:right="@dimen/pen_circle_stroke_gap_size"
                android:top="@dimen/pen_circle_stroke_gap_size"/>
        </shape>
    </item>
    <item>
        <shape android:shape="oval">
            <solid android:color="@color/pen_color_green_mid"/>
            <size android:height="@dimen/pen_circle_size"
                android:width="@dimen/pen_circle_size"/>
        </shape>
    </item>

</layer-list>
```

values/dimins.xml:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="pen_color_red">#E30112</color>
    <color name="pen_color_blue">#316ED9</color>
    <color name="pen_color_green">#74D862</color>
    <color name="pen_color_orange">#FE9518</color>
    <color name="pen_color_yellow">#FFFE00</color>
    <color name="pen_color_black">#000000</color>
    <color name="pen_color_grey">#686868</color>
    <color name="pen_color_white">#FFFFFF</color>
    <color name="pen_color_violet">#331EB5</color>
    <color name="pen_color_green_mid">#306C01</color>

    <color name="pen_color_normal">#ff000000</color>
    <color name="pen_color_normal_inner">#00000000</color>
    <color name="pen_color_selected">#1EA2FC</color>
    <color name="pen_color_selected_inner">#FFFFFF</color>
    <color name="pen_color_activated">#bcb7b7</color>
    <color name="pen_color_activated_inner">#FFFFFF</color>

    <dimen name="pen_circle_size">24.67dp</dimen>
    <dimen name="pen_circle_stroke_size">1.8dp</dimen>
    <dimen name="pen_circle_stroke_gap_size">1.2dp</dimen>
</resources>
```

效果是下面这样，三层环。选中的时候在圆外面套一层环。
![shape_circle][shape_circle]

圆环的实现边距的问题比较奇怪，还没能理解各个值的影响。不断尝试，就可以画出来。

### 多图片的叠加

selector_save_as.xml
```
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <layer-list>
            <item android:drawable="@drawable/archive_tab_bg_pressed" />
            <item android:drawable="@drawable/archive_save_as_pressed" />
        </layer-list>
    </item>
    <item android:state_selected="true">
        <layer-list>
            <item android:drawable="@drawable/archive_tab_bg_pressed" />
            <item android:drawable="@drawable/archive_save_as_pressed" />
        </layer-list>
    </item>
    <item android:drawable="@drawable/archive_save_as_normal" />
</selector>
```

![archive_save_as_pressed][archive_save_as_pressed]
![archive_tab_bg_pressed][archive_tab_bg_pressed]

效果是，选中的时候，两个图标叠加在一起。

![shape_save_as][shape_save_as]


这个页面不错<http://www.cnblogs.com/tianzhijiexian/p/3889770.html>
