## 目的
app开发测试过程，可能会发生以下各种情况：

1. 测试机器上，app会被不断替换，需要信息来定位当前app的状态。
1. apk信息仅附加在命名上，安装后无法跟踪。
1. 版本号在开发过程没有递增，并不清楚当前测试的是哪个版本，也不知道当前出现了bug的是哪个版本。
1. 虽然版本号递增了，但并不清楚版本号对应的功能点或时间节点，需要查看构建历史才能定位，如果机器远离办公室，定位过程尤为繁琐，甚至可能需要VPN。
1. 版本号需要和commit-id绑定，才能跟踪当前版本号对应的提交节点。
1. commit-id仍旧不够直观，在外需要依赖VPN。

以上问题，都会导致测试过程出现问题但无法立即定位问题，有时是因为使用了旧的版本；有时是因为中途版本被替换了；有时它确实是个Bug。但无论如何，定位问题不仅浪费了测试的时间也降低了开发的效率。

## 给app埋入构建信息
在app构建时，埋入构建的时间／地点／参数／版本信息能有助于分析问题。

比如，本地构建与Jenkins上的构建，不同的构建使用的环境和参数不一样，构建出来的apk也很有可能不一样。具体可以有：

1. Debug／Release
2. 非混淆／混淆
3. 本地构建／Jenkins构建
4. 构建时间戳
5. 版本号
6. 渠道号

## 给app埋入提交日志
除了埋入commit-id，埋入适当的日志也是可以更加快捷的定位问题。

比如，看到提交日志信息和最近提交的不一致，则可以立即断定版本是旧的。通过commit-id／版本号等无法做到立即定位。

提交日志具体可以有：
1. 当前构建对应的commit的 id／hash
2. 当前构建对应的commit的 提交人（建议拼音）
3. 当前构建对应的commit的 提交时间戳
4. 当前构建对应的commit的 具体日志信息

安全意识，提交人建议使用拼音，不要添加邮件。

对于git，对应的命令行：
`git rev-list --abbrev-commit --max-count=1 --format=%an_%ar_%ai_%n%B%n HEAD`

> git rev-list HEAD 查看log列表
--abbrev-commit 使用短hash
--max-count 显示commit数量
--format 格式化输出（具体%an %ar %B 等参考git log --help）

输出：
```shell
commit b1ab959
jokin_18 hours ago_2018-01-05 17:21:39 +0800_
Merge branch 'f/add_app_info' into 'develop'

[feature] 添加Trick来显示App详细信息
```

## 使用gradle给app埋入自定义信息

1. 在gradle.properties里

```java
MAJOR_VERSION=0
MINOR_VERSION=1
```

2. 然后在build.gradle里

```javascript
def genVersionName() {
    String versionName = System.getenv("MH_VERSION_NAME")
    if (versionName != null) {
        return versionName
    } else {
        return "U."+MAJOR_VERSION+"."+MINOR_VERSION
    }
}

def genVersionCode() {
    String versionCode = System.getenv("MH_VERSION_CODE")
    if (versionCode != null) {
        return Integer.parseInt(versionCode)
    } else {
        return 1
    }
}

def getBuildDate() {
    String date = new Date().format('yyyy-MM-dd HH:mm:ss')
    println date
    return date
}

def getBuildFrom() {
    String tag = System.getenv("BUILD_TAG")
    boolean inJenkins = tag != null && tag.contains("jenkins")
    if (inJenkins) {
        return "Jenkins"
    } else {
        return "Local"
    }
}

def getGitCommit() {
    def stdout = new ByteArrayOutputStream()
    exec {
        commandLine 'git', 'rev-list', '--abbrev-commit', '--max-count=1', '--format=%an_%ar_%ai_%n%B%n', 'HEAD'
        standardOutput = stdout
    }
    // 转义字符转换
    // String commit = "'\'\\a\\sb\\c\\bdfasdf\\/f/d/e\"\n\n\n\n\r\r\r\r\b\b\t\t\f\f\\'''''''\''"
    String commit = stdout.toString().trim();
    commit = commit.replace('\\', '\\\\').replace('\n', '\\n').replace('\r', '\\r')
            .replace('\b', '\\b').replace('\t', '\\t').replace('\f', '\\f').replace('"', '\\"')
    println commit
    return commit
}
```

3. 然后在defaultConfig里

```
defaultConfig {
        versionCode genVersionCode()
        versionName genVersionName()
        buildConfigField "String", "BuildDate", "\"${getBuildDate()}\""
        buildConfigField "String", "BuildFrom", "\"${getBuildFrom()}\""
        buildConfigField "String", "GitCommit", "\"${getGitCommit()}\""
}
```

4. 同步后，将会生成BuildConfig.java

```java
public final class BuildConfig {
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  public static final String BUILD_TYPE = "debug";
  public static final String FLAVOR = "platform";
  public static final int VERSION_CODE = 1;
  public static final String VERSION_NAME = "U.0.1";
  // Fields from default config.
  public static final String BuildDate = "2018-01-06 09:53:37";
  public static final String BuildFrom = "Local";
  public static final String GitCommit = "commit b1ab959\njokin_17 hours ago_2018-01-05 17:21:39 +0800_\nMerge branch 'f/add_app_info' into 'develop'\n\n[feature] 添加Trick来显示App详细信息\n\n";
}
```

5. 使用时，直接BuildConfig.GitCommit访问

```java
    private void showAppInfo() {
        String appInfo = "Version: "+BuildConfig.VERSION_NAME
                + "\nBuild At: "+BuildConfig.BuildDate
                + "\nBuild From: "+BuildConfig.BuildFrom
                + "\n\nLatest Commit: "+BuildConfig.GitCommit;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
```

## 释义
1. 字符串转换为gradle属性值时进行了转义字符的替换。

原因是，gradle的属性值是直接将字符串打印结果作为内容，直接写到BuildConfig.java里面。举个例子。

`String commit = "abc\ndef";`

转换为gradle属性，得到的是

```
"abc
def"
```
这是字符串的打印结果，并非原始的字符串。字符串的换行在BuildConfig.java里是

```java
public final class BuildConfig {
  public static final String GitCommit = "abc
              def";
}
```
**这是非法的字符串定义！将导致编译失败！**

所以，我们需要将打印出的换行符转换为常量定义：

```java
"abc\ndef"
```

所以，将字符串变量转换为常量字符串的格式，才是正确的。问题就变成了：知道一个字符串变量，如何输出其常量定义？只需要对转义字符进行转换即可。

java中转义字符

```java
\a     
\b
\f
\n
\r
\t
\v
\\
\'     （java中常量输出 ' 不需要做转义）
\"
\0
\ddd
\xhh
```

字符转换为对应的转义字符串

```java
'\r'  -> "\\r"      // 要输出\r，是\\r 
'\n'  -> "\\n"      // 要输出\n，是\\n
'\'   -> "\\"       // 要输出\，常量是\\（怎么表示\脚本语言中是'\\'）
'"'   -> "\\\""     // 常量字符串\"，输出的是"，要输出\"，是"\\\""，对于脚本语言，可以是'\\"'
'\''  -> "\\'"      // 要输出\'，常量是"\\'"
以此类推。
```
**注意的是，脚本语言中，' 与 " 都是表示字符串。但两者需要转义的符号不同。**
