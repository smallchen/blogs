## 分析
要解决的问题：
1. 项目有清晰的依赖：子项目的分支／版本号／提交ID等等。
    大到整个整机，小到某一个Lib。
    以及Release版项目（Tag）／Master版本（trunk）／Develop版本（branch）。
    以及不同厂商定制版本的Release版／Master版／Develop版。
    以及不同尺寸版本的Release版／Master版／Develop版。
2. 分支切换跟随。
    A项目切换到Develop分支，A项目依赖的子项目都应该切换到Develop分支。好处：
    2.1 验证A项目在Develop过程的完整性，比如：A在Developing时，依赖的子项目B也在Developing，可以保证A／B在开发中能够保证功能完整性。反之，如果依赖的子项目B没有进行Develop，那么子项目B的Develop分支应该和Master和Release分支一致，也不影响A开发。
    2.2 假如A项目仅仅基于当前整机环境进行开发，但A依赖的子项目B也正在开发。所以此时把A所有的依赖都切换到Develop是不合理的，因为A是基于Release下进行升级开发。这种属于特殊情况？
3. 批量脚本支持j


A是主项目，B是依赖项目，C是主项目

1. 临时Bug：A需要紧急修Bug，并不需要B进行更新。
2. 新需求：A需要更新，B也需要更新，但C依赖B。如果B进行了重构，则C也需要跟着改变。


A使用B的master进行开发，A可行。但由于B的Develop开发和A不兼容，B提交的时候，A需要进行同步修复。

SDK同时支持多个版本。

1. 多人同时修改同一个Project，添加新特性。版本号冲突。内容冲突。
2. 项目依赖的子项目版本号
3. 项目的版本号管理
4. 项目的依赖管理。比如：A项目依赖的B项目Ver1.0，C项目依赖B项目Ver2.0.
5. 如果依赖的B项目是单实例项目，那么统一升级到2.0，因为依赖的是单实例，不可能出现两个版 本。比如系统应用。
6. 如果依赖的B项目是多实例项目，那么可以分别使用不同的版本，像各自独立的APP依赖的support-v4版本不一样也可以。

单实例：CommonLibrary／LibConstants等基础库
       demoOSService／demoControlForPC等提供IPC服务。
多实例：Calendar／放大镜／投票器等APP

Calendar V1.0 -》 CommonLibrary V1.0
Calendar V2.0 -》 CommonLibrary V1.1。如果向后兼容，那么可以统一升级到V1.1，是否可以不升级。
分为：
1.如果是变量等支持多实例的模块，可以分别依赖V1.1和V1.0，因为仅仅是用于编译。
2.如果模块跑起来是单实例，即CommonLibraryV1.0和V1.1都是同一个类的单实例，那么只能有一处初始化。

Vote V1.0 -》 CommonLibrary V1.0
Vote V2.0 -》 CommonLibrary V1.0

除了版本号外，还有根据指定一代机／二代机／客户机定制的版本。
1.0.0.0正式版。1.0.0.1／1.0.0.2/1.0.0.3 分别对应不同的客户版本。
1.1.0正式版（或者叫修复版）

1. 出现问题回退时，依赖库也可能需要回退。
2. 项目开发过程，依赖库也需要开发。
3. 依赖库改变时，依赖此库的项目都需要重新验证。
4. 提供服务的APP进程，也相当于依赖库一样的存在。


1. 开发过程，某个子模块被更新，项目需要同步，否则编译失败的问题。
2. 开发过程，某个子模块的分支不对，导致编译失败的问题。
3. 开发过程，查看当前项目包括子模块发生的所有的改变（repo是针对整个Big项目）。
4. 开发过程，依赖的package来自哪个库（比如库A提供com.vcommons,库B提供com.commons,很难分清引用的是库A还是库B，因为可能库A进行了命名优化，更可能有人压根不知道库B存在，就一直以为是库A的某个版本）
5. 开发过程，需要注释部分代码才能编译通过的问题，这个会影响diff，也影响正常的pull等操作，最后可能会导致提交不完整，因为注释部分不能提交，导致漏提交或者无意中把注释也提交了。
6. 开发过程，修改了多个平级Application，提交时忘记提交另一个Application。
7. 需要查看某个Application的某个版本，该版本对应的模块的版本呢？
8. 新旧版本，逻辑不一致如何跟踪？
9. 如何跟踪一个Bug的修复。一个Bug的修复可能涉及到多个模块。
10. 合入的时候，会忘记修改了哪个模块，和需要合入哪些提交。
11. 合入的时候，原分支一些功能不上release，此时需要先修改分支。


问题解决：
- 4.1 利用IDE的跳转，先保持包路径，修改类名，通过跳转来确定当前引入的package属于哪个子模块。
- 4.2 通过输出所有的模块提供的包名，进行匹配（但没有找到对应的输出工具）
- 8 从消息入口跟踪，先看消息入口文件的历史修改，然后逐步跟踪。

Tag，利用Tag可以很方便跟踪版本。Tag其实是commit-id的别名，但别名有个好处，就是每个git仓库即使commit-id不一致，但别名是可以相同的，对于项目的版本管理会有很大帮助。只需要通过一个Tag，就可以检出多个git仓库不同的commit-id。这就是Tag的作用！
也可以通过Tag来比对两次Tag之间的差异。
Tag通常为整体系统的版本号，发布日期，额外信息。
子模块也可以有自己的Tag，Tag是可以重合的，所以关系不大。


查看Tag对应的commitid：
jokins-MacBook-Pro:jokinkuang:~/Documents/projects/510/510_demo_master/McuService2014/tmp$ git show-ref --abbrev=5 --tags
4c8f3 refs/tags/v1.2
4c8f3 refs/tags/v1.3
4c8f3 refs/tags/v1.4
ed865 refs/tags/v1.5
ed865 refs/tags/v1.6
ed865 refs/tags/v1.7
ed865 refs/tags/v1.8
ed865 refs/tags/v1.9
ed865 refs/tags/v2.0

其中，abbrev是指commit-id保留多少个字符串
目录是/.git/refs/tags下的文件

后续优化：
1. 是否保留Module，保留的话，意味着Module版本的问题，不保留要解决Module开发的问题
通常，要解决这个问题，先分析Module可能是哪些？
1.1 跨进程通信的结构定义模块，共用常量。
1.2 作为中间件的转发进程？不属于Module，属于。
1.3
2.
