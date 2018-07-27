## Android广播

1. 静态广播
在AndroidManifest.xml中定义，不需程序启动即可接收，可用作自动启动程序

Intent.ACTION_BOOT_COMPLETED //系统启动完成
Intent.ACTION_MEDIA_MOUNTED //SD卡挂载
Intent.ACTION_MEDIA_UNMOUNTED //SD卡卸载
Intent.ACTION_USER_PRESENT//解除锁屏
ConnectivityManager.CONNECTIVITY_ACTION//网络状态变化

```java
        <receiver android:name="com.StaticBroadcastReceiver">
            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
                <action android:name="android.hardware.usb.action.USB_DEVICE_DETACHED" />
                <action android:name="com.action.AP_IP_CHANGED" />
                <action android:name="com.action.AP_CONFIG_CHANGED" />
                <action android:name="com.action.AP_ONLY_TO_2_4_G" />
                <action android:name="com.ACTION_UWSD_SAVE_INFO" />
            </intent-filter>
        </receiver>
```

```java
<receiver android:name=".StaticBroadcastReceiver">  
            <intent-filter>  
                <action android:name="android.intent.action.BOOT_COMPLETED" />  
                <category android:name="android.intent.category.HOME" />  
            </intent-filter>  

            <intent-filter>  
                <action android:name="android.intent.action.MEDIA_MOUNTED"/>  
                <action android:name="android.intent.action.MEDIA_UNMOUNTED"/>  
                <category android:name="android.intent.category.DEFAULT" />  
                <data android:scheme="file" />  
            </intent-filter>  


            <intent-filter>  
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />  
        <action android:name="android.intent.action.USER_PRESENT" />  
            </intent-filter>  
</receiver>  
```

2. 动态广播
只能在代码中注册，程序适应系统变化做操作，程序运行状态才能接收到

Intent.ACTION_SCREEN_ON //屏幕亮
Intent.ACTION_SCREEN_OFF //屏幕灭
Intent.ACTION_TIME_TICK //时间变化  每分钟一次

```java
IntentFilter filter = new IntentFilter();  
filter.addAction(Intent.ACTION_SCREEN_ON);  
filter.addAction(Intent.ACTION_SCREEN_OFF);  
filter.addAction(Intent.ACTION_TIME_TICK);  
registerReceiver(new MyBroadcastReceiver(), filter);
```

```java
IntentFilter msgFilter = new IntentFilter();
msgFilter.addAction(IMutualControlIntent.ACTION_USB_DEVICE_ATTACH);
msgFilter.addAction(IMutualControlIntent.ACTION_USB_DEVICE_DETACH);
mContext.registerReceiver(mMessageReceiver, msgFilter);

    // 匿名
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String type = intent.getStringExtra(IMutualControlIntent.EXTRA_DEVICE_TYPE);
                String path = intent.getStringExtra(IMutualControlIntent.EXTRA_MOUNT_PATH);
                if (path == null) {
                    return;
                }
                if (IMutualControlIntent.ACTION_USB_DEVICE_ATTACH.equals(action)) {
                    // handle usb attach
                } else if (IMutualControlIntent.ACTION_USB_DEVICE_DETACH.equals(action)) {
                    // handle usb detach
                }
        }
    };
```
