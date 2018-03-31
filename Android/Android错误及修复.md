在Application初始化（create）时抛出RuntimeException，或者出现NullPointException，可能会没有log输出，只是提示apk dex错误。

1.  本地构建成功，而Jenkins上构建失败，报unspecified错误。
```java
What went wrong:
A problem occurred configuring project ':app'.
> Could not resolve all dependencies for configuration ':app:_platform551DebugApkCopy'.
   > Could not resolve  :common:unspecified.
     Required by:
         project :app > :librpcservice:R.0.1.29 > :sideslipbar-551-sdk:D.3.1.240-SNAPSHOT
      > Could not resolve SideSlipBar_develop:common:unspecified.
         > Could not get resource '[http:///nexus/content/repositories/snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom](http:///nexus/content/repositories/snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom)'.
            > Could not GET '[http:///nexus/content/repositories/snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom](http:///nexus/content/repositories/snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom)'. Received status code 400 from server: Repository version policy: SNAPSHOT does not allow version: unspecified
      > Could not resolve SideSlipBar_develop:common:unspecified.
         > Could not get resource '[http:///nexus/content/repositories/android_snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom](http:///nexus/content/repositories/android_snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom)'.
            > Could not GET '[http:///nexus/content/repositories/android_snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom](http:///nexus/content/repositories/android_snapshots/SideSlipBar_develop/common/unspecified/common-unspecified.pom)'. Received status code 400 from server: Repository version policy: SNAPSHOT does not allow version: unspecified
```

> 删除mavenLocal()
