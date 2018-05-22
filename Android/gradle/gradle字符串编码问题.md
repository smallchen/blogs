#### gradle.properties中文编码错乱

在gradle.properties中定义了

`VersionInfo="修复一些Bug"`

得到的会是一串乱码。

原因，gradle.properties文件的默认编码方式是`ISO-8859-1`，而不是`UTF-8`。所以，在gradle.properties中的中文，不能直接在代码中使用。

如何得到正确的中文呢？

可以在代码中对字符串流进行解码。

```java
def getVersionInfo() {
	println new String("${VersionInfo}".getBytes(), "UTF-8")
	println new String("${VersionInfo}".getBytes("ISO-8859-1"), "UTF-8")
}
输出：
"®æ­£ï¼ä¿®å¤ä¸äºBug"
"修复一些Bug"
```

本来以为`.getBytes()`和`.getBytes(charset)`是一样的。都是返回原始的二进制字节流。但这个例子可以看出，两者是不同的。

```java
public byte[] getBytes(Charset var1) {
	if(var1 == null) {
		throw new NullPointerException();
	} else {
		return StringCoding.encode(var1, this.value, 0, this.value.length);
	}
}

public byte[] getBytes() {
	return StringCoding.encode(this.value, 0, this.value.length);
}


```
可见，字符串的`bytes`也是有编码的。**是以前对字符串字节流的理解出错**。

字符从来都是有编码的。从一开始就有。

以此为例，在`ISO-8859-1`编码的文件中，输入的中文，会被编码成`ISO-8859-1`，所以在字节流角度，就是`ISO-8859-1`格式的二进制流。

所以，我们的理解是，`getBytes`如果能自动识别，则可以知道当前二进制流是什么格式的编码（字符编码是可以识别的）。但可惜的是，没能识别或不支持自动识别，`getBytes`默认是以`UTF-8`编码解析二进制流（跟代码）。

所以，上面例子中，默认的`getBytes`是使用`UTF-8`来解析原始的字节流。

测试一下就知道了：

```java
println new String("${VersionInfo}".getBytes(), "UTF-8")
println new String("${VersionInfo}".getBytes("UTF-8"), "UTF-8")
输出：
"®æ­£ï¼ä¿®å¤ä¸äºBug"
"®æ­£ï¼ä¿®å¤ä¸äºBug"
```
结果一致，说明`getBytes`默认以`UTF-8`来解析字符串流。

所以可以理解，`ISO-8859-1`编码的字节流，你使用`UTF-8`来解析，当然是乱码。解读的时候就已经是乱码，所以后续构建new String时，无论使用什么编码重新encode，结果都会是乱码。

所以正确的做法是，先正确解析好原始字节流。原始文件是`ISO-8859-1`编码的文件，获取原始字节流时，就需要使用`ISO-8859-1`来解码，得到正确的字节流。然后再编码成其它格式的字符串。

如下，先使用`ISO-8859-1`解码，再使用`UTF-8`编码成代码能输出的中文。

```java
def getVersionInfo() {
    return new String("${VersionInfo}".getBytes("ISO-8859-1"), "UTF-8")
}
```

> 注`getDescription()`默认已经有提供，所以只能另起别名，使用`getVersionInfo()`

另：gradle中，可以直接使用变量代替${}，且`""`不是字符串声明，是属于字符串的内容。

```java
DESCRIPTION.getBytes()  等价于 "${DESCRIPTION}".getBytes()
VersionInfo="修复一些Bug" 应该为 VersionInfo=修复一些Bug
```

.完.
