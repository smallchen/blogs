```java
- Module sdk
      res/anim
           translucent_enter.xml
           translucent_exit.xml

- App app
       res/anim
            translucent_enter.xml
            translucent_exit.xml               
```
如上，Application里的res/anim同名资源，能够覆盖Module下的res/anim资源，相当于hook成自己的配置。

**总结**
App本地的同名资源，可以覆盖子模块/依赖中的同名资源。

所以：
1. 开发库的时候，需要将资源命名为唯一，防止引用时被App主项目覆盖。
