## Android多进程

### 多进程实例

```java
<activity
    android:name=".MainActivity"
    android:label="@string/app_name" >
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity
    android:name="com.example.ipc.SecondActivity"
    android:process=":remote" />

<activity
    android:name="com.example.ipc.ThirdActivity"
    android:process=".remote" />            
```

对MainActivity不进行指定，则默认为当前进程。
对SecondActivity指定属性android:process=":remote"。
对ThirdActivity指定属性android:process=".remote"。

启动了三个进程，分别是：

`com.example.ipc`：默认的程序进程。和包名相同。
`com.example.ipc:remote`：SecondActivity所在的进程。
`.remote`：ThirdActivity所在的进程。

### :remote 和 .remote 区别

如果进程名以`:`开始，表示是要在当前的进程名前附加上当前的包名，表示该进程是本应用的**私有进程**，其他应用不可以和其跑在同一个进程。

如果进程名不以`:`开始，表示不需附加包名信息，是一个完全的命名。同时该进程是**全局进程**，其他应用可以通过ShareUID和其跑在同一个进程中。

`:xxx.xxx` 私有进程
`com.xxx.xxx` 全局进程

### 多进程环境下的单例，全局对象。

由于上面的方式，新进程和当前进程其实是**共用一个项目代码**的。所以，准确的说，是一份代码，两个应用。所以，一个单例（或全局对象），对于进程A和进程B两者而言，各是一个独立的对象。

Android的这种多进程方式，还有其它的问题：

1.Application会多次创建。一个进程就创建一次，所以注意重入！尤其是从一个大项目中抽一个小进程，小进程应该减少资源的不必要创建，所以在Application就需要差分初始化。

2.静态成员和单例模式无法在多进程环境下，不能跨进程访问。如果两个进程都用到一个单例，需要注意两个进程的数据是完全独立的。

3.SharedPreference／IO／File操作的可靠性下降。因为用的是同一块代码，意味着会有多个进程重入同一段代码，所以会出现并发访问SharedPreference／IO／同一个File文件目录等等。

4.系统资源需要进行区分。同样因为是同一块代码，所以，对于端口／系统服务／系统资源／系统锁／系统多媒体／硬件等等的访问，会出现多进程并发访问的可能。所以这部分要进行区分，不能区分的，要进行进程同步。

5.上面，进程同步的方式，可以选择：端口／文件／等作为进程间的同步锁。
