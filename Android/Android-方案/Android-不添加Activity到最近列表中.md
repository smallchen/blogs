## Android不添加Activity到最近列表中

1. 通过`manifest.xml`设置

```java
<activity
            android:name=".MainActivity"
            android:excludeFromRecents="true">
```


在主Activity有LAUNCHER的前提下，android:excludeFromRecents="true",才能达到在最近任务列表中隐藏该应用的目的。


2. 通过Intent启动时设置

```java
public static Intent getServerMainActivityIntent(Context context) {
        Intent intent = new Intent(ACTION_SERVER_ACTIVITY_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }
```
