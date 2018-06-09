## Gradle SourceSets

```java
android {
    sourceSets {
        main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src']
            resources.srcDirs = ['src']
            aidl.srcDirs = ['src']
            renderscript.srcDirs = ['src']
            res.srcDirs = ['res']
            assets.srcDirs = ['assets']
            jniLibs.srcDirs = ['libs']
    	}
	}
}
```

修改：

```java
sourceSets {
    main {
        java {
            srcDir 'src/java' // 指定源码目录
        }
        resources {
            srcDir 'src/resources' //资源目录
        }
    }
}
```

配置：

```java
sourceSets {
	main {
	  if (isDebug.toBoolean()) {
		  manifest.srcFile 'src/main/debug/AndroidManifest.xml'
	  } else {
		  manifest.srcFile 'src/main/release/AndroidManifest.xml'
	  }
	}
}
```

增加：

```java
sourceSets {
    integrationTest {
        compileClasspath += sourceSets.test.runtimeClasspath
        runtimeClasspath += sourceSets.test.runtimeClasspath
    }
}
```

排除：

```java
sourceSets {
	main {
		java {
			exclude '/test/**'  // 不想包含文件的路径
		}
		resources {
			exclude '/resource/**' // 不想包含的资源文件路径
		}
		.....
	}
}
```
