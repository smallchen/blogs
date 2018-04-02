[tcp1]:tcp1.jpg
[tcp2]:tcp2.jpg

#### 三次握手 和 四次挥（握）手

三次握手是连接时

```java
开始
C -> SYN -> S
C <- SYN+ACK <- S
C -> ACK -> S
结束
```

四次挥手是关闭时

```java
开始
C -> FIN -> S
C <- ACK <- S

C <- FIN <- S
C -> ACK -> S
结束
```

**为什么连接时只需要三次，而断开时需要四次？**

因为连接时，服务端可以将SYN和ACK合并一个包发到客户端。无需分两次。
而关闭时，TCP是双通道，支持一端先关闭，另一端后关闭，两端时机不确定，没办法合并，所以需要共四次挥手。

> Client调用shutdown时，只是Client到Server端关闭，表示Client已经没有数据发送了；Server端收到消息时，根据Server是否还需要发送数据来决定是否调用shutdown。如果调用shutdown，则Server到Client端也关闭。

> 在TCP协议层，一端收到关闭SYN时，会立刻应答ACK，因为是上层决定自己这端是否也需要关闭，这个过程等待应答会出现时延。而且也不保证应用层会关闭，所以这里等待可能得不偿失，应用层可能根本不需要合并。

**详细状态切换**

```java
开始
C -> SYN(seq=x) -> S
C <- SYN(seq=y)+ACK(ACK=x+1) <- S
C -> ACK(ACK=y+1) -> S
结束
```

**C端：**
`SYN_SENT`： 发送了SYN还没有收到应答ACK。
`ESTABLISHED`： 发出SYN，收到了ACK。

**S端：**
`LISTEN`： Server端准备好。
`SYN_RECV`： Server收到了SYN。（通常客户端的ACK很快就应答了，所以该状态比较少见）
`ESTABLISHED`：Server收到Client的ACK。

```java
开始
C -> FIN -> S
C <- ACK <- S

C <- FIN <- S
C -> ACK -> S
结束
```

**主动关闭端（通常是C端）：**

`FIN_WAIT_1`：发起方发送了FIN（通常接收端收到都是立即回ACK，所以这个状态比较少见）。
`FIN_WAIT_2`：发起方收到了ACK（半连接状态，主动端已关闭）。
`TIME_WAIT`：半连接状态下，已经收到被动端FIN，发送最后端ACK，进入此状态(*见下面解析*)。
`CLOSING`：向对方发送FIN，没收到ACK，直接收到FIN，即双方都是先收到FIN。（上面说了，ACK一般是秒回，所以这种状态也比较少见）。

**被动关闭端（通常是S端）：**

`CLOSE_WAIT`：收到FIN，发送了ACK（半连接状态，主动端已关闭）**主动关闭端此时对应FIN_WAIT_2状态**。
`LAST_ACK`：发送FIN报文后等待对方最后的ACK，收到这个最后的ACK即进入`CLOSED`状态。

**解析**
1. 连接过程比较简单，就不多说。
1. 关闭过程，主动关闭端表示先发起关闭的那端，被动关闭端表示收到关闭才关闭的那一端。主动关闭端可以是客户端，也可能是服务端。（下面以客户端为主动关闭端来解析）
2. 客户端发送完数据，主动发起关闭，服务端收到并应答了。此时，客户端对应`FIN_WAIT_2`状态，服务端对应`CLOSE_WAIT`状态。
3. 服务端也发送完数据，发送FIN，等待客户端应答。此时服务端状态为`LAST_ACK`。
4. 客户端收到服务端关闭的FIN，应答ACK。此时客户端状态为`TIME_WAIT`，而不是立即进入`CLOSED`状态。两倍报文寿命时间后，客户端才进入`CLOSED`状态。
5. 服务端收到客户端的ACK，此时服务端状态立即变为`CLOSED`。

**TIME_WAIT**
主动关闭端在最后应答了服务端的FIN后，为什么不是立即进入`CLOSED`而是进入`TIME_WAIT`定时器状态？

客户端虽然应答了ACK，但由于网络问题，服务端不一定能够收到ACK，由于TCP协议的超时重传，服务端会再次发起FIN。假如客户端在第一次应答就把连接关闭，接下来重传的数据就会无法发送，所以客户端需要等待两个报文寿命时间，保证收不到服务端的重传数据，此时就可以安全的认为服务端收到了ACK，客户端可以真正的关闭。

相比客户端，服务端发出FIN，收到ACK，说明消息完整了，所以服务端可以立即`CLOSED`。

> `TIME_WAIT`是出现在主动关闭端，服务端有时候拒绝服务，也会主动关闭连接。


**如果服务端FIN一直没收到ACK要怎么办?**
LAST-ACK一直收不到，服务端可以在合适的时机关闭。

```java
The second purpose was to ensure the remote end is not in the LAST-ACK state
because of the lost of the last ACK.
The remote end will retransmit the FIN segment until:
 - it gives up (and tear down the connection), or
 - it receives the ACK it is waiting (and tear down the connection), or
 - it receives a RST (and tear down the connection).
```
tcp-time-wait.md
<http://www.cnblogs.com/lyhabc/p/5577989.html>

附录：
![tcp1][tcp1]
>（左边色块表示主动关闭端，右边色块表示被动关闭端）

下面这张容易理解点

![tcp2][tcp2]

这幅图要比解析那里清楚。

#### 扩展

**去掉TIME_WAIT是否可行**
如上，要去掉TIME_WAIT，可以：
1. 客户端收到服务端的FIN，应答后就马上关闭。
2. 服务端发出FIN后，不管收不收到都进行关闭。

不可行。如果服务端发出FIN后就立即关闭，由于网络问题，客户端可能会收不到FIN。所以服务端必须保留超时重传，确保客户端能够收到FIN。
客户端收到FIN后，不能立即关闭，这个上面已经说了，这也是为什么要引入TIME_WAIT的原因。

服务端不太喜欢TIME_WAIT状态，因为占用了端口和资源。去掉TIME_WAIT状态可以通过设置SOCKET变量的SO_LINGER标志，来避免SOCKET在close()之后进入TIME_WAIT状态，这时将会发送RST强制终止TCP连接(跳过正常的TCP四次握手的终止方式)。

**关闭TCP连接一定需要4次挥手吗?**
不一定。可以通过RST强制终止。但个人觉得，这已经是异常中断了，不应该把异常中断作为打破4次挥手的案例。

> **RST**
> TCP首部中的6个标志比特之一,表示重置连接、复位连接。
> 比如，客户端发送的请求，如果服务端刚好重启了，此时服务端收到请求，由于出现异常，TCP处理原则是，服务端默认以RST报文应答。即重置连接。
> 所以RST可以用来中断TCP连接。

**优化TIME_WAIT是否可行**
1. 保留TIME_WAIT
2. 服务端在收到LAST ACK后发送ACK到客户端，通知客户端可以结束等待了。

未找到答案。

**TCP协议变种**
1. 被动中断端（服务器）收到FIN + ACK + Data
    服务端将最后的数据Data，和中断的FIN，以及应答客户端的FIN的ACK合并成最后一个包。

2. 三次握手，客户端的ACK + Data
    客户端在连接应答时，同时将第一个数据发送到服务端。
