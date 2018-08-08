## Android Parcelable 接口实现

```java
public class IAction implements Parcelable {

    private Action mAction;

    protected IAction(Parcel in) {
        String name = in.readString();
        mAction = Action.create(name);
        mAction.readFromParcel(in);
    }

    IAction(@NonNull Action action) {
        mAction = action;
    }

    static final Creator<IAction> CREATOR = new Creator<IAction>() {
        @Override
        public IAction createFromParcel(Parcel in) {
            return new IAction(in);
        }

        @Override
        public IAction[] newArray(int size) {
            return new IAction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mAction != null) {
            dest.writeString(mAction.getType().name());
            mAction.writeToParcel(dest, flags);
        }
    }

    public Action getAction() {
        return mAction;
    }
}

```

1、要实现以下接口：

##### 内容描述接口

`public int describeContents()`

有两个返回值：`0` 或 `CONTENTS_FILE_DESCRIPTOR`

如果是实体类，则直接返回0；如果是文件描述符`FileDescriptor`，则需要返回`CONTENTS_FILE_DESCRIPTOR`。也即是说，后者是专门为文件描述符序列化保留的，告诉系统，当前要反序列化的对象是特殊的。

<https://stackoverflow.com/questions/4076946/parcelable-where-when-is-describecontents-used/4914799#4914799>

##### 序列化接口
`public void writeToParcel(Parcel dest, int flags)`

* int flags

默认为`0`，在以下场景下为`PARCELABLE_WRITE_RETURN_VALUE`(值为1)。(注：**这个值是由系统传递过来的！**)

* 作为aidl中的返回值
* 作为aidl中的out类型
* 作为aidl中的inout类型

Flag for use with writeToParcel: the object being written is a return value, that is the result of a function such as "Parcelable someFunction()", "void someFunction(out Parcelable)", or "void someFunction(inout Parcelable)". Some implementations may want to release resources at this point.

用于表明，当前已经是返回值了，所以没办法在返回后对资源进行释放。所以要释放资源，最迟在这里要释放了。

这个要通俗的解释一下了。为什么系统需要在特定的情况下，给你传递一个`PARCELABLE_WRITE_RETURN_VALUE`?

原因上面英文段已经提及，就是这些特定的情况下，已经是一个函数调用的结尾。比如，如果序列化是一个包含`资源`的对象B，当`return Parcel(B)`语句执行后，B对象执行序列化，就到了`B:writeToParcel`，`B:writeToParcel`执行完，函数就结束了，但是对象B的`资源`还没有释放。

系统通过在这些最后结点发生的序列化中，添加一个`PARCELABLE_WRITE_RETURN_VALUE`来告诉开发者，当前已经是返回值结点了，没有释放的资源可以释放了，再不释放就没机会释放啦。

##### 数组接口

提供一个静态的`CREATOR`。用来做数组序列化转换。

`static final Creator<IAction> CREATOR = new Creator<IAction>()`

基本默认都是下面这样处理：

```java
@Override
public IAction createFromParcel(Parcel in) {
    return new IAction(in);
}

@Override
public IAction[] newArray(int size) {
    return new IAction[size];
}
```

3. 提供一个参数为`Parcel`的对象构造方法。这不是必须的，只是为了方便反序列化：

```java
protected IAction(Parcel in) {
    String name = in.readString();
    mAction = Action.create(name);
    mAction.readFromParcel(in);
}
```

反序列化的时候，只需要简单构建对象即可完成反序列化：`return new IAction(in)`。
