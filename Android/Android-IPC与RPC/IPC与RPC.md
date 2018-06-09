## IPC 与 RPC

### IPC

进程间通信（IPC，Inter-Process Communication）

进程间通信技术包括消息传递、同步、共享内存和远程过程调用。

分为：
* 本地过程调用(LPC)（消息／管道／共享内存等等）
* 远程过程调用(RPC)

由于Android上的AIDL只是针对于系统内，进程间通信，所以属于IPC，而不属于RPC。

但其实，系统内也是基于Socket的，所以使用socket的IPC通信，和RPC其实应该一样。

### RPC

远程过程调用(Remote Procedure Call)

更偏向于，通过网络进行的调用。
