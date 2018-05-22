### Scrapy系列

Scrapy无需介绍，一般都是非图形。

### Selenium系列

从Scrapy和Selenium的区别中看，Selenium能够更真实的模拟浏览器，但缺点是
1. 需要安装浏览器。
2. 需要对应的浏览器驱动插件。

通常情况下，Selenium在操作的时候会弹出浏览器界面。这导致：
1. 后台测试时也会弹出一个浏览器界面。
2. 无法在非UI平台下工作，比如Linux Server。

一开始，PhantomJs就是为了这个需求而诞生的，它封装了一个webkit浏览器内核来提供无图形操作。

但现在，PhantomJs已经被废弃了：
> UserWarning: Selenium support for PhantomJS has been deprecated, please use headless versions of Chrome or Firefox instead

Chrome和Firefox都提供了headless无图形界面模式，用来支持无图形操作。

#### Selenium + Headless Chrome

安装支持Headless的Chrome浏览器。

**Solution Install Google Chrome on Ubuntu 16.04 no GUI**
```
Solution Install Google Chrome on Ubuntu 16.04 no GUI
Installation is straightforward and can be done with several commands:

sudo apt-get update
sudo apt-get install -y libappindicator1 fonts-liberation
cd temp
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo dpkg -i google-chrome*.deb
This is going to install dependencies required for chrome to be installed.

In case of error related to unmet dependencies you can do:

sudo apt-get -f install
sudo dpkg --configure -a
As last option you can try with:

sudo apt-get -u dist-upgrade
and then to try to installed it again.

Other optional dependencies :

sudo apt-get -y install dbus-x11 xfonts-base xfonts-100dpi xfonts-75dpi xfonts-cyrillic xfonts-scalable
```

使用

```python
chrome_options = chrome.options.Options()
chrome_options.add_argument('--headless')
chrome_options.add_argument('--disable-gpu')
# chrome_options.add_argument('--remote-debugging-port=9222')
# chrome_options.binary_location = r'C:\Users\hldh214\AppData\Local\Google\Chrome\Application\chrome.exe'

g_browser = webdriver.Chrome(chrome_options=chrome_options)
g_wait = WebDriverWait(g_browser, 10) # wait for at most 10s
```

这样启动后，脚本就不再弹出浏览器窗口。

#### Selenium + Headless Firefox

未了解

#### Selenium + PhantomJs

有官网，详细未了解

#### Selenium + Xvfb

Xvfb是`virtual framebuffer X server for X Version 11`
简而言之，Xvfb可以直接处理窗口的图形化功能，但不会把图形输出到屏幕。  
Xvfb通常是Linux下XWindow的虚拟图形设备，所以Windows或Mac系统上不知道可否运行，未了解。
