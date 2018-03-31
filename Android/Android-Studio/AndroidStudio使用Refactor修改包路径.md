## AndroidStudio使用Refactor修改包路径

实例：
修改包名`com.jokin.sdk.demo`为`com.jokin.android.sdk.demo.rpcdemo`
包含了：
1. 中间修改包名，使用Refactor package
2. 包尾添加路径，使用Move package

方式1: 
1. 使用Refactor修改sdk为android。得到：`com.jokin.android.demo`。
2. 使用Refactor修改demo为rpcdemo。 得到：`com.jokin.android.rpcdemo`。
3. 打开showEmptyMiddlePackage，展开包路径。
4. 在android包层级下，添加sdk/demo。得到：

```java
- com
      - jokin
           - android
                  - sdk
                         - demo
                  - rpcdemo （所有源码在此）
```
5. 把包含所有源码的rpcdemo移动到demo包下。AndroidStudio会自动触发Refactor。
6. 包路径修改完成。

方式2:
1. 打开showEmptyMiddlePackage，展开包路径。
2. 建立所需的包路径：
```java
- com
      - jokin
           - sdk
                  - demo （所有源码在此）
           - android
                  - sdk
                        - demo
                            - rpcdemo （目标路径）
```
3. 使用Move，将demo下所有东西移动到rpcdemo下。
4. 包路径修改完成。

**警告：**
Refactor是很危险的操作，要比Move更危险。因为Refactor会匹配整个项目所有包路径，包括项目引入的module，项目资源等等。一不小心，Refactor会搞垮项目的链接，导致编译失败，要修复是非常崩溃的。所以建议使用Move。Move操作仅会影响到所操作的目录，然后更新外部引用，不会对外部同名module或其它同名资源产生影响。

所以，建议使用Move package，而不是Refactor package。方式2要更安全。
