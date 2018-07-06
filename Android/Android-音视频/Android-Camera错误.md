## Android Camera 错误

#### stop preview failed : java.lang.RuntimeException: Camera is being used after Camera.release() was called

```java
10:33:59.404 4551-4551/com.jokin.welcome D/Welcome-EditAndPreviewActivityCameraHelper: │ [main] releaseCamera (CameraHelper.java:346) - releaseCamera
10:33:59.420 4551-4697/com.jokin.welcome E/EditAndPreviewActivityCameraHelper: ########################
06-14 10:33:59.427 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ [RxNewThreadScheduler-3] run (CameraHelper.java:336) - stop preview failed : java.lang.RuntimeException: Camera is being used after Camera.release() was called
06-14 10:33:59.427 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at android.hardware.Camera._stopPreview(Native Method)
06-14 10:33:59.427 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at android.hardware.Camera.stopPreview(Camera.java:733)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at com.jokin.welcome.camera.CameraHelper$6.run(CameraHelper.java:334)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.operators.observable.ObservableDoOnEach$DoOnEachObserver.onComplete(ObservableDoOnEach.java:135)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.checkTerminated(ObservableObserveOn.java:279)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.drainNormal(ObservableObserveOn.java:171)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver.run(ObservableObserveOn.java:250)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.schedulers.ScheduledRunnable.run(ScheduledRunnable.java:59)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at io.reactivex.internal.schedulers.ScheduledRunnable.call(ScheduledRunnable.java:51)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at java.util.concurrent.FutureTask.run(FutureTask.java:237)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:272)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
06-14 10:33:59.428 4551-4697/com.jokin.welcome E/Welcome-EditAndPreviewActivityCameraHelper: │ 	at java.lang.Thread.run(Thread.java:761)
06-14 10:33:59.533 4551-4696/com.jokin.welcome D/Welcome-EditAndPreviewActivityCameraHelper: │ [RxNewThreadScheduler-2] run (CameraHelper.java:362) - releaseCamera done!
```

根本原因：在调用了`Camera.release()`后，还继续调用了Camera其它的API。

这里是因为，releaseCamera的时候（见log头部），先执行stopPreview()，但并不是同一个子线程中执行，而是post到另一个子线程执行（由于Camera的接口都很卡，所以每个接口都进行了二次封装，post到一个新的子线程中执行）。

所以，当前线程已经释放了Camera，另一个线程才开始执行`stopPreview`。所以报错。

> 虽然报错，但似乎对应用没什么重大影响。

#### Camera和SurfaceTextureView
