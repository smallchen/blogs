## Android 开发艺术探索

## Activity的生命周期和启动模式

* IntentFilter匹配规则

* Intent Flags

其它不多说，注意以下几种情况：

### 单个Activity

1. 启动一定会顺序发生：`onCreate->onStart->onResume`

2. 退出一定会顺序发生：`onPause->onStop->onDestroy`

3. 暂停恢复一定会顺序发生：`onPause->onResume`

4. Home恢复一定会顺序发生：`onPause->onStop->onRestart-onResume`

5. Back键/finish()调用一定会顺序发生：`onPause->onStop->onDestroy`

6. `onStop`之前一定有`onPause`，`onStart`之后一定有`onResume`

7. `onStart`可以理解成后台准备完成。`onResume`可以理解成显示完成。

### 两个不同的Activity切换

1. A->B，A的`onPause`一定先于B的`onCreate/onStart/onResume`。至于B是`onCreate->onStart->onResume`还是直接`onStart->onResume`，取决于B是否已经存在。

```java
- ActivityStack.java
- ActivityStack:resumeTopActivityInnerLocked()
  - // Start pausing the currently resumed activity
  - startPausingLocked()
  - mStackSupervisor.startSpecificActivityLocked
  @ ActivityStackSupervisor:startSpecificActivityLocked()
  	- app != null ?
	T: realStartActivityLocked()
	   - // Start activity
       - app.thread.scheduleLaunchActivity()
       - // AIDL
       @ IApplicationThread:scheduleLaunchActivity()
          - // ApplicationThread is extends IApplicationThread.Stub
          @ ActivityThread.ApplicationThread:scheduleLaunchActivity()
             - sendMessage(H.LAUNCH_ACTIVITY, r);
	F: mService.startProcessLocked()
	   - // Start Process
	   @ ActivityManagerService:startProcessLocked()
```

所以，官方说：不能在onPause执行重量级任务，因为下一个Activity必须等待当前Activity的onPause完成后才能Resume。所以，onPause中执行重量级任务，会卡下一个Activity的显示。我们应当尽量把重量级任务放在onStop中处理。

有一个很坑爹的场景是，当新的Activity都是透明的时候，只有onPause而不会有onStop，这个时候只能在onPause中尽快处理。

2. A->B，B不一定有`onCreate`，但一定有`onStart`和`onResume`

3. A->B，不能假定A的`onDestroy`一定先于B的`onDestroy`。除了A会加入栈外，即使要销毁，A的时机也是不太确定的。

### 两个相同的Activity切换

两个相同的Activity切换，需要注意的主要是重入的问题，因为他们是同一份代码。比如，A->A，在`onCreate`时开启定时器，在`onDestroy`时暂停定时器，那么A->A将会发生定时器失效的问题。原因是，后者的`onCreate`，通常先于前者的`onDestroy`，所以前者的`onDestroy`会将定时器暂停，使定时器失效。

为解决这个问题，最好使用`onPause`和`onResume`进行处理。因为A->A，前者的`onPause`一定先于后者所有回调。

### 异常处理

#### onSaveInstanceState/onRestoreInstanceState回调

只出现在Activity被异常终结的情况。比如，后台杀死，屏幕旋转，视图配置变化等等。正常的销毁，比如finish()，Back键等等，不会有回调！！

你可以理解成，出栈并销毁，属于正常情况，不会回调；单纯销毁未出栈，就属于异常销毁，会回调。

`onSave`和`onPause`时序无关，可能在前，可能在后。但一定在`onStop`之前调用。

`onRestore`一定在`onStart`之后。

异常重建时，除了`onSave/onRestore`外，onCreate时，通过`Bundle`对象也可以判断是否是重建过程。

默认情况下，在`onSave/onRestore`中，系统帮忙做了一定的保存工作。比如，保存和恢复文本框数据、ListView滚动位置、视图结构等等。所以，通常并不需要做特殊处理。

View也有`onSaveInstanceState/onRestoreInstanceState`，用于保存和恢复对应View可以处理的数据。

流程：

```java
- Activity意外销毁
- Activity:onSaveInstanceState()
    - 委托Window保存数据
        - Window委托顶层容器，通常是DecorView(ViewGroup)。
        - DecorView委托ViewRootImpl。
        - ViewRootImpl通知所有子View:onSaveInstanceState().
            - 所有子View保存完毕。
            - 得到一个Bundle。
- Activity恢复
- Activity:onRestoreInstanceState(Bundle)
    - 将Bundle传递下去，依次恢复
```

这种分发流程，和事件分发，测量、布局、绘制分发类似。叫**委托思想**，上层委托下层、父容器委托子元素去处理一件事。

问题：onCreate和onRestoreInstanceState区别？

首先，两者回调时机不一样。onRestore一定发生在意外销毁，此时Bundle参数一定不为null，不需判空；而onCreate在正常创建过程，Bundle是null，只有在意外销毁重建时，Bundle才有值，所以需要判空。

两者的回调里都可以进行数据恢复。但官方建议是，在onRestoreInstanceState里面进行数据恢复。

#### 屏幕旋转等资源发生改变

屏幕旋转或资源发生改变，会导致Activity销毁重建，有上面的保存恢复数据的回调。但可以通过配置避免销毁。

影响的配置有：

* locale 语言
* keyboard 外插了键盘
* keyboardHidden 调出/隐藏键盘
* fontScale 系统字体
* uiMode 夜间模式启用
* orientation 旋转
* screenSize 旋转
* layoutDirection 布局方向
* 等等

通过添加在`android:configChanges="locale|keyboard"`等来避免销毁重建。而是通过`onConfigurationChanged()`回调通知。

另外，旋转屏幕的时候，不重建，Android默认也会把视图进行旋转，并不需要自行处理。除了一些特殊的视图，比如：固定布局的View。

## 
