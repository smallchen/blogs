## Android 多线程样例


```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Boolean> mExecutorTask = executor.submit(new Callable<Boolean>() {
    @Override
    public Boolean call() throws Exception {
        return isHasCamera();
    }
});

try {
    isHasCamera = mExecutorTask.get(2000, TimeUnit.MILLISECONDS);
} catch (Exception e) {
    NLog.e("error", e, "ExecutorTask occur exception");
}
```
