## WindowFramework设计

### 统一接口

举例：普通的ViewGroup与添加到WindowManager中的ViewGroup可以使用统一的接口，实现`Move/Scale/Min/Max`等操作。

思路有两种：

**思路1**：将`WindowManager`中的`ViewGroup`装饰一下，使其与普通`ViewGroup`一样。调用`ViewGroup`的接口，就能控制视图在`WindowManager`中进行刷新。

这种方法，已经实现了覆盖`setLayoutParams()`来对`WindowManager`中的ViewGroup进行刷新。

**不足** 的是：

1.1 没办法覆盖重写`setLeft()/setRight()`等`final`方法。

1.2 ViewGroup关于UI布局的接口有很多，比如`setAlpha()，setScaleX()`等等，如果没进行覆盖，则会导致同样的接口，在`WindowManager`中不能刷新布局。

> 关于这点，其实WindowManager中，关注的只有x, y, gravity, alpha, width, height。

1.3 如果使用`ViewGroup.setX()`来控制`WindowManager.LayoutParams.x`，那么两者的效果是不一致的。因为普通的`ViewGroup.setX()`在刷新后会失效；而`WindowManager.LayoutParams.x`则在刷新后仍旧生效。而且，通过`setX()`来改变`WindowManager.LayoutParams.x`时，不能调用`super.setX()`，因为它不能改变到View的x坐标。

1.4 扩展性不足。因为内部不知道哪些接口是有效的，安全的，毕竟并没有实现`setLeft()/setRight()`等`final`方法。

总结：不可行。

**思路2**：统一成接口`IView`，然后两种类型的ViewGroup都实现`IView`接口来实现统一接口。

**优点** 是：

2.1 扩展性好。后续需要新的特性，增加新接口即可。

2.2 接入成本低。无论使用View还是ViewGroup还是其他View，只需要实现对应的接口，就可以接入。

这种方式简单易用。但个人觉得，没有方式一优雅，可惜方式一由于Android的限制，没法完美实现。

总结：使用思路2的方式来统一接口。然后可以结合思路1来实现接口。
