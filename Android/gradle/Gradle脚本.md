AndroidStudio项目结构：

```java
- Project
   - application app
   - module sdk
   - module 3rd
   - module net
   ....
```
上面的Project／Application／Module都具有自己的build.gradle配置文件。真正能构建的好像只能在Application或Module，在Project里面部分命令是不支持的。

对于Module而言，在build过程，和Application对象几乎一样。

### project对象
1. 可以添加在任意build.gradle里面，Project除外。
2. 对于Application或Module，都指向当前Application或Module对象。**不能跨域**。比如，application app中project是app模块；module sdk中project是sdk模块；并不能通过app模块访问到sdk模块。
3. 

```java
build {
    doLast {
        def appName = "app"
        def moduleName = "rpcservice"
        def buildDir = project.projectDir.path+"/../";
        println buildDir
        def fromFile = buildDir + moduleName+"/build/outputs/aar/" + moduleName + "-release.aar"
        def intoFile = buildDir + appName + "/build/outputs/aar/"
        println fromFile
        println intoFile
        copy {
            from fromFile
            into intoFile
            rename {
                appName + "-release.aar"
            }
        }
        println("=====================build.doLast success.=========================")
    }
}
```
以上脚本代码，可以添加到Application或Module里，Project不支持。

### rootProject对象

```java
println project.rootProject.projectDir.absolutePath;
```

### copy&rename
坑爹的copy&rename，因为不提供复制时直接改变文件名。只能将文件复制到目录，然后再修改名称。

```java
 copy {
            from fromFile
            into intoFile
            rename {
                appName + "-release.aar"
            }
 }

task copyTmpChangelog(type: Copy) {
  from "input"
  into '.'
  rename { String fileName ->
    fileName.replace("input", "output")
  }
}
```

### static静态方法

```java
static def genVersionName() {
    String versionName = System.getenv("VERSION_NAME")
    if (versionName != null) {
        return versionName
    } else {
        return "UNDEFINED"
    }
}
def _versionName = genVersionName()
```

### ext 全局变量区

```java
ext {
    bintrayRepo = 'maven'
    bintrayName = 'appswitcher'
    bintrayOrg = 'inmarket'
    versionName = "1.0.0"

    publishedGroupId = 'com.inmarket'
    libraryName = 'appswitcher'
    artifact = 'appswitcher'

    libraryDescription = 'App switching library for Android'

    libraryVersion = '1.0.0'

    developerId = 'zaidd'
    developerName = 'Zaid Daghestani'
    developerEmail = 'zdaghestani@inmarket.com'

    licenseName = 'The Apache Software License, Version 2.0'
    licenseUrl = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
    allLicenses = ["Apache-2.0"]
}
```

## gradlew 命令
坑爹的gradle需要本地安装才能用，而gradlew则不需要，使用gradle会出现各种版本不对的错误，而gradlew则没有。所以放弃使用gradle吧。

1. ./gradlew install 生成pom文件在build目录下
2. ./gradlew build 构建所有版本
3. 

## Pom文件

```java
archivesBaseName = "libqrcodeshare"
```

## Gradle实例

```java
Project
   - app
        - build.gradle
   - sdk
        - build.gradle
        - genPom.gradle
```

app/build.gradle

```java
apply plugin: 'com.android.application'

boolean isBuildInJenkins() {
    String tag = System.getenv("BUILD_TAG")
    println "****************BUILD_TAG is $tag*****************"
    return tag != null && tag.contains("jenkins")
}
android {
    compileSdkVersion 26
    // 所有输出到output
    if (isBuildInJenkins()) {
        applicationVariants.all { variant ->
            variant.packageApplication.outputDirectory = new File(project.buildDir.absolutePath + "/outputs/apk/")
        }
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:26.0.0-beta1'
    implementation 'com.android.support.constraint:constraint-layout:1.0.2'

    compile project(path: ':sdk')
}
```

sdk/build.gradle

```java
apply plugin: 'com.android.library'
apply from: './genPom.gradle'

// 构建完成后执行，仅指当前module构建完成
build {
    doLast {
        def appName = "app"
        def moduleName = "sdk"
        def buildDir = project.projectDir.path+"/../";
        println buildDir
        def fromModule = buildDir + moduleName + "/build/"
        def intoApp = buildDir + appName + "/build/"
        println fromModule
        println intoApp

        println project.rootProject.projectDir.absolutePath;

        // copy aar -> app/build/
        copy {
            from fromModule + "/outputs/aar/" + moduleName + "-release.aar"
            into intoApp + "/outputs/aar/"
            rename {
                appName + "-release.aar"
            }
        }
        // copy pom -> app/build/
        copy {
            from fromModule + "/poms/pom-default.xml"
            into intoApp + "/poms/"
        }
        println("=====================build.doLast success.=========================")
    }
}
```

sdk/genPom.gradle

```java
apply plugin: 'com.github.dcendents.android-maven'

static def genVersionName() {
    String versionName = System.getenv("VERSION_NAME")
    if (versionName != null) {
        return versionName
    } else {
        return "UNDEFINED"
    }
}

static def getGroupId(){
    String groupId = System.getenv("GROUP_ID")
    if (groupId != null) {
        return groupId
    } else {
        return "UNDEFINED"
    }
}

static def getArtifactId(){
    String artifactId = System.getenv("ARTIFACT_ID")
    if (artifactId != null) {
        return artifactId
    } else {
        return "UNDEFINED"
    }
}

static def getName(){
    String name = System.getenv("LIB_NAME")
    if (name != null) {
        return name
    } else {
        return "UNDEFINED"
    }
}

def _groupId = getGroupId()
def _artifactId = getArtifactId()
def _versionName = genVersionName()
def _name = getName()

install {
    println mavenPomDir
    def appName = "app"
    def moduleName = "sdk"
    def buildDir = project.projectDir.path+"/../";
    println buildDir
    def fromModule = buildDir + moduleName + "/build/"
    def intoApp = buildDir + appName + "/build/"
    // 【修改默认的mavenPomDir路径，本来是在sdk/build/poms下，现在将其生成到app/build/poms目录下】
    mavenPomDir = intoApp + "poms/"
    println mavenPomDir
    println '========================xxxx======================='
    repositories.mavenInstaller {
        // This generates POM.xml with proper parameters
        pom {
            project {
                groupId _groupId
                artifactId _artifactId
                version _versionName
                name _name
            }
        }
    }
}
```
maven插件文档<https://docs.gradle.org/current/userguide/maven_plugin.html>，里面有说明，`gradlew install`是执行生成pom文件。

以上gradle，执行结果：

```java

[EnvInject] - Executing scripts and injecting environment variables after the SCM step.
[EnvInject] - Injecting as environment variables the properties file path 'gradle.properties'
[EnvInject] - Variables injected successfully.
[EnvInject] - Injecting environment variables from a build step.
[EnvInject] - Injecting as environment variables the properties content 

【1. 静态方法先执行】
CLASSIFIER=release
ARTIFACT_ID=librpcservice
REPOSITORYID=releases
VERSION_NAME=R.0.1.13
GROUP_ID=com.jokin.demo
LIB_NAME=LibRpcService_master
MVN_URL=http://mvn.gz.jokin.cn/nexus/content/repositories/${REPOSITORYID}

[EnvInject] - Variables injected successfully.
[Gradle] - Launching build.

[LibRpcService_master] $ /usr/local/cov-analysis-linux64-2017.07/bin/cov-build --dir $COV_IDIR /home/demo/tools/hudson.plugins.gradle.GradleInstallation/gradle-4.2/bin/gradle clean build install
Coverity Build Capture (64-bit) version 2017.07 on Linux 4.4.0-97-generic x86_64
Internal version numbers: 7a4de27b47 p-milo-push-30112.654


To honour the JVM settings for this build a new JVM will be forked. Please consider using the daemon: https://docs.gradle.org/4.2/userguide/gradle_daemon.html.
Daemon will be stopped at the end of the build stopping after processing

【2. app/build.gradle中的方法执行】
****************BUILD_TAG is jenkins-write_display-write_display_common_library-LibRpcService_master-13*****************

Configuration 'compile' in project ':app' is deprecated. Use 'implementation' instead.

WARNING [Project: :sdk] Support for not packaging BuildConfig is deprecated.

Configuration 'compile' in project ':sdk' is deprecated. Use 'implementation' instead.
						
【3. sdk／genPom.gradle执行】
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/sdk/build/poms
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/sdk/../
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/app/build/poms
========================xxxx=======================

【4. 各项目依次clean，相当于gradlew clean】
:clean
:app:clean
:sdk:clean

:app:preBuild UP-TO-DATE

:sdk:preBuild UP-TO-DATE
:sdk:preDebugBuild UP-TO-DATE
:sdk:checkDebugManifest
:sdk:processDebugManifest

:app:preDebugBuild
:sdk:compileDebugAidl
:app:compileDebugAidl
:sdk:packageDebugRenderscript NO-SOURCE
:app:compileDebugRenderscript
:app:checkDebugManifest
:app:generateDebugBuildConfig
:app:prepareLintJar UP-TO-DATE
:app:generateDebugResValues
:app:generateDebugResources
:sdk:compileDebugRenderscript
:sdk:generateDebugResValues
:sdk:generateDebugResources
:sdk:packageDebugResources
。。。省略。。。。。

:sdk:transformClassesAndResourcesWithPrepareIntermediateJarsForRelease
:app:javaPreCompileRelease
:app:compileReleaseJavaWithJavac
:app:compileReleaseSources
:app:lintVitalRelease SKIPPED
:app:transformClassesWithPreDexForRelease
:app:transformDexWithDexForRelease
:app:transformNativeLibsWithMergeJniLibsForRelease
:app:transformNativeLibsWithStripDebugSymbolForRelease
:app:transformResourcesWithMergeJavaResForRelease
:app:packageRelease
:app:assembleRelease
:app:assemble          【app】【gradlew assemble】
:app:lint              【app】【gradlew lint】
:app:javaPreCompileDebugUnitTest
:app:compileDebugUnitTestJavaWithJavac NO-SOURCE
:app:testDebugUnitTest NO-SOURCE
:app:javaPreCompileReleaseUnitTest
:app:compileReleaseUnitTestJavaWithJavac NO-SOURCE
:app:testReleaseUnitTest NO-SOURCE
:app:test UP-TO-DATE
:app:check             【app】【gradlew check】
:app:build             【app】【gradlew build】

:sdk:extractDebugAnnotations
:sdk:transformResourcesWithMergeJavaResForDebug
:sdk:transformClassesAndResourcesWithSyncLibJarsForDebug
:sdk:bundleDebug
:sdk:compileDebugSources
:sdk:assembleDebug
:sdk:extractReleaseAnnotations
:sdk:transformResourcesWithMergeJavaResForRelease
:sdk:transformClassesAndResourcesWithSyncLibJarsForRelease
:sdk:bundleRelease
:sdk:compileReleaseSources
:sdk:assembleRelease
:sdk:assemble   【sdk】【gradlew assemble】
:sdk:lint       【sdk】【gradlew lint】
:sdk:javaPreCompileDebugUnitTest
:sdk:compileDebugUnitTestJavaWithJavac NO-SOURCE
:sdk:testDebugUnitTest NO-SOURCE
:sdk:javaPreCompileReleaseUnitTest
:sdk:compileReleaseUnitTestJavaWithJavac NO-SOURCE
:sdk:testReleaseUnitTest NO-SOURCE
:sdk:test UP-TO-DATE
:sdk:check
:sdk:build.      【sdk】【gradlew build】
                 【sdk】【after build】
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/sdk/../
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/sdk/../sdk/build/
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master/sdk/../app/build/
/home/demo/workspace/write_display/write_display_common_library/LibRpcService_master
=====================build.doLast success.=========================
:sdk:install     【gradlew install】

BUILD SUCCESSFUL in 2m 8s
114 actionable tasks: 110 executed, 4 up-to-date
Emitted 94 Java compilation units (100%) successfully
[WARNING] Recoverable errors were encountered during 26 of these Java compilation units.
```

大致流程：
1. 静态方法先执行
2. app/build.gradle先执行
3. sdk引入的genPom.gradle执行
4. 当所有gradle文件执行一遍后，开始构建。
5. 各项目依次clean，相当于gradlew clean，构建前先清理。
6. 接下来依次执行，从Application app到Module sdk。
【app】【gradlew assemble】
【app】【gradlew lint】
【app】【gradlew check】
【app】【gradlew build】
7. 如果在app/build后面添加脚本，则脚本在此时执行（上面的 build { doLast{ } }只是针对当前Project或Module的构建过程，并不是指整个项目构建完成。）
【sdk】【gradlew assemble】
【sdk】【gradlew lint】
【sdk】【gradlew check】
【sdk】【gradlew build】
8. 如果在sdk/build后面添加脚本，则脚本在此时执行。
9. 最后一个module构建完成，执行install。
