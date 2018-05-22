[qq-login-iframe]: qq-login-iframe.png
[qq-login-network]: qq-login-network.png
[qq-login-network2]: qq-login-network2.png
[qq-login-check]: qq-login-check.png
[qq-login-request]: qq-login-request.png
[qq-login-request2]: qq-login-request2.png
[qq-login-pt-verifysession]: qq-login-pt-verifysession.png
[qq-login-call-tree]: qq-login-call-tree.png
[qq-login-getsubmiturl]: qq-login-getsubmiturl.png
[qq-login-h]: qq-login-h.png

### 突破QQ的登录验证

#### 第一步，了解Chrome开发者模式

首先要了解Chrome开发者模式下的`Network`,`Sources`和`Performance`。

#### 第二步，分析QQ登录过程

在Chrome的开发者模式下，进入登录页。如图。

![qq-login-iframe][qq-login-iframe]

可以看到，登录窗是一个`iframe`.

为了减少捕获到的数据包，单独在新的标签打开这个`iframe`页面。

如下图，进入`Network`勾选`Preserve Log`，然后刷新登录`iframe`，得到如下数据包。

![qq-login-network][qq-login-network]

接着，输入任意密码，点击登录，得到如下数据包序列（过滤剩JS）。如下图。

![qq-login-network2][qq-login-network2]

可以直观的看到，主要是`check`,`report`,`login`这三个`api`操作。

查看`check`操作：

![qq-login-check][qq-login-check]

主要是返回验证码。如果第一个参数为0，表示不需要验证码。第二个参数是隐含验证码(`!TLR`)

查看`report`操作，然后发现只是反馈，可能可以忽略。

查看`login`操作：

![qq-login-request][qq-login-request]

可以看到，这个就是QQ登录的核心数据包。包含：QQ账号／验证码(`!TLR`)／加密后的密码／pt_verifysession_v1／pt_randsalt／pt_jstoken／login_sig／aid等等。

我们需要逐一破解这些字段值。**通常一个请求，大部分字段是不变的，只有少数核心的几个会变化。**

为了验证哪些是可变的，我们再一次登录。

![qq-login-request2][qq-login-request2]

然后比对。发现下面这些字段才是变化的：

* verifycode
* pt_verifysession_v1
* p
* action

为了找到这些值来源，我们需要在上下文的Response回包中查找（请求包就不需要看了）。
> 因为，数据一切来自服务器

下面是这些值的常见来源：

* 当前静态页面URL回包的Header。
* 当前静态页面URL回包的Cookie。（比如QQ登录页回包中，Cookie存储着`login_sig`的值)
* 当前静态页面的源码。（比如Bugly页面源码中的meta存储着`X-token`的值）
* 当前静态页面的javascript运算结果。（比如QQ登录的密码加密）
* 紧接着的脚本中任意一次API调用的回包，包括Header／Body。（比如QQ登录中`check`api回包存储着登录时需要的`verifycode`和`pt_verifysession_v1`）

**总结**：值的来源，大部分来自当前静态页面URL的应答包。少数来自当前静态页面发起的API应答包。极少数来自当前静态页面的脚本运算。

基于这个流程，我们找到了`login_sig`是来自`iframe`页面首次加载的应答包头部Cookie。

通过全局搜索，可以发现pt_verifysession_v1也来自上一个`check`(如下图)。

![qq-login-pt-verifysession][qq-login-pt-verifysession]

> 搜索只能针对 参数区／应答内容／源文件，无法搜索Header。所以全局搜索是无法找到存储在`Cookies`里面的`login_sig`，不信你试试。

p很明显来自静态页面javascript的运算结果，下面会重点讲解。

action没有找到，可以先固定不变。

回过头来，**QQ的登录过程** 是这样子：

*1.访问登录页：*
`https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=636014201&s_url=http%3A%2F%2Fwww.qq.com%2Fqq2012%2FloginSuccess.htm&style=20&border_radius=1&target=self&maskOpacity=40&`

其中，`appid`固定，`s_url`表示登录成功后跳转页，其它不重要。

*2.登录页回包中，包含着`login_sig`，以后每次登录都需要传递。*

*3.登录前获取验证码:*
`/check?regmaster=&pt_tea=2&pt_vcode=1....`

需要以下参数：

```javascript
regmaster:
pt_tea: 2
pt_vcode: 1
uin: 2153413946@qq.com
appid: 636014201
js_ver: 10270
js_type: 1
login_sig: wCmJu71RdWuYmDE7YgQB7SZ4kv5S*KImajiZK3OmB3xPnoV-9hJJvHn7BVx6dXod
u1: http://www.qq.com/qq2012/loginSuccess.htm
r: 0.740802233364823
pt_uistyle: 40
pt_jstoken: 1776279543

```
其中，`login_sig`来自登录页。其它可以保持不变。

返回值：

`ptui_checkVC('0','!DBC','\x00\x00\x00\x00\x80\x5a\x7d\x3a','73f6aba09b7f6c339416d0cb5dfcd111689ed5e1ee605277aeb6cddfc446d76822674dc9d23c424e512881b31bc905fdd0d2c8bd36d2232e','2')`

其中，0表示不需要验证码，第二个元素是隐藏验证码`verifycode`，长字符串是`pt_verifysession_v1`

*4.通过本地javascript将密码加密，然后组成登录url：*

`/login?u=2153413946%40qq.com&verifycode=!DBC&....`

需要以下参数：

```javascript
u: 2153413946@qq.com
verifycode: !DBC
pt_vcode_v1: 0
pt_verifysession_v1: 73f6aba09b7f6c339416d0cb5dfcd111689ed5e1ee605277aeb6cddfc446d76822674dc9d23c424e512881b31bc905fdd0d2c8bd36d2232e
p: MPocLNmEQ0Ci2alJtrlD6eI4pDIeCFx6r3UxTcFPYSQFQdmGIWv8X-Jd2IfJT2iPMbPjbfUX2x6JCKjI9fU5x7eXnED-5RxV8OxgsDq3M7RjImc1PtbFl9NymXOxnbtqmAPPQd5RQUK-*VhUsMxAWCyMp3D*LAMwf1DjkE892DD8DpTYz16szlEloacH2YXutLlLGrimYqirqe1*9UreEuWh1NcWH24VVk3uPEOvQcJyVji30HS2akDUBzzWRSzvg86ftms1tSNIjF8BXpaSG1V*dRDvKjnifN3jQuWUW-iWkR6dFHs3055a4W*O64rq3wz8EHQxYIMD3Y*I8ruoMA__
pt_randsalt: 2
pt_jstoken: 1776279543
u1: http://www.qq.com/qq2012/loginSuccess.htm
ptredirect: 0
h: 1
t: 1
g: 1
from_ui: 1
ptlang: 2052
action: 5-8-1523327902216
js_ver: 10270
js_type: 1
login_sig: wCmJu71RdWuYmDE7YgQB7SZ4kv5S*KImajiZK3OmB3xPnoV-9hJJvHn7BVx6dXod
pt_uistyle: 40
aid: 636014201
has_onekey: 1

```
其中，`verifycode`来自验证码回包，`pt_verifysession_v1`也来自验证码回包，`p`是本地加密后密文，`login_sig`来自登录页。其它可以保持不变。

登录返回值是：

`ptuiCB('0','0','https://ptlogin2.bugly.qq.com/check_sig?pttype=1&uin=...&service=login&nodirect=0&ptsigx=68aef354f64db3b27478a846e1e8059a8553bfbeb5045efe552069881e9ff3523cba148bd5cd0cb8dcf007db24a9839d46c48bcc4605e6211835a7349a4cde58&s_url=https%3A%2F%2Fbugly.qq.com%2Fv2%2Fworkbench%2Fapps&f_url=&ptlang=2052&ptredirect=100&aid=603049403&daid=276&j_later=0&low_login_hour=0&regmaster=0&pt_login_type=1&pt_aid=0&pt_aaid=0&pt_light=0&pt_3rd_aid=0','0','登录成功！', 'bugly')`

其中，第一个0表示登录成功，第二个0没考究，第三个是`跳转回目标站点的URL`，最后一个是用户名。

*5.登录成功后，直接访问目标站点，比如`https://bugly.qq.com/`仍旧会提示登录。后来发现，QQ统一登录成功后，并不代表已经登录第三方目标站点（网上看别人的成果）。*

*6.上一步QQ登录成功后，会返回一个跳转URL，需要接着访问这个`跳转URL`，然后得到与目标站点相关的重要Cookies。*

*7.第三方目标站点，还会有类似`login`这样的入口。`跳转URL`得到的Cookies在访问这个`login`入口时，可以不需要做任何输入就能得到目标站点已登录的`Session`。只有带上这个已登录的`Session`，才能访问第三方站点。*

以Bugly为例，QQ登录成功后，登录Bugly：

```javascript
// jump url
self.session.get("https://ptlogin2.bugly.qq.com/check_sig?pttype=1...")
// login to bugly
self.session.get("https://cas.bugly.qq.com/cas/login?service=https%3A%2F%2Fbugly.qq.com%2Fv2%2Fworkbench%2Fapps")
self.session.get("https://cas.bugly.qq.com/cas/loginBack?type=9")
// get data
self.session.get("https://bugly.qq.com/v2/workbench/apps")
```

最后总结，第三方站点QQ登录过程是

1. 访问QQ统一登录页，得到`login_sig`
2. `check`，得到验证码
3. `login`登录到QQ，得到`跳转回第三方的URL`
4. 访问`跳转回第三方的URL`，得到第三方站点的Cookies
5. 带上第三方站点的Cookie访问第三方的`login`入口
6. 将直接通过第三方验证，得到第三方已登录的`Session`
7. 带上这个`Session`就可以任意访问第三方站点

> 这部分的研究可以参考网上的部分文章，可以更加快速的了解哪些参数可变，哪些参数可以固定。

**QQ密码的加密流程突破**

QQ密码的加密，应该是QQ登录验证最难突破的问题。因为不知道加密算法就无法得到加密后的密文。幸运的是，Web端脚本的执行在浏览器，所以可以得到QQ加密的javascript文件。但是混淆压缩后的javascript，很难从中找到加密模块的入口。

这时，Chrome开发者模式的Performance可以大显神通。

Performance可以捕获一段操作的调用栈，我们可以利用这个原理，跟踪QQ登录时javascript的函数调用过程。启动捕获，操作登录按钮，停止捕获。得到下图的调用栈：

![qq-login-call-tree][qq-login-call-tree]

如图，点击`Click`后，

1. 产生事件Event Click
2. 调用`submit`
3. 调用`getSubmitUrl`
4. 然后调用一系列混淆后的方法

从`Summary`窗口，可以跳转跟踪到`getSubmitUrl`的javascript（位于`Sources`下）。如下图。

![qq-login-getsubmiturl][qq-login-getsubmiturl]

从中可以看到QQ密码加密的javascript文件是`c_login_2.js`。密码加密入口是`i.p = $.Encryption.getEncryption(n, salt, verifycode, safeEdit)`。这里是传入参数（密码，盐，验证码，safeEdit）来进行密码加密。

在加密处加个断点，可以看到`i`其实表示`login`api对象。包含了`login`这个api具备的所有参数和值（如上图），比如：

* i.u是用户名
* i.p是密码的密文
* i.verifycode是验证码
* i.pt_verifysession_v1是验证码带上的session

对上上面登录的Http协议包，就能发现和登录协议包中的请求参数一模一样。

`i.p = $.Encryption.getEncryption(n, salt, verifycode, safeEdit)`的下一步(步进调试)，执行如下图：

![qq-login-h][qq-login-h]

可以看到，真正执行加密的，是一个混淆后的方法`function h(e, i, n, o)`。参数分别对应`密码，盐，验证码，safeEdit`。safeEdit通常是`undefine`。

总结，QQ的密码加密模块位于`c_login_2.js`文件，入口是`$.Encryption.getEncryption`，参数是`密码，盐，验证码，safeEdit`。

**QQ密码的加密实现**

通过上面的分析，我们得到了javascript文件，调用入口，差的就是一个可以运行javascript的环境。

python环境下，pyv8库封装了Google V8 javascript引擎。可以在python环境下执行纯javascript脚本。（V8的使用，可以看另一篇文章）

```python
def encrypt_qq_pwd(self, pwd, salt, verifycode):
	with PyV8.JSContext() as ctxt:
		with open("c_login_2.js") as jsfile:
			ctxt.eval(jsfile.read())
			str = u"Encryption.getEncryption('%s', '%s', '%s', undefined)" % (pwd.encode('utf-8'), salt.encode('utf-8'),
					  verifycode.encode('utf-8'))
			encrypt_pwd = ctxt.eval(str.encode("utf-8"))
			print encrypt_pwd
			return encrypt_pwd
```

由于V8引擎只是纯`javascript`引擎，所以不支持`window`，`document`这两个`javascript`对象。所以，原始下载的`c_login_2.js`会报错，需要进行修改。

修改包括：

1. 添加window = {}
2. 添加document = {}
3. 添加setTimeout = function(){}
4. 添加$ = {}
5. 添加pt = {}
6. 删除任何$("p")类似UI操作代码
7. 删除任何ptUI开头的方法
8. 所有全局对象挂在window对象下，比如加密算法Encryption，和其中的TEA，RSA，btoa等等。
9. 简而言之，保留Encryption调用到的方法即可。

其中，pt位于xlogin.html页面内嵌的脚本，当前脚本不存在，所以创建。覆盖setTimeout防止定时器工作。创建不存在的对象。删除所有UI相关操作脚本（都封在一个大对象里面，使用折叠删除很快就搞定）。原本的全局对象是挂在`$`下，改为挂在`window`下，否则可能找不到，或者是被`$`覆盖过的错误的方法。

下面给出部分核心代码：

```javascript
Encryption = function() {
	// 省略
    function h(e, i, n, o) {
        n = n || "",
        e = e || "";
        for (var p = o ? e : t(e), r = g(p), s = t(r + i), a = window.TEA.strToBytes(n.toUpperCase(), !0), l = Number(a.length / 2).toString(16); l.length < 4; )
            l = "0" + l;
        window.TEA.initkey(s);
        var c = window.TEA.encrypt(p + window.TEA.strToBytes(i) + l + a);
        window.TEA.initkey("");
        for (var u = Number(c.length / 2).toString(16); u.length < 4; )
            u = "0" + u;
        var h = window.RSA.rsa_encrypt(g(u + c));
        return window.btoa(g(h)).replace(/[\/\+=]/g, function(t) {
            return {
                "/": "-",
                "+": "*",
                "=": "_"
            }[t]
        })
    }
    return {
        getEncryption: h,
        getRSAEncryption: f,
        md5: t
    }
}(),
```

加密的参数中，`密码，盐，验证码，safeEdit`，多次调用，发现盐可以固定为`2`，safeEdit固定为`undefine`。剩下的验证码通过`check`api得到。

通过测试，即使参数一致，每次加密后的密文都不一样。

> QQ密码加密过程的javascript还是比较清晰的，即使不知道加密算法，也可以将javascript语法转化为其它语言实现来完成加密算法。

#### 第三步，模拟请求

模拟请求就是模拟分析过程中，捕获到的Http协议包，通过`编程语言`实现，而不是通过`浏览器`。

有一个规则，就是，**无论是可变的，还是不变的，都可以先固定不变来测试。然后使用排除法，逐一替换成真正的赋值**，比如，可以固定action不变，直到测试不过，才需要考虑action的值从哪里来。

上面已经分析过QQ登录流程：

`登录页html` - `check` - `login` 三部曲。

其它没什么好说了。

#### 其它方案

只要能够运行js的环境，基本可以实现QQ的脚本登录。比如著名的`nodejs`也是可行的。

#### 附录错误解决方案

1.Boost.Python.ArgumentError错误。`encrypt_pwd = ctxt.eval(str)`报错。

```python
Boost.Python.ArgumentError: Python argument types in
eval( (JSContext)arg1, unicode)
did not match C++ signature:
eval(CContext {lvalue},std::string [,std::string='' [,int=-1 [,int=-1 [,boost::python::api::object=None]]]])
```

原因是参数中，字符串的编码格式不对，重新编码即可。
`str = u'unicode编码'`
`encrypt_pwd = ctxt.eval(str.encode("utf-8"))`


2.javascript: object is not a function

```python
ctxt.eval('Encryption("","","", undefine)')

Encryption = function() {
	function h(e, i, n, o) {
		return xxxx.
	}
	var _ = 1
	  , m = 8
	  , v = 32;
	return {
		getEncryption: h,
		getRSAEncryption: f,
		md5: t
	}
}()
```
如上，Encryption是一个匿名函数的调用返回值。（Encrypt = function(){}())。匿名函数return的是一个object{}。其中，getEncryption属性是h函数，md5属性是t函数。

所以，正确的是`Encryption.getEncryption("","","", undefine)`。

3.window/window.setTimeout/document/$pt/pt undefine

原因，V8引擎只是一个纯js引擎，并没有包含页面Dom对象和浏览器对象。幸运的是，pyv8的官方demo中为我们做了80%以上的工作，提供了w3c.py和browser.py的支持！我们所要做的是在这基础上做一些基本的修改。

解决，删除Dom对象的操作，去除document。由于window对象是全局的，可以自定义对象。`window = {}`。`window.pt = function(){}`。等等。
