方式1. 使用intent-filter，直接在Activity节点中添加intent-filter

```java
        <activity android:name=".display.DisplayActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
```

方式2. 使用Activity的属性process，不同的Activity是运行在不同的进程

```java
<activity
     android:name=".MainActivity"
     android:label="@string/app_name"
	 android:process=”:process.main”>
     <intent-filter>
         <actionandroid:name="android.intent.action.MAIN"/>
         <categoryandroid:name="android.intent.category.LAUNCHER"/>
     </intent-filter>
</activity>

<activity
    android:name=".MainActivityB"
	android:label="@string/app_nameB"
	android:process=”:process.sub”
	android:icon=”@drawable/icon1”
	android:launchMode = “singleInstance”>
    <intent-filter>
       <actionandroid:name="android.intent.action.MAIN"/>
       <categoryandroid:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity>
```

方式3. 使用activity-alias。activity-alias是android里为了重复使用Activity而设计。

```java
<activity
     android:name=".MainActivityB"
     android:exported="true" 
     android:label="@string/app_nameB">
     <intent-filter>
         <actionandroid:name="android.intent.action.MAIN"/>
         <categoryandroid:name="android.intent.category.LAUNCHER"/>
     </intent-filter>
 </activity>

 <activity-alias
    android:name="ActivityB_copy"
    android:icon="@drawable/icon1"   
    android:screenOrientation="landscape"
    android:targetActivity=".MainActivityB">
    <intent-filter>
        <actionandroid:name="android.intent.action.MAIN"/>
        <categoryandroid:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity-alias>

 <activity-alias
    android:name="ActivityB_copy"
    android:icon="@drawable/icon1"   
    android:screenOrientation="landscape"
    android:targetActivity=".MainActivityB">
    <intent-filter>
        <actionandroid:name="android.intent.action.MAIN"/>
        <categoryandroid:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity-alias>
```

注：别名启动时，会重新创建Application，所以不同别名进入时，通过getIntent().getComponent().getClassName()得到的名字会不一样，一个是com.test.MainActivityB，一个是com.test.ActivityB_copy。

以上的方法，原理都是intent-filter.

```java
        <activity android:name=".display.DisplayActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
```

