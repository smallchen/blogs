## Gesture

触摸事件传递：

    触摸事件统一由 MyViewPager 接收，然后经由 GestureHelper::onTouchEvent 处理。

    缘由：
    1. 当图片放大时，用户可以左右拖动图片。当拖动到达图片尽头的时候，事件应该交由 ViewPager 处理，从而
       拖出上/下一张图片。如果使用 onInterceptTouchEvent(), requestDisallowInterceptTouchEvent()
       或 override ViewPager::canScroll，一旦子控件决定将事件交由 ViewPager 处理，子控件将再也无法获
       得后续事件。于是，比方说，当从左向右拖动已经放大的图片时，一开始图片滑动，拖到尽头后，ViewPager 开
       始显示左边的图片。此时，当我们又从右往左拖动时，ViewPager 往左滑。当左边的图片完全隐藏的时候，我们
       再从右往左滑，此时应该是图片右部拖出（即此时 ViewPager 不应该再处理事件，而应该由 ImageView 处理）。

       故而全程事件都应该由同一个 View 处理。

    2. 如果事件统一由 ImageView 处理，当图片拖动至尽头然后由 ViewPager 处理事件的时候，由于 ImageView
       的位置在滑动过程中被 ViewPager 改变，将会导致 GestureDetector 的 onScroll 回调返回错误的数据。



触摸事件响应：

    触摸事件由 GestureHelper 统一处理。

    其中， XXXGestureDetector 检测具体的滑动、旋转和缩放手势。这些类不涉及具体业务逻辑（包括滑动冲突等）

          XXXXXHelper 作为控制器接收由 XXXGestureDetector 给出的事件，对 View 进行缩放、滑动和平移操作

          具体的缩放、滑动和平移由 View 实现


旋转：

    旋转由 RotationGestureDetector 检测。

    由于旋转基于两指角度变化，当多指滑动时，如果首先按下的两指角度发生了变化，将会触发旋转操作。为防止发生
    误操作，使用 RotationDiscriminator 对此类行为进行跟踪。首先对发生的前 R 个旋转时间进行缓存，当 R 个
    事件发生后还没有发生平移事件时，才认为该动作是旋转。R 值会在运行时进行微调。

    RotationHelper 接收旋转事件并对 View 进行实际的操作。它将旋转角度限制为 90° 的倍数。


缩放：

    缩放使用 Google 的 ScaleGestureDetector。由于它取的系统的 minSpan 在 MAXHUB 存在问题，故而 copy
    了一份出来，仍命名为 ScaleGestureDetector。copy 出来后，由于某些不知名的原因，quick-scale 的计算也
    出了问题，便 disable 了 quick-scale 手势。


平移：

    平移使用 Google 的 GestureDetector。当 ImageView 已经无法滑动时，事件交由 ViewPagerDragger 处理。
    滑动 ViewPager 还是 滑动 ImageView 由 GestureHelperImpl 协调。
