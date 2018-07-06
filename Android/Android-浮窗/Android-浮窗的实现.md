## 悬浮窗口的实现
工作中遇到一些项目需要把窗体显示在最上层，像来电弹窗显示电话号码等信息、拦截短信信息显示给用户或者游戏中实现声音的调节，我们想这些数据放在最上层，activity就满足不了我们的需求了，有些开发者使用了循环显示toast的方式，toast是不能获得焦点的，这种方法是不可取的。这个时候，我们如何处理呢？
       原来，整个Android的窗口机制是基于一个叫做 WindowManager，这个接口可以添加view到屏幕，也可以从屏幕删除view。它面向的对象一端是屏幕，另一端就是View，直接忽略我们以前的Activity或者Dialog之类的东东。其实我们的Activity或者Dialog底层的实现也是通过WindowManager，这个WindowManager是全局的，整个系统就是这个唯一的东东。它是显示View的最底层了。

Toast 不能获取焦点
Activity 不支持直接显示在窗口前
View 才是悬浮窗口（意味着，浮窗和浮窗外都可以获取触摸焦点）
Dialog 和Activy一样，显示的时候会占用输入焦点，窗口外无法获取触摸事件

方法
WindowManager继承自ViewManager，里面涉及到窗口管理的三个重要方法，分别是：

     * addView();

     * updateViewLayout();

     * removeView();

最后，还有需要注意的是，如果要用悬浮窗口，需要在AndroidManifest.xml中加入如下的权限：
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

```java
private void showView(){
     mLayout=new FloatView(getApplicationContext());
     mLayout.setBackgroundResource(R.drawable.faceback_head);
     //获取WindowManager
     mWindowManager=(WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置LayoutParams(全局变量）相关参数
     param = ((MyApplication)getApplication()).getMywmParams();

     param.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;     // 系统提示类型,重要
     param.format=1;
     param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
     param.flags = param.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
     param.flags = param.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制

     param.alpha = 1.0f;

     param.gravity=Gravity.LEFT|Gravity.TOP;   //调整悬浮窗口至左上角
        //以屏幕左上角为原点，设置x、y初始值
     param.x=0;
     param.y=0;

        //设置悬浮窗口长宽数据
     param.width=140;
     param.height=140;

        //显示myFloatView图像
     mWindowManager.addView(mLayout, param);

    }
```
1. 声明浮窗类型Type
WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

2. 声明触摸Flag
WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

3. 设置坐标和布局
param.alpha = 1.0f;
param.gravity=Gravity.LEFT|Gravity.TOP;   //调整悬浮窗口至左上角
//以屏幕左上角为原点，设置x、y初始值
param.x=0;
param.y=0;
