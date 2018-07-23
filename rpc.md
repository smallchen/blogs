```java
Rxrpc.create(new Call<String>() {
   @Override
   public String do(Subscriber<? super String> subscriber) {
	   String mDeviceName = mDeviceManagerProxy.getDeviceName();
	   return deviceName;
   }
}).subsribeOn(Schedulers.newThread())
  .observeOn(AndroidSchedulers.mainThread())
  .doOnComplete(new Action<String>() {
		@Override
		public void call(String deviceName) throws Exception {
            mDeviceName = deviceName;
		}
  }).doOnTimeout(new Action() {
      @Override
      public void call(String deviceName) throws Exception {
          Toast.makeText().show();
      }
  }).subsribe();
```

简化为：

```java
Rxrpc.create(new Call<String>() {
   @Override
   public void run() {
       // default in new thread
	   mDeviceName = mDeviceManagerProxy.getDeviceName();
   }
}).doOnComplete(new Action<String>() {
		@Override
		public void call(String deviceName) throws Exception {
            // default in main thread
            mDeviceName = deviceName;
		}
  }).doOnTimeout(new Action() {
      @Override
      public void call(String deviceName) throws Exception {
          // default in main thread
          Toast.makeText().show();
      }
  }).subsribe();
```

```java
new RpcRequest() {
    @Override
    public void onRequest() throws Exception {
        mDeviceName = mDeviceManagerProxy.getDeviceName();
    }
    @Override
    public void onProgress() {
        mTextView.setText("requesting...");
    }
    @Override
    public void onComplete() {
        mTextView.setText(mDeviceName);
    }
    @Override
    public void onError(int errno, Exception e) {
        // Timeout
        if (errno == TimeoutError) {
            mTextView.setText("timeout!");
        }
    }
}.execute();
```

```java
RpcExecutor.post(new RpcRunnable1<ShareInfo[]>() {
    @Override
    public ShareInfo[] run() throws Exception {
        ShareInfo[] infos = mShareManager.getShareInfoByLabel(SHARE_LABEL);
        return infos;
    }
}, new ErrorCallback() {
    @Override
    public void onTimeout() {
        // nop
    }
}, new Complete() {
    @Override
    public void onComplete() {
        // 代码耦合，复用低！！
        mTextView.setText(result);
    }    
});



// public interface
public void query3rdShareList(RpcResult1<ShareInfo[]> result) {
    RpcExecutor.post(new RpcRunnable1<ShareInfo[]>()  {
        @Override
        public void run() throws Exception {
             mShareInfos = mShareManager.getShareInfoByLabel(SHARE_LABEL);
        }
    }, new onComplete() {
        result.onResult1(mShareInfos);
    });
}

// call
query3rdShareList(new RpcResult1<ShareInfo[]>() {
    public void onResult1(ShareInfo[] ShareInfos) {
        mShareInfos = ShareInfos;
    }
})

```
