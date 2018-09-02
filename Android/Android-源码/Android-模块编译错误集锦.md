## Android 模块编译错误集锦

### No rule to make target libbinder.so

```
target  C++: libinputflinger <= frameworks/native/services/inputflinger/InputWindow.cpp
make: *** No rule to make target `out/target/product/rk3399/obj/lib/libbinder.so', needed by `out/target/product/rk3399/obj/SHARED_LIBRARIES/libinputflinger_intermediates/LINKED/libinputflinger.so'.  Stop.
make: Leaving directory `/codes/jokin/3399_jokin'
```

现象：系统已经构建成功，但构建模块的时候，还提示找不到`libbinder.so`
原因：修改了系统构建目标的目录。虽然构建为`rk3399`，但生成的目标却在`out/target/product/3399/`下，所以找不到。
解决：既然知道是路径的问题，修复路径即可。之所以路径错误，是因为构建系统时，选择的lunch是`3399-userdebug`，而构建模块时，选择的lunch为`rk3399-userdebug`。所以，构建模块就会在`out/target/product/rk3399`目录下查找。修复也很简单，lunch选择`3399-userdebug`然后进行模块构建，此时构建模块就会索引`out/target/product/3399`目录。

正常的系统构建成功后，`out/target/product`目录下，`obj/lib`和`obj/SHARED_LIBRARIES/libxxx/LINKED`路径下是有一个`libbinder.so`的。

```shell
~/out/target/product/3399/obj/lib$ ls libbinder.so
libbinder.so

~/out/target/product/3399/obj/SHARED_LIBRARIES/libbinder_intermediates/LINKED$ ls
libbinder.so
```

错误就是找不到这个so。

修复后构建，从构建结果看不到构建出的so文件是哪个，也不知道构建出的so文件在哪个目录。所以只能靠查阅相关资料找到所生成的so文件。

比如`InputDispatcher.cpp`修改后，影响的是`system/lib/libinputflinger.so`。

构建后的目标，会直接替换`out/target/product`下面对应的`system/lib/`下的so文件；所以需要拿到构建后的so目标，需要在`out/target/product/xxx/system/lib/`目录下查找。另外，通常还有一个`lib64`。通过查看文件的生成时间来判断是否是新构建的目标。
