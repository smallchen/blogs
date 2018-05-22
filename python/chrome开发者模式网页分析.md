[chrome-element]: chrome-element.png
[chrome-sources]: chrome-sources.png
[chrome-debug]: chrome-debug.png
[chrome-network]: chrome-network.png
[chrome-performance]: chrome-performance.png
[chrome-application]: chrome-application.png
[chrome-audits]: chrome-audits.png
[chrome-search]: chrome-search.png
[chrome-snippets]: chrome-snippets.png

#### Chrome开发者模式

##### Element

网页渲染后的页面源码。可以：

1. 直接修改页面源码，预览修改后的页面效果。
2. 直接在右侧面板修改css，预览修改后的页面效果。

前端开发者最爱，主要用来调试页面布局。

![chrome-element][chrome-element]

##### Console

`console.log()`日志输出终端

##### Sources

包含当前页面的组成文件，包含：

1. 网页源代码（渲染前）
2. javascript脚本文件
3. 页面的图片／字体／多媒体等等资源

可以：

1. 查看或保存各文件
2. 格式化源代码（Pretty Print，中间面板左下角的`{}`）
3. 断点调试javascript
4. 在当前页面环境执行自定义脚本（Snippets）

![chrome-sources][chrome-sources]
![chrome-snippets][chrome-snippets]

最核心的功能是调试javascript。调试功能有：

1. 运行到下一个断点
2. 运行到下一个函数调用（和步进类似）
2. 跳入函数内
3. 跳出当前函数
4. 步进
5. disable所有断点

![chrome-debug][chrome-debug]

##### Network【重点]

网络监控，可以：

1. 捕获加载页面过程浏览器和服务器之间的Http协议包（分析）。
2. 设置限定当前页面的加载环境（3G／4G／离线）。

用于分析加载过程发送和接收到的Http协议包。

![chrome-network][chrome-network]

##### Performance【重点]

性能监控，可以：

1. 捕获一段操作的调用栈（Call Tree）。
2. 捕获一段操作的函数调用耗时。
3. 捕获一段操作的内存消耗。
4. 设置限定当前CPU性能（在Setting里，4倍／6倍降速）。

![chrome-performance][chrome-performance]

如图，追踪按钮点击事件，找到QQ登录入口点，及后续的函数调用栈。

##### Memory

内存监控，可以：

1. 获取当前内存的快照。
2. 捕获一段操作的内存分配及时间戳。

##### Application

当前页面进程。可以：

1. 查看当前页面的Cookies
2. 查看当前页面的Cache
3. 查看当前页面的Frame

Frame这里可以更方便的获取到当前页面所有的javascript／css／images等等文件。更利于分析，尤其是javascript。

![chrome-application][chrome-application]

##### Security

查看当前页面的证书。

##### Audits

审查建议。给当前页面在性能／SEO／用户体验等等方面的建议。

![chrome-audits][chrome-audits]

##### 全局搜索

![chrome-search][chrome-search]

全局搜索的**缺点**是，只能搜索内容（网页／javascript／css／字符串参数／字符串回包），不能搜索`Request`或`Response`中的Header／Cookies／Url里面的值。

即，虽然能够搜索请求参数和回应数据，但不能搜索请求Header和回应Header中的内容。

所以需要注意，如果搜索不到，只是代表“可见内容”找不到，“不可见的Header／Cookies”中可能存在！！！！

比如，QQ登录中的login_sig就是存储在Header的Cookie中，无法被搜索到。
