## Android Accessibility

最后没忍住，还是看一下Accessibility。

Accessibility是Android的一个辅助功能，要使用Accessibility，必须要有一个服务端和一个客户端：

* 服务端：可以接收AccessibilityEvent的应用。
* 客户端：可以发送AccessibilityEvent的应用。

注：默认情况下，View只有在Visible的时候，才会发送事件，如果不是，则不会有事件。对于这点，只能修改系统源码来实现：

```java
// AccessibilityInteractionController.java
+++ b/frameworks/base/core/java/android/view/AccessibilityInteractionController.java
final class AccessibilityInteractionController {
    private final class AddNodeInfosForViewId implements com.android.internal.util.Predicate<View> {
         @Override
         public boolean apply(View view) {
            // 对特定Id的View，无论显示与否，都触发事件。
            if (view.getId() == mViewId && view.getId() == 2131559634){
                mInfos.add(view.createAccessibilityNodeInfo());
            // 系统默认，只会对shown的View进行事件触发
            } else if (view.getId() == mViewId && isShown(view)) {
                mInfos.add(view.createAccessibilityNodeInfo());
            }
            return false;
         }
     }
}
```

`android8.1`对应的`Predicate`是`java.util.function.Predicate`，没有`apply()`方法，取儿代之的是`test()`方法。

```java
// AccessibilityInteractionController.java
private final class AddNodeInfosForViewId implements java.util.function.Predicate<View> {
     @Override
     public boolean test(View view) {
         if (view.getId() == mViewId && isShown(view)) {
             mInfos.add(view.createAccessibilityNodeInfo());
         }
         // test总是返回false，所以关键作用只是用于添加AccessibilityNodeInfo。
         return false;
     }
 }
```

实现了`implements java.util.function.Predicate`的就只有三处：上面一处，和View里面两处：

```java
// View.java
private static class MatchIdPredicate implements Predicate<View> {
    public int mId;

    @Override
    public boolean test(View view) {
        return (view.mID == mId);
    }
}
private static class MatchLabelForPredicate implements Predicate<View> {
    private int mLabeledId;

    @Override
    public boolean test(View view) {
        return (view.mLabelForId == mLabeledId);
    }
}
```

Predicate主要用来匹配检测。test返回true表示匹配，返回false表示不匹配。其中，上面的AddNodeInfosForViewId里面，test总是返回false，所以关键作用只是用于添加。

`Predicate:test()`只用于`View、ViewGroup、DayPickerViewPager`里面的`findViewByPredicateTraversal()`方法。字面意思是，遍历(Traversal)查找匹配。代码如下。

```java
// View.java
protected <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate,
        View childToSkip) {
    if (predicate.test(this)) {
        return (T) this;
    }
    return null;
}

// ViewGroup.java
@Override
protected <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate,
        View childToSkip) {
    if (predicate.test(this)) {
        return (T) this;
    }
    .....
}
```


## 服务端

服务端是用来处理AccessibilityEvent的应用。如果要成为一个服务端，要执行以下步骤：

1、必须继承系统的`AccessibilityService`，重写两个方法。

```java
public class AccessService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
```

2、然后声明服务：

```xml
<service
    android:name=".AccessService"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService"/>
    </intent-filter>
    <!-- 声明响应的辅助事件 -->
    <meta-data
     android:name="android.accessibilityservice"
     android:resource="@xml/accessibility"/>
</service>
```

* 声明`BIND_ACCESSIBILITY_SERVICE`权限,以便系统能够绑定该服务(4.0版本后要求)
* 配置<intent-filter>,其name为固定的为`android.accessibilityservice.AccessibilityService`。

3、声明响应的辅助事件：

```xml
<meta-data
     android:name="android.accessibilityservice"
     android:resource="@xml/accessibility"/>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeNotificationStateChanged|typeWindowStateChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="true"
    android:notificationTimeout="100"
    android:packageNames="com.event.from.target.package" />
```

以上声明表示，当前服务端接收来源于`com.event.from.target.package`这个应用的辅助事件。一旦目标应用有相关事件，就会触发服务端的`onAccessibilityEvent(AccessibilityEvent event)`，然后服务端通过参数中的event来进行操作。

* `packageNames`: 指定事件来源。比如监听目标为微信，就是`com.tencent.mm`，监听目标为office，就是`cn.wps.moffice_eng,com.microsoft.office.powerpoint`。

* `accessibilityEventTypes`: 表示该服务对界面中的哪些变化感兴趣，即监听哪些事件，比如窗口打开、滑动、焦点变化、长按等。具体的值可以在AccessibilityEvent类中查到。比如，`typeAllMask`表示接受所有的事件通知。

* `accessibilityFeedbackType`: 表示反馈方式，比如，是语音播放，还是震动。

* `accessibilityFlags`: 指定该服务的Flags，类似于窗口Flags的作用，用来设置服务自身的一些特性。比如：flagDefault就是指作为默认的辅助服务。FLAG_REQUEST_FILTER_KEY_EVENTS表示接收key-event。

* `canRetrieveWindowContent`: 表示该服务能否访问目标窗口中的内容。也就是如果你希望在服务中获取窗体内容，则需要设置其值为true。

* `notificationTimeout`: 接受事件的时间间隔，通常将其设置为100即可。

官方文档：<https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo>

4、保证系统启用了辅助服务。

权限的检测，见外面的《PermissionUtil》。

### 服务端方法

作为Accessibility的服务端，服务端是提供了很多方法来主动获取窗口内容（前提是启用了canRetrieveWindowContent）。大概就是，服务端可以主动获取系统当前的一些信息，方便服务端来进行逻辑处理，而不仅仅靠监听对象的AccessibilityEvent。或者说，收到监听对象的AccessibilityEvent事件后，服务端需要主动获取当前系统的内容，因为前者只有事件，而没有具体的环境，当前系统的环境，还需要服务端自行处理。

总而言之，创建了一个服务端，即使不监听任何事件，也可以调用以下方法主动访问：

内容相关：

* `findFoucs(int falg)`	// 查找拥有特定焦点类型的控件
* `getRootInActiveWindow()`	// 如果配置能够获取窗口内容,则会返回当前活动窗口的根结点
* `getSeviceInfo()`	// 获取当前服务的配置信息
* `setServiceInfo(AccessibilityServiceInfo info)` // 设置当前服务的配置信息
* `getWindows()` // 获取屏幕中的窗口
* `getSoftKeyboardController()` // 获取软键盘控制器
* `getFingerprintGestureController()` // 获取手势控制器
* `getAccessibilityButtonController()` // 导航区按钮


事件相关：

* `onAccessibilityEvent(AccessibilityEvent event)` // 有关AccessibilityEvent事件的回调函数.系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
* `onKeyEvent(KeyEvent event)` // 如果允许服务监听按键操作,该方法是按键事件的回调,需要注意,这个过程发生了系统处理按键事件之前
* `performGlobalAction(int action)`	// 执行全局操作,比如返回,回到主页,打开最近等操作

服务相关：
* `getSystemService(String name)` // 获取系统服务
* `onServiceConnected()` // 系统成功绑定该服务时被触发,也就是当你在设置中开启相应的服务,系统成功的绑定了该服务时会触发,通常我们可以在这里做一些初始化操作
* `disableSelf()`	// 禁用当前服务,也就是在服务可以通过该方法停止运行

更多方法接口，访问官网: <https://developer.android.com/reference/android/accessibilityservice/AccessibilityService>

## 客户端

View作为一个通用的发送端，里面调用了很多发送接口：

```
sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)；
sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)；
sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)；
sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)；
sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED)；
```

* TYPE_VIEW_CLICKED    // 当View被点击时发送此事件。
* TYPE_VIEW_LONG_CLICKED    // 当View被长按时发送此事件。
* TYPE_VIEW_FOCUSED    // 当View获取到焦点时发送此事件。
* TYPE_WINDOW_STATE_CHANGED    // 当Window发生变化时发送此事件。
* TYPE_VIEW_SCROLLED    // 当View滑动时发送此事件。

但你会发现，并不是所有应用，操作过程都有AccessibilityEvent出来。从上面事件来说，理论上，操作View的过程，基本上都应该有事件。

跟踪事件发送源码：

```
@ View:sendAccessibilityEvent(int eventType)
- sendAccessibilityEventInternal(int eventType)
    - AccessibilityManager.getInstance(mContext).isEnabled() ?
    F return
    T sendAccessibilityEventUnchecked()
        - sendAccessibilityEventUncheckedInternal(AccessibilityEvent event)
            ? isShown()
            F return
            T dispatchPopulateAccessibilityEvent
                - dispatchPopulateAccessibilityEventInternal()
                    - onPopulateAccessibilityEvent
                        - onPopulateAccessibilityEventInternal(AccessibilityEvent event)
                            - null
            - getParent().requestSendAccessibilityEvent()
                - onRequestSendAccessibilityEvent
                    - onRequestSendAccessibilityEventInternal()
```
