## Android文件共享

```java
/**
 * 写入序列化对象
 */
public void wirte() {
    Book book = new Book();
    book.bookId = 1;
    book.bookName = "si";
    try {

        // 构造序列化输出字节流
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(PATH));
        // 序列化对象
        oos.writeObject(book);
        // 关闭流
        oos.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println(book);
}
```

```java
public void read() {
	Book book = null;
	try {
	    // 创建序列化读取字节流
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
	            MainActivity.PATH));
	    // 反序列化（读取）对象
	    book = (Book) ois.readObject();
	    // 关闭流
	    ois.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	System.out.println(book);
}
```
