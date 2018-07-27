## Python原生request

#### 实例

1、开启request调试

```python
httplib.HTTPConnection.debuglevel = 1
logging.basicConfig()
logging.getLogger().setLevel(logging.DEBUG)
requests_log = logging.getLogger("requests.packages.urllib3")
requests_log.setLevel(logging.DEBUG)
requests_log.propagate = True
```

2、创建`requests`和`session`

```python
def __init__(self):
    self.session = requests.Session()
```

3、发起`multipart/form-data;`请求

注意：

1. formData的数据，要么是字符串，要么是二进制。不存在整数／Object。如果看到`[object Object]`，这是一个对象`toString()`的输出，说明这个对象没有做序列化，也没有重写`toString()`。后台将拿不到这个对象的数据。

2. 如果数据是二进制，数据段在`Chrome`调试中是空白一片，但其实只是无法输出而已。打开了`request`调试，你就能看到二进制数据段。

```python

formData = {
    "account": (None, "[object Object]"),
    "meta": (None, "[object Object]"),

    "status": (None, "send"),
    "sender_title": (None, ""),
    "type": (None, "image"),
    "image": (None, ""),
    "from": (None, "pc"),

    "id": (None, '1529111371.312002'),
    "create_time": (None, "2018-06-16 12:27"),
    "timestamp": (None, '1529111381.047179'),
    "ts": (None, '1529111381.047179'),
}

# return the Pass path else return NONE
def post(self, path):
    (fileDir, longname, shortname, fileExt) = self.get_filePath_fileName_fileExt(path)
    if longname.startswith("."):
        # ignore .DS_Store
        print "ignore:" + path
        return None

    # ['id'] = (NONE, "12345")
    # name =（filename, value, contentType)
    # value为字符串或二进制
    self.formData['image'] = (longname, open(path,'rb'), 'image/jpeg')

    timeString = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')
    # 保留6位小数的时间戳，默认str会截剩2位
    id = str("{0:10f}").format(time.time())
    time.sleep(0.020)
    timestamp = str("{0:10f}").format(time.time())
    self.formData['id'] = (None, str(id))
    self.formData['create_time'] = (None, timeString)
    self.formData['timestamp'] = (None, timestamp)
    self.formData['ts'] = (None, timestamp)

    # 使用流方式，都是字符串！
    files = self.formData
    print self.formData

    self.post_headers['authorization'] = self.account['token']
    response = self.session.post(url=URL_SEND_MESSAGE,
                              files=files,
                              headers=self.post_headers)
    print response.text
    result = response.json()
    print result
    if result['code'] == 0:
        return path
    return None;
```

以上，模拟了以下数据，并且会自动设置`headers`中的`Content-Type`为`multipart/form-data;`，和设置`boundary`，且会计算其长度设置到`Content-Length`。

所以这种方式，不需要额外设置`Headers`。

```java
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="id"

1529156667.142011
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="type"

image
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="account"

[object Object]
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="sender_title"


------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="timestamp"

1529156666.925072
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="create_time"

2018-06-16 21:44
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="local_id"

undefined
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="image"; filename="05.jpg"
Content-Type: image/jpeg


------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="from"

pc
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="status"

send
------WebKitFormBoundaryQN7pNaXkRxKofFt0
Content-Disposition: form-data; name="ts"

1529156666.925072
------WebKitFormBoundaryQN7pNaXkRxKofFt0--
```

参考<https://blog.csdn.net/j_akill/article/details/43560293>
