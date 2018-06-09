## Android Messenger

类似与Message的消息通信。

### Messenger实例

#### 服务端

1.创建一个服务端。还是以Service为例。

```java
public class MessengerService extends Service {
}
```

2.创建一个Messenger对象作为消息接收端。

```java
private Messenger mMessenger;
mMessenger = new Messenger(mHandler);
```

3.Messenger需要依附一个Handler。表示消息处理的线程。这个看需求。可以让其在主线程中处理，也可以创建一个独立的线程。这里使用独立的后台线程来处理消息队列。

```java
private HandlerThread mHandlerThread;
private Handler mHandler;

private void onCreate() {
	initBackgroundThread();
    mMessenger = new Messenger(mHandler);
}

private void initBackgroundThread() {
	mHandlerThread = new HandlerThread("messenger-thread",  Process.THREAD_PRIORITY_BACKGROUND);
	mHandlerThread.start();
	mHandler = new MessengerHandler(mHandlerThread.getLooper());
}

private void destroyBackgroundThread() {
	mHandler.removeCallbacksAndMessages(null);
	mHandlerThread.quitSafely();
}

private class MessengerHandler extends Handler {
	public MessengerHandler(Looper looper) {
		super(looper);
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.what == OPEN_ACTION) {
			// TODO
		} else if (msg.what == CLOSE_ACTION) {
			// TODO
		}
	}
}
```

4.Messenger准备完毕，通过`onBind()`返回给客户端。

```java
@Override
public IBinder onBind(Intent intent) {
	return mMessenger.getBinder();
}
```

#### 客户端

1.和AIDL一样，使用Intent绑定服务

```java
private static final String MESSENGER_PACKAGE_NAME = "com.jokin.demo.aidl.server";
private static final String MESSENGER_ACTION_SERVICE = MESSENGER_PACKAGE_NAME+".MessengerService";

Intent intentService = new Intent();
intentService.setComponent(new ComponentName(MESSENGER_PACKAGE_NAME, MESSENGER_ACTION_SERVICE));
bindService(intentService, mMessengerServiceConnection, BIND_AUTO_CREATE);
```

2.同样是在`ServiceConnection`回调中通过Binder来得到服务端的Messenger。(**区别是，这里是通过new来创建一个Messenger对象，而不是get出来或者转化出来。**)

```java
private Messenger mServerMessenger;
private ServiceConnection mMessengerServiceConnection = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mServerMessenger = new Messenger(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServerMessenger = null;
	}
};
```

3.可以使用这个`Messenger`来进行通信了。

```java
public static final int CLOSE_ACTION = 1001;
public static final String KEY_OF_TIP = "key.tip";

findViewById(R.id.closeActionMessenger).setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View v) {
		try {
			Message message = Message.obtain();
			message.what = CLOSE_ACTION;
			Bundle bundle = new Bundle();
			bundle.putString(KEY_OF_TIP, "Messenger要close拉！");
			message.setData(bundle);
			message.replyTo = mClientMessenger;

			mServerMessenger.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
});
```

**可以注意到，上面的Messenger只是单向通信，由客户端发消息到服务端，那么服务端要回消息给客户端怎么办？**

不需要在客户端也创建一个Server。只需要在客户端的`Message`消息实体中，通过`replyTo`带上客户端自己的`Messenger`即可。

```java
Message message = Message.obtain();
message.what = CLOSE_ACTION;
Bundle bundle = new Bundle();
bundle.putString(KEY_OF_TIP, "Messenger要close拉！");
message.setData(bundle);
message.replyTo = mClientMessenger;

/**
 * 注意内存泄漏，这里不修复了，参考Server。
 */
Messenger mClientMessenger = new Messenger(new Handler() {
	@Override
	public void handleMessage(final Message msg) {
		switch (msg.what) {
			case OPEN_RESULT:
				Log.e(TAG, "open with result:"+msg.toString());
				break;
			case CLOSE_RESULT:
				Log.e(TAG, "close with result:"+msg.toString());
				break;
			default:
				break;
		}
	}
});
```

**服务端得到`Message`消息后，可以通过`replyTo`拿到客户端的`Messenger`对象，从而进行应答。**

```java
@Override
public void handleMessage(Message msg) {
	if (msg.what == OPEN_ACTION) {
	} else if (msg.what == CLOSE_ACTION) {
		Log.e(TAG, String.format("close with tip=%s", msg.getData().getString(KEY_OF_TIP)));

		// 应答
		Message message = Message.obtain();
		message.what = CLOSE_RESULT;
		try {
			msg.replyTo.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
```

### 总结

1. 使用Messenger，客户端和服务端需要各有一份消息KEY。
2. 客户端连上服务端后，通过消息的`replyTo`带上自己的`Messenger`，服务端收到消息后，通过消息的`replyTo`访问客户端的`Messenger`进行应答。
3. `Messenger`线程问题，主要看绑定的`Handler`在哪个线程中。
