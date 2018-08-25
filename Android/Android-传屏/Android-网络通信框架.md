## Android 网络通信框架（网络编程）

### 网络通信框架

* Volley （http）
* Okhttp（http）
* Retrofit（http）
* AsyncHttpClient（http）
* netty（socket）
* mina（socket）

Android 5.0 的时候 Google 就不推荐使用 HttpClient

#### Volley

基本的使用方法： http://www.kwstu.com/ArticleView/kwstu_20144118313429
直接返回Object的话，用Gson/FastJson与Volley的结合：http://www.cnblogs.com/freexiaoyu/p/3955137.html

Volley的request默认回调到主线程,如果有需求是要加载到sqlite等等仍需要在子线程中进行的操作 解决方案 ： https://www.zhihu.com/question/36672622/answer/76003423

#### Okhttp

OKHttp源码位置 https://github.com/square/okhttp
泡网的源码分析：http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0326/2643.html

onResponse执行的线程并不是UI线程。


#### Retrofit

Retrofit 基于注解，提供JSON to POJO(Plain Ordinary Java Object,简单Java对象)，POJO to JSON，网络请求(POST，GET,PUT，DELETE等)封装。

#### AsyncHttpClient

(1)采用异步http请求，并通过匿名内部类处理回调结果
(2)http请求独立在UI主线程之外
(3)采用线程池来处理并发请求
(4)采用RequestParams类创建GET/POST参数
(5)不需要第三方包即可支持Multipart file文件上传
(6)大小只有25kb
(7)自动为各种移动电话处理连接断开时请求重连
(8)超快的自动gzip响应解码支持
(9)使用BinaryHttpResponseHandler类下载二进制文件(如图片)
(10) 使用JsonHttpResponseHandler类可以自动将响应结果解析为json格式
(11)持久化cookie存储，可以将cookie保存到你的应用程序的SharedPreferences中

#### netty

高性能NIO(Noblocking IO)模型的并发socket框架

#### mina
