
线程执行。

```java
private final Handler mHandler = new Handler();
private final Runnable mDismissAction = this::dismissDialog;

void dismissDialog() {
	// ...
}
@Override
public void dismiss() {
	if (Looper.myLooper() == mHandler.getLooper()) {
		dismissDialog();
	} else {
		mHandler.post(mDismissAction);
	}
}
```
