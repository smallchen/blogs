## Android Resources 加载图片

```java
textView.setBackgroundResource(R.drawable.ic_launcher_background);
```

以上述代码为例：

```java
- TextView:setBackgroundResource(@DrawableRes int resid)
- Context:getDrawable(@DrawableRes int id)
- context.getResources().getDrawable(id, getTheme())
- resources.getDrawableForDensity(id, 0, theme)
  - ResourcesImpl.getValueForDensity(id, density, value, true);
      - 这里从另一篇可以得到，主要是查询得到一个TypedValue对象。
  - ResourcesImpl.loadDrawable(this, value, id, density, theme);
      - loadDrawableForCookie(wrapper, value, id, density, null)
      - 核心是这里，通过TypedValue对象，得到图片资源的文件路径，名称等，然后读取文件。
      - file = value.string.toString()
      - name = getResourceName(id)
      // 使用AssetManager.openNonAsset打开图片资源
      - InputStream is = mAssets.openNonAsset(value.assetCookie, file, AssetManager.ACCESS_STREAMING);
      - Drawable.createFromResourceStream(resources, value, is, file, null);
          - Bitmap  bm = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
          - return drawableFromBitmap(res, bm, np, pad, opticalInsets, srcName)
```
