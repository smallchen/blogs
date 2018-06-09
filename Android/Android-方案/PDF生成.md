## PDF生成

```java
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.text.TextUtils;
import android.util.Log;

import com.jokin.commons.util.BitmapUtils;
import com.jokin.commons.util.FileUtils;
import com.jokin.libeasinote.content.OnProgressListener;
import com.jokin.libeasinote.utils.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 把内容保存为PDF的任务
 * Copy & Modify from WhiteBoard.
 */
public class SaveAsPDFTask extends BaseTask {
    private static final String TAG = "SaveAsPDFTask";

    public static final int ERROR_CODE_IMAGE_NO_EXISTS = 1;
    public static final int ERROR_CODE_EXPORT_PDF_FAIL = 2;
    public static final int ERROR_CODE_IO_EXCEPTION = 3;
    public static final int ERROR_CODE_SAVE_IMAGE_FAIL = 4;
    public static final int ERROR_CODE_OOM = 5;
    public static final int ERROR_CODE_RUNTIME_EXCEPTION = 6;
    public static final int ERROR_CODE_IO_EXCEPTION_ENOSPC = 7;

    private String mTargetPath;
    private String mPicturesFolder;
    private OnProgressListener mListener;
    private Bitmap[] mBitmaps;

    /**
     * @param targetPath  pdf path
     * @param picturesFolder pics src path
     * @param onProgressListener callback
     */
    public SaveAsPDFTask(String targetPath, String picturesFolder, OnProgressListener onProgressListener) {
        if (TextUtils.isEmpty(targetPath)) {
            throw new IllegalArgumentException("targetPath can not be empty");
        }
        mListener = onProgressListener;
        mTargetPath = targetPath;
        mPicturesFolder = picturesFolder;
        mBitmaps = null;
    }

    /**
     * NOTE: 如果Bitmap的方式内存OOM，则应该使用磁盘方式，减少内存占用。
     * @param targetPath  pdf path
     * @param bitmaps pics Bitmap
     * @param onProgressListener callback
     */
    public SaveAsPDFTask(String targetPath, Bitmap[] bitmaps, OnProgressListener onProgressListener) {
        if (TextUtils.isEmpty(targetPath)) {
            throw new IllegalArgumentException("targetPath can not be empty");
        }
        mListener = onProgressListener;
        mTargetPath = targetPath;
        mPicturesFolder = null;
        mBitmaps = bitmaps;
    }

    @Override
    public void run() {
        if (mIsCanceled) {
            if (mOnCancelListener != null) {
                mOnCancelListener.onCancel();
            }
            if (mListener != null) {
                mListener.onCancel();
            }
            return;
        }

        if (mListener != null) {
            mListener.onStart();
        }
        if (mBitmaps != null) {
            exportPdfByBitmaps();
        } else if (mPicturesFolder != null) {
            exportPdfByPath();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    private void exportPdfByPath() {
        try {
            PdfDocument document = createPdfDocument();
            File cacheDir = new File(mPicturesFolder);
            if (!cacheDir.exists()) {
                if (mListener != null) {
                    mListener.onFail(ERROR_CODE_IMAGE_NO_EXISTS);
                }
                return;
            }
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            File[] pagePictures = cacheDir.listFiles();
            int totalNum = pagePictures.length;
            int processMid = totalNum;
            for (int i = 0; i < totalNum; i++) {
                if (mIsCanceled) {
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel();
                    }
                    if (mListener != null) {
                        mListener.onCancel();
                    }
                    return;
                }

                PdfDocument.PageInfo.Builder builder = new PdfDocument.PageInfo.Builder(
                        Constants.DEFAULT_WHITEBOARD_WIDTH, Constants.DEFAULT_WHITEBOARD_HEIGHT, i);
                PdfDocument.Page pdfPage = document.startPage(builder.create());
                if (pdfPage != null) {
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmap(pagePictures[i].getAbsolutePath(), Bitmap.Config.RGB_565,
                            Constants.DEFAULT_WHITEBOARD_WIDTH, Constants.DEFAULT_WHITEBOARD_HEIGHT);
                    pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, paint);
                    document.finishPage(pdfPage);
                }
                if (mListener != null) {
                    mListener.onProgress(processMid + (i * 10 / totalNum), totalNum);
                }
            }

            if (mIsCanceled) {
                if (mOnCancelListener != null) {
                    mOnCancelListener.onCancel();
                }
                if (mListener != null) {
                    mListener.onCancel();
                }
                return;
            }

            boolean result = write(document);
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess(null);
                } else {
                    mListener.onFail(ERROR_CODE_EXPORT_PDF_FAIL);
                }
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException", e);
            if (mListener != null) {
                mListener.onFail(ERROR_CODE_RUNTIME_EXCEPTION);
            }
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError", e);
            if (mListener != null) {
                mListener.onFail(ERROR_CODE_OOM);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            if (mListener != null) {
                String message = e.getMessage();
                if (!TextUtils.isEmpty(message) && message.contains("ENOSPC")) {
                    mListener.onFail(ERROR_CODE_IO_EXCEPTION_ENOSPC);
                } else {
                    mListener.onFail(ERROR_CODE_IO_EXCEPTION);
                }
            }
        }
    }

    private void exportPdfByBitmaps() {
        try {
            PdfDocument document = createPdfDocument();
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            int totalNum = mBitmaps.length;
            int processMid = totalNum;
            for (int i = 0; i < totalNum; i++) {
                if (mIsCanceled) {
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel();
                    }
                    if (mListener != null) {
                        mListener.onCancel();
                    }
                    return;
                }

                PdfDocument.PageInfo.Builder builder = new PdfDocument.PageInfo.Builder(
                        Constants.DEFAULT_WHITEBOARD_WIDTH, Constants.DEFAULT_WHITEBOARD_HEIGHT, i);
                PdfDocument.Page pdfPage = document.startPage(builder.create());
                if (pdfPage != null) {
                    Bitmap bitmap = mBitmaps[i];
                    pdfPage.getCanvas().drawBitmap(bitmap, 0, 0, paint);
                    document.finishPage(pdfPage);
                }
                if (mListener != null) {
                    mListener.onProgress(processMid + (i * 10 / totalNum), totalNum);
                }
            }

            if (mIsCanceled) {
                if (mOnCancelListener != null) {
                    mOnCancelListener.onCancel();
                }
                if (mListener != null) {
                    mListener.onCancel();
                }
                return;
            }

            boolean result = write(document);
            if (mListener != null) {
                if (result) {
                    mListener.onSuccess(null);
                } else {
                    mListener.onFail(ERROR_CODE_EXPORT_PDF_FAIL);
                }
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException", e);
            if (mListener != null) {
                mListener.onFail(ERROR_CODE_RUNTIME_EXCEPTION);
            }
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError", e);
            if (mListener != null) {
                mListener.onFail(ERROR_CODE_OOM);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            if (mListener != null) {
                String message = e.getMessage();
                if (!TextUtils.isEmpty(message) && message.contains("ENOSPC")) {
                    mListener.onFail(ERROR_CODE_IO_EXCEPTION_ENOSPC);
                } else {
                    mListener.onFail(ERROR_CODE_IO_EXCEPTION);
                }
            }
        }
    }

    private PdfDocument createPdfDocument() {
        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.ISO_A4).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();
        return new PrintedPdfDocument(null, printAttrs);
    }

    private boolean write(PdfDocument document) throws IOException {
        File pdfFile = new File(mTargetPath);
        boolean result = FileUtils.createFile(mTargetPath, FileUtils.MODE_COVER);
        if (!result){
            return false;
        }
        BufferedOutputStream os = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFile);
            os = new BufferedOutputStream(fos);
            document.writeTo(os);
            os.flush();
        } finally {
            document.close();
            FileUtils.silentlyClose(os);
            FileUtils.silentlyClose(fos);
        }
        return true;
    }
}

```
