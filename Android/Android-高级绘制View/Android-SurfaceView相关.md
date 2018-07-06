## SurfaceView相关

1. surfaceview变得可见时，surface被创建；surfaceview隐藏时，surface被销毁。
2.  surfaceview的核心在于提供了两个线程：UI线程和渲染线程。这里应注意：
        1 所有SurfaceView和SurfaceHolder.Callback的方法都应该在UI线程里调用，一般来说就是应用程序主线程。渲染线程所要访问的各种变量应该作同步处理。
        2 由于surface可能被销毁，它只在SurfaceHolder.Callback.surfaceCreated()和 SurfaceHolder.Callback.surfaceDestroyed()之间有效，所以要确保渲染线程访问的是合法有效的surface。

http://www.cnblogs.com/xuling/archive/2011/06/06/android.html

### 画刷
Paint p = new Paint(); //创建画笔
p.setTextSize(40); // 字体的像素。一般需要根据屏幕大小计算比例，而不是固定大小
p.setColor(Color.BLACK);
对应：颜色是黑色，文本大小是40点画刷

Paint mEraserPaint = new Paint();
mEraserPaint.setAntiAlias(true);
mEraserPaint.setStyle(Paint.Style.FILL);

```java
    private float getTextSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();

        int mScreenWidth = dm.widthPixels;
        int mScreenHeight = dm.heightPixels;

        //以分辨率为720*1080准，计算宽高比值
        float ratioWidth = (float) mScreenWidth / 720;
        float ratioHeight = (float) mScreenHeight / 1080;
        float ratioMetrics = Math.min(ratioWidth, ratioHeight);
        int textSize = Math.round(20 * ratioMetrics);

        Log.d(TAG, "getTextSize: "+textSize);
        return textSize;
    }
```

### 矩形区域绘制
Rect r = new Rect(100, 50, 200, 150);
对应：屏幕坐标的Left／Top／Right／Bottom。画出来的是left=100，top=50，width=100，height=100的正方形。

### 字体绘制
// Canvas c
c.drawText("这是文本", 100, 100, p);
对应：屏幕坐标left=100，top=100，使用画刷p，绘制文本

### 截图

以下为Android系统内隐藏的截图方法
```java
    public static synchronized Bitmap screenShot(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return screenshotInner(metrics.widthPixels, metrics.heightPixels);
    }

    private static Bitmap screenshotInner(int width,int height) {
        Class surfaceControl;
        Bitmap bitmap=null;
        try {
            surfaceControl = Class.forName("android.view.SurfaceControl");
            Method screenshot=surfaceControl.getMethod("screenshot",int.class,int.class);
            bitmap=(Bitmap)screenshot.invoke(null,new Object[]{width,height});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
```
