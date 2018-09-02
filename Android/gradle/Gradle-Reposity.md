## Gradle Repositories



```java
buildscript {
    ext {

    }
    repositories {
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }// 阿里云maven镜像
        maven {
            url 'https://maven.google.com/'
            name 'Google'
        }
        google()
        jcenter()
        maven {
            url 'https://maven.aliyun.com/nexus/content/groups/public/'
        }
    }
    dependencies {
        classpath "io.realm:realm-gradle-plugin:4.3.3"
        classpath 'com.android.tools.build:gradle:3.1.2'
    }
}

allprojects {
    repositories {
        jcenter()
        google()
        flatDir {
            //读取MarqueeText模块的aar
            dirs 'libs', '../MarqueeText/libs', '../Connection/libs'
        }
        maven {
            url 'https://maven.aliyun.com/nexus/content/groups/public/'
        }
    }
}
```

如上，上面的`buildscript.repositories`里的repositories，是用于描述插件所在的仓库；
而下面`allprojects.repositories`里的repositories是描述项目依赖所在的仓库。不一样！！！

解决依赖缺失时，要分清是插件缺失，还是依赖库缺失。
