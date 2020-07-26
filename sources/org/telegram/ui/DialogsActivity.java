package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Property;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$Chat;
import org.telegram.tgnet.TLRPC$ChatFull;
import org.telegram.tgnet.TLRPC$Dialog;
import org.telegram.tgnet.TLRPC$EncryptedChat;
import org.telegram.tgnet.TLRPC$InputDialogPeer;
import org.telegram.tgnet.TLRPC$TL_dialogFolder;
import org.telegram.tgnet.TLRPC$TL_inputFolderPeer;
import org.telegram.tgnet.TLRPC$TL_messages_updateDialogFilter;
import org.telegram.tgnet.TLRPC$User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Cells.AccountSelectCell;
import org.telegram.ui.Cells.ArchiveHintInnerCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DialogsItemAnimator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.FiltersListBottomSheet;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.PacmanAnimation;
import org.telegram.ui.Components.ProxyDrawable;
import org.telegram.ui.Components.PullForegroundDrawable;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.DialogsActivity;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    public static boolean[] dialogsLoaded = new boolean[3];
    /* access modifiers changed from: private */
    public static ArrayList<TLRPC$Dialog> frozenDialogsList;
    /* access modifiers changed from: private */
    public static final Interpolator interpolator = $$Lambda$DialogsActivity$HkM_rV7AfnHydLTgaNFWuC0Nrc.INSTANCE;
    public static float viewOffset = 0.0f;
    public final Property<DialogsActivity, Float> SCROLL_Y = new AnimationProperties.FloatProperty<DialogsActivity>("animationValue") {
        public void setValue(DialogsActivity dialogsActivity, float f) {
            dialogsActivity.setScrollY(f);
        }

        public Float get(DialogsActivity dialogsActivity) {
            return Float.valueOf(DialogsActivity.this.actionBar.getTranslationY());
        }
    };
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private ActionBarMenuSubItem addToFolderItem;
    private String addToGroupAlertString;
    /* access modifiers changed from: private */
    public float additionalFloatingTranslation;
    /* access modifiers changed from: private */
    public float additionalOffset;
    private boolean afterSignup;
    /* access modifiers changed from: private */
    public boolean allowMoving;
    /* access modifiers changed from: private */
    public boolean allowSwipeDuringCurrentTouch;
    private boolean allowSwitchAccount;
    /* access modifiers changed from: private */
    public boolean animatingForward;
    private ActionBarMenuItem archive2Item;
    private ActionBarMenuSubItem archiveItem;
    private boolean askAboutContacts = true;
    private boolean askingForPermissions;
    /* access modifiers changed from: private */
    public boolean backAnimation;
    private BackDrawable backDrawable;
    private ActionBarMenuSubItem blockItem;
    private View blurredView;
    private int canClearCacheCount;
    private boolean canDeletePsaSelected;
    private int canMuteCount;
    private int canPinCount;
    private int canReadCount;
    private int canReportSpamCount;
    /* access modifiers changed from: private */
    public boolean canShowHiddenArchive;
    private int canUnarchiveCount;
    private int canUnmuteCount;
    private boolean cantSendToChannels;
    private boolean checkCanWrite;
    private boolean checkPermission = true;
    private ActionBarMenuSubItem clearItem;
    private boolean closeSearchFieldOnHide;
    /* access modifiers changed from: private */
    public ChatActivityEnterView commentView;
    private int currentConnectionState;
    /* access modifiers changed from: private */
    public DialogsActivityDelegate delegate;
    private ActionBarMenuItem deleteItem;
    /* access modifiers changed from: private */
    public int dialogChangeFinished;
    /* access modifiers changed from: private */
    public int dialogInsertFinished;
    /* access modifiers changed from: private */
    public int dialogRemoveFinished;
    /* access modifiers changed from: private */
    public boolean dialogsListFrozen;
    /* access modifiers changed from: private */
    public DialogsSearchAdapter dialogsSearchAdapter;
    /* access modifiers changed from: private */
    public boolean disableActionBarScrolling;
    /* access modifiers changed from: private */
    public ActionBarMenuItem doneItem;
    /* access modifiers changed from: private */
    public AnimatorSet doneItemAnimator;
    /* access modifiers changed from: private */
    public FilterTabsView filterTabsView;
    private ImageView floatingButton;
    /* access modifiers changed from: private */
    public FrameLayout floatingButtonContainer;
    /* access modifiers changed from: private */
    public float floatingButtonHideProgress;
    /* access modifiers changed from: private */
    public float floatingButtonTranslation;
    /* access modifiers changed from: private */
    public boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
    /* access modifiers changed from: private */
    public int folderId;
    /* access modifiers changed from: private */
    public FragmentContextView fragmentContextView;
    /* access modifiers changed from: private */
    public FragmentContextView fragmentLocationContextView;
    private int hasPoll;
    private int initialDialogsType;
    /* access modifiers changed from: private */
    public boolean lastSearchScrolledToTop;
    /* access modifiers changed from: private */
    public int maximumVelocity;
    /* access modifiers changed from: private */
    public boolean maybeStartTracking;
    /* access modifiers changed from: private */
    public MenuDrawable menuDrawable;
    private int messagesCount;
    /* access modifiers changed from: private */
    public ArrayList<MessagesController.DialogFilter> movingDialogFilters = new ArrayList<>();
    /* access modifiers changed from: private */
    public DialogCell movingView;
    /* access modifiers changed from: private */
    public boolean movingWas;
    private ActionBarMenuItem muteItem;
    /* access modifiers changed from: private */
    public boolean onlySelect;
    /* access modifiers changed from: private */
    public long openedDialogId;
    /* access modifiers changed from: private */
    public PacmanAnimation pacmanAnimation;
    /* access modifiers changed from: private */
    public ActionBarMenuItem passcodeItem;
    /* access modifiers changed from: private */
    public boolean passcodeItemVisible;
    private AlertDialog permissionDialog;
    private ActionBarMenuSubItem pin2Item;
    private ActionBarMenuItem pinItem;
    /* access modifiers changed from: private */
    public int prevPosition;
    /* access modifiers changed from: private */
    public int prevTop;
    private ProxyDrawable proxyDrawable;
    /* access modifiers changed from: private */
    public ActionBarMenuItem proxyItem;
    /* access modifiers changed from: private */
    public boolean proxyItemVisible;
    private ActionBarMenuSubItem readItem;
    private ActionBarMenuSubItem removeFromFolderItem;
    private boolean resetDelegate = true;
    /* access modifiers changed from: private */
    public AnimatorSet scrimAnimatorSet;
    /* access modifiers changed from: private */
    public Paint scrimPaint;
    /* access modifiers changed from: private */
    public ActionBarPopupWindow scrimPopupWindow;
    /* access modifiers changed from: private */
    public ActionBarMenuSubItem[] scrimPopupWindowItems;
    /* access modifiers changed from: private */
    public View scrimView;
    /* access modifiers changed from: private */
    public int[] scrimViewLocation = new int[2];
    /* access modifiers changed from: private */
    public boolean scrimViewSelected;
    private boolean scrollBarVisible = true;
    /* access modifiers changed from: private */
    public boolean scrollUpdated;
    /* access modifiers changed from: private */
    public boolean scrollingManually;
    /* access modifiers changed from: private */
    public AnimatorSet searchAnimator;
    private long searchDialogId;
    /* access modifiers changed from: private */
    public EmptyTextProgressView searchEmptyView;
    /* access modifiers changed from: private */
    public ActionBarMenuItem searchItem;
    /* access modifiers changed from: private */
    public RecyclerListView searchListView;
    private TLObject searchObject;
    /* access modifiers changed from: private */
    public String searchString;
    /* access modifiers changed from: private */
    public boolean searchWas;
    /* access modifiers changed from: private */
    public boolean searching;
    /* access modifiers changed from: private */
    public LinearLayoutManager searchlayoutManager;
    private String selectAlertString;
    private String selectAlertStringGroup;
    /* access modifiers changed from: private */
    public ArrayList<Long> selectedDialogs = new ArrayList<>();
    private NumberTextView selectedDialogsCountTextView;
    private String showingSuggestion;
    private RecyclerView sideMenu;
    /* access modifiers changed from: private */
    public DialogCell slidingView;
    /* access modifiers changed from: private */
    public long startArchivePullingTime;
    /* access modifiers changed from: private */
    public boolean startedTracking;
    /* access modifiers changed from: private */
    public ActionBarMenuItem switchItem;
    /* access modifiers changed from: private */
    public Animator tabsAlphaAnimator;
    /* access modifiers changed from: private */
    public AnimatorSet tabsAnimation;
    /* access modifiers changed from: private */
    public boolean tabsAnimationInProgress;
    /* access modifiers changed from: private */
    public int topPadding;
    /* access modifiers changed from: private */
    public UndoView[] undoView = new UndoView[2];
    /* access modifiers changed from: private */
    public boolean updatePullAfterScroll;
    /* access modifiers changed from: private */
    public ViewPage[] viewPages;
    /* access modifiers changed from: private */
    public boolean waitingForScrollFinished;

    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity dialogsActivity, ArrayList<Long> arrayList, CharSequence charSequence, boolean z);
    }

    static /* synthetic */ boolean lambda$createView$4(View view, MotionEvent motionEvent) {
        return true;
    }

    static /* synthetic */ float lambda$static$0(float f) {
        float f2 = f - 1.0f;
        return (f2 * f2 * f2 * f2 * f2) + 1.0f;
    }

    private class ViewPage extends FrameLayout {
        /* access modifiers changed from: private */
        public int archivePullViewState;
        /* access modifiers changed from: private */
        public DialogsAdapter dialogsAdapter;
        /* access modifiers changed from: private */
        public DialogsItemAnimator dialogsItemAnimator;
        /* access modifiers changed from: private */
        public int dialogsType;
        /* access modifiers changed from: private */
        public ItemTouchHelper itemTouchhelper;
        /* access modifiers changed from: private */
        public int lastItemsCount;
        /* access modifiers changed from: private */
        public LinearLayoutManager layoutManager;
        /* access modifiers changed from: private */
        public DialogsRecyclerView listView;
        /* access modifiers changed from: private */
        public RadialProgressView progressView;
        /* access modifiers changed from: private */
        public PullForegroundDrawable pullForegroundDrawable;
        /* access modifiers changed from: private */
        public RecyclerAnimationScrollHelper scrollHelper;
        /* access modifiers changed from: private */
        public int selectedType;
        /* access modifiers changed from: private */
        public SwipeController swipeController;

        static /* synthetic */ int access$6408(ViewPage viewPage) {
            int i = viewPage.lastItemsCount;
            viewPage.lastItemsCount = i + 1;
            return i;
        }

        static /* synthetic */ int access$6410(ViewPage viewPage) {
            int i = viewPage.lastItemsCount;
            viewPage.lastItemsCount = i - 1;
            return i;
        }

        public ViewPage(DialogsActivity dialogsActivity, Context context) {
            super(context);
        }

        public boolean isDefaultDialogType() {
            int i = this.dialogsType;
            return i == 0 || i == 7 || i == 8;
        }
    }

    private class ContentView extends SizeNotifierFrameLayout {
        private int inputFieldHeight;
        private int[] pos = new int[2];
        private int startedTrackingPointerId;
        private int startedTrackingX;
        private int startedTrackingY;
        private VelocityTracker velocityTracker;

        public boolean hasOverlappingRendering() {
            return false;
        }

        public ContentView(Context context) {
            super(context, SharedConfig.smoothKeyboard);
        }

        private boolean prepareForMoving(MotionEvent motionEvent, boolean z) {
            int nextPageId = DialogsActivity.this.filterTabsView.getNextPageId(z);
            if (nextPageId < 0) {
                return false;
            }
            getParent().requestDisallowInterceptTouchEvent(true);
            boolean unused = DialogsActivity.this.maybeStartTracking = false;
            boolean unused2 = DialogsActivity.this.startedTracking = true;
            this.startedTrackingX = (int) (motionEvent.getX() + DialogsActivity.this.additionalOffset);
            DialogsActivity.this.actionBar.setEnabled(false);
            DialogsActivity.this.filterTabsView.setEnabled(false);
            int unused3 = DialogsActivity.this.viewPages[1].selectedType = nextPageId;
            DialogsActivity.this.viewPages[1].setVisibility(0);
            boolean unused4 = DialogsActivity.this.animatingForward = z;
            DialogsActivity.this.showScrollbars(false);
            DialogsActivity.this.switchToCurrentSelectedMode(true);
            if (z) {
                DialogsActivity.this.viewPages[1].setTranslationX((float) DialogsActivity.this.viewPages[0].getMeasuredWidth());
            } else {
                DialogsActivity.this.viewPages[1].setTranslationX((float) (-DialogsActivity.this.viewPages[0].getMeasuredWidth()));
            }
            return true;
        }

        public void setPadding(int i, int i2, int i3, int i4) {
            int unused = DialogsActivity.this.topPadding = i2;
            float f = 0.0f;
            if (DialogsActivity.this.fragmentContextView != null) {
                DialogsActivity.this.fragmentContextView.setTranslationY(((float) i2) + (DialogsActivity.this.filterTabsView != null ? DialogsActivity.this.filterTabsView.getTranslationY() : 0.0f));
            }
            if (DialogsActivity.this.fragmentLocationContextView != null) {
                FragmentContextView access$1400 = DialogsActivity.this.fragmentLocationContextView;
                float f2 = (float) i2;
                if (DialogsActivity.this.filterTabsView != null) {
                    f = DialogsActivity.this.filterTabsView.getTranslationY();
                }
                access$1400.setTranslationY(f2 + f);
            }
            requestLayout();
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x00a1  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean checkTabsAnimationInProgress() {
            /*
                r7 = this;
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                boolean r0 = r0.tabsAnimationInProgress
                r1 = 0
                if (r0 == 0) goto L_0x00c9
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                boolean r0 = r0.backAnimation
                r2 = -1
                r3 = 0
                r4 = 1065353216(0x3var_, float:1.0)
                r5 = 1
                if (r0 == 0) goto L_0x0059
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r1]
                float r0 = r0.getTranslationX()
                float r0 = java.lang.Math.abs(r0)
                int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
                if (r0 >= 0) goto L_0x009e
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r1]
                r0.setTranslationX(r3)
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r5]
                org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r3 = r3.viewPages
                r3 = r3[r1]
                int r3 = r3.getMeasuredWidth()
                org.telegram.ui.DialogsActivity r4 = org.telegram.ui.DialogsActivity.this
                boolean r4 = r4.animatingForward
                if (r4 == 0) goto L_0x0052
                r2 = 1
            L_0x0052:
                int r3 = r3 * r2
                float r2 = (float) r3
                r0.setTranslationX(r2)
                goto L_0x009c
            L_0x0059:
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r5]
                float r0 = r0.getTranslationX()
                float r0 = java.lang.Math.abs(r0)
                int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
                if (r0 >= 0) goto L_0x009e
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r1]
                org.telegram.ui.DialogsActivity r4 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r4 = r4.viewPages
                r4 = r4[r1]
                int r4 = r4.getMeasuredWidth()
                org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                boolean r6 = r6.animatingForward
                if (r6 == 0) goto L_0x008a
                goto L_0x008b
            L_0x008a:
                r2 = 1
            L_0x008b:
                int r4 = r4 * r2
                float r2 = (float) r4
                r0.setTranslationX(r2)
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r0 = r0.viewPages
                r0 = r0[r5]
                r0.setTranslationX(r3)
            L_0x009c:
                r0 = 1
                goto L_0x009f
            L_0x009e:
                r0 = 0
            L_0x009f:
                if (r0 == 0) goto L_0x00c2
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                r0.showScrollbars(r5)
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                android.animation.AnimatorSet r0 = r0.tabsAnimation
                if (r0 == 0) goto L_0x00bd
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                android.animation.AnimatorSet r0 = r0.tabsAnimation
                r0.cancel()
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                r2 = 0
                android.animation.AnimatorSet unused = r0.tabsAnimation = r2
            L_0x00bd:
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                boolean unused = r0.tabsAnimationInProgress = r1
            L_0x00c2:
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                boolean r0 = r0.tabsAnimationInProgress
                return r0
            L_0x00c9:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.ContentView.checkTabsAnimationInProgress():boolean");
        }

        /* access modifiers changed from: protected */
        public boolean drawChild(Canvas canvas, View view, long j) {
            boolean drawChild = super.drawChild(canvas, view, j);
            if (view == DialogsActivity.this.actionBar && DialogsActivity.this.parentLayout != null) {
                int measuredHeight = DialogsActivity.this.actionBar.getMeasuredHeight();
                if (!(DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() == 8)) {
                    measuredHeight = (int) (((float) measuredHeight) + ((float) DialogsActivity.this.filterTabsView.getMeasuredHeight()) + DialogsActivity.this.filterTabsView.getTranslationY());
                }
                DialogsActivity.this.parentLayout.drawHeaderShadow(canvas, measuredHeight);
            }
            return drawChild;
        }

        /* access modifiers changed from: protected */
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (DialogsActivity.this.scrimView != null) {
                canvas.drawRect(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight(), DialogsActivity.this.scrimPaint);
                canvas.save();
                getLocationInWindow(this.pos);
                canvas.translate((float) (DialogsActivity.this.scrimViewLocation[0] - this.pos[0]), (float) (DialogsActivity.this.scrimViewLocation[1] - (Build.VERSION.SDK_INT < 21 ? AndroidUtilities.statusBarHeight : 0)));
                DialogsActivity.this.scrimView.draw(canvas);
                if (DialogsActivity.this.scrimViewSelected) {
                    Drawable selectorDrawable = DialogsActivity.this.filterTabsView.getSelectorDrawable();
                    canvas.translate((float) (-DialogsActivity.this.scrimViewLocation[0]), (float) ((-selectorDrawable.getIntrinsicHeight()) - 1));
                    selectorDrawable.draw(canvas);
                }
                canvas.restore();
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            int dp;
            int access$1200;
            int size = View.MeasureSpec.getSize(i);
            int size2 = View.MeasureSpec.getSize(i2);
            setMeasuredDimension(size, size2);
            int paddingTop = size2 - getPaddingTop();
            if (DialogsActivity.this.doneItem != null) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) DialogsActivity.this.doneItem.getLayoutParams();
                layoutParams.topMargin = DialogsActivity.this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
                layoutParams.height = ActionBar.getCurrentActionBarHeight();
            }
            measureChildWithMargins(DialogsActivity.this.actionBar, i, 0, i2, 0);
            int measureKeyboardHeight = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
            int childCount = getChildCount();
            if (DialogsActivity.this.commentView != null) {
                measureChildWithMargins(DialogsActivity.this.commentView, i, 0, i2, 0);
                Object tag = DialogsActivity.this.commentView.getTag();
                if (tag == null || !tag.equals(2)) {
                    this.inputFieldHeight = 0;
                } else {
                    if (measureKeyboardHeight <= AndroidUtilities.dp(20.0f) && !AndroidUtilities.isInMultiwindow) {
                        paddingTop -= DialogsActivity.this.commentView.getEmojiPadding();
                    }
                    this.inputFieldHeight = DialogsActivity.this.commentView.getMeasuredHeight();
                }
                if (SharedConfig.smoothKeyboard && DialogsActivity.this.commentView.isPopupShowing()) {
                    DialogsActivity.this.fragmentView.setTranslationY((float) DialogsActivity.this.getCurrentPanTranslationY());
                    for (int i3 = 0; i3 < DialogsActivity.this.viewPages.length; i3++) {
                        if (DialogsActivity.this.viewPages[i3] != null) {
                            DialogsActivity.this.viewPages[i3].setTranslationY(0.0f);
                        }
                    }
                    if (!DialogsActivity.this.onlySelect) {
                        DialogsActivity.this.actionBar.setTranslationY(0.0f);
                    }
                    DialogsActivity.this.searchListView.setTranslationY(0.0f);
                }
            }
            for (int i4 = 0; i4 < childCount; i4++) {
                View childAt = getChildAt(i4);
                if (!(childAt == null || childAt.getVisibility() == 8 || childAt == DialogsActivity.this.commentView || childAt == DialogsActivity.this.actionBar)) {
                    if (childAt instanceof ViewPage) {
                        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, NUM);
                        if (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) {
                            dp = ((paddingTop - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - (DialogsActivity.this.onlySelect ? 0 : DialogsActivity.this.actionBar.getMeasuredHeight());
                            access$1200 = DialogsActivity.this.topPadding;
                        } else {
                            dp = ((paddingTop - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - AndroidUtilities.dp(44.0f);
                            access$1200 = DialogsActivity.this.topPadding;
                        }
                        childAt.measure(makeMeasureSpec, View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), dp - access$1200), NUM));
                        childAt.setPivotX((float) (childAt.getMeasuredWidth() / 2));
                    } else if (childAt == DialogsActivity.this.searchListView || childAt == DialogsActivity.this.searchEmptyView) {
                        childAt.measure(View.MeasureSpec.makeMeasureSpec(size, NUM), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0f), (((paddingTop - this.inputFieldHeight) + AndroidUtilities.dp(2.0f)) - (DialogsActivity.this.onlySelect ? 0 : DialogsActivity.this.actionBar.getMeasuredHeight())) - DialogsActivity.this.topPadding), NUM));
                        childAt.setPivotX((float) (childAt.getMeasuredWidth() / 2));
                    } else if (DialogsActivity.this.commentView == null || !DialogsActivity.this.commentView.isPopupView(childAt)) {
                        measureChildWithMargins(childAt, i, 0, i2, 0);
                    } else if (!AndroidUtilities.isInMultiwindow) {
                        childAt.measure(View.MeasureSpec.makeMeasureSpec(size, NUM), View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, NUM));
                    } else if (AndroidUtilities.isTablet()) {
                        childAt.measure(View.MeasureSpec.makeMeasureSpec(size, NUM), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0f), ((paddingTop - this.inputFieldHeight) - AndroidUtilities.statusBarHeight) + getPaddingTop()), NUM));
                    } else {
                        childAt.measure(View.MeasureSpec.makeMeasureSpec(size, NUM), View.MeasureSpec.makeMeasureSpec(((paddingTop - this.inputFieldHeight) - AndroidUtilities.statusBarHeight) + getPaddingTop(), NUM));
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0094  */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x00af  */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x00d0  */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x00f8  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onLayout(boolean r14, int r15, int r16, int r17, int r18) {
            /*
                r13 = this;
                r0 = r13
                int r1 = r13.getChildCount()
                org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r2 = r2.commentView
                if (r2 == 0) goto L_0x0018
                org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r2 = r2.commentView
                java.lang.Object r2 = r2.getTag()
                goto L_0x0019
            L_0x0018:
                r2 = 0
            L_0x0019:
                boolean r3 = org.telegram.messenger.SharedConfig.smoothKeyboard
                r4 = 0
                if (r3 == 0) goto L_0x0020
                r3 = 0
                goto L_0x0024
            L_0x0020:
                int r3 = r13.measureKeyboardHeight()
            L_0x0024:
                r5 = 2
                if (r2 == 0) goto L_0x0048
                java.lang.Integer r6 = java.lang.Integer.valueOf(r5)
                boolean r2 = r2.equals(r6)
                if (r2 == 0) goto L_0x0048
                r2 = 1101004800(0x41a00000, float:20.0)
                int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
                if (r3 > r2) goto L_0x0048
                boolean r2 = org.telegram.messenger.AndroidUtilities.isInMultiwindow
                if (r2 != 0) goto L_0x0048
                org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r2 = r2.commentView
                int r2 = r2.getEmojiPadding()
                goto L_0x0049
            L_0x0048:
                r2 = 0
            L_0x0049:
                r13.setBottomClip(r2)
                r3 = 0
            L_0x004d:
                if (r3 >= r1) goto L_0x01a9
                android.view.View r6 = r13.getChildAt(r3)
                int r7 = r6.getVisibility()
                r8 = 8
                if (r7 != r8) goto L_0x005d
                goto L_0x01a5
            L_0x005d:
                android.view.ViewGroup$LayoutParams r7 = r6.getLayoutParams()
                android.widget.FrameLayout$LayoutParams r7 = (android.widget.FrameLayout.LayoutParams) r7
                int r8 = r6.getMeasuredWidth()
                int r9 = r6.getMeasuredHeight()
                int r10 = r7.gravity
                r11 = -1
                if (r10 != r11) goto L_0x0072
                r10 = 51
            L_0x0072:
                r11 = r10 & 7
                r10 = r10 & 112(0x70, float:1.57E-43)
                r11 = r11 & 7
                r12 = 1
                if (r11 == r12) goto L_0x0086
                r12 = 5
                if (r11 == r12) goto L_0x0081
                int r11 = r7.leftMargin
                goto L_0x0090
            L_0x0081:
                int r11 = r17 - r8
                int r12 = r7.rightMargin
                goto L_0x008f
            L_0x0086:
                int r11 = r17 - r15
                int r11 = r11 - r8
                int r11 = r11 / r5
                int r12 = r7.leftMargin
                int r11 = r11 + r12
                int r12 = r7.rightMargin
            L_0x008f:
                int r11 = r11 - r12
            L_0x0090:
                r12 = 16
                if (r10 == r12) goto L_0x00af
                r12 = 48
                if (r10 == r12) goto L_0x00a7
                r12 = 80
                if (r10 == r12) goto L_0x009f
                int r7 = r7.topMargin
                goto L_0x00bc
            L_0x009f:
                int r10 = r18 - r2
                int r10 = r10 - r16
                int r10 = r10 - r9
                int r7 = r7.bottomMargin
                goto L_0x00ba
            L_0x00a7:
                int r7 = r7.topMargin
                int r10 = r13.getPaddingTop()
                int r7 = r7 + r10
                goto L_0x00bc
            L_0x00af:
                int r10 = r18 - r2
                int r10 = r10 - r16
                int r10 = r10 - r9
                int r10 = r10 / r5
                int r12 = r7.topMargin
                int r10 = r10 + r12
                int r7 = r7.bottomMargin
            L_0x00ba:
                int r7 = r10 - r7
            L_0x00bc:
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r10 = r10.commentView
                if (r10 == 0) goto L_0x00f8
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r10 = r10.commentView
                boolean r10 = r10.isPopupView(r6)
                if (r10 == 0) goto L_0x00f8
                boolean r7 = org.telegram.messenger.AndroidUtilities.isInMultiwindow
                if (r7 == 0) goto L_0x00ec
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r7 = r7.commentView
                int r7 = r7.getTop()
                int r10 = r6.getMeasuredHeight()
                int r7 = r7 - r10
                r10 = 1065353216(0x3var_, float:1.0)
                int r10 = org.telegram.messenger.AndroidUtilities.dp(r10)
            L_0x00e9:
                int r7 = r7 + r10
                goto L_0x01a0
            L_0x00ec:
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.ChatActivityEnterView r7 = r7.commentView
                int r7 = r7.getBottom()
                goto L_0x01a0
            L_0x00f8:
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r10 = r10.filterTabsView
                if (r6 != r10) goto L_0x010c
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.ActionBar.ActionBar r7 = r7.actionBar
                int r7 = r7.getMeasuredHeight()
                goto L_0x01a0
            L_0x010c:
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.RecyclerListView r10 = r10.searchListView
                if (r6 == r10) goto L_0x0184
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.EmptyTextProgressView r10 = r10.searchEmptyView
                if (r6 != r10) goto L_0x011d
                goto L_0x0184
            L_0x011d:
                boolean r10 = r6 instanceof org.telegram.ui.DialogsActivity.ViewPage
                if (r10 == 0) goto L_0x0155
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                boolean r10 = r10.onlySelect
                if (r10 != 0) goto L_0x014e
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r7 = r7.filterTabsView
                if (r7 == 0) goto L_0x0144
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r7 = r7.filterTabsView
                int r7 = r7.getVisibility()
                if (r7 != 0) goto L_0x0144
                r7 = 1110441984(0x42300000, float:44.0)
                int r7 = org.telegram.messenger.AndroidUtilities.dp(r7)
                goto L_0x014e
            L_0x0144:
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.ActionBar.ActionBar r7 = r7.actionBar
                int r7 = r7.getMeasuredHeight()
            L_0x014e:
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                int r10 = r10.topPadding
                goto L_0x00e9
            L_0x0155:
                boolean r10 = r6 instanceof org.telegram.ui.Components.FragmentContextView
                if (r10 == 0) goto L_0x01a0
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.ActionBar.ActionBar r10 = r10.actionBar
                int r10 = r10.getMeasuredHeight()
                int r7 = r7 + r10
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r10 = r10.filterTabsView
                if (r10 == 0) goto L_0x01a0
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r10 = r10.filterTabsView
                int r10 = r10.getVisibility()
                if (r10 != 0) goto L_0x01a0
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.Components.FilterTabsView r10 = r10.filterTabsView
                int r10 = r10.getMeasuredHeight()
                goto L_0x00e9
            L_0x0184:
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                boolean r7 = r7.onlySelect
                if (r7 == 0) goto L_0x018e
                r7 = 0
                goto L_0x0198
            L_0x018e:
                org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.ActionBar.ActionBar r7 = r7.actionBar
                int r7 = r7.getMeasuredHeight()
            L_0x0198:
                org.telegram.ui.DialogsActivity r10 = org.telegram.ui.DialogsActivity.this
                int r10 = r10.topPadding
                goto L_0x00e9
            L_0x01a0:
                int r8 = r8 + r11
                int r9 = r9 + r7
                r6.layout(r11, r7, r8, r9)
            L_0x01a5:
                int r3 = r3 + 1
                goto L_0x004d
            L_0x01a9:
                r13.notifyHeightChanged()
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.ContentView.onLayout(boolean, int, int, int, int):void");
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if ((actionMasked == 1 || actionMasked == 3) && DialogsActivity.this.actionBar.isActionModeShowed()) {
                boolean unused = DialogsActivity.this.allowMoving = true;
            }
            if (checkTabsAnimationInProgress()) {
                return true;
            }
            if ((DialogsActivity.this.filterTabsView == null || !DialogsActivity.this.filterTabsView.isAnimatingIndicator()) && !onTouchEvent(motionEvent)) {
                return false;
            }
            return true;
        }

        public void requestDisallowInterceptTouchEvent(boolean z) {
            if (DialogsActivity.this.maybeStartTracking && !DialogsActivity.this.startedTracking) {
                onTouchEvent((MotionEvent) null);
            }
            super.requestDisallowInterceptTouchEvent(z);
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            float f;
            float f2;
            float f3;
            int i;
            boolean z = false;
            if (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.isEditing() || DialogsActivity.this.searching || DialogsActivity.this.parentLayout.checkTransitionAnimation() || DialogsActivity.this.parentLayout.isInPreviewMode() || DialogsActivity.this.parentLayout.isPreviewOpenAnimationInProgress() || DialogsActivity.this.parentLayout.getDrawerLayoutContainer().isDrawerOpened() || (motionEvent != null && !DialogsActivity.this.startedTracking && motionEvent.getY() <= ((float) DialogsActivity.this.actionBar.getMeasuredHeight()) + DialogsActivity.this.actionBar.getTranslationY())) {
                return false;
            }
            if (motionEvent != null) {
                if (this.velocityTracker == null) {
                    this.velocityTracker = VelocityTracker.obtain();
                }
                this.velocityTracker.addMovement(motionEvent);
            }
            if (motionEvent != null && motionEvent.getAction() == 0 && checkTabsAnimationInProgress()) {
                boolean unused = DialogsActivity.this.startedTracking = true;
                this.startedTrackingPointerId = motionEvent.getPointerId(0);
                this.startedTrackingX = (int) motionEvent.getX();
                DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(false);
                if (DialogsActivity.this.animatingForward) {
                    if (((float) this.startedTrackingX) < ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) + DialogsActivity.this.viewPages[0].getTranslationX()) {
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        float unused2 = dialogsActivity.additionalOffset = dialogsActivity.viewPages[0].getTranslationX();
                    } else {
                        ViewPage viewPage = DialogsActivity.this.viewPages[0];
                        DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                        DialogsActivity.this.viewPages[1] = viewPage;
                        boolean unused3 = DialogsActivity.this.animatingForward = false;
                        DialogsActivity dialogsActivity2 = DialogsActivity.this;
                        float unused4 = dialogsActivity2.additionalOffset = dialogsActivity2.viewPages[0].getTranslationX();
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, DialogsActivity.this.additionalOffset / ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                        DialogsActivity.this.switchToCurrentSelectedMode(true);
                    }
                } else if (((float) this.startedTrackingX) < ((float) DialogsActivity.this.viewPages[1].getMeasuredWidth()) + DialogsActivity.this.viewPages[1].getTranslationX()) {
                    ViewPage viewPage2 = DialogsActivity.this.viewPages[0];
                    DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                    DialogsActivity.this.viewPages[1] = viewPage2;
                    boolean unused5 = DialogsActivity.this.animatingForward = true;
                    DialogsActivity dialogsActivity3 = DialogsActivity.this;
                    float unused6 = dialogsActivity3.additionalOffset = dialogsActivity3.viewPages[0].getTranslationX();
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, (-DialogsActivity.this.additionalOffset) / ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                    DialogsActivity.this.switchToCurrentSelectedMode(true);
                } else {
                    DialogsActivity dialogsActivity4 = DialogsActivity.this;
                    float unused7 = dialogsActivity4.additionalOffset = dialogsActivity4.viewPages[0].getTranslationX();
                }
                DialogsActivity.this.tabsAnimation.removeAllListeners();
                DialogsActivity.this.tabsAnimation.cancel();
                boolean unused8 = DialogsActivity.this.tabsAnimationInProgress = false;
            } else if (motionEvent != null && motionEvent.getAction() == 0) {
                float unused9 = DialogsActivity.this.additionalOffset = 0.0f;
            }
            if (motionEvent != null && motionEvent.getAction() == 0 && !DialogsActivity.this.startedTracking && !DialogsActivity.this.maybeStartTracking && DialogsActivity.this.filterTabsView.getVisibility() == 0) {
                this.startedTrackingPointerId = motionEvent.getPointerId(0);
                boolean unused10 = DialogsActivity.this.maybeStartTracking = true;
                this.startedTrackingX = (int) motionEvent.getX();
                this.startedTrackingY = (int) motionEvent.getY();
                this.velocityTracker.clear();
            } else if (motionEvent != null && motionEvent.getAction() == 2 && motionEvent.getPointerId(0) == this.startedTrackingPointerId) {
                int x = (int) ((motionEvent.getX() - ((float) this.startedTrackingX)) + DialogsActivity.this.additionalOffset);
                int abs = Math.abs(((int) motionEvent.getY()) - this.startedTrackingY);
                if (DialogsActivity.this.startedTracking && ((DialogsActivity.this.animatingForward && x > 0) || (!DialogsActivity.this.animatingForward && x < 0))) {
                    if (!prepareForMoving(motionEvent, x < 0)) {
                        boolean unused11 = DialogsActivity.this.maybeStartTracking = true;
                        boolean unused12 = DialogsActivity.this.startedTracking = false;
                        DialogsActivity.this.viewPages[0].setTranslationX(0.0f);
                        DialogsActivity.this.viewPages[1].setTranslationX((float) (DialogsActivity.this.animatingForward ? DialogsActivity.this.viewPages[0].getMeasuredWidth() : -DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, 0.0f);
                    }
                }
                if (DialogsActivity.this.maybeStartTracking && !DialogsActivity.this.startedTracking) {
                    float pixelsInCM = AndroidUtilities.getPixelsInCM(0.3f, true);
                    int x2 = (int) (motionEvent.getX() - ((float) this.startedTrackingX));
                    if (((float) Math.abs(x2)) >= pixelsInCM && Math.abs(x2) > abs) {
                        if (x < 0) {
                            z = true;
                        }
                        prepareForMoving(motionEvent, z);
                    }
                } else if (DialogsActivity.this.startedTracking) {
                    DialogsActivity.this.viewPages[0].setTranslationX((float) x);
                    if (DialogsActivity.this.animatingForward) {
                        DialogsActivity.this.viewPages[1].setTranslationX((float) (DialogsActivity.this.viewPages[0].getMeasuredWidth() + x));
                    } else {
                        DialogsActivity.this.viewPages[1].setTranslationX((float) (x - DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                    }
                    DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, ((float) Math.abs(x)) / ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                }
            } else if (motionEvent == null || (motionEvent.getPointerId(0) == this.startedTrackingPointerId && (motionEvent.getAction() == 3 || motionEvent.getAction() == 1 || motionEvent.getAction() == 6))) {
                this.velocityTracker.computeCurrentVelocity(1000, (float) DialogsActivity.this.maximumVelocity);
                if (motionEvent == null || motionEvent.getAction() == 3) {
                    f2 = 0.0f;
                    f = 0.0f;
                } else {
                    f2 = this.velocityTracker.getXVelocity();
                    f = this.velocityTracker.getYVelocity();
                    if (!DialogsActivity.this.startedTracking && Math.abs(f2) >= 3000.0f && Math.abs(f2) > Math.abs(f)) {
                        prepareForMoving(motionEvent, f2 < 0.0f);
                    }
                }
                if (DialogsActivity.this.startedTracking) {
                    float x3 = DialogsActivity.this.viewPages[0].getX();
                    AnimatorSet unused13 = DialogsActivity.this.tabsAnimation = new AnimatorSet();
                    if (DialogsActivity.this.additionalOffset == 0.0f) {
                        boolean unused14 = DialogsActivity.this.backAnimation = Math.abs(x3) < ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) / 3.0f && (Math.abs(f2) < 3500.0f || Math.abs(f2) < Math.abs(f));
                    } else if (Math.abs(f2) > 1500.0f) {
                        DialogsActivity dialogsActivity5 = DialogsActivity.this;
                        boolean unused15 = dialogsActivity5.backAnimation = !dialogsActivity5.animatingForward ? f2 < 0.0f : f2 > 0.0f;
                    } else if (DialogsActivity.this.animatingForward) {
                        DialogsActivity dialogsActivity6 = DialogsActivity.this;
                        boolean unused16 = dialogsActivity6.backAnimation = dialogsActivity6.viewPages[1].getX() > ((float) (DialogsActivity.this.viewPages[0].getMeasuredWidth() >> 1));
                    } else {
                        DialogsActivity dialogsActivity7 = DialogsActivity.this;
                        boolean unused17 = dialogsActivity7.backAnimation = dialogsActivity7.viewPages[0].getX() < ((float) (DialogsActivity.this.viewPages[0].getMeasuredWidth() >> 1));
                    }
                    if (DialogsActivity.this.backAnimation) {
                        f3 = Math.abs(x3);
                        if (DialogsActivity.this.animatingForward) {
                            DialogsActivity.this.tabsAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, new float[]{0.0f}), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, new float[]{(float) DialogsActivity.this.viewPages[1].getMeasuredWidth()})});
                        } else {
                            DialogsActivity.this.tabsAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, new float[]{0.0f}), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, new float[]{(float) (-DialogsActivity.this.viewPages[1].getMeasuredWidth())})});
                        }
                    } else {
                        f3 = ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) - Math.abs(x3);
                        if (DialogsActivity.this.animatingForward) {
                            DialogsActivity.this.tabsAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, new float[]{(float) (-DialogsActivity.this.viewPages[0].getMeasuredWidth())}), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, new float[]{0.0f})});
                        } else {
                            DialogsActivity.this.tabsAnimation.playTogether(new Animator[]{ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[0], View.TRANSLATION_X, new float[]{(float) DialogsActivity.this.viewPages[0].getMeasuredWidth()}), ObjectAnimator.ofFloat(DialogsActivity.this.viewPages[1], View.TRANSLATION_X, new float[]{0.0f})});
                        }
                    }
                    DialogsActivity.this.tabsAnimation.setInterpolator(DialogsActivity.interpolator);
                    int measuredWidth = getMeasuredWidth();
                    float f4 = (float) (measuredWidth / 2);
                    float distanceInfluenceForSnapDuration = f4 + (AndroidUtilities.distanceInfluenceForSnapDuration(Math.min(1.0f, (f3 * 1.0f) / ((float) measuredWidth))) * f4);
                    float abs2 = Math.abs(f2);
                    if (abs2 > 0.0f) {
                        i = Math.round(Math.abs(distanceInfluenceForSnapDuration / abs2) * 1000.0f) * 4;
                    } else {
                        i = (int) (((f3 / ((float) getMeasuredWidth())) + 1.0f) * 100.0f);
                    }
                    DialogsActivity.this.tabsAnimation.setDuration((long) Math.max(150, Math.min(i, 600)));
                    DialogsActivity.this.tabsAnimation.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            AnimatorSet unused = DialogsActivity.this.tabsAnimation = null;
                            if (!DialogsActivity.this.backAnimation) {
                                ViewPage viewPage = DialogsActivity.this.viewPages[0];
                                DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                                DialogsActivity.this.viewPages[1] = viewPage;
                                DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[0].selectedType, 1.0f);
                                DialogsActivity.this.updateCounters(false);
                            }
                            if (DialogsActivity.this.parentLayout != null) {
                                DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(DialogsActivity.this.viewPages[0].selectedType == DialogsActivity.this.filterTabsView.getFirstTabId());
                            }
                            DialogsActivity.this.viewPages[1].setVisibility(8);
                            DialogsActivity.this.showScrollbars(true);
                            boolean unused2 = DialogsActivity.this.tabsAnimationInProgress = false;
                            boolean unused3 = DialogsActivity.this.maybeStartTracking = false;
                            DialogsActivity.this.actionBar.setEnabled(true);
                            DialogsActivity.this.filterTabsView.setEnabled(true);
                            DialogsActivity dialogsActivity = DialogsActivity.this;
                            dialogsActivity.checkListLoad(dialogsActivity.viewPages[0]);
                        }
                    });
                    DialogsActivity.this.tabsAnimation.start();
                    boolean unused18 = DialogsActivity.this.tabsAnimationInProgress = true;
                    boolean unused19 = DialogsActivity.this.startedTracking = false;
                } else {
                    DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(DialogsActivity.this.viewPages[0].selectedType == DialogsActivity.this.filterTabsView.getFirstTabId());
                    boolean unused20 = DialogsActivity.this.maybeStartTracking = false;
                    DialogsActivity.this.actionBar.setEnabled(true);
                    DialogsActivity.this.filterTabsView.setEnabled(true);
                }
                VelocityTracker velocityTracker2 = this.velocityTracker;
                if (velocityTracker2 != null) {
                    velocityTracker2.recycle();
                    this.velocityTracker = null;
                }
            }
            return DialogsActivity.this.startedTracking;
        }
    }

    public class DialogsRecyclerView extends RecyclerListView {
        private int appliedPaddingTop;
        private boolean firstLayout = true;
        private boolean ignoreLayout;
        private ViewPage parentPage;

        public DialogsRecyclerView(Context context, ViewPage viewPage) {
            super(context);
            this.parentPage = viewPage;
        }

        public void setViewsOffset(float f) {
            View findViewByPosition;
            DialogsActivity.viewOffset = f;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).setTranslationY(f);
            }
            if (!(this.selectorPosition == -1 || (findViewByPosition = getLayoutManager().findViewByPosition(this.selectorPosition)) == null)) {
                this.selectorRect.set(findViewByPosition.getLeft(), (int) (((float) findViewByPosition.getTop()) + f), findViewByPosition.getRight(), (int) (((float) findViewByPosition.getBottom()) + f));
                this.selectorDrawable.setBounds(this.selectorRect);
            }
            invalidate();
        }

        public float getViewOffset() {
            return DialogsActivity.viewOffset;
        }

        public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
            super.addView(view, i, layoutParams);
            view.setTranslationY(DialogsActivity.viewOffset);
        }

        public void removeView(View view) {
            super.removeView(view);
            view.setTranslationY(0.0f);
        }

        public void onDraw(Canvas canvas) {
            if (!(this.parentPage.pullForegroundDrawable == null || DialogsActivity.viewOffset == 0.0f)) {
                int paddingTop = getPaddingTop();
                if (paddingTop != 0) {
                    canvas.save();
                    canvas.translate(0.0f, (float) paddingTop);
                }
                this.parentPage.pullForegroundDrawable.drawOverScroll(canvas);
                if (paddingTop != 0) {
                    canvas.restore();
                }
            }
            super.onDraw(canvas);
        }

        /* access modifiers changed from: protected */
        public void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (DialogsActivity.this.slidingView != null && DialogsActivity.this.pacmanAnimation != null) {
                DialogsActivity.this.pacmanAnimation.draw(canvas, DialogsActivity.this.slidingView.getTop() + (DialogsActivity.this.slidingView.getMeasuredHeight() / 2));
            }
        }

        public void setAdapter(RecyclerView.Adapter adapter) {
            super.setAdapter(adapter);
            this.firstLayout = true;
        }

        private void checkIfAdapterValid() {
            RecyclerView.Adapter adapter = getAdapter();
            if (this.parentPage.lastItemsCount != adapter.getItemCount()) {
                this.ignoreLayout = true;
                adapter.notifyDataSetChanged();
                this.ignoreLayout = false;
            }
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            int i3;
            int findFirstVisibleItemPosition;
            RecyclerView.ViewHolder findViewHolderForAdapterPosition;
            if (!DialogsActivity.this.onlySelect) {
                this.ignoreLayout = true;
                if (DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) {
                    i3 = 0;
                } else {
                    i3 = ActionBar.getCurrentActionBarHeight() + (DialogsActivity.this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                }
                setTopGlowOffset(i3);
                setPadding(0, i3, 0, 0);
                if (!(this.appliedPaddingTop == i3 || (findFirstVisibleItemPosition = this.parentPage.layoutManager.findFirstVisibleItemPosition()) == -1 || (findViewHolderForAdapterPosition = this.parentPage.listView.findViewHolderForAdapterPosition(findFirstVisibleItemPosition)) == null)) {
                    this.parentPage.layoutManager.scrollToPositionWithOffset(findFirstVisibleItemPosition, findViewHolderForAdapterPosition.itemView.getTop() - this.appliedPaddingTop);
                }
                this.ignoreLayout = false;
            } else {
                i3 = 0;
            }
            if (this.firstLayout && DialogsActivity.this.getMessagesController().dialogsLoaded) {
                if (this.parentPage.dialogsType == 0 && DialogsActivity.this.hasHiddenArchive()) {
                    this.ignoreLayout = true;
                    ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(1, (int) DialogsActivity.this.actionBar.getTranslationY());
                    this.ignoreLayout = false;
                }
                this.firstLayout = false;
            }
            checkIfAdapterValid();
            super.onMeasure(i, i2);
            if (!DialogsActivity.this.onlySelect && this.appliedPaddingTop != i3 && DialogsActivity.this.viewPages != null && DialogsActivity.this.viewPages.length > 1) {
                DialogsActivity.this.viewPages[1].setTranslationX((float) DialogsActivity.this.viewPages[0].getMeasuredWidth());
            }
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            this.appliedPaddingTop = getPaddingTop();
            if (!(DialogsActivity.this.dialogRemoveFinished == 0 && DialogsActivity.this.dialogInsertFinished == 0 && DialogsActivity.this.dialogChangeFinished == 0) && !this.parentPage.dialogsItemAnimator.isRunning()) {
                DialogsActivity.this.onDialogAnimationFinished();
            }
        }

        public void requestLayout() {
            if (!this.ignoreLayout) {
                super.requestLayout();
            }
        }

        /* access modifiers changed from: private */
        public void toggleArchiveHidden(boolean z, DialogCell dialogCell) {
            SharedConfig.toggleArchiveHidden();
            if (SharedConfig.archiveHidden) {
                boolean unused = DialogsActivity.this.waitingForScrollFinished = true;
                if (dialogCell != null) {
                    boolean unused2 = DialogsActivity.this.disableActionBarScrolling = true;
                    smoothScrollBy(0, dialogCell.getMeasuredHeight() + (dialogCell.getTop() - getPaddingTop()), CubicBezierInterpolator.EASE_OUT);
                    if (z) {
                        boolean unused3 = DialogsActivity.this.updatePullAfterScroll = true;
                    } else {
                        updatePullState();
                    }
                }
                DialogsActivity.this.getUndoView().showWithAction(0, 6, (Runnable) null, (Runnable) null);
                return;
            }
            DialogsActivity.this.getUndoView().showWithAction(0, 7, (Runnable) null, (Runnable) null);
            updatePullState();
            if (z && dialogCell != null) {
                dialogCell.resetPinnedArchiveState();
                dialogCell.invalidate();
            }
        }

        /* access modifiers changed from: private */
        public void updatePullState() {
            boolean z = false;
            int unused = this.parentPage.archivePullViewState = SharedConfig.archiveHidden ? 2 : 0;
            if (this.parentPage.pullForegroundDrawable != null) {
                PullForegroundDrawable access$6100 = this.parentPage.pullForegroundDrawable;
                if (this.parentPage.archivePullViewState != 0) {
                    z = true;
                }
                access$6100.setWillDraw(z);
            }
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            LinearLayoutManager linearLayoutManager;
            int findFirstVisibleItemPosition;
            if (this.fastScrollAnimationRunning || DialogsActivity.this.waitingForScrollFinished || DialogsActivity.this.dialogRemoveFinished != 0 || DialogsActivity.this.dialogInsertFinished != 0 || DialogsActivity.this.dialogChangeFinished != 0) {
                return false;
            }
            int action = motionEvent.getAction();
            if (action == 0) {
                setOverScrollMode(0);
            }
            if ((action == 1 || action == 3) && !this.parentPage.itemTouchhelper.isIdle() && this.parentPage.swipeController.swipingFolder) {
                boolean unused = this.parentPage.swipeController.swipeFolderBack = true;
                if (this.parentPage.itemTouchhelper.checkHorizontalSwipe((RecyclerView.ViewHolder) null, 4) != 0) {
                    toggleArchiveHidden(false, (DialogCell) null);
                }
            }
            boolean onTouchEvent = super.onTouchEvent(motionEvent);
            if (this.parentPage.dialogsType == 0 && ((action == 1 || action == 3) && this.parentPage.archivePullViewState == 2 && DialogsActivity.this.hasHiddenArchive() && (findFirstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()) == 0)) {
                int paddingTop = getPaddingTop();
                View findViewByPosition = (linearLayoutManager = (LinearLayoutManager) getLayoutManager()).findViewByPosition(findFirstVisibleItemPosition);
                int dp = (int) (((float) AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f)) * 0.85f);
                int top = (findViewByPosition.getTop() - paddingTop) + findViewByPosition.getMeasuredHeight();
                if (findViewByPosition != null) {
                    long currentTimeMillis = System.currentTimeMillis() - DialogsActivity.this.startArchivePullingTime;
                    if (top < dp || currentTimeMillis < 200) {
                        boolean unused2 = DialogsActivity.this.disableActionBarScrolling = true;
                        smoothScrollBy(0, top, CubicBezierInterpolator.EASE_OUT_QUINT);
                        int unused3 = this.parentPage.archivePullViewState = 2;
                    } else if (this.parentPage.archivePullViewState != 1) {
                        if (getViewOffset() == 0.0f) {
                            boolean unused4 = DialogsActivity.this.disableActionBarScrolling = true;
                            smoothScrollBy(0, findViewByPosition.getTop() - paddingTop, CubicBezierInterpolator.EASE_OUT_QUINT);
                        }
                        if (!DialogsActivity.this.canShowHiddenArchive) {
                            boolean unused5 = DialogsActivity.this.canShowHiddenArchive = true;
                            performHapticFeedback(3, 2);
                            if (this.parentPage.pullForegroundDrawable != null) {
                                this.parentPage.pullForegroundDrawable.colorize(true);
                            }
                        }
                        ((DialogCell) findViewByPosition).startOutAnimation();
                        int unused6 = this.parentPage.archivePullViewState = 1;
                    }
                    if (getViewOffset() != 0.0f) {
                        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{getViewOffset(), 0.0f});
                        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                                DialogsActivity.DialogsRecyclerView.this.lambda$onTouchEvent$0$DialogsActivity$DialogsRecyclerView(valueAnimator);
                            }
                        });
                        ofFloat.setDuration(Math.max(100, (long) (350.0f - ((getViewOffset() / ((float) PullForegroundDrawable.getMaxOverscroll())) * 120.0f))));
                        ofFloat.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                        setScrollEnabled(false);
                        ofFloat.addListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animator) {
                                super.onAnimationEnd(animator);
                                DialogsRecyclerView.this.setScrollEnabled(true);
                            }
                        });
                        ofFloat.start();
                    }
                }
            }
            return onTouchEvent;
        }

        public /* synthetic */ void lambda$onTouchEvent$0$DialogsActivity$DialogsRecyclerView(ValueAnimator valueAnimator) {
            setViewsOffset(((Float) valueAnimator.getAnimatedValue()).floatValue());
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (this.fastScrollAnimationRunning || DialogsActivity.this.waitingForScrollFinished || DialogsActivity.this.dialogRemoveFinished != 0 || DialogsActivity.this.dialogInsertFinished != 0 || DialogsActivity.this.dialogChangeFinished != 0) {
                return false;
            }
            if (motionEvent.getAction() == 0) {
                DialogsActivity dialogsActivity = DialogsActivity.this;
                boolean unused = dialogsActivity.allowSwipeDuringCurrentTouch = !dialogsActivity.actionBar.isActionModeShowed();
                checkIfAdapterValid();
            }
            return super.onInterceptTouchEvent(motionEvent);
        }
    }

    private class SwipeController extends ItemTouchHelper.Callback {
        private ViewPage parentPage;
        /* access modifiers changed from: private */
        public boolean swipeFolderBack;
        /* access modifiers changed from: private */
        public boolean swipingFolder;

        public float getSwipeEscapeVelocity(float f) {
            return 3500.0f;
        }

        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.3f;
        }

        public float getSwipeVelocityThreshold(float f) {
            return Float.MAX_VALUE;
        }

        public SwipeController(ViewPage viewPage) {
            this.parentPage = viewPage;
        }

        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (!DialogsActivity.this.waitingForDialogsAnimationEnd(this.parentPage) && (DialogsActivity.this.parentLayout == null || !DialogsActivity.this.parentLayout.isInPreviewMode())) {
                if (this.swipingFolder && this.swipeFolderBack) {
                    this.swipingFolder = false;
                    return 0;
                } else if (!DialogsActivity.this.onlySelect && this.parentPage.isDefaultDialogType() && DialogsActivity.this.slidingView == null) {
                    View view = viewHolder.itemView;
                    if (view instanceof DialogCell) {
                        DialogCell dialogCell = (DialogCell) view;
                        long dialogId = dialogCell.getDialogId();
                        if (DialogsActivity.this.actionBar.isActionModeShowed()) {
                            TLRPC$Dialog tLRPC$Dialog = DialogsActivity.this.getMessagesController().dialogs_dict.get(dialogId);
                            if (!DialogsActivity.this.allowMoving || tLRPC$Dialog == null || !DialogsActivity.this.isDialogPinned(tLRPC$Dialog) || DialogObject.isFolderDialogId(dialogId)) {
                                return 0;
                            }
                            DialogCell unused = DialogsActivity.this.movingView = (DialogCell) viewHolder.itemView;
                            DialogsActivity.this.movingView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
                            return ItemTouchHelper.Callback.makeMovementFlags(3, 0);
                        } else if ((DialogsActivity.this.filterTabsView == null || DialogsActivity.this.filterTabsView.getVisibility() != 0) && DialogsActivity.this.allowSwipeDuringCurrentTouch && dialogId != ((long) DialogsActivity.this.getUserConfig().clientUserId) && dialogId != 777000 && (!DialogsActivity.this.getMessagesController().isPromoDialog(dialogId, false) || DialogsActivity.this.getMessagesController().promoDialogType == 1)) {
                            this.swipeFolderBack = false;
                            this.swipingFolder = SharedConfig.archiveHidden && DialogObject.isFolderDialogId(dialogCell.getDialogId());
                            dialogCell.setSliding(true);
                            return ItemTouchHelper.Callback.makeMovementFlags(0, 4);
                        }
                    }
                }
            }
            return 0;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
            r2 = ((org.telegram.ui.Cells.DialogCell) r5).getDialogId();
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onMove(androidx.recyclerview.widget.RecyclerView r5, androidx.recyclerview.widget.RecyclerView.ViewHolder r6, androidx.recyclerview.widget.RecyclerView.ViewHolder r7) {
            /*
                r4 = this;
                android.view.View r5 = r7.itemView
                boolean r0 = r5 instanceof org.telegram.ui.Cells.DialogCell
                r1 = 0
                if (r0 != 0) goto L_0x0008
                return r1
            L_0x0008:
                org.telegram.ui.Cells.DialogCell r5 = (org.telegram.ui.Cells.DialogCell) r5
                long r2 = r5.getDialogId()
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                org.telegram.messenger.MessagesController r5 = r5.getMessagesController()
                android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r5 = r5.dialogs_dict
                java.lang.Object r5 = r5.get(r2)
                org.telegram.tgnet.TLRPC$Dialog r5 = (org.telegram.tgnet.TLRPC$Dialog) r5
                if (r5 == 0) goto L_0x0099
                org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                boolean r5 = r0.isDialogPinned(r5)
                if (r5 == 0) goto L_0x0099
                boolean r5 = org.telegram.messenger.DialogObject.isFolderDialogId(r2)
                if (r5 == 0) goto L_0x002d
                goto L_0x0099
            L_0x002d:
                int r5 = r6.getAdapterPosition()
                int r6 = r7.getAdapterPosition()
                org.telegram.ui.DialogsActivity$ViewPage r7 = r4.parentPage
                org.telegram.ui.Adapters.DialogsAdapter r7 = r7.dialogsAdapter
                r7.notifyItemMoved(r5, r6)
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                r5.updateDialogIndices()
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r5 = r5.viewPages
                r5 = r5[r1]
                int r5 = r5.dialogsType
                r6 = 7
                r7 = 8
                r0 = 1
                if (r5 == r6) goto L_0x006a
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r5 = r5.viewPages
                r5 = r5[r1]
                int r5 = r5.dialogsType
                if (r5 != r7) goto L_0x0064
                goto L_0x006a
            L_0x0064:
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                boolean unused = r5.movingWas = r0
                goto L_0x0098
            L_0x006a:
                org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                org.telegram.messenger.MessagesController r5 = r5.getMessagesController()
                org.telegram.messenger.MessagesController$DialogFilter[] r5 = r5.selectedDialogFilter
                org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                org.telegram.ui.DialogsActivity$ViewPage[] r6 = r6.viewPages
                r6 = r6[r1]
                int r6 = r6.dialogsType
                if (r6 != r7) goto L_0x0081
                r1 = 1
            L_0x0081:
                r5 = r5[r1]
                org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                java.util.ArrayList r6 = r6.movingDialogFilters
                boolean r6 = r6.contains(r5)
                if (r6 != 0) goto L_0x0098
                org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                java.util.ArrayList r6 = r6.movingDialogFilters
                r6.add(r5)
            L_0x0098:
                return r0
            L_0x0099:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.SwipeController.onMove(androidx.recyclerview.widget.RecyclerView, androidx.recyclerview.widget.RecyclerView$ViewHolder, androidx.recyclerview.widget.RecyclerView$ViewHolder):boolean");
        }

        public int convertToAbsoluteDirection(int i, int i2) {
            if (this.swipeFolderBack) {
                return 0;
            }
            return super.convertToAbsoluteDirection(i, i2);
        }

        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                if (DialogObject.isFolderDialogId(dialogCell.getDialogId())) {
                    this.parentPage.listView.toggleArchiveHidden(false, dialogCell);
                    return;
                }
                DialogCell unused = DialogsActivity.this.slidingView = dialogCell;
                int adapterPosition = viewHolder.getAdapterPosition();
                $$Lambda$DialogsActivity$SwipeController$p1WvioWe4QIXpl_h9JruXCBarGg r1 = new Runnable(this.parentPage.dialogsAdapter.fixPosition(adapterPosition), this.parentPage.dialogsAdapter.getItemCount(), adapterPosition) {
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        DialogsActivity.SwipeController.this.lambda$onSwiped$1$DialogsActivity$SwipeController(this.f$1, this.f$2, this.f$3);
                    }
                };
                DialogsActivity.this.setDialogsListFrozen(true);
                if (Utilities.random.nextInt(1000) == 1) {
                    if (DialogsActivity.this.pacmanAnimation == null) {
                        PacmanAnimation unused2 = DialogsActivity.this.pacmanAnimation = new PacmanAnimation(this.parentPage.listView);
                    }
                    DialogsActivity.this.pacmanAnimation.setFinishRunnable(r1);
                    DialogsActivity.this.pacmanAnimation.start();
                    return;
                }
                r1.run();
                return;
            }
            DialogCell unused3 = DialogsActivity.this.slidingView = null;
        }

        public /* synthetic */ void lambda$onSwiped$1$DialogsActivity$SwipeController(int i, int i2, int i3) {
            RecyclerView.ViewHolder findViewHolderForAdapterPosition;
            if (DialogsActivity.frozenDialogsList != null) {
                TLRPC$Dialog tLRPC$Dialog = (TLRPC$Dialog) DialogsActivity.frozenDialogsList.remove(i);
                int i4 = tLRPC$Dialog.pinnedNum;
                DialogCell unused = DialogsActivity.this.slidingView = null;
                this.parentPage.listView.invalidate();
                int findLastVisibleItemPosition = this.parentPage.layoutManager.findLastVisibleItemPosition();
                if (findLastVisibleItemPosition == i2 - 1) {
                    this.parentPage.layoutManager.findViewByPosition(findLastVisibleItemPosition).requestLayout();
                }
                boolean z = false;
                if (DialogsActivity.this.getMessagesController().isPromoDialog(tLRPC$Dialog.id, false)) {
                    DialogsActivity.this.getMessagesController().hidePromoDialog();
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$6410(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(i3);
                    int unused2 = DialogsActivity.this.dialogRemoveFinished = 2;
                    return;
                }
                int addDialogToFolder = DialogsActivity.this.getMessagesController().addDialogToFolder(tLRPC$Dialog.id, DialogsActivity.this.folderId == 0 ? 1 : 0, -1, 0);
                if (!(addDialogToFolder == 2 && i3 == 0)) {
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$6410(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(i3);
                    int unused3 = DialogsActivity.this.dialogRemoveFinished = 2;
                }
                if (DialogsActivity.this.folderId == 0) {
                    if (addDialogToFolder == 2) {
                        this.parentPage.dialogsItemAnimator.prepareForRemove();
                        if (i3 == 0) {
                            int unused4 = DialogsActivity.this.dialogChangeFinished = 2;
                            DialogsActivity.this.setDialogsListFrozen(true);
                            this.parentPage.dialogsAdapter.notifyItemChanged(0);
                        } else {
                            ViewPage.access$6408(this.parentPage);
                            this.parentPage.dialogsAdapter.notifyItemInserted(0);
                            if (!SharedConfig.archiveHidden && this.parentPage.layoutManager.findFirstVisibleItemPosition() == 0) {
                                boolean unused5 = DialogsActivity.this.disableActionBarScrolling = true;
                                this.parentPage.listView.smoothScrollBy(0, -AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f));
                            }
                        }
                        DialogsActivity.frozenDialogsList.add(0, DialogsActivity.getDialogsArray(DialogsActivity.this.currentAccount, this.parentPage.dialogsType, DialogsActivity.this.folderId, false).get(0));
                    } else if (addDialogToFolder == 1 && (findViewHolderForAdapterPosition = this.parentPage.listView.findViewHolderForAdapterPosition(0)) != null) {
                        View view = findViewHolderForAdapterPosition.itemView;
                        if (view instanceof DialogCell) {
                            DialogCell dialogCell = (DialogCell) view;
                            dialogCell.checkCurrentDialogIndex(true);
                            dialogCell.animateArchiveAvatar();
                        }
                    }
                    SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
                    if (globalMainSettings.getBoolean("archivehint_l", false) || SharedConfig.archiveHidden) {
                        z = true;
                    }
                    if (!z) {
                        globalMainSettings.edit().putBoolean("archivehint_l", true).commit();
                    }
                    DialogsActivity.this.getUndoView().showWithAction(tLRPC$Dialog.id, z ? 2 : 3, (Runnable) null, new Runnable(tLRPC$Dialog, i4) {
                        public final /* synthetic */ TLRPC$Dialog f$1;
                        public final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            DialogsActivity.SwipeController.this.lambda$null$0$DialogsActivity$SwipeController(this.f$1, this.f$2);
                        }
                    });
                }
                if (DialogsActivity.this.folderId != 0 && DialogsActivity.frozenDialogsList.isEmpty()) {
                    this.parentPage.listView.setEmptyView((View) null);
                    this.parentPage.progressView.setVisibility(4);
                }
            }
        }

        public /* synthetic */ void lambda$null$0$DialogsActivity$SwipeController(TLRPC$Dialog tLRPC$Dialog, int i) {
            boolean unused = DialogsActivity.this.dialogsListFrozen = true;
            DialogsActivity.this.getMessagesController().addDialogToFolder(tLRPC$Dialog.id, 0, i, 0);
            boolean unused2 = DialogsActivity.this.dialogsListFrozen = false;
            ArrayList<TLRPC$Dialog> dialogs = DialogsActivity.this.getMessagesController().getDialogs(0);
            int indexOf = dialogs.indexOf(tLRPC$Dialog);
            if (indexOf >= 0) {
                ArrayList<TLRPC$Dialog> dialogs2 = DialogsActivity.this.getMessagesController().getDialogs(1);
                if (!dialogs2.isEmpty() || indexOf != 1) {
                    int unused3 = DialogsActivity.this.dialogInsertFinished = 2;
                    DialogsActivity.this.setDialogsListFrozen(true);
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$6408(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemInserted(indexOf);
                }
                if (dialogs2.isEmpty()) {
                    dialogs.remove(0);
                    if (indexOf == 1) {
                        int unused4 = DialogsActivity.this.dialogChangeFinished = 2;
                        DialogsActivity.this.setDialogsListFrozen(true);
                        this.parentPage.dialogsAdapter.notifyItemChanged(0);
                        return;
                    }
                    DialogsActivity.frozenDialogsList.remove(0);
                    this.parentPage.dialogsItemAnimator.prepareForRemove();
                    ViewPage.access$6410(this.parentPage);
                    this.parentPage.dialogsAdapter.notifyItemRemoved(0);
                    return;
                }
                return;
            }
            this.parentPage.dialogsAdapter.notifyDataSetChanged();
        }

        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                this.parentPage.listView.hideSelector(false);
            }
            super.onSelectedChanged(viewHolder, i);
        }

        public long getAnimationDuration(RecyclerView recyclerView, int i, float f, float f2) {
            if (i == 4) {
                return 200;
            }
            if (i == 8 && DialogsActivity.this.movingView != null) {
                AndroidUtilities.runOnUIThread(new Runnable(DialogsActivity.this.movingView) {
                    public final /* synthetic */ View f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        this.f$0.setBackgroundDrawable((Drawable) null);
                    }
                }, this.parentPage.dialogsItemAnimator.getMoveDuration());
                DialogCell unused = DialogsActivity.this.movingView = null;
            }
            return super.getAnimationDuration(recyclerView, i, f, f2);
        }
    }

    public DialogsActivity(Bundle bundle) {
        super(bundle);
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (getArguments() != null) {
            this.onlySelect = this.arguments.getBoolean("onlySelect", false);
            this.cantSendToChannels = this.arguments.getBoolean("cantSendToChannels", false);
            this.initialDialogsType = this.arguments.getInt("dialogsType", 0);
            this.selectAlertString = this.arguments.getString("selectAlertString");
            this.selectAlertStringGroup = this.arguments.getString("selectAlertStringGroup");
            this.addToGroupAlertString = this.arguments.getString("addToGroupAlertString");
            this.allowSwitchAccount = this.arguments.getBoolean("allowSwitchAccount");
            this.checkCanWrite = this.arguments.getBoolean("checkCanWrite", true);
            this.afterSignup = this.arguments.getBoolean("afterSignup", false);
            this.folderId = this.arguments.getInt("folderId", 0);
            this.resetDelegate = this.arguments.getBoolean("resetDelegate", true);
            this.messagesCount = this.arguments.getInt("messagesCount", 0);
            this.hasPoll = this.arguments.getInt("hasPoll", 0);
        }
        if (this.initialDialogsType == 0) {
            this.askAboutContacts = MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
            SharedConfig.loadProxyList();
        }
        if (this.searchString == null) {
            this.currentConnectionState = getConnectionsManager().getConnectionState();
            getNotificationCenter().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
            if (!this.onlySelect) {
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().addObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().addObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().addObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().addObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().addObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().addObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().addObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
        }
        if (!dialogsLoaded[this.currentAccount]) {
            MessagesController messagesController = getMessagesController();
            messagesController.loadGlobalNotificationsSettings();
            messagesController.loadDialogs(this.folderId, 0, 100, true);
            messagesController.loadHintDialogs();
            messagesController.loadUserInfo(UserConfig.getInstance(this.currentAccount).getCurrentUser(), false, this.classGuid);
            getContactsController().checkInviteText();
            getMediaDataController().loadRecents(2, false, true, false);
            getMediaDataController().checkFeaturedStickers();
            Iterator<String> it = messagesController.diceEmojies.iterator();
            while (it.hasNext()) {
                getMediaDataController().loadDiceStickers(it.next(), true);
            }
            dialogsLoaded[this.currentAccount] = true;
        }
        getMessagesController().loadPinnedDialogs(this.folderId, 0, (ArrayList<Long>) null);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.searchString == null) {
            getNotificationCenter().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
            if (!this.onlySelect) {
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().removeObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().removeObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().removeObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().removeObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().removeObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().removeObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
        }
        ChatActivityEnterView chatActivityEnterView = this.commentView;
        if (chatActivityEnterView != null) {
            chatActivityEnterView.onDestroy();
        }
        UndoView[] undoViewArr = this.undoView;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
        this.delegate = null;
    }

    /* access modifiers changed from: protected */
    public ActionBar createActionBar(Context context) {
        AnonymousClass2 r0 = new ActionBar(context) {
            /* access modifiers changed from: protected */
            public boolean shouldClipChild(View view) {
                return super.shouldClipChild(view) || view == DialogsActivity.this.doneItem;
            }
        };
        r0.setBackgroundColor(Theme.getColor("actionBarDefault"));
        r0.setItemsBackgroundColor(Theme.getColor("actionBarDefaultSelector"), false);
        r0.setItemsBackgroundColor(Theme.getColor("actionBarActionModeDefaultSelector"), true);
        r0.setItemsColor(Theme.getColor("actionBarDefaultIcon"), false);
        r0.setItemsColor(Theme.getColor("actionBarActionModeDefaultIcon"), true);
        if (this.inPreviewMode) {
            r0.setOccupyStatusBar(false);
        }
        return r0;
    }

    public View createView(Context context) {
        int i;
        Context context2 = context;
        this.searching = false;
        this.searchWas = false;
        this.pacmanAnimation = null;
        this.selectedDialogs.clear();
        this.maximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        AndroidUtilities.runOnUIThread(new Runnable(context2) {
            public final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                Theme.createChatResources(this.f$0, false);
            }
        });
        ActionBarMenu createMenu = this.actionBar.createMenu();
        if (!this.onlySelect && this.searchString == null && this.folderId == 0) {
            ActionBarMenuItem actionBarMenuItem = new ActionBarMenuItem(context, (ActionBarMenu) null, Theme.getColor("actionBarDefaultSelector"), Theme.getColor("actionBarDefaultIcon"), true);
            this.doneItem = actionBarMenuItem;
            actionBarMenuItem.setText(LocaleController.getString("Done", NUM).toUpperCase());
            this.actionBar.addView(this.doneItem, LayoutHelper.createFrame(-2, -2.0f, 53, 0.0f, 0.0f, 10.0f, 0.0f));
            this.doneItem.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    DialogsActivity.this.lambda$createView$2$DialogsActivity(view);
                }
            });
            this.doneItem.setAlpha(0.0f);
            this.doneItem.setVisibility(8);
            ProxyDrawable proxyDrawable2 = new ProxyDrawable(context2);
            this.proxyDrawable = proxyDrawable2;
            ActionBarMenuItem addItem = createMenu.addItem(2, (Drawable) proxyDrawable2);
            this.proxyItem = addItem;
            addItem.setContentDescription(LocaleController.getString("ProxySettings", NUM));
            this.passcodeItem = createMenu.addItem(1, NUM);
            updatePasscodeButton();
            updateProxyButton(false);
        }
        ActionBarMenuItem addItem2 = createMenu.addItem(0, NUM);
        addItem2.setIsSearchField(true);
        addItem2.setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            public void onSearchExpand() {
                boolean unused = DialogsActivity.this.searching = true;
                if (DialogsActivity.this.switchItem != null) {
                    DialogsActivity.this.switchItem.setVisibility(8);
                }
                if (DialogsActivity.this.proxyItem != null && DialogsActivity.this.proxyItemVisible) {
                    DialogsActivity.this.proxyItem.setVisibility(8);
                }
                if (DialogsActivity.this.viewPages[0] != null) {
                    if (DialogsActivity.this.searchString != null) {
                        DialogsActivity.this.viewPages[0].listView.hide();
                        DialogsActivity.this.searchListView.show();
                    }
                    if (!DialogsActivity.this.onlySelect) {
                        DialogsActivity.this.floatingButtonContainer.setVisibility(8);
                    }
                }
                DialogsActivity.this.setScrollY(0.0f);
                DialogsActivity.this.updatePasscodeButton();
                DialogsActivity.this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrGoBack", NUM));
            }

            public boolean canCollapseSearch() {
                if (DialogsActivity.this.switchItem != null) {
                    DialogsActivity.this.switchItem.setVisibility(0);
                }
                if (DialogsActivity.this.proxyItem != null && DialogsActivity.this.proxyItemVisible) {
                    DialogsActivity.this.proxyItem.setVisibility(0);
                }
                if (DialogsActivity.this.searchString == null) {
                    return true;
                }
                DialogsActivity.this.finishFragment();
                return false;
            }

            public void onSearchCollapse() {
                boolean unused = DialogsActivity.this.searching = false;
                boolean unused2 = DialogsActivity.this.searchWas = false;
                if (DialogsActivity.this.viewPages[0] != null) {
                    DialogsActivity.this.viewPages[0].listView.setEmptyView(DialogsActivity.this.folderId == 0 ? DialogsActivity.this.viewPages[0].progressView : null);
                    if (!DialogsActivity.this.onlySelect) {
                        DialogsActivity.this.floatingButtonContainer.setVisibility(0);
                        boolean unused3 = DialogsActivity.this.floatingHidden = true;
                        float unused4 = DialogsActivity.this.floatingButtonTranslation = (float) AndroidUtilities.dp(100.0f);
                        float unused5 = DialogsActivity.this.floatingButtonHideProgress = 1.0f;
                        DialogsActivity.this.updateFloatingButtonOffset();
                    }
                    DialogsActivity.this.showSearch(false, true);
                }
                DialogsActivity.this.updatePasscodeButton();
                if (DialogsActivity.this.menuDrawable != null) {
                    DialogsActivity.this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", NUM));
                }
            }

            public void onTextChanged(EditText editText) {
                String obj = editText.getText().toString();
                if (obj.length() != 0 || (DialogsActivity.this.dialogsSearchAdapter != null && DialogsActivity.this.dialogsSearchAdapter.hasRecentRearch())) {
                    boolean unused = DialogsActivity.this.searchWas = true;
                    if (DialogsActivity.this.viewPages[0].listView.getVisibility() == 0) {
                        DialogsActivity.this.showSearch(true, true);
                    }
                }
                if (DialogsActivity.this.dialogsSearchAdapter != null) {
                    boolean unused2 = DialogsActivity.this.lastSearchScrolledToTop = false;
                    DialogsActivity.this.dialogsSearchAdapter.searchDialogs(obj);
                }
            }
        });
        this.searchItem = addItem2;
        addItem2.setClearsTextOnSearchCollapse(false);
        this.searchItem.setSearchFieldHint(LocaleController.getString("Search", NUM));
        this.searchItem.setContentDescription(LocaleController.getString("Search", NUM));
        if (this.onlySelect) {
            this.actionBar.setBackButtonImage(NUM);
            if (this.initialDialogsType == 3 && this.selectAlertString == null) {
                this.actionBar.setTitle(LocaleController.getString("ForwardTo", NUM));
            } else {
                this.actionBar.setTitle(LocaleController.getString("SelectChat", NUM));
            }
        } else {
            if (this.searchString == null && this.folderId == 0) {
                ActionBar actionBar = this.actionBar;
                MenuDrawable menuDrawable2 = new MenuDrawable();
                this.menuDrawable = menuDrawable2;
                actionBar.setBackButtonDrawable(menuDrawable2);
                this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", NUM));
            } else {
                ActionBar actionBar2 = this.actionBar;
                BackDrawable backDrawable2 = new BackDrawable(false);
                this.backDrawable = backDrawable2;
                actionBar2.setBackButtonDrawable(backDrawable2);
            }
            if (this.folderId != 0) {
                this.actionBar.setTitle(LocaleController.getString("ArchivedChats", NUM));
            } else if (BuildVars.DEBUG_VERSION) {
                this.actionBar.setTitle("Telegram Beta");
            } else {
                this.actionBar.setTitle(LocaleController.getString("AppName", NUM));
            }
            if (this.folderId == 0) {
                this.actionBar.setSupportsHolidayImage(true);
            }
        }
        if (!this.onlySelect) {
            this.actionBar.setAddToContainer(false);
            this.actionBar.setCastShadows(false);
            this.actionBar.setClipContent(true);
        }
        this.actionBar.setTitleActionRunnable(new Runnable() {
            public final void run() {
                DialogsActivity.this.lambda$createView$3$DialogsActivity();
            }
        });
        if (this.initialDialogsType == 0 && this.folderId == 0 && !this.onlySelect && TextUtils.isEmpty(this.searchString)) {
            this.scrimPaint = new Paint() {
                public void setAlpha(int i) {
                    super.setAlpha(i);
                    if (DialogsActivity.this.fragmentView != null) {
                        DialogsActivity.this.fragmentView.invalidate();
                    }
                }
            };
            AnonymousClass5 r0 = new FilterTabsView(context2) {
                public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    boolean unused = DialogsActivity.this.maybeStartTracking = false;
                    return super.onInterceptTouchEvent(motionEvent);
                }

                public void setTranslationY(float f) {
                    super.setTranslationY(f);
                    if (DialogsActivity.this.fragmentContextView != null) {
                        DialogsActivity.this.fragmentContextView.setTranslationY(((float) DialogsActivity.this.topPadding) + f);
                    }
                    if (DialogsActivity.this.fragmentLocationContextView != null) {
                        DialogsActivity.this.fragmentLocationContextView.setTranslationY(((float) DialogsActivity.this.topPadding) + f);
                    }
                    if (DialogsActivity.this.fragmentView != null) {
                        DialogsActivity.this.fragmentView.invalidate();
                    }
                }

                /* access modifiers changed from: protected */
                public void onLayout(boolean z, int i, int i2, int i3, int i4) {
                    super.onLayout(z, i, i2, i3, i4);
                    if (DialogsActivity.this.scrimView != null) {
                        DialogsActivity.this.scrimView.getLocationInWindow(DialogsActivity.this.scrimViewLocation);
                        DialogsActivity.this.fragmentView.invalidate();
                    }
                }
            };
            this.filterTabsView = r0;
            r0.setBackgroundColor(Theme.getColor("actionBarDefault"));
            this.filterTabsView.setVisibility(8);
            this.filterTabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
                static /* synthetic */ void lambda$null$0() {
                }

                private void showDeleteAlert(MessagesController.DialogFilter dialogFilter) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Context) DialogsActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("FilterDelete", NUM));
                    builder.setMessage(LocaleController.getString("FilterDeleteAlert", NUM));
                    builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                    builder.setPositiveButton(LocaleController.getString("Delete", NUM), new DialogInterface.OnClickListener(dialogFilter) {
                        public final /* synthetic */ MessagesController.DialogFilter f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onClick(DialogInterface dialogInterface, int i) {
                            DialogsActivity.AnonymousClass6.this.lambda$showDeleteAlert$2$DialogsActivity$6(this.f$1, dialogInterface, i);
                        }
                    });
                    AlertDialog create = builder.create();
                    DialogsActivity.this.showDialog(create);
                    TextView textView = (TextView) create.getButton(-1);
                    if (textView != null) {
                        textView.setTextColor(Theme.getColor("dialogTextRed2"));
                    }
                }

                public /* synthetic */ void lambda$showDeleteAlert$2$DialogsActivity$6(MessagesController.DialogFilter dialogFilter, DialogInterface dialogInterface, int i) {
                    TLRPC$TL_messages_updateDialogFilter tLRPC$TL_messages_updateDialogFilter = new TLRPC$TL_messages_updateDialogFilter();
                    tLRPC$TL_messages_updateDialogFilter.id = dialogFilter.id;
                    DialogsActivity.this.getConnectionsManager().sendRequest(tLRPC$TL_messages_updateDialogFilter, $$Lambda$DialogsActivity$6$fXtf5px9w_tu3gnTsZ6u_xzYus.INSTANCE);
                    if (DialogsActivity.this.getMessagesController().dialogFilters.size() > 1) {
                        DialogsActivity.this.filterTabsView.beginCrossfade();
                    }
                    DialogsActivity.this.getMessagesController().removeFilter(dialogFilter);
                    DialogsActivity.this.getMessagesStorage().deleteDialogFilter(dialogFilter);
                    DialogsActivity.this.filterTabsView.commitCrossfade();
                }

                public void onSamePageSelected() {
                    DialogsActivity.this.scrollToTop();
                }

                public void onPageReorder(int i, int i2) {
                    for (int i3 = 0; i3 < DialogsActivity.this.viewPages.length; i3++) {
                        if (DialogsActivity.this.viewPages[i3].selectedType == i) {
                            int unused = DialogsActivity.this.viewPages[i3].selectedType = i2;
                        } else if (DialogsActivity.this.viewPages[i3].selectedType == i2) {
                            int unused2 = DialogsActivity.this.viewPages[i3].selectedType = i;
                        }
                    }
                }

                public void onPageSelected(int i, boolean z) {
                    if (DialogsActivity.this.viewPages[0].selectedType != i) {
                        ArrayList<MessagesController.DialogFilter> arrayList = DialogsActivity.this.getMessagesController().dialogFilters;
                        if (i == Integer.MAX_VALUE || (i >= 0 && i < arrayList.size())) {
                            if (DialogsActivity.this.parentLayout != null) {
                                DialogsActivity.this.parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(i == DialogsActivity.this.filterTabsView.getFirstTabId());
                            }
                            int unused = DialogsActivity.this.viewPages[1].selectedType = i;
                            DialogsActivity.this.viewPages[1].setVisibility(0);
                            DialogsActivity.this.viewPages[1].setTranslationX((float) DialogsActivity.this.viewPages[0].getMeasuredWidth());
                            DialogsActivity.this.showScrollbars(false);
                            DialogsActivity.this.switchToCurrentSelectedMode(true);
                            boolean unused2 = DialogsActivity.this.animatingForward = z;
                        }
                    }
                }

                public boolean canPerformActions() {
                    return !DialogsActivity.this.searching;
                }

                public void onPageScrolled(float f) {
                    if (f != 1.0f || DialogsActivity.this.viewPages[1].getVisibility() == 0 || DialogsActivity.this.searching) {
                        if (DialogsActivity.this.animatingForward) {
                            DialogsActivity.this.viewPages[0].setTranslationX((-f) * ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                            DialogsActivity.this.viewPages[1].setTranslationX(((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) - (((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) * f));
                        } else {
                            DialogsActivity.this.viewPages[0].setTranslationX(((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) * f);
                            DialogsActivity.this.viewPages[1].setTranslationX((((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()) * f) - ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                        }
                        if (f == 1.0f) {
                            ViewPage viewPage = DialogsActivity.this.viewPages[0];
                            DialogsActivity.this.viewPages[0] = DialogsActivity.this.viewPages[1];
                            DialogsActivity.this.viewPages[1] = viewPage;
                            DialogsActivity.this.viewPages[1].setVisibility(8);
                            DialogsActivity.this.showScrollbars(true);
                            DialogsActivity.this.updateCounters(false);
                            DialogsActivity dialogsActivity = DialogsActivity.this;
                            dialogsActivity.checkListLoad(dialogsActivity.viewPages[0]);
                        }
                    }
                }

                public int getTabCounter(int i) {
                    if (i == Integer.MAX_VALUE) {
                        return DialogsActivity.this.getMessagesStorage().getMainUnreadCount();
                    }
                    ArrayList<MessagesController.DialogFilter> arrayList = DialogsActivity.this.getMessagesController().dialogFilters;
                    if (i < 0 || i >= arrayList.size()) {
                        return 0;
                    }
                    return DialogsActivity.this.getMessagesController().dialogFilters.get(i).unreadCount;
                }

                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v0, resolved type: org.telegram.ui.DialogsActivity$6$2} */
                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v1, resolved type: android.widget.ScrollView} */
                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v2, resolved type: org.telegram.ui.DialogsActivity$6$2} */
                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v30, resolved type: org.telegram.ui.DialogsActivity$6$2} */
                /* JADX WARNING: Multi-variable type inference failed */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public boolean didSelectTab(org.telegram.ui.Components.FilterTabsView.TabView r17, boolean r18) {
                    /*
                        r16 = this;
                        r7 = r16
                        r8 = r17
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBar r0 = r0.actionBar
                        boolean r0 = r0.isActionModeShowed()
                        r9 = 0
                        if (r0 == 0) goto L_0x0012
                        return r9
                    L_0x0012:
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r0 = r0.scrimPopupWindow
                        r1 = 0
                        if (r0 == 0) goto L_0x002f
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r0 = r0.scrimPopupWindow
                        r0.dismiss()
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow unused = r0.scrimPopupWindow = r1
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarMenuSubItem[] unused = r0.scrimPopupWindowItems = r1
                        return r9
                    L_0x002f:
                        android.graphics.Rect r0 = new android.graphics.Rect
                        r0.<init>()
                        int r2 = r17.getId()
                        r10 = 2147483647(0x7fffffff, float:NaN)
                        if (r2 != r10) goto L_0x003e
                        goto L_0x0050
                    L_0x003e:
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        org.telegram.messenger.MessagesController r1 = r1.getMessagesController()
                        java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r1 = r1.dialogFilters
                        int r2 = r17.getId()
                        java.lang.Object r1 = r1.get(r2)
                        org.telegram.messenger.MessagesController$DialogFilter r1 = (org.telegram.messenger.MessagesController.DialogFilter) r1
                    L_0x0050:
                        r11 = r1
                        org.telegram.ui.ActionBar.ActionBarPopupWindow$ActionBarPopupWindowLayout r12 = new org.telegram.ui.ActionBar.ActionBarPopupWindow$ActionBarPopupWindowLayout
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r1 = r1.getParentActivity()
                        r12.<init>(r1)
                        org.telegram.ui.DialogsActivity$6$1 r1 = new org.telegram.ui.DialogsActivity$6$1
                        r1.<init>(r0)
                        r12.setOnTouchListener(r1)
                        org.telegram.ui.-$$Lambda$DialogsActivity$6$-HrDtEVLsO28G972M5KBKaQ2RLw r0 = new org.telegram.ui.-$$Lambda$DialogsActivity$6$-HrDtEVLsO28G972M5KBKaQ2RLw
                        r0.<init>()
                        r12.setDispatchKeyEventListener(r0)
                        android.graphics.Rect r13 = new android.graphics.Rect
                        r13.<init>()
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r0 = r0.getParentActivity()
                        android.content.res.Resources r0 = r0.getResources()
                        r1 = 2131165842(0x7var_, float:1.7945913E38)
                        android.graphics.drawable.Drawable r0 = r0.getDrawable(r1)
                        android.graphics.drawable.Drawable r0 = r0.mutate()
                        r0.getPadding(r13)
                        r12.setBackgroundDrawable(r0)
                        java.lang.String r0 = "actionBarDefaultSubmenuBackground"
                        int r0 = org.telegram.ui.ActionBar.Theme.getColor(r0)
                        r12.setBackgroundColor(r0)
                        android.widget.LinearLayout r14 = new android.widget.LinearLayout
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r0 = r0.getParentActivity()
                        r14.<init>(r0)
                        int r0 = android.os.Build.VERSION.SDK_INT
                        r1 = 21
                        if (r0 < r1) goto L_0x00bb
                        org.telegram.ui.DialogsActivity$6$2 r15 = new org.telegram.ui.DialogsActivity$6$2
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r2 = r0.getParentActivity()
                        r3 = 0
                        r4 = 0
                        r5 = 2131689500(0x7f0var_c, float:1.9008017E38)
                        r0 = r15
                        r1 = r16
                        r6 = r14
                        r0.<init>(r1, r2, r3, r4, r5, r6)
                        goto L_0x00c6
                    L_0x00bb:
                        android.widget.ScrollView r15 = new android.widget.ScrollView
                        org.telegram.ui.DialogsActivity r0 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r0 = r0.getParentActivity()
                        r15.<init>(r0)
                    L_0x00c6:
                        r15.setClipToPadding(r9)
                        r0 = -1073741824(0xffffffffCLASSNAME, float:-2.0)
                        r1 = -2
                        android.widget.FrameLayout$LayoutParams r0 = org.telegram.ui.Components.LayoutHelper.createFrame(r1, r0)
                        r12.addView(r15, r0)
                        r0 = 1128792064(0x43480000, float:200.0)
                        int r0 = org.telegram.messenger.AndroidUtilities.dp(r0)
                        r14.setMinimumWidth(r0)
                        r0 = 1
                        r14.setOrientation(r0)
                        org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                        r3 = 3
                        org.telegram.ui.ActionBar.ActionBarMenuSubItem[] r4 = new org.telegram.ui.ActionBar.ActionBarMenuSubItem[r3]
                        org.telegram.ui.ActionBar.ActionBarMenuSubItem[] unused = r2.scrimPopupWindowItems = r4
                        int r2 = r17.getId()
                        r4 = 2
                        if (r2 != r10) goto L_0x00f0
                        r3 = 2
                    L_0x00f0:
                        r2 = 0
                    L_0x00f1:
                        if (r2 >= r3) goto L_0x0166
                        org.telegram.ui.ActionBar.ActionBarMenuSubItem r5 = new org.telegram.ui.ActionBar.ActionBarMenuSubItem
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        android.app.Activity r6 = r6.getParentActivity()
                        r5.<init>(r6)
                        if (r2 != 0) goto L_0x011f
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        org.telegram.messenger.MessagesController r6 = r6.getMessagesController()
                        java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r6 = r6.dialogFilters
                        int r6 = r6.size()
                        if (r6 > r0) goto L_0x010f
                        goto L_0x0162
                    L_0x010f:
                        r6 = 2131625343(0x7f0e057f, float:1.8877891E38)
                        java.lang.String r10 = "FilterReorder"
                        java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r10, r6)
                        r10 = 2131165946(0x7var_fa, float:1.7946123E38)
                        r5.setTextAndIcon(r6, r10)
                        goto L_0x014f
                    L_0x011f:
                        if (r2 != r0) goto L_0x0140
                        r6 = 2131165678(0x7var_ee, float:1.794558E38)
                        if (r3 != r4) goto L_0x0133
                        r10 = 2131625309(0x7f0e055d, float:1.8877822E38)
                        java.lang.String r9 = "FilterEditAll"
                        java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r9, r10)
                        r5.setTextAndIcon(r9, r6)
                        goto L_0x014f
                    L_0x0133:
                        r9 = 2131625308(0x7f0e055c, float:1.887782E38)
                        java.lang.String r10 = "FilterEdit"
                        java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r10, r9)
                        r5.setTextAndIcon(r9, r6)
                        goto L_0x014f
                    L_0x0140:
                        r6 = 2131625302(0x7f0e0556, float:1.8877808E38)
                        java.lang.String r9 = "FilterDeleteItem"
                        java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r9, r6)
                        r9 = 2131165672(0x7var_e8, float:1.7945568E38)
                        r5.setTextAndIcon(r6, r9)
                    L_0x014f:
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarMenuSubItem[] r6 = r6.scrimPopupWindowItems
                        r6[r2] = r5
                        r14.addView(r5)
                        org.telegram.ui.-$$Lambda$DialogsActivity$6$NzA7-lsslTUAbRpm7haqn5xfhl0 r6 = new org.telegram.ui.-$$Lambda$DialogsActivity$6$NzA7-lsslTUAbRpm7haqn5xfhl0
                        r6.<init>(r2, r3, r11)
                        r5.setOnClickListener(r6)
                    L_0x0162:
                        int r2 = r2 + 1
                        r9 = 0
                        goto L_0x00f1
                    L_0x0166:
                        r2 = 51
                        android.widget.FrameLayout$LayoutParams r3 = org.telegram.ui.Components.LayoutHelper.createScroll(r1, r1, r2)
                        r15.addView(r14, r3)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.DialogsActivity$6$3 r5 = new org.telegram.ui.DialogsActivity$6$3
                        r5.<init>(r12, r1, r1)
                        org.telegram.ui.ActionBar.ActionBarPopupWindow unused = r3.scrimPopupWindow = r5
                        r1 = 1086324736(0x40CLASSNAME, float:6.0)
                        int r3 = org.telegram.messenger.AndroidUtilities.dp(r1)
                        java.lang.String r5 = "actionBarDefault"
                        int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
                        android.graphics.drawable.Drawable r3 = org.telegram.ui.ActionBar.Theme.createRoundRectDrawable(r3, r5)
                        r8.setBackground(r3)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r5 = 220(0xdc, float:3.08E-43)
                        r3.setDismissAnimationDuration(r5)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r3.setOutsideTouchable(r0)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r3.setClippingEnabled(r0)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r5 = 2131689477(0x7f0var_, float:1.900797E38)
                        r3.setAnimationStyle(r5)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r3.setFocusable(r0)
                        r3 = 1148846080(0x447a0000, float:1000.0)
                        int r5 = org.telegram.messenger.AndroidUtilities.dp(r3)
                        r6 = -2147483648(0xfffffffvar_, float:-0.0)
                        int r5 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r6)
                        int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
                        int r3 = android.view.View.MeasureSpec.makeMeasureSpec(r3, r6)
                        r12.measure(r5, r3)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r3.setInputMethodMode(r4)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        r5 = 0
                        r3.setSoftInputMode(r5)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r3 = r3.scrimPopupWindow
                        android.view.View r3 = r3.getContentView()
                        r3.setFocusableInTouchMode(r0)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        int[] r3 = r3.scrimViewLocation
                        r8.getLocationInWindow(r3)
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        int[] r3 = r3.scrimViewLocation
                        r3 = r3[r5]
                        int r5 = r13.left
                        int r3 = r3 + r5
                        r5 = 1098907648(0x41800000, float:16.0)
                        int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
                        int r3 = r3 - r5
                        int r5 = org.telegram.messenger.AndroidUtilities.dp(r1)
                        if (r3 >= r5) goto L_0x021b
                        int r3 = org.telegram.messenger.AndroidUtilities.dp(r1)
                        goto L_0x0245
                    L_0x021b:
                        org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                        android.view.View r5 = r5.fragmentView
                        int r5 = r5.getMeasuredWidth()
                        int r6 = org.telegram.messenger.AndroidUtilities.dp(r1)
                        int r5 = r5 - r6
                        int r6 = r12.getMeasuredWidth()
                        int r5 = r5 - r6
                        if (r3 <= r5) goto L_0x0245
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        android.view.View r3 = r3.fragmentView
                        int r3 = r3.getMeasuredWidth()
                        int r1 = org.telegram.messenger.AndroidUtilities.dp(r1)
                        int r3 = r3 - r1
                        int r1 = r12.getMeasuredWidth()
                        int r3 = r3 - r1
                    L_0x0245:
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        int[] r1 = r1.scrimViewLocation
                        r1 = r1[r0]
                        int r5 = r17.getMeasuredHeight()
                        int r1 = r1 + r5
                        r5 = 1094713344(0x41400000, float:12.0)
                        int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
                        int r1 = r1 - r5
                        org.telegram.ui.DialogsActivity r5 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.ActionBar.ActionBarPopupWindow r5 = r5.scrimPopupWindow
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        android.view.View r6 = r6.fragmentView
                        org.telegram.ui.DialogsActivity r9 = org.telegram.ui.DialogsActivity.this
                        int r9 = r9.getCurrentPanTranslationY()
                        int r1 = r1 - r9
                        r5.showAtLocation(r6, r2, r3, r1)
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.view.View unused = r1.scrimView = r8
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        r2 = r18
                        boolean unused = r1.scrimViewSelected = r2
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.view.View r1 = r1.fragmentView
                        r1.invalidate()
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r1 = r1.scrimAnimatorSet
                        if (r1 == 0) goto L_0x0295
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r1 = r1.scrimAnimatorSet
                        r1.cancel()
                    L_0x0295:
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r2 = new android.animation.AnimatorSet
                        r2.<init>()
                        android.animation.AnimatorSet unused = r1.scrimAnimatorSet = r2
                        java.util.ArrayList r1 = new java.util.ArrayList
                        r1.<init>()
                        org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                        android.graphics.Paint r2 = r2.scrimPaint
                        android.util.Property<android.graphics.Paint, java.lang.Integer> r3 = org.telegram.ui.Components.AnimationProperties.PAINT_ALPHA
                        int[] r4 = new int[r4]
                        r4 = {0, 50} // fill-array
                        android.animation.ObjectAnimator r2 = android.animation.ObjectAnimator.ofInt(r2, r3, r4)
                        r1.add(r2)
                        org.telegram.ui.DialogsActivity r2 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r2 = r2.scrimAnimatorSet
                        r2.playTogether(r1)
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r1 = r1.scrimAnimatorSet
                        r2 = 150(0x96, double:7.4E-322)
                        r1.setDuration(r2)
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        android.animation.AnimatorSet r1 = r1.scrimAnimatorSet
                        r1.start()
                        return r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.AnonymousClass6.didSelectTab(org.telegram.ui.Components.FilterTabsView$TabView, boolean):boolean");
                }

                public /* synthetic */ void lambda$didSelectTab$3$DialogsActivity$6(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == 4 && keyEvent.getRepeatCount() == 0 && DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing()) {
                        DialogsActivity.this.scrimPopupWindow.dismiss();
                    }
                }

                public /* synthetic */ void lambda$didSelectTab$4$DialogsActivity$6(int i, int i2, MessagesController.DialogFilter dialogFilter, View view) {
                    if (i == 0) {
                        DialogsActivity.this.resetScroll();
                        DialogsActivity.this.filterTabsView.setIsEditing(true);
                        DialogsActivity.this.showDoneItem(true);
                    } else if (i == 1) {
                        if (i2 == 2) {
                            DialogsActivity.this.presentFragment(new FiltersSetupActivity());
                        } else {
                            DialogsActivity.this.presentFragment(new FilterCreateActivity(dialogFilter));
                        }
                    } else if (i == 2) {
                        showDeleteAlert(dialogFilter);
                    }
                    if (DialogsActivity.this.scrimPopupWindow != null) {
                        DialogsActivity.this.scrimPopupWindow.dismiss();
                    }
                }

                public boolean isTabMenuVisible() {
                    return DialogsActivity.this.scrimPopupWindow != null && DialogsActivity.this.scrimPopupWindow.isShowing();
                }

                public void onDeletePressed(int i) {
                    showDeleteAlert(DialogsActivity.this.getMessagesController().dialogFilters.get(i));
                }
            });
        }
        if (this.allowSwitchAccount && UserConfig.getActivatedAccountsCount() > 1) {
            this.switchItem = createMenu.addItemWithWidth(1, 0, AndroidUtilities.dp(56.0f));
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(AndroidUtilities.dp(12.0f));
            BackupImageView backupImageView = new BackupImageView(context2);
            backupImageView.setRoundRadius(AndroidUtilities.dp(18.0f));
            this.switchItem.addView(backupImageView, LayoutHelper.createFrame(36, 36, 17));
            TLRPC$User currentUser = getUserConfig().getCurrentUser();
            avatarDrawable.setInfo(currentUser);
            backupImageView.getImageReceiver().setCurrentAccount(this.currentAccount);
            backupImageView.setImage(ImageLocation.getForUser(currentUser, false), "50_50", (Drawable) avatarDrawable, (Object) currentUser);
            for (int i2 = 0; i2 < 3; i2++) {
                if (AccountInstance.getInstance(i2).getUserConfig().getCurrentUser() != null) {
                    AccountSelectCell accountSelectCell = new AccountSelectCell(context2);
                    accountSelectCell.setAccount(i2, true);
                    this.switchItem.addSubItem(i2 + 10, (View) accountSelectCell, AndroidUtilities.dp(230.0f), AndroidUtilities.dp(48.0f));
                }
            }
        }
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            public void onItemClick(int i) {
                int i2 = i;
                if (i2 == -1) {
                    if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.isEditing()) {
                        DialogsActivity.this.filterTabsView.setIsEditing(false);
                        DialogsActivity.this.showDoneItem(false);
                    } else if (DialogsActivity.this.actionBar.isActionModeShowed()) {
                        DialogsActivity.this.hideActionMode(true);
                    } else if (DialogsActivity.this.onlySelect || DialogsActivity.this.folderId != 0) {
                        DialogsActivity.this.finishFragment();
                    } else if (DialogsActivity.this.parentLayout != null) {
                        DialogsActivity.this.parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (i2 == 1) {
                    SharedConfig.appLocked = !SharedConfig.appLocked;
                    SharedConfig.saveConfig();
                    DialogsActivity.this.updatePasscodeButton();
                } else if (i2 == 2) {
                    DialogsActivity.this.presentFragment(new ProxyListActivity());
                } else if (i2 < 10 || i2 >= 13) {
                    if (i2 == 109) {
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        FiltersListBottomSheet filtersListBottomSheet = new FiltersListBottomSheet(dialogsActivity, dialogsActivity.selectedDialogs);
                        filtersListBottomSheet.setDelegate(new FiltersListBottomSheet.FiltersListBottomSheetDelegate() {
                            public final void didSelectFilter(MessagesController.DialogFilter dialogFilter) {
                                DialogsActivity.AnonymousClass7.this.lambda$onItemClick$0$DialogsActivity$7(dialogFilter);
                            }
                        });
                        DialogsActivity.this.showDialog(filtersListBottomSheet);
                    } else if (i2 == 110) {
                        MessagesController.DialogFilter dialogFilter = DialogsActivity.this.getMessagesController().dialogFilters.get(DialogsActivity.this.viewPages[0].selectedType);
                        DialogsActivity dialogsActivity2 = DialogsActivity.this;
                        ArrayList<Integer> dialogsCount = FiltersListBottomSheet.getDialogsCount(dialogsActivity2, dialogFilter, dialogsActivity2.selectedDialogs, false, false);
                        if ((dialogFilter != null ? dialogFilter.neverShow.size() : 0) + dialogsCount.size() > 100) {
                            DialogsActivity dialogsActivity3 = DialogsActivity.this;
                            dialogsActivity3.showDialog(AlertsCreator.createSimpleAlert(dialogsActivity3.getParentActivity(), LocaleController.getString("FilterAddToAlertFullTitle", NUM), LocaleController.getString("FilterRemoveFromAlertFullText", NUM)).create());
                            return;
                        }
                        if (!dialogsCount.isEmpty()) {
                            dialogFilter.neverShow.addAll(dialogsCount);
                            for (int i3 = 0; i3 < dialogsCount.size(); i3++) {
                                Integer num = dialogsCount.get(i3);
                                dialogFilter.alwaysShow.remove(num);
                                dialogFilter.pinnedDialogs.remove((long) num.intValue());
                            }
                            FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter.flags, dialogFilter.name, dialogFilter.alwaysShow, dialogFilter.neverShow, dialogFilter.pinnedDialogs, false, false, true, false, false, DialogsActivity.this, (Runnable) null);
                        }
                        DialogsActivity.this.getUndoView().showWithAction(dialogsCount.size() == 1 ? (long) dialogsCount.get(0).intValue() : 0, 21, Integer.valueOf(dialogsCount.size()), dialogFilter, (Runnable) null, (Runnable) null);
                        DialogsActivity.this.hideActionMode(false);
                    } else if (i2 == 100 || i2 == 101 || i2 == 102 || i2 == 103 || i2 == 104 || i2 == 105 || i2 == 106 || i2 == 107 || i2 == 108) {
                        DialogsActivity.this.perfromSelectedDialogsAction(i2, true);
                    }
                } else if (DialogsActivity.this.getParentActivity() != null) {
                    DialogsActivityDelegate access$14700 = DialogsActivity.this.delegate;
                    LaunchActivity launchActivity = (LaunchActivity) DialogsActivity.this.getParentActivity();
                    launchActivity.switchToAccount(i2 - 10, true);
                    DialogsActivity dialogsActivity4 = new DialogsActivity(DialogsActivity.this.arguments);
                    dialogsActivity4.setDelegate(access$14700);
                    launchActivity.presentFragment(dialogsActivity4, false, true);
                }
            }

            public /* synthetic */ void lambda$onItemClick$0$DialogsActivity$7(MessagesController.DialogFilter dialogFilter) {
                ArrayList<Integer> arrayList;
                long j;
                ArrayList<Integer> arrayList2;
                MessagesController.DialogFilter dialogFilter2 = dialogFilter;
                DialogsActivity dialogsActivity = DialogsActivity.this;
                ArrayList<Integer> dialogsCount = FiltersListBottomSheet.getDialogsCount(dialogsActivity, dialogFilter2, dialogsActivity.selectedDialogs, true, false);
                if ((dialogFilter2 != null ? dialogFilter2.alwaysShow.size() : 0) + dialogsCount.size() > 100) {
                    DialogsActivity dialogsActivity2 = DialogsActivity.this;
                    dialogsActivity2.showDialog(AlertsCreator.createSimpleAlert(dialogsActivity2.getParentActivity(), LocaleController.getString("FilterAddToAlertFullTitle", NUM), LocaleController.getString("FilterAddToAlertFullText", NUM)).create());
                    return;
                }
                if (dialogFilter2 != null) {
                    if (!dialogsCount.isEmpty()) {
                        for (int i = 0; i < dialogsCount.size(); i++) {
                            dialogFilter2.neverShow.remove(dialogsCount.get(i));
                        }
                        dialogFilter2.alwaysShow.addAll(dialogsCount);
                        arrayList = dialogsCount;
                        FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter2.flags, dialogFilter2.name, dialogFilter2.alwaysShow, dialogFilter2.neverShow, dialogFilter2.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, (Runnable) null);
                    } else {
                        arrayList = dialogsCount;
                    }
                    if (arrayList.size() == 1) {
                        arrayList2 = arrayList;
                        j = (long) arrayList2.get(0).intValue();
                    } else {
                        arrayList2 = arrayList;
                        j = 0;
                    }
                    DialogsActivity.this.getUndoView().showWithAction(j, 20, Integer.valueOf(arrayList2.size()), dialogFilter, (Runnable) null, (Runnable) null);
                } else {
                    DialogsActivity.this.presentFragment(new FilterCreateActivity((MessagesController.DialogFilter) null, dialogsCount));
                }
                DialogsActivity.this.hideActionMode(true);
            }
        });
        RecyclerView recyclerView = this.sideMenu;
        if (recyclerView != null) {
            recyclerView.setBackgroundColor(Theme.getColor("chats_menuBackground"));
            this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
            this.sideMenu.getAdapter().notifyDataSetChanged();
        }
        ActionBarMenu createActionMode = this.actionBar.createActionMode();
        NumberTextView numberTextView = new NumberTextView(createActionMode.getContext());
        this.selectedDialogsCountTextView = numberTextView;
        numberTextView.setTextSize(18);
        this.selectedDialogsCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.selectedDialogsCountTextView.setTextColor(Theme.getColor("actionBarActionModeDefaultIcon"));
        createActionMode.addView(this.selectedDialogsCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 72, 0, 0, 0));
        this.selectedDialogsCountTextView.setOnTouchListener($$Lambda$DialogsActivity$KLzVO306YcZLv2dmFJwtRdu9X8.INSTANCE);
        this.pinItem = createActionMode.addItemWithWidth(100, NUM, AndroidUtilities.dp(54.0f));
        this.muteItem = createActionMode.addItemWithWidth(104, NUM, AndroidUtilities.dp(54.0f));
        this.archive2Item = createActionMode.addItemWithWidth(107, NUM, AndroidUtilities.dp(54.0f));
        this.deleteItem = createActionMode.addItemWithWidth(102, NUM, AndroidUtilities.dp(54.0f), LocaleController.getString("Delete", NUM));
        ActionBarMenuItem addItemWithWidth = createActionMode.addItemWithWidth(0, NUM, AndroidUtilities.dp(54.0f), LocaleController.getString("AccDescrMoreOptions", NUM));
        this.archiveItem = addItemWithWidth.addSubItem(105, NUM, LocaleController.getString("Archive", NUM));
        this.pin2Item = addItemWithWidth.addSubItem(108, NUM, LocaleController.getString("DialogPin", NUM));
        this.addToFolderItem = addItemWithWidth.addSubItem(109, NUM, LocaleController.getString("FilterAddTo", NUM));
        this.removeFromFolderItem = addItemWithWidth.addSubItem(110, NUM, LocaleController.getString("FilterRemoveFrom", NUM));
        this.readItem = addItemWithWidth.addSubItem(101, NUM, LocaleController.getString("MarkAsRead", NUM));
        this.clearItem = addItemWithWidth.addSubItem(103, NUM, LocaleController.getString("ClearHistory", NUM));
        this.blockItem = addItemWithWidth.addSubItem(106, NUM, LocaleController.getString("BlockUser", NUM));
        this.actionModeViews.add(this.pinItem);
        this.actionModeViews.add(this.archive2Item);
        this.actionModeViews.add(this.muteItem);
        this.actionModeViews.add(this.deleteItem);
        this.actionModeViews.add(addItemWithWidth);
        ContentView contentView = new ContentView(context2);
        this.fragmentView = contentView;
        int i3 = (this.folderId == 0 && this.initialDialogsType == 0 && !this.onlySelect) ? 2 : 1;
        this.viewPages = new ViewPage[i3];
        int i4 = 0;
        while (i4 < i3) {
            final AnonymousClass8 r5 = new ViewPage(context2) {
                public void setTranslationX(float f) {
                    super.setTranslationX(f);
                    if (DialogsActivity.this.tabsAnimationInProgress && DialogsActivity.this.viewPages[0] == this) {
                        DialogsActivity.this.filterTabsView.selectTabWithId(DialogsActivity.this.viewPages[1].selectedType, Math.abs(DialogsActivity.this.viewPages[0].getTranslationX()) / ((float) DialogsActivity.this.viewPages[0].getMeasuredWidth()));
                    }
                }
            };
            contentView.addView(r5, LayoutHelper.createFrame(-1, -1.0f));
            int unused = r5.dialogsType = this.initialDialogsType;
            this.viewPages[i4] = r5;
            DialogsRecyclerView unused2 = r5.listView = new DialogsRecyclerView(context2, r5);
            r5.listView.setClipToPadding(false);
            r5.listView.setPivotY(0.0f);
            DialogsItemAnimator unused3 = r5.dialogsItemAnimator = new DialogsItemAnimator() {
                public void onRemoveStarting(RecyclerView.ViewHolder viewHolder) {
                    super.onRemoveStarting(viewHolder);
                    if (r5.layoutManager.findFirstVisibleItemPosition() == 0) {
                        View findViewByPosition = r5.layoutManager.findViewByPosition(0);
                        if (findViewByPosition != null) {
                            findViewByPosition.invalidate();
                        }
                        if (r5.archivePullViewState == 2) {
                            int unused = r5.archivePullViewState = 1;
                        }
                        if (r5.pullForegroundDrawable != null) {
                            r5.pullForegroundDrawable.doNotShow();
                        }
                    }
                }

                public void onRemoveFinished(RecyclerView.ViewHolder viewHolder) {
                    if (DialogsActivity.this.dialogRemoveFinished == 2) {
                        int unused = DialogsActivity.this.dialogRemoveFinished = 1;
                    }
                }

                public void onAddFinished(RecyclerView.ViewHolder viewHolder) {
                    if (DialogsActivity.this.dialogInsertFinished == 2) {
                        int unused = DialogsActivity.this.dialogInsertFinished = 1;
                    }
                }

                public void onChangeFinished(RecyclerView.ViewHolder viewHolder, boolean z) {
                    if (DialogsActivity.this.dialogChangeFinished == 2) {
                        int unused = DialogsActivity.this.dialogChangeFinished = 1;
                    }
                }

                /* access modifiers changed from: protected */
                public void onAllAnimationsDone() {
                    if (DialogsActivity.this.dialogRemoveFinished == 1 || DialogsActivity.this.dialogInsertFinished == 1 || DialogsActivity.this.dialogChangeFinished == 1) {
                        DialogsActivity.this.onDialogAnimationFinished();
                    }
                }
            };
            r5.listView.setItemAnimator(r5.dialogsItemAnimator);
            r5.listView.setVerticalScrollBarEnabled(true);
            r5.listView.setInstantClick(true);
            LinearLayoutManager unused4 = r5.layoutManager = new LinearLayoutManager(context2) {
                private boolean fixOffset;

                public void scrollToPositionWithOffset(int i, int i2) {
                    if (this.fixOffset) {
                        i2 -= r5.listView.getPaddingTop();
                    }
                    super.scrollToPositionWithOffset(i, i2);
                }

                public void prepareForDrop(View view, View view2, int i, int i2) {
                    this.fixOffset = true;
                    super.prepareForDrop(view, view2, i, i2);
                    this.fixOffset = false;
                }

                public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int i) {
                    if (!DialogsActivity.this.hasHiddenArchive() || i != 1) {
                        LinearSmoothScrollerCustom linearSmoothScrollerCustom = new LinearSmoothScrollerCustom(recyclerView.getContext(), 0);
                        linearSmoothScrollerCustom.setTargetPosition(i);
                        startSmoothScroll(linearSmoothScrollerCustom);
                        return;
                    }
                    super.smoothScrollToPosition(recyclerView, state, i);
                }

                /* JADX WARNING: Removed duplicated region for block: B:111:0x02be  */
                /* JADX WARNING: Removed duplicated region for block: B:52:0x0138  */
                /* JADX WARNING: Removed duplicated region for block: B:53:0x013c  */
                /* JADX WARNING: Removed duplicated region for block: B:61:0x015f  */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public int scrollVerticallyBy(int r18, androidx.recyclerview.widget.RecyclerView.Recycler r19, androidx.recyclerview.widget.RecyclerView.State r20) {
                    /*
                        r17 = this;
                        r0 = r17
                        r1 = r18
                        r2 = r19
                        r3 = r20
                        org.telegram.ui.DialogsActivity$ViewPage r4 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r4 = r4.listView
                        boolean r4 = r4.fastScrollAnimationRunning
                        r5 = 0
                        if (r4 == 0) goto L_0x0014
                        return r5
                    L_0x0014:
                        org.telegram.ui.DialogsActivity$ViewPage r4 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r4 = r4.listView
                        int r4 = r4.getScrollState()
                        r6 = 1
                        if (r4 != r6) goto L_0x0023
                        r4 = 1
                        goto L_0x0024
                    L_0x0023:
                        r4 = 0
                    L_0x0024:
                        org.telegram.ui.DialogsActivity$ViewPage r7 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r7 = r7.listView
                        int r7 = r7.getPaddingTop()
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        int r8 = r8.dialogsType
                        r9 = 2
                        r10 = 1065353216(0x3var_, float:1.0)
                        if (r8 != 0) goto L_0x010a
                        org.telegram.ui.DialogsActivity r8 = org.telegram.ui.DialogsActivity.this
                        boolean r8 = r8.onlySelect
                        if (r8 != 0) goto L_0x010a
                        org.telegram.ui.DialogsActivity r8 = org.telegram.ui.DialogsActivity.this
                        int r8 = r8.folderId
                        if (r8 != 0) goto L_0x010a
                        if (r1 >= 0) goto L_0x010a
                        org.telegram.ui.DialogsActivity r8 = org.telegram.ui.DialogsActivity.this
                        org.telegram.messenger.MessagesController r8 = r8.getMessagesController()
                        boolean r8 = r8.hasHiddenArchive()
                        if (r8 == 0) goto L_0x010a
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        int r8 = r8.archivePullViewState
                        if (r8 != r9) goto L_0x010a
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r8 = r8.listView
                        r8.setOverScrollMode(r5)
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r8 = r8.layoutManager
                        int r8 = r8.findFirstVisibleItemPosition()
                        if (r8 != 0) goto L_0x008c
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r11 = r11.layoutManager
                        android.view.View r11 = r11.findViewByPosition(r8)
                        if (r11 == 0) goto L_0x008c
                        int r11 = r11.getBottom()
                        int r11 = r11 - r7
                        int r12 = org.telegram.messenger.AndroidUtilities.dp(r10)
                        if (r11 > r12) goto L_0x008c
                        r8 = 1
                    L_0x008c:
                        if (r4 != 0) goto L_0x00b8
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r11 = r11.layoutManager
                        android.view.View r11 = r11.findViewByPosition(r8)
                        boolean r12 = org.telegram.messenger.SharedConfig.useThreeLinesLayout
                        if (r12 == 0) goto L_0x009f
                        r12 = 1117519872(0x429CLASSNAME, float:78.0)
                        goto L_0x00a1
                    L_0x009f:
                        r12 = 1116733440(0x42900000, float:72.0)
                    L_0x00a1:
                        int r12 = org.telegram.messenger.AndroidUtilities.dp(r12)
                        int r12 = r12 + r6
                        int r11 = r11.getTop()
                        int r11 = r11 - r7
                        int r11 = -r11
                        int r8 = r8 - r6
                        int r8 = r8 * r12
                        int r11 = r11 + r8
                        int r8 = java.lang.Math.abs(r18)
                        if (r11 >= r8) goto L_0x010a
                        int r8 = -r11
                        goto L_0x010b
                    L_0x00b8:
                        if (r8 != 0) goto L_0x010a
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r11 = r11.layoutManager
                        android.view.View r8 = r11.findViewByPosition(r8)
                        int r11 = r8.getTop()
                        int r11 = r11 - r7
                        float r11 = (float) r11
                        int r8 = r8.getMeasuredHeight()
                        float r8 = (float) r8
                        float r11 = r11 / r8
                        float r11 = r11 + r10
                        int r8 = (r11 > r10 ? 1 : (r11 == r10 ? 0 : -1))
                        if (r8 <= 0) goto L_0x00d7
                        r11 = 1065353216(0x3var_, float:1.0)
                    L_0x00d7:
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r8 = r8.listView
                        r8.setOverScrollMode(r9)
                        float r8 = (float) r1
                        r12 = 1055286886(0x3ee66666, float:0.45)
                        r13 = 1048576000(0x3e800000, float:0.25)
                        float r11 = r11 * r13
                        float r12 = r12 - r11
                        float r8 = r8 * r12
                        int r8 = (int) r8
                        r11 = -1
                        if (r8 <= r11) goto L_0x00f0
                        r8 = -1
                    L_0x00f0:
                        org.telegram.ui.DialogsActivity r11 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.Components.UndoView[] r11 = r11.undoView
                        r11 = r11[r5]
                        int r11 = r11.getVisibility()
                        if (r11 != 0) goto L_0x010b
                        org.telegram.ui.DialogsActivity r11 = org.telegram.ui.DialogsActivity.this
                        org.telegram.ui.Components.UndoView[] r11 = r11.undoView
                        r11 = r11[r5]
                        r11.hide(r6, r6)
                        goto L_0x010b
                    L_0x010a:
                        r8 = r1
                    L_0x010b:
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        int r11 = r11.dialogsType
                        r12 = 0
                        if (r11 != 0) goto L_0x0147
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r11 = r11.listView
                        float r11 = r11.getViewOffset()
                        int r11 = (r11 > r12 ? 1 : (r11 == r12 ? 0 : -1))
                        if (r11 == 0) goto L_0x0147
                        if (r1 <= 0) goto L_0x0147
                        if (r4 == 0) goto L_0x0147
                        org.telegram.ui.DialogsActivity$ViewPage r8 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r8 = r8.listView
                        float r8 = r8.getViewOffset()
                        int r8 = (int) r8
                        float r8 = (float) r8
                        float r11 = (float) r1
                        float r8 = r8 - r11
                        int r11 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1))
                        if (r11 >= 0) goto L_0x013c
                        int r8 = (int) r8
                        r11 = r8
                        r8 = 0
                        goto L_0x013d
                    L_0x013c:
                        r11 = 0
                    L_0x013d:
                        org.telegram.ui.DialogsActivity$ViewPage r13 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r13 = r13.listView
                        r13.setViewsOffset(r8)
                        r8 = r11
                    L_0x0147:
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        int r11 = r11.dialogsType
                        if (r11 != 0) goto L_0x02be
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        int r11 = r11.archivePullViewState
                        if (r11 == 0) goto L_0x02be
                        org.telegram.ui.DialogsActivity r11 = org.telegram.ui.DialogsActivity.this
                        boolean r11 = r11.hasHiddenArchive()
                        if (r11 == 0) goto L_0x02be
                        int r2 = super.scrollVerticallyBy(r8, r2, r3)
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r3 = r3.pullForegroundDrawable
                        if (r3 == 0) goto L_0x0173
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r3 = r3.pullForegroundDrawable
                        r3.scrollDy = r2
                    L_0x0173:
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r3 = r3.layoutManager
                        int r3 = r3.findFirstVisibleItemPosition()
                        r11 = 0
                        if (r3 != 0) goto L_0x018a
                        org.telegram.ui.DialogsActivity$ViewPage r11 = r5
                        androidx.recyclerview.widget.LinearLayoutManager r11 = r11.layoutManager
                        android.view.View r11 = r11.findViewByPosition(r3)
                    L_0x018a:
                        r13 = 0
                        if (r3 != 0) goto L_0x0281
                        if (r11 == 0) goto L_0x0281
                        int r3 = r11.getBottom()
                        int r3 = r3 - r7
                        r15 = 1082130432(0x40800000, float:4.0)
                        int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
                        if (r3 < r15) goto L_0x0281
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        long r15 = r3.startArchivePullingTime
                        int r3 = (r15 > r13 ? 1 : (r15 == r13 ? 0 : -1))
                        if (r3 != 0) goto L_0x01b0
                        org.telegram.ui.DialogsActivity r3 = org.telegram.ui.DialogsActivity.this
                        long r12 = java.lang.System.currentTimeMillis()
                        long unused = r3.startArchivePullingTime = r12
                    L_0x01b0:
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        int r3 = r3.archivePullViewState
                        if (r3 != r9) goto L_0x01c9
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r3 = r3.pullForegroundDrawable
                        if (r3 == 0) goto L_0x01c9
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r3 = r3.pullForegroundDrawable
                        r3.showHidden()
                    L_0x01c9:
                        int r3 = r11.getTop()
                        int r3 = r3 - r7
                        float r3 = (float) r3
                        int r7 = r11.getMeasuredHeight()
                        float r7 = (float) r7
                        float r3 = r3 / r7
                        float r3 = r3 + r10
                        int r7 = (r3 > r10 ? 1 : (r3 == r10 ? 0 : -1))
                        if (r7 <= 0) goto L_0x01dc
                        r3 = 1065353216(0x3var_, float:1.0)
                    L_0x01dc:
                        long r12 = java.lang.System.currentTimeMillis()
                        org.telegram.ui.DialogsActivity r7 = org.telegram.ui.DialogsActivity.this
                        long r14 = r7.startArchivePullingTime
                        long r12 = r12 - r14
                        r7 = 1062836634(0x3var_a, float:0.85)
                        int r7 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
                        if (r7 <= 0) goto L_0x01f5
                        r14 = 220(0xdc, double:1.087E-321)
                        int r7 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1))
                        if (r7 <= 0) goto L_0x01f5
                        r5 = 1
                    L_0x01f5:
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        boolean r6 = r6.canShowHiddenArchive
                        if (r6 == r5) goto L_0x0225
                        org.telegram.ui.DialogsActivity r6 = org.telegram.ui.DialogsActivity.this
                        boolean unused = r6.canShowHiddenArchive = r5
                        org.telegram.ui.DialogsActivity$ViewPage r6 = r5
                        int r6 = r6.archivePullViewState
                        if (r6 != r9) goto L_0x0225
                        org.telegram.ui.DialogsActivity$ViewPage r6 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r6 = r6.listView
                        r7 = 3
                        r6.performHapticFeedback(r7, r9)
                        org.telegram.ui.DialogsActivity$ViewPage r6 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r6 = r6.pullForegroundDrawable
                        if (r6 == 0) goto L_0x0225
                        org.telegram.ui.DialogsActivity$ViewPage r6 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r6 = r6.pullForegroundDrawable
                        r6.colorize(r5)
                    L_0x0225:
                        org.telegram.ui.DialogsActivity$ViewPage r5 = r5
                        int r5 = r5.archivePullViewState
                        if (r5 != r9) goto L_0x0261
                        int r8 = r8 - r2
                        if (r8 == 0) goto L_0x0261
                        if (r1 >= 0) goto L_0x0261
                        if (r4 == 0) goto L_0x0261
                        org.telegram.ui.DialogsActivity$ViewPage r4 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r4 = r4.listView
                        float r4 = r4.getViewOffset()
                        int r5 = org.telegram.ui.Components.PullForegroundDrawable.getMaxOverscroll()
                        float r5 = (float) r5
                        float r4 = r4 / r5
                        float r10 = r10 - r4
                        org.telegram.ui.DialogsActivity$ViewPage r4 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r4 = r4.listView
                        float r4 = r4.getViewOffset()
                        float r1 = (float) r1
                        r5 = 1045220557(0x3e4ccccd, float:0.2)
                        float r1 = r1 * r5
                        float r1 = r1 * r10
                        float r4 = r4 - r1
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r1 = r1.listView
                        r1.setViewsOffset(r4)
                    L_0x0261:
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        if (r1 == 0) goto L_0x02b8
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        r1.pullProgress = r3
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r3 = r3.listView
                        r1.setListView(r3)
                        goto L_0x02b8
                    L_0x0281:
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        long unused = r1.startArchivePullingTime = r13
                        org.telegram.ui.DialogsActivity r1 = org.telegram.ui.DialogsActivity.this
                        boolean unused = r1.canShowHiddenArchive = r5
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        int unused = r1.archivePullViewState = r9
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        if (r1 == 0) goto L_0x02b8
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        r1.resetText()
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        r1.pullProgress = r12
                        org.telegram.ui.DialogsActivity$ViewPage r1 = r5
                        org.telegram.ui.Components.PullForegroundDrawable r1 = r1.pullForegroundDrawable
                        org.telegram.ui.DialogsActivity$ViewPage r3 = r5
                        org.telegram.ui.DialogsActivity$DialogsRecyclerView r3 = r3.listView
                        r1.setListView(r3)
                    L_0x02b8:
                        if (r11 == 0) goto L_0x02bd
                        r11.invalidate()
                    L_0x02bd:
                        return r2
                    L_0x02be:
                        int r1 = super.scrollVerticallyBy(r8, r2, r3)
                        return r1
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.AnonymousClass10.scrollVerticallyBy(int, androidx.recyclerview.widget.RecyclerView$Recycler, androidx.recyclerview.widget.RecyclerView$State):int");
                }
            };
            r5.layoutManager.setOrientation(1);
            r5.listView.setLayoutManager(r5.layoutManager);
            r5.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
            r5.addView(r5.listView, LayoutHelper.createFrame(-1, -1.0f));
            r5.listView.setOnItemClickListener((RecyclerListView.OnItemClickListener) new RecyclerListView.OnItemClickListener(r5) {
                public final /* synthetic */ DialogsActivity.ViewPage f$1;

                {
                    this.f$1 = r2;
                }

                public final void onItemClick(View view, int i) {
                    DialogsActivity.this.lambda$createView$5$DialogsActivity(this.f$1, view, i);
                }
            });
            r5.listView.setOnItemLongClickListener((RecyclerListView.OnItemLongClickListenerExtended) new RecyclerListView.OnItemLongClickListenerExtended() {
                public boolean onItemClick(View view, int i, float f, float f2) {
                    if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0 && DialogsActivity.this.filterTabsView.isEditing()) {
                        return false;
                    }
                    return DialogsActivity.this.onItemLongClick(view, i, f, f2, r5.dialogsType, r5.dialogsAdapter);
                }

                public void onLongClickRelease() {
                    DialogsActivity.this.finishPreviewFragment();
                }

                public void onMove(float f, float f2) {
                    DialogsActivity.this.movePreviewFragment(f2);
                }
            });
            SwipeController unused5 = r5.swipeController = new SwipeController(r5);
            ItemTouchHelper unused6 = r5.itemTouchhelper = new ItemTouchHelper(r5.swipeController);
            r5.itemTouchhelper.attachToRecyclerView(r5.listView);
            r5.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                private boolean wasManualScroll;

                public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                    if (i == 1) {
                        this.wasManualScroll = true;
                        boolean unused = DialogsActivity.this.scrollingManually = true;
                    } else {
                        boolean unused2 = DialogsActivity.this.scrollingManually = false;
                    }
                    if (i == 0) {
                        this.wasManualScroll = false;
                        boolean unused3 = DialogsActivity.this.disableActionBarScrolling = false;
                        if (DialogsActivity.this.waitingForScrollFinished) {
                            boolean unused4 = DialogsActivity.this.waitingForScrollFinished = false;
                            if (DialogsActivity.this.updatePullAfterScroll) {
                                r5.listView.updatePullState();
                                boolean unused5 = DialogsActivity.this.updatePullAfterScroll = false;
                            }
                        }
                        if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0 && DialogsActivity.this.viewPages[0].listView == recyclerView) {
                            int i2 = (int) (-DialogsActivity.this.actionBar.getTranslationY());
                            int currentActionBarHeight = ActionBar.getCurrentActionBarHeight();
                            if (i2 != 0 && i2 != currentActionBarHeight) {
                                if (i2 < currentActionBarHeight / 2) {
                                    recyclerView.smoothScrollBy(0, -i2);
                                } else if (DialogsActivity.this.viewPages[0].listView.canScrollVertically(1)) {
                                    recyclerView.smoothScrollBy(0, currentActionBarHeight - i2);
                                }
                            }
                        }
                    }
                }

                public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                    View childAt;
                    int findFirstVisibleItemPosition;
                    boolean z;
                    boolean z2;
                    r5.dialogsItemAnimator.onListScroll(-i2);
                    DialogsActivity.this.checkListLoad(r5);
                    if (this.wasManualScroll && DialogsActivity.this.floatingButtonContainer.getVisibility() != 8 && recyclerView.getChildCount() > 0 && (findFirstVisibleItemPosition = r5.layoutManager.findFirstVisibleItemPosition()) != -1) {
                        RecyclerView.ViewHolder findViewHolderForAdapterPosition = recyclerView.findViewHolderForAdapterPosition(findFirstVisibleItemPosition);
                        if (!DialogsActivity.this.hasHiddenArchive() || !(findViewHolderForAdapterPosition == null || findViewHolderForAdapterPosition.getAdapterPosition() == 0)) {
                            int top = findViewHolderForAdapterPosition != null ? findViewHolderForAdapterPosition.itemView.getTop() : 0;
                            if (DialogsActivity.this.prevPosition == findFirstVisibleItemPosition) {
                                int access$15700 = DialogsActivity.this.prevTop - top;
                                z = top < DialogsActivity.this.prevTop;
                                if (Math.abs(access$15700) <= 1) {
                                    z2 = false;
                                    if (z2 && DialogsActivity.this.scrollUpdated && (z || (!z && DialogsActivity.this.scrollingManually))) {
                                        DialogsActivity.this.hideFloatingButton(z);
                                    }
                                    int unused = DialogsActivity.this.prevPosition = findFirstVisibleItemPosition;
                                    int unused2 = DialogsActivity.this.prevTop = top;
                                    boolean unused3 = DialogsActivity.this.scrollUpdated = true;
                                }
                            } else {
                                z = findFirstVisibleItemPosition > DialogsActivity.this.prevPosition;
                            }
                            z2 = true;
                            DialogsActivity.this.hideFloatingButton(z);
                            int unused4 = DialogsActivity.this.prevPosition = findFirstVisibleItemPosition;
                            int unused5 = DialogsActivity.this.prevTop = top;
                            boolean unused6 = DialogsActivity.this.scrollUpdated = true;
                        }
                    }
                    if (DialogsActivity.this.filterTabsView != null && DialogsActivity.this.filterTabsView.getVisibility() == 0 && recyclerView == DialogsActivity.this.viewPages[0].listView && !DialogsActivity.this.searching && !DialogsActivity.this.actionBar.isActionModeShowed() && !DialogsActivity.this.disableActionBarScrolling) {
                        if (i2 > 0 && DialogsActivity.this.hasHiddenArchive() && DialogsActivity.this.viewPages[0].dialogsType == 0 && (childAt = recyclerView.getChildAt(0)) != null && recyclerView.getChildViewHolder(childAt).getAdapterPosition() == 0) {
                            int measuredHeight = childAt.getMeasuredHeight() + (childAt.getTop() - recyclerView.getPaddingTop());
                            if (measuredHeight + i2 > 0) {
                                if (measuredHeight < 0) {
                                    i2 = -measuredHeight;
                                } else {
                                    return;
                                }
                            }
                        }
                        float translationY = DialogsActivity.this.actionBar.getTranslationY();
                        float f = translationY - ((float) i2);
                        if (f < ((float) (-ActionBar.getCurrentActionBarHeight()))) {
                            f = (float) (-ActionBar.getCurrentActionBarHeight());
                        } else if (f > 0.0f) {
                            f = 0.0f;
                        }
                        if (f != translationY) {
                            DialogsActivity.this.setScrollY(f);
                        }
                    }
                }
            });
            RadialProgressView unused7 = r5.progressView = new RadialProgressView(context2);
            r5.progressView.setPivotY(0.0f);
            r5.progressView.setVisibility(8);
            r5.addView(r5.progressView, LayoutHelper.createFrame(-2, -2, 17));
            int unused8 = r5.archivePullViewState = SharedConfig.archiveHidden ? 2 : 0;
            if (r5.pullForegroundDrawable == null && this.folderId == 0) {
                PullForegroundDrawable unused9 = r5.pullForegroundDrawable = new PullForegroundDrawable(this, LocaleController.getString("AccSwipeForArchive", NUM), LocaleController.getString("AccReleaseForArchive", NUM)) {
                    /* access modifiers changed from: protected */
                    public float getViewOffset() {
                        return r5.listView.getViewOffset();
                    }
                };
                if (hasHiddenArchive()) {
                    r5.pullForegroundDrawable.showHidden();
                } else {
                    r5.pullForegroundDrawable.doNotShow();
                }
                r5.pullForegroundDrawable.setWillDraw(r5.archivePullViewState != 0);
            }
            AnonymousClass14 r14 = r0;
            AnonymousClass8 r21 = r5;
            int i5 = i4;
            int i6 = i3;
            final AnonymousClass8 r7 = r21;
            AnonymousClass14 r02 = new DialogsAdapter(this, context, r5.dialogsType, this.folderId, this.onlySelect, this.selectedDialogs) {
                public void notifyDataSetChanged() {
                    int unused = r7.lastItemsCount = getItemCount();
                    super.notifyDataSetChanged();
                }
            };
            AnonymousClass8 r03 = r21;
            DialogsAdapter unused10 = r03.dialogsAdapter = r14;
            if (AndroidUtilities.isTablet() && this.openedDialogId != 0) {
                r03.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
            }
            r03.dialogsAdapter.setArchivedPullDrawable(r03.pullForegroundDrawable);
            r03.listView.setAdapter(r03.dialogsAdapter);
            r03.listView.setEmptyView(this.folderId == 0 ? r03.progressView : null);
            RecyclerAnimationScrollHelper unused11 = r03.scrollHelper = new RecyclerAnimationScrollHelper(r03.listView, r03.layoutManager);
            if (i5 != 0) {
                this.viewPages[i5].setVisibility(8);
            }
            i4 = i5 + 1;
            i3 = i6;
        }
        if (this.searchString != null) {
            i = 2;
        } else {
            i = !this.onlySelect ? 1 : 0;
        }
        AnonymousClass15 r4 = new DialogsSearchAdapter(context2, i, this.initialDialogsType) {
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
                if (!DialogsActivity.this.lastSearchScrolledToTop && DialogsActivity.this.searchListView != null) {
                    DialogsActivity.this.searchListView.scrollToPosition(0);
                    boolean unused = DialogsActivity.this.lastSearchScrolledToTop = true;
                }
            }
        };
        this.dialogsSearchAdapter = r4;
        r4.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            public void searchStateChanged(boolean z) {
                if (DialogsActivity.this.searching && DialogsActivity.this.searchWas && DialogsActivity.this.searchEmptyView != null) {
                    if (z || DialogsActivity.this.dialogsSearchAdapter.getItemCount() != 0) {
                        DialogsActivity.this.searchEmptyView.showProgress();
                    } else {
                        DialogsActivity.this.searchEmptyView.showTextView();
                    }
                }
            }

            public void didPressedOnSubDialog(long j) {
                if (!DialogsActivity.this.onlySelect) {
                    int i = (int) j;
                    Bundle bundle = new Bundle();
                    if (i > 0) {
                        bundle.putInt("user_id", i);
                    } else {
                        bundle.putInt("chat_id", -i);
                    }
                    DialogsActivity.this.closeSearch();
                    if (AndroidUtilities.isTablet() && DialogsActivity.this.viewPages != null) {
                        for (ViewPage access$9500 : DialogsActivity.this.viewPages) {
                            DialogsAdapter access$95002 = access$9500.dialogsAdapter;
                            long unused = DialogsActivity.this.openedDialogId = j;
                            access$95002.setOpenedDialogId(j);
                        }
                        DialogsActivity.this.updateVisibleRows(512);
                    }
                    if (DialogsActivity.this.searchString != null) {
                        if (DialogsActivity.this.getMessagesController().checkCanOpenChat(bundle, DialogsActivity.this)) {
                            DialogsActivity.this.getNotificationCenter().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                            DialogsActivity.this.presentFragment(new ChatActivity(bundle));
                        }
                    } else if (DialogsActivity.this.getMessagesController().checkCanOpenChat(bundle, DialogsActivity.this)) {
                        DialogsActivity.this.presentFragment(new ChatActivity(bundle));
                    }
                } else if (DialogsActivity.this.validateSlowModeDialog(j)) {
                    if (!DialogsActivity.this.selectedDialogs.isEmpty()) {
                        DialogsActivity.this.findAndUpdateCheckBox(j, DialogsActivity.this.addOrRemoveSelectedDialog(j, (View) null));
                        DialogsActivity.this.updateSelectedCount();
                        DialogsActivity.this.actionBar.closeSearchField();
                        return;
                    }
                    DialogsActivity.this.didSelectResult(j, true, false);
                }
            }

            public void needRemoveHint(int i) {
                TLRPC$User user;
                if (DialogsActivity.this.getParentActivity() != null && (user = DialogsActivity.this.getMessagesController().getUser(Integer.valueOf(i))) != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Context) DialogsActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("ChatHintsDeleteAlertTitle", NUM));
                    builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("ChatHintsDeleteAlert", NUM, ContactsController.formatName(user.first_name, user.last_name))));
                    builder.setPositiveButton(LocaleController.getString("StickersRemove", NUM), new DialogInterface.OnClickListener(i) {
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onClick(DialogInterface dialogInterface, int i) {
                            DialogsActivity.AnonymousClass16.this.lambda$needRemoveHint$0$DialogsActivity$16(this.f$1, dialogInterface, i);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                    AlertDialog create = builder.create();
                    DialogsActivity.this.showDialog(create);
                    TextView textView = (TextView) create.getButton(-1);
                    if (textView != null) {
                        textView.setTextColor(Theme.getColor("dialogTextRed2"));
                    }
                }
            }

            public /* synthetic */ void lambda$needRemoveHint$0$DialogsActivity$16(int i, DialogInterface dialogInterface, int i2) {
                DialogsActivity.this.getMediaDataController().removePeer(i);
            }

            public void needClearList() {
                AlertDialog.Builder builder = new AlertDialog.Builder((Context) DialogsActivity.this.getParentActivity());
                builder.setTitle(LocaleController.getString("ClearSearchAlertTitle", NUM));
                builder.setMessage(LocaleController.getString("ClearSearchAlert", NUM));
                builder.setPositiveButton(LocaleController.getString("ClearButton", NUM).toUpperCase(), new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        DialogsActivity.AnonymousClass16.this.lambda$needClearList$1$DialogsActivity$16(dialogInterface, i);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                AlertDialog create = builder.create();
                DialogsActivity.this.showDialog(create);
                TextView textView = (TextView) create.getButton(-1);
                if (textView != null) {
                    textView.setTextColor(Theme.getColor("dialogTextRed2"));
                }
            }

            public /* synthetic */ void lambda$needClearList$1$DialogsActivity$16(DialogInterface dialogInterface, int i) {
                if (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentSearch();
                } else {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentHashtags();
                }
            }
        });
        RecyclerListView recyclerListView = new RecyclerListView(context2);
        this.searchListView = recyclerListView;
        recyclerListView.setPivotY(0.0f);
        this.searchListView.setAdapter(this.dialogsSearchAdapter);
        this.searchListView.setVerticalScrollBarEnabled(true);
        this.searchListView.setInstantClick(true);
        this.searchListView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        RecyclerListView recyclerListView2 = this.searchListView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context2, 1, false);
        this.searchlayoutManager = linearLayoutManager;
        recyclerListView2.setLayoutManager(linearLayoutManager);
        contentView.addView(this.searchListView, LayoutHelper.createFrame(-1, -1.0f));
        this.searchListView.setOnItemClickListener((RecyclerListView.OnItemClickListener) new RecyclerListView.OnItemClickListener() {
            public final void onItemClick(View view, int i) {
                DialogsActivity.this.lambda$createView$6$DialogsActivity(view, i);
            }
        });
        this.searchListView.setOnItemLongClickListener((RecyclerListView.OnItemLongClickListenerExtended) new RecyclerListView.OnItemLongClickListenerExtended() {
            public boolean onItemClick(View view, int i, float f, float f2) {
                DialogsActivity dialogsActivity = DialogsActivity.this;
                return dialogsActivity.onItemLongClick(view, i, f, f2, -1, dialogsActivity.dialogsSearchAdapter);
            }

            public void onLongClickRelease() {
                DialogsActivity.this.finishPreviewFragment();
            }

            public void onMove(float f, float f2) {
                DialogsActivity.this.movePreviewFragment(f2);
            }
        });
        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context2);
        this.searchEmptyView = emptyTextProgressView;
        emptyTextProgressView.setPivotY(0.0f);
        this.searchEmptyView.setShowAtCenter(true);
        this.searchEmptyView.setTopImage(NUM);
        this.searchEmptyView.setText(LocaleController.getString("SettingsNoResults", NUM));
        contentView.addView(this.searchEmptyView, LayoutHelper.createFrame(-1, -1.0f));
        this.searchListView.setEmptyView(this.searchEmptyView);
        FrameLayout frameLayout = new FrameLayout(context2);
        this.floatingButtonContainer = frameLayout;
        frameLayout.setVisibility((this.onlySelect || this.folderId != 0) ? 8 : 0);
        contentView.addView(this.floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (float) ((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 14), (LocaleController.isRTL ? 3 : 5) | 80, LocaleController.isRTL ? 4.0f : 0.0f, 0.0f, LocaleController.isRTL ? 0.0f : 4.0f, 0.0f));
        this.floatingButtonContainer.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                DialogsActivity.this.lambda$createView$7$DialogsActivity(view);
            }
        });
        ImageView imageView = new ImageView(context2);
        this.floatingButton = imageView;
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        Drawable createSimpleSelectorCircleDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0f), Theme.getColor("chats_actionBackground"), Theme.getColor("chats_actionPressedBackground"));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable mutate = context.getResources().getDrawable(NUM).mutate();
            mutate.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(mutate, createSimpleSelectorCircleDrawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
            createSimpleSelectorCircleDrawable = combinedDrawable;
        }
        this.floatingButton.setBackgroundDrawable(createSimpleSelectorCircleDrawable);
        this.floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_actionIcon"), PorterDuff.Mode.MULTIPLY));
        this.floatingButton.setImageResource(NUM);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator stateListAnimator = new StateListAnimator();
            stateListAnimator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.floatingButton, View.TRANSLATION_Z, new float[]{(float) AndroidUtilities.dp(2.0f), (float) AndroidUtilities.dp(4.0f)}).setDuration(200));
            stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(this.floatingButton, View.TRANSLATION_Z, new float[]{(float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(2.0f)}).setDuration(200));
            this.floatingButton.setStateListAnimator(stateListAnimator);
            this.floatingButton.setOutlineProvider(new ViewOutlineProvider(this) {
                @SuppressLint({"NewApi"})
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                }
            });
        }
        this.floatingButtonContainer.setContentDescription(LocaleController.getString("NewMessageTitle", NUM));
        this.floatingButtonContainer.addView(this.floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, (float) (Build.VERSION.SDK_INT >= 21 ? 56 : 60), 51, 10.0f, 0.0f, 10.0f, 0.0f));
        this.searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 1 && DialogsActivity.this.searching && DialogsActivity.this.searchWas) {
                    AndroidUtilities.hideKeyboard(DialogsActivity.this.getParentActivity().getCurrentFocus());
                }
            }

            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int abs = Math.abs(DialogsActivity.this.searchlayoutManager.findLastVisibleItemPosition() - DialogsActivity.this.searchlayoutManager.findFirstVisibleItemPosition()) + 1;
                int itemCount = recyclerView.getAdapter().getItemCount();
                if (DialogsActivity.this.searching && DialogsActivity.this.searchWas && abs > 0 && DialogsActivity.this.searchlayoutManager.findLastVisibleItemPosition() == itemCount - 1 && !DialogsActivity.this.dialogsSearchAdapter.isMessagesSearchEndReached()) {
                    DialogsActivity.this.dialogsSearchAdapter.loadMoreSearchMessages();
                }
            }
        });
        if (this.searchString != null) {
            showSearch(true, false);
            this.searchEmptyView.showProgress();
            this.actionBar.openSearchField(this.searchString, false);
        } else {
            showSearch(false, false);
        }
        if (!this.onlySelect && this.initialDialogsType == 0) {
            FragmentContextView fragmentContextView2 = new FragmentContextView(context2, this, true);
            this.fragmentLocationContextView = fragmentContextView2;
            contentView.addView(fragmentContextView2, LayoutHelper.createFrame(-1, 39.0f, 51, 0.0f, -36.0f, 0.0f, 0.0f));
            FragmentContextView fragmentContextView3 = new FragmentContextView(context2, this, false);
            this.fragmentContextView = fragmentContextView3;
            contentView.addView(fragmentContextView3, LayoutHelper.createFrame(-1, 39.0f, 51, 0.0f, -36.0f, 0.0f, 0.0f));
            this.fragmentContextView.setAdditionalContextView(this.fragmentLocationContextView);
            this.fragmentLocationContextView.setAdditionalContextView(this.fragmentContextView);
        } else if (this.initialDialogsType == 3) {
            ChatActivityEnterView chatActivityEnterView = this.commentView;
            if (chatActivityEnterView != null) {
                chatActivityEnterView.onDestroy();
            }
            ChatActivityEnterView chatActivityEnterView2 = new ChatActivityEnterView(getParentActivity(), contentView, (ChatActivity) null, false);
            this.commentView = chatActivityEnterView2;
            chatActivityEnterView2.setAllowStickersAndGifs(false, false);
            this.commentView.setForceShowSendButton(true, false);
            this.commentView.setVisibility(8);
            contentView.addView(this.commentView, LayoutHelper.createFrame(-1, -2, 83));
            this.commentView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
                public /* synthetic */ void bottomPanelTranslationYChanged(float f) {
                    ChatActivityEnterView.ChatActivityEnterViewDelegate.CC.$default$bottomPanelTranslationYChanged(this, f);
                }

                public void didPressAttachButton() {
                }

                public /* synthetic */ boolean hasScheduledMessages() {
                    return ChatActivityEnterView.ChatActivityEnterViewDelegate.CC.$default$hasScheduledMessages(this);
                }

                public void needChangeVideoPreviewState(int i, float f) {
                }

                public void needSendTyping() {
                }

                public void needShowMediaBanHint() {
                }

                public void needStartRecordAudio(int i) {
                }

                public void needStartRecordVideo(int i, boolean z, int i2) {
                }

                public void onAttachButtonHidden() {
                }

                public void onAttachButtonShow() {
                }

                public void onAudioVideoInterfaceUpdated() {
                }

                public void onMessageEditEnd(boolean z) {
                }

                public void onPreAudioVideoRecord() {
                }

                public void onSendLongClick() {
                }

                public void onStickersExpandedChange() {
                }

                public void onStickersTab(boolean z) {
                }

                public void onSwitchRecordMode(boolean z) {
                }

                public void onTextChanged(CharSequence charSequence, boolean z) {
                }

                public void onTextSelectionChanged(int i, int i2) {
                }

                public void onTextSpansChanged(CharSequence charSequence) {
                }

                public void onUpdateSlowModeButton(View view, boolean z, CharSequence charSequence) {
                }

                public void onWindowSizeChanged(int i) {
                }

                public /* synthetic */ void openScheduledMessages() {
                    ChatActivityEnterView.ChatActivityEnterViewDelegate.CC.$default$openScheduledMessages(this);
                }

                public /* synthetic */ void prepareMessageSending() {
                    ChatActivityEnterView.ChatActivityEnterViewDelegate.CC.$default$prepareMessageSending(this);
                }

                public /* synthetic */ void scrollToSendingMessage() {
                    ChatActivityEnterView.ChatActivityEnterViewDelegate.CC.$default$scrollToSendingMessage(this);
                }

                public void onMessageSend(CharSequence charSequence, boolean z, int i) {
                    if (DialogsActivity.this.delegate != null && !DialogsActivity.this.selectedDialogs.isEmpty()) {
                        DialogsActivityDelegate access$14700 = DialogsActivity.this.delegate;
                        DialogsActivity dialogsActivity = DialogsActivity.this;
                        access$14700.didSelectDialogs(dialogsActivity, dialogsActivity.selectedDialogs, charSequence, false);
                    }
                }
            });
        }
        FilterTabsView filterTabsView2 = this.filterTabsView;
        if (filterTabsView2 != null) {
            contentView.addView(filterTabsView2, LayoutHelper.createFrame(-1, 44.0f));
        }
        if (!this.onlySelect) {
            contentView.addView(this.actionBar, LayoutHelper.createFrame(-1, -2.0f));
        }
        for (int i7 = 0; i7 < 2; i7++) {
            this.undoView[i7] = new UndoView(context2) {
                public void setTranslationY(float f) {
                    super.setTranslationY(f);
                    if (this == DialogsActivity.this.undoView[0] && DialogsActivity.this.undoView[1].getVisibility() != 0) {
                        float unused = DialogsActivity.this.additionalFloatingTranslation = ((float) (getMeasuredHeight() + AndroidUtilities.dp(8.0f))) - f;
                        if (DialogsActivity.this.additionalFloatingTranslation < 0.0f) {
                            float unused2 = DialogsActivity.this.additionalFloatingTranslation = 0.0f;
                        }
                        if (!DialogsActivity.this.floatingHidden) {
                            DialogsActivity.this.updateFloatingButtonOffset();
                        }
                    }
                }

                /* access modifiers changed from: protected */
                public boolean canUndo() {
                    for (ViewPage access$7400 : DialogsActivity.this.viewPages) {
                        if (access$7400.dialogsItemAnimator.isRunning()) {
                            return false;
                        }
                    }
                    return true;
                }
            };
            contentView.addView(this.undoView[i7], LayoutHelper.createFrame(-1, -2.0f, 83, 8.0f, 0.0f, 8.0f, 8.0f));
        }
        if (this.folderId != 0) {
            this.actionBar.setBackgroundColor(Theme.getColor("actionBarDefaultArchived"));
            this.viewPages[0].listView.setGlowColor(Theme.getColor("actionBarDefaultArchived"));
            this.actionBar.setTitleColor(Theme.getColor("actionBarDefaultArchivedTitle"));
            this.actionBar.setItemsColor(Theme.getColor("actionBarDefaultArchivedIcon"), false);
            this.actionBar.setItemsBackgroundColor(Theme.getColor("actionBarDefaultArchivedSelector"), false);
            this.actionBar.setSearchTextColor(Theme.getColor("actionBarDefaultArchivedSearch"), false);
            this.actionBar.setSearchTextColor(Theme.getColor("actionBarDefaultSearchArchivedPlaceholder"), true);
        }
        if (!this.onlySelect && this.initialDialogsType == 0) {
            View view = new View(context2);
            this.blurredView = view;
            view.setVisibility(8);
            contentView.addView(this.blurredView, LayoutHelper.createFrame(-1, -1.0f));
        }
        updateFilterTabs(false);
        return this.fragmentView;
    }

    public /* synthetic */ void lambda$createView$2$DialogsActivity(View view) {
        this.filterTabsView.setIsEditing(false);
        showDoneItem(false);
    }

    public /* synthetic */ void lambda$createView$3$DialogsActivity() {
        hideFloatingButton(false);
        scrollToTop();
    }

    public /* synthetic */ void lambda$createView$5$DialogsActivity(ViewPage viewPage, View view, int i) {
        onItemClick(view, i, viewPage.dialogsAdapter);
    }

    public /* synthetic */ void lambda$createView$6$DialogsActivity(View view, int i) {
        onItemClick(view, i, this.dialogsSearchAdapter);
    }

    public /* synthetic */ void lambda$createView$7$DialogsActivity(View view) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("destroyAfterSelect", true);
        presentFragment(new ContactsActivity(bundle));
    }

    /* JADX WARNING: type inference failed for: r7v0, types: [boolean] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void switchToCurrentSelectedMode(boolean r7) {
        /*
            r6 = this;
            r0 = 0
            r1 = 0
        L_0x0002:
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            int r3 = r2.length
            if (r1 >= r3) goto L_0x0013
            r2 = r2[r1]
            org.telegram.ui.DialogsActivity$DialogsRecyclerView r2 = r2.listView
            r2.stopScroll()
            int r1 = r1 + 1
            goto L_0x0002
        L_0x0013:
            r1 = r2[r7]
            org.telegram.ui.DialogsActivity$DialogsRecyclerView r1 = r1.listView
            r1.getAdapter()
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r6.viewPages
            r1 = r1[r7]
            int r1 = r1.selectedType
            r2 = 2147483647(0x7fffffff, float:NaN)
            r3 = 1
            if (r1 != r2) goto L_0x003d
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r6.viewPages
            r1 = r1[r7]
            int unused = r1.dialogsType = r0
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r6.viewPages
            r1 = r1[r7]
            org.telegram.ui.DialogsActivity$DialogsRecyclerView r1 = r1.listView
            r1.updatePullState()
            goto L_0x008e
        L_0x003d:
            org.telegram.messenger.MessagesController r1 = r6.getMessagesController()
            java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r1 = r1.dialogFilters
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            int r2 = r2.selectedType
            java.lang.Object r1 = r1.get(r2)
            org.telegram.messenger.MessagesController$DialogFilter r1 = (org.telegram.messenger.MessagesController.DialogFilter) r1
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r4 = r7 ^ 1
            r2 = r2[r4]
            int r2 = r2.dialogsType
            r4 = 8
            r5 = 7
            if (r2 != r5) goto L_0x0068
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            int unused = r2.dialogsType = r4
            goto L_0x006f
        L_0x0068:
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            int unused = r2.dialogsType = r5
        L_0x006f:
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            org.telegram.ui.DialogsActivity$DialogsRecyclerView r2 = r2.listView
            r2.setScrollEnabled(r3)
            org.telegram.messenger.MessagesController r2 = r6.getMessagesController()
            org.telegram.ui.DialogsActivity$ViewPage[] r5 = r6.viewPages
            r5 = r5[r7]
            int r5 = r5.dialogsType
            if (r5 != r4) goto L_0x008a
            r4 = 1
            goto L_0x008b
        L_0x008a:
            r4 = 0
        L_0x008b:
            r2.selectDialogFilter(r1, r4)
        L_0x008e:
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r6.viewPages
            r1 = r1[r7]
            org.telegram.ui.Adapters.DialogsAdapter r1 = r1.dialogsAdapter
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            int r2 = r2.dialogsType
            r1.setDialogsType(r2)
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r6.viewPages
            r1 = r1[r7]
            androidx.recyclerview.widget.LinearLayoutManager r1 = r1.layoutManager
            org.telegram.ui.DialogsActivity$ViewPage[] r2 = r6.viewPages
            r2 = r2[r7]
            int r2 = r2.dialogsType
            if (r2 != 0) goto L_0x00ba
            boolean r2 = r6.hasHiddenArchive()
            if (r2 == 0) goto L_0x00ba
            r0 = 1
        L_0x00ba:
            org.telegram.ui.ActionBar.ActionBar r2 = r6.actionBar
            float r2 = r2.getTranslationY()
            int r2 = (int) r2
            r1.scrollToPositionWithOffset(r0, r2)
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r6.viewPages
            r7 = r0[r7]
            r6.checkListLoad(r7)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.switchToCurrentSelectedMode(boolean):void");
    }

    /* access modifiers changed from: private */
    public void showScrollbars(boolean z) {
        if (this.viewPages != null && this.scrollBarVisible != z) {
            this.scrollBarVisible = z;
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    if (z) {
                        viewPageArr[i].listView.setScrollbarFadingEnabled(false);
                    }
                    this.viewPages[i].listView.setVerticalScrollBarEnabled(z);
                    if (z) {
                        this.viewPages[i].listView.setScrollbarFadingEnabled(true);
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private void updateFilterTabs(boolean z) {
        int findFirstVisibleItemPosition;
        if (this.filterTabsView != null) {
            ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
            if (actionBarPopupWindow != null) {
                actionBarPopupWindow.dismiss();
                this.scrimPopupWindow = null;
            }
            ArrayList<MessagesController.DialogFilter> arrayList = getMessagesController().dialogFilters;
            MessagesController.getMainSettings(this.currentAccount);
            boolean z2 = true;
            if (arrayList.isEmpty()) {
                if (this.filterTabsView.getVisibility() != 8) {
                    this.filterTabsView.setIsEditing(false);
                    showDoneItem(false);
                    this.maybeStartTracking = false;
                    if (this.startedTracking) {
                        this.startedTracking = false;
                        this.viewPages[0].setTranslationX(0.0f);
                        ViewPage[] viewPageArr = this.viewPages;
                        viewPageArr[1].setTranslationX((float) viewPageArr[0].getMeasuredWidth());
                    }
                    if (this.viewPages[0].selectedType != Integer.MAX_VALUE) {
                        int unused = this.viewPages[0].selectedType = Integer.MAX_VALUE;
                        this.viewPages[0].dialogsAdapter.setDialogsType(0);
                        int unused2 = this.viewPages[0].dialogsType = 0;
                        this.viewPages[0].dialogsAdapter.notifyDataSetChanged();
                    }
                    this.viewPages[1].setVisibility(8);
                    int unused3 = this.viewPages[1].selectedType = Integer.MAX_VALUE;
                    this.viewPages[1].dialogsAdapter.setDialogsType(0);
                    int unused4 = this.viewPages[1].dialogsType = 0;
                    this.viewPages[1].dialogsAdapter.notifyDataSetChanged();
                    this.filterTabsView.setVisibility(8);
                    int i = 0;
                    while (true) {
                        ViewPage[] viewPageArr2 = this.viewPages;
                        if (i >= viewPageArr2.length) {
                            break;
                        }
                        if (viewPageArr2[i].dialogsType == 0 && this.viewPages[i].archivePullViewState == 2 && hasHiddenArchive() && ((findFirstVisibleItemPosition = this.viewPages[i].layoutManager.findFirstVisibleItemPosition()) == 0 || findFirstVisibleItemPosition == 1)) {
                            this.viewPages[i].layoutManager.scrollToPositionWithOffset(findFirstVisibleItemPosition, 0);
                        }
                        this.viewPages[i].listView.setScrollingTouchSlop(0);
                        this.viewPages[i].listView.requestLayout();
                        this.viewPages[i].requestLayout();
                        i++;
                    }
                }
                ActionBarLayout actionBarLayout = this.parentLayout;
                if (actionBarLayout != null) {
                    actionBarLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(true);
                }
            } else if (z || this.filterTabsView.getVisibility() != 0) {
                this.filterTabsView.setVisibility(0);
                int currentTabId = this.filterTabsView.getCurrentTabId();
                if (currentTabId != Integer.MAX_VALUE && currentTabId >= arrayList.size()) {
                    this.filterTabsView.resetTabId();
                }
                this.filterTabsView.removeTabs();
                this.filterTabsView.addTab(Integer.MAX_VALUE, LocaleController.getString("FilterAllChats", NUM));
                int size = arrayList.size();
                for (int i2 = 0; i2 < size; i2++) {
                    this.filterTabsView.addTab(i2, arrayList.get(i2).name);
                }
                int currentTabId2 = this.filterTabsView.getCurrentTabId();
                if (currentTabId2 >= 0) {
                    int unused5 = this.viewPages[0].selectedType = currentTabId2;
                }
                int i3 = 0;
                while (true) {
                    ViewPage[] viewPageArr3 = this.viewPages;
                    if (i3 >= viewPageArr3.length) {
                        break;
                    }
                    if (viewPageArr3[i3].selectedType != Integer.MAX_VALUE && this.viewPages[i3].selectedType >= arrayList.size()) {
                        int unused6 = this.viewPages[i3].selectedType = arrayList.size() - 1;
                    }
                    this.viewPages[i3].listView.setScrollingTouchSlop(1);
                    i3++;
                }
                this.filterTabsView.finishAddingTabs();
                switchToCurrentSelectedMode(false);
                ActionBarLayout actionBarLayout2 = this.parentLayout;
                if (actionBarLayout2 != null) {
                    DrawerLayoutContainer drawerLayoutContainer = actionBarLayout2.getDrawerLayoutContainer();
                    if (currentTabId2 != this.filterTabsView.getFirstTabId()) {
                        z2 = false;
                    }
                    drawerLayoutContainer.setAllowOpenDrawerBySwipe(z2);
                }
            }
            updateCounters(false);
        }
    }

    /* access modifiers changed from: protected */
    public void onPanTranslationUpdate(int i) {
        if (this.viewPages != null) {
            ChatActivityEnterView chatActivityEnterView = this.commentView;
            int i2 = 0;
            if (chatActivityEnterView == null || !chatActivityEnterView.isPopupShowing()) {
                while (true) {
                    ViewPage[] viewPageArr = this.viewPages;
                    if (i2 >= viewPageArr.length) {
                        break;
                    }
                    viewPageArr[i2].setTranslationY((float) i);
                    i2++;
                }
                if (!this.onlySelect) {
                    this.actionBar.setTranslationY((float) i);
                }
                this.searchListView.setTranslationY((float) i);
                return;
            }
            this.fragmentView.setTranslationY((float) i);
            while (true) {
                ViewPage[] viewPageArr2 = this.viewPages;
                if (i2 >= viewPageArr2.length) {
                    break;
                }
                viewPageArr2[i2].setTranslationY(0.0f);
                i2++;
            }
            if (!this.onlySelect) {
                this.actionBar.setTranslationY(0.0f);
            }
            this.searchListView.setTranslationY(0.0f);
        }
    }

    public void finishFragment() {
        super.finishFragment();
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0130  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01a5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onResume() {
        /*
            r10 = this;
            super.onResume()
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.parentLayout
            boolean r0 = r0.isInPreviewMode()
            r1 = 0
            if (r0 != 0) goto L_0x0022
            android.view.View r0 = r10.blurredView
            if (r0 == 0) goto L_0x0022
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x0022
            android.view.View r0 = r10.blurredView
            r2 = 8
            r0.setVisibility(r2)
            android.view.View r0 = r10.blurredView
            r0.setBackground(r1)
        L_0x0022:
            org.telegram.ui.Components.FilterTabsView r0 = r10.filterTabsView
            r2 = 1
            r3 = 0
            if (r0 == 0) goto L_0x004a
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x004a
            org.telegram.ui.ActionBar.ActionBarLayout r0 = r10.parentLayout
            org.telegram.ui.ActionBar.DrawerLayoutContainer r0 = r0.getDrawerLayoutContainer()
            org.telegram.ui.DialogsActivity$ViewPage[] r4 = r10.viewPages
            r4 = r4[r3]
            int r4 = r4.selectedType
            org.telegram.ui.Components.FilterTabsView r5 = r10.filterTabsView
            int r5 = r5.getFirstTabId()
            if (r4 != r5) goto L_0x0046
            r4 = 1
            goto L_0x0047
        L_0x0046:
            r4 = 0
        L_0x0047:
            r0.setAllowOpenDrawerBySwipe(r4)
        L_0x004a:
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r10.viewPages
            if (r0 == 0) goto L_0x0064
            boolean r0 = r10.dialogsListFrozen
            if (r0 != 0) goto L_0x0064
            r0 = 0
        L_0x0053:
            org.telegram.ui.DialogsActivity$ViewPage[] r4 = r10.viewPages
            int r5 = r4.length
            if (r0 >= r5) goto L_0x0064
            r4 = r4[r0]
            org.telegram.ui.Adapters.DialogsAdapter r4 = r4.dialogsAdapter
            r4.notifyDataSetChanged()
            int r0 = r0 + 1
            goto L_0x0053
        L_0x0064:
            org.telegram.ui.Components.ChatActivityEnterView r0 = r10.commentView
            if (r0 == 0) goto L_0x006b
            r0.onResume()
        L_0x006b:
            boolean r0 = r10.onlySelect
            if (r0 != 0) goto L_0x007b
            int r0 = r10.folderId
            if (r0 != 0) goto L_0x007b
            org.telegram.messenger.MediaDataController r0 = r10.getMediaDataController()
            r4 = 4
            r0.checkStickers(r4)
        L_0x007b:
            org.telegram.ui.Adapters.DialogsSearchAdapter r0 = r10.dialogsSearchAdapter
            if (r0 == 0) goto L_0x0082
            r0.notifyDataSetChanged()
        L_0x0082:
            boolean r0 = r10.afterSignup
            if (r0 != 0) goto L_0x0092
            int r0 = org.telegram.messenger.UserConfig.selectedAccount
            org.telegram.messenger.UserConfig r0 = org.telegram.messenger.UserConfig.getInstance(r0)
            org.telegram.tgnet.TLRPC$TL_help_termsOfService r0 = r0.unacceptedTermsOfService
            if (r0 != 0) goto L_0x0094
            r0 = 1
            goto L_0x0095
        L_0x0092:
            r10.afterSignup = r3
        L_0x0094:
            r0 = 0
        L_0x0095:
            r4 = 2131624238(0x7f0e012e, float:1.887565E38)
            java.lang.String r5 = "AppName"
            if (r0 == 0) goto L_0x0130
            boolean r0 = r10.checkPermission
            if (r0 == 0) goto L_0x0130
            boolean r0 = r10.onlySelect
            if (r0 != 0) goto L_0x0130
            int r0 = android.os.Build.VERSION.SDK_INT
            r6 = 23
            if (r0 < r6) goto L_0x0130
            android.app.Activity r0 = r10.getParentActivity()
            if (r0 == 0) goto L_0x019e
            r10.checkPermission = r3
            java.lang.String r6 = "android.permission.READ_CONTACTS"
            int r7 = r0.checkSelfPermission(r6)
            if (r7 == 0) goto L_0x00bc
            r7 = 1
            goto L_0x00bd
        L_0x00bc:
            r7 = 0
        L_0x00bd:
            java.lang.String r8 = "android.permission.WRITE_EXTERNAL_STORAGE"
            int r9 = r0.checkSelfPermission(r8)
            if (r9 == 0) goto L_0x00c7
            r9 = 1
            goto L_0x00c8
        L_0x00c7:
            r9 = 0
        L_0x00c8:
            if (r7 != 0) goto L_0x00cc
            if (r9 == 0) goto L_0x019e
        L_0x00cc:
            r10.askingForPermissions = r2
            if (r7 == 0) goto L_0x00f6
            boolean r7 = r10.askAboutContacts
            if (r7 == 0) goto L_0x00f6
            org.telegram.messenger.UserConfig r7 = r10.getUserConfig()
            boolean r7 = r7.syncContacts
            if (r7 == 0) goto L_0x00f6
            boolean r6 = r0.shouldShowRequestPermissionRationale(r6)
            if (r6 == 0) goto L_0x00f6
            org.telegram.ui.-$$Lambda$DialogsActivity$clUvlBLfAKsuaK_B3T1eT4z1Gtc r1 = new org.telegram.ui.-$$Lambda$DialogsActivity$clUvlBLfAKsuaK_B3T1eT4z1Gtc
            r1.<init>()
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = org.telegram.ui.Components.AlertsCreator.createContactsPermissionDialog(r0, r1)
            org.telegram.ui.ActionBar.AlertDialog r0 = r0.create()
            r10.permissionDialog = r0
            r10.showDialog(r0)
            goto L_0x019e
        L_0x00f6:
            if (r9 == 0) goto L_0x012c
            boolean r6 = r0.shouldShowRequestPermissionRationale(r8)
            if (r6 == 0) goto L_0x012c
            org.telegram.ui.ActionBar.AlertDialog$Builder r6 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            r6.<init>((android.content.Context) r0)
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r6.setTitle(r0)
            r0 = 2131626465(0x7f0e09e1, float:1.8880167E38)
            java.lang.String r4 = "PermissionStorage"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r4, r0)
            r6.setMessage(r0)
            r0 = 2131626121(0x7f0e0889, float:1.887947E38)
            java.lang.String r4 = "OK"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r4, r0)
            r6.setPositiveButton(r0, r1)
            org.telegram.ui.ActionBar.AlertDialog r0 = r6.create()
            r10.permissionDialog = r0
            r10.showDialog(r0)
            goto L_0x019e
        L_0x012c:
            r10.askForPermissons(r2)
            goto L_0x019e
        L_0x0130:
            boolean r0 = r10.onlySelect
            if (r0 != 0) goto L_0x019e
            boolean r0 = org.telegram.messenger.XiaomiUtilities.isMIUI()
            if (r0 == 0) goto L_0x019e
            int r0 = android.os.Build.VERSION.SDK_INT
            r1 = 19
            if (r0 < r1) goto L_0x019e
            r0 = 10020(0x2724, float:1.4041E-41)
            boolean r0 = org.telegram.messenger.XiaomiUtilities.isCustomPermissionGranted(r0)
            if (r0 != 0) goto L_0x019e
            android.app.Activity r0 = r10.getParentActivity()
            if (r0 != 0) goto L_0x014f
            return
        L_0x014f:
            android.content.SharedPreferences r0 = org.telegram.messenger.MessagesController.getGlobalNotificationsSettings()
            java.lang.String r1 = "askedAboutMiuiLockscreen"
            boolean r0 = r0.getBoolean(r1, r3)
            if (r0 == 0) goto L_0x015c
            return
        L_0x015c:
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r1 = r10.getParentActivity()
            r0.<init>((android.content.Context) r1)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r0.setTitle(r1)
            r1 = 2131626466(0x7f0e09e2, float:1.888017E38)
            java.lang.String r4 = "PermissionXiaomiLockscreen"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            r0.setMessage(r1)
            r1 = 2131626464(0x7f0e09e0, float:1.8880165E38)
            java.lang.String r4 = "PermissionOpenSettings"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$LFpO29Hi6P9UXk9vf4Rx2HdTKDQ r4 = new org.telegram.ui.-$$Lambda$DialogsActivity$LFpO29Hi6P9UXk9vf4Rx2HdTKDQ
            r4.<init>()
            r0.setPositiveButton(r1, r4)
            r1 = 2131624853(0x7f0e0395, float:1.8876897E38)
            java.lang.String r4 = "ContactsPermissionAlertNotNow"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r4, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$gBjuaG54WzRhYTxEGEJ-3AtBCLASSNAME r4 = org.telegram.ui.$$Lambda$DialogsActivity$gBjuaG54WzRhYTxEGEJ3AtBCLASSNAME.INSTANCE
            r0.setNegativeButton(r1, r4)
            org.telegram.ui.ActionBar.AlertDialog r0 = r0.create()
            r10.showDialog(r0)
        L_0x019e:
            r10.showFiltersHint()
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r10.viewPages
            if (r0 == 0) goto L_0x01e0
            r0 = 0
        L_0x01a6:
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r10.viewPages
            int r4 = r1.length
            if (r0 >= r4) goto L_0x01e0
            r1 = r1[r0]
            int r1 = r1.dialogsType
            if (r1 != 0) goto L_0x01dd
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r10.viewPages
            r1 = r1[r0]
            int r1 = r1.archivePullViewState
            r4 = 2
            if (r1 != r4) goto L_0x01dd
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r10.viewPages
            r1 = r1[r0]
            androidx.recyclerview.widget.LinearLayoutManager r1 = r1.layoutManager
            int r1 = r1.findFirstVisibleItemPosition()
            if (r1 != 0) goto L_0x01dd
            boolean r1 = r10.hasHiddenArchive()
            if (r1 == 0) goto L_0x01dd
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r10.viewPages
            r1 = r1[r0]
            androidx.recyclerview.widget.LinearLayoutManager r1 = r1.layoutManager
            r1.scrollToPositionWithOffset(r2, r3)
        L_0x01dd:
            int r0 = r0 + 1
            goto L_0x01a6
        L_0x01e0:
            r10.showNextSupportedSuggestion()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.onResume():void");
    }

    public /* synthetic */ void lambda$onResume$8$DialogsActivity(int i) {
        this.askAboutContacts = i != 0;
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", this.askAboutContacts).commit();
        askForPermissons(false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x000e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$onResume$9$DialogsActivity(android.content.DialogInterface r2, int r3) {
        /*
            r1 = this;
            android.content.Intent r2 = org.telegram.messenger.XiaomiUtilities.getPermissionManagerIntent()
            if (r2 == 0) goto L_0x003f
            android.app.Activity r3 = r1.getParentActivity()     // Catch:{ Exception -> 0x000e }
            r3.startActivity(r2)     // Catch:{ Exception -> 0x000e }
            goto L_0x003f
        L_0x000e:
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x003b }
            java.lang.String r3 = "android.settings.APPLICATION_DETAILS_SETTINGS"
            r2.<init>(r3)     // Catch:{ Exception -> 0x003b }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003b }
            r3.<init>()     // Catch:{ Exception -> 0x003b }
            java.lang.String r0 = "package:"
            r3.append(r0)     // Catch:{ Exception -> 0x003b }
            android.content.Context r0 = org.telegram.messenger.ApplicationLoader.applicationContext     // Catch:{ Exception -> 0x003b }
            java.lang.String r0 = r0.getPackageName()     // Catch:{ Exception -> 0x003b }
            r3.append(r0)     // Catch:{ Exception -> 0x003b }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x003b }
            android.net.Uri r3 = android.net.Uri.parse(r3)     // Catch:{ Exception -> 0x003b }
            r2.setData(r3)     // Catch:{ Exception -> 0x003b }
            android.app.Activity r3 = r1.getParentActivity()     // Catch:{ Exception -> 0x003b }
            r3.startActivity(r2)     // Catch:{ Exception -> 0x003b }
            goto L_0x003f
        L_0x003b:
            r2 = move-exception
            org.telegram.messenger.FileLog.e((java.lang.Throwable) r2)
        L_0x003f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.lambda$onResume$9$DialogsActivity(android.content.DialogInterface, int):void");
    }

    public void onPause() {
        super.onPause();
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
        ChatActivityEnterView chatActivityEnterView = this.commentView;
        if (chatActivityEnterView != null) {
            chatActivityEnterView.onResume();
        }
        UndoView[] undoViewArr = this.undoView;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
    }

    public boolean onBackPressed() {
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
            return false;
        }
        FilterTabsView filterTabsView2 = this.filterTabsView;
        if (filterTabsView2 == null || !filterTabsView2.isEditing()) {
            ActionBar actionBar = this.actionBar;
            if (actionBar == null || !actionBar.isActionModeShowed()) {
                FilterTabsView filterTabsView3 = this.filterTabsView;
                if (filterTabsView3 == null || filterTabsView3.getVisibility() != 0 || this.tabsAnimationInProgress || this.filterTabsView.isAnimatingIndicator() || this.filterTabsView.getCurrentTabId() == Integer.MAX_VALUE || this.startedTracking) {
                    ChatActivityEnterView chatActivityEnterView = this.commentView;
                    if (chatActivityEnterView == null || !chatActivityEnterView.isPopupShowing()) {
                        return super.onBackPressed();
                    }
                    this.commentView.hidePopup(true);
                    return false;
                }
                this.filterTabsView.selectFirstTab();
                return false;
            }
            hideActionMode(true);
            return false;
        }
        this.filterTabsView.setIsEditing(false);
        showDoneItem(false);
        return false;
    }

    /* access modifiers changed from: protected */
    public void onBecomeFullyHidden() {
        if (this.closeSearchFieldOnHide) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            TLObject tLObject = this.searchObject;
            if (tLObject != null) {
                this.dialogsSearchAdapter.putRecentSearch(this.searchDialogId, tLObject);
                this.searchObject = null;
            }
            this.closeSearchFieldOnHide = false;
        }
        FilterTabsView filterTabsView2 = this.filterTabsView;
        if (filterTabsView2 != null && filterTabsView2.getVisibility() == 0) {
            int i = (int) (-this.actionBar.getTranslationY());
            int currentActionBarHeight = ActionBar.getCurrentActionBarHeight();
            if (!(i == 0 || i == currentActionBarHeight)) {
                if (i < currentActionBarHeight / 2) {
                    setScrollY(0.0f);
                } else if (this.viewPages[0].listView.canScrollVertically(1)) {
                    setScrollY((float) (-currentActionBarHeight));
                }
            }
        }
        UndoView[] undoViewArr = this.undoView;
        if (undoViewArr[0] != null) {
            undoViewArr[0].hide(true, 0);
        }
    }

    public boolean addOrRemoveSelectedDialog(long j, View view) {
        if (this.selectedDialogs.contains(Long.valueOf(j))) {
            this.selectedDialogs.remove(Long.valueOf(j));
            if (view instanceof DialogCell) {
                ((DialogCell) view).setChecked(false, true);
            }
            return false;
        }
        this.selectedDialogs.add(Long.valueOf(j));
        if (view instanceof DialogCell) {
            ((DialogCell) view).setChecked(true, true);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void showSearch(final boolean z, boolean z2) {
        if (this.initialDialogsType != 0) {
            z2 = false;
        }
        AnimatorSet animatorSet = this.searchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.searchAnimator = null;
        }
        Animator animator = this.tabsAlphaAnimator;
        if (animator != null) {
            animator.cancel();
            this.tabsAlphaAnimator = null;
        }
        float f = 0.9f;
        float f2 = 0.0f;
        if (z2) {
            if (z) {
                this.searchListView.setVisibility(0);
            } else {
                this.viewPages[0].listView.setVisibility(0);
                this.viewPages[0].setVisibility(0);
            }
            setDialogsListFrozen(true);
            this.viewPages[0].listView.setVerticalScrollBarEnabled(false);
            this.searchListView.setVerticalScrollBarEnabled(false);
            this.searchListView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
            this.searchAnimator = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            ViewPage viewPage = this.viewPages[0];
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            fArr[0] = z ? 0.0f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage, property, fArr));
            ViewPage viewPage2 = this.viewPages[0];
            Property property2 = View.SCALE_X;
            float[] fArr2 = new float[1];
            fArr2[0] = z ? 0.9f : 1.0f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage2, property2, fArr2));
            ViewPage viewPage3 = this.viewPages[0];
            Property property3 = View.SCALE_Y;
            float[] fArr3 = new float[1];
            if (!z) {
                f = 1.0f;
            }
            fArr3[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(viewPage3, property3, fArr3));
            RecyclerListView recyclerListView = this.searchListView;
            Property property4 = View.ALPHA;
            float[] fArr4 = new float[1];
            fArr4[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(recyclerListView, property4, fArr4));
            RecyclerListView recyclerListView2 = this.searchListView;
            Property property5 = View.SCALE_X;
            float[] fArr5 = new float[1];
            float f3 = 1.05f;
            fArr5[0] = z ? 1.0f : 1.05f;
            arrayList.add(ObjectAnimator.ofFloat(recyclerListView2, property5, fArr5));
            RecyclerListView recyclerListView3 = this.searchListView;
            Property property6 = View.SCALE_Y;
            float[] fArr6 = new float[1];
            fArr6[0] = z ? 1.0f : 1.05f;
            arrayList.add(ObjectAnimator.ofFloat(recyclerListView3, property6, fArr6));
            EmptyTextProgressView emptyTextProgressView = this.searchEmptyView;
            Property property7 = View.ALPHA;
            float[] fArr7 = new float[1];
            fArr7[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(emptyTextProgressView, property7, fArr7));
            EmptyTextProgressView emptyTextProgressView2 = this.searchEmptyView;
            Property property8 = View.SCALE_X;
            float[] fArr8 = new float[1];
            fArr8[0] = z ? 1.0f : 1.05f;
            arrayList.add(ObjectAnimator.ofFloat(emptyTextProgressView2, property8, fArr8));
            EmptyTextProgressView emptyTextProgressView3 = this.searchEmptyView;
            Property property9 = View.SCALE_Y;
            float[] fArr9 = new float[1];
            if (z) {
                f3 = 1.0f;
            }
            fArr9[0] = f3;
            arrayList.add(ObjectAnimator.ofFloat(emptyTextProgressView3, property9, fArr9));
            FilterTabsView filterTabsView2 = this.filterTabsView;
            if (filterTabsView2 != null && filterTabsView2.getVisibility() == 0) {
                FilterTabsView filterTabsView3 = this.filterTabsView;
                Property property10 = View.TRANSLATION_Y;
                float[] fArr10 = new float[1];
                fArr10[0] = z ? (float) (-AndroidUtilities.dp(44.0f)) : 0.0f;
                arrayList.add(ObjectAnimator.ofFloat(filterTabsView3, property10, fArr10));
                RecyclerListView tabsContainer = this.filterTabsView.getTabsContainer();
                Property property11 = View.ALPHA;
                float[] fArr11 = new float[1];
                if (!z) {
                    f2 = 1.0f;
                }
                fArr11[0] = f2;
                ObjectAnimator duration = ObjectAnimator.ofFloat(tabsContainer, property11, fArr11).setDuration(100);
                this.tabsAlphaAnimator = duration;
                duration.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        Animator unused = DialogsActivity.this.tabsAlphaAnimator = null;
                    }
                });
            }
            this.searchAnimator.playTogether(arrayList);
            this.searchAnimator.setDuration(z ? 200 : 180);
            this.searchAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
            if (!z) {
                this.searchAnimator.setStartDelay(20);
                Animator animator2 = this.tabsAlphaAnimator;
                if (animator2 != null) {
                    animator2.setStartDelay(80);
                }
            }
            this.searchAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (DialogsActivity.this.searchAnimator == animator) {
                        DialogsActivity.this.setDialogsListFrozen(false);
                        if (z) {
                            DialogsActivity.this.viewPages[0].listView.hide();
                            DialogsActivity.this.searchListView.show();
                        } else {
                            DialogsActivity.this.searchEmptyView.setScaleX(1.1f);
                            DialogsActivity.this.searchEmptyView.setScaleY(1.1f);
                            DialogsActivity.this.searchListView.hide();
                            DialogsActivity.this.viewPages[0].listView.show();
                            DialogsActivity.this.dialogsSearchAdapter.searchDialogs((String) null);
                            if (!DialogsActivity.this.onlySelect) {
                                DialogsActivity.this.hideFloatingButton(false);
                            }
                        }
                        DialogsActivity.this.viewPages[0].listView.setVerticalScrollBarEnabled(true);
                        DialogsActivity.this.searchListView.setVerticalScrollBarEnabled(true);
                        DialogsActivity.this.searchListView.setBackground((Drawable) null);
                        AnimatorSet unused = DialogsActivity.this.searchAnimator = null;
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    if (DialogsActivity.this.searchAnimator == animator) {
                        if (z) {
                            DialogsActivity.this.viewPages[0].listView.hide();
                            DialogsActivity.this.searchListView.show();
                        } else {
                            DialogsActivity.this.searchListView.hide();
                            DialogsActivity.this.viewPages[0].listView.show();
                        }
                        AnimatorSet unused = DialogsActivity.this.searchAnimator = null;
                    }
                }
            });
            this.searchAnimator.start();
            Animator animator3 = this.tabsAlphaAnimator;
            if (animator3 != null) {
                animator3.start();
                return;
            }
            return;
        }
        setDialogsListFrozen(false);
        if (z) {
            this.viewPages[0].listView.hide();
            this.searchListView.show();
        } else {
            this.viewPages[0].listView.show();
            this.searchListView.hide();
        }
        this.viewPages[0].setAlpha(z ? 0.0f : 1.0f);
        this.viewPages[0].setScaleX(z ? 0.9f : 1.0f);
        ViewPage viewPage4 = this.viewPages[0];
        if (!z) {
            f = 1.0f;
        }
        viewPage4.setScaleY(f);
        this.searchListView.setAlpha(z ? 1.0f : 0.0f);
        float f4 = 1.1f;
        this.searchListView.setScaleX(z ? 1.0f : 1.1f);
        this.searchListView.setScaleY(z ? 1.0f : 1.1f);
        this.searchEmptyView.setAlpha(z ? 1.0f : 0.0f);
        this.searchEmptyView.setScaleX(z ? 1.0f : 1.1f);
        EmptyTextProgressView emptyTextProgressView4 = this.searchEmptyView;
        if (z) {
            f4 = 1.0f;
        }
        emptyTextProgressView4.setScaleY(f4);
        FilterTabsView filterTabsView4 = this.filterTabsView;
        if (filterTabsView4 != null && filterTabsView4.getVisibility() == 0) {
            this.filterTabsView.setTranslationY(z ? (float) (-AndroidUtilities.dp(44.0f)) : 0.0f);
            RecyclerListView tabsContainer2 = this.filterTabsView.getTabsContainer();
            if (!z) {
                f2 = 1.0f;
            }
            tabsContainer2.setAlpha(f2);
        }
    }

    /* access modifiers changed from: private */
    public void findAndUpdateCheckBox(long j, boolean z) {
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    int childCount = viewPageArr[i].listView.getChildCount();
                    int i2 = 0;
                    while (true) {
                        if (i2 >= childCount) {
                            break;
                        }
                        View childAt = this.viewPages[i].listView.getChildAt(i2);
                        if (childAt instanceof DialogCell) {
                            DialogCell dialogCell = (DialogCell) childAt;
                            if (dialogCell.getDialogId() == j) {
                                dialogCell.setChecked(z, true);
                                break;
                            }
                        }
                        i2++;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x011a A[ADDED_TO_REGION] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkListLoad(org.telegram.ui.DialogsActivity.ViewPage r14) {
        /*
            r13 = this;
            boolean r0 = r13.tabsAnimationInProgress
            if (r0 != 0) goto L_0x0126
            boolean r0 = r13.startedTracking
            if (r0 != 0) goto L_0x0126
            org.telegram.ui.Components.FilterTabsView r0 = r13.filterTabsView
            if (r0 == 0) goto L_0x001c
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x001c
            org.telegram.ui.Components.FilterTabsView r0 = r13.filterTabsView
            boolean r0 = r0.isAnimatingIndicator()
            if (r0 == 0) goto L_0x001c
            goto L_0x0126
        L_0x001c:
            androidx.recyclerview.widget.LinearLayoutManager r0 = r14.layoutManager
            int r0 = r0.findFirstVisibleItemPosition()
            androidx.recyclerview.widget.LinearLayoutManager r1 = r14.layoutManager
            int r1 = r1.findLastVisibleItemPosition()
            int r1 = r1 - r0
            int r0 = java.lang.Math.abs(r1)
            r1 = 1
            int r0 = r0 + r1
            int r2 = r14.dialogsType
            r3 = 8
            r4 = 7
            r5 = 0
            if (r2 == r4) goto L_0x0043
            int r2 = r14.dialogsType
            if (r2 != r3) goto L_0x00b8
        L_0x0043:
            org.telegram.messenger.MessagesController r2 = r13.getMessagesController()
            java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r2 = r2.dialogFilters
            int r6 = r14.selectedType
            if (r6 < 0) goto L_0x00b8
            int r6 = r14.selectedType
            int r2 = r2.size()
            if (r6 >= r2) goto L_0x00b8
            org.telegram.messenger.MessagesController r2 = r13.getMessagesController()
            java.util.ArrayList<org.telegram.messenger.MessagesController$DialogFilter> r2 = r2.dialogFilters
            int r6 = r14.selectedType
            java.lang.Object r2 = r2.get(r6)
            org.telegram.messenger.MessagesController$DialogFilter r2 = (org.telegram.messenger.MessagesController.DialogFilter) r2
            int r2 = r2.flags
            int r6 = org.telegram.messenger.MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED
            r2 = r2 & r6
            if (r2 != 0) goto L_0x00b8
            if (r0 <= 0) goto L_0x008e
            androidx.recyclerview.widget.LinearLayoutManager r2 = r14.layoutManager
            int r2 = r2.findLastVisibleItemPosition()
            int r6 = r13.currentAccount
            int r7 = r14.dialogsType
            boolean r8 = r13.dialogsListFrozen
            java.util.ArrayList r6 = getDialogsArray(r6, r7, r1, r8)
            int r6 = r6.size()
            int r6 = r6 + -10
            if (r2 >= r6) goto L_0x009c
        L_0x008e:
            if (r0 != 0) goto L_0x00b8
            int r2 = r13.currentAccount
            org.telegram.messenger.MessagesController r2 = org.telegram.messenger.MessagesController.getInstance(r2)
            boolean r2 = r2.isDialogsEndReached(r1)
            if (r2 != 0) goto L_0x00b8
        L_0x009c:
            org.telegram.messenger.MessagesController r2 = r13.getMessagesController()
            boolean r2 = r2.isDialogsEndReached(r1)
            r2 = r2 ^ r1
            if (r2 != 0) goto L_0x00b5
            org.telegram.messenger.MessagesController r6 = r13.getMessagesController()
            boolean r6 = r6.isServerDialogsEndReached(r1)
            if (r6 != 0) goto L_0x00b2
            goto L_0x00b5
        L_0x00b2:
            r12 = r2
            r11 = 0
            goto L_0x00ba
        L_0x00b5:
            r12 = r2
            r11 = 1
            goto L_0x00ba
        L_0x00b8:
            r11 = 0
            r12 = 0
        L_0x00ba:
            if (r0 <= 0) goto L_0x00da
            androidx.recyclerview.widget.LinearLayoutManager r2 = r14.layoutManager
            int r2 = r2.findLastVisibleItemPosition()
            int r6 = r13.currentAccount
            int r7 = r14.dialogsType
            int r8 = r13.folderId
            boolean r9 = r13.dialogsListFrozen
            java.util.ArrayList r6 = getDialogsArray(r6, r7, r8, r9)
            int r6 = r6.size()
            int r6 = r6 + -10
            if (r2 >= r6) goto L_0x00f6
        L_0x00da:
            if (r0 != 0) goto L_0x0116
            int r0 = r14.dialogsType
            if (r0 == r4) goto L_0x00e8
            int r14 = r14.dialogsType
            if (r14 != r3) goto L_0x0116
        L_0x00e8:
            int r14 = r13.currentAccount
            org.telegram.messenger.MessagesController r14 = org.telegram.messenger.MessagesController.getInstance(r14)
            int r0 = r13.folderId
            boolean r14 = r14.isDialogsEndReached(r0)
            if (r14 != 0) goto L_0x0116
        L_0x00f6:
            org.telegram.messenger.MessagesController r14 = r13.getMessagesController()
            int r0 = r13.folderId
            boolean r14 = r14.isDialogsEndReached(r0)
            r14 = r14 ^ r1
            if (r14 != 0) goto L_0x0113
            org.telegram.messenger.MessagesController r0 = r13.getMessagesController()
            int r2 = r13.folderId
            boolean r0 = r0.isServerDialogsEndReached(r2)
            if (r0 != 0) goto L_0x0110
            goto L_0x0113
        L_0x0110:
            r10 = r14
            r9 = 0
            goto L_0x0118
        L_0x0113:
            r10 = r14
            r9 = 1
            goto L_0x0118
        L_0x0116:
            r9 = 0
            r10 = 0
        L_0x0118:
            if (r9 != 0) goto L_0x011c
            if (r11 == 0) goto L_0x0126
        L_0x011c:
            org.telegram.ui.-$$Lambda$DialogsActivity$FCjTAtyjCMI2yWCw85XaHl1v0No r14 = new org.telegram.ui.-$$Lambda$DialogsActivity$FCjTAtyjCMI2yWCw85XaHl1v0No
            r7 = r14
            r8 = r13
            r7.<init>(r9, r10, r11, r12)
            org.telegram.messenger.AndroidUtilities.runOnUIThread(r14)
        L_0x0126:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.checkListLoad(org.telegram.ui.DialogsActivity$ViewPage):void");
    }

    public /* synthetic */ void lambda$checkListLoad$11$DialogsActivity(boolean z, boolean z2, boolean z3, boolean z4) {
        if (z) {
            getMessagesController().loadDialogs(this.folderId, -1, 100, z2);
        }
        if (z3) {
            getMessagesController().loadDialogs(1, -1, 100, z4);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:93:0x0174 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0175  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onItemClick(android.view.View r11, int r12, androidx.recyclerview.widget.RecyclerView.Adapter r13) {
        /*
            r10 = this;
            android.app.Activity r0 = r10.getParentActivity()
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            boolean r0 = r13 instanceof org.telegram.ui.Adapters.DialogsAdapter
            r1 = 32
            r2 = 0
            r4 = 1
            r5 = 0
            if (r0 == 0) goto L_0x00e5
            r0 = r13
            org.telegram.ui.Adapters.DialogsAdapter r0 = (org.telegram.ui.Adapters.DialogsAdapter) r0
            org.telegram.tgnet.TLObject r12 = r0.getItem(r12)
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$User
            if (r0 == 0) goto L_0x0023
            org.telegram.tgnet.TLRPC$User r12 = (org.telegram.tgnet.TLRPC$User) r12
            int r12 = r12.id
        L_0x0020:
            long r6 = (long) r12
            goto L_0x016e
        L_0x0023:
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$Dialog
            if (r0 == 0) goto L_0x005d
            org.telegram.tgnet.TLRPC$Dialog r12 = (org.telegram.tgnet.TLRPC$Dialog) r12
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$TL_dialogFolder
            if (r0 == 0) goto L_0x004f
            org.telegram.ui.ActionBar.ActionBar r11 = r10.actionBar
            boolean r11 = r11.isActionModeShowed()
            if (r11 == 0) goto L_0x0036
            return
        L_0x0036:
            org.telegram.tgnet.TLRPC$TL_dialogFolder r12 = (org.telegram.tgnet.TLRPC$TL_dialogFolder) r12
            android.os.Bundle r11 = new android.os.Bundle
            r11.<init>()
            org.telegram.tgnet.TLRPC$TL_folder r12 = r12.folder
            int r12 = r12.id
            java.lang.String r13 = "folderId"
            r11.putInt(r13, r12)
            org.telegram.ui.DialogsActivity r12 = new org.telegram.ui.DialogsActivity
            r12.<init>(r11)
            r10.presentFragment(r12)
            return
        L_0x004f:
            long r6 = r12.id
            org.telegram.ui.ActionBar.ActionBar r0 = r10.actionBar
            boolean r0 = r0.isActionModeShowed()
            if (r0 == 0) goto L_0x016e
            r10.showOrUpdateActionMode(r12, r11)
            return
        L_0x005d:
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$TL_recentMeUrlChat
            if (r0 == 0) goto L_0x0067
            org.telegram.tgnet.TLRPC$TL_recentMeUrlChat r12 = (org.telegram.tgnet.TLRPC$TL_recentMeUrlChat) r12
            int r12 = r12.chat_id
        L_0x0065:
            int r12 = -r12
            goto L_0x0020
        L_0x0067:
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$TL_recentMeUrlUser
            if (r0 == 0) goto L_0x0070
            org.telegram.tgnet.TLRPC$TL_recentMeUrlUser r12 = (org.telegram.tgnet.TLRPC$TL_recentMeUrlUser) r12
            int r12 = r12.user_id
            goto L_0x0020
        L_0x0070:
            boolean r0 = r12 instanceof org.telegram.tgnet.TLRPC$TL_recentMeUrlChatInvite
            if (r0 == 0) goto L_0x00b8
            org.telegram.tgnet.TLRPC$TL_recentMeUrlChatInvite r12 = (org.telegram.tgnet.TLRPC$TL_recentMeUrlChatInvite) r12
            org.telegram.tgnet.TLRPC$ChatInvite r0 = r12.chat_invite
            org.telegram.tgnet.TLRPC$Chat r6 = r0.chat
            if (r6 != 0) goto L_0x0084
            boolean r6 = r0.channel
            if (r6 == 0) goto L_0x0094
            boolean r6 = r0.megagroup
            if (r6 != 0) goto L_0x0094
        L_0x0084:
            org.telegram.tgnet.TLRPC$Chat r6 = r0.chat
            if (r6 == 0) goto L_0x00b0
            boolean r6 = org.telegram.messenger.ChatObject.isChannel(r6)
            if (r6 == 0) goto L_0x0094
            org.telegram.tgnet.TLRPC$Chat r6 = r0.chat
            boolean r6 = r6.megagroup
            if (r6 == 0) goto L_0x00b0
        L_0x0094:
            java.lang.String r11 = r12.url
            r12 = 47
            int r12 = r11.indexOf(r12)
            if (r12 <= 0) goto L_0x00a3
            int r12 = r12 + r4
            java.lang.String r11 = r11.substring(r12)
        L_0x00a3:
            org.telegram.ui.Components.JoinGroupAlert r12 = new org.telegram.ui.Components.JoinGroupAlert
            android.app.Activity r13 = r10.getParentActivity()
            r12.<init>(r13, r0, r11, r10)
            r10.showDialog(r12)
            return
        L_0x00b0:
            org.telegram.tgnet.TLRPC$Chat r12 = r0.chat
            if (r12 == 0) goto L_0x00b7
            int r12 = r12.id
            goto L_0x0065
        L_0x00b7:
            return
        L_0x00b8:
            boolean r11 = r12 instanceof org.telegram.tgnet.TLRPC$TL_recentMeUrlStickerSet
            if (r11 == 0) goto L_0x00e0
            org.telegram.tgnet.TLRPC$TL_recentMeUrlStickerSet r12 = (org.telegram.tgnet.TLRPC$TL_recentMeUrlStickerSet) r12
            org.telegram.tgnet.TLRPC$StickerSetCovered r11 = r12.set
            org.telegram.tgnet.TLRPC$StickerSet r11 = r11.set
            org.telegram.tgnet.TLRPC$TL_inputStickerSetID r3 = new org.telegram.tgnet.TLRPC$TL_inputStickerSetID
            r3.<init>()
            long r12 = r11.id
            r3.id = r12
            long r11 = r11.access_hash
            r3.access_hash = r11
            org.telegram.ui.Components.StickersAlert r11 = new org.telegram.ui.Components.StickersAlert
            android.app.Activity r1 = r10.getParentActivity()
            r4 = 0
            r5 = 0
            r0 = r11
            r2 = r10
            r0.<init>(r1, r2, r3, r4, r5)
            r10.showDialog(r11)
            return
        L_0x00e0:
            boolean r11 = r12 instanceof org.telegram.tgnet.TLRPC$TL_recentMeUrlUnknown
            if (r11 == 0) goto L_0x00e4
        L_0x00e4:
            return
        L_0x00e5:
            org.telegram.ui.Adapters.DialogsSearchAdapter r0 = r10.dialogsSearchAdapter
            if (r13 != r0) goto L_0x016d
            java.lang.Object r0 = r0.getItem(r12)
            org.telegram.ui.Adapters.DialogsSearchAdapter r6 = r10.dialogsSearchAdapter
            boolean r12 = r6.isGlobalSearch(r12)
            boolean r6 = r0 instanceof org.telegram.tgnet.TLRPC$User
            if (r6 == 0) goto L_0x0106
            org.telegram.tgnet.TLRPC$User r0 = (org.telegram.tgnet.TLRPC$User) r0
            int r6 = r0.id
            long r6 = (long) r6
            boolean r8 = r10.onlySelect
            if (r8 != 0) goto L_0x016f
            r10.searchDialogId = r6
            r10.searchObject = r0
            goto L_0x016f
        L_0x0106:
            boolean r6 = r0 instanceof org.telegram.tgnet.TLRPC$Chat
            if (r6 == 0) goto L_0x0119
            org.telegram.tgnet.TLRPC$Chat r0 = (org.telegram.tgnet.TLRPC$Chat) r0
            int r6 = r0.id
            int r6 = -r6
            long r6 = (long) r6
            boolean r8 = r10.onlySelect
            if (r8 != 0) goto L_0x016f
            r10.searchDialogId = r6
            r10.searchObject = r0
            goto L_0x016f
        L_0x0119:
            boolean r6 = r0 instanceof org.telegram.tgnet.TLRPC$EncryptedChat
            if (r6 == 0) goto L_0x012c
            org.telegram.tgnet.TLRPC$EncryptedChat r0 = (org.telegram.tgnet.TLRPC$EncryptedChat) r0
            int r6 = r0.id
            long r6 = (long) r6
            long r6 = r6 << r1
            boolean r8 = r10.onlySelect
            if (r8 != 0) goto L_0x016f
            r10.searchDialogId = r6
            r10.searchObject = r0
            goto L_0x016f
        L_0x012c:
            boolean r6 = r0 instanceof org.telegram.messenger.MessageObject
            if (r6 == 0) goto L_0x0144
            org.telegram.messenger.MessageObject r0 = (org.telegram.messenger.MessageObject) r0
            long r6 = r0.getDialogId()
            int r0 = r0.getId()
            org.telegram.ui.Adapters.DialogsSearchAdapter r8 = r10.dialogsSearchAdapter
            java.lang.String r9 = r8.getLastSearchString()
            r8.addHashtagsFromMessage(r9)
            goto L_0x0170
        L_0x0144:
            boolean r6 = r0 instanceof java.lang.String
            if (r6 == 0) goto L_0x016b
            java.lang.String r0 = (java.lang.String) r0
            org.telegram.ui.Adapters.DialogsSearchAdapter r6 = r10.dialogsSearchAdapter
            boolean r6 = r6.isHashtagSearch()
            if (r6 == 0) goto L_0x0158
            org.telegram.ui.ActionBar.ActionBar r6 = r10.actionBar
            r6.openSearchField(r0, r5)
            goto L_0x016b
        L_0x0158:
            java.lang.String r6 = "section"
            boolean r6 = r0.equals(r6)
            if (r6 != 0) goto L_0x016b
            org.telegram.ui.NewContactActivity r6 = new org.telegram.ui.NewContactActivity
            r6.<init>()
            r6.setInitialPhoneNumber(r0)
            r10.presentFragment(r6)
        L_0x016b:
            r6 = r2
            goto L_0x016f
        L_0x016d:
            r6 = r2
        L_0x016e:
            r12 = 0
        L_0x016f:
            r0 = 0
        L_0x0170:
            int r8 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r8 != 0) goto L_0x0175
            return
        L_0x0175:
            boolean r2 = r10.onlySelect
            if (r2 == 0) goto L_0x01a2
            boolean r12 = r10.validateSlowModeDialog(r6)
            if (r12 != 0) goto L_0x0180
            return
        L_0x0180:
            java.util.ArrayList<java.lang.Long> r12 = r10.selectedDialogs
            boolean r12 = r12.isEmpty()
            if (r12 != 0) goto L_0x019d
            boolean r11 = r10.addOrRemoveSelectedDialog(r6, r11)
            org.telegram.ui.Adapters.DialogsSearchAdapter r12 = r10.dialogsSearchAdapter
            if (r13 != r12) goto L_0x0198
            org.telegram.ui.ActionBar.ActionBar r12 = r10.actionBar
            r12.closeSearchField()
            r10.findAndUpdateCheckBox(r6, r11)
        L_0x0198:
            r10.updateSelectedCount()
            goto L_0x0260
        L_0x019d:
            r10.didSelectResult(r6, r4, r5)
            goto L_0x0260
        L_0x01a2:
            android.os.Bundle r11 = new android.os.Bundle
            r11.<init>()
            int r2 = (int) r6
            long r3 = r6 >> r1
            int r1 = (int) r3
            if (r2 == 0) goto L_0x01dd
            if (r2 <= 0) goto L_0x01b5
            java.lang.String r1 = "user_id"
            r11.putInt(r1, r2)
            goto L_0x01e2
        L_0x01b5:
            if (r2 >= 0) goto L_0x01e2
            if (r0 == 0) goto L_0x01d6
            org.telegram.messenger.MessagesController r1 = r10.getMessagesController()
            int r3 = -r2
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            org.telegram.tgnet.TLRPC$Chat r1 = r1.getChat(r3)
            if (r1 == 0) goto L_0x01d6
            org.telegram.tgnet.TLRPC$InputChannel r3 = r1.migrated_to
            if (r3 == 0) goto L_0x01d6
            java.lang.String r3 = "migrated_to"
            r11.putInt(r3, r2)
            org.telegram.tgnet.TLRPC$InputChannel r1 = r1.migrated_to
            int r1 = r1.channel_id
            int r2 = -r1
        L_0x01d6:
            int r1 = -r2
            java.lang.String r2 = "chat_id"
            r11.putInt(r2, r1)
            goto L_0x01e2
        L_0x01dd:
            java.lang.String r2 = "enc_id"
            r11.putInt(r2, r1)
        L_0x01e2:
            if (r0 == 0) goto L_0x01ea
            java.lang.String r12 = "message_id"
            r11.putInt(r12, r0)
            goto L_0x01fe
        L_0x01ea:
            if (r12 != 0) goto L_0x01f0
            r10.closeSearch()
            goto L_0x01fe
        L_0x01f0:
            org.telegram.tgnet.TLObject r12 = r10.searchObject
            if (r12 == 0) goto L_0x01fe
            org.telegram.ui.Adapters.DialogsSearchAdapter r0 = r10.dialogsSearchAdapter
            long r1 = r10.searchDialogId
            r0.putRecentSearch(r1, r12)
            r12 = 0
            r10.searchObject = r12
        L_0x01fe:
            boolean r12 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r12 == 0) goto L_0x022c
            long r0 = r10.openedDialogId
            int r12 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1))
            if (r12 != 0) goto L_0x020f
            org.telegram.ui.Adapters.DialogsSearchAdapter r12 = r10.dialogsSearchAdapter
            if (r13 == r12) goto L_0x020f
            return
        L_0x020f:
            org.telegram.ui.DialogsActivity$ViewPage[] r12 = r10.viewPages
            if (r12 == 0) goto L_0x0227
            r12 = 0
        L_0x0214:
            org.telegram.ui.DialogsActivity$ViewPage[] r13 = r10.viewPages
            int r0 = r13.length
            if (r12 >= r0) goto L_0x0227
            r13 = r13[r12]
            org.telegram.ui.Adapters.DialogsAdapter r13 = r13.dialogsAdapter
            r10.openedDialogId = r6
            r13.setOpenedDialogId(r6)
            int r12 = r12 + 1
            goto L_0x0214
        L_0x0227:
            r12 = 512(0x200, float:7.175E-43)
            r10.updateVisibleRows(r12)
        L_0x022c:
            java.lang.String r12 = r10.searchString
            if (r12 == 0) goto L_0x024e
            org.telegram.messenger.MessagesController r12 = r10.getMessagesController()
            boolean r12 = r12.checkCanOpenChat(r11, r10)
            if (r12 == 0) goto L_0x0260
            org.telegram.messenger.NotificationCenter r12 = r10.getNotificationCenter()
            int r13 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r12.postNotificationName(r13, r0)
            org.telegram.ui.ChatActivity r12 = new org.telegram.ui.ChatActivity
            r12.<init>(r11)
            r10.presentFragment(r12)
            goto L_0x0260
        L_0x024e:
            org.telegram.messenger.MessagesController r12 = r10.getMessagesController()
            boolean r12 = r12.checkCanOpenChat(r11, r10)
            if (r12 == 0) goto L_0x0260
            org.telegram.ui.ChatActivity r12 = new org.telegram.ui.ChatActivity
            r12.<init>(r11)
            r10.presentFragment(r12)
        L_0x0260:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.onItemClick(android.view.View, int, androidx.recyclerview.widget.RecyclerView$Adapter):void");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onItemLongClick(android.view.View r6, int r7, float r8, float r9, int r10, androidx.recyclerview.widget.RecyclerView.Adapter r11) {
        /*
            r5 = this;
            android.app.Activity r0 = r5.getParentActivity()
            r1 = 0
            if (r0 != 0) goto L_0x0008
            return r1
        L_0x0008:
            org.telegram.ui.ActionBar.ActionBar r0 = r5.actionBar
            boolean r0 = r0.isActionModeShowed()
            r2 = 1
            if (r0 != 0) goto L_0x00aa
            boolean r0 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r0 != 0) goto L_0x00aa
            boolean r0 = r5.onlySelect
            if (r0 != 0) goto L_0x00aa
            boolean r0 = r6 instanceof org.telegram.ui.Cells.DialogCell
            if (r0 == 0) goto L_0x00aa
            r0 = r6
            org.telegram.ui.Cells.DialogCell r0 = (org.telegram.ui.Cells.DialogCell) r0
            boolean r8 = r0.isPointInsideAvatar(r8, r9)
            if (r8 == 0) goto L_0x00aa
            long r6 = r0.getDialogId()
            android.os.Bundle r8 = new android.os.Bundle
            r8.<init>()
            int r7 = (int) r6
            int r6 = r0.getMessageId()
            if (r7 == 0) goto L_0x00a9
            if (r7 <= 0) goto L_0x0040
            java.lang.String r9 = "user_id"
            r8.putInt(r9, r7)
            goto L_0x0067
        L_0x0040:
            if (r7 >= 0) goto L_0x0067
            if (r6 == 0) goto L_0x0061
            org.telegram.messenger.MessagesController r9 = r5.getMessagesController()
            int r10 = -r7
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            org.telegram.tgnet.TLRPC$Chat r9 = r9.getChat(r10)
            if (r9 == 0) goto L_0x0061
            org.telegram.tgnet.TLRPC$InputChannel r10 = r9.migrated_to
            if (r10 == 0) goto L_0x0061
            java.lang.String r10 = "migrated_to"
            r8.putInt(r10, r7)
            org.telegram.tgnet.TLRPC$InputChannel r7 = r9.migrated_to
            int r7 = r7.channel_id
            int r7 = -r7
        L_0x0061:
            int r7 = -r7
            java.lang.String r9 = "chat_id"
            r8.putInt(r9, r7)
        L_0x0067:
            if (r6 == 0) goto L_0x006e
            java.lang.String r7 = "message_id"
            r8.putInt(r7, r6)
        L_0x006e:
            java.lang.String r6 = r5.searchString
            if (r6 == 0) goto L_0x0093
            org.telegram.messenger.MessagesController r6 = r5.getMessagesController()
            boolean r6 = r6.checkCanOpenChat(r8, r5)
            if (r6 == 0) goto L_0x00a8
            org.telegram.messenger.NotificationCenter r6 = r5.getNotificationCenter()
            int r7 = org.telegram.messenger.NotificationCenter.closeChats
            java.lang.Object[] r9 = new java.lang.Object[r1]
            r6.postNotificationName(r7, r9)
            r5.prepareBlurBitmap()
            org.telegram.ui.ChatActivity r6 = new org.telegram.ui.ChatActivity
            r6.<init>(r8)
            r5.presentFragmentAsPreview(r6)
            goto L_0x00a8
        L_0x0093:
            org.telegram.messenger.MessagesController r6 = r5.getMessagesController()
            boolean r6 = r6.checkCanOpenChat(r8, r5)
            if (r6 == 0) goto L_0x00a8
            r5.prepareBlurBitmap()
            org.telegram.ui.ChatActivity r6 = new org.telegram.ui.ChatActivity
            r6.<init>(r8)
            r5.presentFragmentAsPreview(r6)
        L_0x00a8:
            return r2
        L_0x00a9:
            return r1
        L_0x00aa:
            org.telegram.ui.Adapters.DialogsSearchAdapter r8 = r5.dialogsSearchAdapter
            r9 = 0
            if (r11 != r8) goto L_0x0196
            java.lang.Object r6 = r8.getItem(r7)
            org.telegram.ui.Adapters.DialogsSearchAdapter r7 = r5.dialogsSearchAdapter
            boolean r7 = r7.isRecentSearchDisplayed()
            if (r7 == 0) goto L_0x0195
            org.telegram.ui.ActionBar.AlertDialog$Builder r7 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r8 = r5.getParentActivity()
            r7.<init>((android.content.Context) r8)
            r8 = 2131624800(0x7f0e0360, float:1.887679E38)
            java.lang.String r10 = "ClearSearchSingleAlertTitle"
            java.lang.String r8 = org.telegram.messenger.LocaleController.getString(r10, r8)
            r7.setTitle(r8)
            boolean r8 = r6 instanceof org.telegram.tgnet.TLRPC$Chat
            r10 = 2131624801(0x7f0e0361, float:1.8876792E38)
            java.lang.String r11 = "ClearSearchSingleChatAlertText"
            if (r8 == 0) goto L_0x00ed
            org.telegram.tgnet.TLRPC$Chat r6 = (org.telegram.tgnet.TLRPC$Chat) r6
            java.lang.Object[] r8 = new java.lang.Object[r2]
            java.lang.String r0 = r6.title
            r8[r1] = r0
            java.lang.String r8 = org.telegram.messenger.LocaleController.formatString(r11, r10, r8)
            r7.setMessage(r8)
            int r6 = r6.id
            int r6 = -r6
        L_0x00eb:
            long r10 = (long) r6
            goto L_0x015a
        L_0x00ed:
            boolean r8 = r6 instanceof org.telegram.tgnet.TLRPC$User
            r0 = 2131624802(0x7f0e0362, float:1.8876794E38)
            java.lang.String r3 = "ClearSearchSingleUserAlertText"
            if (r8 == 0) goto L_0x012d
            org.telegram.tgnet.TLRPC$User r6 = (org.telegram.tgnet.TLRPC$User) r6
            int r8 = r6.id
            org.telegram.messenger.UserConfig r4 = r5.getUserConfig()
            int r4 = r4.clientUserId
            if (r8 != r4) goto L_0x0117
            java.lang.Object[] r8 = new java.lang.Object[r2]
            r0 = 2131626757(0x7f0e0b05, float:1.888076E38)
            java.lang.String r3 = "SavedMessages"
            java.lang.String r0 = org.telegram.messenger.LocaleController.getString(r3, r0)
            r8[r1] = r0
            java.lang.String r8 = org.telegram.messenger.LocaleController.formatString(r11, r10, r8)
            r7.setMessage(r8)
            goto L_0x012a
        L_0x0117:
            java.lang.Object[] r8 = new java.lang.Object[r2]
            java.lang.String r10 = r6.first_name
            java.lang.String r11 = r6.last_name
            java.lang.String r10 = org.telegram.messenger.ContactsController.formatName(r10, r11)
            r8[r1] = r10
            java.lang.String r8 = org.telegram.messenger.LocaleController.formatString(r3, r0, r8)
            r7.setMessage(r8)
        L_0x012a:
            int r6 = r6.id
            goto L_0x00eb
        L_0x012d:
            boolean r8 = r6 instanceof org.telegram.tgnet.TLRPC$EncryptedChat
            if (r8 == 0) goto L_0x0195
            org.telegram.tgnet.TLRPC$EncryptedChat r6 = (org.telegram.tgnet.TLRPC$EncryptedChat) r6
            org.telegram.messenger.MessagesController r8 = r5.getMessagesController()
            int r10 = r6.user_id
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            org.telegram.tgnet.TLRPC$User r8 = r8.getUser(r10)
            java.lang.Object[] r10 = new java.lang.Object[r2]
            java.lang.String r11 = r8.first_name
            java.lang.String r8 = r8.last_name
            java.lang.String r8 = org.telegram.messenger.ContactsController.formatName(r11, r8)
            r10[r1] = r8
            java.lang.String r8 = org.telegram.messenger.LocaleController.formatString(r3, r0, r10)
            r7.setMessage(r8)
            int r6 = r6.id
            long r10 = (long) r6
            r6 = 32
            long r10 = r10 << r6
        L_0x015a:
            r6 = 2131624799(0x7f0e035f, float:1.8876788E38)
            java.lang.String r8 = "ClearSearchRemove"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            java.lang.String r6 = r6.toUpperCase()
            org.telegram.ui.-$$Lambda$DialogsActivity$YqtGTk4qTK5zJgvYFXCnzbBO6Ns r8 = new org.telegram.ui.-$$Lambda$DialogsActivity$YqtGTk4qTK5zJgvYFXCnzbBO6Ns
            r8.<init>(r10)
            r7.setPositiveButton(r6, r8)
            r6 = 2131624540(0x7f0e025c, float:1.8876263E38)
            java.lang.String r8 = "Cancel"
            java.lang.String r6 = org.telegram.messenger.LocaleController.getString(r8, r6)
            r7.setNegativeButton(r6, r9)
            org.telegram.ui.ActionBar.AlertDialog r6 = r7.create()
            r5.showDialog(r6)
            r7 = -1
            android.view.View r6 = r6.getButton(r7)
            android.widget.TextView r6 = (android.widget.TextView) r6
            if (r6 == 0) goto L_0x0194
            java.lang.String r7 = "dialogTextRed2"
            int r7 = org.telegram.ui.ActionBar.Theme.getColor(r7)
            r6.setTextColor(r7)
        L_0x0194:
            return r2
        L_0x0195:
            return r1
        L_0x0196:
            org.telegram.ui.ActionBar.ActionBar r8 = r5.actionBar
            boolean r8 = r8.isSearchFieldVisible()
            if (r8 == 0) goto L_0x019f
            return r1
        L_0x019f:
            org.telegram.ui.Adapters.DialogsAdapter r11 = (org.telegram.ui.Adapters.DialogsAdapter) r11
            int r8 = r5.currentAccount
            int r0 = r5.folderId
            boolean r3 = r5.dialogsListFrozen
            java.util.ArrayList r8 = getDialogsArray(r8, r10, r0, r3)
            int r7 = r11.fixPosition(r7)
            if (r7 < 0) goto L_0x0256
            int r10 = r8.size()
            if (r7 < r10) goto L_0x01b9
            goto L_0x0256
        L_0x01b9:
            java.lang.Object r7 = r8.get(r7)
            org.telegram.tgnet.TLRPC$Dialog r7 = (org.telegram.tgnet.TLRPC$Dialog) r7
            boolean r8 = r5.onlySelect
            if (r8 == 0) goto L_0x01dc
            int r8 = r5.initialDialogsType
            r9 = 3
            if (r8 == r9) goto L_0x01c9
            return r1
        L_0x01c9:
            long r8 = r7.id
            boolean r8 = r5.validateSlowModeDialog(r8)
            if (r8 != 0) goto L_0x01d2
            return r1
        L_0x01d2:
            long r7 = r7.id
            r5.addOrRemoveSelectedDialog(r7, r6)
            r5.updateSelectedCount()
            goto L_0x0255
        L_0x01dc:
            boolean r8 = r7 instanceof org.telegram.tgnet.TLRPC$TL_dialogFolder
            if (r8 == 0) goto L_0x0243
            org.telegram.ui.ActionBar.BottomSheet$Builder r6 = new org.telegram.ui.ActionBar.BottomSheet$Builder
            android.app.Activity r7 = r5.getParentActivity()
            r6.<init>(r7)
            org.telegram.messenger.MessagesStorage r7 = r5.getMessagesStorage()
            int r7 = r7.getArchiveUnreadCount()
            if (r7 == 0) goto L_0x01f5
            r7 = 1
            goto L_0x01f6
        L_0x01f5:
            r7 = 0
        L_0x01f6:
            r8 = 2
            int[] r10 = new int[r8]
            if (r7 == 0) goto L_0x01ff
            r11 = 2131165628(0x7var_bc, float:1.7945478E38)
            goto L_0x0200
        L_0x01ff:
            r11 = 0
        L_0x0200:
            r10[r1] = r11
            boolean r11 = org.telegram.messenger.SharedConfig.archiveHidden
            if (r11 == 0) goto L_0x020a
            r11 = 2131165322(0x7var_a, float:1.7944858E38)
            goto L_0x020d
        L_0x020a:
            r11 = 2131165326(0x7var_e, float:1.7944866E38)
        L_0x020d:
            r10[r2] = r11
            java.lang.CharSequence[] r8 = new java.lang.CharSequence[r8]
            if (r7 == 0) goto L_0x021c
            r7 = 2131625742(0x7f0e070e, float:1.88787E38)
            java.lang.String r9 = "MarkAllAsRead"
            java.lang.String r9 = org.telegram.messenger.LocaleController.getString(r9, r7)
        L_0x021c:
            r8[r1] = r9
            boolean r7 = org.telegram.messenger.SharedConfig.archiveHidden
            if (r7 == 0) goto L_0x0228
            r7 = 2131626496(0x7f0e0a00, float:1.888023E38)
            java.lang.String r9 = "PinInTheList"
            goto L_0x022d
        L_0x0228:
            r7 = 2131625521(0x7f0e0631, float:1.8878252E38)
            java.lang.String r9 = "HideAboveTheList"
        L_0x022d:
            java.lang.String r7 = org.telegram.messenger.LocaleController.getString(r9, r7)
            r8[r2] = r7
            org.telegram.ui.-$$Lambda$DialogsActivity$jubw7sk_hMJmE4X5j0bMD98NPBM r7 = new org.telegram.ui.-$$Lambda$DialogsActivity$jubw7sk_hMJmE4X5j0bMD98NPBM
            r7.<init>()
            r6.setItems(r8, r10, r7)
            org.telegram.ui.ActionBar.BottomSheet r6 = r6.create()
            r5.showDialog(r6)
            return r1
        L_0x0243:
            org.telegram.ui.ActionBar.ActionBar r8 = r5.actionBar
            boolean r8 = r8.isActionModeShowed()
            if (r8 == 0) goto L_0x0252
            boolean r8 = r5.isDialogPinned(r7)
            if (r8 == 0) goto L_0x0252
            return r1
        L_0x0252:
            r5.showOrUpdateActionMode(r7, r6)
        L_0x0255:
            return r2
        L_0x0256:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.onItemLongClick(android.view.View, int, float, float, int, androidx.recyclerview.widget.RecyclerView$Adapter):boolean");
    }

    public /* synthetic */ void lambda$onItemLongClick$12$DialogsActivity(long j, DialogInterface dialogInterface, int i) {
        this.dialogsSearchAdapter.removeRecentSearch(j);
    }

    public /* synthetic */ void lambda$onItemLongClick$13$DialogsActivity(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            getMessagesStorage().readAllDialogs(1);
        } else if (i == 1 && this.viewPages != null) {
            int i2 = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i2 < viewPageArr.length) {
                    if (viewPageArr[i2].dialogsType == 0 && this.viewPages[i2].getVisibility() == 0) {
                        View childAt = this.viewPages[i2].listView.getChildAt(0);
                        DialogCell dialogCell = null;
                        if (childAt instanceof DialogCell) {
                            DialogCell dialogCell2 = (DialogCell) childAt;
                            if (dialogCell2.isFolderCell()) {
                                dialogCell = dialogCell2;
                            }
                        }
                        this.viewPages[i2].listView.toggleArchiveHidden(true, dialogCell);
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateFloatingButtonOffset() {
        this.floatingButtonContainer.setTranslationY(this.floatingButtonTranslation - (this.additionalFloatingTranslation * (1.0f - this.floatingButtonHideProgress)));
    }

    /* access modifiers changed from: private */
    public boolean hasHiddenArchive() {
        return !this.onlySelect && this.initialDialogsType == 0 && this.folderId == 0 && getMessagesController().hasHiddenArchive();
    }

    /* access modifiers changed from: private */
    public boolean waitingForDialogsAnimationEnd(ViewPage viewPage) {
        return (!viewPage.dialogsItemAnimator.isRunning() && this.dialogRemoveFinished == 0 && this.dialogInsertFinished == 0 && this.dialogChangeFinished == 0) ? false : true;
    }

    /* access modifiers changed from: private */
    public void onDialogAnimationFinished() {
        this.dialogRemoveFinished = 0;
        this.dialogInsertFinished = 0;
        this.dialogChangeFinished = 0;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public final void run() {
                DialogsActivity.this.lambda$onDialogAnimationFinished$14$DialogsActivity();
            }
        });
    }

    public /* synthetic */ void lambda$onDialogAnimationFinished$14$DialogsActivity() {
        ArrayList<TLRPC$Dialog> arrayList;
        if (!(this.viewPages == null || this.folderId == 0 || ((arrayList = frozenDialogsList) != null && !arrayList.isEmpty()))) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i].listView.setEmptyView((View) null);
                this.viewPages[i].progressView.setVisibility(4);
                i++;
            }
            finishFragment();
        }
        setDialogsListFrozen(false);
        updateDialogIndices();
    }

    /* access modifiers changed from: private */
    public void setScrollY(float f) {
        View view = this.scrimView;
        if (view != null) {
            view.getLocationInWindow(this.scrimViewLocation);
        }
        this.actionBar.setTranslationY(f);
        FilterTabsView filterTabsView2 = this.filterTabsView;
        if (filterTabsView2 != null) {
            filterTabsView2.setTranslationY(f);
        }
        FragmentContextView fragmentContextView2 = this.fragmentContextView;
        if (fragmentContextView2 != null) {
            fragmentContextView2.setTranslationY(((float) this.topPadding) + f);
        }
        FragmentContextView fragmentContextView3 = this.fragmentLocationContextView;
        if (fragmentContextView3 != null) {
            fragmentContextView3.setTranslationY(((float) this.topPadding) + f);
        }
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i].listView.setTopGlowOffset(this.viewPages[i].listView.getPaddingTop() + ((int) f));
                i++;
            }
        }
        this.fragmentView.invalidate();
    }

    private void prepareBlurBitmap() {
        if (this.blurredView != null) {
            int measuredWidth = (int) (((float) this.fragmentView.getMeasuredWidth()) / 6.0f);
            int measuredHeight = (int) (((float) this.fragmentView.getMeasuredHeight()) / 6.0f);
            Bitmap createBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.scale(0.16666667f, 0.16666667f);
            this.fragmentView.draw(canvas);
            Utilities.stackBlurBitmap(createBitmap, Math.max(7, Math.max(measuredWidth, measuredHeight) / 180));
            this.blurredView.setBackground(new BitmapDrawable(createBitmap));
            this.blurredView.setAlpha(0.0f);
            this.blurredView.setVisibility(0);
        }
    }

    /* access modifiers changed from: protected */
    public void onTransitionAnimationProgress(boolean z, float f) {
        View view = this.blurredView;
        if (view != null && view.getVisibility() == 0) {
            if (z) {
                this.blurredView.setAlpha(1.0f - f);
            } else {
                this.blurredView.setAlpha(f);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onTransitionAnimationEnd(boolean z, boolean z2) {
        View view;
        if (z && (view = this.blurredView) != null && view.getVisibility() == 0) {
            this.blurredView.setVisibility(8);
            this.blurredView.setBackground((Drawable) null);
        }
    }

    /* access modifiers changed from: private */
    public void resetScroll() {
        if (this.actionBar.getTranslationY() != 0.0f) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, this.SCROLL_Y, new float[]{0.0f})});
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.setDuration(180);
            animatorSet.start();
        }
    }

    /* access modifiers changed from: private */
    public void hideActionMode(boolean z) {
        this.actionBar.hideActionMode();
        if (this.menuDrawable != null) {
            this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", NUM));
        }
        this.selectedDialogs.clear();
        MenuDrawable menuDrawable2 = this.menuDrawable;
        if (menuDrawable2 != null) {
            menuDrawable2.setRotation(0.0f, true);
        } else {
            BackDrawable backDrawable2 = this.backDrawable;
            if (backDrawable2 != null) {
                backDrawable2.setRotation(0.0f, true);
            }
        }
        FilterTabsView filterTabsView2 = this.filterTabsView;
        if (filterTabsView2 != null) {
            filterTabsView2.animateColorsTo("actionBarTabLine", "actionBarTabActiveText", "actionBarTabUnactiveText", "actionBarTabSelector", "actionBarDefault");
        }
        int i = 0;
        this.allowMoving = false;
        if (!this.movingDialogFilters.isEmpty()) {
            int i2 = 0;
            for (int size = this.movingDialogFilters.size(); i2 < size; size = size) {
                MessagesController.DialogFilter dialogFilter = this.movingDialogFilters.get(i2);
                FilterCreateActivity.saveFilterToServer(dialogFilter, dialogFilter.flags, dialogFilter.name, dialogFilter.alwaysShow, dialogFilter.neverShow, dialogFilter.pinnedDialogs, false, false, true, true, false, this, (Runnable) null);
                i2++;
            }
            this.movingDialogFilters.clear();
        }
        if (this.movingWas) {
            getMessagesController().reorderPinnedDialogs(this.folderId, (ArrayList<TLRPC$InputDialogPeer>) null, 0);
            this.movingWas = false;
        }
        updateCounters(true);
        if (this.viewPages != null) {
            int i3 = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i3 >= viewPageArr.length) {
                    break;
                }
                viewPageArr[i3].dialogsAdapter.onReorderStateChanged(false);
                i3++;
            }
        }
        if (z) {
            i = 8192;
        }
        updateVisibleRows(196608 | i);
    }

    private int getPinnedCount() {
        ArrayList<TLRPC$Dialog> arrayList;
        if (this.viewPages[0].dialogsType == 7 || this.viewPages[0].dialogsType == 8) {
            arrayList = getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, this.dialogsListFrozen);
        } else {
            arrayList = getMessagesController().getDialogs(this.folderId);
        }
        int size = arrayList.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            TLRPC$Dialog tLRPC$Dialog = arrayList.get(i2);
            if (!(tLRPC$Dialog instanceof TLRPC$TL_dialogFolder)) {
                long j = tLRPC$Dialog.id;
                if (isDialogPinned(tLRPC$Dialog)) {
                    i++;
                } else if (!getMessagesController().isPromoDialog(tLRPC$Dialog.id, false)) {
                    break;
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: private */
    public boolean isDialogPinned(TLRPC$Dialog tLRPC$Dialog) {
        MessagesController.DialogFilter dialogFilter;
        if (this.viewPages[0].dialogsType == 7 || this.viewPages[0].dialogsType == 8) {
            dialogFilter = getMessagesController().selectedDialogFilter[this.viewPages[0].dialogsType == 8 ? (char) 1 : 0];
        } else {
            dialogFilter = null;
        }
        if (dialogFilter == null) {
            return tLRPC$Dialog.pinned;
        }
        if (dialogFilter.pinnedDialogs.indexOfKey(tLRPC$Dialog.id) >= 0) {
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v5, resolved type: org.telegram.tgnet.TLRPC$User} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v21, resolved type: org.telegram.tgnet.TLRPC$EncryptedChat} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v14, resolved type: org.telegram.tgnet.TLRPC$User} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v20, resolved type: org.telegram.tgnet.TLRPC$User} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v22, resolved type: org.telegram.tgnet.TLRPC$User} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v38, resolved type: org.telegram.tgnet.TLRPC$Chat} */
    /* JADX WARNING: type inference failed for: r10v6, types: [org.telegram.tgnet.TLRPC$Chat] */
    /* JADX WARNING: type inference failed for: r3v7, types: [org.telegram.tgnet.TLRPC$EncryptedChat] */
    /* JADX WARNING: type inference failed for: r3v21 */
    /* JADX WARNING: type inference failed for: r3v22 */
    /* JADX WARNING: type inference failed for: r10v22 */
    /* JADX WARNING: type inference failed for: r10v27 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void perfromSelectedDialogsAction(int r32, boolean r33) {
        /*
            r31 = this;
            r13 = r31
            r14 = r32
            android.app.Activity r0 = r31.getParentActivity()
            if (r0 != 0) goto L_0x000b
            return
        L_0x000b:
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r13.viewPages
            r15 = 0
            r0 = r0[r15]
            int r0 = r0.dialogsType
            r1 = 7
            r2 = 8
            r3 = 0
            if (r0 == r1) goto L_0x0027
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r13.viewPages
            r0 = r0[r15]
            int r0 = r0.dialogsType
            if (r0 != r2) goto L_0x0025
            goto L_0x0027
        L_0x0025:
            r0 = r3
            goto L_0x003c
        L_0x0027:
            org.telegram.messenger.MessagesController r0 = r31.getMessagesController()
            org.telegram.messenger.MessagesController$DialogFilter[] r0 = r0.selectedDialogFilter
            org.telegram.ui.DialogsActivity$ViewPage[] r4 = r13.viewPages
            r4 = r4[r15]
            int r4 = r4.dialogsType
            if (r4 != r2) goto L_0x0039
            r4 = 1
            goto L_0x003a
        L_0x0039:
            r4 = 0
        L_0x003a:
            r0 = r0[r4]
        L_0x003c:
            java.util.ArrayList<java.lang.Long> r4 = r13.selectedDialogs
            int r4 = r4.size()
            r5 = 105(0x69, float:1.47E-43)
            if (r14 == r5) goto L_0x0639
            r5 = 107(0x6b, float:1.5E-43)
            if (r14 != r5) goto L_0x004c
            goto L_0x0639
        L_0x004c:
            java.lang.String r9 = "Cancel"
            r6 = 108(0x6c, float:1.51E-43)
            r7 = 100
            if (r14 == r7) goto L_0x0056
            if (r14 != r6) goto L_0x01ac
        L_0x0056:
            int r8 = r13.canPinCount
            if (r8 == 0) goto L_0x01ac
            org.telegram.messenger.MessagesController r8 = r31.getMessagesController()
            int r6 = r13.folderId
            java.util.ArrayList r6 = r8.getDialogs(r6)
            int r8 = r6.size()
            r5 = 0
            r18 = 0
            r19 = 0
        L_0x006d:
            if (r5 >= r8) goto L_0x00a5
            java.lang.Object r20 = r6.get(r5)
            r10 = r20
            org.telegram.tgnet.TLRPC$Dialog r10 = (org.telegram.tgnet.TLRPC$Dialog) r10
            boolean r11 = r10 instanceof org.telegram.tgnet.TLRPC$TL_dialogFolder
            if (r11 == 0) goto L_0x007e
            r33 = r8
            goto L_0x009e
        L_0x007e:
            r33 = r8
            long r7 = r10.id
            int r8 = (int) r7
            boolean r7 = r13.isDialogPinned(r10)
            if (r7 == 0) goto L_0x0091
            if (r8 != 0) goto L_0x008e
            int r19 = r19 + 1
            goto L_0x009e
        L_0x008e:
            int r18 = r18 + 1
            goto L_0x009e
        L_0x0091:
            org.telegram.messenger.MessagesController r7 = r31.getMessagesController()
            long r11 = r10.id
            boolean r7 = r7.isPromoDialog(r11, r15)
            if (r7 != 0) goto L_0x009e
            goto L_0x00a5
        L_0x009e:
            int r5 = r5 + 1
            r8 = r33
            r7 = 100
            goto L_0x006d
        L_0x00a5:
            r5 = 0
            r6 = 0
            r7 = 0
            r10 = 0
        L_0x00a9:
            if (r5 >= r4) goto L_0x00e7
            java.util.ArrayList<java.lang.Long> r11 = r13.selectedDialogs
            java.lang.Object r11 = r11.get(r5)
            java.lang.Long r11 = (java.lang.Long) r11
            long r11 = r11.longValue()
            org.telegram.messenger.MessagesController r8 = r31.getMessagesController()
            android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r8 = r8.dialogs_dict
            java.lang.Object r8 = r8.get(r11)
            org.telegram.tgnet.TLRPC$Dialog r8 = (org.telegram.tgnet.TLRPC$Dialog) r8
            if (r8 == 0) goto L_0x00e4
            boolean r8 = r13.isDialogPinned(r8)
            if (r8 == 0) goto L_0x00cc
            goto L_0x00e4
        L_0x00cc:
            int r8 = (int) r11
            if (r8 != 0) goto L_0x00d2
            int r7 = r7 + 1
            goto L_0x00d4
        L_0x00d2:
            int r6 = r6 + 1
        L_0x00d4:
            if (r0 == 0) goto L_0x00e4
            java.util.ArrayList<java.lang.Integer> r11 = r0.alwaysShow
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            boolean r8 = r11.contains(r8)
            if (r8 == 0) goto L_0x00e4
            int r10 = r10 + 1
        L_0x00e4:
            int r5 = r5 + 1
            goto L_0x00a9
        L_0x00e7:
            org.telegram.ui.DialogsActivity$ViewPage[] r5 = r13.viewPages
            r5 = r5[r15]
            int r5 = r5.dialogsType
            if (r5 == r1) goto L_0x0111
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r13.viewPages
            r1 = r1[r15]
            int r1 = r1.dialogsType
            if (r1 != r2) goto L_0x00fc
            goto L_0x0111
        L_0x00fc:
            int r1 = r13.folderId
            if (r1 != 0) goto L_0x010a
            if (r0 == 0) goto L_0x0103
            goto L_0x010a
        L_0x0103:
            org.telegram.messenger.MessagesController r1 = r31.getMessagesController()
            int r1 = r1.maxPinnedDialogsCount
            goto L_0x011b
        L_0x010a:
            org.telegram.messenger.MessagesController r1 = r31.getMessagesController()
            int r1 = r1.maxFolderPinnedDialogsCount
            goto L_0x011b
        L_0x0111:
            java.util.ArrayList<java.lang.Integer> r1 = r0.alwaysShow
            int r1 = r1.size()
            r2 = 100
            int r1 = 100 - r1
        L_0x011b:
            int r7 = r7 + r19
            if (r7 > r1) goto L_0x0124
            int r6 = r6 + r18
            int r6 = r6 - r10
            if (r6 <= r1) goto L_0x02ba
        L_0x0124:
            int r2 = r13.folderId
            java.lang.String r4 = "Chats"
            if (r2 != 0) goto L_0x017c
            if (r0 == 0) goto L_0x012d
            goto L_0x017c
        L_0x012d:
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r2 = r31.getParentActivity()
            r0.<init>((android.content.Context) r2)
            r2 = 2131624238(0x7f0e012e, float:1.887565E38)
            java.lang.String r5 = "AppName"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r5, r2)
            r0.setTitle(r2)
            r2 = 2131626504(0x7f0e0a08, float:1.8880246E38)
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatPluralString(r4, r1)
            r5[r15] = r1
            java.lang.String r1 = "PinToTopLimitReached2"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r1, r2, r5)
            r0.setMessage(r1)
            r1 = 2131625356(0x7f0e058c, float:1.8877918E38)
            java.lang.String r2 = "FiltersSetupPinAlert"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$hpmOtiQ2CIXMPd3GUTyx2K3euSc r2 = new org.telegram.ui.-$$Lambda$DialogsActivity$hpmOtiQ2CIXMPd3GUTyx2K3euSc
            r2.<init>()
            r0.setNegativeButton(r1, r2)
            r1 = 2131626121(0x7f0e0889, float:1.887947E38)
            java.lang.String r2 = "OK"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setPositiveButton(r1, r3)
            org.telegram.ui.ActionBar.AlertDialog r0 = r0.create()
            r13.showDialog(r0)
            goto L_0x0191
        L_0x017c:
            r0 = 2131626495(0x7f0e09ff, float:1.8880228E38)
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatPluralString(r4, r1)
            r2[r15] = r1
            java.lang.String r1 = "PinFolderLimitReached"
            java.lang.String r0 = org.telegram.messenger.LocaleController.formatString(r1, r0, r2)
            org.telegram.ui.Components.AlertsCreator.showSimpleAlert(r13, r0)
        L_0x0191:
            org.telegram.ui.ActionBar.ActionBarMenuItem r0 = r13.pinItem
            r1 = 1073741824(0x40000000, float:2.0)
            org.telegram.messenger.AndroidUtilities.shakeView(r0, r1, r15)
            android.app.Activity r0 = r31.getParentActivity()
            java.lang.String r1 = "vibrator"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.os.Vibrator r0 = (android.os.Vibrator) r0
            if (r0 == 0) goto L_0x01ab
            r1 = 200(0xc8, double:9.9E-322)
            r0.vibrate(r1)
        L_0x01ab:
            return
        L_0x01ac:
            r1 = 102(0x66, float:1.43E-43)
            if (r14 == r1) goto L_0x01b4
            r2 = 103(0x67, float:1.44E-43)
            if (r14 != r2) goto L_0x028a
        L_0x01b4:
            r2 = 1
            if (r4 <= r2) goto L_0x028a
            if (r33 == 0) goto L_0x028a
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r5 = r31.getParentActivity()
            r0.<init>((android.content.Context) r5)
            if (r14 != r1) goto L_0x01fb
            r1 = 2131624966(0x7f0e0406, float:1.8877127E38)
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r5 = "ChatsSelected"
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatPluralString(r5, r4)
            r2[r15] = r4
            java.lang.String r4 = "DeleteFewChatsTitle"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r4, r1, r2)
            r0.setTitle(r1)
            r1 = 2131624289(0x7f0e0161, float:1.8875754E38)
            java.lang.String r2 = "AreYouSureDeleteFewChats"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setMessage(r1)
            r1 = 2131624943(0x7f0e03ef, float:1.887708E38)
            java.lang.String r2 = "Delete"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$XSXPShQxscefLopYKCLASSNAMEkxbrNGg r2 = new org.telegram.ui.-$$Lambda$DialogsActivity$XSXPShQxscefLopYKCLASSNAMEkxbrNGg
            r2.<init>(r14)
            r0.setPositiveButton(r1, r2)
        L_0x01f7:
            r1 = 2131624540(0x7f0e025c, float:1.8876263E38)
            goto L_0x0269
        L_0x01fb:
            int r1 = r13.canClearCacheCount
            if (r1 == 0) goto L_0x0234
            r1 = 2131624784(0x7f0e0350, float:1.8876757E38)
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r5 = "ChatsSelectedClearCache"
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatPluralString(r5, r4)
            r2[r15] = r4
            java.lang.String r4 = "ClearCacheFewChatsTitle"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r4, r1, r2)
            r0.setTitle(r1)
            r1 = 2131624278(0x7f0e0156, float:1.8875731E38)
            java.lang.String r2 = "AreYouSureClearHistoryCacheFewChats"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setMessage(r1)
            r1 = 2131624788(0x7f0e0354, float:1.8876766E38)
            java.lang.String r2 = "ClearHistoryCache"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$YXXSWcMZ-WrhiGrbkxEtiwcuKYA r2 = new org.telegram.ui.-$$Lambda$DialogsActivity$YXXSWcMZ-WrhiGrbkxEtiwcuKYA
            r2.<init>(r14)
            r0.setPositiveButton(r1, r2)
            goto L_0x01f7
        L_0x0234:
            r1 = 2131624786(0x7f0e0352, float:1.8876762E38)
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.String r5 = "ChatsSelectedClear"
            java.lang.String r4 = org.telegram.messenger.LocaleController.formatPluralString(r5, r4)
            r2[r15] = r4
            java.lang.String r4 = "ClearFewChatsTitle"
            java.lang.String r1 = org.telegram.messenger.LocaleController.formatString(r4, r1, r2)
            r0.setTitle(r1)
            r1 = 2131624280(0x7f0e0158, float:1.8875735E38)
            java.lang.String r2 = "AreYouSureClearHistoryFewChats"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setMessage(r1)
            r1 = 2131624787(0x7f0e0353, float:1.8876764E38)
            java.lang.String r2 = "ClearHistory"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$J1qA9A62EhbTLWLQ-xS3gpilIJ4 r2 = new org.telegram.ui.-$$Lambda$DialogsActivity$J1qA9A62EhbTLWLQ-xS3gpilIJ4
            r2.<init>(r14)
            r0.setPositiveButton(r1, r2)
            goto L_0x01f7
        L_0x0269:
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r9, r1)
            r0.setNegativeButton(r1, r3)
            org.telegram.ui.ActionBar.AlertDialog r0 = r0.create()
            r13.showDialog(r0)
            r1 = -1
            android.view.View r0 = r0.getButton(r1)
            android.widget.TextView r0 = (android.widget.TextView) r0
            if (r0 == 0) goto L_0x0289
            java.lang.String r1 = "dialogTextRed2"
            int r1 = org.telegram.ui.ActionBar.Theme.getColor(r1)
            r0.setTextColor(r1)
        L_0x0289:
            return
        L_0x028a:
            r1 = 106(0x6a, float:1.49E-43)
            if (r14 != r1) goto L_0x02ba
            if (r33 == 0) goto L_0x02ba
            r1 = 1
            if (r4 != r1) goto L_0x02ac
            java.util.ArrayList<java.lang.Long> r0 = r13.selectedDialogs
            java.lang.Object r0 = r0.get(r15)
            java.lang.Long r0 = (java.lang.Long) r0
            long r0 = r0.longValue()
            org.telegram.messenger.MessagesController r2 = r31.getMessagesController()
            int r1 = (int) r0
            java.lang.Integer r0 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r3 = r2.getUser(r0)
        L_0x02ac:
            int r0 = r13.canReportSpamCount
            if (r0 == 0) goto L_0x02b1
            r15 = 1
        L_0x02b1:
            org.telegram.ui.-$$Lambda$DialogsActivity$pqjQMn2IPjinPy4fWwywtr-KAM4 r0 = new org.telegram.ui.-$$Lambda$DialogsActivity$pqjQMn2IPjinPy4fWwywtr-KAM4
            r0.<init>()
            org.telegram.ui.Components.AlertsCreator.createBlockDialogAlert(r13, r4, r15, r3, r0)
            return
        L_0x02ba:
            r1 = 2147483647(0x7fffffff, float:NaN)
            if (r0 == 0) goto L_0x02ea
            r2 = 100
            if (r14 == r2) goto L_0x02c7
            r2 = 108(0x6c, float:1.51E-43)
            if (r14 != r2) goto L_0x02ea
        L_0x02c7:
            int r2 = r13.canPinCount
            if (r2 == 0) goto L_0x02ea
            android.util.LongSparseArray<java.lang.Integer> r2 = r0.pinnedDialogs
            int r2 = r2.size()
            r5 = 0
        L_0x02d2:
            if (r5 >= r2) goto L_0x02e7
            android.util.LongSparseArray<java.lang.Integer> r6 = r0.pinnedDialogs
            java.lang.Object r6 = r6.valueAt(r5)
            java.lang.Integer r6 = (java.lang.Integer) r6
            int r6 = r6.intValue()
            int r1 = java.lang.Math.min(r1, r6)
            int r5 = r5 + 1
            goto L_0x02d2
        L_0x02e7:
            int r2 = r13.canPinCount
            int r1 = r1 - r2
        L_0x02ea:
            r2 = 0
            r18 = 0
        L_0x02ed:
            if (r2 >= r4) goto L_0x05d6
            java.util.ArrayList<java.lang.Long> r5 = r13.selectedDialogs
            java.lang.Object r5 = r5.get(r2)
            java.lang.Long r5 = (java.lang.Long) r5
            long r5 = r5.longValue()
            org.telegram.messenger.MessagesController r7 = r31.getMessagesController()
            android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r7 = r7.dialogs_dict
            java.lang.Object r7 = r7.get(r5)
            org.telegram.tgnet.TLRPC$Dialog r7 = (org.telegram.tgnet.TLRPC$Dialog) r7
            if (r7 != 0) goto L_0x0315
        L_0x0309:
            r10 = r4
            r11 = 3
        L_0x030b:
            r12 = 2131624540(0x7f0e025c, float:1.8876263E38)
        L_0x030e:
            r15 = 1
            r16 = 2
        L_0x0311:
            r17 = 102(0x66, float:1.43E-43)
            goto L_0x05cf
        L_0x0315:
            int r8 = (int) r5
            r10 = 32
            long r11 = r5 >> r10
            int r10 = (int) r11
            if (r8 == 0) goto L_0x033f
            if (r8 <= 0) goto L_0x032f
            org.telegram.messenger.MessagesController r10 = r31.getMessagesController()
            java.lang.Integer r11 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$User r10 = r10.getUser(r11)
            r11 = r3
            r12 = r10
            r10 = r11
            goto L_0x0364
        L_0x032f:
            org.telegram.messenger.MessagesController r10 = r31.getMessagesController()
            int r11 = -r8
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)
            org.telegram.tgnet.TLRPC$Chat r10 = r10.getChat(r11)
            r11 = r3
            r12 = r11
            goto L_0x0364
        L_0x033f:
            org.telegram.messenger.MessagesController r11 = r31.getMessagesController()
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            org.telegram.tgnet.TLRPC$EncryptedChat r10 = r11.getEncryptedChat(r10)
            if (r10 == 0) goto L_0x035c
            org.telegram.messenger.MessagesController r11 = r31.getMessagesController()
            int r12 = r10.user_id
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)
            org.telegram.tgnet.TLRPC$User r11 = r11.getUser(r12)
            goto L_0x0361
        L_0x035c:
            org.telegram.tgnet.TLRPC$TL_userEmpty r11 = new org.telegram.tgnet.TLRPC$TL_userEmpty
            r11.<init>()
        L_0x0361:
            r12 = r11
            r11 = r10
            r10 = r3
        L_0x0364:
            if (r10 != 0) goto L_0x0369
            if (r12 != 0) goto L_0x0369
            goto L_0x0309
        L_0x0369:
            if (r12 == 0) goto L_0x0379
            boolean r15 = r12.bot
            if (r15 == 0) goto L_0x0379
            boolean r15 = org.telegram.messenger.MessagesController.isSupportUser(r12)
            if (r15 != 0) goto L_0x0379
            r3 = 100
            r15 = 1
            goto L_0x037c
        L_0x0379:
            r3 = 100
            r15 = 0
        L_0x037c:
            if (r14 == r3) goto L_0x052e
            r3 = r11
            r11 = 108(0x6c, float:1.51E-43)
            if (r14 != r11) goto L_0x0386
            r10 = r4
            goto L_0x0530
        L_0x0386:
            r3 = 101(0x65, float:1.42E-43)
            if (r14 != r3) goto L_0x03c1
            int r3 = r13.canReadCount
            if (r3 == 0) goto L_0x03b2
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            r3.markMentionsAsRead(r5)
            org.telegram.messenger.MessagesController r21 = r31.getMessagesController()
            int r3 = r7.top_message
            int r7 = r7.last_message_date
            r27 = 0
            r28 = 0
            r29 = 1
            r30 = 0
            r22 = r5
            r24 = r3
            r25 = r3
            r26 = r7
            r21.markDialogAsRead(r22, r24, r25, r26, r27, r28, r29, r30)
            goto L_0x0309
        L_0x03b2:
            org.telegram.messenger.MessagesController r21 = r31.getMessagesController()
            r24 = 0
            r25 = 0
            r22 = r5
            r21.markDialogAsUnread(r22, r24, r25)
            goto L_0x0309
        L_0x03c1:
            r3 = 102(0x66, float:1.43E-43)
            if (r14 == r3) goto L_0x041b
            r3 = 103(0x67, float:1.44E-43)
            if (r14 != r3) goto L_0x03cc
            r3 = 1
            r11 = 3
            goto L_0x041d
        L_0x03cc:
            r3 = 104(0x68, float:1.46E-43)
            if (r14 != r3) goto L_0x0417
            r3 = 1
            if (r4 != r3) goto L_0x03e8
            int r7 = r13.canMuteCount
            if (r7 != r3) goto L_0x03e8
            android.app.Activity r0 = r31.getParentActivity()
            org.telegram.ui.ActionBar.BottomSheet r0 = org.telegram.ui.Components.AlertsCreator.createMuteAlert(r0, r5)
            org.telegram.ui.-$$Lambda$DialogsActivity$JxH6FFKdSJdQV-IUsmGs4tEf_nw r1 = new org.telegram.ui.-$$Lambda$DialogsActivity$JxH6FFKdSJdQV-IUsmGs4tEf_nw
            r1.<init>()
            r13.showDialog(r0, r1)
            return
        L_0x03e8:
            int r3 = r13.canUnmuteCount
            if (r3 == 0) goto L_0x0402
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            boolean r3 = r3.isDialogMuted(r5)
            if (r3 != 0) goto L_0x03f8
            goto L_0x0309
        L_0x03f8:
            org.telegram.messenger.NotificationsController r3 = r31.getNotificationsController()
            r7 = 4
            r3.setDialogNotificationsSettings(r5, r7)
            goto L_0x0309
        L_0x0402:
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            boolean r3 = r3.isDialogMuted(r5)
            if (r3 == 0) goto L_0x040e
            goto L_0x0309
        L_0x040e:
            org.telegram.messenger.NotificationsController r3 = r31.getNotificationsController()
            r11 = 3
            r3.setDialogNotificationsSettings(r5, r11)
            goto L_0x0418
        L_0x0417:
            r11 = 3
        L_0x0418:
            r10 = r4
            goto L_0x030b
        L_0x041b:
            r11 = 3
            r3 = 1
        L_0x041d:
            if (r4 != r3) goto L_0x0493
            r3 = 102(0x66, float:1.43E-43)
            if (r14 != r3) goto L_0x046c
            boolean r0 = r13.canDeletePsaSelected
            if (r0 == 0) goto L_0x046c
            org.telegram.ui.ActionBar.AlertDialog$Builder r0 = new org.telegram.ui.ActionBar.AlertDialog$Builder
            android.app.Activity r1 = r31.getParentActivity()
            r0.<init>((android.content.Context) r1)
            r1 = 2131626596(0x7f0e0a64, float:1.8880433E38)
            java.lang.String r2 = "PsaHideChatAlertTitle"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setTitle(r1)
            r1 = 2131626595(0x7f0e0a63, float:1.888043E38)
            java.lang.String r2 = "PsaHideChatAlertText"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            r0.setMessage(r1)
            r1 = 2131626594(0x7f0e0a62, float:1.8880429E38)
            java.lang.String r2 = "PsaHide"
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r2, r1)
            org.telegram.ui.-$$Lambda$DialogsActivity$MizUGtuILJ-xVyFuYUcviouhWIk r2 = new org.telegram.ui.-$$Lambda$DialogsActivity$MizUGtuILJ-xVyFuYUcviouhWIk
            r2.<init>()
            r0.setPositiveButton(r1, r2)
            r12 = 2131624540(0x7f0e025c, float:1.8876263E38)
            java.lang.String r1 = org.telegram.messenger.LocaleController.getString(r9, r12)
            r2 = 0
            r0.setNegativeButton(r1, r2)
            org.telegram.ui.ActionBar.AlertDialog r0 = r0.create()
            r13.showDialog(r0)
            goto L_0x0492
        L_0x046c:
            r0 = 103(0x67, float:1.44E-43)
            if (r14 != r0) goto L_0x0472
            r7 = 1
            goto L_0x0473
        L_0x0472:
            r7 = 0
        L_0x0473:
            if (r8 != 0) goto L_0x0478
            r19 = 1
            goto L_0x047a
        L_0x0478:
            r19 = 0
        L_0x047a:
            org.telegram.ui.-$$Lambda$DialogsActivity$V0mmRXs8SEoSTUDk0lzRpd10Vco r8 = new org.telegram.ui.-$$Lambda$DialogsActivity$V0mmRXs8SEoSTUDk0lzRpd10Vco
            r0 = r8
            r1 = r31
            r2 = r32
            r3 = r10
            r4 = r5
            r6 = r15
            r0.<init>(r2, r3, r4, r6)
            r0 = r31
            r1 = r7
            r2 = r10
            r3 = r12
            r4 = r19
            r5 = r8
            org.telegram.ui.Components.AlertsCreator.createClearOrDeleteDialogAlert(r0, r1, r2, r3, r4, r5)
        L_0x0492:
            return
        L_0x0493:
            r3 = 102(0x66, float:1.43E-43)
            r12 = 2131624540(0x7f0e025c, float:1.8876263E38)
            org.telegram.messenger.MessagesController r7 = r31.getMessagesController()
            r3 = 1
            boolean r7 = r7.isPromoDialog(r5, r3)
            if (r7 == 0) goto L_0x04ad
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            r3.hidePromoDialog()
        L_0x04aa:
            r10 = r4
            goto L_0x030e
        L_0x04ad:
            r3 = 103(0x67, float:1.44E-43)
            if (r14 != r3) goto L_0x04bf
            int r7 = r13.canClearCacheCount
            if (r7 == 0) goto L_0x04bf
            org.telegram.messenger.MessagesController r7 = r31.getMessagesController()
            r8 = 2
            r10 = 0
            r7.deleteDialog(r5, r8, r10)
            goto L_0x04aa
        L_0x04bf:
            r7 = 0
            r16 = 2
            if (r14 != r3) goto L_0x04d0
            org.telegram.messenger.MessagesController r8 = r31.getMessagesController()
            r10 = 1
            r8.deleteDialog(r5, r10, r7)
            r10 = r4
        L_0x04cd:
            r15 = 1
            goto L_0x0311
        L_0x04d0:
            if (r10 == 0) goto L_0x0501
            boolean r8 = org.telegram.messenger.ChatObject.isNotInChat(r10)
            if (r8 == 0) goto L_0x04e1
            org.telegram.messenger.MessagesController r8 = r31.getMessagesController()
            r8.deleteDialog(r5, r7, r7)
            r10 = r4
            goto L_0x0513
        L_0x04e1:
            org.telegram.messenger.MessagesController r7 = r31.getMessagesController()
            org.telegram.messenger.UserConfig r8 = r31.getUserConfig()
            int r8 = r8.getClientUserId()
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)
            org.telegram.tgnet.TLRPC$User r7 = r7.getUser(r8)
            org.telegram.messenger.MessagesController r8 = r31.getMessagesController()
            r10 = r4
            long r3 = -r5
            int r4 = (int) r3
            r3 = 0
            r8.deleteUserFromChat(r4, r7, r3)
            goto L_0x0513
        L_0x0501:
            r10 = r4
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            r4 = 0
            r3.deleteDialog(r5, r4, r4)
            if (r15 == 0) goto L_0x0513
            org.telegram.messenger.MessagesController r3 = r31.getMessagesController()
            r3.blockUser(r8)
        L_0x0513:
            boolean r3 = org.telegram.messenger.AndroidUtilities.isTablet()
            if (r3 == 0) goto L_0x04cd
            org.telegram.messenger.NotificationCenter r3 = r31.getNotificationCenter()
            int r4 = org.telegram.messenger.NotificationCenter.closeChats
            r15 = 1
            java.lang.Object[] r7 = new java.lang.Object[r15]
            java.lang.Long r5 = java.lang.Long.valueOf(r5)
            r6 = 0
            r7[r6] = r5
            r3.postNotificationName(r4, r7)
            goto L_0x0311
        L_0x052e:
            r10 = r4
            r3 = r11
        L_0x0530:
            r11 = 3
            r12 = 2131624540(0x7f0e025c, float:1.8876263E38)
            r15 = 1
            r16 = 2
            r17 = 102(0x66, float:1.43E-43)
            int r4 = r13.canPinCount
            if (r4 == 0) goto L_0x0597
            boolean r4 = r13.isDialogPinned(r7)
            if (r4 == 0) goto L_0x0545
            goto L_0x05cf
        L_0x0545:
            if (r0 == 0) goto L_0x0584
            android.util.LongSparseArray<java.lang.Integer> r4 = r0.pinnedDialogs
            java.lang.Integer r7 = java.lang.Integer.valueOf(r1)
            r4.put(r5, r7)
            int r1 = r1 + 1
            if (r3 == 0) goto L_0x056e
            java.util.ArrayList<java.lang.Integer> r4 = r0.alwaysShow
            int r5 = r3.user_id
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            boolean r4 = r4.contains(r5)
            if (r4 != 0) goto L_0x05cf
            java.util.ArrayList<java.lang.Integer> r4 = r0.alwaysShow
            int r3 = r3.user_id
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r4.add(r3)
            goto L_0x05cf
        L_0x056e:
            java.util.ArrayList<java.lang.Integer> r3 = r0.alwaysShow
            java.lang.Integer r4 = java.lang.Integer.valueOf(r8)
            boolean r3 = r3.contains(r4)
            if (r3 != 0) goto L_0x05cf
            java.util.ArrayList<java.lang.Integer> r3 = r0.alwaysShow
            java.lang.Integer r4 = java.lang.Integer.valueOf(r8)
            r3.add(r4)
            goto L_0x05cf
        L_0x0584:
            org.telegram.messenger.MessagesController r21 = r31.getMessagesController()
            r24 = 1
            r25 = 0
            r26 = -1
            r22 = r5
            boolean r3 = r21.pinDialog(r22, r24, r25, r26)
            if (r3 == 0) goto L_0x05cf
            goto L_0x05cd
        L_0x0597:
            boolean r3 = r13.isDialogPinned(r7)
            if (r3 != 0) goto L_0x059e
            goto L_0x05cf
        L_0x059e:
            if (r0 == 0) goto L_0x05bb
            android.util.LongSparseArray<java.lang.Integer> r3 = r0.pinnedDialogs
            r4 = -2147483648(0xfffffffvar_, float:-0.0)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r4)
            java.lang.Object r3 = r3.get(r5, r7)
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            if (r3 != r4) goto L_0x05b5
            goto L_0x05cf
        L_0x05b5:
            android.util.LongSparseArray<java.lang.Integer> r3 = r0.pinnedDialogs
            r3.remove(r5)
            goto L_0x05cf
        L_0x05bb:
            org.telegram.messenger.MessagesController r21 = r31.getMessagesController()
            r24 = 0
            r25 = 0
            r26 = -1
            r22 = r5
            boolean r3 = r21.pinDialog(r22, r24, r25, r26)
            if (r3 == 0) goto L_0x05cf
        L_0x05cd:
            r18 = 1
        L_0x05cf:
            int r2 = r2 + 1
            r4 = r10
            r3 = 0
            r15 = 0
            goto L_0x02ed
        L_0x05d6:
            r7 = 100
            r15 = 1
            r17 = 102(0x66, float:1.43E-43)
            r6 = 108(0x6c, float:1.51E-43)
            if (r14 == r7) goto L_0x05e5
            if (r14 != r6) goto L_0x05e2
            goto L_0x05e5
        L_0x05e2:
            r15 = 108(0x6c, float:1.51E-43)
            goto L_0x061f
        L_0x05e5:
            if (r0 == 0) goto L_0x0611
            int r1 = r0.flags
            java.lang.String r2 = r0.name
            java.util.ArrayList<java.lang.Integer> r3 = r0.alwaysShow
            java.util.ArrayList<java.lang.Integer> r4 = r0.neverShow
            android.util.LongSparseArray<java.lang.Integer> r5 = r0.pinnedDialogs
            r8 = 0
            r9 = 0
            r10 = 1
            r11 = 1
            r12 = 0
            r16 = 0
            r15 = 108(0x6c, float:1.51E-43)
            r6 = r8
            r8 = 100
            r7 = r9
            r9 = 100
            r8 = r10
            r10 = 100
            r9 = r11
            r11 = 100
            r10 = r12
            r12 = 102(0x66, float:1.43E-43)
            r11 = r31
            r12 = r16
            org.telegram.ui.FilterCreateActivity.saveFilterToServer(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
            goto L_0x061f
        L_0x0611:
            r15 = 108(0x6c, float:1.51E-43)
            org.telegram.messenger.MessagesController r0 = r31.getMessagesController()
            int r1 = r13.folderId
            r2 = 0
            r4 = 0
            r0.reorderPinnedDialogs(r1, r4, r2)
        L_0x061f:
            if (r18 == 0) goto L_0x0628
            r0 = 0
            r13.hideFloatingButton(r0)
            r31.scrollToTop()
        L_0x0628:
            if (r14 == r15) goto L_0x0634
            r0 = 100
            if (r14 == r0) goto L_0x0634
            r0 = 102(0x66, float:1.43E-43)
            if (r14 == r0) goto L_0x0634
            r15 = 1
            goto L_0x0635
        L_0x0634:
            r15 = 0
        L_0x0635:
            r13.hideActionMode(r15)
            return
        L_0x0639:
            r11 = 3
            r16 = 2
            java.util.ArrayList r7 = new java.util.ArrayList
            java.util.ArrayList<java.lang.Long> r0 = r13.selectedDialogs
            r7.<init>(r0)
            org.telegram.messenger.MessagesController r0 = r31.getMessagesController()
            int r1 = r13.canUnarchiveCount
            if (r1 != 0) goto L_0x064d
            r2 = 1
            goto L_0x064e
        L_0x064d:
            r2 = 0
        L_0x064e:
            r3 = -1
            r4 = 0
            r5 = 0
            r1 = r7
            r0.addDialogToFolder(r1, r2, r3, r4, r5)
            int r0 = r13.canUnarchiveCount
            if (r0 != 0) goto L_0x06a6
            android.content.SharedPreferences r0 = org.telegram.messenger.MessagesController.getGlobalMainSettings()
            java.lang.String r1 = "archivehint_l"
            r2 = 0
            boolean r3 = r0.getBoolean(r1, r2)
            if (r3 != 0) goto L_0x066e
            boolean r2 = org.telegram.messenger.SharedConfig.archiveHidden
            if (r2 == 0) goto L_0x066c
            goto L_0x066e
        L_0x066c:
            r12 = 0
            goto L_0x066f
        L_0x066e:
            r12 = 1
        L_0x066f:
            if (r12 != 0) goto L_0x067e
            android.content.SharedPreferences$Editor r0 = r0.edit()
            r2 = 1
            android.content.SharedPreferences$Editor r0 = r0.putBoolean(r1, r2)
            r0.commit()
            goto L_0x067f
        L_0x067e:
            r2 = 1
        L_0x067f:
            if (r12 == 0) goto L_0x068c
            int r0 = r7.size()
            if (r0 <= r2) goto L_0x0689
            r6 = 4
            goto L_0x068a
        L_0x0689:
            r6 = 2
        L_0x068a:
            r4 = r6
            goto L_0x0696
        L_0x068c:
            int r0 = r7.size()
            if (r0 <= r2) goto L_0x0694
            r0 = 5
            goto L_0x0695
        L_0x0694:
            r0 = 3
        L_0x0695:
            r4 = r0
        L_0x0696:
            org.telegram.ui.Components.UndoView r1 = r31.getUndoView()
            r2 = 0
            r5 = 0
            org.telegram.ui.-$$Lambda$DialogsActivity$0QpyGwN-yiHv-mK6RAwKtAjJSo0 r6 = new org.telegram.ui.-$$Lambda$DialogsActivity$0QpyGwN-yiHv-mK6RAwKtAjJSo0
            r6.<init>(r7)
            r1.showWithAction(r2, r4, r5, r6)
            goto L_0x06d7
        L_0x06a6:
            org.telegram.messenger.MessagesController r0 = r31.getMessagesController()
            int r1 = r13.folderId
            java.util.ArrayList r0 = r0.getDialogs(r1)
            org.telegram.ui.DialogsActivity$ViewPage[] r1 = r13.viewPages
            if (r1 == 0) goto L_0x06d7
            boolean r0 = r0.isEmpty()
            if (r0 == 0) goto L_0x06d7
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r13.viewPages
            r1 = 0
            r0 = r0[r1]
            org.telegram.ui.DialogsActivity$DialogsRecyclerView r0 = r0.listView
            r2 = 0
            r0.setEmptyView(r2)
            org.telegram.ui.DialogsActivity$ViewPage[] r0 = r13.viewPages
            r0 = r0[r1]
            org.telegram.ui.Components.RadialProgressView r0 = r0.progressView
            r2 = 4
            r0.setVisibility(r2)
            r31.finishFragment()
            goto L_0x06d8
        L_0x06d7:
            r1 = 0
        L_0x06d8:
            r13.hideActionMode(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.perfromSelectedDialogsAction(int, boolean):void");
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$15$DialogsActivity(ArrayList arrayList) {
        getMessagesController().addDialogToFolder(arrayList, this.folderId == 0 ? 0 : 1, -1, (ArrayList<TLRPC$TL_inputFolderPeer>) null, 0);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$16$DialogsActivity(DialogInterface dialogInterface, int i) {
        presentFragment(new FiltersSetupActivity());
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$17$DialogsActivity(int i, DialogInterface dialogInterface, int i2) {
        getMessagesController().setDialogsInTransaction(true);
        perfromSelectedDialogsAction(i, false);
        getMessagesController().setDialogsInTransaction(false);
        MessagesController.getInstance(this.currentAccount).checkIfFolderEmpty(this.folderId);
        if (this.folderId != 0 && getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false).size() == 0) {
            this.viewPages[0].listView.setEmptyView((View) null);
            this.viewPages[0].progressView.setVisibility(4);
            finishFragment();
        }
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$18$DialogsActivity(int i, DialogInterface dialogInterface, int i2) {
        perfromSelectedDialogsAction(i, false);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$19$DialogsActivity(int i, DialogInterface dialogInterface, int i2) {
        perfromSelectedDialogsAction(i, false);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$20$DialogsActivity(boolean z, boolean z2) {
        int size = this.selectedDialogs.size();
        for (int i = 0; i < size; i++) {
            long longValue = this.selectedDialogs.get(i).longValue();
            int i2 = (int) longValue;
            if (z) {
                getMessagesController().reportSpam(longValue, getMessagesController().getUser(Integer.valueOf(i2)), (TLRPC$Chat) null, (TLRPC$EncryptedChat) null, false);
            }
            if (z2) {
                getMessagesController().deleteDialog(longValue, 0, true);
            }
            getMessagesController().blockUser(i2);
        }
        hideActionMode(false);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$21$DialogsActivity(DialogInterface dialogInterface, int i) {
        getMessagesController().hidePromoDialog();
        hideActionMode(false);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$23$DialogsActivity(int i, TLRPC$Chat tLRPC$Chat, long j, boolean z, boolean z2) {
        int i2 = i;
        TLRPC$Chat tLRPC$Chat2 = tLRPC$Chat;
        long j2 = j;
        hideActionMode(false);
        if (i2 != 103 || !ChatObject.isChannel(tLRPC$Chat) || (tLRPC$Chat2.megagroup && TextUtils.isEmpty(tLRPC$Chat2.username))) {
            boolean z3 = z2;
            if (i2 == 102 && this.folderId != 0 && getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false).size() == 1) {
                this.viewPages[0].progressView.setVisibility(4);
            }
            getUndoView().showWithAction(j2, i2 == 103 ? 0 : 1, (Runnable) new Runnable(i, j, z2, tLRPC$Chat, z) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ long f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ TLRPC$Chat f$4;
                public final /* synthetic */ boolean f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r5;
                    this.f$4 = r6;
                    this.f$5 = r7;
                }

                public final void run() {
                    DialogsActivity.this.lambda$null$22$DialogsActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
            return;
        }
        getMessagesController().deleteDialog(j2, 2, z2);
    }

    public /* synthetic */ void lambda$null$22$DialogsActivity(int i, long j, boolean z, TLRPC$Chat tLRPC$Chat, boolean z2) {
        if (i == 103) {
            getMessagesController().deleteDialog(j, 1, z);
            return;
        }
        if (tLRPC$Chat == null) {
            getMessagesController().deleteDialog(j, 0, z);
            if (z2) {
                getMessagesController().blockUser((int) j);
            }
        } else if (ChatObject.isNotInChat(tLRPC$Chat)) {
            getMessagesController().deleteDialog(j, 0, z);
        } else {
            getMessagesController().deleteUserFromChat((int) (-j), getMessagesController().getUser(Integer.valueOf(getUserConfig().getClientUserId())), (TLRPC$ChatFull) null);
        }
        if (AndroidUtilities.isTablet()) {
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats, Long.valueOf(j));
        }
        MessagesController.getInstance(this.currentAccount).checkIfFolderEmpty(this.folderId);
    }

    public /* synthetic */ void lambda$perfromSelectedDialogsAction$24$DialogsActivity(DialogInterface dialogInterface) {
        hideActionMode(true);
    }

    /* access modifiers changed from: private */
    public void scrollToTop() {
        int findFirstVisibleItemPosition = this.viewPages[0].layoutManager.findFirstVisibleItemPosition() * AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78.0f : 72.0f);
        int i = (this.viewPages[0].dialogsType != 0 || !hasHiddenArchive()) ? 0 : 1;
        if (((float) findFirstVisibleItemPosition) >= ((float) this.viewPages[0].listView.getMeasuredHeight()) * 1.2f) {
            this.viewPages[0].scrollHelper.setScrollDirection(1);
            this.viewPages[0].scrollHelper.scrollToPosition(i, 0, false, true);
            resetScroll();
            return;
        }
        this.viewPages[0].listView.smoothScrollToPosition(i);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e4, code lost:
        if (r4.getBoolean("dialog_bar_report" + r12, true) != false) goto L_0x00e8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateCounters(boolean r19) {
        /*
            r18 = this;
            r0 = r18
            r1 = 0
            r0.canDeletePsaSelected = r1
            r0.canUnarchiveCount = r1
            r0.canUnmuteCount = r1
            r0.canMuteCount = r1
            r0.canPinCount = r1
            r0.canReadCount = r1
            r0.canClearCacheCount = r1
            r0.canReportSpamCount = r1
            if (r19 == 0) goto L_0x0016
            return
        L_0x0016:
            java.util.ArrayList<java.lang.Long> r2 = r0.selectedDialogs
            int r2 = r2.size()
            org.telegram.messenger.UserConfig r3 = r18.getUserConfig()
            int r3 = r3.getClientUserId()
            android.content.SharedPreferences r4 = r18.getNotificationsSettings()
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            r10 = 0
        L_0x002e:
            if (r5 >= r2) goto L_0x01c6
            org.telegram.messenger.MessagesController r11 = r18.getMessagesController()
            android.util.LongSparseArray<org.telegram.tgnet.TLRPC$Dialog> r11 = r11.dialogs_dict
            java.util.ArrayList<java.lang.Long> r12 = r0.selectedDialogs
            java.lang.Object r12 = r12.get(r5)
            java.lang.Long r12 = (java.lang.Long) r12
            long r12 = r12.longValue()
            java.lang.Object r11 = r11.get(r12)
            org.telegram.tgnet.TLRPC$Dialog r11 = (org.telegram.tgnet.TLRPC$Dialog) r11
            if (r11 != 0) goto L_0x004e
            r16 = r2
            goto L_0x01bf
        L_0x004e:
            long r12 = r11.id
            boolean r14 = r0.isDialogPinned(r11)
            int r15 = r11.unread_count
            if (r15 != 0) goto L_0x005f
            boolean r15 = r11.unread_mark
            if (r15 == 0) goto L_0x005d
            goto L_0x005f
        L_0x005d:
            r15 = 0
            goto L_0x0060
        L_0x005f:
            r15 = 1
        L_0x0060:
            org.telegram.messenger.MessagesController r1 = r18.getMessagesController()
            boolean r1 = r1.isDialogMuted(r12)
            if (r1 == 0) goto L_0x0073
            int r1 = r0.canUnmuteCount
            r16 = r2
            r2 = 1
            int r1 = r1 + r2
            r0.canUnmuteCount = r1
            goto L_0x007b
        L_0x0073:
            r16 = r2
            r2 = 1
            int r1 = r0.canMuteCount
            int r1 = r1 + r2
            r0.canMuteCount = r1
        L_0x007b:
            if (r15 == 0) goto L_0x0082
            int r1 = r0.canReadCount
            int r1 = r1 + r2
            r0.canReadCount = r1
        L_0x0082:
            int r1 = r0.folderId
            if (r1 == r2) goto L_0x00a5
            int r1 = r11.folder_id
            if (r1 != r2) goto L_0x008b
            goto L_0x00a5
        L_0x008b:
            long r1 = (long) r3
            int r15 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r15 == 0) goto L_0x00ab
            r1 = 777000(0xbdb28, double:3.83889E-318)
            int r15 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r15 == 0) goto L_0x00ab
            org.telegram.messenger.MessagesController r1 = r18.getMessagesController()
            r2 = 0
            boolean r1 = r1.isPromoDialog(r12, r2)
            if (r1 != 0) goto L_0x00ab
            int r8 = r8 + 1
            goto L_0x00ab
        L_0x00a5:
            int r1 = r0.canUnarchiveCount
            r2 = 1
            int r1 = r1 + r2
            r0.canUnarchiveCount = r1
        L_0x00ab:
            int r1 = (int) r12
            r2 = 32
            r15 = r7
            r17 = r8
            long r7 = r12 >> r2
            int r2 = (int) r7
            if (r1 <= 0) goto L_0x00ee
            if (r1 != r3) goto L_0x00b9
            goto L_0x00ee
        L_0x00b9:
            org.telegram.messenger.MessagesController r7 = r18.getMessagesController()
            java.lang.Integer r8 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r7 = r7.getUser(r8)
            boolean r7 = org.telegram.messenger.MessagesController.isSupportUser(r7)
            if (r7 == 0) goto L_0x00cc
            goto L_0x00ee
        L_0x00cc:
            if (r1 == 0) goto L_0x00e7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "dialog_bar_report"
            r7.append(r8)
            r7.append(r12)
            java.lang.String r7 = r7.toString()
            r8 = 1
            boolean r7 = r4.getBoolean(r7, r8)
            if (r7 == 0) goto L_0x00f0
            goto L_0x00e8
        L_0x00e7:
            r8 = 1
        L_0x00e8:
            int r7 = r0.canReportSpamCount
            int r7 = r7 + r8
            r0.canReportSpamCount = r7
            goto L_0x00f0
        L_0x00ee:
            int r10 = r10 + 1
        L_0x00f0:
            boolean r7 = org.telegram.messenger.DialogObject.isChannel(r11)
            if (r7 == 0) goto L_0x0155
            org.telegram.messenger.MessagesController r2 = r18.getMessagesController()
            int r1 = -r1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$Chat r1 = r2.getChat(r1)
            org.telegram.messenger.MessagesController r2 = r18.getMessagesController()
            long r7 = r11.id
            r11 = 1
            boolean r2 = r2.isPromoDialog(r7, r11)
            if (r2 == 0) goto L_0x0123
            int r1 = r0.canClearCacheCount
            int r1 = r1 + r11
            r0.canClearCacheCount = r1
            org.telegram.messenger.MessagesController r1 = r18.getMessagesController()
            int r1 = r1.promoDialogType
            if (r1 != r11) goto L_0x0121
            int r6 = r6 + 1
            r0.canDeletePsaSelected = r11
        L_0x0121:
            r7 = r15
            goto L_0x0151
        L_0x0123:
            if (r14 == 0) goto L_0x0128
            int r9 = r9 + 1
            goto L_0x012d
        L_0x0128:
            int r2 = r0.canPinCount
            int r2 = r2 + r11
            r0.canPinCount = r2
        L_0x012d:
            if (r1 == 0) goto L_0x0148
            boolean r2 = r1.megagroup
            if (r2 == 0) goto L_0x0148
            java.lang.String r1 = r1.username
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x013f
            int r7 = r15 + 1
            goto L_0x01bc
        L_0x013f:
            int r1 = r0.canClearCacheCount
            r7 = 1
            int r1 = r1 + r7
            r0.canClearCacheCount = r1
            r7 = r15
            goto L_0x01bc
        L_0x0148:
            r7 = 1
            int r1 = r0.canClearCacheCount
            int r1 = r1 + r7
            r0.canClearCacheCount = r1
            int r6 = r6 + 1
            goto L_0x0121
        L_0x0151:
            r8 = r17
            goto L_0x01bf
        L_0x0155:
            r7 = 1
            if (r1 >= 0) goto L_0x015c
            if (r2 == r7) goto L_0x015c
            r7 = 1
            goto L_0x015d
        L_0x015c:
            r7 = 0
        L_0x015d:
            if (r7 == 0) goto L_0x016b
            org.telegram.messenger.MessagesController r8 = r18.getMessagesController()
            int r11 = -r1
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)
            r8.getChat(r11)
        L_0x016b:
            if (r1 != 0) goto L_0x0190
            org.telegram.messenger.MessagesController r1 = r18.getMessagesController()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            org.telegram.tgnet.TLRPC$EncryptedChat r1 = r1.getEncryptedChat(r2)
            if (r1 == 0) goto L_0x018a
            org.telegram.messenger.MessagesController r2 = r18.getMessagesController()
            int r1 = r1.user_id
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r1 = r2.getUser(r1)
            goto L_0x01a5
        L_0x018a:
            org.telegram.tgnet.TLRPC$TL_userEmpty r1 = new org.telegram.tgnet.TLRPC$TL_userEmpty
            r1.<init>()
            goto L_0x01a5
        L_0x0190:
            if (r7 != 0) goto L_0x01a4
            if (r1 <= 0) goto L_0x01a4
            r7 = 1
            if (r2 == r7) goto L_0x01a4
            org.telegram.messenger.MessagesController r2 = r18.getMessagesController()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            org.telegram.tgnet.TLRPC$User r1 = r2.getUser(r1)
            goto L_0x01a5
        L_0x01a4:
            r1 = 0
        L_0x01a5:
            if (r1 == 0) goto L_0x01af
            boolean r2 = r1.bot
            if (r2 == 0) goto L_0x01af
            boolean r1 = org.telegram.messenger.MessagesController.isSupportUser(r1)
        L_0x01af:
            if (r14 == 0) goto L_0x01b4
            int r9 = r9 + 1
            goto L_0x01ba
        L_0x01b4:
            int r1 = r0.canPinCount
            r2 = 1
            int r1 = r1 + r2
            r0.canPinCount = r1
        L_0x01ba:
            int r7 = r15 + 1
        L_0x01bc:
            int r6 = r6 + 1
            goto L_0x0151
        L_0x01bf:
            int r5 = r5 + 1
            r2 = r16
            r1 = 0
            goto L_0x002e
        L_0x01c6:
            r16 = r2
            r15 = r7
            r1 = 8
            if (r6 == r2) goto L_0x01d3
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.deleteItem
            r3.setVisibility(r1)
            goto L_0x01d9
        L_0x01d3:
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.deleteItem
            r4 = 0
            r3.setVisibility(r4)
        L_0x01d9:
            int r3 = r0.canClearCacheCount
            if (r3 == 0) goto L_0x01df
            if (r3 != r2) goto L_0x01e4
        L_0x01df:
            if (r15 == 0) goto L_0x01ea
            r7 = r15
            if (r7 == r2) goto L_0x01ea
        L_0x01e4:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.clearItem
            r3.setVisibility(r1)
            goto L_0x0211
        L_0x01ea:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.clearItem
            r4 = 0
            r3.setVisibility(r4)
            int r3 = r0.canClearCacheCount
            if (r3 == 0) goto L_0x0203
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.clearItem
            r4 = 2131624788(0x7f0e0354, float:1.8876766E38)
            java.lang.String r5 = "ClearHistoryCache"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r3.setText(r4)
            goto L_0x0211
        L_0x0203:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.clearItem
            r4 = 2131624787(0x7f0e0353, float:1.8876764E38)
            java.lang.String r5 = "ClearHistory"
            java.lang.String r4 = org.telegram.messenger.LocaleController.getString(r5, r4)
            r3.setText(r4)
        L_0x0211:
            int r3 = r0.canUnarchiveCount
            if (r3 == 0) goto L_0x0252
            r3 = 2131627222(0x7f0e0cd6, float:1.8881702E38)
            java.lang.String r4 = "Unarchive"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r4 = r0.archiveItem
            r5 = 2131165737(0x7var_, float:1.79457E38)
            r4.setTextAndIcon(r3, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.archive2Item
            r4.setIcon((int) r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.archive2Item
            r4.setContentDescription(r3)
            org.telegram.ui.Components.FilterTabsView r3 = r0.filterTabsView
            if (r3 == 0) goto L_0x0246
            int r3 = r3.getVisibility()
            if (r3 != 0) goto L_0x0246
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.archive2Item
            r4 = 0
            r3.setVisibility(r4)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.archiveItem
            r3.setVisibility(r1)
            goto L_0x029b
        L_0x0246:
            r4 = 0
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.archiveItem
            r3.setVisibility(r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.archive2Item
            r3.setVisibility(r1)
            goto L_0x029b
        L_0x0252:
            if (r8 == 0) goto L_0x0291
            r3 = 2131624245(0x7f0e0135, float:1.8875664E38)
            java.lang.String r4 = "Archive"
            java.lang.String r3 = org.telegram.messenger.LocaleController.getString(r4, r3)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r4 = r0.archiveItem
            r5 = 2131165661(0x7var_dd, float:1.7945545E38)
            r4.setTextAndIcon(r3, r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.archive2Item
            r4.setIcon((int) r5)
            org.telegram.ui.ActionBar.ActionBarMenuItem r4 = r0.archive2Item
            r4.setContentDescription(r3)
            org.telegram.ui.Components.FilterTabsView r3 = r0.filterTabsView
            if (r3 == 0) goto L_0x0285
            int r3 = r3.getVisibility()
            if (r3 != 0) goto L_0x0285
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.archive2Item
            r4 = 0
            r3.setVisibility(r4)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.archiveItem
            r3.setVisibility(r1)
            goto L_0x029b
        L_0x0285:
            r4 = 0
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.archiveItem
            r3.setVisibility(r4)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.archive2Item
            r3.setVisibility(r1)
            goto L_0x029b
        L_0x0291:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r3 = r0.archiveItem
            r3.setVisibility(r1)
            org.telegram.ui.ActionBar.ActionBarMenuItem r3 = r0.archive2Item
            r3.setVisibility(r1)
        L_0x029b:
            int r3 = r0.canPinCount
            int r3 = r3 + r9
            if (r3 == r2) goto L_0x02ac
            org.telegram.ui.ActionBar.ActionBarMenuItem r2 = r0.pinItem
            r2.setVisibility(r1)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.pin2Item
            r2.setVisibility(r1)
            r3 = 0
            goto L_0x02cd
        L_0x02ac:
            org.telegram.ui.Components.FilterTabsView r2 = r0.filterTabsView
            if (r2 == 0) goto L_0x02c2
            int r2 = r2.getVisibility()
            if (r2 != 0) goto L_0x02c2
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.pin2Item
            r3 = 0
            r2.setVisibility(r3)
            org.telegram.ui.ActionBar.ActionBarMenuItem r2 = r0.pinItem
            r2.setVisibility(r1)
            goto L_0x02cd
        L_0x02c2:
            r3 = 0
            org.telegram.ui.ActionBar.ActionBarMenuItem r2 = r0.pinItem
            r2.setVisibility(r3)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.pin2Item
            r2.setVisibility(r1)
        L_0x02cd:
            if (r10 == 0) goto L_0x02d5
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.blockItem
            r2.setVisibility(r1)
            goto L_0x02da
        L_0x02d5:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.blockItem
            r2.setVisibility(r3)
        L_0x02da:
            org.telegram.ui.Components.FilterTabsView r2 = r0.filterTabsView
            r3 = 2147483647(0x7fffffff, float:NaN)
            if (r2 == 0) goto L_0x02f7
            int r2 = r2.getVisibility()
            if (r2 != 0) goto L_0x02f7
            org.telegram.ui.Components.FilterTabsView r2 = r0.filterTabsView
            int r2 = r2.getCurrentTabId()
            if (r2 != r3) goto L_0x02f0
            goto L_0x02f7
        L_0x02f0:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.removeFromFolderItem
            r4 = 0
            r2.setVisibility(r4)
            goto L_0x02fc
        L_0x02f7:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.removeFromFolderItem
            r2.setVisibility(r1)
        L_0x02fc:
            org.telegram.ui.Components.FilterTabsView r2 = r0.filterTabsView
            if (r2 == 0) goto L_0x0321
            int r2 = r2.getVisibility()
            if (r2 != 0) goto L_0x0321
            org.telegram.ui.Components.FilterTabsView r2 = r0.filterTabsView
            int r2 = r2.getCurrentTabId()
            if (r2 != r3) goto L_0x0321
            java.util.ArrayList<java.lang.Long> r2 = r0.selectedDialogs
            java.util.ArrayList r2 = org.telegram.ui.Components.FiltersListBottomSheet.getCanAddDialogFilters(r0, r2)
            boolean r2 = r2.isEmpty()
            if (r2 != 0) goto L_0x0321
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r1 = r0.addToFolderItem
            r2 = 0
            r1.setVisibility(r2)
            goto L_0x0326
        L_0x0321:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r2 = r0.addToFolderItem
            r2.setVisibility(r1)
        L_0x0326:
            int r1 = r0.canUnmuteCount
            if (r1 == 0) goto L_0x0341
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.muteItem
            r2 = 2131165739(0x7var_b, float:1.7945704E38)
            r1.setIcon((int) r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.muteItem
            r2 = 2131624755(0x7f0e0333, float:1.8876699E38)
            java.lang.String r3 = "ChatsUnmute"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setContentDescription(r2)
            goto L_0x0357
        L_0x0341:
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.muteItem
            r2 = 2131165701(0x7var_, float:1.7945627E38)
            r1.setIcon((int) r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.muteItem
            r2 = 2131624735(0x7f0e031f, float:1.8876658E38)
            java.lang.String r3 = "ChatsMute"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setContentDescription(r2)
        L_0x0357:
            int r1 = r0.canReadCount
            if (r1 == 0) goto L_0x036d
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r1 = r0.readItem
            r2 = 2131625743(0x7f0e070f, float:1.8878703E38)
            java.lang.String r3 = "MarkAsRead"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r3 = 2131165696(0x7var_, float:1.7945616E38)
            r1.setTextAndIcon(r2, r3)
            goto L_0x037e
        L_0x036d:
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r1 = r0.readItem
            r2 = 2131625744(0x7f0e0710, float:1.8878705E38)
            java.lang.String r3 = "MarkAsUnread"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r3 = 2131165697(0x7var_, float:1.7945618E38)
            r1.setTextAndIcon(r2, r3)
        L_0x037e:
            int r1 = r0.canPinCount
            if (r1 == 0) goto L_0x03a7
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.pinItem
            r2 = 2131165708(0x7var_c, float:1.794564E38)
            r1.setIcon((int) r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.pinItem
            r2 = 2131626503(0x7f0e0a07, float:1.8880244E38)
            java.lang.String r3 = "PinToTop"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setContentDescription(r2)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r1 = r0.pin2Item
            r2 = 2131625007(0x7f0e042f, float:1.887721E38)
            java.lang.String r3 = "DialogPin"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setText(r2)
            goto L_0x03cb
        L_0x03a7:
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.pinItem
            r2 = 2131165740(0x7var_c, float:1.7945706E38)
            r1.setIcon((int) r2)
            org.telegram.ui.ActionBar.ActionBarMenuItem r1 = r0.pinItem
            r2 = 2131627236(0x7f0e0ce4, float:1.888173E38)
            java.lang.String r3 = "UnpinFromTop"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setContentDescription(r2)
            org.telegram.ui.ActionBar.ActionBarMenuSubItem r1 = r0.pin2Item
            r2 = 2131625008(0x7f0e0430, float:1.8877212E38)
            java.lang.String r3 = "DialogUnpin"
            java.lang.String r2 = org.telegram.messenger.LocaleController.getString(r3, r2)
            r1.setText(r2)
        L_0x03cb:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DialogsActivity.updateCounters(boolean):void");
    }

    /* access modifiers changed from: private */
    public boolean validateSlowModeDialog(long j) {
        int i;
        TLRPC$Chat chat;
        ChatActivityEnterView chatActivityEnterView;
        if ((this.messagesCount <= 1 && ((chatActivityEnterView = this.commentView) == null || chatActivityEnterView.getVisibility() != 0 || TextUtils.isEmpty(this.commentView.getFieldText()))) || (i = (int) j) >= 0 || (chat = getMessagesController().getChat(Integer.valueOf(-i))) == null || ChatObject.hasAdminRights(chat) || !chat.slowmode_enabled) {
            return true;
        }
        AlertsCreator.showSimpleAlert(this, LocaleController.getString("Slowmode", NUM), LocaleController.getString("SlowmodeSendError", NUM));
        return false;
    }

    private void showOrUpdateActionMode(TLRPC$Dialog tLRPC$Dialog, View view) {
        addOrRemoveSelectedDialog(tLRPC$Dialog.id, view);
        boolean z = true;
        if (!this.actionBar.isActionModeShowed()) {
            this.actionBar.createActionMode();
            this.actionBar.showActionMode();
            resetScroll();
            if (this.menuDrawable != null) {
                this.actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrGoBack", NUM));
            }
            if (getPinnedCount() > 1) {
                if (this.viewPages != null) {
                    int i = 0;
                    while (true) {
                        ViewPage[] viewPageArr = this.viewPages;
                        if (i >= viewPageArr.length) {
                            break;
                        }
                        viewPageArr[i].dialogsAdapter.onReorderStateChanged(true);
                        i++;
                    }
                }
                updateVisibleRows(131072);
            }
            AnimatorSet animatorSet = new AnimatorSet();
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < this.actionModeViews.size(); i2++) {
                View view2 = this.actionModeViews.get(i2);
                view2.setPivotY((float) (ActionBar.getCurrentActionBarHeight() / 2));
                AndroidUtilities.clearDrawableAnimation(view2);
                arrayList.add(ObjectAnimator.ofFloat(view2, View.SCALE_Y, new float[]{0.1f, 1.0f}));
            }
            animatorSet.playTogether(arrayList);
            animatorSet.setDuration(200);
            animatorSet.start();
            FilterTabsView filterTabsView2 = this.filterTabsView;
            if (filterTabsView2 != null) {
                filterTabsView2.animateColorsTo("profile_tabSelectedLine", "profile_tabSelectedText", "profile_tabText", "profile_tabSelector", "actionBarActionModeDefault");
            }
            MenuDrawable menuDrawable2 = this.menuDrawable;
            if (menuDrawable2 != null) {
                menuDrawable2.setRotateToBack(false);
                this.menuDrawable.setRotation(1.0f, true);
            } else {
                BackDrawable backDrawable2 = this.backDrawable;
                if (backDrawable2 != null) {
                    backDrawable2.setRotation(1.0f, true);
                }
            }
            z = false;
        } else if (this.selectedDialogs.isEmpty()) {
            hideActionMode(true);
            return;
        }
        updateCounters(false);
        this.selectedDialogsCountTextView.setNumber(this.selectedDialogs.size(), z);
    }

    /* access modifiers changed from: private */
    public void closeSearch() {
        if (AndroidUtilities.isTablet()) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            TLObject tLObject = this.searchObject;
            if (tLObject != null) {
                this.dialogsSearchAdapter.putRecentSearch(this.searchDialogId, tLObject);
                this.searchObject = null;
                return;
            }
            return;
        }
        this.closeSearchFieldOnHide = true;
    }

    /* access modifiers changed from: protected */
    public RecyclerListView getListView() {
        return this.viewPages[0].listView;
    }

    /* access modifiers changed from: protected */
    public RecyclerListView getSearchListView() {
        return this.searchListView;
    }

    /* access modifiers changed from: protected */
    public View getEmptyView() {
        return this.searchEmptyView;
    }

    /* access modifiers changed from: private */
    public UndoView getUndoView() {
        if (this.undoView[0].getVisibility() == 0) {
            UndoView[] undoViewArr = this.undoView;
            UndoView undoView2 = undoViewArr[0];
            undoViewArr[0] = undoViewArr[1];
            undoViewArr[1] = undoView2;
            undoView2.hide(true, 2);
            ContentView contentView = (ContentView) this.fragmentView;
            contentView.removeView(this.undoView[0]);
            contentView.addView(this.undoView[0]);
        }
        return this.undoView[0];
    }

    private void updateProxyButton(boolean z) {
        ActionBarMenuItem actionBarMenuItem;
        if (this.proxyDrawable != null) {
            ActionBarMenuItem actionBarMenuItem2 = this.doneItem;
            if (actionBarMenuItem2 == null || actionBarMenuItem2.getVisibility() != 0) {
                boolean z2 = false;
                SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                boolean z3 = sharedPreferences.getBoolean("proxy_enabled", false) && !TextUtils.isEmpty(sharedPreferences.getString("proxy_ip", ""));
                if (z3 || (getMessagesController().blockedCountry && !SharedConfig.proxyList.isEmpty())) {
                    if (!this.actionBar.isSearchFieldVisible() && ((actionBarMenuItem = this.doneItem) == null || actionBarMenuItem.getVisibility() != 0)) {
                        this.proxyItem.setVisibility(0);
                    }
                    this.proxyItemVisible = true;
                    ProxyDrawable proxyDrawable2 = this.proxyDrawable;
                    int i = this.currentConnectionState;
                    if (i == 3 || i == 5) {
                        z2 = true;
                    }
                    proxyDrawable2.setConnected(z3, z2, z);
                    return;
                }
                this.proxyItemVisible = false;
                this.proxyItem.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showDoneItem(final boolean z) {
        if (this.doneItem != null) {
            AnimatorSet animatorSet = this.doneItemAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.doneItemAnimator = null;
            }
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.doneItemAnimator = animatorSet2;
            animatorSet2.setDuration(180);
            if (z) {
                this.doneItem.setVisibility(0);
            } else {
                this.doneItem.setSelected(false);
                Drawable background = this.doneItem.getBackground();
                if (background != null) {
                    background.setState(StateSet.NOTHING);
                    background.jumpToCurrentState();
                }
                ActionBarMenuItem actionBarMenuItem = this.searchItem;
                if (actionBarMenuItem != null) {
                    actionBarMenuItem.setVisibility(0);
                }
                ActionBarMenuItem actionBarMenuItem2 = this.proxyItem;
                if (actionBarMenuItem2 != null && this.proxyItemVisible) {
                    actionBarMenuItem2.setVisibility(0);
                }
                ActionBarMenuItem actionBarMenuItem3 = this.passcodeItem;
                if (actionBarMenuItem3 != null && this.passcodeItemVisible) {
                    actionBarMenuItem3.setVisibility(0);
                }
            }
            ArrayList arrayList = new ArrayList();
            ActionBarMenuItem actionBarMenuItem4 = this.doneItem;
            Property property = View.ALPHA;
            float[] fArr = new float[1];
            float f = 1.0f;
            fArr[0] = z ? 1.0f : 0.0f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem4, property, fArr));
            if (this.proxyItemVisible) {
                ActionBarMenuItem actionBarMenuItem5 = this.proxyItem;
                Property property2 = View.ALPHA;
                float[] fArr2 = new float[1];
                fArr2[0] = z ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem5, property2, fArr2));
            }
            if (this.passcodeItemVisible) {
                ActionBarMenuItem actionBarMenuItem6 = this.passcodeItem;
                Property property3 = View.ALPHA;
                float[] fArr3 = new float[1];
                fArr3[0] = z ? 0.0f : 1.0f;
                arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem6, property3, fArr3));
            }
            ActionBarMenuItem actionBarMenuItem7 = this.searchItem;
            Property property4 = View.ALPHA;
            float[] fArr4 = new float[1];
            if (z) {
                f = 0.0f;
            }
            fArr4[0] = f;
            arrayList.add(ObjectAnimator.ofFloat(actionBarMenuItem7, property4, fArr4));
            this.doneItemAnimator.playTogether(arrayList);
            this.doneItemAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    AnimatorSet unused = DialogsActivity.this.doneItemAnimator = null;
                    if (z) {
                        if (DialogsActivity.this.searchItem != null) {
                            DialogsActivity.this.searchItem.setVisibility(4);
                        }
                        if (DialogsActivity.this.proxyItem != null && DialogsActivity.this.proxyItemVisible) {
                            DialogsActivity.this.proxyItem.setVisibility(4);
                        }
                        if (DialogsActivity.this.passcodeItem != null && DialogsActivity.this.passcodeItemVisible) {
                            DialogsActivity.this.passcodeItem.setVisibility(4);
                        }
                    } else if (DialogsActivity.this.doneItem != null) {
                        DialogsActivity.this.doneItem.setVisibility(8);
                    }
                }
            });
            this.doneItemAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    public void updateSelectedCount() {
        if (this.commentView != null) {
            if (this.selectedDialogs.isEmpty()) {
                if (this.initialDialogsType == 3 && this.selectAlertString == null) {
                    this.actionBar.setTitle(LocaleController.getString("ForwardTo", NUM));
                } else {
                    this.actionBar.setTitle(LocaleController.getString("SelectChat", NUM));
                }
                if (this.commentView.getTag() != null) {
                    this.commentView.hidePopup(false);
                    this.commentView.closeKeyboard();
                    AnimatorSet animatorSet = new AnimatorSet();
                    ChatActivityEnterView chatActivityEnterView = this.commentView;
                    animatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(chatActivityEnterView, View.TRANSLATION_Y, new float[]{0.0f, (float) chatActivityEnterView.getMeasuredHeight()})});
                    animatorSet.setDuration(180);
                    animatorSet.setInterpolator(new DecelerateInterpolator());
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            DialogsActivity.this.commentView.setVisibility(8);
                        }
                    });
                    animatorSet.start();
                    this.commentView.setTag((Object) null);
                    this.fragmentView.requestLayout();
                    return;
                }
                return;
            }
            if (this.commentView.getTag() == null) {
                this.commentView.setFieldText("");
                this.commentView.setVisibility(0);
                AnimatorSet animatorSet2 = new AnimatorSet();
                ChatActivityEnterView chatActivityEnterView2 = this.commentView;
                animatorSet2.playTogether(new Animator[]{ObjectAnimator.ofFloat(chatActivityEnterView2, View.TRANSLATION_Y, new float[]{(float) chatActivityEnterView2.getMeasuredHeight(), 0.0f})});
                animatorSet2.setDuration(180);
                animatorSet2.setInterpolator(new DecelerateInterpolator());
                animatorSet2.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        DialogsActivity.this.commentView.setTag(2);
                        DialogsActivity.this.commentView.requestLayout();
                    }
                });
                animatorSet2.start();
                this.commentView.setTag(1);
            }
            this.actionBar.setTitle(LocaleController.formatPluralString("Recipient", this.selectedDialogs.size()));
        }
    }

    @TargetApi(23)
    private void askForPermissons(boolean z) {
        Activity parentActivity = getParentActivity();
        if (parentActivity != null) {
            ArrayList arrayList = new ArrayList();
            if (getUserConfig().syncContacts && this.askAboutContacts && parentActivity.checkSelfPermission("android.permission.READ_CONTACTS") != 0) {
                if (z) {
                    AlertDialog create = AlertsCreator.createContactsPermissionDialog(parentActivity, new MessagesStorage.IntCallback() {
                        public final void run(int i) {
                            DialogsActivity.this.lambda$askForPermissons$25$DialogsActivity(i);
                        }
                    }).create();
                    this.permissionDialog = create;
                    showDialog(create);
                    return;
                }
                arrayList.add("android.permission.READ_CONTACTS");
                arrayList.add("android.permission.WRITE_CONTACTS");
                arrayList.add("android.permission.GET_ACCOUNTS");
            }
            if (parentActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                arrayList.add("android.permission.READ_EXTERNAL_STORAGE");
                arrayList.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
            if (!arrayList.isEmpty()) {
                try {
                    parentActivity.requestPermissions((String[]) arrayList.toArray(new String[0]), 1);
                } catch (Exception unused) {
                }
            } else if (this.askingForPermissions) {
                this.askingForPermissions = false;
                showFiltersHint();
            }
        }
    }

    public /* synthetic */ void lambda$askForPermissons$25$DialogsActivity(int i) {
        this.askAboutContacts = i != 0;
        MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", this.askAboutContacts).commit();
        askForPermissons(false);
    }

    /* access modifiers changed from: protected */
    public void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        AlertDialog alertDialog = this.permissionDialog;
        if (alertDialog != null && dialog == alertDialog && getParentActivity() != null) {
            askForPermissons(false);
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        FrameLayout frameLayout;
        super.onConfigurationChanged(configuration);
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss();
        }
        if (!this.onlySelect && (frameLayout = this.floatingButtonContainer) != null) {
            frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    DialogsActivity dialogsActivity = DialogsActivity.this;
                    float unused = dialogsActivity.floatingButtonTranslation = dialogsActivity.floatingHidden ? (float) AndroidUtilities.dp(100.0f) : 0.0f;
                    DialogsActivity.this.updateFloatingButtonOffset();
                    DialogsActivity.this.floatingButtonContainer.setClickable(!DialogsActivity.this.floatingHidden);
                    if (DialogsActivity.this.floatingButtonContainer != null) {
                        DialogsActivity.this.floatingButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    public void onRequestPermissionsResultFragment(int i, String[] strArr, int[] iArr) {
        if (i == 1) {
            for (int i2 = 0; i2 < strArr.length; i2++) {
                if (iArr.length > i2) {
                    String str = strArr[i2];
                    char c = 65535;
                    int hashCode = str.hashCode();
                    if (hashCode != NUM) {
                        if (hashCode == NUM && str.equals("android.permission.READ_CONTACTS")) {
                            c = 0;
                        }
                    } else if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                        c = 1;
                    }
                    if (c != 0) {
                        if (c == 1 && iArr[i2] == 0) {
                            ImageLoader.getInstance().checkMediaPaths();
                        }
                    } else if (iArr[i2] == 0) {
                        getContactsController().forceImportContacts();
                    } else {
                        SharedPreferences.Editor edit = MessagesController.getGlobalNotificationsSettings().edit();
                        this.askAboutContacts = false;
                        edit.putBoolean("askAboutContacts", false).commit();
                    }
                }
            }
            if (this.askingForPermissions) {
                this.askingForPermissions = false;
                showFiltersHint();
            }
        }
    }

    public void didReceivedNotification(int i, int i2, Object... objArr) {
        int i3 = 0;
        if (i == NotificationCenter.dialogsNeedReload) {
            if (this.viewPages != null && !this.dialogsListFrozen) {
                while (true) {
                    ViewPage[] viewPageArr = this.viewPages;
                    if (i3 >= viewPageArr.length) {
                        break;
                    }
                    if (viewPageArr[i3].getVisibility() == 0) {
                        if (this.viewPages[i3].dialogsAdapter.isDataSetChanged() || objArr.length > 0) {
                            this.viewPages[i3].dialogsAdapter.notifyDataSetChanged();
                        } else {
                            updateVisibleRows(2048);
                        }
                        try {
                            this.viewPages[i3].listView.setEmptyView(this.folderId == 0 ? this.viewPages[i3].progressView : null);
                        } catch (Exception e) {
                            FileLog.e((Throwable) e);
                        }
                        checkListLoad(this.viewPages[i3]);
                    }
                    i3++;
                }
                FilterTabsView filterTabsView2 = this.filterTabsView;
                if (filterTabsView2 != null && filterTabsView2.getVisibility() == 0) {
                    this.filterTabsView.checkTabsCounter();
                }
            }
        } else if (i == NotificationCenter.dialogsUnreadCounterChanged) {
            FilterTabsView filterTabsView3 = this.filterTabsView;
            if (filterTabsView3 != null && filterTabsView3.getVisibility() == 0) {
                this.filterTabsView.notifyTabCounterChanged(Integer.MAX_VALUE);
            }
        } else if (i == NotificationCenter.emojiDidLoad) {
            updateVisibleRows(0);
            FilterTabsView filterTabsView4 = this.filterTabsView;
            if (filterTabsView4 != null) {
                filterTabsView4.getTabsContainer().invalidateViews();
            }
        } else if (i == NotificationCenter.closeSearchByActiveAction) {
            ActionBar actionBar = this.actionBar;
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
        } else if (i == NotificationCenter.proxySettingsChanged) {
            updateProxyButton(false);
        } else if (i == NotificationCenter.updateInterfaces) {
            Integer num = objArr[0];
            updateVisibleRows(num.intValue());
            FilterTabsView filterTabsView5 = this.filterTabsView;
            if (!(filterTabsView5 == null || filterTabsView5.getVisibility() != 0 || (num.intValue() & 256) == 0)) {
                this.filterTabsView.checkTabsCounter();
            }
            if (this.viewPages != null) {
                while (i3 < this.viewPages.length) {
                    if ((num.intValue() & 4) != 0) {
                        this.viewPages[i3].dialogsAdapter.sortOnlineContacts(true);
                    }
                    i3++;
                }
            }
        } else if (i == NotificationCenter.appDidLogout) {
            dialogsLoaded[this.currentAccount] = false;
        } else if (i == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (i == NotificationCenter.contactsDidLoad) {
            if (this.viewPages != null && !this.dialogsListFrozen) {
                int i4 = 0;
                boolean z = false;
                while (true) {
                    ViewPage[] viewPageArr2 = this.viewPages;
                    if (i4 >= viewPageArr2.length) {
                        break;
                    }
                    if (!viewPageArr2[i4].isDefaultDialogType() || !getMessagesController().getDialogs(this.folderId).isEmpty()) {
                        z = true;
                    } else {
                        this.viewPages[i4].dialogsAdapter.notifyDataSetChanged();
                    }
                    i4++;
                }
                if (z) {
                    updateVisibleRows(0);
                }
            }
        } else if (i == NotificationCenter.openedChatChanged) {
            if (this.viewPages != null) {
                int i5 = 0;
                while (true) {
                    ViewPage[] viewPageArr3 = this.viewPages;
                    if (i5 < viewPageArr3.length) {
                        if (viewPageArr3[i5].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                            boolean booleanValue = objArr[1].booleanValue();
                            long longValue = objArr[0].longValue();
                            if (!booleanValue) {
                                this.openedDialogId = longValue;
                            } else if (longValue == this.openedDialogId) {
                                this.openedDialogId = 0;
                            }
                            this.viewPages[i5].dialogsAdapter.setOpenedDialogId(this.openedDialogId);
                        }
                        i5++;
                    } else {
                        updateVisibleRows(512);
                        return;
                    }
                }
            }
        } else if (i == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (i == NotificationCenter.messageReceivedByAck || i == NotificationCenter.messageReceivedByServer || i == NotificationCenter.messageSendError) {
            updateVisibleRows(4096);
        } else if (i == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (i == NotificationCenter.needReloadRecentDialogsSearch) {
            DialogsSearchAdapter dialogsSearchAdapter2 = this.dialogsSearchAdapter;
            if (dialogsSearchAdapter2 != null) {
                dialogsSearchAdapter2.loadRecentSearch();
            }
        } else if (i == NotificationCenter.replyMessagesDidLoad) {
            updateVisibleRows(32768);
        } else if (i == NotificationCenter.reloadHints) {
            DialogsSearchAdapter dialogsSearchAdapter3 = this.dialogsSearchAdapter;
            if (dialogsSearchAdapter3 != null) {
                dialogsSearchAdapter3.notifyDataSetChanged();
            }
        } else if (i == NotificationCenter.didUpdateConnectionState) {
            int connectionState = AccountInstance.getInstance(i2).getConnectionsManager().getConnectionState();
            if (this.currentConnectionState != connectionState) {
                this.currentConnectionState = connectionState;
                updateProxyButton(true);
            }
        } else if (i == NotificationCenter.needDeleteDialog) {
            if (this.fragmentView != null && !this.isPaused) {
                long longValue2 = objArr[0].longValue();
                $$Lambda$DialogsActivity$2y6ntZCb0_UYSZKtVgbtovoNeQg r3 = new Runnable(objArr[2], longValue2, objArr[3].booleanValue(), objArr[1]) {
                    public final /* synthetic */ TLRPC$Chat f$1;
                    public final /* synthetic */ long f$2;
                    public final /* synthetic */ boolean f$3;
                    public final /* synthetic */ TLRPC$User f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r5;
                        this.f$4 = r6;
                    }

                    public final void run() {
                        DialogsActivity.this.lambda$didReceivedNotification$26$DialogsActivity(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                };
                if (this.undoView[0] != null) {
                    getUndoView().showWithAction(longValue2, 1, (Runnable) r3);
                } else {
                    r3.run();
                }
            }
        } else if (i == NotificationCenter.folderBecomeEmpty) {
            int intValue = objArr[0].intValue();
            int i6 = this.folderId;
            if (i6 == intValue && i6 != 0) {
                finishFragment();
            }
        } else if (i == NotificationCenter.dialogFiltersUpdated) {
            updateFilterTabs(true);
        } else if (i == NotificationCenter.filterSettingsUpdated) {
            showFiltersHint();
        } else if (i == NotificationCenter.newSuggestionsAvailable) {
            showNextSupportedSuggestion();
        }
    }

    public /* synthetic */ void lambda$didReceivedNotification$26$DialogsActivity(TLRPC$Chat tLRPC$Chat, long j, boolean z, TLRPC$User tLRPC$User) {
        if (tLRPC$Chat == null) {
            getMessagesController().deleteDialog(j, 0, z);
            if (tLRPC$User != null && tLRPC$User.bot) {
                getMessagesController().blockUser(tLRPC$User.id);
            }
        } else if (ChatObject.isNotInChat(tLRPC$Chat)) {
            getMessagesController().deleteDialog(j, 0, z);
        } else {
            getMessagesController().deleteUserFromChat((int) (-j), getMessagesController().getUser(Integer.valueOf(getUserConfig().getClientUserId())), (TLRPC$ChatFull) null, false, z);
        }
        MessagesController.getInstance(this.currentAccount).checkIfFolderEmpty(this.folderId);
    }

    private void showNextSupportedSuggestion() {
        if (this.showingSuggestion == null) {
            for (String next : getMessagesController().pendingSuggestions) {
                if (showSuggestion(next)) {
                    this.showingSuggestion = next;
                    return;
                }
            }
        }
    }

    private void onSuggestionDismiss() {
        if (this.showingSuggestion != null) {
            getMessagesController().removeSuggestion(this.showingSuggestion);
            this.showingSuggestion = null;
            showNextSupportedSuggestion();
        }
    }

    private boolean showSuggestion(String str) {
        if (!"AUTOARCHIVE_POPULAR".equals(str)) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
        builder.setTitle(LocaleController.getString("HideNewChatsAlertTitle", NUM));
        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("HideNewChatsAlertText", NUM)));
        builder.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
        builder.setPositiveButton(LocaleController.getString("GoToSettings", NUM), new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                DialogsActivity.this.lambda$showSuggestion$27$DialogsActivity(dialogInterface, i);
            }
        });
        showDialog(builder.create(), new DialogInterface.OnDismissListener() {
            public final void onDismiss(DialogInterface dialogInterface) {
                DialogsActivity.this.lambda$showSuggestion$28$DialogsActivity(dialogInterface);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$showSuggestion$27$DialogsActivity(DialogInterface dialogInterface, int i) {
        presentFragment(new PrivacySettingsActivity());
        AndroidUtilities.scrollToFragmentRow(this.parentLayout, "newChatsRow");
    }

    public /* synthetic */ void lambda$showSuggestion$28$DialogsActivity(DialogInterface dialogInterface) {
        onSuggestionDismiss();
    }

    private void showFiltersHint() {
        FilterTabsView filterTabsView2;
        if (!this.askingForPermissions && getMessagesController().dialogFiltersLoaded && getMessagesController().showFiltersTooltip && (filterTabsView2 = this.filterTabsView) != null && filterTabsView2.getVisibility() != 0 && !this.isPaused && getUserConfig().filtersLoaded) {
            SharedPreferences globalMainSettings = MessagesController.getGlobalMainSettings();
            if (!globalMainSettings.getBoolean("filterhint", false)) {
                globalMainSettings.edit().putBoolean("filterhint", true).commit();
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public final void run() {
                        DialogsActivity.this.lambda$showFiltersHint$30$DialogsActivity();
                    }
                }, 1000);
            }
        }
    }

    public /* synthetic */ void lambda$null$29$DialogsActivity() {
        presentFragment(new FiltersSetupActivity());
    }

    public /* synthetic */ void lambda$showFiltersHint$30$DialogsActivity() {
        getUndoView().showWithAction(0, 15, (Runnable) null, new Runnable() {
            public final void run() {
                DialogsActivity.this.lambda$null$29$DialogsActivity();
            }
        });
    }

    /* access modifiers changed from: private */
    public void setDialogsListFrozen(boolean z) {
        if (this.viewPages != null && this.dialogsListFrozen != z) {
            if (z) {
                frozenDialogsList = new ArrayList<>(getDialogsArray(this.currentAccount, this.viewPages[0].dialogsType, this.folderId, false));
            } else {
                frozenDialogsList = null;
            }
            this.dialogsListFrozen = z;
            this.viewPages[0].dialogsAdapter.setDialogsListFrozen(z);
            if (!z) {
                this.viewPages[0].dialogsAdapter.notifyDataSetChanged();
            }
        }
    }

    public static ArrayList<TLRPC$Dialog> getDialogsArray(int i, int i2, int i3, boolean z) {
        ArrayList<TLRPC$Dialog> arrayList;
        if (z && (arrayList = frozenDialogsList) != null) {
            return arrayList;
        }
        MessagesController messagesController = AccountInstance.getInstance(i).getMessagesController();
        if (i2 == 0) {
            return messagesController.getDialogs(i3);
        }
        char c = 1;
        if (i2 == 1) {
            return messagesController.dialogsServerOnly;
        }
        if (i2 == 2) {
            return messagesController.dialogsCanAddUsers;
        }
        if (i2 == 3) {
            return messagesController.dialogsForward;
        }
        if (i2 == 4) {
            return messagesController.dialogsUsersOnly;
        }
        if (i2 == 5) {
            return messagesController.dialogsChannelsOnly;
        }
        if (i2 == 6) {
            return messagesController.dialogsGroupsOnly;
        }
        if (i2 != 7 && i2 != 8) {
            return null;
        }
        MessagesController.DialogFilter[] dialogFilterArr = messagesController.selectedDialogFilter;
        if (i2 == 7) {
            c = 0;
        }
        MessagesController.DialogFilter dialogFilter = dialogFilterArr[c];
        if (dialogFilter == null) {
            return messagesController.getDialogs(i3);
        }
        return dialogFilter.dialogs;
    }

    public void setSideMenu(RecyclerView recyclerView) {
        this.sideMenu = recyclerView;
        recyclerView.setBackgroundColor(Theme.getColor("chats_menuBackground"));
        this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
    }

    /* access modifiers changed from: private */
    public void updatePasscodeButton() {
        if (this.passcodeItem != null) {
            if (SharedConfig.passcodeHash.length() == 0 || this.searching) {
                this.passcodeItem.setVisibility(8);
                this.passcodeItemVisible = false;
                return;
            }
            ActionBarMenuItem actionBarMenuItem = this.doneItem;
            if (actionBarMenuItem == null || actionBarMenuItem.getVisibility() != 0) {
                this.passcodeItem.setVisibility(0);
            }
            this.passcodeItemVisible = true;
            if (SharedConfig.appLocked) {
                this.passcodeItem.setIcon(NUM);
                this.passcodeItem.setContentDescription(LocaleController.getString("AccDescrPasscodeUnlock", NUM));
                return;
            }
            this.passcodeItem.setIcon(NUM);
            this.passcodeItem.setContentDescription(LocaleController.getString("AccDescrPasscodeLock", NUM));
        }
    }

    /* access modifiers changed from: private */
    public void hideFloatingButton(boolean z) {
        if (this.floatingHidden != z) {
            this.floatingHidden = z;
            AnimatorSet animatorSet = new AnimatorSet();
            float[] fArr = new float[2];
            fArr[0] = this.floatingButtonHideProgress;
            fArr[1] = this.floatingHidden ? 1.0f : 0.0f;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DialogsActivity.this.lambda$hideFloatingButton$31$DialogsActivity(valueAnimator);
                }
            });
            animatorSet.playTogether(new Animator[]{ofFloat});
            animatorSet.setDuration(300);
            animatorSet.setInterpolator(this.floatingInterpolator);
            this.floatingButtonContainer.setClickable(!z);
            animatorSet.start();
        }
    }

    public /* synthetic */ void lambda$hideFloatingButton$31$DialogsActivity(ValueAnimator valueAnimator) {
        this.floatingButtonHideProgress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.floatingButtonTranslation = ((float) AndroidUtilities.dp(100.0f)) * this.floatingButtonHideProgress;
        updateFloatingButtonOffset();
    }

    /* access modifiers changed from: private */
    public void updateDialogIndices() {
        int indexOf;
        if (this.viewPages != null) {
            int i = 0;
            while (true) {
                ViewPage[] viewPageArr = this.viewPages;
                if (i < viewPageArr.length) {
                    if (viewPageArr[i].getVisibility() == 0) {
                        ArrayList<TLRPC$Dialog> dialogsArray = getDialogsArray(this.currentAccount, this.viewPages[i].dialogsType, this.folderId, false);
                        int childCount = this.viewPages[i].listView.getChildCount();
                        for (int i2 = 0; i2 < childCount; i2++) {
                            View childAt = this.viewPages[i].listView.getChildAt(i2);
                            if (childAt instanceof DialogCell) {
                                DialogCell dialogCell = (DialogCell) childAt;
                                TLRPC$Dialog tLRPC$Dialog = getMessagesController().dialogs_dict.get(dialogCell.getDialogId());
                                if (tLRPC$Dialog != null && (indexOf = dialogsArray.indexOf(tLRPC$Dialog)) >= 0) {
                                    dialogCell.setDialogIndex(indexOf);
                                }
                            }
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateVisibleRows(int i) {
        RecyclerListView recyclerListView;
        if (!this.dialogsListFrozen) {
            int i2 = 0;
            while (i2 < 3) {
                if (i2 == 2) {
                    recyclerListView = this.searchListView;
                } else {
                    ViewPage[] viewPageArr = this.viewPages;
                    if (viewPageArr != null) {
                        recyclerListView = i2 < viewPageArr.length ? viewPageArr[i2].listView : null;
                        if (!(recyclerListView == null || this.viewPages[i2].getVisibility() == 0)) {
                        }
                    }
                    i2++;
                }
                if (recyclerListView != null) {
                    int childCount = recyclerListView.getChildCount();
                    for (int i3 = 0; i3 < childCount; i3++) {
                        View childAt = recyclerListView.getChildAt(i3);
                        if (childAt instanceof DialogCell) {
                            if (recyclerListView.getAdapter() != this.dialogsSearchAdapter) {
                                DialogCell dialogCell = (DialogCell) childAt;
                                boolean z = true;
                                if ((131072 & i) != 0) {
                                    dialogCell.onReorderStateChanged(this.actionBar.isActionModeShowed(), true);
                                }
                                if ((65536 & i) != 0) {
                                    if ((i & 8192) == 0) {
                                        z = false;
                                    }
                                    dialogCell.setChecked(false, z);
                                } else {
                                    if ((i & 2048) != 0) {
                                        dialogCell.checkCurrentDialogIndex(this.dialogsListFrozen);
                                        if (this.viewPages[i2].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                            if (dialogCell.getDialogId() != this.openedDialogId) {
                                                z = false;
                                            }
                                            dialogCell.setDialogSelected(z);
                                        }
                                    } else if ((i & 512) == 0) {
                                        dialogCell.update(i);
                                    } else if (this.viewPages[i2].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                        if (dialogCell.getDialogId() != this.openedDialogId) {
                                            z = false;
                                        }
                                        dialogCell.setDialogSelected(z);
                                    }
                                    ArrayList<Long> arrayList = this.selectedDialogs;
                                    if (arrayList != null) {
                                        dialogCell.setChecked(arrayList.contains(Long.valueOf(dialogCell.getDialogId())), false);
                                    }
                                }
                            }
                        } else if (childAt instanceof UserCell) {
                            ((UserCell) childAt).update(i);
                        } else if (childAt instanceof ProfileSearchCell) {
                            ((ProfileSearchCell) childAt).update(i);
                        } else if (childAt instanceof RecyclerListView) {
                            RecyclerListView recyclerListView2 = (RecyclerListView) childAt;
                            int childCount2 = recyclerListView2.getChildCount();
                            for (int i4 = 0; i4 < childCount2; i4++) {
                                View childAt2 = recyclerListView2.getChildAt(i4);
                                if (childAt2 instanceof HintDialogCell) {
                                    ((HintDialogCell) childAt2).update(i);
                                }
                            }
                        }
                    }
                }
                i2++;
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        this.delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String str) {
        this.searchString = str;
    }

    public boolean isMainDialogList() {
        return this.delegate == null && this.searchString == null;
    }

    /* access modifiers changed from: private */
    public void didSelectResult(long j, boolean z, boolean z2) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        if (this.addToGroupAlertString == null && this.checkCanWrite) {
            int i = (int) j;
            if (i < 0) {
                int i2 = -i;
                TLRPC$Chat chat = getMessagesController().getChat(Integer.valueOf(i2));
                if (ChatObject.isChannel(chat) && !chat.megagroup && (this.cantSendToChannels || !ChatObject.isCanWriteToChannel(i2, this.currentAccount) || this.hasPoll == 2)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Context) getParentActivity());
                    builder.setTitle(LocaleController.getString("SendMessageTitle", NUM));
                    if (this.hasPoll == 2) {
                        builder.setMessage(LocaleController.getString("PublicPollCantForward", NUM));
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelCantSendMessage", NUM));
                    }
                    builder.setNegativeButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
                    showDialog(builder.create());
                    return;
                }
            } else if (i == 0 && this.hasPoll != 0) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder((Context) getParentActivity());
                builder2.setTitle(LocaleController.getString("SendMessageTitle", NUM));
                builder2.setMessage(LocaleController.getString("PollCantForwardSecretChat", NUM));
                builder2.setNegativeButton(LocaleController.getString("OK", NUM), (DialogInterface.OnClickListener) null);
                showDialog(builder2.create());
                return;
            }
        }
        if (!z || ((this.selectAlertString == null || this.selectAlertStringGroup == null) && this.addToGroupAlertString == null)) {
            if (this.delegate != null) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(Long.valueOf(j));
                this.delegate.didSelectDialogs(this, arrayList, (CharSequence) null, z2);
                if (this.resetDelegate) {
                    this.delegate = null;
                    return;
                }
                return;
            }
            finishFragment();
        } else if (getParentActivity() != null) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder((Context) getParentActivity());
            int i3 = (int) j;
            int i4 = (int) (j >> 32);
            if (i3 == 0) {
                TLRPC$User user = getMessagesController().getUser(Integer.valueOf(getMessagesController().getEncryptedChat(Integer.valueOf(i4)).user_id));
                if (user != null) {
                    str5 = LocaleController.getString("SendMessageTitle", NUM);
                    str4 = LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user));
                    str2 = LocaleController.getString("Send", NUM);
                } else {
                    return;
                }
            } else if (i3 == getUserConfig().getClientUserId()) {
                str = LocaleController.getString("SendMessageTitle", NUM);
                str3 = LocaleController.formatStringSimple(this.selectAlertStringGroup, LocaleController.getString("SavedMessages", NUM));
                str2 = LocaleController.getString("Send", NUM);
                builder3.setTitle(str);
                builder3.setMessage(AndroidUtilities.replaceTags(str3));
                builder3.setPositiveButton(str2, new DialogInterface.OnClickListener(j) {
                    public final /* synthetic */ long f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onClick(DialogInterface dialogInterface, int i) {
                        DialogsActivity.this.lambda$didSelectResult$32$DialogsActivity(this.f$1, dialogInterface, i);
                    }
                });
                builder3.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
                showDialog(builder3.create());
            } else if (i3 > 0) {
                TLRPC$User user2 = getMessagesController().getUser(Integer.valueOf(i3));
                if (user2 != null && this.selectAlertString != null) {
                    str5 = LocaleController.getString("SendMessageTitle", NUM);
                    str4 = LocaleController.formatStringSimple(this.selectAlertString, UserObject.getUserName(user2));
                    str2 = LocaleController.getString("Send", NUM);
                } else {
                    return;
                }
            } else {
                TLRPC$Chat chat2 = getMessagesController().getChat(Integer.valueOf(-i3));
                if (chat2 != null) {
                    if (this.addToGroupAlertString != null) {
                        str5 = LocaleController.getString("AddToTheGroupAlertTitle", NUM);
                        str4 = LocaleController.formatStringSimple(this.addToGroupAlertString, chat2.title);
                        str2 = LocaleController.getString("Add", NUM);
                    } else {
                        str5 = LocaleController.getString("SendMessageTitle", NUM);
                        str4 = LocaleController.formatStringSimple(this.selectAlertStringGroup, chat2.title);
                        str2 = LocaleController.getString("Send", NUM);
                    }
                } else {
                    return;
                }
            }
            String str6 = str5;
            str3 = str4;
            str = str6;
            builder3.setTitle(str);
            builder3.setMessage(AndroidUtilities.replaceTags(str3));
            builder3.setPositiveButton(str2, new DialogInterface.OnClickListener(j) {
                public final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    DialogsActivity.this.lambda$didSelectResult$32$DialogsActivity(this.f$1, dialogInterface, i);
                }
            });
            builder3.setNegativeButton(LocaleController.getString("Cancel", NUM), (DialogInterface.OnClickListener) null);
            showDialog(builder3.create());
        }
    }

    public /* synthetic */ void lambda$didSelectResult$32$DialogsActivity(long j, DialogInterface dialogInterface, int i) {
        didSelectResult(j, false, false);
    }

    public ArrayList<ThemeDescription> getThemeDescriptions() {
        RecyclerListView recyclerListView;
        RecyclerListView recyclerListView2;
        $$Lambda$DialogsActivity$MBqOohoaWZLO4_eVzbVCwtaKoeA r10 = new ThemeDescription.ThemeDescriptionDelegate() {
            public final void didSetColor() {
                DialogsActivity.this.lambda$getThemeDescriptions$33$DialogsActivity();
            }
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        if (this.movingView != null) {
            arrayList.add(new ThemeDescription(this.movingView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
        }
        if (this.doneItem != null) {
            arrayList.add(new ThemeDescription(this.doneItem, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSelector"));
        }
        char c = 0;
        if (this.folderId == 0) {
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
            arrayList.add(new ThemeDescription(this.searchListView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, r10, "actionBarDefaultIcon"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, new Drawable[]{Theme.dialogs_holidayDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultTitle"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSelector"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSearch"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSearchPlaceholder"));
        } else {
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchived"));
            arrayList.add(new ThemeDescription(this.searchListView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchived"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchivedIcon"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, (Class[]) null, (Paint) null, new Drawable[]{Theme.dialogs_holidayDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchivedTitle"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchivedSelector"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchivedSearch"));
            arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultSearchArchivedPlaceholder"));
        }
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefaultIcon"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefault"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefaultTop"));
        arrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefaultSelector"));
        arrayList.add(new ThemeDescription(this.selectedDialogsCountTextView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefaultIcon"));
        $$Lambda$DialogsActivity$MBqOohoaWZLO4_eVzbVCwtaKoeA r7 = r10;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "actionBarDefaultSubmenuBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "actionBarDefaultSubmenuItem"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "actionBarDefaultSubmenuItemIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r7, "dialogButtonSelector"));
        if (this.filterTabsView != null) {
            if (this.actionBar.isActionModeShowed()) {
                arrayList.add(new ThemeDescription(this.filterTabsView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarActionModeDefault"));
                arrayList.add(new ThemeDescription((View) this.filterTabsView, 0, new Class[]{FilterTabsView.class}, new String[]{"selectorDrawable"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_tabSelectedLine"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_tabSelectedText"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_tabText"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "profile_tabSelector"));
            } else {
                arrayList.add(new ThemeDescription(this.filterTabsView, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
                arrayList.add(new ThemeDescription((View) this.filterTabsView, 0, new Class[]{FilterTabsView.class}, new String[]{"selectorDrawable"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarTabLine"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarTabActiveText"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarTabUnactiveText"));
                arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarTabSelector"));
            }
            arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), 0, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_tabUnreadActiveBackground"));
            arrayList.add(new ThemeDescription(this.filterTabsView.getTabsContainer(), 0, new Class[]{FilterTabsView.TabView.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_tabUnreadUnactiveBackground"));
        }
        arrayList.add(new ThemeDescription(this.searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "emptyListPlaceholder"));
        arrayList.add(new ThemeDescription(this.searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "progressCircle"));
        arrayList.add(new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_IMAGECOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionIcon"));
        arrayList.add(new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionBackground"));
        arrayList.add(new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_DRAWABLESELECTEDSTATE | ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionPressedBackground"));
        int i = 0;
        while (true) {
            recyclerListView = null;
            if (i >= 3) {
                break;
            }
            if (i == 2) {
                recyclerListView2 = this.searchListView;
            } else {
                ViewPage[] viewPageArr = this.viewPages;
                if (viewPageArr == null) {
                    i++;
                } else {
                    if (i < viewPageArr.length) {
                        recyclerListView = viewPageArr[i].listView;
                    }
                    recyclerListView2 = recyclerListView;
                }
            }
            if (recyclerListView2 != null) {
                RecyclerListView recyclerListView3 = recyclerListView2;
                arrayList.add(new ThemeDescription(recyclerListView3, ThemeDescription.FLAG_SELECTOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "listSelectorSDK21"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{View.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, Theme.avatarDrawables, (ThemeDescription.ThemeDescriptionDelegate) null, "avatar_text"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounter"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterMuted"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterText"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_lockDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_secretIcon"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameIcon"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_scamDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_draft"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_pinnedDrawable, Theme.dialogs_reorderDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_pinnedIcon"));
                TextPaint[] textPaintArr = Theme.dialogs_namePaint;
                arrayList.add(new ThemeDescription((View) recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (String[]) null, new Paint[]{textPaintArr[0], textPaintArr[1], Theme.dialogs_searchNamePaint}, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_name"));
                TextPaint[] textPaintArr2 = Theme.dialogs_nameEncryptedPaint;
                arrayList.add(new ThemeDescription((View) recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (String[]) null, new Paint[]{textPaintArr2[0], textPaintArr2[1], Theme.dialogs_searchNameEncryptedPaint}, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_secretName"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[1], (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message_threeLines"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[0], (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_messageNamePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameMessage_threeLines"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_draft"));
                arrayList.add(new ThemeDescription((View) recyclerListView3, 0, new Class[]{DialogCell.class}, (String[]) null, (Paint[]) Theme.dialogs_messagePrintingPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_actionMessage"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_date"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_pinnedOverlay"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_tabletSelectedOverlay"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_checkDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentCheck"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_checkReadDrawable, Theme.dialogs_halfCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentReadCheck"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_clockDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentClock"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentError"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_errorDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_sentErrorIcon"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_verifiedCheck"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_verifiedDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_verifiedBackground"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_muteDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_muteIcon"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, new Drawable[]{Theme.dialogs_mentionDrawable}, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_mentionIcon"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_archivePinBackground"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_archiveBackground"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_onlineCircle"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{DialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
                arrayList.add(new ThemeDescription((View) recyclerListView3, ThemeDescription.FLAG_CHECKBOX, new Class[]{DialogCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhite"));
                arrayList.add(new ThemeDescription((View) recyclerListView3, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{DialogCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "checkboxCheck"));
                arrayList.add(new ThemeDescription((View) recyclerListView3, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "progressCircle"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteGrayText3"));
                arrayList.add(new ThemeDescription(recyclerListView3, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText3"));
                GraySectionCell.createThemeDescriptions(arrayList, recyclerListView2);
                arrayList.add(new ThemeDescription(recyclerListView3, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                arrayList.add(new ThemeDescription(recyclerListView3, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGrayShadow"));
                arrayList.add(new ThemeDescription(recyclerListView3, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundGray"));
                arrayList.add(new ThemeDescription((View) recyclerListView3, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlueText2"));
            }
            i++;
        }
        $$Lambda$DialogsActivity$MBqOohoaWZLO4_eVzbVCwtaKoeA r72 = r10;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundRed"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundOrange"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundViolet"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundGreen"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundCyan"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundPink"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundSaved"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundArchived"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "avatar_backgroundArchivedHidden"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_nameMessage"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_draft"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_attachMessage"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_nameArchived"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_nameMessageArchived"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_nameMessageArchived_threeLines"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r72, "chats_messageArchived"));
        if (this.viewPages != null) {
            int i2 = 0;
            while (i2 < this.viewPages.length) {
                if (this.folderId == 0) {
                    arrayList.add(new ThemeDescription(this.viewPages[i2].listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefault"));
                } else {
                    arrayList.add(new ThemeDescription(this.viewPages[i2].listView, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchived"));
                }
                DialogsRecyclerView access$6700 = this.viewPages[i2].listView;
                int i3 = ThemeDescription.FLAG_TEXTCOLOR;
                Class[] clsArr = new Class[1];
                clsArr[c] = DialogsEmptyCell.class;
                String[] strArr = new String[1];
                strArr[c] = "emptyTextView1";
                arrayList.add(new ThemeDescription((View) access$6700, i3, clsArr, strArr, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameMessage_threeLines"));
                DialogsRecyclerView access$67002 = this.viewPages[i2].listView;
                int i4 = ThemeDescription.FLAG_TEXTCOLOR;
                Class[] clsArr2 = new Class[1];
                clsArr2[c] = DialogsEmptyCell.class;
                String[] strArr2 = new String[1];
                strArr2[c] = "emptyTextView2";
                arrayList.add(new ThemeDescription((View) access$67002, i4, clsArr2, strArr2, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message"));
                if (SharedConfig.archiveHidden) {
                    DialogsRecyclerView access$67003 = this.viewPages[i2].listView;
                    Class[] clsArr3 = new Class[1];
                    clsArr3[c] = DialogCell.class;
                    RLottieDrawable[] rLottieDrawableArr = new RLottieDrawable[1];
                    rLottieDrawableArr[c] = Theme.dialogs_archiveAvatarDrawable;
                    arrayList.add(new ThemeDescription((View) access$67003, 0, clsArr3, rLottieDrawableArr, "Arrow1", "avatar_backgroundArchivedHidden"));
                    DialogsRecyclerView access$67004 = this.viewPages[i2].listView;
                    Class[] clsArr4 = new Class[1];
                    clsArr4[c] = DialogCell.class;
                    RLottieDrawable[] rLottieDrawableArr2 = new RLottieDrawable[1];
                    rLottieDrawableArr2[c] = Theme.dialogs_archiveAvatarDrawable;
                    arrayList.add(new ThemeDescription((View) access$67004, 0, clsArr4, rLottieDrawableArr2, "Arrow2", "avatar_backgroundArchivedHidden"));
                } else {
                    DialogsRecyclerView access$67005 = this.viewPages[i2].listView;
                    Class[] clsArr5 = new Class[1];
                    clsArr5[c] = DialogCell.class;
                    RLottieDrawable[] rLottieDrawableArr3 = new RLottieDrawable[1];
                    rLottieDrawableArr3[c] = Theme.dialogs_archiveAvatarDrawable;
                    arrayList.add(new ThemeDescription((View) access$67005, 0, clsArr5, rLottieDrawableArr3, "Arrow1", "avatar_backgroundArchived"));
                    DialogsRecyclerView access$67006 = this.viewPages[i2].listView;
                    Class[] clsArr6 = new Class[1];
                    clsArr6[c] = DialogCell.class;
                    RLottieDrawable[] rLottieDrawableArr4 = new RLottieDrawable[1];
                    rLottieDrawableArr4[c] = Theme.dialogs_archiveAvatarDrawable;
                    arrayList.add(new ThemeDescription((View) access$67006, 0, clsArr6, rLottieDrawableArr4, "Arrow2", "avatar_backgroundArchived"));
                }
                DialogsRecyclerView access$67007 = this.viewPages[i2].listView;
                Class[] clsArr7 = new Class[1];
                clsArr7[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr5 = new RLottieDrawable[1];
                rLottieDrawableArr5[c] = Theme.dialogs_archiveAvatarDrawable;
                arrayList.add(new ThemeDescription((View) access$67007, 0, clsArr7, rLottieDrawableArr5, "Box2", "avatar_text"));
                DialogsRecyclerView access$67008 = this.viewPages[i2].listView;
                Class[] clsArr8 = new Class[1];
                clsArr8[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr6 = new RLottieDrawable[1];
                rLottieDrawableArr6[c] = Theme.dialogs_archiveAvatarDrawable;
                arrayList.add(new ThemeDescription((View) access$67008, 0, clsArr8, rLottieDrawableArr6, "Box1", "avatar_text"));
                DialogsRecyclerView access$67009 = this.viewPages[i2].listView;
                Class[] clsArr9 = new Class[1];
                clsArr9[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr7 = new RLottieDrawable[1];
                rLottieDrawableArr7[c] = Theme.dialogs_pinArchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$67009, 0, clsArr9, rLottieDrawableArr7, "Arrow", "chats_archiveIcon"));
                DialogsRecyclerView access$670010 = this.viewPages[i2].listView;
                Class[] clsArr10 = new Class[1];
                clsArr10[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr8 = new RLottieDrawable[1];
                rLottieDrawableArr8[c] = Theme.dialogs_pinArchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670010, 0, clsArr10, rLottieDrawableArr8, "Line", "chats_archiveIcon"));
                DialogsRecyclerView access$670011 = this.viewPages[i2].listView;
                Class[] clsArr11 = new Class[1];
                clsArr11[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr9 = new RLottieDrawable[1];
                rLottieDrawableArr9[c] = Theme.dialogs_unpinArchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670011, 0, clsArr11, rLottieDrawableArr9, "Arrow", "chats_archiveIcon"));
                DialogsRecyclerView access$670012 = this.viewPages[i2].listView;
                Class[] clsArr12 = new Class[1];
                clsArr12[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr10 = new RLottieDrawable[1];
                rLottieDrawableArr10[c] = Theme.dialogs_unpinArchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670012, 0, clsArr12, rLottieDrawableArr10, "Line", "chats_archiveIcon"));
                DialogsRecyclerView access$670013 = this.viewPages[i2].listView;
                Class[] clsArr13 = new Class[1];
                clsArr13[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr11 = new RLottieDrawable[1];
                rLottieDrawableArr11[c] = Theme.dialogs_archiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670013, 0, clsArr13, rLottieDrawableArr11, "Arrow", "chats_archiveBackground"));
                DialogsRecyclerView access$670014 = this.viewPages[i2].listView;
                Class[] clsArr14 = new Class[1];
                clsArr14[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr12 = new RLottieDrawable[1];
                rLottieDrawableArr12[c] = Theme.dialogs_archiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670014, 0, clsArr14, rLottieDrawableArr12, "Box2", "chats_archiveIcon"));
                DialogsRecyclerView access$670015 = this.viewPages[i2].listView;
                Class[] clsArr15 = new Class[1];
                clsArr15[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr13 = new RLottieDrawable[1];
                rLottieDrawableArr13[c] = Theme.dialogs_archiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670015, 0, clsArr15, rLottieDrawableArr13, "Box1", "chats_archiveIcon"));
                DialogsRecyclerView access$670016 = this.viewPages[i2].listView;
                Class[] clsArr16 = new Class[1];
                clsArr16[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr14 = new RLottieDrawable[1];
                rLottieDrawableArr14[c] = Theme.dialogs_hidePsaDrawable;
                arrayList.add(new ThemeDescription((View) access$670016, 0, clsArr16, rLottieDrawableArr14, "Line 1", "chats_archiveBackground"));
                DialogsRecyclerView access$670017 = this.viewPages[i2].listView;
                Class[] clsArr17 = new Class[1];
                clsArr17[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr15 = new RLottieDrawable[1];
                rLottieDrawableArr15[c] = Theme.dialogs_hidePsaDrawable;
                arrayList.add(new ThemeDescription((View) access$670017, 0, clsArr17, rLottieDrawableArr15, "Line 2", "chats_archiveBackground"));
                DialogsRecyclerView access$670018 = this.viewPages[i2].listView;
                Class[] clsArr18 = new Class[1];
                clsArr18[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr16 = new RLottieDrawable[1];
                rLottieDrawableArr16[c] = Theme.dialogs_hidePsaDrawable;
                arrayList.add(new ThemeDescription((View) access$670018, 0, clsArr18, rLottieDrawableArr16, "Line 3", "chats_archiveBackground"));
                DialogsRecyclerView access$670019 = this.viewPages[i2].listView;
                Class[] clsArr19 = new Class[1];
                clsArr19[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr17 = new RLottieDrawable[1];
                rLottieDrawableArr17[c] = Theme.dialogs_hidePsaDrawable;
                arrayList.add(new ThemeDescription((View) access$670019, 0, clsArr19, rLottieDrawableArr17, "Cup Red", "chats_archiveIcon"));
                DialogsRecyclerView access$670020 = this.viewPages[i2].listView;
                Class[] clsArr20 = new Class[1];
                clsArr20[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr18 = new RLottieDrawable[1];
                rLottieDrawableArr18[c] = Theme.dialogs_hidePsaDrawable;
                arrayList.add(new ThemeDescription((View) access$670020, 0, clsArr20, rLottieDrawableArr18, "Box", "chats_archiveIcon"));
                DialogsRecyclerView access$670021 = this.viewPages[i2].listView;
                Class[] clsArr21 = new Class[1];
                clsArr21[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr19 = new RLottieDrawable[1];
                rLottieDrawableArr19[c] = Theme.dialogs_unarchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670021, 0, clsArr21, rLottieDrawableArr19, "Arrow1", "chats_archiveIcon"));
                DialogsRecyclerView access$670022 = this.viewPages[i2].listView;
                Class[] clsArr22 = new Class[1];
                clsArr22[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr20 = new RLottieDrawable[1];
                rLottieDrawableArr20[c] = Theme.dialogs_unarchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670022, 0, clsArr22, rLottieDrawableArr20, "Arrow2", "chats_archivePinBackground"));
                DialogsRecyclerView access$670023 = this.viewPages[i2].listView;
                Class[] clsArr23 = new Class[1];
                clsArr23[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr21 = new RLottieDrawable[1];
                rLottieDrawableArr21[c] = Theme.dialogs_unarchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670023, 0, clsArr23, rLottieDrawableArr21, "Box2", "chats_archiveIcon"));
                DialogsRecyclerView access$670024 = this.viewPages[i2].listView;
                Class[] clsArr24 = new Class[1];
                clsArr24[c] = DialogCell.class;
                RLottieDrawable[] rLottieDrawableArr22 = new RLottieDrawable[1];
                rLottieDrawableArr22[c] = Theme.dialogs_unarchiveDrawable;
                arrayList.add(new ThemeDescription((View) access$670024, 0, clsArr24, rLottieDrawableArr22, "Box1", "chats_archiveIcon"));
                DialogsRecyclerView access$670025 = this.viewPages[i2].listView;
                Class[] clsArr25 = new Class[1];
                clsArr25[c] = UserCell.class;
                String[] strArr3 = new String[1];
                strArr3[c] = "nameTextView";
                arrayList.add(new ThemeDescription((View) access$670025, 0, clsArr25, strArr3, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
                DialogsRecyclerView access$670026 = this.viewPages[i2].listView;
                Class[] clsArr26 = new Class[1];
                clsArr26[c] = UserCell.class;
                String[] strArr4 = new String[1];
                strArr4[c] = "statusColor";
                ThemeDescription themeDescription = r1;
                int i5 = i2;
                ThemeDescription themeDescription2 = new ThemeDescription((View) access$670026, 0, clsArr26, strArr4, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r10, "windowBackgroundWhiteGrayText");
                arrayList.add(themeDescription);
                arrayList.add(new ThemeDescription((View) this.viewPages[i5].listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) r10, "windowBackgroundWhiteBlueText"));
                arrayList.add(new ThemeDescription(this.viewPages[i5].progressView, ThemeDescription.FLAG_PROGRESSBAR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "progressCircle"));
                ViewPager archiveHintCellPager = this.viewPages[i5].dialogsAdapter.getArchiveHintCellPager();
                ViewPager viewPager = archiveHintCellPager;
                arrayList.add(new ThemeDescription((View) viewPager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"imageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameMessage_threeLines"));
                arrayList.add(new ThemeDescription((View) viewPager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"imageView2"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounter"));
                arrayList.add(new ThemeDescription((View) viewPager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"headerTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_nameMessage_threeLines"));
                arrayList.add(new ThemeDescription((View) viewPager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"messageTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_message"));
                arrayList.add(new ThemeDescription(archiveHintCellPager, ThemeDescription.FLAG_LISTGLOWCOLOR, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "actionBarDefaultArchived"));
                i2 = i5 + 1;
                c = 0;
            }
        }
        $$Lambda$DialogsActivity$MBqOohoaWZLO4_eVzbVCwtaKoeA r73 = r10;
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r73, "chats_archivePullDownBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, r73, "chats_archivePullDownBackgroundActive"));
        arrayList.add(new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_BACKGROUND, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuBackground"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuName"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuPhone"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuPhoneCats"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuCloudBackgroundCats"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chat_serviceBackground"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuTopShadow"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuTopShadowCats"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerProfileCell.class}, new String[]{"darkThemeView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuName"));
        arrayList.add(new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, r73, "chats_menuTopBackgroundCats"));
        arrayList.add(new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{DrawerProfileCell.class}, (Paint) null, (Drawable[]) null, r73, "chats_menuTopBackground"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuItemIcon"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuItemText"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, 0, new Class[]{DrawerUserCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuItemText"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterText"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounter"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuBackground"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuItemIcon"));
        arrayList.add(new ThemeDescription((View) this.sideMenu, 0, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_menuItemText"));
        arrayList.add(new ThemeDescription(this.sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "divider"));
        DialogsSearchAdapter dialogsSearchAdapter2 = this.dialogsSearchAdapter;
        arrayList.add(new ThemeDescription(dialogsSearchAdapter2 != null ? dialogsSearchAdapter2.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounter"));
        DialogsSearchAdapter dialogsSearchAdapter3 = this.dialogsSearchAdapter;
        arrayList.add(new ThemeDescription(dialogsSearchAdapter3 != null ? dialogsSearchAdapter3.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterMuted"));
        DialogsSearchAdapter dialogsSearchAdapter4 = this.dialogsSearchAdapter;
        arrayList.add(new ThemeDescription(dialogsSearchAdapter4 != null ? dialogsSearchAdapter4.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_unreadCounterText"));
        DialogsSearchAdapter dialogsSearchAdapter5 = this.dialogsSearchAdapter;
        arrayList.add(new ThemeDescription(dialogsSearchAdapter5 != null ? dialogsSearchAdapter5.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_archiveTextPaint, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_archiveText"));
        DialogsSearchAdapter dialogsSearchAdapter6 = this.dialogsSearchAdapter;
        arrayList.add(new ThemeDescription((View) dialogsSearchAdapter6 != null ? dialogsSearchAdapter6.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "windowBackgroundWhiteBlackText"));
        DialogsSearchAdapter dialogsSearchAdapter7 = this.dialogsSearchAdapter;
        if (dialogsSearchAdapter7 != null) {
            recyclerListView = dialogsSearchAdapter7.getInnerListView();
        }
        arrayList.add(new ThemeDescription(recyclerListView, 0, new Class[]{HintDialogCell.class}, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "chats_onlineCircle"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerBackground"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerPlayPause"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerTitle"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_FASTSCROLL, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerPerformer"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "inappPlayerClose"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "returnToCallBackground"));
        arrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "returnToCallText"));
        for (int i6 = 0; i6 < this.undoView.length; i6++) {
            arrayList.add(new ThemeDescription(this.undoView[i6], ThemeDescription.FLAG_BACKGROUNDFILTER, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_background"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_cancelColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_cancelColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"subinfoTextView"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, (Paint[]) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "info1", "undo_background"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "info2", "undo_background"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luCLASSNAME", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luCLASSNAME", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luCLASSNAME", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc9", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc8", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc7", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc6", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc5", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc4", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc3", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc2", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc1", "undo_infoColor"));
            arrayList.add(new ThemeDescription((View) this.undoView[i6], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "Oval", "undo_infoColor"));
        }
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBackgroundGray"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextBlack"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextLink"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogLinkSelection"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextBlue"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextBlue2"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextBlue3"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextBlue4"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextRed"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextRed2"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextGray"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextGray2"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextGray3"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextGray4"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogRedIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogTextHint"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogInputField"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogInputFieldActivated"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogCheckboxSquareBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogCheckboxSquareCheck"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogCheckboxSquareUnchecked"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogCheckboxSquareDisabled"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogRadioBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogRadioBackgroundChecked"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogProgressCircle"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogButton"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogButtonSelector"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogScrollGlow"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogRoundCheckBox"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogRoundCheckBoxCheck"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBadgeBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogBadgeText"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogLineProgress"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogLineProgressBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogGrayLine"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialog_inlineProgressBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialog_inlineProgress"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchHint"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogSearchText"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogFloatingButton"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogFloatingIcon"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "dialogShadowLine"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_sheet_scrollUp"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_sheet_other"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBar"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarSelector"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTitle"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarTop"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarSubtitle"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_actionBarItems"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_background"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_time"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progressBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "key_player_progressCachedBackground"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_progress"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_button"));
        arrayList.add(new ThemeDescription((View) null, 0, (Class[]) null, (Paint) null, (Drawable[]) null, (ThemeDescription.ThemeDescriptionDelegate) null, "player_buttonActive"));
        return arrayList;
    }

    public /* synthetic */ void lambda$getThemeDescriptions$33$DialogsActivity() {
        RecyclerListView innerListView;
        ViewGroup viewGroup;
        int i = 0;
        int i2 = 0;
        while (i2 < 3) {
            if (i2 == 2) {
                viewGroup = this.searchListView;
            } else {
                ViewPage[] viewPageArr = this.viewPages;
                if (viewPageArr == null) {
                    i2++;
                } else {
                    viewGroup = i2 < viewPageArr.length ? viewPageArr[i2].listView : null;
                }
            }
            if (viewGroup != null) {
                int childCount = viewGroup.getChildCount();
                for (int i3 = 0; i3 < childCount; i3++) {
                    View childAt = viewGroup.getChildAt(i3);
                    if (childAt instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) childAt).update(0);
                    } else if (childAt instanceof DialogCell) {
                        ((DialogCell) childAt).update(0);
                    } else if (childAt instanceof UserCell) {
                        ((UserCell) childAt).update(0);
                    }
                }
            }
            i2++;
        }
        DialogsSearchAdapter dialogsSearchAdapter2 = this.dialogsSearchAdapter;
        if (!(dialogsSearchAdapter2 == null || (innerListView = dialogsSearchAdapter2.getInnerListView()) == null)) {
            int childCount2 = innerListView.getChildCount();
            for (int i4 = 0; i4 < childCount2; i4++) {
                View childAt2 = innerListView.getChildAt(i4);
                if (childAt2 instanceof HintDialogCell) {
                    ((HintDialogCell) childAt2).update();
                }
            }
        }
        RecyclerView recyclerView = this.sideMenu;
        if (recyclerView != null) {
            View childAt3 = recyclerView.getChildAt(0);
            if (childAt3 instanceof DrawerProfileCell) {
                DrawerProfileCell drawerProfileCell = (DrawerProfileCell) childAt3;
                drawerProfileCell.applyBackground(true);
                drawerProfileCell.updateColors();
            }
        }
        if (this.viewPages != null) {
            int i5 = 0;
            while (true) {
                ViewPage[] viewPageArr2 = this.viewPages;
                if (i5 >= viewPageArr2.length) {
                    break;
                }
                if (viewPageArr2[i5].pullForegroundDrawable != null) {
                    this.viewPages[i5].pullForegroundDrawable.updateColors();
                }
                i5++;
            }
        }
        ActionBar actionBar = this.actionBar;
        if (actionBar != null) {
            actionBar.setPopupBackgroundColor(Theme.getColor("actionBarDefaultSubmenuBackground"), true);
            this.actionBar.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItem"), false, true);
            this.actionBar.setPopupItemsColor(Theme.getColor("actionBarDefaultSubmenuItemIcon"), true, true);
            this.actionBar.setPopupItemsSelectorColor(Theme.getColor("dialogButtonSelector"), true);
        }
        if (this.scrimPopupWindowItems != null) {
            while (true) {
                ActionBarMenuSubItem[] actionBarMenuSubItemArr = this.scrimPopupWindowItems;
                if (i >= actionBarMenuSubItemArr.length) {
                    break;
                }
                actionBarMenuSubItemArr[i].setColors(Theme.getColor("actionBarDefaultSubmenuItem"), Theme.getColor("actionBarDefaultSubmenuItemIcon"));
                this.scrimPopupWindowItems[i].setSelectorColor(Theme.getColor("dialogButtonSelector"));
                i++;
            }
        }
        ActionBarPopupWindow actionBarPopupWindow = this.scrimPopupWindow;
        if (actionBarPopupWindow != null) {
            View contentView = actionBarPopupWindow.getContentView();
            contentView.setBackgroundColor(Theme.getColor("actionBarDefaultSubmenuBackground"));
            contentView.invalidate();
        }
        ActionBarMenuItem actionBarMenuItem = this.doneItem;
        if (actionBarMenuItem != null) {
            actionBarMenuItem.setIconColor(Theme.getColor("actionBarDefaultIcon"));
        }
    }
}
