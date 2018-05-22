
## 使用当前时间戳进行点击限制存在的漏洞。

```java
private long last = -1;
public void onClick(View v) {
	long now = System.currentTimeMillis();
	if (last > 0 && now - last <= 200) {
		Log.e(TAG, "click too fast, ignore. now:"+now+",last:"+last);
		return;
	}
	last = now;
}

E/Toolbar: click too fast, ignore. now:1526388568960,last:2050195501909
```

如上，由于系统时间被设置，或者被网络同步了，导致后续的点击一直不生效。

可以改为系统启动时间。

SystemClock.elapsedRealtime();
