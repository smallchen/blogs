## Android 判断服务／广播是否存在

```java
private void myHasPrintService(ValueCallback callback) {
	PackageManager pm = this.mContext.getPackageManager();
	List<ResolveInfo> installedServices = pm.queryIntentServices(new Intent("android.printservice.PrintService"), 132);
	boolean hasPrintService = installedServices != null && !installedServices.isEmpty();
	if(callback != null) {
		callback.onValid(Value.HAS_PRINT_SERVICE, hasPrintService);
	}

}
```

```java
Intent intent = new Intent(ACTION_MARK_OPERATE);
intent.putExtra(EXTRA_MARK_OPERATION, VAL_CLOSE_MARK);
intent.putExtra(EXTRA_MARK_HAS_SAVED, !mHasModified);
checkIntentHasReceiver(intent)

private boolean checkIntentHasReceiver(Intent intent) {
	PackageManager pm = mContext.getPackageManager();
	List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(intent, 0);
	return resolveInfos != null && !resolveInfos.isEmpty();
}
```
