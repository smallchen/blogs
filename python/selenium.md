## selenium使用

#### 概述

selenium是自动化测试工具，由于不是纯脚本，所以有一些限制：

* 可以获取／设置cookies和headers
* 可以直接`get`，但不能直接`post`，只能通过控制网页来实现post。

所以，有一个思路是，使用selenium进行登录，然后把Cookies和Sessions导出到python原生的`requests`。

#### 实例

具体见<https://github.com/jokinkuang>

```python
from selenium import webdriver
import time

driver = webdriver.Firefox()
driver.get("http://192.168.1.51/oneCard/login")
#将用户名密码写入浏览器cookie中
driver.add_cookie({'name':'username','value':'super'})
driver.add_cookie({'name':'password','value':'asd,./123*.'})
#再次访问网站，将会自动登录
driver.get("http://192.168.1.51/oneCard/login")
```

```python
from selenium import webdriver

driver = webdriver.Firefox ()
# driver.get("http://member.rltxtest.xyz/login/login.html")
driver.get ("http://www.youdao.com")
# 向cookie中name和value中添加回话信息
driver.add_cookie ({'name': 'key-aaaaaaa'}, {'value': 'value-bbbbb'})

# 遍历cookie中name和value信息并打印对应的信息，并包括添加对应的信息
for cookie in driver.get_cookies ():
    print("%s->%s" % (cookie['name'], cookie['value']))

driver.quit ()
```

```python
# switch selenium to python environment
def switch(self):
    global g_browser
    global g_wait

    self.cookieMap = {}
    cookieList = g_browser.get_cookies()

    for cookie in cookieList:
        self.cookieMap[cookie.get('name')] = cookie.get('value')

    print "## set Cookies to Session ##"
    requests.utils.add_dict_to_cookiejar(self.session.cookies, self.cookieMap)
    print self.session.cookies

    print "## get Authorization ##"
    response = self.session.get(url = URL_LOGIN, headers=self.headers)
    response = self.session.get(url = URL_SELF_INFO, headers=self.headers)
    self.account = response.json()['data']
    print self.account
```

控制表单

```python
def sendAudio(self, path):
    global g_browser
    global g_wait

    inputs = g_browser.find_elements_by_tag_name(TAG_INPUT);
    audioInput = inputs[1]
    audioInput.send_keys(path)

    form = g_browser.find_element_by_tag_name(TAG_FORM);
    form.submit()
```

控制弹窗

```python
public static void testAlert(WebDriver driver)
    {
        String url="http://sislands.com/coin70/week1/dialogbox.htm";
        driver.get(url);

        WebElement alertButton = driver.findElement(By.xpath("//input[@value='alert']"));
        alertButton.click();

        Alert javascriptAlert = driver.switchTo().alert();
        System.out.println(javascriptAlert.getText());
        javascriptAlert.accept();
    }

public static void testPrompt(WebDriver driver) throws Exception
    {
        String url="http://sislands.com/coin70/week1/dialogbox.htm";
        driver.get(url);

        WebElement promptButton = driver.findElement(By.xpath("//input[@value='prompt']"));
        promptButton.click();
        Thread.sleep(2000);
        Alert javascriptPrompt = driver.switchTo().alert();
        javascriptPrompt.sendKeys("This is learning Selenium");
        javascriptPrompt.accept();    

        System.out.println(javascriptPrompt.getText());

        javascriptPrompt=driver.switchTo().alert();
        javascriptPrompt.accept();

        Thread.sleep(2000);
        promptButton.click();
        javascriptPrompt=driver.switchTo().alert();
        javascriptPrompt.dismiss();
        Thread.sleep(2000);
        javascriptPrompt=driver.switchTo().alert();
        javascriptPrompt.accept();
    }

public static void testConfirm(WebDriver driver) throws Exception
    {
        String url="http://sislands.com/coin70/week1/dialogbox.htm";
        driver.get(url);

        WebElement confirmButton = driver.findElement(By.xpath("//input[@value='confirm']"));
        confirmButton.click();
        Thread.sleep(2000);
        Alert javascriptConfirm = driver.switchTo().alert();
        javascriptConfirm.accept();
        Thread.sleep(2000);
        javascriptConfirm = driver.switchTo().alert();
        javascriptConfirm.accept();
    }
```

参考<http://www.cnblogs.com/TankXiao/p/5260445.html>
