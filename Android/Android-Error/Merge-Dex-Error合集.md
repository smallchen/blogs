所谓dex，其实就是class。通常，unable to merge dex错误的核心是：

1. 引入的lib中，有多个同名的类，导致merge失败。
2. 引入的lib中，有多个同名的资源，导致merge失败。
3. 引入的lib中，有多个同名的xxx，导致merge失败。

**案例1**: 原本好好的项目，突然不能编译，报MultiDex Error错误。
```java

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:transformDexArchiveWithExternalLibsDexMergerForPlatform551Debug'.
> com.android.builder.dexing.DexArchiveMergerException:
Unable to merge dex

```

1. 以上错误不明显，使用`Run with --trace`打印详细。输出更详细的错误：

```java
... 47 more
Caused by: com.android.dex.DexException:
Multiple dex files define Landroid/app/ActivityThread;
// 多个文件定义了Landroid/app/ActivityThread
```

2. 意味着，错误是由定义了多个`android/app/ActivityThread`引发的。

3. AndroidStudio中`Command+O`查找`ActivityThread`类，就能发现有多个。
![F9E8DE09-5936-4A16-8A9D-026CD5A44DC2.png](https://upload-images.jianshu.io/upload_images/2166887-b16199490f544884.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

4. 该重复定义来自引用的两个sdk。

5. **为什么突然就不能编译了呢？**

以上错误来自Platform551Debug的Variant。所以打印其依赖树`./gradlew :app:dependencies --configuration platform551DebugCompileClasspath`
(Variant+CompileClasspath编译期，Variant+RuntimeClasspath运行期)

```java
---:libsystemcontract:+ -> R.0.2.12
---:libsystemcontract:latest.release -> R.0.2.12
```

可见，因为其中某个sdk并不是使用固定版本号，而是每次构建都拉取最新版本，这样导致每一次构建都是不稳定的。所以出现，好好的项目突然不能构建了。

每次构建拉取最新版本，个人觉得**不可取**：
优点：sdk版本更新的时候，不需要手动更新。
缺点：打破了版本的约束，无法做版本管理。对于App来说，版本是R.0.1，而随着构建次数的增加，依赖的sdk有可能是R.0.1／R.0.2／R.0.3（总之是最新版），这导致：

1. 每次构建拉取最新版本sdk，意味着每次构建内容都可能发生改变，App每一次构建相当于一个新版本。无法做版本管理。
2. 稳定性没法延续，构建后可能被打破。比如：构建过程sdk发生改变，可能会导致App构建失败，也可能是构建成功但运行时崩溃等等。
3. 开发者没法感知sdk何时进行了升级，升级到哪个版本。将导致无法定位问题，尤其是sdk导致的问题。更可怕的是，开发者可能想都没想到是sdk版本的问题。
4. 线上已发布的版本，难以跟踪sdk的版本号。即使跟踪构建任务，也难以确定当时构建时，sdk对应的版本。
5. sdk升级的时候，手动升级可以将控制权交给开发者。在Jenkins上构建的时候，固定版本号可以让多次构建的结果始终保持不变。没有理由在每次构建都时候拉取最新版本。



示例：

如果构建某个flavor出现此错误。执行：

`./gradlew assemble[Flavor] --stacktrace`

输出：

```java
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:transformDexArchiveWithExternalLibsDexMergerForRk3399Debug'.
> java.lang.RuntimeException: com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex

* Try:
Run with --info or --debug option to get more log output.

* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:transformDexArchiveWithExternalLibsDexMergerForRk3399Debug'.
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:100)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:70)
        at org.gradle.api.internal.tasks.execution.SkipUpToDateTaskExecuter.execute(SkipUpToDateTaskExecuter.java:62)
        at org.gradle.api.internal.tasks.execution.ResolveTaskOutputCachingStateExecuter.execute(ResolveTaskOutputCachingStateExecuter.java:54)
        at org.gradle.api.internal.tasks.execution.ValidatingTaskExecuter.execute(ValidatingTaskExecuter.java:58)
        at org.gradle.api.internal.tasks.execution.SkipEmptySourceFilesTaskExecuter.execute(SkipEmptySourceFilesTaskExecuter.java:97)
        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:87)
        at org.gradle.api.internal.tasks.execution.ResolveTaskArtifactStateTaskExecuter.execute(ResolveTaskArtifactStateTaskExecuter.java:52)
        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:52)
        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:54)
        at org.gradle.api.internal.tasks.execution.ExecuteAtMostOnceTaskExecuter.execute(ExecuteAtMostOnceTaskExecuter.java:43)
        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:34)
        at org.gradle.execution.taskgraph.DefaultTaskGraphExecuter$EventFiringTaskWorker$1.run(DefaultTaskGraphExecuter.java:248)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:336)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:328)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:199)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:110)
        at org.gradle.execution.taskgraph.DefaultTaskGraphExecuter$EventFiringTaskWorker.execute(DefaultTaskGraphExecuter.java:241)
        at org.gradle.execution.taskgraph.DefaultTaskGraphExecuter$EventFiringTaskWorker.execute(DefaultTaskGraphExecuter.java:230)
        at org.gradle.execution.taskgraph.DefaultTaskPlanExecutor$TaskExecutorWorker.processTask(DefaultTaskPlanExecutor.java:123)
        at org.gradle.execution.taskgraph.DefaultTaskPlanExecutor$TaskExecutorWorker.access$200(DefaultTaskPlanExecutor.java:79)
        at org.gradle.execution.taskgraph.DefaultTaskPlanExecutor$TaskExecutorWorker$1.execute(DefaultTaskPlanExecutor.java:104)
        at org.gradle.execution.taskgraph.DefaultTaskPlanExecutor$TaskExecutorWorker$1.execute(DefaultTaskPlanExecutor.java:98)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionPlan.execute(DefaultTaskExecutionPlan.java:625)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionPlan.executeWithTask(DefaultTaskExecutionPlan.java:580)
        at org.gradle.execution.taskgraph.DefaultTaskPlanExecutor$TaskExecutorWorker.run(DefaultTaskPlanExecutor.java:98)
        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:63)
        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:46)
        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:55)
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex
        at com.android.builder.dexing.DxDexArchiveMerger.mergeMultidex(DxDexArchiveMerger.java:266)
        at com.android.builder.dexing.DxDexArchiveMerger.mergeDexArchives(DxDexArchiveMerger.java:133)
        at com.android.build.gradle.internal.transforms.DexMergerTransformCallable.call(DexMergerTransformCallable.java:97)
        at com.android.build.gradle.internal.transforms.ExternalLibsMergerTransform.transform(ExternalLibsMergerTransform.kt:121)
        at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:222)
        at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:218)
        at com.android.builder.profile.ThreadRecorder.record(ThreadRecorder.java:102)
        at com.android.build.gradle.internal.pipeline.TransformTask.transform(TransformTask.java:213)
        at org.gradle.internal.reflect.JavaMethod.invoke(JavaMethod.java:73)
        at org.gradle.api.internal.project.taskfactory.DefaultTaskClassInfoStore$IncrementalTaskAction.doExecute(DefaultTaskClassInfoStore.java:179)
        at org.gradle.api.internal.project.taskfactory.DefaultTaskClassInfoStore$StandardTaskAction.execute(DefaultTaskClassInfoStore.java:135)
        at org.gradle.api.internal.project.taskfactory.DefaultTaskClassInfoStore$StandardTaskAction.execute(DefaultTaskClassInfoStore.java:122)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$1.run(ExecuteActionsTaskExecuter.java:121)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:336)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:328)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:199)
        at org.gradle.internal.progress.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:110)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:110)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:92)
        ... 28 more
Caused by: java.lang.RuntimeException: com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex
Caused by: com.android.builder.dexing.DexArchiveMergerException: Unable to merge dex
        at com.android.builder.dexing.DexArchiveMergerCallable.call(DexArchiveMergerCallable.java:72)
        at com.android.builder.dexing.DexArchiveMergerCallable.call(DexArchiveMergerCallable.java:36)
Caused by: com.android.dex.DexException: Multiple dex files define Lorg/intellij/lang/annotations/JdkConstants$AdjustableOrientation;
        at com.android.dx.merge.DexMerger.readSortableTypes(DexMerger.java:661)
        at com.android.dx.merge.DexMerger.getSortedTypes(DexMerger.java:616)
        at com.android.dx.merge.DexMerger.mergeClassDefs(DexMerger.java:598)
        at com.android.dx.merge.DexMerger.mergeDexes(DexMerger.java:171)
        at com.android.dx.merge.DexMerger.merge(DexMerger.java:198)
        at com.android.builder.dexing.DexArchiveMergerCallable.call(DexArchiveMergerCallable.java:61)
        ... 1 more


* Get more help at https://help.gradle.org
```

最后的是：

`Caused by: com.android.dex.DexException: Multiple dex files define Lorg/intellij/lang/annotations/JdkConstants$AdjustableOrientation;`

可见，是`annotations`重复依赖导致的错误。对于我的项目，去除多余的注解依赖即可。

```java
myflavorCompile(project(path: ':share')) {
    exclude group: 'org.jetbrains'
}
```

#### D8: Program type already present

> Task :app:transformDexArchiveWithExternalLibsDexMergerFordemoHisi510Debug FAILED
D8: Program type already present: org.intellij.lang.annotations.JdkConstants$AdjustableOrientation


FAILURE: Build failed with an exception.

原因：引入了两个`JDKConstants`，command+o可以看到两个对象:

```java
compile 'org.jetbrains:annotations-java5:15.0'
和
compile 'org.jetbrains:annotations-java13'
```
删除一个即可。
> 上面已经说过了 ～～～～
