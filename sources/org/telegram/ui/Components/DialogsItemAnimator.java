package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.ui.Cells.DialogCell;

public class DialogsItemAnimator extends SimpleItemAnimator {
    private static TimeInterpolator sDefaultInterpolator = new DecelerateInterpolator();
    private int bottomClip;
    ArrayList<RecyclerView.ViewHolder> mAddAnimations = new ArrayList<>();
    ArrayList<ArrayList<RecyclerView.ViewHolder>> mAdditionsList = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mChangeAnimations = new ArrayList<>();
    ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mMoveAnimations = new ArrayList<>();
    ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingRemovals = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mRemoveAnimations = new ArrayList<>();
    private DialogCell removingDialog;
    private int topClip;

    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder, List<Object> list) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onAllAnimationsDone() {
        throw null;
    }

    private static class MoveInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder holder;
        public int toX;
        public int toY;

        MoveInfo(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4) {
            this.holder = viewHolder;
            this.fromX = i;
            this.fromY = i2;
            this.toX = i3;
            this.toY = i4;
        }
    }

    private static class ChangeInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder newHolder;
        public RecyclerView.ViewHolder oldHolder;
        public int toX;
        public int toY;

        private ChangeInfo(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            this.oldHolder = viewHolder;
            this.newHolder = viewHolder2;
        }

        ChangeInfo(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4) {
            this(viewHolder, viewHolder2);
            this.fromX = i;
            this.fromY = i2;
            this.toX = i3;
            this.toY = i4;
        }

        public String toString() {
            return "ChangeInfo{oldHolder=" + this.oldHolder + ", newHolder=" + this.newHolder + ", fromX=" + this.fromX + ", fromY=" + this.fromY + ", toX=" + this.toX + ", toY=" + this.toY + '}';
        }
    }

    public void runPendingAnimations() {
        boolean z = !this.mPendingRemovals.isEmpty();
        boolean z2 = !this.mPendingMoves.isEmpty();
        boolean z3 = !this.mPendingChanges.isEmpty();
        boolean z4 = !this.mPendingAdditions.isEmpty();
        if (z || z2 || z4 || z3) {
            Iterator<RecyclerView.ViewHolder> it = this.mPendingRemovals.iterator();
            while (it.hasNext()) {
                animateRemoveImpl(it.next());
            }
            this.mPendingRemovals.clear();
            if (z2) {
                ArrayList arrayList = new ArrayList(this.mPendingMoves);
                this.mMovesList.add(arrayList);
                this.mPendingMoves.clear();
                new Runnable(arrayList) {
                    public final /* synthetic */ ArrayList f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        DialogsItemAnimator.this.lambda$runPendingAnimations$0$DialogsItemAnimator(this.f$1);
                    }
                }.run();
            }
            if (z3) {
                ArrayList arrayList2 = new ArrayList(this.mPendingChanges);
                this.mChangesList.add(arrayList2);
                this.mPendingChanges.clear();
                new Runnable(arrayList2) {
                    public final /* synthetic */ ArrayList f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        DialogsItemAnimator.this.lambda$runPendingAnimations$1$DialogsItemAnimator(this.f$1);
                    }
                }.run();
            }
            if (z4) {
                ArrayList arrayList3 = new ArrayList(this.mPendingAdditions);
                this.mAdditionsList.add(arrayList3);
                this.mPendingAdditions.clear();
                new Runnable(arrayList3) {
                    public final /* synthetic */ ArrayList f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        DialogsItemAnimator.this.lambda$runPendingAnimations$2$DialogsItemAnimator(this.f$1);
                    }
                }.run();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runPendingAnimations$0 */
    public /* synthetic */ void lambda$runPendingAnimations$0$DialogsItemAnimator(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            MoveInfo moveInfo = (MoveInfo) it.next();
            animateMoveImpl(moveInfo.holder, (RecyclerView.ItemAnimator.ItemHolderInfo) null, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
        }
        arrayList.clear();
        this.mMovesList.remove(arrayList);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runPendingAnimations$1 */
    public /* synthetic */ void lambda$runPendingAnimations$1$DialogsItemAnimator(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            animateChangeImpl((ChangeInfo) it.next());
        }
        arrayList.clear();
        this.mChangesList.remove(arrayList);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$runPendingAnimations$2 */
    public /* synthetic */ void lambda$runPendingAnimations$2$DialogsItemAnimator(ArrayList arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            animateAddImpl((RecyclerView.ViewHolder) it.next());
        }
        arrayList.clear();
        this.mAdditionsList.remove(arrayList);
    }

    public boolean animateRemove(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo) {
        resetAnimation(viewHolder);
        this.mPendingRemovals.add(viewHolder);
        return true;
    }

    public void prepareForRemove() {
        this.topClip = Integer.MAX_VALUE;
        this.bottomClip = Integer.MAX_VALUE;
        this.removingDialog = null;
    }

    private void animateRemoveImpl(final RecyclerView.ViewHolder viewHolder) {
        final View view = viewHolder.itemView;
        this.mRemoveAnimations.add(viewHolder);
        if (view instanceof DialogCell) {
            final DialogCell dialogCell = (DialogCell) view;
            this.removingDialog = dialogCell;
            if (this.topClip != Integer.MAX_VALUE) {
                int measuredHeight = dialogCell.getMeasuredHeight();
                int i = this.topClip;
                this.bottomClip = measuredHeight - i;
                this.removingDialog.setTopClip(i);
                this.removingDialog.setBottomClip(this.bottomClip);
            } else if (this.bottomClip != Integer.MAX_VALUE) {
                int measuredHeight2 = dialogCell.getMeasuredHeight() - this.bottomClip;
                this.topClip = measuredHeight2;
                this.removingDialog.setTopClip(measuredHeight2);
                this.removingDialog.setBottomClip(this.bottomClip);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                dialogCell.setElevation(-1.0f);
                dialogCell.setOutlineProvider((ViewOutlineProvider) null);
            }
            ObjectAnimator duration = ObjectAnimator.ofFloat(dialogCell, AnimationProperties.CLIP_DIALOG_CELL_PROGRESS, new float[]{1.0f}).setDuration(180);
            duration.setInterpolator(sDefaultInterpolator);
            duration.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DialogsItemAnimator.this.dispatchRemoveStarting(viewHolder);
                }

                public void onAnimationEnd(Animator animator) {
                    animator.removeAllListeners();
                    dialogCell.setClipProgress(0.0f);
                    if (Build.VERSION.SDK_INT >= 21) {
                        dialogCell.setElevation(0.0f);
                    }
                    DialogsItemAnimator.this.dispatchRemoveFinished(viewHolder);
                    DialogsItemAnimator.this.mRemoveAnimations.remove(viewHolder);
                    DialogsItemAnimator.this.dispatchFinishedWhenDone();
                }
            });
            duration.start();
            return;
        }
        final ViewPropertyAnimator animate = view.animate();
        animate.setDuration(180).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DialogsItemAnimator.this.dispatchRemoveStarting(viewHolder);
            }

            public void onAnimationEnd(Animator animator) {
                animate.setListener((Animator.AnimatorListener) null);
                view.setAlpha(1.0f);
                DialogsItemAnimator.this.dispatchRemoveFinished(viewHolder);
                DialogsItemAnimator.this.mRemoveAnimations.remove(viewHolder);
                DialogsItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        resetAnimation(viewHolder);
        View view = viewHolder.itemView;
        if (view instanceof DialogCell) {
            ((DialogCell) view).setClipProgress(1.0f);
        } else {
            view.setAlpha(0.0f);
        }
        this.mPendingAdditions.add(viewHolder);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void animateAddImpl(final RecyclerView.ViewHolder viewHolder) {
        final View view = viewHolder.itemView;
        this.mAddAnimations.add(viewHolder);
        if (view instanceof DialogCell) {
            final DialogCell dialogCell = (DialogCell) view;
            ObjectAnimator duration = ObjectAnimator.ofFloat(dialogCell, AnimationProperties.CLIP_DIALOG_CELL_PROGRESS, new float[]{0.0f}).setDuration(180);
            duration.setInterpolator(sDefaultInterpolator);
            duration.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DialogsItemAnimator.this.dispatchAddStarting(viewHolder);
                }

                public void onAnimationCancel(Animator animator) {
                    dialogCell.setClipProgress(0.0f);
                }

                public void onAnimationEnd(Animator animator) {
                    animator.removeAllListeners();
                    DialogsItemAnimator.this.dispatchAddFinished(viewHolder);
                    DialogsItemAnimator.this.mAddAnimations.remove(viewHolder);
                    DialogsItemAnimator.this.dispatchFinishedWhenDone();
                }
            });
            duration.start();
            return;
        }
        final ViewPropertyAnimator animate = view.animate();
        animate.alpha(1.0f).setDuration(180).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DialogsItemAnimator.this.dispatchAddStarting(viewHolder);
            }

            public void onAnimationCancel(Animator animator) {
                view.setAlpha(1.0f);
            }

            public void onAnimationEnd(Animator animator) {
                animate.setListener((Animator.AnimatorListener) null);
                DialogsItemAnimator.this.dispatchAddFinished(viewHolder);
                DialogsItemAnimator.this.mAddAnimations.remove(viewHolder);
                DialogsItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, int i, int i2, int i3, int i4) {
        View view = viewHolder.itemView;
        int translationX = i + ((int) view.getTranslationX());
        int translationY = i2 + ((int) viewHolder.itemView.getTranslationY());
        resetAnimation(viewHolder);
        int i5 = i3 - translationX;
        int i6 = i4 - translationY;
        if (i5 == 0 && i6 == 0) {
            dispatchMoveFinished(viewHolder);
            return false;
        }
        if (i5 != 0) {
            view.setTranslationX((float) (-i5));
        }
        if (i6 != 0) {
            view.setTranslationY((float) (-i6));
        }
        this.mPendingMoves.add(new MoveInfo(viewHolder, translationX, translationY, i3, i4));
        return true;
    }

    public void onListScroll(int i) {
        if (!this.mPendingRemovals.isEmpty()) {
            int size = this.mPendingRemovals.size();
            for (int i2 = 0; i2 < size; i2++) {
                View view = this.mPendingRemovals.get(i2).itemView;
                view.setTranslationY(view.getTranslationY() + ((float) i));
            }
        }
        if (!this.mRemoveAnimations.isEmpty()) {
            int size2 = this.mRemoveAnimations.size();
            for (int i3 = 0; i3 < size2; i3++) {
                View view2 = this.mRemoveAnimations.get(i3).itemView;
                view2.setTranslationY(view2.getTranslationY() + ((float) i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void animateMoveImpl(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, int i, int i2, int i3, int i4) {
        final View view = viewHolder.itemView;
        final int i5 = i3 - i;
        final int i6 = i4 - i2;
        if (i5 != 0) {
            view.animate().translationX(0.0f);
        }
        if (i6 != 0) {
            view.animate().translationY(0.0f);
        }
        if (i2 > i4) {
            this.bottomClip = i2 - i4;
        } else {
            this.topClip = i6;
        }
        DialogCell dialogCell = this.removingDialog;
        if (dialogCell != null) {
            if (this.topClip != Integer.MAX_VALUE) {
                int measuredHeight = dialogCell.getMeasuredHeight();
                int i7 = this.topClip;
                this.bottomClip = measuredHeight - i7;
                this.removingDialog.setTopClip(i7);
                this.removingDialog.setBottomClip(this.bottomClip);
            } else if (this.bottomClip != Integer.MAX_VALUE) {
                int measuredHeight2 = dialogCell.getMeasuredHeight() - this.bottomClip;
                this.topClip = measuredHeight2;
                this.removingDialog.setTopClip(measuredHeight2);
                this.removingDialog.setBottomClip(this.bottomClip);
            }
        }
        final ViewPropertyAnimator animate = view.animate();
        this.mMoveAnimations.add(viewHolder);
        final RecyclerView.ViewHolder viewHolder2 = viewHolder;
        animate.setDuration(180).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DialogsItemAnimator.this.dispatchMoveStarting(viewHolder2);
            }

            public void onAnimationCancel(Animator animator) {
                if (i5 != 0) {
                    view.setTranslationX(0.0f);
                }
                if (i6 != 0) {
                    view.setTranslationY(0.0f);
                }
            }

            public void onAnimationEnd(Animator animator) {
                animate.setListener((Animator.AnimatorListener) null);
                DialogsItemAnimator.this.dispatchMoveFinished(viewHolder2);
                DialogsItemAnimator.this.mMoveAnimations.remove(viewHolder2);
                DialogsItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, int i, int i2, int i3, int i4) {
        if (!(viewHolder.itemView instanceof DialogCell)) {
            return false;
        }
        resetAnimation(viewHolder);
        resetAnimation(viewHolder2);
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder2.itemView.setAlpha(0.0f);
        viewHolder2.itemView.setTranslationX(0.0f);
        this.mPendingChanges.add(new ChangeInfo(viewHolder, viewHolder2, i, i2, i3, i4));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void animateChangeImpl(final ChangeInfo changeInfo) {
        final RecyclerView.ViewHolder viewHolder = changeInfo.oldHolder;
        RecyclerView.ViewHolder viewHolder2 = changeInfo.newHolder;
        if (viewHolder != null && viewHolder2 != null) {
            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(180);
            animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(viewHolder.itemView, View.ALPHA, new float[]{0.0f}), ObjectAnimator.ofFloat(viewHolder2.itemView, View.ALPHA, new float[]{1.0f})});
            this.mChangeAnimations.add(changeInfo.oldHolder);
            this.mChangeAnimations.add(changeInfo.newHolder);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DialogsItemAnimator.this.dispatchChangeStarting(changeInfo.oldHolder, true);
                    DialogsItemAnimator.this.dispatchChangeStarting(changeInfo.newHolder, false);
                }

                public void onAnimationEnd(Animator animator) {
                    viewHolder.itemView.setAlpha(1.0f);
                    animatorSet.removeAllListeners();
                    DialogsItemAnimator.this.dispatchChangeFinished(changeInfo.oldHolder, true);
                    DialogsItemAnimator.this.mChangeAnimations.remove(changeInfo.oldHolder);
                    DialogsItemAnimator.this.dispatchFinishedWhenDone();
                    DialogsItemAnimator.this.dispatchChangeFinished(changeInfo.newHolder, false);
                    DialogsItemAnimator.this.mChangeAnimations.remove(changeInfo.newHolder);
                    DialogsItemAnimator.this.dispatchFinishedWhenDone();
                }
            });
            animatorSet.start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> list, RecyclerView.ViewHolder viewHolder) {
        for (int size = list.size() - 1; size >= 0; size--) {
            ChangeInfo changeInfo = list.get(size);
            if (endChangeAnimationIfNecessary(changeInfo, viewHolder) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                list.remove(changeInfo);
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        RecyclerView.ViewHolder viewHolder = changeInfo.oldHolder;
        if (viewHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, viewHolder);
        }
        RecyclerView.ViewHolder viewHolder2 = changeInfo.newHolder;
        if (viewHolder2 != null) {
            endChangeAnimationIfNecessary(changeInfo, viewHolder2);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, RecyclerView.ViewHolder viewHolder) {
        boolean z = false;
        if (changeInfo.newHolder == viewHolder) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder != viewHolder) {
            return false;
        } else {
            changeInfo.oldHolder = null;
            z = true;
        }
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setTranslationX(0.0f);
        viewHolder.itemView.setTranslationY(0.0f);
        dispatchChangeFinished(viewHolder, z);
        return true;
    }

    public void endAnimation(RecyclerView.ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        view.animate().cancel();
        int size = this.mPendingMoves.size();
        while (true) {
            size--;
            if (size < 0) {
                break;
            } else if (this.mPendingMoves.get(size).holder == viewHolder) {
                view.setTranslationY(0.0f);
                view.setTranslationX(0.0f);
                dispatchMoveFinished(viewHolder);
                this.mPendingMoves.remove(size);
            }
        }
        endChangeAnimation(this.mPendingChanges, viewHolder);
        if (this.mPendingRemovals.remove(viewHolder)) {
            if (view instanceof DialogCell) {
                ((DialogCell) view).setClipProgress(0.0f);
            } else {
                view.setAlpha(1.0f);
            }
            dispatchRemoveFinished(viewHolder);
        }
        if (this.mPendingAdditions.remove(viewHolder)) {
            if (view instanceof DialogCell) {
                ((DialogCell) view).setClipProgress(0.0f);
            } else {
                view.setAlpha(1.0f);
            }
            dispatchAddFinished(viewHolder);
        }
        for (int size2 = this.mChangesList.size() - 1; size2 >= 0; size2--) {
            ArrayList arrayList = this.mChangesList.get(size2);
            endChangeAnimation(arrayList, viewHolder);
            if (arrayList.isEmpty()) {
                this.mChangesList.remove(size2);
            }
        }
        for (int size3 = this.mMovesList.size() - 1; size3 >= 0; size3--) {
            ArrayList arrayList2 = this.mMovesList.get(size3);
            int size4 = arrayList2.size() - 1;
            while (true) {
                if (size4 < 0) {
                    break;
                } else if (((MoveInfo) arrayList2.get(size4)).holder == viewHolder) {
                    view.setTranslationY(0.0f);
                    view.setTranslationX(0.0f);
                    dispatchMoveFinished(viewHolder);
                    arrayList2.remove(size4);
                    if (arrayList2.isEmpty()) {
                        this.mMovesList.remove(size3);
                    }
                } else {
                    size4--;
                }
            }
        }
        for (int size5 = this.mAdditionsList.size() - 1; size5 >= 0; size5--) {
            ArrayList arrayList3 = this.mAdditionsList.get(size5);
            if (arrayList3.remove(viewHolder)) {
                if (view instanceof DialogCell) {
                    ((DialogCell) view).setClipProgress(1.0f);
                } else {
                    view.setAlpha(1.0f);
                }
                dispatchAddFinished(viewHolder);
                if (arrayList3.isEmpty()) {
                    this.mAdditionsList.remove(size5);
                }
            }
        }
        this.mRemoveAnimations.remove(viewHolder);
        this.mAddAnimations.remove(viewHolder);
        this.mChangeAnimations.remove(viewHolder);
        this.mMoveAnimations.remove(viewHolder);
        dispatchFinishedWhenDone();
    }

    private void resetAnimation(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.animate().setInterpolator(sDefaultInterpolator);
        endAnimation(viewHolder);
    }

    public boolean isRunning() {
        return !this.mPendingAdditions.isEmpty() || !this.mPendingChanges.isEmpty() || !this.mPendingMoves.isEmpty() || !this.mPendingRemovals.isEmpty() || !this.mMoveAnimations.isEmpty() || !this.mRemoveAnimations.isEmpty() || !this.mAddAnimations.isEmpty() || !this.mChangeAnimations.isEmpty() || !this.mMovesList.isEmpty() || !this.mAdditionsList.isEmpty() || !this.mChangesList.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
            onAllAnimationsDone();
        }
    }

    public void endAnimations() {
        int size = this.mPendingMoves.size();
        while (true) {
            size--;
            if (size < 0) {
                break;
            }
            MoveInfo moveInfo = this.mPendingMoves.get(size);
            View view = moveInfo.holder.itemView;
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            dispatchMoveFinished(moveInfo.holder);
            this.mPendingMoves.remove(size);
        }
        for (int size2 = this.mPendingRemovals.size() - 1; size2 >= 0; size2--) {
            RecyclerView.ViewHolder viewHolder = this.mPendingRemovals.get(size2);
            View view2 = viewHolder.itemView;
            view2.setTranslationY(0.0f);
            view2.setTranslationX(0.0f);
            dispatchRemoveFinished(viewHolder);
            this.mPendingRemovals.remove(size2);
        }
        int size3 = this.mPendingAdditions.size();
        while (true) {
            size3--;
            if (size3 < 0) {
                break;
            }
            RecyclerView.ViewHolder viewHolder2 = this.mPendingAdditions.get(size3);
            View view3 = viewHolder2.itemView;
            if (view3 instanceof DialogCell) {
                ((DialogCell) view3).setClipProgress(0.0f);
            } else {
                view3.setAlpha(1.0f);
            }
            dispatchAddFinished(viewHolder2);
            this.mPendingAdditions.remove(size3);
        }
        for (int size4 = this.mPendingChanges.size() - 1; size4 >= 0; size4--) {
            endChangeAnimationIfNecessary(this.mPendingChanges.get(size4));
        }
        this.mPendingChanges.clear();
        if (isRunning()) {
            for (int size5 = this.mMovesList.size() - 1; size5 >= 0; size5--) {
                ArrayList arrayList = this.mMovesList.get(size5);
                for (int size6 = arrayList.size() - 1; size6 >= 0; size6--) {
                    MoveInfo moveInfo2 = (MoveInfo) arrayList.get(size6);
                    View view4 = moveInfo2.holder.itemView;
                    view4.setTranslationY(0.0f);
                    view4.setTranslationX(0.0f);
                    dispatchMoveFinished(moveInfo2.holder);
                    arrayList.remove(size6);
                    if (arrayList.isEmpty()) {
                        this.mMovesList.remove(arrayList);
                    }
                }
            }
            for (int size7 = this.mAdditionsList.size() - 1; size7 >= 0; size7--) {
                ArrayList arrayList2 = this.mAdditionsList.get(size7);
                for (int size8 = arrayList2.size() - 1; size8 >= 0; size8--) {
                    RecyclerView.ViewHolder viewHolder3 = (RecyclerView.ViewHolder) arrayList2.get(size8);
                    View view5 = viewHolder3.itemView;
                    if (view5 instanceof DialogCell) {
                        ((DialogCell) view5).setClipProgress(0.0f);
                    } else {
                        view5.setAlpha(1.0f);
                    }
                    dispatchAddFinished(viewHolder3);
                    arrayList2.remove(size8);
                    if (arrayList2.isEmpty()) {
                        this.mAdditionsList.remove(arrayList2);
                    }
                }
            }
            for (int size9 = this.mChangesList.size() - 1; size9 >= 0; size9--) {
                ArrayList arrayList3 = this.mChangesList.get(size9);
                for (int size10 = arrayList3.size() - 1; size10 >= 0; size10--) {
                    endChangeAnimationIfNecessary((ChangeInfo) arrayList3.get(size10));
                    if (arrayList3.isEmpty()) {
                        this.mChangesList.remove(arrayList3);
                    }
                }
            }
            cancelAll(this.mRemoveAnimations);
            cancelAll(this.mMoveAnimations);
            cancelAll(this.mAddAnimations);
            cancelAll(this.mChangeAnimations);
            dispatchAnimationsFinished();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAll(List<RecyclerView.ViewHolder> list) {
        for (int size = list.size() - 1; size >= 0; size--) {
            list.get(size).itemView.animate().cancel();
        }
    }
}
