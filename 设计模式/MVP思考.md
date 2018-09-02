<!-- TOC titleSize:2 depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

## 目录(TOC)
- [MVP思考](#mvp思考)
- [MVVM](#mvvm)
- [MVP设计思想](#mvp设计思想)

<!-- /TOC -->

## MVP思考

但MVP也存在一些弊端：

* Presenter（以下简称P）层与View（以下简称V）层是通过接口进行交互的，接口粒度不好控制。**粒度太小，就会存在大量接口的情况，使代码太过碎版化；粒度太大，解耦效果不好。** 同时对于UI的输入和数据的变化，需要手动调用V层或者P层相关的接口，相对来说缺乏自动性、监听性。如果数据的变化能自动响应到UI、UI的输入能自动更新到数据，那该多好！

* MVP是以UI为驱动的模型，更新UI都需要保证能获取到控件的引用，同时更新UI的时候要考虑当前是否是UI线程，也要考虑Activity的生命周期（是否已经销毁等）。

* MVP是以UI和事件为驱动的传统模型，数据都是被动地通过UI控件做展示，但是由于数据的时变性，我们更希望数据能转被动为主动，希望数据能更有活性，由数据来驱动UI。

* V层与P层还是有一定的耦合度。一旦V层某个UI元素更改，那么对应的接口就必须得改，数据如何映射到UI上、事件监听接口这些都需要转变，牵一发而动全身。如果这一层也能解耦就更好了。
复杂的业务同时也可能会导致P层太大，代码臃肿的问题依然不能解决。


## MVVM
View: 对应于Activity和XML，负责View的绘制以及与用户交互。
Model: 实体模型。
ViewModel: 负责完成View与Model间的交互，负责业务逻辑。

MVVM的目标和思想与MVP类似，利用数据绑定(Data Binding)、依赖属性(Dependency Property)、命令(Command)、路由事件(Routed Event)等新特性，打造了一个更加灵活高效的架构。

## MVP设计思想

常见的Listener回调：

```java
class Presenter {
    interface IListener {
        void onDone();
    }

    IListener mListener;
    void setListener(IListener listener) {
        mListener = listener;
    }

    void notifyDone() {
        mListener.onDone();
    }
}

class View {
    Presenter mPresenter = new Presenter();
    {
        mPresenter.setListener(new IListener() {
            void onDone() {
                Log.i(TAG, "Presenter Done!");
            }
        })
    }
}

或

class View implements IListener {
    Presenter mPresenter = new Presenter();
    {
        mPresenter.setListener(this);
    }
    void onDone() {
        Log.i(TAG, "Presenter Done!");        
    }
}
```

Listener回调是外部注册一个通知到黑盒子里。

改为Presenter

```java
class Presenter {
    Listener mListener;
    public Module(IListener listener) {
        mListener = listener
    }

    void notifyDone() {
        mListener.onDone();
    }
}

class View implements IListener {
    Presenter mPresenter = new Presenter(this);

    void onDone() {
        Log.i(TAG, "Presenter Done!");        
    }
}
```

Presenter和Listener其实一样，没什么区别。
