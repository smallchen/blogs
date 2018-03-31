判断对应的Intent是否有广播接收器

```java
private boolean checkIntentHasReceiver(Intent intent) {
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(intent, 0);
		return resolveInfos != null && !resolveInfos.isEmpty();
	}
```
