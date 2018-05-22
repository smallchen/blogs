

#### 自定义View属性

1.声明自定义属性。

```java
// styles.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="TextButton">
        <attr name="imageSrc" format="reference"/>
        <attr name="imageWidth" format="dimension" />
        <attr name="imageHeight" format="dimension" />
        <attr name="imageMarginTop" format="dimension" />

        <attr name="text" format="string" />
        <attr name="textSize" format="dimension" />
        <attr name="textColor" format="color" />
        <attr name="textMarginTop" format="dimension" />
    </declare-styleable>
</resources>
```

2.layout里设置

```java
<com.mark.widget.TextButton
	  android:id="@+id/btn_prev"
	  style="@style/ToolbarTextButtonStyle"
	  app:imageSrc="@drawable/toolbar_btn_prev"
	  app:text="上一页">
</com.mark.widget.TextButton>
```

#### 在ThemeStyle里统一设置View自定义属性。

如下，直接设置自定义属性的值即可。app命名空间的属性，不需要android前缀，直接填。

```java
// values/styles.xml
<style name="ToolbarTextButtonStyle" parent="ToolbarTextButtonStyle">
	<item name="android:layout_width">40dp</item>
	<item name="android:layout_height">40dp</item>
	<item name="android:background">@drawable/toolbar_btn_bg</item>
	<item name="imageMarginTop">7.33dp</item>
	<item name="imageWidth">17.33dp</item>
	<item name="imageHeight">17.33dp</item>
	<item name="textMarginTop">3.33dp</item>
	<item name="textSize">6.67dp</item>
	<item name="textColor">#343434</item>
</style>
```
Â
