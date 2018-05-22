#### PyV8
<https://code.google.com/archive/p/pyv8/>
<https://code.google.com/archive/p/pyv8/source/default/source>

V8引擎只是一个纯js引擎，并没有包含页面Dom对象和浏览器对象。所以和浏览器端不一样的是，V8环境默认没有提供window／document／browser等对象，所以如果js中调用到这些对象就会报错。
幸运的是，pyv8的官方demo中提供了`w3c.py`和`browser.py`用来提供Dom和浏览器环境支持！
我们所要做的是在这基础上做一些基本的修改。

#### 浏览器运行环境


#### 错误

1.ImportError: No module named v8browser

```python
from v8browser.jsrt import JSR
```

原因：由于文件夹v8browser不是python module，所以报错。
解决：在v8browser目录下，创建一个python文件`__init__.py`即可。


2.JS Error: please enter a context first

```python
PyV8.JSContext()
// 此处要执行 ctxt.enter()
ctxt.eval(jsfile.read())
print ctxt.eval("1+2")
```

原因：没有调用pyv8的enter方法。
解决：初始化完成后，加载js文件前，执行`ctxt.enter()`。或者使用python的域`with`：
> with 语句作为try/finally 编码范式的一种替代，用于对资源访问进行控制的场合。我习惯称其为域。其实就是自动释放。

```python
with PyV8.JSContext() as ctxt:
    print ctxt.eval("1+2")
```

3.JSError: TypeError: Property '$' of object [object JSR] is not a function (  @ 2661 : 16 )


3.python意外退出

原因：pyv8加载的javascript语法出错。使用在线javascript语法检查修复语法。另一种是多线程导致的偶发错误。重新运行。另外的原因是，javascript初始化失败，导致了崩溃，只是来不及输出错误信息。

另一个原因，脚本中有多个定时器，定时器执行的脚本导致多线程崩溃。
