## 悬浮窗口的实现

### 窗口类型

所有`WindowTypes`如下，大致有三类：

* 1~99，`APPLICATION_WINDOW`（应用窗口），范围[FIRST_APPLICATION_WINDOW, LAST_APPLICATION_WINDOW]。
* 1000~1999，`SUB_WINDOW`（应用子窗口），范围[FIRST_SUB_WINDOW，LAST_SUB_WINDOW]。
* 2000~2999，`SYSTEM_WINDOW`（系统窗口），范围[FIRST_SYSTEM_WINDOW, LAST_SYSTEM_WINDOW]

其中，低于2000的，都是应用浮窗，跟随应用切换到后台；高于2000的，都是系统浮窗，应用切换到后台仍旧能够显示，且部分浮窗可以覆盖在锁屏界面之上。

```java
private List<Integer> mWindowTypes = new ArrayList<>();
{
	mWindowTypes.add(LayoutParams.FIRST_APPLICATION_WINDOW); // 1
	mWindowTypes.add(LayoutParams.TYPE_BASE_APPLICATION); // 1
	mWindowTypes.add(LayoutParams.TYPE_APPLICATION); // 2
	mWindowTypes.add(LayoutParams.TYPE_APPLICATION_STARTING); // 3
	mWindowTypes.add(LayoutParams.TYPE_DRAWN_APPLICATION); // 4
	mWindowTypes.add(LayoutParams.LAST_APPLICATION_WINDOW); // 99

	mWindowTypes.add(LayoutParams.FIRST_SUB_WINDOW); // 1000
	mWindowTypes.add(LayoutParams.LAST_SUB_WINDOW); // 1999

	mWindowTypes.add(LayoutParams.FIRST_SYSTEM_WINDOW); // 2000

	mWindowTypes.add(LayoutParams.TYPE_STATUS_BAR); // 2000
	mWindowTypes.add(LayoutParams.TYPE_SEARCH_BAR); // 2001
	mWindowTypes.add(LayoutParams.TYPE_PHONE); // 2002
	mWindowTypes.add(LayoutParams.TYPE_SYSTEM_ALERT); // 2003
	mWindowTypes.add(LayoutParams.TYPE_TOAST); // 2005
	mWindowTypes.add(LayoutParams.TYPE_SYSTEM_OVERLAY); // 2006
	mWindowTypes.add(LayoutParams.TYPE_SYSTEM_DIALOG); // 2008
	mWindowTypes.add(LayoutParams.TYPE_SYSTEM_ERROR); // 2010
	mWindowTypes.add(LayoutParams.TYPE_INPUT_METHOD); // 2011
	mWindowTypes.add(LayoutParams.TYPE_DISPLAY_OVERLAY); // 2026

	mWindowTypes.add(LayoutParams.TYPE_APPLICATION_OVERLAY); // 2038
	mWindowTypes.add(LayoutParams.LAST_SYSTEM_WINDOW); // 2999
}
```

### 悬浮窗类型：

* 应用内悬浮窗，自动跟随应用切换前后台。
* 手机内悬浮窗，应用切换到后台，仍旧显示在屏幕顶层。

以上两种，以下简称为`应用悬浮窗`和`系统悬浮窗`。

### `应用悬浮窗`实现：

* 无需任何声明。
* 使用`FIRST_SUB_WINDOW`~`LAST_SUB_WINDOW`之间类型。

测试过程：
1. TYPE_APPLICATION_MEDIA没能显示窗口，其余都可以。
2. 除TYPE_APPLICATION_ATTACHED_DIALOG外，其余窗口坐标系为屏幕；只有这个类型，坐标以Activity显示区域为坐标系（即，0-0坐标，这个类型显示在Activity显示区域左上角（除去状态栏和虚拟按键栏），其它显示在屏幕左上角）。
3. 从上可以看出，不同类型，WindowManger.LayoutParams(x = 0，y = 0)的坐标系不一样。

> 以上测试，真机Android版本为7.0。模拟器Android版本为8.0。

### `系统悬浮窗`实现：

* 无需任何声明的情况

在锤子的`7.1`系统中，没有声明，使用TYPE_PHONE，可以顺利显示`系统悬浮窗`（系统会自动提示，是否显示悬浮窗），当Service在后台被杀死，悬浮窗消失。

但在7.0的模拟器中，以上参数，并不能弹出系统悬浮窗，提示`Permission denied`。


模拟器 API 25

```java
case 0:return LayoutParams.FIRST_APPLICATION_WINDOW; // no error. no show.
case 1:return LayoutParams.TYPE_BASE_APPLICATION; // no error. no show.
case 2:return LayoutParams.TYPE_APPLICATION; // shown, activity-0,0,
case 3:return LayoutParams.TYPE_APPLICATION_STARTING; // no,no
case 4:return LayoutParams.TYPE_DRAWN_APPLICATION; // shown, activity,
case 5:return LayoutParams.LAST_APPLICATION_WINDOW; // shown, activity.
case 6:return LayoutParams.FIRST_SUB_WINDOW; // error
case 7:return LayoutParams.FIRST_SYSTEM_WINDOW; // error
case 8:return LayoutParams.TYPE_STATUS_BAR; // error
case 9:return LayoutParams.TYPE_SEARCH_BAR; // error
case 10:return LayoutParams.TYPE_PHONE; // error
case 11:return LayoutParams.TYPE_SYSTEM_ERROR; // error
case 12:return LayoutParams.TYPE_INPUT_METHOD; // error
```

模拟器 API 27
```java
case 0:return LayoutParams.FIRST_APPLICATION_WINDOW; // show, activity-0,0
case 1:return LayoutParams.TYPE_BASE_APPLICATION; // show, activity
case 2:return LayoutParams.TYPE_APPLICATION; // show, activity-0,0,
case 3:return LayoutParams.TYPE_APPLICATION_STARTING; // show, activity-0,0,
case 4:return LayoutParams.TYPE_DRAWN_APPLICATION; // shown, activity,
case 5:return LayoutParams.LAST_APPLICATION_WINDOW; // shown, activity.
case 6:return LayoutParams.FIRST_SUB_WINDOW; // error
case 7:return LayoutParams.FIRST_SYSTEM_WINDOW; // error
case 8:return LayoutParams.TYPE_STATUS_BAR; // error
case 9:return LayoutParams.TYPE_SEARCH_BAR; // error
case 10:return LayoutParams.TYPE_PHONE; // error
case 11:return LayoutParams.TYPE_SYSTEM_ERROR; // error
case 12:return LayoutParams.TYPE_INPUT_METHOD; // error
```

锤子 API 25

```java
case 0:return LayoutParams.FIRST_APPLICATION_WINDOW; // no err, no shown.
case 1:return LayoutParams.TYPE_BASE_APPLICATION; // no err, no shown
case 2:return LayoutParams.TYPE_APPLICATION; // show, activity-0,0,
case 3:return LayoutParams.TYPE_APPLICATION_STARTING; // no err, no shown,
case 4:return LayoutParams.TYPE_DRAWN_APPLICATION; // no err, no shown,
case 5:return LayoutParams.LAST_APPLICATION_WINDOW; // shown, activity.
case 6:return LayoutParams.FIRST_SUB_WINDOW; // error
case 7:return LayoutParams.FIRST_SYSTEM_WINDOW; // error
case 8:return LayoutParams.TYPE_STATUS_BAR; // error
case 9:return LayoutParams.TYPE_SEARCH_BAR; // error
case 10:return LayoutParams.TYPE_PHONE; // shown, actiivty
case 11:return LayoutParams.TYPE_SYSTEM_ERROR; // shown, activty，锁屏还能显示。最顶层。
case 12:return LayoutParams.TYPE_INPUT_METHOD; // error
```

#### TYPE_PHONE

**API25 (Android N)(7.x)**

#### TYPE_APPLICATION_OVERLAY

`public static final int TYPE_APPLICATION_OVERLAY = FIRST_SYSTEM_WINDOW + 38;`

**API26 (Android O)(8.x)** 接口文档里提及，以下类型，对于非系统应用，应当使用`TYPE_APPLICATION_OVERLAY`代替。

`API26 for non-system apps. Use {@link #TYPE_APPLICATION_OVERLAY} instead`，

* TYPE_PHONE（FIRST_SYSTEM_WINDOW + 2）
* TYPE_SYSTEM_ALERT（FIRST_SYSTEM_WINDOW + 3）
* TYPE_TOAST（FIRST_SYSTEM_WINDOW + 5）
* TYPE_SYSTEM_OVERLAY（FIRST_SYSTEM_WINDOW + 6）
* TYPE_PRIORITY_PHONE（FIRST_SYSTEM_WINDOW + 7）
* TYPE_SYSTEM_ERROR（FIRST_SYSTEM_WINDOW + 10）


添加声明：

无论是哪个版本，哪种类型

Android版本





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

     param.type=LayoutParams.TYPE_SYSTEM_ALERT;     // 系统提示类型,重要
     param.format=1;
     param.flags = LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
     param.flags = param.flags | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
     param.flags = param.flags | LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制

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




### 测试代码

```java
private static int index = -1;
private int getWindowType() {
	index += 1;
	switch (index) {
		case 0:return LayoutParams.FIRST_SUB_WINDOW;
		case 1:return LayoutParams.TYPE_APPLICATION_PANEL;
		case 2:return LayoutParams.TYPE_APPLICATION_MEDIA;
		case 3:return LayoutParams.TYPE_APPLICATION_SUB_PANEL;
		case 4:return LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		case 5:return LayoutParams.LAST_SUB_WINDOW;
		case 6:return LayoutParams.TYPE_STATUS_BAR;
		case 7:return LayoutParams.TYPE_SEARCH_BAR;
		case 8:return LayoutParams.TYPE_PHONE;
		default:
			return LayoutParams.FIRST_SUB_WINDOW;
	}
}
public void show() {
	int LAYOUT_FLAG = getWindowType();
	if (mContentView != null) {
		wmParams.type = LAYOUT_FLAG;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.alpha = 1.0f;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = 200;
		wmParams.height = 200;
		wm.addView(mContentView, wmParams);
	}
}
```
