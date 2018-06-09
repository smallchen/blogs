
## FindBug或Coverity错误类型和解决方案列表。


### wait 和 notify 注意点

##### UW_UNCOND_WAIT && WA_NOT_IN_LOOP

```java
synchronized (LOCK) {
	try {
		LOCK.wait();
	} catch (InterruptedException e) {
		Log.e(TAG, "InterruptedException!", e);
	}
}

```

改为：

```java
private boolean mWaiting;
mWaiting = true;
synchronized (LOCK) {
	while (mWaiting) {
		try {
			LOCK.wait();
		} catch (InterruptedException e) {
			NLog.e(TAG, "InterruptedException!", e);
			// 看需求，是否需要 break。
		}
	}
}
```

##### NN_NAKED_NOTIFY

```java
synchronized (LOCK) {
	LOCK.notifyAll();
}
```

改为：

```java
synchronized (LOCK) {
	if (mWaiting) {
		mWaiting = false;
		LOCK.notifyAll();
	}
}
```


这部分，可以参考`wait()`和`notify()`的文档。

```java
wait
added in API level 1
void wait ()
Causes the current thread to wait until another thread invokes the notify() method or the notifyAll() method for this object. In other words, this method behaves exactly as if it simply performs the call wait(0).
The current thread must own this object's monitor. The thread releases ownership of this monitor and waits until another thread notifies threads waiting on this object's monitor to wake up either through a call to the notify method or the notifyAll method. The thread then waits until it can re-obtain ownership of the monitor and resumes execution.
As in the one argument version, interrupts and spurious wakeups are possible, and this method should always be used in a loop:
     synchronized (obj) {
         while (<condition does not hold>)
             obj.wait();
         ... // Perform action appropriate to condition
     }

This method should only be called by a thread that is the owner of this object's monitor. See the notify method for a description of the ways in which a thread can become the owner of a monitor.
Throws
IllegalMonitorStateException
if the current thread is not the owner of the object's monitor.
InterruptedException
if any thread interrupted the current thread before or while the current thread was waiting for a notification. The interrupted status of the current thread is cleared when this exception is thrown.
See also:
notify()
notifyAll()

Protected methods
```

**wait需要在loop里面执行，是为了保证wait能够长时间生效。由于interrupt或其它spurious wakeups（假的唤醒）可能会导致wait失效，继而引发问题。所以，需要将wait放在loop里面，在interrupted之后，还能继续等待，直到while里面真正的条件成立。**




1.       NP_NULL_ON_SOME_PATH_EXCEPTION



类型

必改项

描述

A  reference value which is null on some  exception control path is dereferenced here.  This may lead to aNullPointerException when the code is executed.  Note that because FindBugs  currently does not prune infeasible exception paths, this may be a false  warning.



代码调用时，遇到异常分支，可能造成一个对象没有获得赋值依旧保持NULL空指针。接下来如果对这个对象有引用，可能造成NullPointerException空指针异常。

案例

private voidtest_NP_NULL_ON_SOME_PATH_EXCEPTION(String name, String

pass){

User user = null;

int nport = 10;

try{

user = checkUser(name, pass);

if (user == null){

System.out.println("密码错误");

close();

return ;

}

}

catch (SQLException e){

		e.printStackTrace();

}

user.setPort(nport); //这端代码有NP_NULL_ON_SOME_PATH_EXCEPTION

}

代码分析：这里User对象是在try 块中赋值的， 但是由于checkUser 方法赋值失败造成异常，导致User 对象为空后， 直接转入异常处理模块执行 e.printStackTrace();此时并没有返回，接下来执行user.setPort(nport) 时， 必然造成空指针。



案例二：

md = null;  

try {  

   md = MessageDigest.getInstance("SHA-256");  

   md.update(bt);  

  } catch (NoSuchAlgorithmException e) {  

   e.printStackTrace();// script1    

  }  

  byte[] digest = md.digest();// script2  

解决方法

案例一：根据代码逻辑实际情况采取解决方法， 在异常情况下考虑好分支路径



案例二：bug出现在script2处，在script1处处理相应的exception即可，如throw或 return；



2.       NP_NULL_ON_SOME_PATH



类型

必改项

描述

There  is a branch of statement that,if executed, guarantees that a null  value will be dereferenced, which would generate aNullPointerException when the code is executed. Of course, the problem might be that  the branch or statement is infeasible and that the null pointer exception  can't ever be executed; deciding that is beyond the ability of FindBugs.



对象可能没有重新赋值

案例



如在JDBC编程时候，在关闭ResultSet时候(rs.close())，经常会出现这个bug，



解决方法

解决办法很容易想到，判断是否为null或

使用try...catch...finally。





3.       EC_UNRELATED_CLASS_AND_INTERFACE



类型

必改项

描述

This  method calls equals(Object) on two references, one of which is a class and  the other an interface, where neither the class nor any of its non-abstract  subclasses implement the interface. Therefore, the objects being compared are  unlikely to be members of the same class at runtime (unless some application  classes were not analyzed, or dynamic class loading can occur at runtime).  According to the contract of equals(), objects of different classes should  always compare as unequal; therefore, according to the contract defined by  java.lang.Object.equals(Object), the result of this comparison will always be  false at runtime.



使用equals方法比较不相关的类和接口

案例

StringBuilder  builder = new StringBuilder（"nihao"）;

String  string = "nihao";

builder.equals（string）;



解决方法

调用equals()比较不同的类型。

此方法调用相当于两个不同的类类型的引用，没有共同的子类（对象）。

因此，所比较的对象是不太可能在运行时相同的类成员（除非一些

应用类没有分析或动态类加载可以发生在运行时）。据

equals()的规则，不同类的对象应始终比较不平等，因此，根据

由java.lang.Object.equals定义的合同（对象），FALSE将永远是比较的结果

在运行时错误。





4.       UG_SYNC_SET_UNSYNC_GET



类型

必改项

描述

This  class contains similarly-named get and set methods where the set method is  synchronized and the get method is not.  This may result in incorrect  behavior at runtime, as callers of the get method will not necessarily see a  consistent state for the object.  The get method should be made  synchronized.



这个类包含类似命名的get和set方法。在set方法是同步方法和get方法是非同步方法。这可能会导致在运行时的不正确行为，因为调用的get方法不一定返回对象一致状态。 GET方法应该同步。

案例





解决方法







5.       DLS_DEAD_LOCAL_STORE



类型

必改项

描述

This  instruction assigns a value to a local variable, but the value is not read or  used in any subsequent instruction. Often, this indicates an error, because  the value computed is never used.

Note that Sun's  javac compiler often generates dead stores for final local variables. Because  FindBugs is a bytecode-based tool, there is no easy way to eliminate these  false positives.

该指令为局部变量赋值，但在其后的没有对她做任何使用。通常，这表明一个错误，因为值从未使用过。

案例




案例二：

String  abc = "abc";

String  xyz = new String("");

xyz =  abc;

System.out.println(xyz);

解决方法

删除未使用的本地变量

案例二：

因为String xyz = new String("");

 这一句执行3个动作：  

 1)创建一个引用xyz  

 2)创建一个String对象  

 3)把String的引用赋值给xyz

 其中，后面两个动作是多余的，因为后面的程序中你没有使用这个新建的String对象，而是重新给xyz赋值，

xyz =  abc;所以，只需要String  xyz = abc;就可以了



6.       RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE



类型

选改项

描述

A  value is checked here to see whether it is null, but this value can't be null  because it was previously dereferenced and if it were null a null pointer  exception would have occurred at the earlier dereference. Essentially, this  code and the previous dereference disagree as to whether this value is  allowed to be null. Either the check is redundant or the previous dereference  is erroneous.



案例

出现该bug有两种情况：多余的null检查；前边没有null值检查的。

比如：我们经常会这个使用ActionForm，

String  clazzId = request.getParameter("clazzId");// script1

studentForm.setClazzID(clazzId);//  script2

往往会在script2会出现该错误，因为在script1处未检查clazzId是否为null才导致的。

修改为：

if(clazzId  != null) {

studentForm.setClazzID(clazzId);

}



解决方法

在设置使用clazzId之前先判断其是否为null。





7.       ES_COMPARING_STRINGS_WITH_EQ



类型

必改项

描述

This  code comparesjava.lang.String objects for reference equality using the == or != operators.  Unless both strings are either constants in a source file, or have been  interned using the String.intern() method, the same string value may be represented by two different  String objects. Consider using theequals(Object) method  instead.



用==或者！=去比较String类型的对象

案例

String  str1 = "java";

String  str2 = "java";

System.out.print（str1==str2）;

成果：true（二者都为常量）

String  str1 = new String（"java"）;

String  str2 = new String（"java"）;

System.out.print（str1==str2）;

成果：false（二者为对象）

String  str1 = "java";

String  str2 = "blog";

String  s = str1+str2;

System.out.print（s=="javablog"）;

成果：false（s不为常量，为对象）

String  s1 = "java";

String  s2 = new String（"java"）;

System.out.print（s1.intern（）==s2.intern（））;

成果：true



解决方法

String对象进行比较的时候：只有两种情况可以使用== or !=，这两种情况是；仅当两个字符串在源文件中都是常量或者是调用String.intern()方法，使用String的规范化表示形式来进行比较,如果不是这两中情况的话推荐使用.equals(object)方式







8.       NP_NULL_PARAM_DEREF



类型

必改项

描述

This  method call passes a null value for a nonnull method parameter. Either the  parameter is annotated as a parameter that should always be nonnull, or  analysis has shown that it will always be dereferenced.

这种方法调用传递一个空值给非空参数。，要这个参明确注释它是非空参数，要么经过分析得出它总是会被引用不可能为空。



案例

int  FunctionOne（int x，int y=0，int z=0，int w=0）；

我们要给z传递整型值8，作如下调用：

FunctionOne（8）；

显然，编译器无法确定这个8到底要传递给哪个参数。为了达到我们的目的，必须这样调用：

FunctionOne（0，0，8）；

这是x被传递了0，y被传递了 0，z被传递了8



解决方法

我们可以赋予函数参数默认值。

所谓默认值就是在调用时，可以不写某些参数的值，编译器会自动把默认值传递给调用语句中。默认值一般在函数声明中设置；





9.       UWF_UNWRITTEN_FIELD



类型

必改项

描述

This  field is never written.  All reads of it will return the default value.  Check for errors (should it have been initialized?), or remove it if it is  useless.



此字段是永远不会写入值。所有读取将返回默认值。检查错误（如果它被初始化？），如果它确实没有用就删除掉。

案例





解决方法







10.  NP_TOSTRING_COULD_RETURN_NULL



类型

必改项

描述

This toString  method seems to return null in some circumstances. A liberal reading of the  spec could be interpreted as allowing this, but it is probably a bad idea and  could cause other code to break. Return the empty string or some other  appropriate string rather than null.

toString方法可能返回null

案例





解决方法







11.  MF_CLASS_MASKS_FIELD



类型

必改项

描述

This  class defines a field with the same name as a visible instance field in a  superclass. This is confusing, and may indicate an error if methods update or  access one of the fields when they wanted the other.



子类中定义了和父类中同名的字段。在调用时会出错

案例





解决方法







12.  NS_DANGEROUS_NON_SHORT_CIRCUIT



类型

必改项

描述

This code  seems to be using non-short-circuit logic (e.g., & or |) rather than  short-circuit logic (&& or ||). In addition, it seem possible that,  depending on the value of the left hand side, you might not want to evaluate  the right hand side (because it would have side effects, could cause an  exception or could be expensive.

Non-short-circuit  logic causes both sides of the expression to be evaluated even when the  result can be inferred from knowing the left-hand side. This can be less  efficient and can result in errors if the left-hand side guards cases when  evaluating the right-hand side can generate an error.

See theJava Language Specification for details

代码中使用（& or |）代替（&& or ||）操作，这会造成潜在的危险

案例




缺陷详解：解释一下短路与非短路的区别：

（1）短路运算（short-circuit）

逻辑与（&&）：如果左操作数结果为true，则继续计算右操作数；如果左操作数结果为false

，则对右操作数的运算已经没有必要，直接返回结果false，忽略右操作运算。

逻辑或（||）：如果左操作数结果为false，则继续计算右操作数；如果左操作数结果为true

，则对右操作数的运算已经没有必要，直接返回结果true，忽略右操作运算。

正确地使用短路运算符，就可以写出节省时间、提高效率的代码。

（2）非短路运算（non-short-circuit）

逻辑与（&）、逻辑或（|）：不管左操作数的运算结果如何，都一律计算右边的操作数。

我们回放的代码使用的是非短路与（&），也就是不管左边的sr !=null

的结果如何，都一律运算右边的sr.getInt(0) == 0，那如果遇到sr为null

值的情况，则一样也会调用到sr.getInt(0)，显然就会出现空指针异常。

解决方法

解决这个问题的最好选择是改用短路运算与（&&）。

SysDataSet sds =  con.csCommonSP("P_KMS_GETORIGINALFILETREE");

SysRecord sr = sds  != null ? sds.getParamSet() : null;

rs = (sr != null&& sr.getInt(0) == 0) ?  sds.getResultSet() :null;





13.  HE_EQUALS_USE_HASHCODE



类型

必改项

描述

This class  overrides equals(Object), but does not override hashCode(), and inherits the  implementation of hashCode() from java.lang.Object (which returns the identity  hash code, an arbitrary value assigned to the object by the VM).   Therefore, the class is very likely to violate the invariant that equal  objects must have equal hashcodes.

If you  don't think instances of this class will ever be inserted into a HashMap/HashTable,  the recommended hashCode implementation to use is:

public int hashCode() {

 assert false :  "hashCode not designed";

 return 42; // any  arbitrary constant will do

 }



一个类覆写了equals方法，没有覆写hashCode方法，使用了Object对象的hashCode方法

案例







解决方法







14.  NP_EQUALS_SHOULD_HANDLE_NULL_ARGUMENT



类型

必改项

描述

This  implementation of equals(Object) violates the contract defined by  java.lang.Object.equals() because it does not check for null being passed as  the argument. All equals() methods should return false if passed a null  value.



变量调用equals方法时没有进行是否为null的判断

案例





解决方法







15.  SE_BAD_FIELD



类型

必改项

描述

This  Serializable class defines a non-primitive instance field which is neither  transient, Serializable, orjava.lang.Object, and does not appear  to implement theExternalizable interface or thereadObject() andwriteObject() methods.  Objects of this class will not be deserialized  correctly if a non-Serializable object is stored in this field.



序列化的类定义了一个非原生实例对象，这个对象是不可序列化的或非java对象，而且没有添加transient关键字，没有实现Externalizable接口或没有添加readObject()和writeObject()方法，那么这个类的对象将不能正确的反序列化。



案例

示例代码：

publicclassMytest implements Serializable {

   private ThreadmThread;

   protected FileOutputStreamoutputStream ;

   //这两个变量会提示defines  non-transient non-serializable instance field finoutStream

   private Stringstr;

   private Stringstr1;

   privateinti;



}



解决方法

解决方法：

1：给对象加上transient关键字，对该象将不会被序列化保存和恢复。

publicclassMytest implements Serializable{

   privatetransient ThreadmThread;

   protectedtransient FileOutputStreamoutputStream;

   private Stringstr;

private Stringstr1;

   privateinti;

}

2：实现Externalizable接口代替Serializable接口，对序列化过程进行完全自主控制。Externalizable没有默认序列化机制，我们需要在writeExternal()方法中将希望被序列化的对象的信息写入，在readExternal()中将其恢复，并且writeObject()方法与readObject()方法中保存和恢复对象的顺序必须相同。

publicclass Mytestimplements Externalizable{

   private ThreadmThread;

   protected FileOutputStreamfoutputStream;

   private Stringstr;

   private Stringstr1;

   privateinti;

   @Override

   publicvoid readExternal(ObjectInput  arg0)throws IOException,

		  ClassNotFoundException {

	  str=(String) arg0.readObject();

	  str1=(String) arg0.readObject();

	  i= arg0.readInt();

   }

   @Override

publicvoid  writeExternal(ObjectOutput arg0)throws IOException{

	  arg0.writeObject(str);

	  arg0.writeObject(str1);

	  arg0.writeInt(i);

   }

}

3：添加writeObject()和readObject()方法替代Externalizable，对序列化过程进行自主控制。注意是“添加”不是“复写”或“实现”，类型必须为private，同样writeObject()方法与readObject()方法中保存和恢复对象的顺序必须相同。

publicclassMytest implements Serializable{

   private ThreadmThread;

   protected FileOutputStreamfoutputStream;

   private Stringstr;

   private Stringstr1;

   privateinti;

   privatevoid  writeObject(ObjectOutputStream outputStream)throws IOException {

	  outputStream.writeObject(str);

	  outputStream.writeObject(str1);

	  outputStream.writeInt(i);

   }  

   privatevoid readObject(ObjectInputStream  inputStream)throws IOException,ClassNotFoundException {

	  str=(String) inputStream.readObject();

	  str1=(String) inputStream.readObject();

	  i= inputStream.readInt();

   }

}



16.  NP_UNWRITTEN_FIELD



类型

必改项

描述

The  program is dereferencing a field that does not seem to ever have a non-null  value written to it. Unless the field is initialized via some mechanism not  seen by the analysis, dereferencing this value will generate a null pointer  exception.



程序中引用一变量，没有一个可见的机制将一个非空的值赋给它，引用时需要对其进行判断（是否有一不可见的机制对其进行了初始化）不然会空指针异常。

案例

privatestaticArrayList<String> paramList =null;

publicArrayList checkValue(String str1) {

   paramList.clear();

   returnparamList;

}

//调用clear()只是清除list内容并不会使list报空指针异常，但是list为null时，就会报空指针异常了。

解决方法

引用前进行判空

privatestatic ArrayList<String>paramList =null;

publicArrayList checkValue(String str1) {

   if (null!=paramList) {          

	   paramList.clear();

   }

   returnparamList;

}





17.  UWF_NULL_FIELD



类型

必改项

描述

All  writes to this field are of the constant value null, and thus all reads of  the field will return null. Check for errors, or remove it if it is useless.



字段的值总是为null值，所有读取该字段的值都为null。检查错误，如果它确实没有用就删除掉。

案例

privatestatic ArrayList<String>paramList =null;

public ArrayList<String> returnList(ArrayList  mList) {

	  returnmList;         

}

注：程序中无其函数对paramList进行赋值，或一直赋null值。

解决方法

1、对paramList进行正确赋值。

2、如果paramList没有用就删掉。



18.  IM_BAD_CHECK_FOR_ODD



类型

必改项

描述

The  code uses x % 2 == 1 to check to see if a value is odd, but this won't work  for negative numbers (e.g., (-5) % 2 == -1). If this code is intending to  check for oddness, consider using x & 1 == 1, or x % 2 != 0.



如果使用代码x % 2  == 1来检查值是否为奇数，对于负数是无效的，应该使用x&1==1或x%2!=0。

案例

publicboolean isOddness(int value){

   boolean result =false;

	   if ((value % 2)  ==1) {

	  result = true;

   }

   return result;

   }

分析：如果传进来的value是负数则此函数返回的结果是错误的。

解决方法

publicboolean isOddness(int value){

   boolean result =false;

   if ((value & 1)  ==1) {

	  result = true;

   }

   return result;

   }





19.  NP_NULL_ON_SOME_PATH_MIGHT_BE_INFEASIBLE



类型

必改项

描述

There  is a branch of statement that,if executed, guarantees that a null  value will be dereferenced, which would generate aNullPointerException when the code is executed. Of course, the problem might be that  the branch or statement is infeasible and that the null pointer exception  can't ever be executed; deciding that is beyond the ability of FindBugs. Due  to the fact that this value had been previously tested for nullness, this is  a definite possibility.



有一个分支如果进入的话，里面的某些变量可能会是空指针

案例

publicstaticvoid main(String[] args) {

   String  s1 = null;

   String  s2 = "";

   doCompare(s1,  s2);

   }



   publicstaticint doCompare(String s1, String s2) {

   int result;

   if (s1 ==null && s2 == null) {

	   result  = 0;

   } elseif (s1 ==null && s2 != null) {

	   result  = -1;

   } elseif (s1 !=null && s2 == null) {

	   result  = 1;

   } else {// s1 != null && s2 != null

	   result  = s1.compareTo(s2);

//这里s1可能发生空指针异常，但是此语句不会执行。

   }

   return result;

   }

解决方法

publicstaticint doCompare(String s1, String s2) {

   int result;

   if (s1 ==null && s2 == null) {

	   result  = 0;

   } elseif (s1 ==null) {

	   assert s2 !=null;

	   result  = -1;

   } elseif (s2 ==null) {

	   assert s1 !=null;

	   result  = 1;

   } else {

	   result  = s1.compareTo(s2);

   }

   return result;

   }





20.  RV_EXCEPTION_NOT_THROWN



类型

必改项

描述

This code  creates an exception (or error) object, but doesn't do anything with it. For  example, something like

if (x < 0)
 new IllegalArgumentException("x must be nonnegative");
It was probably  the intent of the programmer to throw the created exception:

if (x < 0)
 throw new IllegalArgumentException("x must be nonnegative");


此代码创建一个异常（或错误）的对象，但不会用它做任何事情。例如：if (x < 0)

new IllegalArgumentException("x must  be nonnegative");

这可能是程序员的意图抛出创建的异常：

if (x < 0)

throw new IllegalArgumentException("x  must be nonnegative");



案例

privatevoid test(){

new  IllegalArgumentException("x must be  nonnegative");

   }

解决方法

1、打印异常调用栈。

privatevoid test(){

   if(x<0){

new IllegalArgumentException("x must be nonnegative "). printStackTrace();

}

   }

2、抛出异常。

privatevoid test(){

if(x<0){

throw new IllegalArgumentException("x must be nonnegative ");

}

}





21.  DL_SYNCHRONIZATION_ON_BOOLEAN



类型

必改项

描述

The code  synchronizes on a boxed primitive constant, such as an Boolean.

private static Boolean inited = Boolean.FALSE;
...
 synchronized(inited) {
   if (!inited) {
	  init();
	  inited = Boolean.TRUE;
	  }
	}
...
Since there  normally exist only two Boolean objects, this code could be synchronizing on  the same object as other, unrelated code, leading to unresponsiveness and  possible deadlock

该代码同步一个封装的原始常量，例如一个Boolean类型。

private static Boolean inited = Boolean.FALSE;

...

 synchronized(inited) {

  if  (!inited) {

	 init();

	 inited  = Boolean.TRUE;

	 }

   }

...

由于通常只存在两个布尔对象，此代码可能是同步的其他无关的代码中相同的对象，这时会导致反应迟钝和可能死锁



案例



解决方法







22.  ES_COMPARING_PARAMETER_STRING_WITH_EQ



类型

必改项

描述

This  code compares ajava.lang.String parameter for reference equality using the == or != operators.  Requiring callers to pass only String constants or interned strings to a  method is unnecessarily fragile, and rarely leads to measurable performance  gains. Consider using theequals(Object) method instead.



用==或者!=方法去比较String类型的参数

案例



解决方法







23.  JLM_JSR166_UTILCONCURRENT_MONITORENTER



类型

必改项

描述

This method performs synchronization an  object that is an instance of a class from the java.util.concurrent package  (or its subclasses). Instances of these classes have their own concurrency  control mechanisms that are orthogonal to the synchronization provided by the  Java keyword synchronized. For example, synchronizing on an AtomicBoolean  will not prevent other threads from modifying the AtomicBoolean.

Such code may be correct, but should be  carefully reviewed and documented, and may confuse people who have to  maintain the code at a later date.





案例



解决方法







24.  DL_SYNCHRONIZATION_ON_SHARED_CONSTANT



类型

必改项

描述

The code synchronizes on interned String.

private static String LOCK =  "LOCK";

...

  synchronized(LOCK) { ...}

...

Constant Strings are interned and shared  across all other classes loaded by the JVM. Thus, this could is locking on  something that other code might also be locking. This could result in very  strange and hard to diagnose blocking and deadlock behavior. See http://www.javalobby.org/java/forums/t96352.html andhttp://jira.codehaus.org/browse/JETTY-352.

See CERTCON08-J. Do not synchronize on objects that  may be reused for more information.



同步String类型的常量时，由于它被JVM中多个其他的对象所共有，这样在其他代码中会引起死锁。

案例



解决方法







25.  SP_SPIN_ON_FIELD



类型

必改项

描述

This  method spins in a loop which reads a field.  The compiler may legally  hoist the read out of the loop, turning the code into an infinite loop.   The class should be changed so it uses proper synchronization (including wait  and notify calls).



方法无限循环读取一个字段。编译器可合法悬挂宣读循环，变成一个无限循环的代码。这个类应该改变，所以使用适当的同步（包括等待和通知要求）

案例



解决方法







26.  RpC_REPEATED_CONDITIONAL_TEST



类型

必改项

描述

The  code contains a conditional test is performed twice, one right after the  other (e.g.,x == 0 ||  x == 0). Perhaps the second occurrence is  intended to be something else (e.g.,x == 0 || y == 0).



该代码包含对同一个条件试验了两次，两边完全一样例如：（如X == 0 | | x == 0）。可能第二次出现是打算判断别的不同条件（如X == 0 | | y== 0）。

案例



解决方法







27.  NP_GUARANTEED_DEREF



类型

必改项

描述

There is a statement or branch that if  executed guarantees that a value is null at this point, and that value that  is guaranteed to be dereferenced (except on forward paths involving runtime  exceptions).

Note that a check such as if (x == null)  throw new NullPointerException();x.



在正常的null判断分支上，对象去除引用操作是受保护的不允许的

案例

public void test()  {
 String var = "";
 int index = 2;
 if (index == -1) {
 var = String.class.getName();
 if (var.length() == 0) {
 var = null;
 }
 } else {
 var = Integer.class.getName();
 if (var.length() == 0) {
 var = null;
 }
 }
 if (var == null) {// FINBUGS reports on this line NP_GUARANTEED_DEREF
 /*
 * There is a statement or branch that if executed guarantees that a value
 * is null at this point, and that value that is guaranteed to be
 * dereferenced (except on forward paths involving runtime exceptions).
 */
 throw new NullPointerException("NULL");
 }
 }



解决方法

据说改为throw new RuntimeException就会消除警告

去掉throw new NullPointerException也可以



28.  UR_UNINIT_READ



类型

必改项

描述

This  constructor reads a field which has not yet been assigned a value.  This  is often caused when the programmer mistakenly uses the field instead of one  of the constructor's parameters.



此构造方法中使用了一个尚未赋值的字段或属性。



案例

String a;

public FindBugsTest(String b) {

   String abc = a; // UR_UNINIT_READ ,将b误写为a

   System.out.println(abc);

}

解决方法

使用函数的参数或初始化的字段



29.  NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE



类型

必改项

描述

The  return value from a method is dereferenced without a null check, and the  return value of that method is one that should generally be checked for null.  This may lead to aNullPointerException when the code is executed.



方法的返回值没有进行是否为空的检查就重新赋值，这样可能会出现空指针异常。

案例

publicvoid caller(int a) {

	  String c = called(a);

	  if (c.equals("")) { //c maybe null

	  } elseif (c.equals("")) {

	  }

   }



解决方法

增加对返回值是否为空的判断



30.  IL_INFINITE_RECURSIVE_LOOP



类型

必改项

描述

This  method unconditionally invokes itself. This would seem to indicate an  infinite recursive loop that will result in a stack overflow.



案例

public void foo() {

foo();  //  IL_INFINITE_RECURSIVE_LOOP

	  //...

}



解决方法

避免函数的递归调用，或加上结束递归的条件判断



31.  IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_

OUTER_METHOD



类型

必改项

描述

An  inner class is invoking a method that could be resolved to either a inherited  method or a method defined in an outer class. By the Java semantics, it will  be resolved to invoke the inherited method, but this may not be want you  intend. If you really intend to invoke the inherited method, invoke it by  invoking the method on super (e.g., invoke super.foo(17)), and thus it will  be clear to other readers of your code and to FindBugs that you want to  invoke the inherited method, not the method in the outer class.





案例

public void onOK() {

   }



   protected  class parentClass {

	   public  void onOK() {

   }

   }



   protected  class childClass extends parentClass {



	   public  void callOnOK() {

		   onOK();   //IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD, should be super.onOK

	   }



   }



解决方法

如果要引用外部类对象，可以加上“outclass.this”。

如果要引用父类的onOK方法，请使用super.onOK()。



32.  IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN



类型

必改项

描述

The  initial value of this parameter is ignored, and the parameter is overwritten  here. This often indicates a mistaken belief that the write to the parameter  will be conveyed back to the caller.



传入参数的值被忽略，但是对传入值进行了修改，并返回给了调用者

案例

publicvoid foo(String p) {

   p = "abc"; //  IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN

}



解决方法

不要将函数的参数作为工作变量



33.  FE_FLOATING_POINT_EQUALITY



类型

必改项

描述

This  operation compares two floating point values for equality. Because floating  point calculations may involve rounding, calculated float and double values  may not be accurate. For values that must be precise, such as monetary  values, consider using a fixed-precision type such as BigDecimal. For values  that need not be precise, consider comparing for equality within some range,  for example:if (  Math.abs(x - y) < .0000001 ). See the Java  Language Specification, section 4.2.4.



此操作比较两个浮点值是否相等。由于浮点运算可能会涉及到舍入，计算float和double值可能不准确。如果要求值必须准确，如货币值，可以考虑使用固定精度类型，如BigDecimal类型的值来比较

案例



解决方法







34.  NP_ALWAYS_NULL



类型

必改项

描述

A null  pointer is dereferenced here.  This will lead to aNullPointerException when the code is executed.



对象赋为null值后没有被重新赋值

案例



解决方法







35.  DLS_DEAD_LOCAL_STORE_OF_NULL



类型

必改项

描述

The  code stores null into a local variable, and the stored value is not read.  This store may have been introduced to assist the garbage collector, but as  of Java SE 6.0, this is no longer needed or useful.



把一个本地变量赋值为null值，并且再也没有对这个变量做任何的操作。这样可能是为了垃圾回收，而是Java SE 6.0，这已不再需要

案例



解决方法







36.  SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH



类型

必改项

描述

A  value stored in the previous switch case is overwritten here due to a switch  fall through. It is likely that you forgot to put a break or return at the  end of the previous case.



在swtich中先前的case值因为swtich执行失败而被覆写，这就像是忘记使用break推出或者没有使用return语句放回先前的值一样。

案例



解决方法







37.  INT_BAD_COMPARISON_WITH_SIGNED_BYTE



类型

必改项

描述

Signed  bytes can only have a value in the range -128 to 127. Comparing a signed byte  with a value outside that range is vacuous and likely to be incorrect. To  convert a signed byteb to an unsigned value in the range 0..255, use0xff & b



比较有符合数，要先把有符号数转换为无符合数再进行比较

案例



解决方法







38.  NP_DEREFERENCE_OF_READLINE_VALUE



类型

必改项

描述

The  result of invoking readLine() is dereferenced without checking to see if the  result is null. If there are no more lines of text to read, readLine() will  return null and dereferencing that will generate a null pointer exception.



对readLine()的结果值没有进行判空操作就去重新赋值，这样的操作可以会抛出空指针异常。

案例



解决方法







39.  SA_FIELD_DOUBLE_ASSIGNMENT



类型

必改项

描述

This method contains a double assignment of  a field; e.g.

 int  x,y;

  public void foo() {

   x  = x = 17;

 }

Assigning to a field twice is useless, and  may indicate a logic error or typo.



方法中的字段包含了双重任务，例如：

int x;

public void foo() {

 x = x = 17;

}

这种为变量赋值是无用的，并可能表明一个逻辑错误或拼写错误。

案例



解决方法







40.  DMI_INVOKING_TOSTRING_ON_ARRAY



类型

必改项

描述

The  code invokes toString on an array, which will generate a fairly useless  result such as [C@16f0472. Consider using Arrays.toString to convert the  array into a readable String that gives the contents of the array. See  Programming Puzzlers, chapter 3, puzzle 12.



该代码调用上数组的toString（）方法，产生的结果形如[@16f0472并不能显示数组的真实内容。考虑使用Arrays.toString方法来转换成可读的字符串，提供该数组的内容数组

案例



解决方法







41.  EC_UNRELATED_TYPES



类型

必改项

描述

This  method calls equals(Object) on two references of different class types with  no common subclasses. Therefore, the objects being compared are unlikely to  be members of the same class at runtime (unless some application classes were  not analyzed, or dynamic class loading can occur at runtime). According to  the contract of equals(), objects of different classes should always compare  as unequal; therefore, according to the contract defined by  java.lang.Object.equals(Object), the result of this comparison will always be  false at runtime.



调用equals方法比较不同类型的类

案例



解决方法







42.  BC_IMPOSSIBLE_CAST



类型

必改项

描述

This  cast will always throw a ClassCastException. FindBugs tracks type information  from instanceof checks, and also uses more precise information about the  types of values returned from methods and loaded from fields. Thus, it may  have more precise information that just the declared type of a variable, and  can use this to determine that a cast will always throw an exception at  runtime.



不可能的类转换，执行时会抛出ClassCastException

案例



解决方法







43.  ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND



类型

必改项

描述

This  code converts an int value to a float precision floating point number and  then passing the result to the Math.round() function, which returns the  int/long closest to the argument. This operation should always be a no-op,  since the converting an integer to a float should give a number with no  fractional part. It is likely that the operation that generated the value to  be passed to Math.round was intended to be performed using floating point  arithmetic.



int类型的值转换为float类型之后调用了Math.round方法

案例



解决方法







44.  RV_RETURN_VALUE_IGNORED



类型

必改项

描述

The return value of this method should be  checked. One common cause of this warning is to invoke a method on an  immutable object, thinking that it updates the object. For example, in the  following code fragment,

String dateString = getHeaderField(name);

dateString.trim();

the programmer seems to be thinking that  the trim() method will update the String referenced by dateString. But since  Strings are immutable, the trim() function returns a new String value, which  is being ignored here. The code should be corrected to:

String dateString = getHeaderField(name);

dateString = dateString.trim();



该方法的返回值应该进行检查。这种警告通常出现在调用一个不可变对象的方法，认为它更新了对象的值。例如：String dateString = getHeaderField(name);

dateString.trim();

程序员似乎以为trim（）方法将更新dateString引用的字符串。但由于字符串是不可改变的，trim（）函数返回一个新字符串值，在这里它是被忽略了。该代码应更正：

String dateString  = getHeaderField(name);

dateString =  dateString.trim();



案例



解决方法







45.  SF_SWITCH_FALLTHROUGH



类型

必改项

描述

This  method contains a switch statement where one case branch will fall through to  the next case. Usually you need to end this case with a break or return.



Switch语句中一个分支执行后又执行了下一个分支。通常case后面要跟break或者return语句来跳出。

案例

int main(int argc, char* argv[])
 {
  printf("Hello World!/n");

int num_of_operands = 2;


  //fall through 的设计可以把代码设计得很简约，
  //以下的这个操作数处理是一个很好的例子，它可以很简约的去处理每一个操作数
  //当然不得不说 switch将 fall through设计为默认操作是引起许多bug的原因

//session 1:
  switch(num_of_operands){
  case 2://process_operand(operator->operand_2);
  case 1://process_operand(operator->operand_1);
  default:;
  }

//session 2:即使是default没有break，一样会出现fall  through现象
  num_of_operands = 2;
  switch(num_of_operands){
  case 1:
   printf("case 1 fall through/n");
  default:
   printf("default fall through/n");
   //num_of_operands = 10;
  case 3:
   printf("case 2 fall through/n");

  }

//conclusion :在使用switch时需要注意，如果是一般应用，请别忘了加上 break

return 0;
 }

解决方法

在使用switch时需要注意，如果是一般应用，请别忘了加上 break



46.  DE_MIGHT_IGNORE



类型

必改项

描述

This  method might ignore an exception.  In general, exceptions should be  handled or reported in some way, or they should be thrown out of the method.



方法可能忽略异常

案例

try{}catch(Exception  ex){}



解决方法

方法有可能抛异常或者忽略异常，需要对异常进行处理,即需要在catch体中对异常进行处理



47.  VA_FORMAT_STRING_EXTRA_ARGUMENTS_PASSED



类型

比改项

描述

A  format-string method with a variable number of arguments is called, but more  arguments are passed than are actually used by the format string. This won't  cause a runtime exception, but the code may be silently omitting information  that was intended to be included in the formatted string.



使用String的format方法时有非法的参数也经过了格式化操作。

案例

这个错误很简单，是使用String.format的时候出了问题，format里面的参数没有被全部用上。
看下面一段代码：
 public void test(){
	 String str1 = "123";
	 String str2 = "456";
	 String str3 = String.format("{0} {1}" , str1 ,str2);
	 System.out.println(str3);
 }
输出的结果是：{0}  {1}


解决方法

这个Bug描述就是这种问题，str1和str2根本没有被用上！{0}{1}这种Format格式是.NET上面的用法，java里面应该是%s %s。
这个是一个代码逻辑问题，可能是你写代码时不小心导致的，它在这段代码里不会导致异常，但往往会很可能导致其他地方异常，那时候你可能会百思不得其解。





48.  RE_POSSIBLE_UNINTENDED_PATTERN



类型

必改项

描述

A  String function is being invoked and "." is being passed to a  parameter that takes a regular expression as an argument. Is this what you  intended? For example s.replaceAll(".", "/") will return  a String in which every character has been replaced by a /  character, and s.split(".")always returns a zero length  array of String.





案例

if(version  != null && (!version.equalsIgnoreCase("")) &&  version.split(".").length < 4){

	   this.m_txfSoftVersion.setVersion(version);

}



String的split方法传递的参数是正则表达式，正则表达式本身用到的字符需要转义，如：句点符号"."，美元符号"$"，乘方符号"^"，大括号"{}"，方括号"[]"，圆括号"()"，竖线"|"，星号"*"，加号"+"，问号"?"等等，这些需要在前面加上"\\"转义符。

解决方法

在前面加上"\\"转义符



49.  SE_BAD_FIELD_INNER_CLASS



类型

必改项

描述

This Serializable class is an inner class  of a non-serializable class. Thus, attempts to serialize it will also attempt  to associate instance of the outer class with which it is associated, leading  to a runtime error.

If possible, making the inner class a  static inner class should solve the problem. Making the outer class  serializable might also work, but that would mean serializing an instance of  the inner class would always also serialize the instance of the outer class,  which it often not what you really want.





案例



解决方法







50.  RV_01_TO_INT



类型

必改项

描述

A  random value from 0 to 1 is being coerced to the integer value 0. You  probably want to multiple the random value by something else before coercing  it to an integer, or use theRandom.nextInt(n) method.



从0到1随机值被强制为整数值0。在强制得到一个整数之前，你可能想得到多个随机值。或使用Random.nextInt（n）的方法。

案例



解决方法







51.  UW_UNCOND_WAIT



类型

必改项

描述

This  method contains a call tojava.lang.Object.wait() which is not  guarded by conditional control flow.  The code should verify that  condition it intends to wait for is not already satisfied before calling  wait; any previous notifications will be ignored.



方法中包含调用java.lang.Object.wait（），而却没有放到条件流程控制中。该代码应确认条件尚未满足之前等待;先前任何通知将被忽略。

案例

public class Test

{

	 void clueless() throws Exception

	 {

			synchronized(this)

			{

				   this.wait(); // VIOLATION

			}

	 }

}

该检测模式寻找在进入同步块时无条件地wait()，与wait相关的条件判断在无锁（不在同步块内）情况下进行的，有可能导致其它线程的notification被忽略。

解决方法

多线程错误 -无条件等待

将wait()放到条件控制流中：If we are not enabled, then waitif (!enabled) { /*条件判断不在同步块内*/

try {

synchronized (lock) {      /*if (!enabled) 应该在这里*/

lock.wait();

...



52.  UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS



类型

必改项

描述

This  anonymous class defined a method that is not directly invoked and does not  override a method in a superclass. Since methods in other classes cannot  directly invoke methods declared in an anonymous class, it seems that this  method is uncallable. The method might simply be dead code, but it is also  possible that the method is intended to override a method declared in a  superclass, and due to an typo or other error the method does not, in fact,  override the method it is intended to.



在匿名类中定义了一个既没有覆写超类中方法也不能直接调用的方法。因为在其他类的方法不能直接引用匿名类声明的方法，似乎这种方法不能被调用，这种方法可能只是没有任何作用的代码，但也可能覆写超类中声明。

案例

For example, in the following code, it is  impossible to invoke the initalValue method (because the name is misspelled  and as a result is doesn't override a method in ThreadLocal).

private static ThreadLocal serialNum = new  ThreadLocal() {

		protected synchronized Object initalValue() {

			return new Integer(nextSerialNum++);

		}

	 };

解决方法

错误用法 -匿名内部类中定义的不可调用的方法

Added check for  uncallable method of an anonymous inner class.



53.  EQ_SELF_USE_OBJECT



类型

必改项

描述

This  class defines a covariant version of theequals() method, but  inherits the normalequals(Object) method defined in the basejava.lang.Object  class.  The class should probably define aboolean equals(Object) method.



类中定义了一组equals方法，但是都是继承的java.lang.Object class中的equals(Object)方法

案例

public class Point {

  private static double version = 1.0;

  private transient double distance;

  private int x, y;

 /* public  boolean equals(Object other) {

	if (other == this) return true;

	if (other == null) return false;

	if (getClass() != other.getClass()) return false;

	Point point = (Point)other;

	return (x == point.x && y == point.y);

 }  */

}

Point p1,p2;

if(p1.equals(p2)) return ture;



Note：Regardless  of which class contains the equals() method, the signature must always  declare an Object argument type. Since Java's libraries look for one with an  Object argument, if the signature is not correct, then the java.lang.Object  method will be called instead, leading to incorrect behavior.

此处我们想比较的仅是x和y，而由于代码中未重写equals()方法，调用了Object.equals(Object)方法，对distance与version也进行了比较。

解决方法

错误用法 -协变equals()方法定义，继承了Object.equals(Object)

类中增加public boolean equals(Object)方法的定义（见上例注释部分）。



54.  REC_CATCH_EXCEPTION



类型

必改项

描述

This  method uses a try-catch block that catches Exception objects, but Exception  is not thrown within the try block, and RuntimeException is not explicitly  caught. It is a common bug pattern to say try { ... } catch (Exception e) {  something } as a shorthand for catching a number of types of exception each  of whose catch blocks is identical, but this construct also accidentally  catches RuntimeException as well, masking potential bugs.


在try/catch块中捕获异常，但是异常没有在try语句中抛出而RuntimeException又没有明确的被捕获

案例

public void addInstance(String className) {

	try {

	   Class clazz = Class.forName(className);

	   objectSet.add(clazz.newInstance());

   }

   catch (IllegalAccessException e) {

	   logger.log("Exception in addInstance", e);

   }

	catch (InstantiationException e) {

	   logger.log("Exception in addInstance", e);

   }

	catch (ClassNotFoundException e) {

	   logger.log("Exception in addInstance", e);

   }

}

下面将上述代码中三个捕获块合并成捕获 Exception的单独捕获块，因为每个捕获块的捕获恢复操作是相同的，这样便造成了未经检查的异常将被记录。

public void addInstance(String className) {

	try {

	   Class clazz = Class.forName(className);

	   objectSet.add(clazz.newInstance());

}

/* catch (RuntimeException e) {

	  throw e;

} */

	catch (Exception e) {

	   logger.log("Exception in newInstance", e);

   }

}



解决方法

错误用法 -捕获了没有抛出的异常

因为 RuntimeException扩展了 Exception，代码中增加捕获 RuntimeException，并在捕获 Exception 之前重新将其抛出。



55.  RU_INVOKE_RUN



类型

必改项

描述

This  method explicitly invokesrun() on an object.  In general,  classes implement theRunnable interface because they are going to have theirrun()  method invoked in a new thread, in which caseThread.start() is the  right method to call.



这种方法显式调用一个对象的run（）。一般来说，类是实现Runnable接口的，因为在一个新的线程他们将有自己的run（）方法，在这种情况下Thread.start（）方法调用是正确的。

案例

	public static void main(final String[] args) {

	   System.out.println("Main thread: " +  Thread.currentThread().getId());

	   final FooThread thread = new FooThread();

	   thread.run();

	   //thread.start();

   }

	public static class FooThread extends Thread

   {

	   @Override

	   public void run() {

		   System.out.println("I'm executing from thread " +  Thread.currentThread().getId());

		   super.run();

	   }

   }

解决方法

多线程错误 -在线程中调用了run()

run()方法只是类的一个普通方法而已，如果直接调用run方法，程序中依然只有主线程这一个线程。启动一个新的线程，我们不是直接调用Thread的子类对象的run方法，而是调用Thread子类对象的start（从Thread类中继承的）方法，Thread类对象的start方法将产生一个新的线程，并在该线程上运行该Thread类对象中的run方法，根据面向对象的多态性，在该线程上实际运行的是Thread子类（也就是我们编写的那个类）对象中的run方法。



56.  NP_SYNC_AND_NULL_CHECK_FIELD



类型

必改项

描述

Since  the field is synchronized on, it seems not likely to be null. If it is null  and then synchronized on a NullPointerException will be thrown and the check  would be pointless. Better to synchronize on another field.



如果代码块是同步的，那么久不可能为空。如果是空，同步时就会抛出NullPointerException异常。最好是在另一个代码块中进行同步。

案例



解决方法







57.  DLS_DEAD_STORE_OF_CLASS_LITERAL



类型

必改项

描述

This instruction assigns a class literal to  a variable and then never uses it.The behavior of this differs in Java 1.4  and in Java 5. In Java 1.4 and earlier, a reference to Foo.class would force the  static initializer for Foo to be executed, if it has not been executed  already. In Java 5 and later, it does not.

See Sun'sarticle on Java SE compatibility for more details and examples,  and suggestions on how to force class initialization in Java 5.



以类的字面名称方式为一个字段赋值后再也没有去使用它，在1.4jdk中它会自动调用静态的初始化方法，而在jdk1.5中却不会去执行。

案例



解决方法







58.  INT_BAD_REM_BY_1



类型

必改项

描述

Any  expression (exp % 1) is guaranteed to always return zero. Did you mean (exp  & 1) or (exp % 2) instead?



案例



解决方法







59.  DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD



类型

必改项

描述

This  instruction assigns a value to a local variable, but the value is not read or  used in any subsequent instruction. Often, this indicates an error, because  the value computed is never used. There is a field with the same name as the  local variable. Did you mean to assign to that variable instead?



案例



解决方法







60.  ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD



类型

选改项

描述

This  instance method writes to a static field. This is tricky to get correct if  multiple instances are being manipulated, and generally bad practice.



案例





解决方法







61.  SWL_SLEEP_WITH_LOCK_HELD



类型

选改项

描述

This  method calls Thread.sleep() with a lock held. This may result in very poor  performance and scalability, or a deadlock, since other threads may be waiting  to acquire the lock. It is a much better idea to call wait() on the lock,  which releases the lock and allows other threads to run.



当持有对象时调用Thread.sleep（）。这可能会导致很差的性能和可扩展性，或陷入死锁，因为其他线程可能正在等待获得锁。调用wait（）是一个更好的主意，释放对象的持有以允许其他线程运行。

案例





解决方法







62.  LI_LAZY_INIT_STATIC



类型

选改项

描述

This  method contains an unsynchronized lazy initialization of a non-volatile  static field. Because the compiler or processor may reorder instructions,  threads are not guaranteed to see a completely initialized object, if the  method can be called by multiple threads. You can make the field  volatile to correct the problem. For more information, see theJava Memory Model web  site.



静态域不正确的延迟初始化。这种方法包含了一个不同步延迟初始化的非volatile静态字段。因为编译器或处理器可能会重新排列指令，如果该方法可以被多个线程调用，线程不能保证看到一个完全初始化的对象。可以让字段可变来解决此问题

案例





解决方法







63.  NM_SAME_SIMPLE_NAME_AS_SUPERCLASS



类型

选改项

描述

This  class has a simple name that is identical to that of its superclass, except  that its superclass is in a different package (e.g.,alpha.Foo  extendsbeta.Foo). This can be exceptionally confusing, create lots of situations  in which you have to look at import statements to resolve references and  creates many opportunities to accidently define methods that do not override  methods in their superclasses.



继承同一父类的子类不能使用相同的名称，即使它们位于不同的包中

案例





解决方法







64.  LI_LAZY_INIT_UPDATE_STATIC



类型

选改项

描述

This method  contains an unsynchronized lazy initialization of a static field. After the  field is set, the object stored into that location is further updated or  accessed. The setting of the field is visible to other threads as soon as it  is set. If the futher accesses in the method that set the field serve to  initialize the object, then you have avery serious multithreading  bug, unless something else prevents any other thread from accessing the  stored object until it is fully initialized.

Even if you  feel confident that the method is never called by multiple threads, it might  be better to not set the static field until the value you are setting it to  is fully populated/initialized.

这种方法包含一个不同步延迟初始化的静态字段。之后为字段赋值，对象存储到该位置后进一步更新或访问。字段后尽快让其他线程能够访问。如果该方法的进一步访问该字段为初始化对象提供服务，然后你有一个非常严重的多线程bug，除非别的东西阻止任何其他线程访问存储的对象，直到它完全初始化。

即使你有信心，该方法是永远不会被多个线程调用时，在它的值还没有被充分初始化或移动，不把它设定为static字段时它可能会更好。

案例



解决方法







65.  GC_UNRELATED_TYPES



类型

选改项

描述

This call to a generic collection method  contains an argument with an incompatible class from that of the collection's  parameter (i.e., the type of the argument is neither a supertype nor a  subtype of the corresponding generic type argument). Therefore, it is  unlikely that the collection contains any objects that are equal to the  method argument used here. Most likely, the wrong value is being passed to  the method.

In general, instances of two unrelated  classes are not equal. For example, if the Foo and Bar classes are not  related by subtyping, then an instance of Foo should not be equal to an  instance of Bar. Among other issues, doing so will likely result in an equals  method that is not symmetrical. For example, if you define the Foo class so  that a Foo can be equal to a String, your equals method isn't symmetrical  since a String can only be equal to a String.

In rare cases, people do define  nonsymmetrical equals methods and still manage to make their code work.  Although none of the APIs document or guarantee it, it is typically the case  that if you check if a Collection<String> contains a Foo, the equals  method of argument (e.g., the equals method of the Foo class) used to perform  the equality checks.





案例

Foo<A, B>  foo;
 foo.remove(a, Collections<B>.emptySet()); //This generated a false  positive because it thinks that the second parameter should be of type B, not  Set<B>

 class Foo<X, Y> extends Bar<X, Set<Y>>{
 ...
 }

 class Bar<T, S> extends ConcurrentHashMap<T, S> {
 @Override
 public boolean remove(Object key, Object value) {
 ...
 }
 }

解决方法







66.  SC_START_IN_CTOR



类型

选改项

描述

The  constructor starts a thread. This is likely to be wrong if the class is ever extended/subclassed,  since the thread will be started before the subclass constructor is started



在构造函数中启动一个线程。如果类曾经被子类扩展过，那么这很可能是错的，因为线程将在子类构造之前开始启动。

案例



解决方法







67.  EQ_ALWAYS_FALSE



类型

选该项

描述

This class defines an equals method that  always returns false. This means that an object is not equal to itself, and  it is impossible to create useful Maps or Sets of this class. More  fundamentally, it means that equals is not reflexive, one of the requirements  of the equals method.

The likely intended semantics are object  identity: that an object is equal to itself. This is the behavior inherited  from class Object. If you need to override an equals inherited from a  different superclass, you can use use:

public boolean equals(Object o) { return  this == o; }



使用equals方法返回值总是false

案例



解决方法







68.  RC_REF_COMPARISON



类型

选改项

描述

This  method compares two reference values using the == or != operator, where the  correct way to compare instances of this type is generally with the equals()  method. It is possible to create distinct instances that are equal but do not  compare as == since they are different objects. Examples of classes which  should generally not be compared by reference are java.lang.Integer,  java.lang.Float, etc.



比较两个对象值是否相等时应该采用equals方法，而不是==方法

案例



解决方法







69.  IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD



类型

选改项

描述

A  JUnit assertion is performed in a run method. Failed JUnit assertions just  result in exceptions being thrown. Thus, if this exception occurs in a thread  other than the thread that invokes the test method, the exception will  terminate the thread but not result in the test failing.



在JUnit中的断言在run方法中不会被告知

案例



解决方法







70.  STCAL_INVOKE_ON_STATIC_DATE_FORMAT_INSTANCE



类型

选改项

描述

As the JavaDoc states, DateFormats are  inherently unsafe for multithreaded use. The detector has found a call to an  instance of DateFormat that has been obtained via a static field. This looks  suspicous.

For more information on this seeSun Bug #6231579 andSun Bug #6178997.



在官方的JavaDoc，DateFormats多线程使用本事就是不安全的。探测器发现调用一个DateFormat的实例将会获得一个静态对象。

myString =  DateFormat.getDateInstance().format(myDate);



案例



解决方法







71.  STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE



类型

选改项

描述

As the JavaDoc states, DateFormats are  inherently unsafe for multithreaded use. Sharing a single instance across  thread boundaries without proper synchronization will result in erratic  behavior of the application.

You may also experience serialization  problems.

Using an instance field is recommended.

For more information on this seeSun Bug #6231579 andSun Bug #6178997.



DateFormat在多线程中本身就是不安全的，如果在线程范围中共享一个DateFormat的实例而不使用一个同步的方法在应用中就会出现一些奇怪的行为。

案例



解决方法







72.  AT_OPERATION_SEQUENCE_ON_CONCURRENT_ABSTR

ACTION



类型

选改项

描述

This  code contains a sequence of calls to a concurrent abstraction (such as a  concurrent hash map). These calls will not be executed atomically.





案例



解决方法







73.  BC_UNCONFIRMED_CAST



类型

选改项

描述

This  cast is unchecked, and not all instances of the type casted from can be cast  to the type it is being cast to. Check that your program logic ensures that  this cast will not fail.



强制类型转换操作没有经过验证，而且不是所有的此种类型装换过的类都可以再强制类型转换为原类型。在代码中需要进行逻辑判断以保证可以进行这样的操作。

案例



解决方法







74.  RV_ABSOLUTE_VALUE_OF_RANDOM_INT



类型

选改项

描述

This  code generates a random signed integer and then computes the absolute value  of that random integer. If the number returned by the random number generator  isInteger.MIN_VALUE, then the result will be negative as well (sinceMath.abs(Integer.MIN_VALUE) ==  Integer.MIN_VALUE). (Same problem arised for  long values as well).



此代码生成一个随机的符号整数，然后计算该随机整数的绝对值。如果随机数生成数绝对值为Integer.MIN_VALUE的，那么结果将是负数（因为Math.abs（Integer.MIN_VALUE的）== Integer.MIN_VALUE的）。

案例

public intgetAbsRandom() {

	  Random random = new Random();

	  int raw = random.nextInt();

	  returnMath.abs(raw);

	  }

解决方法

对产生的随机数做判断，是否为Integer.MIN_VALUE特殊情况



75.  NN_NAKED_NOTIFY



类型

选改项

描述

A call to notify() or notifyAll() was made  without any (apparent) accompanying modification to mutable object  state.  In general, calling a notify method on a monitor is done because  some condition another thread is waiting for has become true.  However,  for the condition to be meaningful, it must involve a heap object that is  visible to both threads.

This bug does not necessarily indicate an  error, since the change to mutable object state may have taken place in a  method which then called the method containing the notification.



赤裸的通知

案例

protectedvoid notifyExchange(MessageExchangeImpl me, Object lock, String from) {
		 if (LOG.isDebugEnabled()) {
			LOG.debug("Notifying exchange " + me.getExchangeId() +  "(" + Integer.toHexString(me.hashCode()) + ") in " +this + " from "+ from);
		  }
	   &nb  sp; synchronized (lock) {
			 lock.notify();
		  }
	}

解决方法







76.  BC_IMPOSSIBLE_INSTANCEOF



类型

选改项

描述

This  instanceof test will always return false. Although this is safe, make sure it  isn't an indication of some misunderstanding or some other logic error.



采用instaneof方法进行比较时总是返回false。前提是保证它不是由于某些逻辑错误造成的。

案例

publicclass Person {

   publicclass Sunextends Person

   {

		 …….

   }

   public boolean isPerson(){

	  Person obj = new Person();

	   if(obj instanceof  Sun){

		  returntrue;

	  }

	  returnfalse;

   }

解决方法

防止这类错误出现



77.  DMI_RANDOM_USED_ONLY_ONCE



类型

选改项

描述

This code creates a java.util.Random  object, uses it to generate one random number, and then discards the Random  object. This produces mediocre quality random numbers and is inefficient. If  possible, rewrite the code so that the Random object is created once and  saved, and each time a new random number is required invoke a method on the  existing Random object to obtain it.

If it is important that the generated  Random numbers not be guessable, youmust not create a new Random for  each random number; the values are too easily guessable. You should strongly  consider using a java.security.SecureRandom instead (and avoid allocating a  new SecureRandom for each random number needed).



随机创建对象只使用过一次就抛弃

案例



publicint getRandom(int seed){

	  return new  Random(seed).nextInt();

   }



解决方法

findbugs建议使用SecureRandom

publicint getRandom(byte[] seed){

	  returnnew SecureRandom(seed).nextInt();

   }





78.  NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH



类型

选改项

描述

There  is a statement or branch on an exception path that if executed guarantees  that a value is null at this point, and that value that is guaranteed to be  dereferenced (except on forward paths involving runtime exceptions).





案例



解决方法







79.  WA_NOT_IN_LOOP



类型

选改项

描述

This method contains a call to java.lang.Object.wait()  which is not in a loop.  If the monitor is used for multiple conditions,  the condition the caller intended to wait for might not be the one that  actually occurred.



这种方法包含调用java.lang.Object.wait（），而这并不是一个循环。如果监视器用于多个条件，打算调用wait()方法的条件可能不是实际发生的。



案例

protectedvoid waitForExchange(MessageExchangeImpl me, Object  lock,long timeout, String from)throws InterruptedException {

	   // If the channel is closed while here, we must abort

	  if (LOG.isDebugEnabled()) {

		  LOG.debug("Waiting for exchange " + me.getExchangeId() +" (" +  Integer.toHexString(me.hashCode()) +") to be answered in "

						   + this + " from " + from);

	   }

	   Thread th = Thread.currentThread();

	   try {

		   waiters.put(th,  Boolean.TRUE);

		   lock.wait(timeout);

	 } finally {

		   waiters.remove(th);

	 }

	   if (LOG.isDebugEnabled()) {

		  LOG.debug("Notified: " + me.getExchangeId() +"(" + Integer.toHexString(me.hashCode()) +") in " + this +" from " + from);

	   }

   }

解决方法



















整理的行业规则：

80.  OBL_UNSATISFIED_OBLIGATION



类型

必改项

描述

This method may fail to clean up (close, dispose of)  a stream, database object, or other resource requiring an explicit cleanup  operation.

In general, if a method opens a stream or other  resource, the method should use a try/finally block to ensure that the stream  or resource is cleaned up before the method returns.

This bug pattern is essentially the same as the  OS_OPEN_STREAM and ODR_OPEN_DATABASE_RESOURCE bug patterns, but is based on a  different (and hopefully better) static analysis technique. We are interested  is getting feedback about the usefulness of this bug pattern. To send  feedback, either:

send       email to findbugs@cs.umd.edu
file a bug report: http://findbugs.sourceforge.net/reportingBugs.html
In particular, the false-positive suppression  heuristics for this bug pattern have not been extensively tuned, so reports  about false positives are helpful to us.

See Weimer and Necula, Finding and Preventing  Run-Time Error Handling Mistakes, for a description of the analysis  technique.

这种方法可能无法清除（关闭，处置）一个流，数据库对象，或其他资源需要一个明确的清理行动。

一般来说，如果一个方法打开一个流或其他资源，该方法应该使用try / finally块来确保在方法返回之前流或资源已经被清除了。这种错误模式基本上和OS_OPEN_STREAM和ODR_OPEN_DATABASE_RESOURCE错误模式相同，但是是在不同在静态分析技术。我们正为这个错误模式的效用收集反馈意见。

案例



解决方法







81.  IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION



类型

必改项

描述

During  the initialization of a class, the class makes an active use of a subclass.  That subclass will not yet be initialized at the time of this use. For  example, in the following code,foo  will be null.

public class CircularClassInitialization {
   static class InnerClassSingleton extends CircularClassInitialization {
	   static InnerClassSingleton singleton = new InnerClassSingleton();
   }

   static CircularClassInitialization foo = InnerClassSingleton.singleton;
}


子类在父类未初始化之前使用父类对象实例

案例



解决方法







82.  IMSE_DONT_CATCH_IMSE



类型

必改项

描述

IllegalMonitorStateException is generally  only thrown in case of a design flaw in your code (calling wait or notify on  an object you do not hold a lock on).



捕捉违法的监控状态异常，例如当没有获取到对象锁时使用其wait和notify方法

案例



解决方法







83.  IL_INFINITE_LOOP



类型

必改项

描述

This loop doesn't seem to have a way to  terminate (other than by perhaps throwing an exception).



方法的自调用引起的死循环

案例



解决方法







84.  NP_CLOSING_NULL



类型

必改项

描述

close() is being invoked on a value that is  always null. If this statement is executed, a null pointer exception will  occur. But the big risk here you never close something that should be closed.



一个为空的对象调用close方法

案例



解决方法







85.  NP_NONNULL_PARAM_VIOLATION



类型

必改项

描述

This  method passes a null value as the parameter of a method which must be  nonnull. Either this parameter has been explicitly marked as @Nonnull, or  analysis has determined that this parameter is always dereferenced.



方法中为null的参数没有被重新赋值

案例



解决方法







86.  NP_NONNULL_RETURN_VIOLATION



类型

必改项

描述

This  method may return a null value, but the method (or a superclass method which  it overrides) is declared to return @NonNull.



方法声明了返回值不能为空，但是方法中有可能返回null

案例

void test(){

			String ss = null;

			sya(ss);

	 }     

	 public void sya(String ad){

			ad.getBytes();

	 }

解决方法







87.  NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS



类型

必改项

描述

A  possibly-null value is passed at a call site where all known target methods  require the parameter to be nonnull. Either the parameter is annotated as a  parameter that should always be nonnull, or analysis has shown that it will  always be dereferenced.



方法参数中声明为nonnull类型的参数为null

案例



解决方法







88.  NM_BAD_EQUAL



类型

必改项

描述

This  class defines a methodequal(Object).  This method does not override theequals(Object) method injava.lang.Object, which is probably what was intended.



类中定义了一个equal方法但是却不是覆写的Object对象的equals方法

案例



解决方法









89.  SA_LOCAL_SELF_COMPUTATION



类型

必改项

描述

This  method performs a nonsensical computation of a local variable with another  reference to the same variable (e.g., x&x or x-x). Because of the nature  of the computation, this operation doesn't seem to make sense, and may  indicate a typo or a logic error. Double check the computation.



此方法对同一变量执行了荒谬的计算（如x&x或x-x）操作。由于计算的性质，这一行动似乎没有意义，并可能表明错误或逻辑错误。

案例



解决方法







90.  UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR



类型

必改项

描述

This method is invoked in the constructor  of of the superclass. At this point, the fields of the class have not yet  initialized.

To make this more concrete, consider the  following classes:

When a B is constructed, the constructor  for the A class is invokedbefore the constructor for B sets value.  Thus, when the constructor for A invokes getValue, an uninitialized value is  read for value



方法被超类的构造函数调用时，在当前类中的字段或属性还没有被初始化



案例

abstract class A {

 int  hashCode;

  abstract Object getValue();

 A()  {

	hashCode = getValue().hashCode();

   }

 }

class B extends A {

  Object value;

  B(Object v) {

	this.value = v;

   }

  Object getValue() {

	return value;

 }

 }





当B是创建时，A的构造函数将在B为value赋值之前触发，然而在A的初始化方法调用getValue方法时value这个变量还没有被初始化。

解决方法







91.  DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY



类型

必改项

描述

The  code invokes toString on an (anonymous) array. Calling toString on an array  generates a fairly useless result such as [C@16f0472. Consider using Arrays.toString  to convert the array into a readable String that gives the contents of the  array. See Programming Puzzlers, chapter 3, puzzle 12.



该代码调用上匿名数组的toString（）方法，产生的结果形如[@16f0472并没有实际的意义。考虑使用Arrays.toString方法来转换成可读的字符串，提供该数组的内容数组。

案例

String[] a = { "a" };

System.out.println(a.toString());



解决方法

//正确的使用为

System.out.println(Arrays.toString(a));





92.  UWF_UNWRITTEN_FIELD



类型

必改项

描述

This  field is never written.  All reads of it will return the default value.  Check for errors (should it have been initialized?), or remove it if it is  useless.



此字段是永远不会写入值。所有读取将返回默认值。检查错误（如果它被初始化？），如果它确实没有用就删除掉。

案例



解决方法







93.  DM_STRING_VOID_CTOR



类型

必改项

描述

Creating  a newjava.lang.String object using the no-argument constructor wastes memory because  the object so created will be functionally indistinguishable from the empty  string constant "".  Java guarantees that identical string constants will be  represented by the sameString object.  Therefore, you should just use the empty string  constant directly.



使用没有参数的构造方法去创建新的String对象是浪费内存空间的，因为这样创建会和空字符串“”混淆。Java中保证完成相同的构造方法会产生描绘相同的String对象。所以你只要使用空字符串来创建就可以了。

案例



解决方法







94.  SBSC_USE_STRINGBUFFER_CONCATENATION



类型

必改项

描述

The method seems to be building a String  using concatenation in a loop. In each iteration, the String is converted to  a StringBuffer/StringBuilder, appended to, and converted back to a String.  This can lead to a cost quadratic in the number of iterations, as the growing  string is recopied in each iteration.

Better performance can be obtained by using  a StringBuffer (or StringBuilder in Java 1.5) explicitly.

For example:



在循环中构建一个String对象时从性能上讲使用StringBuffer来代替String对象

案例

// This is bad

  String s = "";

 for  (int i = 0; i < field.length; ++i) {

   s  = s + field[i];

 }



解决方法

// This is better

  StringBuffer buf = new StringBuffer();

 for  (int i = 0; i < field.length; ++i) {

	buf.append(field[i]);

 }

  String s = buf.toString();





95.  BC_BAD_CAST_TO_CONCRETE_COLLECTION



类型

必改项

描述

This  code casts an abstract collection (such as a Collection, List, or Set) to a  specific concrete implementation (such as an ArrayList or HashSet). This  might not be correct, and it may make your code fragile, since it makes it  harder to switch to other concrete implementations at a future point. Unless  you have a particular reason to do so, just use the abstract collection  class.



代码把抽象的集合（如List，Set，或Collection）强制转换为具体落实类型（如一个ArrayList或HashSet）。这可能不正确，也可能使您的代码很脆弱，因为它使得难以在今后的切换指向其他具体实现。除非你有特别理由这样做，否则只需要使用抽象的集合类。

案例



解决方法







96.  DLS_DEAD_LOCAL_STORE



类型

必改项

描述

This instruction assigns a value to a local  variable, but the value is not read or used in any subsequent instruction.  Often, this indicates an error, because the value computed is never used.

Note that Sun's javac compiler often  generates dead stores for final local variables. Because FindBugs is a  bytecode-based tool, there is no easy way to eliminate these false positives.



该指令为局部变量赋值，但在其后的没有对她做任何使用。通常，这表明一个错误，因为值从未使用过。

案例



解决方法







97.  NS_NON_SHORT_CIRCUIT



类型

必改项

描述

This code seems to be using  non-short-circuit logic (e.g., & or |) rather than short-circuit logic  (&& or ||). Non-short-circuit logic causes both sides of the  expression to be evaluated even when the result can be inferred from knowing  the left-hand side. This can be less efficient and can result in errors if  the left-hand side guards cases when evaluating the right-hand side can  generate an error.

See the Java Language Specification for details



代码中使用（& or |）代替（&& or ||）操作，会引起不安全的操作

案例



解决方法







98.  RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE



类型

必改项

描述

This  method contains a redundant check of a known non-null value against the  constant null.



方法中对不为空的值进行为空的判断

案例



解决方法







99.  RI_REDUNDANT_INTERFACES



类型

必改项

描述

This  class declares that it implements an interface that is also implemented by a  superclass. This is redundant because once a superclass implements an  interface, all subclasses by default also implement this interface. It may  point out that the inheritance hierarchy has changed since this class was  created, and consideration should be given to the ownership of the  interface's implementation.



子类和父类都实现了同一个接口，这种定义是多余的。

案例



解决方法







100.          SA_LOCAL_DOUBLE_ASSIGNMENT



类型

必改项

描述

This method contains a double assignment of  a local variable; e.g.

  public void foo() {

	int x,y;

   x  = x = 17;

 }

Assigning the same value to a variable  twice is useless, and may indicate a logic error or typo.



为一个局部变量两次赋值，这样是没有意义的

案例



解决方法







101.          SA_LOCAL_SELF_ASSIGNMENT



类型

必改项

描述

This method contains a self assignment of a  local variable; e.g.

  public void foo() {

	int x = 3;

   x  = x;

 }

Such assignments are useless, and may  indicate a logic error or typo



局部变量使用自身给自己赋值

案例



解决方法







102.          SF_SWITCH_NO_DEFAULT



类型

必改项

描述

This  method contains a switch statement where default case is missing. Usually you  need to provide a default case.



Switch没有默认情况下执行的case语句

案例



解决方法







103.          UCF_USELESS_CONTROL_FLOW



类型

必改项

描述

This method contains a useless control flow  statement, where control flow continues onto the same place regardless of  whether or not the branch is taken. For example, this is caused by having an  empty statement block for an if statement:



	if (argv.length == 0) {

	// TODO: handle this case

   }



没有任何作用的条件语句

案例



解决方法







104.          UCF_USELESS_CONTROL_FLOW_NEXT_LINE



类型

必改项

描述

This method contains a useless control flow  statement in which control flow follows to the same or following line  regardless of whether or not the branch is taken. Often, this is caused by  inadvertently using an empty statement as the body of an if statement, e.g.:





无效的条件控制语句

案例

if (argv.length == 1);

	   System.out.println("Hello, " + argv[0]);

无效的条件控制语句，注意if (argv.length == 1);以“;”结尾，下面的语句无论是否满足都会运行。

解决方法







105.          NP_BOOLEAN_RETURN_NULL



类型

必改项

描述

A  method that returns either Boolean.TRUE, Boolean.FALSE or null is an accident  waiting to happen. This method can be invoked as though it returned a value  of type boolean, and the compiler will insert automatic unboxing of the  Boolean value. If a null value is returned, this will result in a  NullPointerException.



返回值为boolean类型的方法直接返回null，这样会导致空指针异常

案例



解决方法







106.          SI_INSTANCE_BEFORE_FINALS_ASSIGNED



类型

必改项

描述

The  class's static initializer creates an instance of the class before all of the  static final fields are assigned.



在所有的static final字段赋值之前去使用静态初始化的方法创建一个类的实例。

案例



解决方法







107.          VA_FORMAT_STRING_BAD_ARGUMENT



类型

必改项

描述

The format string placeholder is  incompatible with the corresponding argument. For example,  System.out.println("%d\n", "hello");

The %d placeholder requires a numeric  argument, but a string value is passed instead. A runtime exception will  occur when this statement is executed.



错误使用参数类型来格式化字符串



案例



解决方法







108.          MF_METHOD_MASKS_FIELD



类型

必改项

描述

This  method defines a local variable with the same name as a field in this class  or a superclass. This may cause the method to read an uninitialized value  from the field, leave the field uninitialized, or both.



在方法中定义的局部变量和类变量或者父类变量同名，从而引起字段混淆。

案例



解决方法







109.          NP_ARGUMENT_MIGHT_BE_NULL



类型

必改项

描述

A  parameter to this method has been identified as a value that should always be  checked to see whether or not it is null, but it is being dereferenced  without a preceding null check.



方法没有判断参数是否为空

案例



解决方法







110.          MWN_MISMATCHED_NOTIFY



类型

必改项

描述

This  method calls Object.notify() or Object.notifyAll() without obviously holding  a lock on the object.  Calling notify() or notifyAll() without a lock  held will result in anIllegalMonitorStateException being thrown.



此方法调用Object.notify（）或Object.notifyAll（）而没有获取到该对象的对象锁。调用notify（）或notifyAll（）而没有持有该对象的对象锁，将导致IllegalMonitorStateException异常。

案例



解决方法







111.          MWN_MISMATCHED_WAIT



类型

必改项

描述

This  method calls Object.wait() without obviously holding a lock on the  object.  Calling wait() without a lock held will result in anIllegalMonitorStateException being thrown.



此方法调用Object.wait()而没有获取到该对象的对象锁。调用wait（）而没有持有该对象的对象锁，将导致IllegalMonitorStateException异常。

案例



解决方法







112.          UL_UNRELEASED_LOCK



类型

必改项

描述

This method  acquires a JSR-166 (java.util.concurrent) lock, but does not release it on  all paths out of the method. In general, the correct idiom for using a  JSR-166 lock is:

  Lock l = ...;

  l.lock();

  try {

	  // do something

  } finally {

	  l.unlock();

  }





方法获得了当前的对象所，但是在方法中始终没有释放它

案例

Lock l = ...;

  l.lock();

  try {

	  // do something

  } finally {

	  l.unlock();

  }

解决方法
