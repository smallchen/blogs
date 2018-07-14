<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android 浮窗测试](#android-浮窗测试)
	- [表格标题说明](#表格标题说明)
- [不添加任何权限声明测试](#不添加任何权限声明测试)
	- [不添加任何权限声明 + ActivityContext](#不添加任何权限声明-activitycontext)
		- [模拟器 API 27（8.1）](#模拟器-api-2781)
		- [模拟器 API 25（7.1）](#模拟器-api-2571)
		- [锤子真机 API 25（7.1）](#锤子真机-api-2571)
	- [WindowTypes的顺序问题](#windowtypes的顺序问题)
		- [模拟器 API 27（8.1）](#模拟器-api-2781)
		- [模拟器 API 25（7.1）](#模拟器-api-2571)
		- [锤子真机 API 25（7.1）](#锤子真机-api-2571)
	- [不添加任何权限声明 + ApplicationContext](#不添加任何权限声明-applicationcontext)
		- [模拟器 API 27（8.1）](#模拟器-api-2781)
	- [总结（不添加权限声明）](#总结不添加权限声明)
- [添加权限声明测试](#添加权限声明测试)
	- [添加权限声明 + ActivityContext](#添加权限声明-activitycontext)
		- [模拟器 API 27（8.1）](#模拟器-api-2781)
		- [模拟器 API 25（7.1）](#模拟器-api-2571)
		- [锤子真机 API 25（7.1）](#锤子真机-api-2571)
	- [添加权限声明 + ApplicationContext](#添加权限声明-applicationcontext)
	- [总结（添加权限声明）](#总结添加权限声明)
- [总结](#总结)

<!-- /TOC -->

## Android 浮窗测试

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

	mWindowTypes.add(LayoutParams.TYPE_APPLICATION_OVERLAY); // 2038
	mWindowTypes.add(LayoutParams.LAST_SYSTEM_WINDOW); // 2999
}
```

### 表格标题说明

以下，表格中：

* x.y表示坐标系。
* activity是指坐标系为activity的可视区域（减去虚拟按键区和状态栏）。
* screen是指坐标系为屏幕（覆盖虚拟按键区和状态栏）。
* 出错了肯定没办法显示。

## 不添加任何权限声明测试

### 不添加任何权限声明 + ActivityContext

#### 模拟器 API 27（8.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       |    no      |     yes     |    activty      |
|  1    | TYPE_BASE_APPLICATION          |    no      |     yes     |    activty      |
|  2    | TYPE_APPLICATION               |    no      |     yes     |    activty      |
|  3    | TYPE_APPLICATION_STARTING      |    no      |     yes     |    activty      |
|  4    | TYPE_DRAWN_APPLICATION         |    no      |     yes     |    activty      |
|  99   | LAST_APPLICATION_WINDOW        |    no      |     yes     |    activty      |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               |    is your activity running?      |          |          |
|  1999 | LAST_SUB_WINDOW                |    is your activity running?      |          |          |
|             | -       |   -      |  -        | -         |
|  2000 | FIRST_SYSTEM_WINDOW            |   permission denied       |          |          |
|  2000 | TYPE_STATUS_BAR                |   denied       |          |          |
|  2001 | TYPE_SEARCH_BAR                |   denied       |          |          |
|  2002 | TYPE_PHONE                     |   denied       |          |          |
|  2003 | TYPE_SYSTEM_ALERT              |   denied       |          |          |
|  2005 | TYPE_TOAST                     |   is your activity running?       |          |          |
|  2006 | TYPE_SYSTEM_OVERLAY            |   denied       |          |          |
|  2008 | TYPE_SYSTEM_DIALOG             |   denied       |          |          |
|  2010 | TYPE_SYSTEM_ERROR              |   denied       |          |          |
|  2011 | TYPE_INPUT_METHOD              |   is your activity running?       |          |          |
|  2038 | TYPE_APPLICATION_OVERLAY       |   denied       |          |          |
|  2999 | LAST_SYSTEM_WINDOW             |   denied       |          |          |


#### 模拟器 API 25（7.1）


|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       |    no      |     no     |                 |
|  1    | TYPE_BASE_APPLICATION          |    no      |     no     |                 |
|  2    | TYPE_APPLICATION               |    no      |     yes    |    activty      |
|  3    | TYPE_APPLICATION_STARTING      |    no      |     no     |                 |
|  4    | TYPE_DRAWN_APPLICATION         |    no      |     yes    |    activty      |
|  99   | LAST_APPLICATION_WINDOW        |    no      |     yes    |    activty      |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               |    is your activity running?      |          |          |
|  1999 | LAST_SUB_WINDOW                |    is your activity running?      |          |          |
|             | -       |   -      |  -        | -         |
|  2000 | FIRST_SYSTEM_WINDOW            |   permission denied       |          |          |
|  2000 | TYPE_STATUS_BAR                |   denied       |          |          |
|  2001 | TYPE_SEARCH_BAR                |   denied       |          |          |
|  2002 | TYPE_PHONE                     |   denied       |          |          |
|  2003 | TYPE_SYSTEM_ALERT              |   denied       |          |          |
|  2005 | TYPE_TOAST                     |   is your activity running?       |          |          |
|  2006 | TYPE_SYSTEM_OVERLAY            |   denied       |          |          |
|  2008 | TYPE_SYSTEM_DIALOG             |   denied       |          |          |
|  2010 | TYPE_SYSTEM_ERROR              |   denied       |          |          |
|  2011 | TYPE_INPUT_METHOD              |   is your activity running?       |          |          |
|  2038 | TYPE_APPLICATION_OVERLAY       |   denied       |          |          |
|  2999 | LAST_SYSTEM_WINDOW             |   denied       |          |          |



#### 锤子真机 API 25（7.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       |    no      |     no     |                 |
|  1    | TYPE_BASE_APPLICATION          |    no      |     no     |                 |
|  2    | TYPE_APPLICATION               |    no      |     yes    |    activty      |
|  3    | TYPE_APPLICATION_STARTING      |    no      |     no     |                 |
|  4    | TYPE_DRAWN_APPLICATION         |    no      |     yes    |    activty      |
|  99   | LAST_APPLICATION_WINDOW        |    no      |     yes    |    activty      |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               |    is your activity running?      |          |          |
|  1999 | LAST_SUB_WINDOW                |    is your activity running?      |          |          |
|             | -       |   -      |  -        | -         |
|  2000 | FIRST_SYSTEM_WINDOW            |   permission denied       |          |          |
|  2000 | TYPE_STATUS_BAR                |   denied       |          |          |
|  2001 | TYPE_SEARCH_BAR                |   denied       |          |          |
|  2002 | TYPE_PHONE                     |   no           |    yes            |    activty       |
|  2003 | TYPE_SYSTEM_ALERT              |   no           |    yes            |    activty      |
|  2005 | TYPE_TOAST                     |   no           |    yes            |    activty      |
|  2006 | TYPE_SYSTEM_OVERLAY            |   no           |    yes (不能拖动,比锁屏高   |    activty      |
|  2008 | TYPE_SYSTEM_DIALOG             |   denied       |          |          |
|  2010 | TYPE_SYSTEM_ERROR              |   no           |    yes (比锁屏层级高  |  activty       |
|  2011 | TYPE_INPUT_METHOD              |   is your activity running?       |          |          |
|  2038 | TYPE_APPLICATION_OVERLAY       |   denied       |          |          |
|  2999 | LAST_SYSTEM_WINDOW             |   denied       |          |          |

> 真机和模拟器是有区别的，这就是厂商的修改。锤子手机下，不声明也能显示部分系统浮窗。原生系统中，不声明无法使用任何系统浮窗。

### WindowTypes的顺序问题

以上是从1-2999的顺序测试，会发现，`FIRST_SUB_WINDOW～LAST_SUB_WINDOW`之间的浮窗，都不能显示！！

但可以明确的是，`FIRST_SUB_WINDOW～LAST_SUB_WINDOW`之间的浮窗是肯定能显示的。所以是次序的问题？

```java
mWindowTypes.add(LayoutParams.FIRST_SUB_WINDOW); // 1000
mWindowTypes.add(LayoutParams.LAST_SUB_WINDOW); // 1999

mWindowTypes.add(LayoutParams.FIRST_APPLICATION_WINDOW); // 1
mWindowTypes.add(LayoutParams.TYPE_BASE_APPLICATION); // 1
mWindowTypes.add(LayoutParams.TYPE_APPLICATION); // 2
mWindowTypes.add(LayoutParams.TYPE_APPLICATION_STARTING); // 3
mWindowTypes.add(LayoutParams.TYPE_DRAWN_APPLICATION); // 4
mWindowTypes.add(LayoutParams.LAST_APPLICATION_WINDOW); // 99
```

把`FIRST_SUB_WINDOW～LAST_SUB_WINDOW`提前，在API 27 (8.1)上测试：

#### 模拟器 API 27（8.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1000 | FIRST_SUB_WINDOW               |    no      |    yes      |    screen      |
|  1999 | LAST_SUB_WINDOW                |    no      |    yes      |    screen      |
|             | -       |   -      |  -        | -         |
|  1    | FIRST_APPLICATION_WINDOW       | is your activity running? |          |    |
|  1    | TYPE_BASE_APPLICATION          | 同上       |          |          |
|  2    | TYPE_APPLICATION               | 同上       |          |          |
|  3    | TYPE_APPLICATION_STARTING      | 同上       |          |          |
|  4    | TYPE_DRAWN_APPLICATION         | 同上       |          |          |
|  99   | LAST_APPLICATION_WINDOW        | 同上       |          |          |

#### 模拟器 API 25（7.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1000 | FIRST_SUB_WINDOW               |    no      |    yes      |    screen      |
|  1999 | LAST_SUB_WINDOW                |    no      |    yes      |    screen      |
|             | -       |   -      |  -        | -         |
|  1    | FIRST_APPLICATION_WINDOW       | token is not for an application |    |    |
|  1    | TYPE_BASE_APPLICATION          | 同上       |          |          |
|  2    | TYPE_APPLICATION               | 同上       |          |          |
|  3    | TYPE_APPLICATION_STARTING      | 同上       |          |          |
|  4    | TYPE_DRAWN_APPLICATION         | 同上       |          |          |
|  99   | LAST_APPLICATION_WINDOW        | 同上       |          |          |

#### 锤子真机 API 25（7.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1000 | FIRST_SUB_WINDOW               |    no      |    yes      |    screen      |
|  1999 | LAST_SUB_WINDOW                |    no      |    yes      |    screen      |
|             | -       |   -      |  -        | -         |
|  1    | FIRST_APPLICATION_WINDOW       | token is not for an application |    |    |
|  1    | TYPE_BASE_APPLICATION          | 同上       |          |          |
|  2    | TYPE_APPLICATION               | 同上       |          |          |
|  3    | TYPE_APPLICATION_STARTING      | 同上       |          |          |
|  4    | TYPE_DRAWN_APPLICATION         | 同上       |          |          |
|  99   | LAST_APPLICATION_WINDOW        | 同上       |          |          |

可见，`FIRST_SUB_WINDOW～LAST_SUB_WINDOW`提前，是可以显示的，且导致`APPLICATION_WINDOW`无法显示。

也就是说，`SUB_WINDOW`和`APPLICATION_WINDOW`之间，先设置的会生效，后设置的会被无效化。`SYSTEM_WINDOW`则没有任何影响。

另外，错误提示在不同版本不一样。低版本提示`token is not for an application`，高版本提示`is your activity running`。实际上，`is your activity running`会更贴切。因为这两类Window，只能使用Activity的Context（见下面）。

### 不添加任何权限声明 + ApplicationContext

#### 模拟器 API 27（8.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       | is your activity running?      |         |          |
|  1    | TYPE_BASE_APPLICATION          | 同上      |          |          |
|  2    | TYPE_APPLICATION               | 同上      |          |          |
|  3    | TYPE_APPLICATION_STARTING      | 同上      |          |          |
|  4    | TYPE_DRAWN_APPLICATION         | 同上      |          |          |
|  99   | LAST_APPLICATION_WINDOW        | 同上      |          |          |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               | 同上      |          |          |
|  1999 | LAST_SUB_WINDOW                | 同上      |          |          |

API 25 （7.1）和真机下，以及顺序倒过来，结果都是和上表一致。

可见，ApplicationContext不能用于`应用悬浮窗`。`应用悬浮窗`只能使用ActivityContext。

### 总结（不添加权限声明）

基于Android原生：

1. 不进行权限声明，可以使用`APPLICATION_WINDOW`和`SUB_WINDOW`，统称为`应用悬浮窗`。即`应用悬浮窗`不需要权限声明。

2. `APPLICATION_WINDOW`和`SUB_WINDOW`不能同时使用在同一个Activity。一个Activity显示过`APPLICATION_WINDOW`后，则另一类`SUB_WINDOW`不能显示；反之亦然。

3. `APPLICATION_WINDOW`的坐标都是基于Activity可视区域；而`SUB_WINDOW`大部分是基于Screen屏幕坐标（部分子窗口是显示在Activity）。

4. `应用悬浮窗`只能使用ActivityContext，不能使用ApplicationContext／ServiceContext。

> 意味着，如果要在Service中创建一个悬浮窗，只能使用2000以上的SystemWindow。


## 添加权限声明测试

首先，需要了解的是，Android 6.0 (API 23) 以前，权限声明了就可以直接使用系统悬浮窗。

API 24以后，即Android 7.0以后，权限管理变成了用时申请的方式。所以，悬浮窗权限除了声明外，还需要在代码层对悬浮窗权限进行判断。

```java
if (Build.VERSION.SDK_INT >= 23) {
	// 判断悬浮窗权限是否开启
	if (Settings.canDrawOverlays(this)) {
		// 悬浮窗权限已开启，可以直接使用
		showOverlayWindow();
	} else {
		// 没有悬浮窗权限，去设置开启悬浮窗权限
		try {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			// 在Activity::onActivityResult里启动悬浮窗
			startActivityForResult(intent, 9999);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} else {
	// Android6.0以下，声明默认有悬浮窗权限，但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下的悬浮窗权限管理，也需要考虑做适配！！！
	showOverlayWindow();
}
```

如上，悬浮窗启动流程大致是：

1. 声明悬浮窗权限。
2. 判断当前运行环境。如果是Android6.0，可以直接显示悬浮窗。但如果是国产手机，由于国产手机的悬浮窗管理有自己的一套，需要自行考虑适配；如果是Android6.0以上，则先判断是否具备显示悬浮窗权限（Android7.0以上，权限配置有个开关，是否允许悬浮窗），如果已经具备，则可以显示悬浮窗，如果不具备，则通过Intent跳转到权限设置，等待用户开启悬浮窗权限。
3. 如果最后用户没有允许权限，则系统悬浮窗是不能用了。

> 锤子手机，对悬浮窗的管理是：如果开启了权限，则系统悬浮窗可以覆盖在其它APP上面；如果禁用了权限，则系统悬浮窗会自动变为应用悬浮窗，跟随应用切换前后台。


### 添加权限声明 + ActivityContext

#### 模拟器 API 27（8.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       |    no      |     yes     |    activty      |
|  1    | TYPE_BASE_APPLICATION          |    no      |     yes     |    activty      |
|  2    | TYPE_APPLICATION               |    no      |     yes     |    activty      |
|  3    | TYPE_APPLICATION_STARTING      |    no      |     yes     |    activty      |
|  4    | TYPE_DRAWN_APPLICATION         |    no      |     yes     |    activty      |
|  99   | LAST_APPLICATION_WINDOW        |    no      |     yes     |    activty      |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               |    is your activity running?      |          |          |
|  1999 | LAST_SUB_WINDOW                |    is your activity running?      |          |          |
|             | -       |   -      |  -        | -         |
|  2000 | FIRST_SYSTEM_WINDOW            |   permission denied       |          |          |
|  2000 | TYPE_STATUS_BAR                |   denied       |          |          |
|  2001 | TYPE_SEARCH_BAR                |   denied       |          |          |
|  2002 | TYPE_PHONE                     |   denied       |          |          |
|  2003 | TYPE_SYSTEM_ALERT              |   denied       |          |          |
|  2005 | TYPE_TOAST                     |   is your activity running?       |          |          |
|  2006 | TYPE_SYSTEM_OVERLAY            |   denied       |          |          |
|  2008 | TYPE_SYSTEM_DIALOG             |   denied       |          |          |
|  2010 | TYPE_SYSTEM_ERROR              |   denied       |          |          |
|  2011 | TYPE_INPUT_METHOD              |   is your activity running?       |          |          |
|  2038 | TYPE_APPLICATION_OVERLAY       |   no       |     yes      |   activity     |
|  2999 | LAST_SYSTEM_WINDOW             |   denied       |          |          |

与不声明权限对比，`TYPE_APPLICATION_OVERLAY`可以显示悬浮窗，其它`SYSTEM_WINDOW`大部分不能显示。


#### 模拟器 API 25（7.1）

|    Value    | WindowTypes              | ERROR     | SHOWN     | X.Y     |
| :------------- | :-------------  | :------------- |:------------- |:------------- |
|  1    | FIRST_APPLICATION_WINDOW       |    no      |     no     |                 |
|  1    | TYPE_BASE_APPLICATION          |    no      |     no     |                 |
|  2    | TYPE_APPLICATION               |    no      |     yes    |    activty      |
|  3    | TYPE_APPLICATION_STARTING      |    no      |     no     |                 |
|  4    | TYPE_DRAWN_APPLICATION         |    no      |     yes    |    activty      |
|  99   | LAST_APPLICATION_WINDOW        |    no      |     yes    |    activty      |
|             | -       |   -      |  -        | -         |
|  1000 | FIRST_SUB_WINDOW               |    is your activity running?      |          |          |
|  1999 | LAST_SUB_WINDOW                |    is your activity running?      |          |          |
|             | -       |   -      |  -        | -         |
|  2000 | FIRST_SYSTEM_WINDOW            |   permission denied       |          |          |
|  2000 | TYPE_STATUS_BAR                |   denied       |          |          |
|  2001 | TYPE_SEARCH_BAR                |   denied       |          |          |
|  2002 | TYPE_PHONE                     |   no       |    yes      |    activty      |
|  2003 | TYPE_SYSTEM_ALERT              |   no       |    yes      |    activty      |
|  2005 | TYPE_TOAST                     |   is your activity running?       |          |          |
|  2006 | TYPE_SYSTEM_OVERLAY            |   no       |    yes      |    activty      |
|  2008 | TYPE_SYSTEM_DIALOG             |   denied       |          |          |
|  2010 | TYPE_SYSTEM_ERROR              |   no       |    yes      |    activty      |
|  2011 | TYPE_INPUT_METHOD              |   is your activity running?       |          |          |
|  2038 | TYPE_APPLICATION_OVERLAY       |   denied       |          |          |
|  2999 | LAST_SYSTEM_WINDOW             |   denied       |          |          |

API 25（7.1）下， `TYPE_PHONE`，`TYPE_SYSTEM_ALERT`，`TYPE_SYSTEM_OVERLAY`，`TYPE_SYSTEM_ERROR`是可以显示的！!

#### 锤子真机 API 25（7.1）

锤子手机就不测了，和上面一致，因为锤子手机不声明权限也一样可以使用系统悬浮窗。

区别就是，多了`TYPE_TOAST`这个可以显示。


### 添加权限声明 + ApplicationContext

使用`ApplicationContext`效果一样。

### 总结（添加权限声明）

1. 对于系统悬浮窗，使用`ActivityContext`和`ApplicationContext`是一样的。
2. Android7.0可以使用多个系统类型弹窗，而Android8.0只能使用`TYPE_APPLICATION_OVERLAY`了。
3. 意味着，如果弹系统悬浮窗，需要考虑当前Android系统版本。Android7.0可以使用`TYPE_PHONE`等，而Android8.0只能使用`TYPE_APPLICATION_OVERLAY`。

## 总结

以上测试是没有获取系统UID基础上进行测试，如果是`系统应用`，能用的`WindowType`应该会更多。但对于`普通应用`，能用的窗口类型，就是以上测试通过的类型。

Service中可以弹出窗口，但根据上面的测试，Service只能使用系统悬浮窗。
