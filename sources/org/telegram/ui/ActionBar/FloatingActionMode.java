package org.telegram.ui.ActionBar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.PopupWindow.OnDismissListener;
import java.util.Arrays;
import org.telegram.messenger.AndroidUtilities;

@TargetApi(23)
public final class FloatingActionMode extends ActionMode {
    private static final int MAX_HIDE_DURATION = 3000;
    private static final int MOVING_HIDE_DELAY = 50;
    private final int mBottomAllowance;
    private final Callback2 mCallback;
    private final Rect mContentRect;
    private final Rect mContentRectOnScreen;
    private final Context mContext;
    private final Point mDisplaySize;
    private FloatingToolbar mFloatingToolbar;
    private FloatingToolbarVisibilityHelper mFloatingToolbarVisibilityHelper;
    private final Runnable mHideOff = new Runnable() {
        public void run() {
            if (FloatingActionMode.this.isViewStillActive()) {
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.setHideRequested(false);
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
            }
        }
    };
    private final Menu mMenu;
    private final Runnable mMovingOff = new Runnable() {
        public void run() {
            if (FloatingActionMode.this.isViewStillActive()) {
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.setMoving(false);
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
            }
        }
    };
    private final View mOriginatingView;
    private final Rect mPreviousContentRectOnScreen;
    private final int[] mPreviousViewPositionOnScreen;
    private final Rect mPreviousViewRectOnScreen;
    private final int[] mRootViewPositionOnScreen;
    private final Rect mScreenRect;
    private final int[] mViewPositionOnScreen;
    private final Rect mViewRectOnScreen;

    private static final class FloatingToolbarVisibilityHelper {
        private static final long MIN_SHOW_DURATION_FOR_MOVE_HIDE = 500;
        private boolean mActive;
        private boolean mHideRequested;
        private long mLastShowTime;
        private boolean mMoving;
        private boolean mOutOfBounds;
        private final FloatingToolbar mToolbar;
        private boolean mWindowFocused = true;

        public FloatingToolbarVisibilityHelper(FloatingToolbar floatingToolbar) {
            this.mToolbar = floatingToolbar;
        }

        public void activate() {
            this.mHideRequested = false;
            this.mMoving = false;
            this.mOutOfBounds = false;
            this.mWindowFocused = true;
            this.mActive = true;
        }

        public void deactivate() {
            this.mActive = false;
            this.mToolbar.dismiss();
        }

        public void setHideRequested(boolean z) {
            this.mHideRequested = z;
        }

        public void setMoving(boolean z) {
            Object obj = System.currentTimeMillis() - this.mLastShowTime > 500 ? 1 : null;
            if (!z || obj != null) {
                this.mMoving = z;
            }
        }

        public void setOutOfBounds(boolean z) {
            this.mOutOfBounds = z;
        }

        public void setWindowFocused(boolean z) {
            this.mWindowFocused = z;
        }

        public void updateToolbarVisibility() {
            if (this.mActive) {
                if (this.mHideRequested || this.mMoving || this.mOutOfBounds || !this.mWindowFocused) {
                    this.mToolbar.hide();
                } else {
                    this.mToolbar.show();
                    this.mLastShowTime = System.currentTimeMillis();
                }
            }
        }
    }

    public View getCustomView() {
        return null;
    }

    public CharSequence getSubtitle() {
        return null;
    }

    public CharSequence getTitle() {
        return null;
    }

    public void setCustomView(View view) {
    }

    public void setSubtitle(int i) {
    }

    public void setSubtitle(CharSequence charSequence) {
    }

    public void setTitle(int i) {
    }

    public void setTitle(CharSequence charSequence) {
    }

    public FloatingActionMode(Context context, Callback2 callback2, View view, FloatingToolbar floatingToolbar) {
        this.mContext = context;
        this.mCallback = callback2;
        PopupMenu popupMenu = new PopupMenu(context, null);
        this.mMenu = popupMenu.getMenu();
        setType(1);
        popupMenu.setOnMenuItemClickListener(new -$$Lambda$FloatingActionMode$UF9AqZsp5KWdxpBshC-kIC1foHc(this));
        this.mContentRect = new Rect();
        this.mContentRectOnScreen = new Rect();
        this.mPreviousContentRectOnScreen = new Rect();
        this.mViewPositionOnScreen = new int[2];
        this.mPreviousViewPositionOnScreen = new int[2];
        this.mRootViewPositionOnScreen = new int[2];
        this.mViewRectOnScreen = new Rect();
        this.mPreviousViewRectOnScreen = new Rect();
        this.mScreenRect = new Rect();
        this.mOriginatingView = view;
        this.mOriginatingView.getLocationOnScreen(this.mViewPositionOnScreen);
        this.mBottomAllowance = AndroidUtilities.dp(20.0f);
        this.mDisplaySize = new Point();
        setFloatingToolbar(floatingToolbar);
    }

    public /* synthetic */ boolean lambda$new$0$FloatingActionMode(MenuItem menuItem) {
        return this.mCallback.onActionItemClicked(this, menuItem);
    }

    private void setFloatingToolbar(FloatingToolbar floatingToolbar) {
        this.mFloatingToolbar = floatingToolbar.setMenu(this.mMenu).setOnMenuItemClickListener(new -$$Lambda$FloatingActionMode$hR9ZZtWiw1UsA14pSX4ELgwU9cs(this));
        this.mFloatingToolbarVisibilityHelper = new FloatingToolbarVisibilityHelper(this.mFloatingToolbar);
        this.mFloatingToolbarVisibilityHelper.activate();
    }

    public /* synthetic */ boolean lambda$setFloatingToolbar$1$FloatingActionMode(MenuItem menuItem) {
        return this.mCallback.onActionItemClicked(this, menuItem);
    }

    public void invalidate() {
        this.mCallback.onPrepareActionMode(this, this.mMenu);
        invalidateContentRect();
    }

    public void invalidateContentRect() {
        this.mCallback.onGetContentRect(this, this.mOriginatingView, this.mContentRect);
        repositionToolbar();
    }

    public void updateViewLocationInWindow() {
        this.mOriginatingView.getLocationOnScreen(this.mViewPositionOnScreen);
        this.mOriginatingView.getRootView().getLocationOnScreen(this.mRootViewPositionOnScreen);
        this.mOriginatingView.getGlobalVisibleRect(this.mViewRectOnScreen);
        Rect rect = this.mViewRectOnScreen;
        int[] iArr = this.mRootViewPositionOnScreen;
        rect.offset(iArr[0], iArr[1]);
        if (!Arrays.equals(this.mViewPositionOnScreen, this.mPreviousViewPositionOnScreen) || !this.mViewRectOnScreen.equals(this.mPreviousViewRectOnScreen)) {
            repositionToolbar();
            int[] iArr2 = this.mPreviousViewPositionOnScreen;
            iArr = this.mViewPositionOnScreen;
            iArr2[0] = iArr[0];
            iArr2[1] = iArr[1];
            this.mPreviousViewRectOnScreen.set(this.mViewRectOnScreen);
        }
    }

    private void repositionToolbar() {
        Rect rect;
        this.mContentRectOnScreen.set(this.mContentRect);
        ViewParent parent = this.mOriginatingView.getParent();
        int[] iArr;
        if (parent instanceof ViewGroup) {
            parent.getChildVisibleRect(this.mOriginatingView, this.mContentRectOnScreen, null);
            rect = this.mContentRectOnScreen;
            iArr = this.mRootViewPositionOnScreen;
            rect.offset(iArr[0], iArr[1]);
        } else {
            rect = this.mContentRectOnScreen;
            iArr = this.mViewPositionOnScreen;
            rect.offset(iArr[0], iArr[1]);
        }
        if (isContentRectWithinBounds()) {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(false);
            rect = this.mContentRectOnScreen;
            rect.set(Math.max(rect.left, this.mViewRectOnScreen.left), Math.max(this.mContentRectOnScreen.top, this.mViewRectOnScreen.top), Math.min(this.mContentRectOnScreen.right, this.mViewRectOnScreen.right), Math.min(this.mContentRectOnScreen.bottom, this.mViewRectOnScreen.bottom + this.mBottomAllowance));
            if (!this.mContentRectOnScreen.equals(this.mPreviousContentRectOnScreen)) {
                this.mOriginatingView.removeCallbacks(this.mMovingOff);
                this.mFloatingToolbarVisibilityHelper.setMoving(true);
                this.mOriginatingView.postDelayed(this.mMovingOff, 50);
                this.mFloatingToolbar.setContentRect(this.mContentRectOnScreen);
                this.mFloatingToolbar.updateLayout();
            }
        } else {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(true);
            this.mContentRectOnScreen.setEmpty();
        }
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mPreviousContentRectOnScreen.set(this.mContentRectOnScreen);
    }

    private boolean isContentRectWithinBounds() {
        ((WindowManager) this.mContext.getSystemService(WindowManager.class)).getDefaultDisplay().getRealSize(this.mDisplaySize);
        Rect rect = this.mScreenRect;
        Point point = this.mDisplaySize;
        rect.set(0, 0, point.x, point.y);
        if (intersectsClosed(this.mContentRectOnScreen, this.mScreenRect) && intersectsClosed(this.mContentRectOnScreen, this.mViewRectOnScreen)) {
            return true;
        }
        return false;
    }

    private static boolean intersectsClosed(Rect rect, Rect rect2) {
        return rect.left <= rect2.right && rect2.left <= rect.right && rect.top <= rect2.bottom && rect2.top <= rect.bottom;
    }

    public void hide(long j) {
        if (j == -1) {
            j = ViewConfiguration.getDefaultActionModeHideDuration();
        }
        j = Math.min(3000, j);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
        if (j <= 0) {
            this.mHideOff.run();
            return;
        }
        this.mFloatingToolbarVisibilityHelper.setHideRequested(true);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mOriginatingView.postDelayed(this.mHideOff, j);
    }

    public void setOutsideTouchable(boolean z, OnDismissListener onDismissListener) {
        this.mFloatingToolbar.setOutsideTouchable(z, onDismissListener);
    }

    public void onWindowFocusChanged(boolean z) {
        this.mFloatingToolbarVisibilityHelper.setWindowFocused(z);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
    }

    public void finish() {
        reset();
        this.mCallback.onDestroyActionMode(this);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    private void reset() {
        this.mFloatingToolbar.dismiss();
        this.mFloatingToolbarVisibilityHelper.deactivate();
        this.mOriginatingView.removeCallbacks(this.mMovingOff);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
    }

    private boolean isViewStillActive() {
        return this.mOriginatingView.getWindowVisibility() == 0 && this.mOriginatingView.isShown();
    }
}
