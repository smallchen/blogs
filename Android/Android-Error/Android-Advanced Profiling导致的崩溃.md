## Android advanced profiling 导致的崩溃

```
CrashReport: java.lang.UnsatisfiedLinkError: No implementation found for long com.android.tools.profiler.support.network.HttpTracker$Connection.nextId() (tried Java_com_android_tools_profiler_support_network_HttpTracker_00024Connection_nextId and Java_com_android_tools_profiler_support_network_HttpTracker_00024Connection_nextId__)
      at com.android.tools.profiler.support.network.HttpTracker$Connection.nextId(Native Method)
      at com.android.tools.profiler.support.network.HttpTracker$Connection.<init>(HttpTracker.java:191)
      at com.android.tools.profiler.support.network.HttpTracker$Connection.<init>(HttpTracker.java:186)
      at com.android.tools.profiler.support.network.HttpTracker.trackConnection(HttpTracker.java:280)
      at com.android.tools.profiler.support.network.httpurl.TrackedHttpURLConnection.<init>(TrackedHttpURLConnection.java:49)
      at com.android.tools.profiler.support.network.httpurl.HttpURLConnection$.<init>(HttpURLConnection$.java:42)
      at com.android.tools.profiler.support.network.httpurl.HttpURLWrapper.wrapURLConnectionHelper(HttpURLWrapper.java:42)
      at com.android.tools.profiler.support.network.httpurl.HttpURLWrapper.wrapURLConnection(HttpURLWrapper.java:55)
      at com.jokin.fridayreport.util.http.HttpURLHelper.createConnection(HttpURLHelper.java:90)
      at com.jokin.fridayreport.util.http.HttpURLHelper.openConnection(HttpURLHelper.java:24)
      at com.jokin.fridayreport.util.http.HttpStack.performRequest(HttpStack.java:59)
      at com.jokin.fridayreport.util.http.BasicNetwork.performRequest(BasicNetwork.java:71)
      at com.jokin.fridayreport.util.http.NetworkDispatcher.runInter(NetworkDispatcher.java:83)
      at com.jokin.fridayreport.util.http.NetworkDispatcher.run(NetworkDispatcher.java:73)
E/CrashReport: #++++++++++++++++++++++++++++++++++++++++++#
```

如上，出现`native`崩溃，定位到的是`java.lang.UnsatisfiedLinkError`。

出现这个崩溃，是因为，有一次手贱，开启了高级性能检测。

关闭它即可：`Run -> Edit Configurations -> app -> Profiling - Disable advanced profiling`。
