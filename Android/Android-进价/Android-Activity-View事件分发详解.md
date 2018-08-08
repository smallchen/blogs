[event-activity]: event-activity.png
[event-activity2]: event-activity2.png
[event-extend-relation]: event-extend-relation.png
[event-flow]: event-flow.png

1. 触摸事件都被封装为MotionEvent
2. 触摸事件有：
     - ACTION_DOWN
     - ACTION_UP
     - ACTION_MOVE
     - ACTION_POINTER_DOWN
     - ACTION_POINTER_UP
     - ACTION_CANCEL
3. 每个MotionEvent包含
     - 触摸位置
     - 触点数量
     - 事件时间戳
4. 一个“手势Gesture”是以ACTION_DOWN开始，以ACTION_UP结束。

事件的分发处理：

1. 开始于Activity的dispatchTouchEvent()
2. 分发到ViewGroup，ViewGroup可以进行截获
3. ViewGroup分发到子View
4. 要处理事件，必须对事件的ACTION_DOWN声明为感兴趣。
5. 事件处理，结束于处理事件的View；事件未处理，结束于Activity的onTouchEvent。

最初的事件，来源于Activity。是`Activity::dispatchTouchEvent`。
> 另外还有:
> `Activity::dispatchKeyEvent(KeyEvent event)`
> `Activity::dispatchTrackballEvent(MotionEvent ev)`
> `Activity::dispatchGenericMotionEvent(MotionEvent ev)`
> 等等

```java
// Activity源码

/**
 * Called to process touch screen events.  You can override this to
 * intercept all touch screen events before they are dispatched to the
 * window.  Be sure to call this implementation for touch screen events
 * that should be handled normally.
 *
 * @param ev The touch screen event.
 *
 * @return boolean Return true if this event was consumed.
 */
public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        onUserInteraction();
    }
    if (getWindow().superDispatchTouchEvent(ev)) {
        return true;
    }
    return onTouchEvent(ev);
}

/**
 * Called whenever a key, touch, or trackball event is dispatched to the
 * activity.  Implement this method if you wish to know that the user has
 * interacted with the device in some way while your activity is running.
 */
public void onUserInteraction() {
}

private Window mWindow;
public Window getWindow() {
    return mWindow;
}

final void attach(........) {
    mWindow = new PhoneWindow(this, window, activityConfigCallback);
    mWindow.setWindowControllerCallback(this);
    mWindow.setCallback(this);
    mWindow.setOnWindowDismissedCallback(this);
    mWindow.getLayoutInflater().setPrivateFactory(this);
    mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
    if (mParent != null) {
        mWindow.setContainer(mParent.getWindow());
    }
    mWindowManager = mWindow.getWindowManager();
    mCurrentConfig = config;

    mWindow.setColorMode(info.colorMode);
}
```

上面可以看到：

1. Activity::dispatchTouchEvent里面对事件进行分发，可以重写这个方法，截获所有触摸事件，使得没有任何事件可以分发到**Window**／ViewGroup／View，是所有事件的入口。

1. 分发顺序是：Activity -> Window -> ViewGroup -> ... ViewGroup -> View。

1. ACTION_DOWN是一次新的事件的开始，所以会特别对待。ViewGroup和View里面在收到ACTION_DOWN事件时，会重置自身的触摸状态，重新开始计数。

2. 仅当Activity里面的ViewGroup／View都没有消化事件，才能执行到Activity自身的onTouchEvent事件。

3. onUserInteraction是用户与Activity交互的回调，只要用户与Activity有交互，就会触发。表示用户触发了任意事件。

4. Activity里面的Window实例是`PhoneWindow`，所以superDispatchTouchEvent调用的`PhoneWindow::superDispatchTouchEvent()`.


PhoneWindow源码
/frameworks/base/core/java/com/android/internal/policy/PhoneWindow.java

```java
@Override
public boolean superDispatchKeyEvent(KeyEvent event) {
    return mDecor.superDispatchKeyEvent(event);
}

@Override
public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
    return mDecor.superDispatchKeyShortcutEvent(event);
}

@Override
public boolean superDispatchTouchEvent(MotionEvent event) {
    return mDecor.superDispatchTouchEvent(event);
}

@Override
public boolean superDispatchTrackballEvent(MotionEvent event) {
    return mDecor.superDispatchTrackballEvent(event);
}

@Override
public boolean superDispatchGenericMotionEvent(MotionEvent event) {
    return mDecor.superDispatchGenericMotionEvent(event);
}

// This is the top-level view of the window, containing the window decor.
private DecorView mDecor;
private void installDecor() {
    mForceDecorInstall = false;
    if (mDecor == null) {
        mDecor = generateDecor(-1);
        mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mDecor.setIsRootNamespace(true);
        if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
            mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
        }
    } else {
        mDecor.setWindow(this);
    }
}
```

上面可以看到：

1. PhoneWindow::superDispatchTouchEvent()调用的是mDecor::superDispatchTouchEvent().

2. mDecor是`DecorView`，是Window的rootview。`DecorView`继承于`FrameLayout`.

3. 所以最终superDispatchTouchEvent会调用到`DecorView::superDispatchTouchEvent`.

4. 也即是，Activity里接受到的事件，最先会分发到`DecorView`（当然，你可以重写方法，使其分发到自己的View).

DecorView源码
/frameworks/base/core/java/com/android/internal/policy/DecorView.java

```java
public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
    public boolean superDispatchKeyEvent(KeyEvent event) {
        // Give priority to closing action modes if applicable.
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            final int action = event.getAction();
            // Back cancels action modes first.
            if (mPrimaryActionMode != null) {
                if (action == KeyEvent.ACTION_UP) {
                    mPrimaryActionMode.finish();
                }
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
        return super.dispatchKeyShortcutEvent(event);
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return super.dispatchTrackballEvent(event);
    }

    public boolean superDispatchGenericMotionEvent(MotionEvent event) {
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onInterceptTouchEvent(event);
    }
}
```

最后还是调用到父类`FrameLayout::dispatchTouchEvent`

FrameLayout源码
/frameworks/base/core/java/android/widget/FrameLayout.java

```java
public class FrameLayout extends ViewGroup {

}
```

FrameLayout没有dispatchTouchEvent方法，意味着FrameLayout没有覆盖ViewGroup::dispatchTouchEvent。所以，最终调用到`ViewGroup::dispatchTouchEvent`

ViewGroup源码
/frameworks/base/core/java/android/view/ViewGroup.java

```java
public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(ev, 1);
        }

        // If the event targets the accessibility focused view and this is it, start
        // normal event dispatch. Maybe a descendant is what will handle the click.
        if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
            ev.setTargetAccessibilityFocus(false);
        }

        boolean handled = false;
        if (onFilterTouchEventForSecurity(ev)) {
            final int action = ev.getAction();
            final int actionMasked = action & MotionEvent.ACTION_MASK;

            // Handle an initial down.
            /// ACTION_DOWN表示一次新事件，进行了状态的清理

            if (actionMasked == MotionEvent.ACTION_DOWN) {
                // Throw away all previous state when starting a new touch gesture.
                // The framework may have dropped the up or cancel event for the previous gesture
                // due to an app switch, ANR, or some other state change.

                cancelAndClearTouchTargets(ev);
                /// 这个方法会向TouchTarget链表分发ACTION_CANCEL.

                resetTouchState();
                /// 这个方法会设置mFirstTouchTarget为null。
            }

            // Check for interception.
            /// 截获仅发生在ACTION_DOWN，或 mFirstTouchTarget不为空才进行截获（mFirstTouchTarget是指当前事件最先处理的子对象）
            /// ACTION_DOWN是初次事件，所以直接进入onInterceptTouchEvent()；
            /// mFirstTouchTarget不为空，1.可能是POINTER_DOWN/MOVE/UP等非初次事件且有子对象。

            final boolean intercepted;
            if (actionMasked == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {

                /// 能进入这里，说明：1.是ACTION_DOWN初次事件，不管有没有子对象 2.是其它POINTER_DOWN/MOVE/UP事件且当前ViewGroup有子对象。

                final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;

                /// 如果没有禁止截获，则调用onInterceptTouchEvent，也即是说onInterceptTouchEvent并不是无条件执行，还需要启用截获。
                if (!disallowIntercept) {
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action); // restore action in case it was changed
                } else {
                    intercepted = false;
                }
            } else {
                // There are no touch targets and this action is not an initial down
                // so this view group continues to intercept touches.
                /// 既不是事件的开始（DOWN事件表示事件开始），又没有可以接受事件的子对象（mFirstTouchTarget==null）则认为事件无需再往下分发。
                /// 无需往下分发，等价于当前ViewGroup截获了事件。所以设置intercepted为true。
                ///（其实还是有点本质区别，这里只是没有子对象可以接受事件，并不是ViewGroup主动截获。可以叫这种情况为叶子）

                intercepted = true;
            }

            // If intercepted, start normal event dispatch. Also if there is already
            // a view that is handling the gesture, do normal event dispatch.
            /// 这里截获为true有两种情况：
            /// 1. this.onInterceptTouchEvent 返回true。
            /// 2. 非DOWN事件且没有子对象。
            /// 所以，这里能调用，只有1情况下发生，即主动截获。
            if (intercepted || mFirstTouchTarget != null) {
                ev.setTargetAccessibilityFocus(false);
            }

            /// 上面整个过程都没有return，说明会继续执行。

            // Check for cancelation.
            final boolean canceled = resetCancelNextUpFlag(this)
                    || actionMasked == MotionEvent.ACTION_CANCEL;

            // Update list of touch targets for pointer down, if needed.
            final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0;
            TouchTarget newTouchTarget = null;
            boolean alreadyDispatchedToNewTouchTarget = false;

            /// 既非取消事件，也非叶子，且没有主动截获才能执行。（如果是叶子，无需往这个代码块看）
            if (!canceled && !intercepted) {

                // If the event is targeting accessiiblity focus we give it to the
                // view that has accessibility focus and if it does not handle it
                // we clear the flag and dispatch the event to all children as usual.
                // We are looking up the accessibility focused host to avoid keeping
                // state since these events are very rare.

                /// 如果事件目标是有焦点的，那么将事件分发给焦点View，否则清除flag，按照正常逻辑分发。

                View childWithAccessibilityFocus = ev.isTargetAccessibilityFocus()
                        ? findChildWithAccessibilityFocus() : null;

                if (actionMasked == MotionEvent.ACTION_DOWN
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                    final int actionIndex = ev.getActionIndex(); // always 0 for down
                    final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex)
                            : TouchTarget.ALL_POINTER_IDS;

                    // Clean up earlier touch targets for this pointer id in case they
                    // have become out of sync.
                    removePointersFromTouchTargets(idBitsToAssign);

                    final int childrenCount = mChildrenCount;
                    if (newTouchTarget == null && childrenCount != 0) {
                        final float x = ev.getX(actionIndex);
                        final float y = ev.getY(actionIndex);

                        // Find a child that can receive the event.
                        // Scan children from front to back.
                        /// 根据坐标点，查找ViewGroup里面可以处理这个点的Children Views，从前往后查找。

                        final ArrayList<View> preorderedList = buildOrderedChildList();
                        final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                        /// isChildrenDrawingOrderEnabled可以指定child的绘制顺序。通过重写getChildDrawingOrder。

                        final View[] children = mChildren;
                        for (int i = childrenCount - 1; i >= 0; i--) {
                            final int childIndex = customOrder
                                    ? getChildDrawingOrder(childrenCount, i) : I;

                            /// 重写getChildDrawingOrder可以改变ViewGroup绘制child的顺序。

                            final View child = (preorderedList == null)
                                    ? children[childIndex] : preorderedList.get(childIndex);

                            // If there is a view that has accessibility focus we want it
                            // to get the event first and if not handled we will perform a
                            // normal dispatch. We may do a double iteration but this is
                            // safer given the timeframe.

                            /// 如果有一个child获取了焦点，那么先让它获得事件，遍历找到那个获取了焦点的child。
                            /// 如果没有焦点，则进行普通的遍历分发，看哪个child能够处理事件。

                            if (childWithAccessibilityFocus != null) {
                                if (childWithAccessibilityFocus != child) {
                                    continue;
                                }
                                childWithAccessibilityFocus = null;
                                i = childrenCount - 1;
                            }

                            /// 1. 有焦点，找到了获取焦点的child。没焦点，child是当前遍历的child。

                            /// 当前child是否能够接受点事件，点是否在当前View里面。不是，查找下一个。
                            if (!canViewReceivePointerEvents(child)
                                    || !isTransformedTouchPointInView(x, y, child, null)) {
                                ev.setTargetAccessibilityFocus(false);
                                continue;
                            }

                            /// 找到了点所在，可以接收事件的child view。中断查找！！！（意思是，ViewGroup里面仅能有一个子View能够处理事件）
                            /// 从TouchTarget链表里查找当前child view，找到，说明DOWN事件时，child view返回了true，接收了这次事件。
                            /// 所以紧随的POINTER_DOWN／MOVE／UP事件就直接分发到该child view即可。所以退出遍历。

                            newTouchTarget = getTouchTarget(child);
                            if (newTouchTarget != null) {
                                // Child is already receiving touch within its bounds.
                                // Give it the new pointer in addition to the ones it is handling.
                                // child已经表明接收事件，所以将新的信息发给它处理。
                                newTouchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }

                            /// 如果从TouchTarget链表中找不到当前child view，则表示，这次事件是新的事件（可能是新的DOWN事件）。
                            /// 如果这个child view准备分发Up事件，先取消了Up事件分发的标识（因为有新的事件已经到来）。
                            /// 分发入口dispatchTransformedTouchEvent：参数是非cancel，对象是child。（即分发事件到child）。
                            /// 我觉得这里能够进入调用，除非是ACTION_DOWN事件，即新的一次事件到来。没有测试，不敢保证。

                            resetCancelNextUpFlag(child);
                            if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {

                                /// dispatchTransformedTouchEvent其实就是调用了dispatchTouchEvent。这里是child::dispatchTouchEvent。
                                /// 如果返回true，表示child::dispatchTouchEvent返回true，意味着child view接收这次新的事件。

                                // Child wants to receive touch within its bounds.
                                mLastTouchDownTime = ev.getDownTime();
                                if (preorderedList != null) {
                                    // childIndex points into presorted list, find original index
                                    for (int j = 0; j < childrenCount; j++) {
                                        if (children[childIndex] == mChildren[j]) {
                                            mLastTouchDownIndex = j;
                                            break;
                                        }
                                    }
                                } else {
                                    mLastTouchDownIndex = childIndex;
                                }
                                mLastTouchDownX = ev.getX();
                                mLastTouchDownY = ev.getY();

                                /// ACTION_DOWN事件后，当前的child表明自己能够接收事件，所以加入到TouchTarget链表中。
                                /// 这样，下一个ACTION_MOVE/UP等事件就直接分发到child，而不再需要这里的dispatchTouchEvent。
                                /// 由于dispatchTouchEvent已经执行过，所以标识alreadyDispatchedToNewTouchTarget为true。

                                newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                alreadyDispatchedToNewTouchTarget = true;
                                break;
                            }

                            // The accessibility focus didn't handle the event, so clear
                            // the flag and do a normal dispatch to all children.
                            ev.setTargetAccessibilityFocus(false);
                        }
                        if (preorderedList != null) preorderedList.clear();
                    }

                    /// 如果遍历了所有子View，发觉没有子View接收事件，那么将事件分发给最近响应了事件的View。

                    if (newTouchTarget == null && mFirstTouchTarget != null) {
                        // Did not find a child to receive the event.
                        // Assign the pointer to the least recently added target.
                        newTouchTarget = mFirstTouchTarget;
                        while (newTouchTarget.next != null) {
                            newTouchTarget = newTouchTarget.next;
                        }
                        newTouchTarget.pointerIdBits |= idBitsToAssign;
                    }
                }
            }

            /// 到这里可能有：
            /// 1. 叶子
            /// 2. 非叶子 且 非ACTION_DOWN的其它事件。

            // Dispatch to touch targets.
            if (mFirstTouchTarget == null) {
                /// 如果是叶子，将这叶子作为普通View分发事件。
                // No touch targets so treat this as an ordinary view.

                //重点// 事件分发!!
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            } else {
                /// 非叶子，遍历子View分发事件。
                // Dispatch to touch targets, excluding the new touch target if we already
                // dispatched to it.  Cancel touch targets if necessary.
                TouchTarget predecessor = null;
                TouchTarget target = mFirstTouchTarget;
                while (target != null) {
                    final TouchTarget next = target.next;
                    if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                        handled = true;
                    } else {
                        final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                || intercepted;

                        //重点// 事件分发!! intercepted如果为true，则cancelChild就一直为true。

                        if (dispatchTransformedTouchEvent(ev, cancelChild,
                                target.child, target.pointerIdBits)) {
                            handled = true;
                        }
                        if (cancelChild) {
                            if (predecessor == null) {
                                mFirstTouchTarget = next;
                            } else {
                                predecessor.next = next;
                            }
                            target.recycle();
                            target = next;
                            continue;
                        }
                    }
                    predecessor = target;
                    target = next;
                }
            }

            // Update list of touch targets for pointer up or cancel, if needed.
            if (canceled
                    || actionMasked == MotionEvent.ACTION_UP
                    || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                resetTouchState();
            } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
                final int actionIndex = ev.getActionIndex();
                final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
                removePointersFromTouchTargets(idBitsToRemove);
            }
        }

        if (!handled && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(ev, 1);
        }
        return handled;
    }
}

```
有点长，没关系。

1. ViewGroup继承于View，重写了dispatchTouchEvent，将事件分发给ViewGroup自身或某一个子View。

2. 收到ACTION_DOWN表示一次新的触摸事件，进行了状态的清理。

3. 先执行onInterceptTouchEvent()。但onInterceptTouchEvent并不是无条件执行，当且仅当下面条件符合才会执行：
    1. 启用了截获（先决条件）
    2. 是ACTION_DOWN事件；或者是非ACTION_DOWN事件，但必须含有子对象（非叶子）。

4. 这里的**叶子**是指：ViewGroup可能含有子View，但事件没法往下传递（主动拦截或子View不接收事件），此时称这个ViewGroup为叶子。

4. DOWN事件到来，如果ViewGroup没有截获事件，且含有子View：
    1. 先获取事件是否分发给有焦点子View，是则直接分发到有焦点的子View。
    2. 如果有焦点的子View没能处理事件，或者触点不在有焦点的子View内，那么分发到其它可能的子View。
    3. 没有焦点，遍历所有子View，找到能够接收事件，且触点在子View范围内的那个子View。
    4. 如果那个child view已经在TouchTarget链表里，那么退出查找，直接将事件分发到child view。
    5. 如果那个child view不在TouchTarget链表里，将事件分发到child view，调用child view::dispatchTouchEvent，看child view是否对事件感兴趣。
        1. 如果child view::dispatchTouchEvent返回true，则表示child view表明关心事件，那么将其加入到TouchTarget链表里。
        2. 如果child view::dispatchTouchEvent返回false，则表示虽然点是在child view里，但child view不关心事件，继续查找下一个child。
        3. **如果Down事件child view::dispatchTouchEvent返回false，将不会把child view加入TouchTarget链表中，将导致后续的POINTER_DOWN/MOVE/UP事件都不再分发给该child view，因为除了DOWN事件外，其它事件仅分发给TouchTarget链表。**（这就是onTouchEvent返回false的效果）
    6. 含有child view，但都没有child view对事件感兴趣，则将事件分发到最近响应了事件的View。

5. dispatchTransformedTouchEvent封装了下一个dispatchTouchEvent。即要么dispatchTouchEvent给自己，要么dispatchTouchEvent给子View。

5. 子View要表明自己对事件感兴趣，则需要在ACTION_DOWN时，dispatchTouchEvent返回true。即使其它ACTION情况下dispatchTouchEvent返回false也没关系，**只要ACTION_DOWN时dispatchTouchEvent返回true，属于这一次事件的MOVE/UP等都会分发到该View**（因为它已经加入了TouchTarget链表了）。

4. 仅当ViewGroup是叶子时，ViewGroup才会dispatchTouchEvent给自己。换句话，只要子View表明了接收事件，子View所在的ViewGroup就不会把事件分发给自己！**所以，如果子View表明了接收事件，那么子View所在的ViewGroup就不会产生onTouch和onTouchEvent！**

6. 子View的dispatchTouchEvent坐标系，以父View为坐标系。其中getX表示父坐标系，getRawX才是屏幕坐标系。


```java
    /**
     * Transforms a motion event into the coordinate space of a particular child view,
     * filters out irrelevant pointer ids, and overrides its action if necessary.
     * If child is null, assumes the MotionEvent will be sent to this ViewGroup instead.
     * 根据参数有不同的行为。关键的参数是 boolean cancel，View child。
     * 如果cancel是true，表示分发取消事件，否则分发的是参数中的MotionEvent。
     * 如果child是null，表示分发对象是ViewGroup自身。
     * 如果child非null，表示分发对象是子child对象。
     *
     * 也即是说，这个方法用来分发MotionEvent到参数中的View child，如果View child为null，则分发到ViewGroup自身。
     */
    private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
            View child, int desiredPointerIdBits) {
        final boolean handled;

        // Canceling motions is a special case.  We don't need to perform any transformations
        // or filtering.  The important part is the action, not the contents.

        /// 如果是取消事件：分发取消事件完成后，直接返回。

        final int oldAction = event.getAction();
        if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            if (child == null) {
                /// 此次调用，分发对象是自身
                handled = super.dispatchTouchEvent(event);
            } else {
                /// 此次调用，分发对象的是子child对象
                handled = child.dispatchTouchEvent(event);
            }
            event.setAction(oldAction);
            return handled;
        }

        // Calculate the number of pointers to deliver.
        final int oldPointerIdBits = event.getPointerIdBits();
        final int newPointerIdBits = oldPointerIdBits & desiredPointerIdBits;

        /// 计算此次事件对应的触点数量，即pointerCount和对应的数组。

        // If for some reason we ended up in an inconsistent state where it looks like we
        // might produce a motion event with no pointers in it, then drop the event.

        /// 如果发觉信息不合法，直接认为分发没有处理，返回false。
        if (newPointerIdBits == 0) {
            return false;
        }

        // If the number of pointers is the same and we don't need to perform any fancy
        // irreversible transformations, then we can reuse the motion event for this
        // dispatch as long as we are careful to revert any changes we make.
        // Otherwise we need to make a copy.

        // 如果触点数量和上一次一样，我们可以小心的重用，否则需要复制。

        final MotionEvent transformedEvent;
        if (newPointerIdBits == oldPointerIdBits) {
            if (child == null || child.hasIdentityMatrix()) {
                if (child == null) {
                    /// 此次调用，分发对象是自身
                    handled = super.dispatchTouchEvent(event);
                } else {
                    /// 此次调用，分发对象是子child对象，坐标系是相对于ViewGroup自身，重新计算坐标。
                    final float offsetX = mScrollX - child.mLeft;
                    final float offsetY = mScrollY - child.mTop;
                    event.offsetLocation(offsetX, offsetY);

                    handled = child.dispatchTouchEvent(event);

                    event.offsetLocation(-offsetX, -offsetY);
                    /// 分发完成，恢复坐标系。（这就是小心重用）
                }
                return handled;
            }
            transformedEvent = MotionEvent.obtain(event);
        } else {
            transformedEvent = event.split(newPointerIdBits);
        }

        /// 到达这里有点复杂，只有两种情况：
        /// 1. newPointerIdBits != oldPointerIdBits 可以到达。
        /// 2. newPointerIdBits == oldPointerIdBits 且 child != null 且 child.hasIdentityMatrix()为false 可以到达。
        /// 3. 其它情况则已经返回了。

        // Perform any necessary transformations and dispatch.
        if (child == null) {
            /// 此次调用，分发对象是自身
            handled = super.dispatchTouchEvent(transformedEvent);
        } else {
            /// 此次调用，分发对象是子child对象，坐标系是相对于ViewGroup自身，重新计算坐标。
            final float offsetX = mScrollX - child.mLeft;
            final float offsetY = mScrollY - child.mTop;
            transformedEvent.offsetLocation(offsetX, offsetY);
            if (! child.hasIdentityMatrix()) {
                transformedEvent.transform(child.getInverseMatrix());
            }

            handled = child.dispatchTouchEvent(transformedEvent);
        }

        // Done.
        transformedEvent.recycle();
        return handled;
    }

```

1. 这个方法是dispatchTouchEvent的统一入口，如果参数View child不为null，则分发到View child，否则分发到ViewGroup自身。

2. 参数中的int desiredPointerIdBits和触摸事件MotionEvent的封装有关，只是从MotionEvent里拿数据。

3. 返回值就是dispatchTouchEvent的返回值，false表示未消化，true表示消化。

4. View child并不是仅仅只是View对象，它的指向可以是任何继承于View的对象。

5. 仅当ViewGroup是叶子时，才调用View::dispatchTouchEvent。仅当事件是分发给自身时，才需要调用View::dispatchTouchEvent。换句话，事件分发最后，只要到了叶子，都会调用到View::dispatchTouchEvent。

6. 事件的分发，一个MotionEvent对象跨越了一系列调用链，调用过程不能随意修改MotionEvent的数据，所以修改了要进行还原；或者修改前复制一个副本，在副本上修改。

View源码
/frameworks/base/core/java/android/view/View.java

```java
    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     *
     * 表示事件已经分发到目标了。
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        /// 获取了焦点。
        // If the event should be handled by accessibility focus first.
        if (event.isTargetAccessibilityFocus()) {
            // We don't have focus or no virtual descendant has it, do not handle the event.
            if (!isAccessibilityFocusedViewOrHost()) {
                return false;
            }
            // We have focus and got the event, then use normal event dispatch.
            event.setTargetAccessibilityFocus(false);
        }

        boolean result = false;

        if (mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onTouchEvent(event, 0);
        }

        /// ACTION_DOWN表示新的一次事件到来，清除状态

        final int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            // Defensive cleanup for new gesture
            stopNestedScroll();
        }

        if (onFilterTouchEventForSecurity(event)) {
            //noinspection SimplifiableIfStatement
            ListenerInfo li = mListenerInfo;

            /// 如果是enabled，且注册了onTouchListener，则调用当前View的onTouch!!  enabled的真假，并不影响onTouchEvent的调用！

            if (li != null && li.mOnTouchListener != null
                    && (mViewFlags & ENABLED_MASK) == ENABLED
                    && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }

            /// 紧接着，如果onTouch返回的是false，表示onTouch未消化事件，则调用当前View的onTouchEvent！！！
           /// 注意，如果enabled为false，也会执行onTouchEvent。

            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }

        if (!result && mInputEventConsistencyVerifier != null) {
            mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }

        // Clean up after nested scrolls if this is the end of a gesture;
        // also cancel it if we tried an ACTION_DOWN but we didn't want the rest
        // of the gesture.
        if (actionMasked == MotionEvent.ACTION_UP ||
                actionMasked == MotionEvent.ACTION_CANCEL ||
                (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
            stopNestedScroll();
        }

        return result;
    }
```

从没觉得如此简单：

1. 无论是ViewGroup还是View，onTouch和onTouchEvent都是在dispatchTouchEvent中触发。调用顺序为：dispatchTouchEvent - onTouch - onTouchEvent。
    1. 对于View，由于是叶子，毫无疑问，就是：View::dispatchTouchEvent - View::onTouch - View::onTouchEvent。
    2. 对于ViewGroup，如果是叶子，也是：ViewGroup::dispatchTouchEvent - ViewGroup::onTouch - ViewGroup::onTouchEvent。
    3. 对于ViewGroup，如果不是叶子，是：ViewGroup::dispatchTouchEvent - ChildView::dispatchTouchEvent(onTouch - onTouchEvent都在此触发) - ViewGroup::onTouch - ViewGroup::onTouchEvent。

2. 事件分发就是这么一回事，都在一个dispatchTouchEvent方法内就分发完成。

3. Enabled下，当目标Target自身的onTouch返回false，自身的onTouchEvent才能被执行。而Disabled下，onTouch不会调用，但onTouchEvent能调用！！

4. dispatchTouchEvent返回false，**仅当**onTouch返回false**且**onTouchEvent返回false。表示目标不关心事件。

5. dispatchTouchEvent返回true，可以是onTouch返回true，或者onTouch返回false，但onTouchEvent返回true。表示目标关心事件。

6. 要表明当前View对事件感兴趣，则需要在ACTION_DOWN时，dispatchTouchEvent返回true。

大致的调用链（**核心**）：
```java
// 事件分发入口
dispatchTouchEvent() {
   onIntercept
   dispatchTouchEvent() {
         onIntercept
         dispatchTouchEvent() {
                onIntercept
                ...
                dispatchTouchEvent() {（叶子）
                      if (! onTouch()) {
                          onTouchEvent()
                      }
                }
                ...
                onTouch
                onTouchEvent
         }
         onTouch
         onTouchEvent
   }
   onTouch
   onTouchEvent
}
```
1. 对于事件分发的入口，只需要一个。**所以如果你要自定义一个事件分发器，仅需一个方法接口就足够了。**

2. 调用链自上而下，只要其中一个方法返回**true**，则表示事件被消化，分发完成！！所以，**越在调用链下面，调用的条件越苛刻**。

2. 对于Target自身，onTouch和onTouchEvent并没有什么区别，仅仅是调用先后顺序不同而已。（这也解释了，GestureDetector既可以放在目标的onTouch，也可以放在目标的onTouchEvent）

3. **父View有拦截的权利，子View有优先响应事件的权利。**

3. onIntercept可以提供截获的机会，返回**true**表示停止调用链的下潜，将自身作为叶子看待。

5. **原生不支持调用链从头到尾都能执行。**虽然所有onIntercept返回false，所有dispatchTouchEvent返回false，所有onTouch返回false，所有onTouchEvent返回false时，ACTION_DOWN事件可以在调用链执行一遍，但也仅仅限于ACTION_DOWN事件。**要真正实现一个完整的事件可以被调用链所有View消化，必须重写掉TouchTarget链表。**

附录：
![继承关系图][event-extend-relation]

实例讲解。
![一般的视图][event-activity]

图中，有以下实例：
1. Activity实例
2. ListView实例 - ViewGroup - View
3. TextView实例 - View

**涉及继承，就有覆盖重写，override会导致调用顺序发生改变！最重要看清实例！实例！实例！**

事件分发模型：
```java
// 事件分发入口
Activity::dispatchTouchEvent(MotionEvent ev)
  |
PhoneWindow::superDispatchTouchEvent(MotionEvent ev)
  |
DecorView::superDispatchTouchEvent(MotionEvent event)
  |
FrameLayout::dispatchTouchEvent(MotionEvent event)
  |
ViewGroup::dispatchTouchEvent(MotionEvent ev)
DecorView::onInterceptTouchEvent(MotionEvent ev)
FrameLayout::onInterceptTouchEvent(MotionEvent ev)
ViewGroup::onInterceptTouchEvent(MotionEvent ev)
  |
        ListView::dispatchTouchEvent(MotionEvent ev)
           |
        ViewGroup::dispatchTouchEvent(MotionEvent ev)
        ListView::onInterceptTouchEvent(MotionEvent ev)
        ViewGroup::onInterceptTouchEvent(MotionEvent ev)
                     |
                  TextView::dispatchTouchEvent(MotionEvent ev)
                     |
                  View::dispatchTouchEvent(MotionEvent ev)
                  TextView::onTouch(MotionEvent ev)
                  View::onTouch(MotionEvent ev)
                  TextView::onTouchEvent(MotionEvent ev)
                  View::onTouchEvent(MotionEvent ev)
         |
       ListView::onTouch(MotionEvent ev)
       ViewGroup::onTouch(MotionEvent ev)
       View::onTouch(MotionEvent ev)
       ListView::onTouchEvent(MotionEvent ev)
       ViewGroup::onTouchEvent(MotionEvent ev)
       View::onTouchEvent(MotionEvent ev)
....
....
```

ListView只是举例，可以使用RelativeLayout代替。

功能1: 点击TextView能够响应。

1. 要事件传递到TextView，则说明从上而下，调用链要直达TextView。所以TextView之前的onIntercept要返回false，这样dispatch才能分发到TextView。

2. TextView的dispatchTouchEvent要返回true，表明TextView关心事件！

3. dispatchTouchEvent要返回true，要么TextView的onTouch返回true，要么TextView的onTouchEvent返回true。要么TextView重写dispatchTouchEvent返回true。

功能2: 禁用TextView的响应。

1. 要禁用TextView的响应，只需要从调用链到TextView之前进行截获即可。所以，ListView重写onInterceptTouchEvent返回true，将ListView作为叶子，事件就不会再往下分发（包括ACTION_DOWN事件）。

2. TextView中重写View的dispatchTouchEvent返回false，表明TextView不关心事件（但仍旧会收到ACTION_DOWN事件）。如果重写过程如果不调用super，将导致onTouch和onTouchEvent都不会调用到。

3. TextView中重写View的onTouch返回false，重写View的onTouchEvent返回false（但也仍旧能收到ACTION_DOWN事件）。

4. 一旦TextView在第一次dispatchTouchEvent中返回false，后续将不再收到MOVE/UP等事件，直到下一次ACTION_DOWN到来（**见上面的TouchTarget链表**)。

5. **由于调用链中都返回false，所以ACTION_DOWN事件在调用链中执行了一遍，但也仅仅只有ACTION_DOWN事件。后续的事件被截获！**

```java
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyTextView: onTouch() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyTextView: onTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyViewGroup: onTouch() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MyViewGroup: onTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=17.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366519, downTime=745366519, deviceId=7, source=0x1002 }]
// ACTION_DOWN在调用链中执行了一次，后续的事件就被顶层截获消化
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366534, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366534, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=745366551, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=745366551, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=745366568, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=745366568, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366584, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366584, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366618, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=745366618, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366618, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366618, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366629, downTime=745366519, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=121.887146, y[0]=263.86258, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=745366629, downTime=745366519, deviceId=7, source=0x1002 }]
```

功能3：重写TextView的dispatchTouchEvent为

```java
@Override
public boolean dispatchTouchEvent(MotionEvent event) {
    Log.d(TAG, "dispatchTouchEvent() called with: event = [" + event + "]");
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        return true;
    }
    return super.dispatchTouchEvent(event);
}
```
输出：

```java
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506673, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506673, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506673, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506673, downTime=750506673, deviceId=7, source=0x1002 }]

// MyTextView在action=ACTION_DOWN时直接返回true，导致onTouch和onTouchEvent没能执行。
// 由于表明了关心事件，下面的ACTION_MOVE继续分发到TextView，此时由于调用的是super，所以会执行到onTouch和onTouchEvent。

D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouch() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750506690, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouch() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750506723, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouch() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506737, downTime=750506673, deviceId=7, source=0x1002 }]

D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouch() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MyTextView: onTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=24.858917, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=131.8779, y[0]=270.85892, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750506740, downTime=750506673, deviceId=7, source=0x1002 }]
```

改为：

```java
```java
@Override
public boolean dispatchTouchEvent(MotionEvent event) {
    Log.d(TAG, "dispatchTouchEvent() called with: event = [" + event + "]");
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        return true;
    }
    return false;
}
```

输出：

```java
D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911608, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911608, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911608, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_DOWN, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911608, downTime=750911608, deviceId=7, source=0x1002 }]

// 在DOWN事件中，TextView::dispatchTouchEvent直接返回true，导致onTouch和onTouchEvent都没有调用。

D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750911624, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750911624, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750911624, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750911624, downTime=750911608, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=1, eventTime=750911624, downTime=750911608, deviceId=7, source=0x1002 }]

// 在其它事件中，TextView::dispatchTouchEvent直接返回false，导致onTouch和onTouchEvent都没有调用，直接将事件抛到上层。
// 由于TextView声明为关心事件，所以ViewGroup不会分发到自身，自然就没有onTouch和onTouchEvent。

D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750911641, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750911641, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750911641, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750911641, downTime=750911608, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_MOVE, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=2, eventTime=750911641, downTime=750911608, deviceId=7, source=0x1002 }]

D/MainActivity: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911721, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: dispatchTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911721, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyViewGroup: onInterceptTouchEvent() called with: ev = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911721, downTime=750911608, deviceId=7, source=0x1002 }]
D/MyTextView: dispatchTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=38.851624, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911721, downTime=750911608, deviceId=7, source=0x1002 }]
D/MainActivity: onTouchEvent() called with: event = [MotionEvent { action=ACTION_UP, actionButton=0, id[0]=0, x[0]=118.889915, y[0]=284.85162, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=750911721, downTime=750911608, deviceId=7, source=0x1002 }]
```


## 图解：

![点击][event-activity2.png]
![事件流][event-flow.png]
如图，
事件分发，从窗口到Activity到ViewGroup再到View，经历着ABCD四个节点。
**事件分发从父到子**，向下分发（父的dispatch先于子的dispatch）。
**事件响应从子到父**，向上冒泡，子不响应父才有机会响应（子的onTouch先于父的onTouch）。
**DOWN事件**，最深分发到最先返回true的那个View。
**其它事件**，最深分发到上面DOWN事件记录的那个View，按DOWN事件记录的路径分发。
**只有DOWN事件关心触点范围的View，其它事件按DOWN事件记录的路径依次分发到最深的那个View**

一次完整的事件分发流图
```java
DOWN - Activity - ...- ViewGroupZ - ViewA - onTouch - true - END
MOVE - Activity - ...- ViewGroupZ - ViewA - onTouch - ? - END
UP - Activity - ...- ViewGroupZ - ViewA - onTouch - ? - END
```
意思是：
DOWN事件，最先返回true的是ViewA。
后续的MOVE／UP事件，最深分发到ViewA，此时无论MOVE／UP是否在ViewA之上，都按照DOWN事件记录的到ViewA的路径分发。ViewA返回true分发结束；返回false事件冒泡，但没有意义，因为DOWN事件仅被ViewA消化了。**虽然最深可以到ViewA，但分发过程可以截获**。

总结，**DOWN事件返回true的那个View决定了事件向下分发的深度，决定了其它事件向下分发的路径，决定了事件最终响应的View**。

以上是不改动Android的事件分发机制情况下，正常的分发流程。可以通过修改其中某个节点，改变事件的分发和响应：

1. 修改时机A：父可以在onIntercept截获事件，优先处理。**如果父在DOWN事件时没有截获，但在其它事件将其截获，将导致子收到ACTION_CANCEL**。当且仅当在onIntercept截获才有事件。在dispatch时截获不会有事件。ACTION_CANCEL向下分发最深到DOWN记录的View，向上冒泡不超过进行截获的View。
2. 修改时机B：子可以在onTouch处理事件。
3. 修改时机C：子可以在onTouchEvent处理事件（效果同B）。
4. 修改时机D：父可以重写dispatch分发函数，将事件一一分发到子View而不管子View的返回值，实现**事件透传，多级响应**。

## 总结：

1.  dispatchTouchEvent在ACTION_DOWN返回true，表示关心事件；此时包含此View的ViewGroup将不会有onTouch和onTouchEvent发生（相当于子View吃掉了）。

2. dispatchTouchEvent在ACTION_DOWN返回false，表示不关心事件；后续的ACTION_DOWN/UP等事件都不会再分发到当前View，而是被父截获（相当于子View不吃就没机会再吃）。

3. dispatchTouchEvent的返回值，可以通过dispatchTouchEvent／onTouch／onTouchEvent控制。onTouch和onTouchEvent由`super.dispatchTouchEvent`调用，并返回其返回值。

5. onTouch前提是开启了Enabled。

6. **onTouchEvent前提是Disabled。或者Enabled状态下，onTouch返回false**（测试过）。

5. ACTION_DOWN是一次手势事件／触摸事件的开始。

6. ViewGroup才有onInterceptTouchEvent。

7. onInterceptTouchEvent前提是没有禁用截获。

8. ACTION_DOWN在目标外，然后划过目标释放，目标不会收到任何事件。ACTION_DOWN在目标内，然后划出目标释放，目标会收到父发送过来的ACTION_MOVE/UP等事件。

9. **事件都是先从父dispatcher然后到子dispatcher，如果不拦截，先到子的onTouch／onTouchEvent，后到父的onTouch／onTouchEvent。** 所以，在子View上点击，子View是有优先响应权的。


## 错误

其实，View的分发还有一种，就是`Split_Touch_Event`。表示每个子View单独响应`TouchEvent`。

效果就是，多指可以移动多个子View。每个子View有独立的事件分发。每个子View都可以接收`Down`事件。

默认是开启了`SPLIT_TOUCH_EVENT`这个Flag的。

比如：当touch第一个子View时，返回down，touch第二个子View时，也返回down，则两个子View都是独立的一套分发。
