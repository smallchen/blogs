Coping with the TCP TIME-WAIT state on busy Linux servers
Coping with the TCP TIME-WAIT state on busy Linux servers



文章源自于：https://vincent.bernat.im/en/blog/2014-tcp-time-wait-state-linux.html



 Do not enable net.ipv4.tcp_tw_recycle.

The Linux kernel documentation is not very helpful about whatnet.ipv4.tcp_tw_recycle does:

Enable fast recycling TIME-WAIT sockets. Default value is 0. It should not be changed without advice/request of technical experts.

Its sibling, net.ipv4.tcp_tw_reuse is a little bit more documented but the language is about the same:

Allow to reuse TIME-WAIT sockets for new connections when it is safe from protocol viewpoint. Default value is 0. It should not be changed without advice/request of technical experts.

The mere result of this lack of documentation is that we find numerous tuning guides advising to set both these settings to 1 to reduce the number of entries in the TIME-WAIT state. However, as stated by tcp(7)manual page, the net.ipv4.tcp_tw_recycle option is quite problematic for public-facing servers as it won’t handle connections from two different computers behind the same NAT device, which is a problem hard to detect and waiting to bite you:

Enable fast recycling of TIME-WAIT sockets. Enabling this option is not recommended since this causes problems when working with NAT (Network Address Translation).

I will provide here a more detailed explanation in the hope to teach people who are wrong on the Internet.

xkcd illustration

xkcd: Duty Calls — Someone is wrong on the Internet


As a sidenote, despite the use of ipv4 in its name, thenet.ipv4.tcp_tw_recycle control also applies to IPv6. Also, keep in mind we are looking at the TCP stack of Linux. This is completely unrelated to Netfilter connection tracking which may be tweaked in other ways1.

About TIME-WAIT state
Purpose
Problems
Connection table slot
Memory
CPU
Other solutions
Socket lingering
net.ipv4.tcp_tw_reuse
net.ipv4.tcp_tw_recycle
Summary
About TIME-WAIT state
Let’s rewind a bit and have a close look at this TIME-WAIT state. What is it? See the TCP state diagram below2:

TCP state diagram

TCP state diagram


Only the end closing the connection first will reach the TIME-WAIT state. The other end will follow a path which usually permits to quickly get rid of the connection.

You can have a look at the current state of connections with ss -tan:

$ ss -tan | head -5
LISTEN     0  511             *:80              *:*     
SYN-RECV   0  0     192.0.2.145:80    203.0.113.5:35449
SYN-RECV   0  0     192.0.2.145:80   203.0.113.27:53599
ESTAB      0  0     192.0.2.145:80   203.0.113.27:33605
TIME-WAIT  0  0     192.0.2.145:80   203.0.113.47:50685
Purpose
There are two purposes for the TIME-WAIT state:

The most known one is to prevent delayed segments from one connection being accepted by a later connection relying on the same quadruplet (source address, source port, destination address, destination port). The sequence number also needs to be in a certain range to be accepted. This narrows a bit the problem but it still exists, especially on fast connections with large receive windows. RFC 1337 explains in details what happens when the TIME-WAIT state is deficient3. Here is an example of what could be avoided if the TIME-WAIT state wasn’t shortened:
Duplicate segments accepted in another connection

Due to a shortened TIME-WAIT state, a delayed TCP segment has been accepted in an unrelated connection.


The other purpose is to ensure the remote end has closed the connection. When the last ACK is lost, the remote end stays in the LAST-ACK state4. Without the TIME-WAIT state, a connection could be reopened while the remote end still thinks the previous connection is valid. When it receives a SYN segment (and the sequence number matches), it will answer with a RST as it is not expecting such a segment. The new connection will be aborted with an error:
Last ACK lost

If the remote end stays in LAST-ACK state because the last ACK was lost, opening a new connection with the same quadruplet will not work.


RFC 793 requires the TIME-WAIT state to last twice the time of the MSL. On Linux, this duration is not tunable and is defined ininclude/net/tcp.h as one minute:

#define TCP_TIMEWAIT_LEN (60*HZ) /* how long to wait to destroy TIME-WAIT
                                  * state, about 60 seconds     */
There have been propositions to turn this into a tunable value but it has been refused on the ground the TIME-WAIT state is a good thing.

Problems
Now, let’s see why this state can be annoying on a server handling a lot of connections. There are three aspects of the problem:

the slot taken in the connection table preventing new connections of the same kind,
the memory occupied by the socket structure in the kernel, and
the additional CPU usage.
The result of ss -tan state time-wait | wc -l is not a problem per se!

Connection table slot
A connection in the TIME-WAIT state is kept for one minute in the connection table. This means, another connection with the samequadruplet (source address, source port, destination address, destination port) cannot exist.

For a web server, the destination address and the destination port are likely to be constant. If your web server is behind a L7 load-balancer, the source address will also be constant. On Linux, the client port is by default allocated in a port range of about 30,000 ports (this can be changed by tuning net.ipv4.ip_local_port_range). This means that only 30,000 connections can be established between the web server and the load-balancer every minute, so about 500 connections per second.

If the TIME-WAIT sockets are on the client side, such a situation is easy to detect. The call to connect() will return EADDRNOTAVAIL and the application will log some error message about that. On the server side, this is more complex as there is no log and no counter to rely on. In doubt, you should just try to come with something sensible to list the number of used quadruplets:

$ ss -tan 'sport = :80' | awk '{print $(NF)" "$(NF-1)}' | \
>     sed 's/:[^ ]*//g' | sort | uniq -c
    696 10.24.2.30 10.33.1.64
   1881 10.24.2.30 10.33.1.65
   5314 10.24.2.30 10.33.1.66
   5293 10.24.2.30 10.33.1.67
   3387 10.24.2.30 10.33.1.68
   2663 10.24.2.30 10.33.1.69
   1129 10.24.2.30 10.33.1.70
  10536 10.24.2.30 10.33.1.73
The solution is more quadruplets5. This can be done in several ways (in the order of difficulty to setup):

use more client ports by setting net.ipv4.ip_local_port_range to a wider range,
use more server ports by asking the web server to listen to several additional ports (81, 82, 83, …),
use more client IP by configuring additional IP on the load balancer and use them in a round-robin fashion,
use more server IP by configuring additional IP on the web server6.
Of course, a last solution is to tweak net.ipv4.tcp_tw_reuse andnet.ipv4.tcp_tw_recycle. Don’t do that yet, we will cover those settings later.

Memory
With many connections to handle, leaving a socket open for one additional minute may cost your server some memory. For example, if you want to handle about 10,000 new connections per second, you will have about 600,000 sockets in the TIME-WAIT state. How much memory does it represent? Not that much!

First, from the application point of view, a TIME-WAIT socket does not consume any memory: the socket has been closed. In the kernel, aTIME-WAIT socket is present in three structures (for three different purposes):

A hash table of connections, named the “TCP established hash table” (despite containing connections in other states) is used to locate an existing connection, for example when receiving a new segment.

Each bucket of this hash table contains both a list of connections in the TIME-WAIT state and a list of regular active connections. The size of the hash table depends on the system memory and is printed at boot:

$ dmesg | grep "TCP established hash table"
[    0.169348] TCP established hash table entries: 65536 (order: 8, 1048576 bytes)
It is possible to override it by specifying the number of entries on the kernel command line with the thash_entries parameter.

Each element of the list of connections in the TIME-WAIT state is astruct tcp_timewait_sock, while the type for other states is struct tcp_sock7:

struct tcp_timewait_sock {
    struct inet_timewait_sock tw_sk;
    u32    tw_rcv_nxt;
    u32    tw_snd_nxt;
    u32    tw_rcv_wnd;
    u32    tw_ts_offset;
    u32    tw_ts_recent;
    long   tw_ts_recent_stamp;
};

struct inet_timewait_sock {
    struct sock_common  __tw_common;

    int                     tw_timeout;
    volatile unsigned char  tw_substate;
    unsigned char           tw_rcv_wscale;
    __be16 tw_sport;
    unsigned int tw_ipv6only     : 1,
                 tw_transparent  : 1,
                 tw_pad          : 6,
                 tw_tos          : 8,
                 tw_ipv6_offset  : 16;
    unsigned long            tw_ttd;
    struct inet_bind_bucket *tw_tb;
    struct hlist_node        tw_death_node;
};
A set of lists of connections, called the “death row”, is used to expire the connections in the TIME-WAIT state. They are ordered by how much time left before expiration.

It uses the same memory space as for the entries in the hash table of connections. This is the struct hlist_node tw_death_nodemember of struct inet_timewait_sock8.

A hash table of bound ports, holding the locally bound ports and the associated parameters, is used to determine if it is safe to listen to a given port or to find a free port in the case of dynamic bind. The size of this hash table is the same as the size of the hash table of connections:

$ dmesg | grep "TCP bind hash table"
[    0.169962] TCP bind hash table entries: 65536 (order: 8, 1048576 bytes)
Each element is a struct inet_bind_socket. There is one element for each locally bound port. A TIME-WAIT connection to a web server is locally bound to the port 80 and shares the same entry as its sibling TIME-WAIT connections. On the other hand, a connection to a remote service is locally bound to some random port and does not share its entry.

So, we are only concerned by the space occupied by struct tcp_timewait_sock and struct inet_bind_socket. There is one struct tcp_timewait_sock for each connection in the TIME-WAIT state, inbound or outbound. There is one dedicated struct inet_bind_socket for each outbound connection and none for an inbound connection.

A struct tcp_timewait_sock is only 168 bytes while a struct inet_bind_socket is 48 bytes:

$ sudo apt-get install linux-image-$(uname -r)-dbg
[...]
$ gdb /usr/lib/debug/boot/vmlinux-$(uname -r)
(gdb) print sizeof(struct tcp_timewait_sock)
 $1 = 168
(gdb) print sizeof(struct tcp_sock)
 $2 = 1776
(gdb) print sizeof(struct inet_bind_bucket)
 $3 = 48
So, if you have about 40,000 inbound connections in the TIME-WAITstate, it should eat less than 10MB of memory. If you have about 40,000 outbound connections in the TIME-WAIT state, you need to account for 2.5MB of additional memory. Let’s check that by looking at the output of slabtop. Here is the result on a server with about 50,000 connections in the TIME-WAIT state, 45,000 of which are outbound connections:

$ sudo slabtop -o | grep -E '(^  OBJS|tw_sock_TCP|tcp_bind_bucket)'
  OBJS ACTIVE  USE OBJ SIZE  SLABS OBJ/SLAB CACHE SIZE NAME                   
 50955  49725  97%    0.25K   3397       15     13588K tw_sock_TCP            
 44840  36556  81%    0.06K    760       59      3040K tcp_bind_bucket
There is nothing to change here: the memory used by TIME-WAITconnections is really small. If your server need to handle thousands of new connections per second, you need far more memory to be able to efficiently push data to clients. The overhead of TIME-WAIT connections is negligible.

CPU
On the CPU side, searching for a free local port can be a bit expensive. The work is done by the inet_csk_get_port() function which uses a lock and iterate on locally bound ports until a free port is found. A large number of entries in this hash table is usually not a problem if you have a lot of outbound connections in the TIME-WAIT state (like ephemeral connections to a memcached server): the connections usually share the same profile, the function will quickly find a free port as it iterates on them sequentially.

Other solutions
If you still think you have a problem with TIME-WAIT connections after reading the previous section, there are three additional solutions to solve them:

disable socket lingering,
net.ipv4.tcp_tw_reuse, and
net.ipv4.tcp_tw_recycle.
Socket lingering
When close() is called, any remaining data in the kernel buffers will be sent in the background and the socket will eventually transition to theTIME-WAIT state. The application can continue to work immediatly and assume that all data will eventually be safely delivered.

However, an application can choose to disable this behaviour, known assocket lingering. There are two flavors:

In the first one, any remaining data will be discarded and instead of closing the connection with the normal four-packet connection termination sequence, the connection will be closed with a RST(and therefore, the peer will detect an error) and will be immediatly destroyed. No TIME-WAIT state in this case.

With the second flavor, if there is any data still remaining in the socket send buffer, the process will sleep when calling close()until either all the data is sent and acknowledged by the peer or the configured linger timer expires. It is possible for a process to not sleep by setting the socket as non-blocking. In this case, the same process happens in the background. It permits the remaining data to be sent during a configured timeout but if the data is succesfully sent, the normal close sequence is run and you get aTIME-WAIT state. And on the other case, you’ll get the connection close with a RST and the remaining data is discarded.

In both cases, disabling socket lingering is not a one-size-fits-all solution. It may be used by some applications like HAProxy or Nginx when it is safe to use from the upper protocol point of view. There are good reasons to not disable it unconditionnaly.

net.ipv4.tcp_tw_reuse
The TIME-WAIT state prevents delayed segments to be accepted in an unrelated connection. However, on certain conditions, it is possible to assume a new connection’s segment cannot be misinterpreted with an old connection’s segment.

RFC 1323 presents a set of TCP extensions to improve performance over high-bandwidth paths. Among other things, it defines a new TCP option carrying two four-byte timestamp fields. The first one is the current value of the timestamp clock of the TCP sending the option while the second one is the most recent timestamp received from the remote host.

By enabling net.ipv4.tcp_tw_reuse, Linux will reuse an existing connection in the TIME-WAIT state for a new outgoing connection if the new timestamp is strictly bigger than the most recent timestamp recorded for the previous connection: an outgoing connection in theTIME-WAIT state can be reused after just one second.

How is it safe? The first purpose of the TIME-WAIT state was to avoid duplicate segments to be accepted in an unrelated connection. Thanks to the use of timestamps, such a duplicate segments will come with an outdated timestamp and therefore be discarded.

The second purpose was to ensure the remote end is not in the LAST-ACK state because of the lost of the last ACK. The remote end will retransmit the FIN segment until:

it gives up (and tear down the connection), or
it receives the ACK it is waiting (and tear down the connection), or
it receives a RST (and tear down the connection).
If the FIN segments are received in a timely manner, the local end socket will still be in the TIME-WAIT state and the expected ACKsegments will be sent.

Once a new connection replaces the TIME-WAIT entry, the SYN segment of the new connection is ignored (thanks to the timestamps) and won’t be answered by a RST but only by a retransmission of the FIN segment. The FIN segment will then be answered with a RST (because the local connection is in the SYN-SENT state) which will allow the transition out of the LAST-ACK state. The initial SYN segment will eventually be resent (after one second) because there was no answer and the connection will be established without apparent error, except a slight delay:

Last ACK lost and timewait reuse

If the remote end stays in LAST-ACK state because the last ACK was lost, the remote connection will be reset when the local end transition to the SYN-SENT state.


It should be noted that when a connection is reused, the TWRecycledcounter is increased (despite its name).

net.ipv4.tcp_tw_recycle
This mechanism also relies on the timestamp option but affects both incoming and outgoing connections which is handy when the server usually closes the connection first9.

The TIME-WAIT state is scheduled to expire sooner: it will be removed after the retransmission timeout (RTO) interval which is computed from the RTT and its variance. You can spot the appropriate values for a living connection with the ss command:

$ ss --info  sport = :2112 dport = :4057
State      Recv-Q Send-Q    Local Address:Port        Peer Address:Port   
ESTAB      0      1831936   10.47.0.113:2112          10.65.1.42:4057    
         cubic wscale:7,7 rto:564 rtt:352.5/4 ato:40 cwnd:386 ssthresh:200 send 4.5Mbps rcv_space:5792
To keep the same guarantees the TIME-WAIT state was providing, while reducing the expiration timer, when a connection enters the TIME-WAITstate, the latest timestamp is remembered in a dedicated structure containing various metrics for previous known destinations. Then, Linux will drop any segment from the remote host whose timestamp is not strictly bigger than the latest recorded timestamp, unless the TIME-WAITstate would have expired:

if (tmp_opt.saw_tstamp &&
    tcp_death_row.sysctl_tw_recycle &&
    (dst = inet_csk_route_req(sk, &fl4, req, want_cookie)) != NULL &&
    fl4.daddr == saddr &&
    (peer = rt_get_peer((struct rtable *)dst, fl4.daddr)) != NULL) {
        inet_peer_refcheck(peer);
        if ((u32)get_seconds() - peer->tcp_ts_stamp < TCP_PAWS_MSL &&
            (s32)(peer->tcp_ts - req->ts_recent) >
                                        TCP_PAWS_WINDOW) {
                NET_INC_STATS_BH(sock_net(sk), LINUX_MIB_PAWSPASSIVEREJECTED);
                goto drop_and_release;
        }
}
When the remote host is in fact a NAT device, the condition on timestamps will forbid allof the hosts except one behind the NAT device to connect during one minute because they do not share the same timestamp clock. In doubt, this is far better to disable this option since it leads to difficult to detect and difficult to diagnose problems.

The LAST-ACK state is handled in the exact same way as fornet.ipv4.tcp_tw_recycle.

Summary
The universal solution is to increase the number of possible quadruplets by using, for example, more server ports. This will allow you to not exhaust the possible connections with TIME-WAIT entries.

On the server side, do not enable net.ipv4.tcp_tw_recycle unless you are pretty sure you will never have NAT devices in the mix. Enablingnet.ipv4.tcp_tw_reuse is useless for incoming connections.

On the client side, enabling net.ipv4.tcp_tw_reuse is another almost-safe solution. Enabling net.ipv4.tcp_tw_recycle in addition tonet.ipv4.tcp_tw_reuse is mostly useless.

Moreover, when designing protocols, don’t let clients close first. Clients won’t have to deal with the TIME-WAIT state pushing the responsability to servers which are better suited to handle this.

And a final quote by W. Richard Stevens, in Unix Network Programming:

The TIME_WAIT state is our friend and is there to help us (i.e., to let old duplicate segments expire in the network). Instead of trying to avoid the state, we should understand it.

Notably, fiddling with net.netfilter.nf_conntrack_tcp_timeout_time_wait won’t change anything on how the TCP stack will handle the TIME-WAIT state. ↩

This diagram is licensed under the LaTeX Project Public License 1.3. The original file is available on this page. ↩

The first work-around proposed in RFC 1337 is to ignore RST segments in the TIME-WAIT state. This behaviour is controlled by net.ipv4.rfc1337 which is not enabled by default on Linux because this is not a complete solution to the problem described in the RFC. ↩

While in the LAST-ACK state, a connection will retransmit the last FIN segment until it gets the expected ACK segment. Therfore, it is unlikely we stay long in this state. ↩

On the client side, older kernels also have to find a free local tuple (source address and source port) for each outgoing connection. Increasing the number of server ports or IPwon’t help in this case. Linux 3.2 is recent enough to be able to share the same local tuple for different destinations. Thanks to Willy Tarreau for his insight on this aspect. ↩

This last solution may seem a bit dumb since you could just use more ports but some servers are not able to be configured this way. The before last solution can also be quite cumbersome to setup, depending on the load-balancing software, but uses less IPthan the last solution. ↩

The use of a dedicated memory structure for sockets in the TIME-WAIT is here since Linux 2.6.14. The struct sock_common structure is a bit more verbose and I won’t copy it here. ↩

Since Linux 4.1, the way TIME-WAIT sockets are tracked has been modified to increase performance and parallelism. The death row is now just a hash table. ↩

When the server closes the connection first, it gets the TIME-WAIT state while the client will consider the corresponding quadruplet free and hence may reuse it for a new connection. ↩


建议将这两个内核参数设置为0
net.ipv4.tcp_tw_recycle = 0
net.ipv4.tcp_tw_reuse = 0

参考文章：
TCP 的那些事儿（上）
http://coolshell.cn/articles/11564.html

TCP 的那些事儿（下）
http://coolshell.cn/articles/11609.html


Nagle算法默认是打开的，所以，对于一些需要小包场景的程序——比如像telnet或ssh这样的交互性比较强的程序，你需要关闭这个算法。你可以在Socket设置TCP_NODELAY选项来关闭这个算法（关闭Nagle算法没有全局参数，需要根据每个应用自己的特点来关闭）

setsockopt(sock_fd, IPPROTO_TCP, TCP_NODELAY, (char *)&value,sizeof(int));
另外，网上有些文章说TCP_CORK的socket option是也关闭Nagle算法，这个还不够准确。TCP_CORK是禁止小包发送，而Nagle算法没有禁止小包发送，只是禁止了大量的小包发送。最好不要两个选项都设置。 老实说，我觉得Nagle算法其实只加了个延时，没有别的什么，我觉得最好还是把他关闭，然后由自己的应用层来控制数据，我个觉得不应该什么事都去依赖内核算法

xshell



http://mp.weixin.qq.com/s?__biz=MjM5NTU2MTQwNA==&mid=2650652755&idx=1&sn=be3c509602233fa2804121ff97ed934e&scene=0#wechat_redirect

这个问题在网上已经有很多人讨论过了，再谈这个问题，只是根据我处理过的相关业务来谈谈我的看法。至于什么是TIMEWAIT，我想，并不需要多说。

TIMEWAIT状态本身和应用层的客户端或者服务器是没有关系的。仅仅是主动关闭的一方，在使用FIN|ACK|FIN|ACK四分组正常关闭TCP连接的时候会出现这个TIMEWAIT。服务器在处理客户端请求的时候，如果你的程序设计为服务器主动关闭，那么你才有可能需要关注这个TIMEWAIT状态过多的问题。如果你的服务器设计为被动关闭，那么你首先要关注的是CLOSE_WAIT。


原则

TIMEWAIT并不是多余的。在TCP协议被创造，经历了大量的实际场景实践之后，TIMEWAIT出现了，因为TCP主动关闭连接的一方需要TIMEWAIT状态，它是我们的朋友。这是《UNIX网络编程》的作者----Steven对TIMEWAIT的态度。


TIMEWAIT是友好的

TCP要保证在所有可能的情况下使得所有的数据都能够被正确送达。当你关闭一个socket时，主动关闭一端的socket将进入TIME_WAIT状态，而被动关闭一方则转入CLOSED状态，这的确能够保证所有的数据都被传输。当一个socket关闭的时候，是通过两端四次握手完成的，当一端调用close()时，就说明本端没有数据要发送了。这好似看来在握手完成以后，socket就都可以处于初始的CLOSED状态了，其实不然。原因是这样安排状态有两个问题， 首先，我们没有任何机制保证最后的一个ACK能够正常传输，第二，网络上仍然有可能有残余的数据包(wandering duplicates)，我们也必须能够正常处理。

TIMEWAIT就是为了解决这两个问题而生的。

1.假设最后一个ACK丢失了，被动关闭一方会重发它的FIN。主动关闭一方必须维持一个有效状态信息（TIMEWAIT状态下维持），以便能够重发ACK。如果主动关闭的socket不维持这种状态而进入CLOSED状态，那么主动关闭的socket在处于CLOSED状态时，接收到FIN后将会响应一个RST。被动关闭一方接收到RST后会认为出错了。如果TCP协议想要正常完成必要的操作而终止双方的数据流传输，就必须完全正确的传输四次握手的四个节，不能有任何的丢失。这就是为什么socket在关闭后，仍然处于TIME_WAIT状态的第一个原因，因为他要等待以便重发ACK。

2.假设目前连接的通信双方都已经调用了close()，双方同时进入CLOSED的终结状态，而没有走TIME_WAIT状态。会出现如下问题，现在有一个新的连接被建立起来，使用的IP地址与端口与先前的完全相同，后建立的连接是原先连接的一个完全复用。还假定原先的连接中有数据报残存于网络之中，这样新的连接收到的数据报中有可能是先前连接的数据报。为了防止这一点，TCP不允许新连接复用TIME_WAIT状态下的socket。处于TIME_WAIT状态的socket在等待两倍的MSL时间以后（之所以是两倍的MSL，是由于MSL是一个数据报在网络中单向发出到认定丢失的时间，一个数据报有可能在发送途中或是其响应过程中成为残余数据报，确认一个数据报及其响应的丢弃的需要两倍的MSL），将会转变为CLOSED状态。这就意味着，一个成功建立的连接，必然使得先前网络中残余的数据报都丢失了。


大量TIMEWAIT在某些场景中导致令人头疼的业务问题

大量TIMEWAIT出现，并且需要解决的场景。

在高并发短连接的TCP服务器上，当服务器处理完请求后立刻按照主动正常关闭连接。。。这个场景下，会出现大量socket处于TIMEWAIT状态。如果客户端的并发量持续很高，此时部分客户端就会显示连接不上。
我来解释下这个场景。主动正常关闭TCP连接，都会出现TIMEWAIT。为什么我们要关注这个高并发短连接呢？有两个方面需要注意：

1.高并发可以让服务器在短时间范围内同时占用大量端口，而端口有个0~65535的范围，并不是很多，刨除系统和其他服务要用的，剩下的就更少了。

2.在这个场景中，短连接表示“业务处理+传输数据的时间 远远小于 TIMEWAIT超时的时间”的连接。这里有个相对长短的概念，比如，取一个web页面，1秒钟的http短连接处理完业务，在关闭连接之后，这个业务用过的端口会停留在TIMEWAIT状态几分钟，而这几分钟，其他HTTP请求来临的时候是无法占用此端口的。单用这个业务计算服务器的利用率会发现，服务器干正经事的时间和端口（资源）被挂着无法被使用的时间的比例是 1：几百，服务器资源严重浪费。（说个题外话，从这个意义出发来考虑服务器性能调优的话，长连接业务的服务就不需要考虑TIMEWAIT状态。同时，假如你对服务器业务场景非常熟悉，你会发现，在实际业务场景中，一般长连接对应的业务的并发量并不会很高）

综合这两个方面，持续的到达一定量的高并发短连接，会使服务器因端口资源不足而拒绝为一部分客户服务。同时，这些端口都是服务器临时分配，无法用SO_REUSEADDR选项解决这个问题。


一对矛盾

TIMEWAIT既友好，又令人头疼。

但是我们还是要抱着一个友好的态度来看待它，因为它尽它的能力保证了服务器的健壮性。


可行而且必须存在，但是不符合原则的解决方式

1.linux没有在sysctl或者proc文件系统暴露修改这个TIMEWAIT超时时间的接口，可以修改内核协议栈代码中关于这个TIMEWAIT的超时时间参数，重编内核，让它缩短超时时间，加快回收；

2.利用SO_LINGER选项的强制关闭方式，发RST而不是FIN，来越过TIMEWAIT状态，直接进入CLOSED状态。详见我的博文《TCP之选项SO_LINGER》。


我如何看待这个问题

为什么说上述两种解决方式我觉得可行，但是不符合原则？
我首先认为，我要依靠TIMEWAIT状态来保证我的服务器程序健壮，网络上发生的乱七八糟的问题太多了，我先要服务功能正常。
