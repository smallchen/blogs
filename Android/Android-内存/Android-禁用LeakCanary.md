## Android 禁用LeakCanary

`command + o` 查找 LeakCanary，然后发现no-op也有一个：

```
public final class LeakCanary {

  public static RefWatcher install(Application application) {
    return RefWatcher.DISABLED;
  }
}
```

带op的初始化：

```
public final class LeakCanary {

    public static RefWatcher install(Application application) {
      return refWatcher(application).listenerServiceClass(DisplayLeakService.class)
          .excludedRefs(AndroidExcludedRefs.createAppDefaults().build())
          .buildAndInstall();
    }
}
```

所以，禁用就是：

```
mRefWatcher = RefWatcher.DISABLED;
```

开启就是：

```
mRefWatcher = LeakCanary.install(this);
```
