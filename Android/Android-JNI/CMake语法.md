## Android NDK 开发：CMake 使用

转自<http://cfanr.cn/2017/08/26/Android-NDK-dev-CMake-s-usage/>

1. 前言
当在做 Android NDK 开发时，如果不熟悉用 CMake 来构建，读不懂 CMakeLists.txt 的配置脚本，很容易就会踩坑，遇到编译失败，一个很小的配置问题都会浪费很多时间。所谓工欲善其事必先利其器，学习 NDK 开发还是要大致了解 CMake 的基本语法和配置的。下面文章是根据 CMake 实践手册 做的一些简短笔记，具体说得不够详细的地方，可以查看手册。

2. CMake 是什么？
CMake 是一个开源的跨平台自动化构建系统。官网地址：CMake

2.1CMake 的特点
1）开放源代码，使用类 BSD 许可发布。
2）跨平台，并可生成 native 编译配置文件，在 Linux/Unix 平台，生成 makefile，在
Mac 平台，可以生成 xcode，在 Windows 平台，可以生成 MSVC 的工程文件。
3）能够管理大型项目；
4）简化编译构建过程和编译过程。Cmake 的工具链非常简单：cmake+make。
5）高效率；
6）可扩展，可以为 cmake 编写特定功能的模块，扩充 cmake 功能。
2.2 使用建议
1）如果你没有实际的项目需求，那么看到这里就可以停下来了，因为 CMake 的学习过程就是实践过程，没有实践，读的再多几天后也会忘记；
2）如果你的工程只有几个文件，直接编写 Makefile 是最好的选择；（那得学习 make 命令和熟悉 Makefile 的构建规则，这是另外一回事了）
3）如果使用的是 C/C++/Java 之外的语言，请不要使用 CMake；
4）如果你使用的语言有非常完备的构建体系，比如 java 的 ant，也不需要学习 cmake；
5）如果项目已经采用了非常完备的工程管理工具，并且不存在维护问题，没有必要迁移到CMake

CMakeLists.txt 文件是 CMake 的构建定义文件。如果工程存在多个目录，需要在每个要管理的目录都添加一个 CMakeLists.txt 文件。

3. CMake 命令
CMake 命令行格式有很多种，这里只介绍一种比较常用的

cmake [<options>] (<path-to-source> | <path-to-existing-build>)
options 为可选项，为空时，构建的路径为当前路径。
options 的值，可以通过输入cmake --help 或到官方文档CMake-cmake查看，比如：
-G <generator-name> 是指定构建系统的生成器，当前平台所支持的 generator-name 也可以通过cmake --help查看。（options 一般默认为空就好，这里不做过多介绍）

path-to-source和path-to-existing-build二选一，分别表示 CMakeLists.txt 所在的路径和一个已存在的构建工程的目录

cmake .表示构建当前目录下 CMakeLists.txt 的配置，并在当前目录下生成 Makefile 等文件；【属于内部构建】
cmake ..表示构建上一级目录下 CMakeLists.txt 的配置，并在当前目录下生成 Makefile 等文件；
cmake [参数] [指定进行编译的目录或存放Makefile文件的目录] [指定CMakeLists.txt文件所在的目录] 【属于外部构建】
附：内部构建（in-source build）与外部构建（out-of-source build）
内部构建生成的临时文件可能比源代码还要多，非常影响工程的目录结构和可读性。而CMake 官方建议使用外部构建，外部构建可以达到将生成中间产物与源代码分离。

4. Hello World CMake
注：以下 Mac 平台

安装 CMake （Windows 可以到官网下载安装包安装 Download | CMake）

brew install cmake
brew link cmake
cmake -version #检验是否安装成功，显示对应 CMake 版本号即表示安装成功
创建一个 CMake/t1 目录，并分别编写 main.c 和 CMakeLists.txt （CMakeLists.txt 是 CMake 的构建定义文件）

#include <stdio.h>
int main()
{
    printf(“Hello World from CMake!\n”);
    return 0;
}
PROJECT(HELLO)
SET(SRC_LIST main.c)
MESSAGE(STATUS "This is BINARY dir " ${HELLO_BINARY_DIR})  #终端打印的信息
MESSAGE(STATUS "This is SOURCE dir "${HELLO_SOURCE_DIR})
ADD_EXECUTABLE(hello ${SRC_LIST})
这里如果直接输入cmake .开始构建，属于内部构建。建议采用外部构建的方法，先建一个 build 文件夹，进入 build 文件夹在执行cmake ..。构建后出现很多 log 包含以下，说明构建成功，并且目录下会生成CMakeFiles, CMakeCache.txt, cmake_install.cmake, Makefile 等文件

-- This is BINARY dir /Users/cfanr/AndroidStudioProjects/NDK/CMake/t1
-- This is SOURCE dir /Users/cfanr/AndroidStudioProjects/NDK/CMake/t1
-- Configuring done
-- Generating done
-- Build files have been written to: /Users/cfanr/AndroidStudioProjects/NDK/CMake/t1
然后在执行 make命令，会生成 main.c 对应的可执行文件hello，并会出现以下彩色的 log

[ 50%] Building C object CMakeFiles/hello.dir/main.c.o
[100%] Linking C executable hello
[100%] Built target hello
最后执行 ./hello 会打印输出：
Hello World from CMake!

5. CMake 的基本语法规则
使用星号 # 作为注释；
变量使用 ${} 方式取值，但是在 IF 控制语句中是直接使用变量名；
指令名(参数1 参数2 …)，其中参数之间使用空格或分号隔开；
指令与大小写无关，但参数和变量是大小写相关的；
6. CMake 的常用指令
注：指令与大小写无关，官方建议使用大写，不过 Android 的 CMake 指令是小写的，下面为了便于阅读，采取小写的方式。

6.1 project 指令
语法：project( [CXX] [C] [Java])
这个指令是定义工程名称的，并且可以指定工程支持的语言（当然也可以忽略，默认情况表示支持所有语言），不是强制定义的。例如：project(HELLO)
定义完这个指令会隐式定义了两个变量：
<projectname>_BINARY_DIR和<projectname>_SOURCE_DIR
由上面的例子也可以看到，MESSAGE 指令有用到这两个变量；

另外 CMake 系统还会预定义了 PROJECT_BINARY_DIR 和 PROJECT_SOURCE_DIR 变量，它们的值和上面两个的变量对应的值是一致的。不过为了统一起见，建议直接使用PROJECT_BINARY_DIR 和PROJECT_SOURCE_DIR，即使以后修改了工程名字，也不会影响两个变量的使用。

6.2 set 指令
语法：set(VAR [VALUE])
这个指令是用来显式地定义变量，多个变量用空格或分号隔开
例如：set(SRC_LIST main.c test.c)

注意，当需要用到定义的 SRC_LIST 变量时，需要用${var}的形式来引用，如：${SRC_LIST}
不过，在 IF 控制语句中可以直接使用变量名。

6.3 message 指令
语法：message([SEND_ERROR | STATUS | FATAL_ERROR] “message to display” … )
这个指令用于向终端输出用户定义的信息，包含了三种类型：
SEND_ERROR，产生错误，生成过程被跳过；
STATUS，输出前缀为—-的信息；（由上面例子也可以看到会在终端输出相关信息）
FATAL_ERROR，立即终止所有 CMake 过程；

6.4 add_executable 指令
语法：add_executable(executable_file_name [source])
将一组源文件 source 生成一个可执行文件。 source 可以是多个源文件，也可以是对应定义的变量
如：add_executable(hello main.c)

6.5 cmake_minimun_required(VERSION 3.4.1)
用来指定 CMake 最低版本为3.4.1，如果没指定，执行 cmake 命令时可能会出错

6.6 add_subdirectory 指令
语法：add_subdirectory(source_dir [binary_dir] [EXCLUDE_FROM_ALL])
这个指令用于向当前工程添加存放源文件的子目录，并可以指定中间二进制和目标二进制存放的位置。EXCLUDE_FROM_ALL参数含义是将这个目录从编译过程中排除。

另外，也可以通过 SET 指令重新定义 EXECUTABLE_OUTPUT_PATH 和 LIBRARY_OUTPUT_PATH 变量来指定最终的目标二进制的位置(指最终生成的 hello 或者最终的共享库，不包含编译生成的中间文件)
set(EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/bin)
set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/lib)

6.7 add_library 指令
语法：add_library(libname [SHARED | STATIC | MODULE] [EXCLUDE_FROM_ALL] [source])
将一组源文件 source 编译出一个库文件，并保存为 libname.so (lib 前缀是生成文件时 CMake自动添加上去的)。其中有三种库文件类型，不写的话，默认为 STATIC:

SHARED: 表示动态库，可以在(Java)代码中使用 System.loadLibrary(name) 动态调用；
STATIC: 表示静态库，集成到代码中会在编译时调用；
MODULE: 只有在使用 dyId 的系统有效，如果不支持 dyId，则被当作 SHARED 对待；
EXCLUDE_FROM_ALL: 表示这个库不被默认构建，除非其他组件依赖或手工构建
#将compress.c 编译成 libcompress.so 的共享库
add_library(compress SHARED compress.c)
add_library 命令也可以用来导入第三方的库:
add_library(libname [SHARED | STATIC | MODULE | UNKNOWN] IMPORTED)
如，导入 libjpeg.so

add_library(libjpeg SHARED IMPORTED)
导入库后，当需要使用 target_link_libraries 链接库时，可以直接使用该库

6.8 find_library 指令
语法：find_library( name1 path1 path2 …)
VAR 变量表示找到的库全路径，包含库文件名 。例如：

find_library(libX  X11 /usr/lib)
find_library(log-lib log)  #路径为空，应该是查找系统环境变量路径
6.9 set_target_properties 指令
语法: set_target_properties(target1 target2 … PROPERTIES prop1 value1 prop2 value2 …)
这条指令可以用来设置输出的名称（设置构建同名的动态库和静态库，或者指定要导入的库文件的路径），对于动态库，还可以用来指定动态库版本和 API 版本。
如，set_target_properties(hello_static PROPERTIES OUTPUT_NAME “hello”)
设置同名的 hello 动态库和静态库：

set_target_properties(hello PROPERTIES CLEAN_DIRECT_OUTPUT 1)
set_target_properties(hello_static PROPERTIES CLEAN_DIRECT_OUTPUT 1)
指定要导入的库文件的路径

add_library(jpeg SHARED IMPORTED)
#注意要先 add_library，再 set_target_properties
set_target_properties(jpeg PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI}/libjpeg.so)
设置动态库 hello 版本和 API 版本：
set_target_properties(hello PROPERTIES VERSION 1.2 SOVERSION 1)

和它对应的指令：
get_target_property(VAR target property)
如上面的例子，获取输出的库的名字

get_target_property(OUTPUT_VALUE hello_static OUTPUT_NAME)
message(STATUS "this is the hello_static OUTPUT_NAME:"${OUTPUT_VALUE})
6.10 include_directories 指令
语法：include_directories([AFTER | BEFORE] [SYSTEM] dir1 dir2…)
这个指令可以用来向工程添加多个特定的头文件搜索路径，路径之间用空格分割，如果路径中包含了空格，可以使用双引号将它括起来，默认的行为是追加到当前的头文件搜索路径的
后面。

6.11 target_link_libraries 指令
语法：target_link_libraries(target library library2…)
这个指令可以用来为 target 添加需要的链接的共享库，同样也可以用于为自己编写的共享库添加共享库链接。
如：

#指定 compress 工程需要用到 libjpeg 库和 log 库
target_link_libraries(compress libjpeg ${log-lib})
同样，link_directories(directory1 directory2 …) 可以添加非标准的共享库搜索路径。

还有其他 file、list、install 、find_ 指令和控制指令等就不介绍了，详细可以查看手册。

7. CMake 的常用变量
7.1 变量引用方式
使用 ${} 进行变量的引用。不过在 IF 等语句中，可以直接使用变量名而不用通过 ${} 取值

7.2 自定义变量的方式
主要有隐式定义和显式定义两种。隐式定义，如 PROJECT 指令会隐式定义_BINARY_DIR 和 _SOURCE_DIR
而对于显式定义就是通过 SET 指令来定义。如：set(HELLO_SRC main.c)

7.3 CMake 常用变量
1）CMAKE_BINARY_DIR, PROJECT_BINARY_DIR, _BINARY_DIR
这三个变量指代的内容都是一样的，如果是 in-source 编译，指的是工程顶层目录，如果是 out-of-source 编译，指的是工程编译发生的目录。

2）CMAKE_SOURCE_DIR, PROJECT_SOURCE_DIR, _SOURCE_DIR
这三个变量指代的内容也是一样的，不论哪种编译方式，都是工程顶层目录。

3）CMAKE_CURRENT_SOURCE_DIR
当前处理的 CMakeLists.txt 所在的路径

4）CMAKE_CURRENT_BINARY_DIR
如果是 in-source 编译，它跟 CMAKE_CURRENT_SOURCE_DIR 一致，如果是 out-of-source 编译，指的是 target 编译目录。
使用 ADD_SUBDIRECTORY(src bin)可以修改这个变量的值；
而使用 SET(EXECUTABLE_OUTPUT_PATH < 新路径>) 并不会对这个变量造成影响，它仅仅修改了最终目标文件存放的路径。

5）CMAKE_CURRENT_LIST_FILE
输出调用这个变量的 CMakeLists.txt 的完整路径

6）CMAKE_CURRENT_LIST_LINE
输出这个变量所在的行

7）CMAKE_MODULE_PATH
这个变量用来定义自己的 CMake 模块所在的路径。如果你的工程比较复杂，有可能会自己
编写一些 cmake 模块，这些 cmake 模块是随你的工程发布的，为了让 cmake 在处理
CMakeLists.txt 时找到这些模块，你需要通过 SET 指令，将自己的 cmake 模块路径设
置一下。
比如 SET(CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}/cmake)
这时候你就可以通过 INCLUDE 指令来调用自己的模块了。

８）EXECUTABLE_OUTPUT_PATH 和 LIBRARY_OUTPUT_PATH
分别用来重新定义最终结果的存放目录，前面我们已经提到了这两个变量。

9）PROJECT_NAME
返回通过 PROJECT 指令定义的项目名称。

介绍了那么多，可以通过一些小练习来巩固下，参考：cmake 学习笔记(一) - dbzhang800- CSDN博客

代码地址：NdkSample/CMake Sample

8. Android CMake 的使用
8.1 CMakeList.txt 的编写
再回归到 Android NDK 开发中 CMake 的使用，先看一个系统生成的 NDK 项目的 CMakeLists.txt 的配置：( 去掉原有的注释)

#设置编译 native library 需要最小的 cmake 版本
cmake_minimum_required(VERSION 3.4.1)
#将指定的源文件编译为名为 libnative-lib.so 的动态库
add_library(native-lib SHARED src/main/cpp/native-lib.cpp)
#查找本地 log 库
find_library(log-lib log)
#将预构建的库添加到自己的原生库
target_link_libraries(native-lib ${log-lib} )
复杂一点的 CMakeLists，这是一个本地使用 libjpeg.so 来做图片压缩的项目

cmake_minimum_required(VERSION 3.4.1)
#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
#指定要引用的libjpeg.so的头文件目录
set(LIBJPEG_INCLUDE_DIR src/main/cpp/include)
include_directories(${LIBJPEG_INCLUDE_DIR})
#导入libjpeg动态库 SHARED；静态库为STATIC
add_library(jpeg SHARED IMPORTED)
#对应so目录，注意要先 add_library，再 set_target_properties）
set_target_properties(jpeg PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI}/libjpeg.so)
add_library(compress SHARED src/main/cpp/compress.c)
find_library(graphics jnigraphics)
find_library(log-lib log)
#添加链接上面个所 find 和 add 的 library
target_link_libraries(compress jpeg ${log-lib} ${graphics})
8.2 配置 Gradle
简单的配置如下，至于 cppFlags 或 cFlags 的参数有点复杂，一般设置为空或不设置也是可以的，这里就不过多介绍了

android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    defaultConfig {
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        externalNativeBuild {
            cmake {
                // Passes optional arguments to CMake.
                arguments "-DANDROID_ARM_NEON=TRUE", "-DANDROID_TOOLCHAIN=clang"
                // Sets optional flags for the C compiler.
                cFlags "-D_EXAMPLE_C_FLAG1", "-D_EXAMPLE_C_FLAG2"
                // Sets a flag to enable format macro constants for the C++ compiler.
                cppFlags "-D__STDC_FORMAT_MACROS"
                //生成.so库的目标平台
                abiFilters 'x86', 'x86_64', 'armeabi', 'armeabi-v7a',
                   'arm64-v8a'
            }
        }
    }
      //配置 CMakeLists.txt 路径
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
}
对于 CMake 的知识点其实还是有很多的，这里只是简单介绍了 CMake 的基本语法规则和使用方法，了解了这些，遇到问题应该也能快速定位到原因，找出解决的版本，就算不记得一些指令，也通过查找文档解决。能达到这种程度，对于 Android NDK 开发来说，掌握这些也足够了吧。

参考：

CMake 实践手册
CMake语法学习笔记 - 亚特兰蒂斯 - CSDN博客
cmake 学习笔记(一) - dbzhang800 - CSDN博客
向您的项目添加 C 和 C++ 代码 | Android Studio 官方文档
扩展阅读：

Android Studio NDK CMake 指定so输出路径以及生成多个so的案例与总结 - zhangbh的专栏 - CSDN博客
如果项目中需要将功能模块生成不同的 so 库，可以参考下文章的例子
Make 命令教程 - 阮一峰的网络日志
学习 make 命令可以了解 Makefile 构建规则
make makefile cmake qmake都是什么，有什么区别？ - 知乎
make用来执行Makefile；Makefile是类unix环境下(比如Linux)的类似于批处理的”脚本”文件；cmake是跨平台项目管理工具，它用更抽象的语法来组织项目，是一个项目管理工具，是用来执行CMakeLists.txt；qmake是Qt专用的项目管理工具，用来处理*.pro工程文件。Makefile的抽象层次最低，cmake和qmake在Linux等环境下最后还是会生成一个Makefile。cmake和qmake支持跨平台，cmake的做法是生成指定编译器的工程文件，而qmake完全自成体系。
