## Android 使用系统隐藏API

### android.os.SystemProperties

1. 在项目中`src/main/java`下创建`android.os`同名包

2. 复制系统的`SystemProperties.java`到`android.os`包下（需要源码）

3. 构建即可直接使用`@hide`的`android.os.SystemProperties`类。
