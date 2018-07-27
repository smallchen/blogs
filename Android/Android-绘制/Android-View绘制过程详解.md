## Android View 绘制过程详解

```java
- ViewRootImpl.performDraw()
    - draw(fullRedrawNeeded)
       - // Draw with hardware renderer.
       - mAttachInfo.mThreadedRenderer.draw(mView, mAttachInfo, this);
          - choreographer = attachInfo.mViewRootImpl.mChoreographer;
          - choreographer.mFrameInfo.markDrawStart();
          - updateRootDisplayList(view, callbacks);
              - // Canvas 常见绘制过程
              - DisplayListCanvas canvas = mRootNode.start(mSurfaceWidth, mSurfaceHeight);
              - saveCount = canvas.save();
              - canvas.drawRenderNode(view.updateDisplayListIfDirty());
              - canvas.restoreToCount(saveCount);
              - 硬件绘制失败，会触发软件绘制。
          - // native方法 nSyncAndDrawFrame
          - syncResult = nSyncAndDrawFrame(mNativeProxy, frameInfo, frameInfo.length);
       - // Or Draw with software renderer.
       - drawSoftware(surface, mAttachInfo, xOffset, yOffset, scalingRequired, dirty);
          - // Canvas 常见绘制过程
          - canvas = mSurface.lockCanvas(dirty);
          - // mView 其实是 DecorView，即 FrameLayout！！开始遍历onDraw()!!!
          - mView.draw(canvas);
          - surface.unlockCanvasAndPost(canvas);
```
