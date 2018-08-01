<!-- TOC titleSize:2 tabSpaces:4 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [Android签名(Sign)](#android签名sign)
    - [生成和安装debug版本的apk](#生成和安装debug版本的apk)
    - [调试证书的有效期](#调试证书的有效期)
    - [生成和安装未签名的release版本的apk](#生成和安装未签名的release版本的apk)
    - [安装已签名的release版本的apk](#安装已签名的release版本的apk)
    - [给未签名的apk签名](#给未签名的apk签名)
        - [生成密钥和密钥库](#生成密钥和密钥库)
        - [给release签名](#给release签名)
        - [使用AndroidStudio构建Release版本](#使用androidstudio构建release版本)
    - [Debug和Release Key分离](#debug和release-key分离)

<!-- /TOC -->

## Android签名(Sign)

<https://developer.android.com/studio/publish/app-signing?hl=zh-cn>

### 生成和安装debug版本的apk

Android中，debug版本默认有debug签名，因此，生成和安装debug版本的apk，都可以通过`AndroidStudio`完成。

当然也可以通过命令`./gradlew clean assembleDebug`构建。

安装命令：`adb -s 29d1a493 install -r app/build/outputs/apk/debug/app-debug.apk`

Debug版本，Android Studio 将自动使用通过 Android SDK 工具生成的调试证书签署您的 APK。在 Android Studio 中首次运行或调试项目时，IDE 将自动在 $HOME/.android/debug.keystore 中创建调试密钥库和证书，并设置密钥库和密钥密码。

### 调试证书的有效期

用于针对调试签署 APK 的自签署证书的有效期为 365 天，从其创建日期算起。当此证书到期时，您将收到一个构建错误。若要修复此问题，删除 debug.keystore 文件即可。文件存储在以下位置：

* ~/.android/（OS X 和 Linux）
* C:\Documents and Settings\<user>\.android\（Windows XP）
* C:\Users\<user>\.android\ （Windows Vista，Windows 7、8 和 10）

### 生成和安装未签名的release版本的apk

如果使用`AndroidStudio`中，选择`release`的构建方式，则由于没有签名文件，默认不支持构建！！

如果要生成未签名的release版本apk，只能通过命令行！！

`./gradlew clean assembleRelease`

使用以上命令，就可以生成`release-unsigned.apk`，未签名的release版本的apk。

如果安装未签名的release版本的apk，会报错：

`adb -s 29d1a493 install -r app/build/outputs/apk/release/app-release-unsigned.apk `

报错：

```
Failed to install app/build/outputs/apk/release/app-release-unsigned.apk: Failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES: Package /data/app/vmdl1322654535.tmp/base.apk has no certificates at entry AndroidManifest.xml]
```

**所以，release版本必须签名后才能安装到设备！**

### 安装已签名的release版本的apk

如果设备已经安装了一个Debug版本，或者另一个签名的版本，那么直接使用`install -r`是无法安装的，必须先卸载旧的版本！！！然后重新安装新的签名版本！！！

### 给未签名的apk签名

使用 Android Studio 可以签署 APK

#### 生成密钥和密钥库

1. 在菜单栏中，点击 Build > Generate Signed APK。

2. 从下拉菜单中选择一个模块，然后点击 Next。

3. 点击 Create new 以创建一个新密钥和密钥库。

4. 在 New Key Store 窗口上，为您的`密钥库`和`密钥`提供以下信息，如下所示。

**注：`Generate Signed APK`只会产生一次`release`的apk，存放目录是`app/app-release.apk`**，如果要直接在`AndroidStudio`中选择`release`直接构建，需要修改`build.gradle`，见下面构建Release版本步骤。

**密钥库**

* Key store path：选择创建密钥库的位置，文件名建议带上后缀`.keystore`，方便识别。
* Password：为您的密钥库创建并确认一个安全的密码。

**密钥**

* Alias：为您的密钥输入一个标识名，比如`debugkey`,`keyforxxx`。
* Password：为您的密钥创建并确认一个安全的密码。此密码应当与您为密钥库选择的密码不同
* Validity (years)：以年为单位设置密钥的有效时长。密钥的有效期应至少为 25 年，以便您可以在应用的整个生命期内使用相同的密钥签署应用更新。
* Certificate：为证书输入一些关于您自己的信息。此信息不会显示在应用中，但会作为 APK 的一部分包含在您的证书中。

填写完表单后，请点击 OK。如果您只想生成一个密钥和密钥库而不签署 APK，则点击 Cancel。

#### 给release签名

上面步骤完成后，点击OK会进入签名安装。如果要对新应用签名时，也可以选择这个已经构建好的签名文件！

签名时，可以选择`Flavor`，如果没有`Flavor`，则只有一个选择`release`。

签名方式，可以选择两种（默认是两种都选择）：

* V1(jar signature) 旧的签名方式。
* V2(full apk signature)  Android7.0引入的新的签名方案`APK Signature Scheme v2`,它能提供更快的应用安装时间和更多针对未授权 APK 文件更改的保护。在默认情况下，Android Studio 2.2 和 Android Plugin for Gradle 2.2 会使用 APK Signature Scheme v2 和传统签名方案来签署您的应用（**默认是两种都选择！**）。可以添加`v2SigningEnabled false`来禁用新的签名。

```java
android {
   ...
   defaultConfig { ... }
   signingConfigs {
     release {
       storeFile file("myreleasekey.keystore")
       storePassword "password"
       keyAlias "MyReleaseKey"
       keyPassword "password"
       v2SigningEnabled false
     }
   }
}
```

#### 使用AndroidStudio构建Release版本

ProjectStructure - app - Signing - 添加新的配置：
> 或者，Run那里会打叉，直接在Run那里，`Edit Configuration`，然后有一个`Fix`按钮。自动跳转到同样配置路径。

* Name: 配置名，建议是`release`，(就是下面的`jokin`)。
* KeyAlias: 上面填写的`KeyAlias`，默认的是`key0`，取决于上面的值。
* KeyPassword: `密钥`密码。上面填写的`KeyPassword`
* StoreFile: 上面生成的签名文件位置（右侧可以定位），如果没有填写后缀，则key文件没有后缀！（所以可以使用`.keystore`后缀）
* StorePassword: `密钥库`密码。上面填写的`StorePassword`

以上配置，必须和签名文件配置的内容一致，否则无法通过构建！

配置完成，但发现还是不能直接构建Release版本，真是尴尬了。因为生成的`build.gradle`并不完整。需要手动添加：

```
buildTypes {
    release {
        signingConfig signingConfigs.jokin
    }
}
```

对应gradle完整的配置为：

```java
android {
    signingConfigs {
        jokin {
            keyAlias 'key0'
            keyPassword '123456'
            storePassword '123456'
            storeFile file('../jokinkey.keystore')
        }
    }
    buildTypes {
        release {
         minifyEnabled false
         shrinkResources false
         signingConfig signingConfigs.jokin
        }
    }
}
```

这下子OK了。

### Debug和Release Key分离

```java
android {
signingConfigs {
    signDebug {
        storeFile file(KEY_STORE_FILE_DEBUG)
        storePassword KEY_STORE_PASSWORD_OS
        keyAlias KEY_ALIAS_OS
        keyPassword KEY_PASSWORD_OS
    }

    signRelease {
        storeFile file(KEY_STORE_FILE_RELEASE)
        storePassword KEY_STORE_PASSWORD_OS
        keyAlias KEY_ALIAS_OS
        keyPassword KEY_PASSWORD_OS
    }
}
buildTypes {
    debug {
        signingConfig signingConfigs.signDebug
        // 修改manifest.xml
        manifestPlaceholders = [friday_appkey : "",
                                    bugly_app_id : ""]
    }

    release {
        minifyEnabled false
        shrinkResources false
        signingConfig signingConfigs.signRelease
        // 修改manifest.xml
        manifestPlaceholders = [friday_appkey : "",
                                    bugly_app_id : ""]
    }
}
}
```
