package org.telegram.messenger.support.widget;

import android.view.View;
import org.telegram.messenger.support.widget.RecyclerView.ItemAnimator;
import org.telegram.messenger.support.widget.RecyclerView.ItemAnimator.ItemHolderInfo;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;

public abstract class SimpleItemAnimator extends ItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleItemAnimator";
    boolean mSupportsChangeAnimations = true;

    public abstract boolean animateAdd(ViewHolder viewHolder);

    public abstract boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, int i, int i2, int i3, int i4);

    public abstract boolean animateMove(ViewHolder viewHolder, int i, int i2, int i3, int i4);

    public abstract boolean animateRemove(ViewHolder viewHolder);

    public void onAddFinished(ViewHolder viewHolder) {
    }

    public void onAddStarting(ViewHolder viewHolder) {
    }

    public void onChangeFinished(ViewHolder viewHolder, boolean z) {
    }

    public void onChangeStarting(ViewHolder viewHolder, boolean z) {
    }

    public void onMoveFinished(ViewHolder viewHolder) {
    }

    public void onMoveStarting(ViewHolder viewHolder) {
    }

    public void onRemoveFinished(ViewHolder viewHolder) {
    }

    public void onRemoveStarting(ViewHolder viewHolder) {
    }

    public boolean getSupportsChangeAnimations() {
        return this.mSupportsChangeAnimations;
    }

    public void setSupportsChangeAnimations(boolean z) {
        this.mSupportsChangeAnimations = z;
    }

    public boolean canReuseUpdatedViewHolder(ViewHolder viewHolder) {
        return !this.mSupportsChangeAnimations || viewHolder.isInvalid();
    }

    public boolean animateDisappearance(ViewHolder viewHolder, ItemHolderInfo itemHolderInfo, ItemHolderInfo itemHolderInfo2) {
        int i = itemHolderInfo.left;
        int i2 = itemHolderInfo.top;
        View view = viewHolder.itemView;
        int left = itemHolderInfo2 == null ? view.getLeft() : itemHolderInfo2.left;
        int top = itemHolderInfo2 == null ? view.getTop() : itemHolderInfo2.top;
        if (viewHolder.isRemoved() || (i == left && i2 == top)) {
            return animateRemove(viewHolder);
        }
        view.layout(left, top, view.getWidth() + left, view.getHeight() + top);
        return animateMove(viewHolder, i, i2, left, top);
    }

    public boolean animateAppearance(ViewHolder viewHolder, ItemHolderInfo itemHolderInfo, ItemHolderInfo itemHolderInfo2) {
        if (itemHolderInfo == null || (itemHolderInfo.left == itemHolderInfo2.left && itemHolderInfo.top == itemHolderInfo2.top)) {
            return animateAdd(viewHolder);
        }
        return animateMove(viewHolder, itemHolderInfo.left, itemHolderInfo.top, itemHolderInfo2.left, itemHolderInfo2.top);
    }

    public boolean animatePersistence(ViewHolder viewHolder, ItemHolderInfo itemHolderInfo, ItemHolderInfo itemHolderInfo2) {
        if (itemHolderInfo.left == itemHolderInfo2.left && itemHolderInfo.top == itemHolderInfo2.top) {
            dispatchMoveFinished(viewHolder);
            return false;
        }
        return animateMove(viewHolder, itemHolderInfo.left, itemHolderInfo.top, itemHolderInfo2.left, itemHolderInfo2.top);
    }

    public boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, ItemHolderInfo itemHolderInfo, ItemHolderInfo itemHolderInfo2) {
        int i;
        int i2;
        int i3 = itemHolderInfo.left;
        int i4 = itemHolderInfo.top;
        if (viewHolder2.shouldIgnore()) {
            int i5 = itemHolderInfo.left;
            i = itemHolderInfo.top;
            i2 = i5;
        } else {
            i2 = itemHolderInfo2.left;
            i = itemHolderInfo2.top;
        }
        return animateChange(viewHolder, viewHolder2, i3, i4, i2, i);
    }

    public final void dispatchRemoveFinished(ViewHolder viewHolder) {
        onRemoveFinished(viewHolder);
        dispatchAnimationFinished(viewHolder);
    }

    public final void dispatchMoveFinished(ViewHolder viewHolder) {
        onMoveFinished(viewHolder);
        dispatchAnimationFinished(viewHolder);
    }

    public final void dispatchAddFinished(ViewHolder viewHolder) {
        onAddFinished(viewHolder);
        dispatchAnimationFinished(viewHolder);
    }

    public final void dispatchChangeFinished(ViewHolder viewHolder, boolean z) {
        onChangeFinished(viewHolder, z);
        dispatchAnimationFinished(viewHolder);
    }

    public final void dispatchRemoveStarting(ViewHolder viewHolder) {
        onRemoveStarting(viewHolder);
    }

    public final void dispatchMoveStarting(ViewHolder viewHolder) {
        onMoveStarting(viewHolder);
    }

    public final void dispatchAddStarting(ViewHolder viewHolder) {
        onAddStarting(viewHolder);
    }

    public final void dispatchChangeStarting(ViewHolder viewHolder, boolean z) {
        onChangeStarting(viewHolder, z);
    }
}
