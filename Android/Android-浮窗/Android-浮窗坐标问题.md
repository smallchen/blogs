## Android浮窗坐标问题

1. Android中，浮窗的坐标，与浮窗的`Gravity`相关！比如，`Gravity.Center`的窗口，坐标`x=0, y=0`就是中心位置！！！这会导致基于触摸的拖拉实现有问题！！
