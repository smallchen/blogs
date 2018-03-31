## scrapy和selenium对比：
1. scrapy还是比较重的，各种回调不是那么容易被理解；不具备js执行能力，web页面中的脚本无力执行，得到的网页是js未执行前的源码，所以要得到页面数据需要分析网页中js的执行，然后模拟ajax进行get/post来得到用于渲染页面的纯数据（模拟ajax请求）；比较灵活，能够模拟任何的请求。
缺点是，请求的参数要用户自行模拟；尤其复杂的登录验证机制和复杂的cookie/session验证机制，比如：图片／滑块／手机等验证码，以及使用js进行复杂算法得到的cookie/session/key。这些都需要用户自行计算然后填到get/post请求中。只要有一个不对应，服务器就可能拒绝请求。

2. 相比，selenium就是一个浏览器，流式控制，简单容易理解。可以执行web页面中的脚本，既能得到网页源码，也能够得到js执行后的网页内容。但不支持post请求，不支持设定header／cookies等等，它只能通过执行js脚本来完成post请求。优点是，浏览器能做的事它都能做，不需要关心请求的参数，不惧怕任何的验证机制。

## 防止爬虫

1. js要混淆，要大文件，要复杂。`防止别人看懂逻辑`。
2. 内容使用js生成（Ajax数据接口）。`防止爬虫直接爬内容`。
3. Ajax数据接口可以提供一个或多个key，且要本地js即时生成，服务器严格判断key的值（key的值可以是根据cookie等特性生成的唯一值）。`爬虫一般没办法完整模拟浏览器，尤其是请求时才通过js生成的值`。
4. 就算方案多完善，都不能防住selenium的自动化脚本，因为selenium本质是一个浏览器。

## scrapy
1. clone scrapy
2. pip2.7 install -r requirements.txt
3. pip2.7 install sphinx
```java
// 修复以下错误
/bin/bash: sphinx-build: command not found
```
4. cd docs   &  make htmlview（文档）

scrapy有默认的请求回调流程，比如默认会执行start_url里的网页，然后执行start_request／parse等等。`只能参照文档`

可以记住：
1. scrapy有一套自己的回调流程，一套默认的设置和变量。
2. 只要回调返回了Request或者Request[]，这些Request就会被接着执行，执行完后会回调Request中设置的callback。callback中又可以返回Request。以此形成调用链。
3. 回调返回空表示调用链结束。
4. 所有调用链都结束，scrapy有结束的回调。
5. 能做不能做的，看开头。

## selenium
selenium是Web测试库，用于模拟web操作。可以运行在浏览器上。可以录制操作。可以作为库被其它语言使用。

python使用selenium库
> http://seleniumhq.github.io/selenium/docs/api/py/index.html

1. 安装 pip2.7 install -U selenium（要求2.7／3.5以上）
2. 下载浏览器驱动 Drivers （Chrome／Firefox／Safari），解压并放在`usr/local/bin`下面，或者初始化时指定路径也行。
3. 使用代码操作浏览器（会弹出一个浏览器窗口）

selenium的文档很详细。

1. WebDriver
2. WebElement  元素，元素的查找得到的也是元素
3. 等待，最多等待时间。

**Demo：列出常用操作**
```python
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

g_browser = webdriver.Chrome()
g_wait = WebDriverWait(g_browser, 10) # wait for at most 10s

webElement = g_browser.execute_script("return document.getElementById('root')")
webElement = g_browser.execute_script("return document.getElementById('root').innerHtml")
webElement = g_browser.execute_script("return alert('OK');")

webElement = g_browser.find_element(By.ID, "a-id")
webElement = g_browser.find_element(By.NAME, "a-name")
webElement = g_browser.find_element(By.CLASS_NAME, "a-class")
webElement = g_browser.find_element(By.TAG_NAME, "a-tag")
webElement = g_browser.find_element(By.XPATH, "a-xpath")
webElement = g_browser.find_element(By.CSS_SELECTOR, "a-css-sel")

webElement = g_browser.find_element_by_id("a-id")
webElement = g_browser.find_element_by_name("a-name")
webElement = g_browser.find_element_by_class_name("a-class")
webElement = g_browser.find_element_by_tag_name("a-tag")
webElement = g_browser.find_element_by_xpath("a-xpath")

name = g_browser.get_property("name")
length = g_browser.get_property("text_length")

# get_attribute > get_property，先查get_property，没有再查get_attribute
id = g_browser.get_attribute("id")
name = g_browser.get_attribute("name")
cls = g_browser.get_attribute("class")
href = g_browser.get_attribute("href")
style = g_browser.get_attribute("style")

EC.presence_of_all_elements_located()
EC.element_to_be_clickable()
EC.title_is()
EC.element_located_to_be_selected()
EC.alert_is_present()
EC.element_selection_state_to_be()
EC.frame_to_be_available_and_switch_to_it()
EC.new_window_is_opened()

# 等待，如果条件符合，则往下执行，不符合，最多等待10s（上面设置的值）
# 每一次until会重新计时（源码是这样）。
g_wait.until(EC.presence_of_all_elements_located)
g_wait.until(EC.element_to_be_clickable((By.CLASS_NAME, "btn-search")))
g_wait.until(EC.title_is("OK"))
g_wait.until(EC.element_located_to_be_selected(By.NAME, "selector"))
```

**简单的例子**
```python
from selenium import webdriver

browser = webdriver.Chrome()
browser = webdriver.Firefox()

browser.get("http://www.baidu.com")
print(browser.page_source)
browser.close() 
```

> http://selenium-python.readthedocs.io/api.html#module-selenium.webdriver.common.action_chains

## 一个登录QQ的例子

```python
# -*- coding:utf-8 -*-
# @author jokinkuang

from selenium import webdriver
import time

browser = webdriver.Chrome()
# or browser = webdriver.Chrome(executable_path="/path/to/chromedriver")
browser.get("https://ui.ptlogin2.qq.com/cgi-bin/login?appid=636026402&hln_css=https://mat1.gtimg.com/www/webapp/images/shipei_logo.png&style=8&s_url=https%3a%2f%2fbugly.qq.com%2fv2%2fworkbench%2fapps&pt_no_onekey=1")

input_str = browser.find_element_by_id('u')
input_str.send_keys("@qq.com")
time.sleep(1)

input_str = browser.find_element_by_id('p')
input_str.send_keys("xxxxx")
time.sleep(1)


button = browser.find_element_by_id('go')
button.click()
time.sleep(10)
print browser.page_source
```

通过js进行post
```java
WebDriver driver = new FirefoxDriver();   

        // 打开这个网站，防止js跨域请求（用baidu举例）
        driver.get("https://www.baidu.com");        

        // 开启开发者模式，方便观察请求
        Actions builder = new Actions(driver);
        builder.sendKeys(Keys.F12).perform();

        JavascriptExecutor jse = (JavascriptExecutor) driver ;

        try {

             String resp = (String) jse.executeScript(
                        "var xmlhttp=new XMLHttpRequest();\n" +
                        "xmlhttp.open(\"GET\",\"https://www.baidu.com\",false);\n" +
　　　　　　　　　　　　　　 "xmlHttp.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\");\n" +  // 表单提交的头部信息
                        "xmlhttp.setRequestHeader(\"testHeader\",\"123456\");\n" +  // 自定义请求头
                        "xmlhttp.send(\"name=test&sex=1&age=18\");\n" +   // 表单数据
                        "return xmlhttp.responseText;");

        　　 System.out.println(resp);

        } catch (Exception e) {
            //.......... Exception 
        }
```
