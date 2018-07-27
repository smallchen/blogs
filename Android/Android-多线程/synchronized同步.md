## synchronized

1. synchronized(this)
2. synchronized(XXX.class)
3. synchronized方法：静态方法／实例方法
4. synchronized代码块


### synchronized(this)对象锁

对访问同一个对象实例才生效的锁。如果是多个对象，则不同的对象有自己的锁。

### synchronized(XXX.class)类锁

对访问同一个类就生效的锁。常用于单实例的创建。表示对类独占。

### synchronized方法，方法锁

对于静态方法，则任何访问该方法都需要独占锁。
对于实例方法，则对除了同一个方法外，还需要是同一个实例，才需要独占锁。

### synchronized代码块

其实就是对象锁。

```java
synchronized (this) {
    // 同步代码块
}

synchronized (obj) {
    // 同步代码块
}
```

附上，最小的对象。
`byte[] lock = new byte[0]`

JVM虚拟机中，new byte[0] 确实比 new Object() 少 4 条字节码操作。
但Android虚拟机DVM是进行了优化的。对比一下发现，指令都是 4 条。所以，在 Android 编程中，使用 new Object() 或者 new byte[0] 作为对象锁差别不大。
没有优化，new byte[0]要比new Object()优。有优化后，两者差不多。
