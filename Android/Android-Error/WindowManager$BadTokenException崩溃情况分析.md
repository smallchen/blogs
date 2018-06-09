## WindowManager$BadTokenException崩溃情况分析

1.Unable to add window --token null is not valid; is your activity running

2.Unable to add window --token null is not for an application **

3.Unable to add window -- token android.os.BinderProxy@XXX is not valid;
is your activity running

4.Unable to add window -- token android.app.LocalActivityManager
$LocalActivityRecord @xxx is not valid; is your activity running

### token null is not valid; is your activity running

该异常多见于PopupWindow组件的使用中抛出。

**比如：** 在Activity的onCreate中，直接执行PopupWindow的show方法。

**原因：** 在PopupWindow.showAtLocation时，popwindow必须依附于某一个parent view，而在oncreate中parent view还没有加载完毕，必须要等activity的生命周期函数全部执行完毕，依附的view加载好后，才可以执行PopupWindow的显示。
> parent view所在的Activity还没创建完毕，就依赖Activity，所以报 is your activity running错误。

**解决：** MainThread.post()可以解决此类先后顺序问题。post到主线程，会将代码块放在主线程队列尾部，做到当前流程执行完毕后，才执行代码块的效果。由于UI都是是主线程中执行的，所以在UI的生命周期中，使用post，可以保证UI流程执行完后，才执行代码块。

> 建议看post的文档。

例子：
```java
findviewById（R.id.mView）.post（new Runnable() {
    @Override
    public void run() {
        popwindow.showAtLocation(mView, Gravity.CENTER, 0, 0);

    }
});
```
**总结：** PopupWindow必须在某个事件中显示或者是开启一个新线程去调用，不能直接在onCreate方法中显示一个PopupWindow，否则永远会有以上的错误。

### token null is not for an application

该异常多见于AlertDialog组件的使用中抛出。

**比如：** 使用ApplicationContext来启动AlertDialog。
**原因：** 必须使用Activity,因为只有Activity才能添加一个窗体。是由于AlertDialog的WindowType决定的。Dialog需要一个window token不为空的Context实例。ApplicationContext是没有window token的。有window token的指的是在Window Manager中添加过View，比如Activity。

> Dialog requires a Context reference whose window token is not null. here ApplicationContext's window token is null where as Activity will have it's own window

**解决：** 使用ActivityContext。

例子：
```java
new AlertDialog.Builder(this)
    .setIcon(android.R.drawable.ic_dialog_alert)  
    .setTitle("Warnning")  
    .setPositiveButton("Yes", positiveListener)
    .setNegativeButton("No", negativeListener)
    .create().show();
```

### token android.os.BinderProxy@XXX is not valid; is your activity running?

**原因：** 从错误信息我们也可以明白其原因，此问题根本原因就是由于将要弹出的dialog所要依附的View已经不存在导致的。当界面销毁后再弹出来；或者界面跳转时我们的view发生改变，dialog依附的context发生变化或者界面未运行了。

**解决：** 界面已经销毁引起的错误就只能判断界面是否存在然后再弹出了。

```java
//修正后代码
if(!isFinishing()) {
     alert.show();
}
```

### LocalActivityManager$LocalActivityRecord @xxx is not valid; is your activity running?

**原因：** 因为new对话框的时候，参数context 指定成了this，即指向当前子Activity的context。但子Activity是动态创建的，不能保证一直存在。其父Activity的context是稳定存在的，所以有下面的解决办法。

**解决：** 将context替换为getParent()即可。 注意：要创建dialog对象，上下文环境必须是activity,同时若ActivityGroup中嵌套ActivityGroup,嵌套多少就该使用多少个getParent()。

```java
//修正后代码，适用于一个或多个parent的情形
Activity activity = TestActivity.this;  
while (activity.getParent() != null) {  
    activity = activity.getParent();  
 }  

TipDialog dialog = new TipDialog(activity) ;
```


**注：为什么要使用getParent我们可以从ActivityGroup的内部机制来理解：**

TabActivity的父类是ActivityGroup,而ActivityGroup的父类是Activity。因此从Ams的角度来看，ActivityGroup与普通的Activity没有什么区别，其生命周期包括标准的start,stop,resume,destroy等，而且系统中只允许同时允许一个ActivityGroup.但ActivityGroup内部有一个重要成员变量，其类型为LocalActivityManager,该类的最大特点在于它可以访问应用进程的主类，即ActivityThread类。Ams要启动某个Activity或者赞同某个Activity都是通过ActivityThread类执行的，而LocalActivityManager类就意味着可以通过它来装载不同的Activity,并控制Activity的不同的状态。注意，这里是装载，而不是启动，这点很重要。所谓的启动，一般是指会创建一个进程（如果所在的应用经常还不存在）运行该Activity,而装载仅仅是指把该Activity作为一个普通类进行加载，并创建一个该类的对象而已，而该类的任何函数都没有被运行。装载Activity对象的过程对AmS来讲是完全不可见的，那些嵌入的Activity仅仅贡献了自己所包含的Window窗口而已。而子Activity的不同状态是通过moveToState来处理的。

所以子Activity不是像普通的Activity一样，它只是提供Window而已，所以在创建Dialog时就应该使用getParent获取ActivityGroup真正的Activity，才可以加Dialog加入Activity中。

参考：
```
作者：爱情小傻蛋
链接：https://www.jianshu.com/p/4c5fafe08fa7
來源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```
