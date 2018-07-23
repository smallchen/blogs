## Android判断应用是否安装


```java
//判断应用是否安装
public boolean isAppInstalled(Context context, String packageName) {  
  final PackageManager packageManager = context.getPackageManager();  
  List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);  
  if (pinfo != null) {  
	  for (int i = 0; i < pinfo.size(); i++) {  
		  if (pinfo.get(i).packageName.contains(packageName)) {
			  return true;
		  }
	  }  
  }  
  return false;  
}
```
