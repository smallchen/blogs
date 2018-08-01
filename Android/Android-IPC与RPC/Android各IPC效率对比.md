## Android各IPC方式的效率对比

| IPC 方式 | 缓冲区大小     | 吞吐量     |
| :------------- | :------------- | :------------- |
| Binder       | 4M       | 2Gb/s |
| Socket       | 64K       |  |









Binder数据来源：

很好的一篇。
<https://elinux.org/Android_Binder>
<https://source.android.com/compatibility/vts/performance>
小米系统工程师–Gityuan
<https://blog.csdn.net/qian520ao/article/details/78089877>
<http://gityuan.com/2015/10/31/binder-prepare/>
<http://blog.ifjy.me/%E8%BD%AF%E4%BB%B6%E5%BC%80%E5%8F%91/2016/07/16/android%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0.html>

```java
… binder 可以达到的最大吞吐量的计算公式为：

8 字节有效负荷的最大吞吐量 = (8 * 21296)/69974 ~= 2.423 b/ns ~= 2.268 Gb/s
```
