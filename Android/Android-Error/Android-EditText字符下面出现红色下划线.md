## Android EditText字符下面出现红色下划线

使用EditText的时候，有时候字符串会出现红色下划线，解决方法是：

把EditText的inputType由text改成：

`android:inputType=”textNoSuggestions”`

```java
mEditText.setInputType(mEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
```
