## AIDL 入门

### 概述

Android Interface Definition Language
Android接口定义语言

* 文件名后缀：必须为`.aidl`
* 基本类型不需要导入，非基本类型，即使在同一个目录下，也需要进行导入。
* List／Map等集合元素，需要满足序列化。
* 定向tag：in，out，inout。
    * 基本类型默认只能是in，因为只能作为值，不能作为引用被修改。
* 两种aidl类型：一种定义数据，一种定义接口。

### 理解in，out，inout
*  in 表示数据只能由客户端流向服务端
* out 表示数据只能由服务端流向客户端
* inout 则表示数据可在服务端与客户端之间双向流通

in 表现为服务端将会接收到一个那个对象的完整数据，但是客户端的那个对象不会因为服务端对传参的修改而发生变动。（简而言之，服务端只能读，不能写）

out 表现为服务端将会接收到那个对象的的空对象，但是在服务端对接收到的空对象有任何修改之后客户端将会同步变动。（简而言之，服务端只能写，不能读）

inout 表现为，服务端将会接收到客户端传来对象的完整信息，并且客户端将会同步服务端对该对象的任何变动。（简而言之，服务端可读可写）

由于java中的基本类型，本身只能作为值，不可以作为引用，所以本身不支持被修改。所以，即使在aidl中，也不能被服务端修改。所以，基本类型，只能是in，不能作为任何out。

而对于引用类型。in表示，服务端拿到的是数据的备份，修改不会影响客户端的对象。out表示，服务端只能拿到一个空对象，修改空对象会影响到客户端的对象。inout表示，服务端拿到的类似于客户端的引用，可以读，也可以修改。

个人猜测，定向tag，主要用于区分数据流是否需要序列化问题。in表示客户端只序列化，不进行反序列化。out表示客户端不进行序列化，只进行反序列化。inout表示客户端和服务端都需要进行序列化和反序列化。

### 理解oneway

`oneway void quit();`

oneway表示，操作调用后就立即返回，不等待服务器的执行结果。默认的接口，在客户端是同步的。添加`oneway`关键字，表示接口在客户端的调用为异步。

### 理解aidl

aidl分为两类，一是数据定义，一是接口定义。

数据定义

```java
// IAction.aidl
package com.jokin.demo.sdk;
parcelable IAction;
```

接口定义

```java
// IActionListener.aidl
package com.jokin.demo.sdk;

// 自定义类型，必须显式 import
import com.jokin.demo.sdk.IAction;

interface IActionListener {
    // 基本类型，不需 import，默认是 in
    void onDone(String action);
}
```

接口也可以作为另一个接口的参数。参数中的接口，如果是匿名接口，匿名接口的返回值，也会被原封不动的传递过去！！非常实用。

```java
// Isdk.aidl
package com.jokin.demo.sdk;

import com.jokin.demo.sdk.IAction;
import com.jokin.demo.sdk.IActionListener;

interface Isdk {
    boolean doAction(String key, in IAction action);
    void addActionListener(String key, IActionListener listener);
    void removeActionListener(String key);
    boolean isTrue(String key, String state);
    oneway void quit();
}
```

### 实例，第一个aidl应用

#### AIDL定义

1.**确定AIDL包名。**(比如`com.jokin.demo.sdk`)

2.在`src/main/java`目录下，创建包路径。称为`java包路径`。

3.在`src/main/aidl`目录下，创建包路径。称为`aidl包路径`。

4.可见，上述`java`和`aidl`包路径是一致的。据说新的IDE，aidl不需要和java目录路径相同。

5.**开始创建aidl文件。**

6.在`aidl`包路径下，创建`IAction.aidl`文件，如下。

```java
package com.jokin.demo.sdk;
parcelable IAction;
```
> 这一步，可以在java对应包路径下，鼠标 - 右键 - New - AIDL - 来创建。会参照当前java目录下的包路径，创建对应的aidl包路径。

7.aidl文件中定义的`IAction`对象，需要在`java`目录下，相同包路径下补上。

```java
public class IAction implements Parcelable {
    // 序列化。。。
    // 反序列化。。。
}
```

8.**所以，aidl中自定义的数据类型，需要在java对应package下，有对应的实体实现。（主要是序列化和反序列化）**

9.定义接口`IActionListener.aidl`和`Isdk.aidl`。

```java
package com.jokin.demo.aidl.sdk;
import com.jokin.demo.aidl.sdk.IAction;

interface IActionListener {
    void onDone(String action);
}
```

```java
package com.jokin.demo.aidl.sdk;
import com.jokin.demo.aidl.sdk.IAction;
import com.jokin.demo.aidl.sdk.IActionListener;

interface Isdk {
    boolean doAction(String key, in IAction action);
    void addActionListener(String key, IActionListener listener);
    void removeActionListener(String key);
    oneway void quit();
}
```

10.**接口不需要在对应的java目录下有实现。**

11.`make project`make一下（或同步以下），就能在`build/generated/source/aidl/`下生成对应的`IActionListener`和`Isdk`。

12.**接口才会在make后生成实体类，数据类型需要自行在java目录下预先定义好。**

#### Server端
AIDL文件的创建已经完成，下面可以在Service服务中使用。

1.创建一个暴露在外的Service服务。（比如`ActionService exported="true"`)
> 使用IDE右键创建，可以免去配置`AndroidManifest.xml`，省掉麻烦。

```java
public class ActionService extends Service {
}
```

2.把上面的aidl创建在服务端操作一遍，即服务端要包含aidl定义。（可以将aidl定义放在`module`里，这样，服务端直接依赖`module`既可以完成接口定义）。

3.使用上面aidl文件生成的`Isdk.Stub`创建一个对象。这个`Isdk.Stub`其实是一个`Binder`对象。实现了`IBinder`接口。

```java
// 1. 创建AIDL接口Binder
private final Isdk.Stub mBinder = new Isdk.Stub() {
    @Override
    public boolean doAction(String key, IAction action) throws RemoteException {
        // Server端的处理。注意线程问题。
    }

    @Override
    public void addActionListener(String key, IActionListener listener) throws RemoteException {
        // Server端的处理。注意线程问题。
    }

    @Override
    public void removeActionListener(String key) throws RemoteException {
        // Server端的处理。注意线程问题。
    }
}
```

4.在`onBind()`时，将这个`Isdk.Stub`Binder对象，返回给客户端即可。(每次new一个对象，和每次返回同一个对象有没有区别呢？AndroidDemo里，是使用同一个对象。)

```java
@Override
public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    return mBinder;
}
```

#### Client端

1.任意一个地方。同一项目，或不同项目都可以。先按照`aidl定义`引入aidl接口。（可以参考Server，独立出的`module`，客户端也引用这个`module`即可完成客户端的接口定义）

> 可见，AIDL定义，是两份，一份在客户端，一份在服务端。

2.发起一个Intent，绑定服务端Service。

```java
private static final String PACKAGE_NAME = "com.jokin.demo.aidl.server";
private static final String ACTION_SERVICE = PACKAGE_NAME+".ActionService";

Intent intentService = new Intent();
intentService.setComponent(new ComponentName(PACKAGE_NAME, ACTION_SERVICE));
bindService(intentService, mServiceConnection, BIND_AUTO_CREATE);
```

3.绑定服务时，服务连接有一个`ServiceConnection`回调。通过这个回调，我们拿到服务端返回的Binder对象。这个Binder对象，其实就是上面Server端返回的`Isdk.Stub`对象。

通过`Isdk.Stub.asInterface()`静态方法，可以将一个`Isdk.Stub`Binder对象转化为`Isdk`接口简化调用。

```java
private Isdk mIsdk;
private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
        mIsdk = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
        // 这里对接Server端的 onBind() 返回值。
        mIsdk = Isdk.Stub.asInterface(service);
    }
};
```

4.此时，我们可以使用`Isdk`接口来调用服务端的接口。实现跨进程调用。

```java
findViewById(R.id.openAction).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (mIsdk == null) {
            return;
        }
        try {
            mIsdk.doAction("open", new IAction(new OpenAction(Color.BLACK, 10)));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
});
```

5.使用完后，调用`Context.unbindService()`解除连接即可。


### 总结

1. AIDL定义需要有两份。一份在服务端，一份在客户端。这个影响**兼容性问题**。

2. AIDL自定义数据类型，使用`parcelable`声明，且需要声明包名，且独立为一个aidl文件。同时，需要对应的实现了`parcelable`的java实体对象。

3. AIDL定义接口，使用`interface`，和普通接口类似，只是多了`in`,`out`,`inout`定向TAG。

4. 服务端在`onBind()`返回Binder对象。客户端在`ServiceConnection::onServiceConnected`中得到服务端返回的那个Binder对象。（这个Binder对象，可以是继承了Binder的本地对象，也可以是XXX.Stub由aidl生成的Binder对象。见另一篇LocalBinder）

5. `ServiceConnection`回调是在主线程中回调（UI线程）。所以不需要做同步。

6. 可以用于进程内，也可以用于跨进程。进程内还可以使用另一种，Local Binder。（见另一篇）

7. 服务端的异常，都不会传递给客户端。

8. AIDL接口调用默认是同步的！需要异步，使用`oneway`关键字。

### AIDL定义和JAVA实体在同一个目录的配置。

```java
src/main/java/com/jokin/demo/aidl/sdk
    - IAction.java
    - IAction.aidl
    - Isdk.aidl
```
以上目录结构是支持的，前提是，在`gradle`中配置`sourceSets`。

```java
android {
    sourceSets {
        main {
            aidl.srcDirs = ['src/main/java']
        }
    }
}
```

这样子，就可以集中管理AIDL相关的资源文件。

### 远程Service

设置`android:process`即可将Service运行于另一进程空间。

```xml
<service
    android:name=".ActionService"
    android:enabled="true"
    android:exported="true"
    android:process="com.jokin.demo.aidl.remote"
    >
```

### 兼容性问题

AIDL文件需要放置在客户端，所以，在AIDL接口首次发布后，对其进行的任何更改都可能会影响现有客户端。尤其是已经上线的客户端。

所以，AIDL文件必须保持向后兼容性（旧兼容），以避免中断其他应用对您的服务的使用。

也就是说，修改AIDL接口时，必须保留对原始接口的支持，只能增加接口，而不能删除或修改接口。（除非你确定没有线上客户端，或清楚确定不再向后兼容）

测试过，增加接口，旧的AIDL文件生成的接口还是可以安全进行访问。


### 脚本产生AIDL接口文件

有时候，你可能需要，通过脚本而不是IDE来生成aidl接口文件（编译后java文件）。

`gradle assembleDebug`（或 `gradle assembleRelease`）编译项目，代码就能够链接到生成的类。
