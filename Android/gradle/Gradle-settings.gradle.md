
### 项目自定义路径

先`include`引入项目，后指定项目的路径。反过来会编译不过！！
```java
include ':LibFBWriter'
project(':LibFBWriter').projectDir = new File('../LibFBWriter')
```
