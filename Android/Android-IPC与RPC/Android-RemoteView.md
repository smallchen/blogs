## Android RemoteView的使用场景

```java
private void updateUI(RemoteViews remoteViews) {
//        View view = remoteViews.apply(this, mRemoteViewsContent);
     int layoutId = getResources().getIdentifier("layout_simulated_notification", "layout", getPackageName());
     View view = getLayoutInflater().inflate(layoutId, mRemoteViewsContent, false);
     remoteViews.reapply(this, view);
     mRemoteViewsContent.addView(view);
}
```
