## Android 父类实现Parcelable接口，子类继承父类需要实现的操作


```java
public class A implements Parcelable {

    public String str;

    public boolean is;

    public int count;

    public A() {

    }

    protected A(Parcel in) {
        str = in.readString();
        is = in.readByte() != 0;
        count = in.readInt();
    }

    public static final Creator<A> CREATOR = new Creator<A>() {
        @Override
        public A createFromParcel(Parcel in) {
            return new A(in);
        }

        @Override
        public A[] newArray(int size) {
            return new A[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(str);
        dest.writeByte((byte) (is ? 1 : 0));
        dest.writeInt(count);
    }

    @Override
    public String toString() {
        return "A{" +
                "str='" + str + '\'' +
                ", is=" + is +
                ", count=" + count +
                '}';
    }
}
```

```java
public class B extends A {

    public String bStr;

    public int test;

    public B() {
        super();
    }

    public static final Parcelable.Creator<B> CREATOR = new Parcelable.Creator<B>() {
        public B createFromParcel(Parcel in) {
            // new自己
            return new B(in);
        }

        public B[] newArray(int size) {
            // new自己
            return new B[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        // 调用父类的写操作
        super.writeToParcel(out, flags);

        // 子类实现的写操作
        out.writeString(bStr);
        out.writeInt(test);
    }

    protected B(Parcel in) {
        // 调用父类的读取操作
        super(in);
        // 子类实现的读取操作
        bStr = in.readString();
        test = in.readInt();
    }

    @Override
    public String toString() {
        return "B{" +
                "str='" + str + '\'' +
                ", bStr='" + bStr + '\'' +
                ", is=" + is +
                ", test=" + test +
                ", count=" + count +
                '}';
    }
}
```

1. 需要实现子类自己的`Parcelable`接口，只不过序列化和反序列化化的时候，要先调用父类的`Parcelable`接口！！主要有以下接口：

* public static final Parcelable.Creator<B> CREATOR = new Parcelable.Creator<B>()，

通常比较固定。

* public int describeContents()，

通常返回0.

* public void writeToParcel(Parcel dest, int flags)

调用`super.writeToParcel()`然后处理当前子类的属性。

2. protected B(Parcel in)的构造方法。

调用`super()`然后处理当前子类的属性。

```java
protected B(Parcel in) {
    // 调用父类的读取操作
    super(in);
    // 子类实现的读取操作
    bStr = in.readString();
    test = in.readInt();
}
```

这个构造方法并不是必要的，只是`Parcel`转`Object`的一个便捷的方法而已。
