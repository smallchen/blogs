## Android拖动窗口抖动或闪屏

自定义窗口，通过`onDraw()`绘制的自定义View，在拖动View时，出现View闪屏，尤其是线条抖动。

原因是：`onDraw()`使用了`float`值进行绘制，导致float值在显示的时候，精度损失，继而在显示的时候闪动，抖动。

解决：把float值改为int值。
