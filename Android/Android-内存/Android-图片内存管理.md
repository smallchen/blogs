## Android 图片内存管理

非常好的一篇官方教程：
<https://developer.android.com/topic/performance/graphics/manage-memory>

```java
static boolean canUseForInBitmap(
        Bitmap candidate, BitmapFactory.Options targetOptions) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        // From Android 4.4 (KitKat) onward we can re-use if the byte size of
        // the new bitmap is smaller than the reusable bitmap candidate
        // allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
    return candidate.getWidth() == targetOptions.outWidth
            && candidate.getHeight() == targetOptions.outHeight
            && targetOptions.inSampleSize == 1;
}

/**
 * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
 */
static int getBytesPerPixel(Config config) {
    if (config == Config.ARGB_8888) {
        return 4;
    } else if (config == Config.RGB_565) {
        return 2;
    } else if (config == Config.ARGB_4444) {
        return 2;
    } else if (config == Config.ALPHA_8) {
        return 1;
    }
    return 1;
}
```

计算图片的内存大小：
`int byteCount = width * height * getBytesPerPixel(candidate.getConfig());`


### BitmapFactory.Options.inBitmap作用

inBitmap是标记Bitmap内存可复用。Bitmap内存可复用有什么用？

1. 在某些场景下，可以避免OOM。
2. 在某些场景下，可以提供内存运行效率。

情况1的实例：

目前应用的一个Bitmap已经占用了1G，现在要加载另一张同样大小的图片，通常的做法是：
`mBitmap = BitmapFactory.decodeFile()`，也即是说，先加载另一张图片，再通过覆盖引用来释放旧的图片。
这种做法，在一瞬间，内存消耗会占用2倍。假如应用可用内存仅剩余500MB，那么就会出现OOM。
所以，对于这种做法，要先释放旧的Bitmap引用，再加载新的图片。

> 先释放后加载，会导致，当加载的图片失败时，没有图片可显示

假如加载的时候，可以指定图片加载到的Bitmap内存，那么这个实例就不会出现OOM，而且不用进行内存申请和释放操作，效率也更高。

情况2的实例：

应用内有一个BitmapMerger，图片合成器，通常合并的时候都会创建新的Bitmap，然后合并。但这样会导致合并过程频繁创建和释放Bitmap内存。
使用inBitmap复用Bitmap内存，可以使得图片合成器有一个内存区用于合并，不需要进行额外的内存申请，减少GC，可以提高效率。

> 但这个方式，是牺牲了内存，换取效率。如果这个Bitmap内存可以被应用内复用，还是可以的。
