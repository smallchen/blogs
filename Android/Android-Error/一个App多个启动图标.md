原因：
依赖的aar中，含有对android.intent.category.LAUNCHER感兴趣的Activity，然后被系统Launcher自动添加到启动列表中，出现多个启动入口。

```java
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
```

可以通过adb查看App的所有启动方式：
`dumpsys package com.jokin.example`

**方式1**: 在App的AndroidManifest.xml里覆盖掉。

```java
        <activity android:name=".MainActivity"
            android:exported="false"
            android:enabled="false"
            android:icon="@null">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
```
intent-filter是不能覆盖的，所以只能通过控制Activity的属性，使得Activity对外不可见／不可用／没图标等等，使得无法显示在Launcher。

**方式2**: 直接修改aar本身。

将aar改为zip，解压，修改aar的AndroidManifest.xml，打包，改为aar。
