## 最新Android版本对照表

具体对照官方网址，Android版本和API-Level对照：
<https://developer.android.com/studio/releases/platforms>

对应的Android BuildTool版本对照表：
<https://developer.android.com/studio/releases/build-tools>

你也可以参考wiki，查看Android的历史版本和对应的修改：
<https://zh.wikipedia.org/wiki/Android%E6%AD%B7%E5%8F%B2%E7%89%88%E6%9C%AC>


下面你可以发现，。

Android的一个版本，都可能对应多个API-LEVEL。比如，Android O的API-LEVEL可能是25，24。

Android每一次小版本升级或者大版本升级，都会递增一次API-LEVEL。所以一个大版本，通常包含多个API-LEVEL，因为过程有多个小版本。


|  命名            | Android版本        | API-Level |
| :-------------  | :-------------    | :------------- |
| Android P       | Android 9.0       |  28        |
| Android O       | Android 8.1       |  27        |
| Android O       | Android 8.0       |  26        |
| Android N       | Android 7.1       |  25        |
| Android N       | Android 7.0       |  24        |
| Android M       | Android 6.0       |  23        |
| Android L       | Android 5.1       |  22        |
| Android L       | Android 5.0       |  21        |
| Android K       | Android 4.4W      |  20        |
| Android K       | Android 4.4       |  19        |
| Android J       | Android 4.3       |  18        |
| Android J       | Android 4.2       |  17        |
| Android J       | Android 4.1       |  16        |
| Android I       | Android 4.0.3     |  15        |
| Android I       | Android 4.0       |  14        |

由表可以看出：

1. Android的命名遵从字母表顺序，从`I`到`P`。最初的Android版本是`C`，当前最新的是`P`。

2. 旧的4.x由于有多个版本，所以4.x跨越了几个字母，从`I`到`K`。但从5.0开始，我们可以认为，一个字母代表一个**大版本**。所以我们只需要记住，`O`表示`8.x`，然后往前递减，`N`就是`7.0`。(可以帮助记忆)

3. API-LEVEL是跟随**小版本**进行更新的。这个记忆比较麻烦，因为你不知道7.x有多少个小版本。所以只能强行记忆了。(7.x和8.x各只有两个版本，6.x只有一个版本)（记忆突破口可以是6.0，只有一个版本号，是API-23，所以API24肯定是7.0!)
