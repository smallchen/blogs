## Git冲突解决

```java
/**
 * 通道变化时关闭批注
 * 主线程关闭批注，涉及到UI资源的释放
 */
<<<<<<< HEAD
public void closeMarkIfMulti() {
=======
private void closeMarkIfMulti() {
>>>>>>> gitlab/master
    CommonUtil.disposeDisposable(mCloseMultiMarkDisposable);
    mCloseMultiMarkDisposable = Observable.empty()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(new io.reactivex.functions.Action() {
                @Override
                public void run() throws Exception {
                    closeMark();
<<<<<<< HEAD
                    NLog.e(TAG, "--关闭批注--" + mMultiMark);
                    if (mMultiMark != null) {
                        mMultiMark.realClose();
                        mMultiMark = null;
                        NLog.e(TAG, "--多页批注已经关闭--");
=======
                    if (mMultiMark != null) {
                        mMultiMark.realClose();
                        mMultiMark = null;
                        Log.d(TAG, "--多页批注已经关闭--");
>>>>>>> gitlab/master
                    }
                }
            }).subscribe();
}
```

如上。

```java
<<<<<<< HEAD
=======
```
表示当前最新的修改。最新的修改，可能是本地的，也可能是远程的。

```java
=======
>>>>>>> gitlab/master
```
表示gitlab/master这个分支的修改。

```java
<<<<<<< HEAD
// 最新的修改
=======
// gitlab/master分支的修改
>>>>>>> gitlab/master
```

HEAD是最新的，如果本地走在前面，则本地是HEAD；如果远程走在前面，则远程是HEAD。

```java
<<<<<<< HEAD
        tools:replace="android:name, android:label"
=======
        tools:replace="android:name,android:theme"
>>>>>>> origin/feature_jd
```

两边都进行了修改！这种要非常小心！以上修改，就是合并两者。

需要注意：

1、冲突从`<<<<<<`开始，以`>>>>>>`结束。冲突解决只需看中间的内容。

2、不在两个标记中的代码，一定不要动！！！

2、两块代码，是有重复的。取一边是比较容易解决的。如果两边都各取一部分，非常危险！！

3、可能存在函数或变量重命名的冲突，要考虑是否被重命名了。

4、同一个文件，上一块冲突，可能夹杂着下一块的冲突！！！

比如。

```java
<<<<<<< HEAD
        @Override
        public void onReceiveNotify(String notifyType, @Nullable SystemInput systemInput, int priority) {
            NLog.e(TAG, " notifyType: " + notifyType);
            //when input source change, close mark!!!
            closeMarkIfMulti();
        }
    };

    private Mark.StatusCallback mMarkStatusCallback = new Mark.StatusCallback() {
        @Override
        public void onVisible() {
            mMarkStatusCallbackHandler.onVisible();
=======
        @Override
        public void onReceiveNotify(String notifyType, @Nullable SystemInput systemInput, int priority) {
            Log.e(TAG, " notifyType: " + notifyType);
            //when input source change, close mark!!!
            closeMarkIfMulti();
>>>>>>> gitlab/master
    }

／／ 下一块冲突
@Override
<<<<<<< HEAD
public void onInvisible() {
    mMarkStatusCallbackHandler.onInvisible();
}
```

很明显，HEAD并不是修改了`onReceiveNotify`，而是新增了`StatusCallback`，导致了旧的`onReceiveNotify`冲突。

HEAD中冲突，包含着下一个冲突的一部分。修改后应该为：

```java
///// 保留HEAD，删除gitlab/master.
        @Override
        public void onReceiveNotify(String notifyType, @Nullable SystemInput systemInput, int priority) {
            NLog.e(TAG, " notifyType: " + notifyType);
            //when input source change, close mark!!!
            closeMarkIfMulti();
        }
    };

    private Mark.StatusCallback mMarkStatusCallback = new Mark.StatusCallback() {
        @Override
        public void onVisible() {
            mMarkStatusCallbackHandler.onVisible();
////
    }
```
