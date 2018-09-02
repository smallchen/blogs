## java try catch finally

```
try {

} catch (Exeption e) {

} finally {

}
```

正常的`try-catch-finally`。


```
try {
    return A
} catch (Exeption e) {
    return B
} finally {
    ...
}
```

正常：先执行returnA，然后执行finally，最后才执行returnA之后的语句（即调用了这个模块的地方）。
异常：先执行try块，有异常执行returnB，然后执行finally，最后才执行returnB之后的语句（即调用了这个模块的地方）。

所以，总结：`finally`始终在当前块先执行，即使有返回值，返回值只是暂存，等待finally执行完后，才执行返回值所在的调用语句。这个原则始终适用。

Yes, finally will be called after the execution of the try or catch code blocks.
`finally`会在`try`或者`catch`之后调用。

The only times finally won't be called are:
仅当以下情况，`finally`不会被调用：

* If you invoke `System.exit()`;
* If the OS forcibly terminates the JVM process; e.g.` "kill -9 "` on UNIX.
* If the JVM reaches an infinite loop (or some other non-interruptable, non-terminating statement) in the try or catch block;
* If the JVM crashes first;
* If the host system dies; e.g. power failure, hardware error, OS panic, etcetera.
* If finally block is going to be executed by daemon thread and all other non daemon threads exit before finally is called.

* 调用了`System.exit()`
* 命令行中使用`kill -9`kill掉进程
* JVM虚拟机在`try`或`catch`代码块中进入了无限循环、或者不可中断的、不能结束的语句。
* JVM虚拟机先挂掉
* JVM虚拟机的主系统挂掉。比如，关机／硬件错误／操作系统死机等等。
* 如果`finally`在守护线程中执行，则当其它非守护线程退出后，finally才在最后执行（不懂）。


```
try {

} finally {

}
```

如上，不进行`catch`会是怎样？抛异常和`finally`如何执行？

`finally`始终在当前代码块返回前先执行，这个原则仍然适用。也即是说，先执行`try`，然后不管是否有异常都先暂存起来，然后执行`finally`，最后如果有暂存的异常，才往上抛。

如果看过Android-Looper的源码，你会发现，`Looper.loop()`里面就是有一个`try - finally`。

```java
// Looper.loop()
try {
    msg.target.dispatchMessage(msg);
    end = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
} finally {
    if (traceTag != 0) {
        Trace.traceEnd(traceTag);
    }
}
```

意思是，无论是否有异常，都先打印当前的Trace，然后才执行异常部分。

```
try {
    System.out.println('A');
    try {
        System.out.println('B');
        throw new Exception("threw exception in B");
    } finally {
        System.out.println('X');
    }
} catch (Exception e) {
    System.out.println('Y');
} finally {
    System.out.println('Z');
}
```

如上，不管嵌套了多少层`try-catch`，`finally`始终在所属的代码块返回前执行，这个原则仍旧适用。

所以，上面输出是`A B X Y Z`。

https://stackoverflow.com/questions/65035/does-finally-always-execute-in-java
https://stackoverflow.com/questions/4559661/java-try-catch-finally-blocks-without-catch
