## Android硬件加速

1、应用：
<application android:hardwareAccelerated="true">
2、Activity
<activity android:hardwareAccelerated="true">
3、Window
getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
4、View
view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
在这四个层次中，应用和Activity是可以选择的，Window只能打开，View只能关闭。

在apk的AndroidManifest中，如果指定了minSDKVersion&targetSDKVersion=7，会使得应用无法使用硬件加速进行绘图。
