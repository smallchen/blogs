## Android 打印View树

```java
printTree(root);
```

```java
private String space = "-";
private String SEPARATOR = "--";
private void printTree(ViewGroup parent) {
    Log.e(TAG, space+"child count:" + parent.getChildCount());
    for (int i = 0; i < parent.getChildCount(); ++i) {
        View child = parent.getChildAt(i);
        Log.e(TAG, space+String.format("child(%d): %s", i, child.toString()));
        if (child instanceof ViewGroup) {
            space += SEPARATOR;
            printTree((ViewGroup) child);
            space = space.substring(SEPARATOR.length());
        }
    }
}
```

输出：

```
E: -child count:2
E: -child(0): android.widget.LinearLayout{1e06e26 V.E...... ......I. 0,0-0,0}
E: ---child count:4
E: ---child(0): android.widget.TextView{582aa67 V.ED..... ......ID 0,0-0,0 #7f070080 app:id/text}
E: ---child(1): android.widget.ImageView{b382014 V.ED..... ......I. 0,0-0,0 #7f070043 app:id/image}
E: ---child(2): android.widget.LinearLayout{423f0bd V.E...... ......I. 0,0-0,0}
E: -----child count:4
E: -----child(0): android.widget.TextView{5d546b2 V.ED..... ......I. 0,0-0,0 #7f070023 app:id/btn_close}
E: -----child(1): android.widget.TextView{7058603 V.ED..... ......I. 0,0-0,0 #7f070026 app:id/btn_min}
E: -----child(2): android.widget.TextView{5812980 V.ED..... ......I. 0,0-0,0 #7f070025 app:id/btn_max}
E: -----child(3): android.widget.TextView{ba3dbb9 V.ED..... ......I. 0,0-0,0 #7f070024 app:id/btn_crash}
E: ---child(3): android.widget.EditText{bcd1bfe VFED..CL. ......I. 0,0-0,0 #7f070037 app:id/edit_text}
E: -child(1): android.widget.Button{9507f5f VFED..C.. ......I. 0,0-0,0 #7f070027 app:id/btn_scale}
```
