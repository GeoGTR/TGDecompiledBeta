package org.telegram.p005ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build.VERSION;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.support.p000v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.EmojiData;
import org.telegram.messenger.EmojiSuggestion;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.support.widget.GridLayoutManager;
import org.telegram.messenger.support.widget.GridLayoutManager.SpanSizeLookup;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.p005ui.ActionBar.AlertDialog.Builder;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Cells.ContextLinkCell;
import org.telegram.p005ui.Cells.EmptyCell;
import org.telegram.p005ui.Cells.FeaturedStickerSetInfoCell;
import org.telegram.p005ui.Cells.StickerEmojiCell;
import org.telegram.p005ui.Cells.StickerSetGroupInfoCell;
import org.telegram.p005ui.Cells.StickerSetNameCell;
import org.telegram.p005ui.Components.PagerSlidingTabStrip.IconTabProvider;
import org.telegram.p005ui.Components.RecyclerListView.Holder;
import org.telegram.p005ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.p005ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.p005ui.StickerPreviewViewer;
import org.telegram.p005ui.StickerPreviewViewer.StickerPreviewViewerDelegate;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_foundStickerSets;
import org.telegram.tgnet.TLRPC.TL_messages_getStickers;
import org.telegram.tgnet.TLRPC.TL_messages_searchStickerSets;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickers;

/* renamed from: org.telegram.ui.Components.EmojiView */
public class EmojiView extends FrameLayout implements NotificationCenterDelegate {
    private static final OnScrollChangedListener NOP = EmojiView$$Lambda$11.$instance;
    private static final Field superListenerField;
    private ArrayList<EmojiGridAdapter> adapters = new ArrayList();
    private ImageView backspaceButton;
    private boolean backspaceOnce;
    private boolean backspacePressed;
    private ImageView clearSearchImageView;
    private int currentAccount = UserConfig.selectedAccount;
    private int currentBackgroundType = -1;
    private int currentChatId;
    private int currentPage;
    private Paint dotPaint;
    private DragListener dragListener;
    private ArrayList<GridView> emojiGrids = new ArrayList();
    private int emojiSize;
    private LinearLayout emojiTab;
    private int favTabBum = -2;
    private ArrayList<Document> favouriteStickers = new ArrayList();
    private int featuredStickersHash;
    private boolean firstAttach = true;
    private ExtendedGridLayoutManager flowLayoutManager;
    private boolean forseMultiwindowLayout;
    private int gifTabNum = -2;
    private GifsAdapter gifsAdapter;
    private RecyclerListView gifsGridView;
    private int groupStickerPackNum;
    private int groupStickerPackPosition;
    private TL_messages_stickerSet groupStickerSet;
    private boolean groupStickersHidden;
    private Drawable[] icons;
    private ChatFull info;
    private LongSparseArray<StickerSetCovered> installingStickerSets = new LongSparseArray();
    private boolean isLayout;
    private int lastNotifyWidth;
    private Listener listener;
    private int[] location = new int[2];
    private TextView mediaBanTooltip;
    private int minusDy;
    private TextView noRecentTextView;
    private int oldWidth;
    private Object outlineProvider;
    private ViewPager pager;
    private PagerSlidingTabStrip pagerSlidingTabStrip;
    private EmojiColorPickerView pickerView;
    private EmojiPopupWindow pickerViewPopup;
    private int popupHeight;
    private int popupWidth;
    private CloseProgressDrawable2 progressDrawable;
    private ArrayList<Document> recentGifs = new ArrayList();
    private ArrayList<Document> recentStickers = new ArrayList();
    private int recentTabBum = -2;
    private LongSparseArray<StickerSetCovered> removingStickerSets = new LongSparseArray();
    private AnimatorSet searchAnimation;
    private View searchBackground;
    private EditTextBoldCursor searchEditText;
    private FrameLayout searchEditTextContainer;
    private int searchFieldHeight = AndroidUtilities.m9dp(64.0f);
    private ImageView searchIconImageView;
    private View shadowLine;
    private boolean showGifs;
    private StickerPreviewViewerDelegate stickerPreviewViewerDelegate = new CLASSNAME();
    private ArrayList<TL_messages_stickerSet> stickerSets = new ArrayList();
    private TextView stickersCounter;
    private Drawable stickersDrawable;
    private TextView stickersEmptyView;
    private StickersGridAdapter stickersGridAdapter;
    private RecyclerListView stickersGridView;
    private GridLayoutManager stickersLayoutManager;
    private OnItemClickListener stickersOnItemClickListener;
    private StickersSearchGridAdapter stickersSearchGridAdapter;
    private ScrollSlidingTabStrip stickersTab;
    private int stickersTabOffset;
    private FrameLayout stickersWrap;
    private boolean switchToGifTab;
    private TrendingGridAdapter trendingGridAdapter;
    private RecyclerListView trendingGridView;
    private GridLayoutManager trendingLayoutManager;
    private boolean trendingLoaded;
    private int trendingTabNum = -2;
    private ArrayList<View> views = new ArrayList();

    /* renamed from: org.telegram.ui.Components.EmojiView$20 */
    class CLASSNAME extends AnimatorListenerAdapter {
        CLASSNAME() {
        }

        public void onAnimationEnd(Animator animation) {
            if (animation.equals(EmojiView.this.searchAnimation)) {
                int pos = EmojiView.this.stickersLayoutManager.findFirstVisibleItemPosition();
                int firstVisPos = EmojiView.this.stickersLayoutManager.findFirstVisibleItemPosition();
                int top = 0;
                if (firstVisPos != -1) {
                    top = (int) (((float) EmojiView.this.stickersLayoutManager.findViewByPosition(firstVisPos).getTop()) + EmojiView.this.stickersGridView.getTranslationY());
                }
                EmojiView.this.stickersGridView.setTranslationY(0.0f);
                EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.m9dp(52.0f), 0, 0);
                if (firstVisPos != -1) {
                    EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(firstVisPos, top - EmojiView.this.stickersGridView.getPaddingTop());
                }
                EmojiView.this.searchAnimation = null;
            }
        }

        public void onAnimationCancel(Animator animation) {
            if (animation.equals(EmojiView.this.searchAnimation)) {
                EmojiView.this.searchAnimation = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$21 */
    class CLASSNAME extends AnimatorListenerAdapter {

        /* renamed from: org.telegram.ui.Components.EmojiView$21$1 */
        class CLASSNAME extends AnimatorListenerAdapter {
            CLASSNAME() {
            }

            public void onAnimationEnd(Animator animation1) {
                if (EmojiView.this.mediaBanTooltip != null) {
                    EmojiView.this.mediaBanTooltip.setVisibility(4);
                }
            }
        }

        CLASSNAME() {
        }

        public void onAnimationEnd(Animator animation) {
            AndroidUtilities.runOnUIThread(new EmojiView$21$$Lambda$0(this), DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
        }

        final /* synthetic */ void lambda$onAnimationEnd$0$EmojiView$21() {
            if (EmojiView.this.mediaBanTooltip != null) {
                AnimatorSet AnimatorSet1 = new AnimatorSet();
                Animator[] animatorArr = new Animator[1];
                animatorArr[0] = ObjectAnimator.ofFloat(EmojiView.this.mediaBanTooltip, "alpha", new float[]{0.0f});
                AnimatorSet1.playTogether(animatorArr);
                AnimatorSet1.addListener(new CLASSNAME());
                AnimatorSet1.setDuration(300);
                AnimatorSet1.start();
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$2 */
    class CLASSNAME extends ViewOutlineProvider {
        CLASSNAME() {
        }

        @TargetApi(21)
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(view.getPaddingLeft(), view.getPaddingTop(), view.getMeasuredWidth() - view.getPaddingRight(), view.getMeasuredHeight() - view.getPaddingBottom(), (float) AndroidUtilities.m9dp(6.0f));
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$6 */
    class CLASSNAME implements TextWatcher {
        CLASSNAME() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            boolean show;
            boolean showed;
            float f = 1.0f;
            if (EmojiView.this.searchEditText.length() > 0) {
                show = true;
            } else {
                show = false;
            }
            if (EmojiView.this.clearSearchImageView.getAlpha() != 0.0f) {
                showed = true;
            } else {
                showed = false;
            }
            if (show != showed) {
                float f2;
                ViewPropertyAnimator animate = EmojiView.this.clearSearchImageView.animate();
                if (show) {
                    f2 = 1.0f;
                } else {
                    f2 = 0.0f;
                }
                animate = animate.alpha(f2).setDuration(150);
                if (show) {
                    f2 = 1.0f;
                } else {
                    f2 = 0.1f;
                }
                ViewPropertyAnimator scaleX = animate.scaleX(f2);
                if (!show) {
                    f = 0.1f;
                }
                scaleX.scaleY(f).start();
            }
            EmojiView.this.stickersSearchGridAdapter.search(EmojiView.this.searchEditText.getText().toString());
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$DragListener */
    public interface DragListener {
        void onDrag(int i);

        void onDragCancel();

        void onDragEnd(float f);

        void onDragStart();
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$EmojiColorPickerView */
    private class EmojiColorPickerView extends View {
        private Drawable arrowDrawable = getResources().getDrawable(R.drawable.stickers_back_arrow);
        private int arrowX;
        private Drawable backgroundDrawable = getResources().getDrawable(R.drawable.stickers_back_all);
        private String currentEmoji;
        private RectF rect = new RectF();
        private Paint rectPaint = new Paint(1);
        private int selection;

        public void setEmoji(String emoji, int arrowPosition) {
            this.currentEmoji = emoji;
            this.arrowX = arrowPosition;
            this.rectPaint.setColor(Theme.ACTION_BAR_AUDIO_SELECTOR_COLOR);
            invalidate();
        }

        public String getEmoji() {
            return this.currentEmoji;
        }

        public void setSelection(int position) {
            if (this.selection != position) {
                this.selection = position;
                invalidate();
            }
        }

        public int getSelection() {
            return this.selection;
        }

        public EmojiColorPickerView(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float f;
            this.backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 60.0f : 52.0f));
            this.backgroundDrawable.draw(canvas);
            Drawable drawable = this.arrowDrawable;
            int dp = this.arrowX - AndroidUtilities.m9dp(9.0f);
            int dp2 = AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 55.5f : 47.5f);
            int dp3 = AndroidUtilities.m9dp(9.0f) + this.arrowX;
            if (AndroidUtilities.isTablet()) {
                f = 55.5f;
            } else {
                f = 47.5f;
            }
            drawable.setBounds(dp, dp2, dp3, AndroidUtilities.m9dp(f + 8.0f));
            this.arrowDrawable.draw(canvas);
            if (this.currentEmoji != null) {
                for (int a = 0; a < 6; a++) {
                    int x = (EmojiView.this.emojiSize * a) + AndroidUtilities.m9dp((float) ((a * 4) + 5));
                    int y = AndroidUtilities.m9dp(9.0f);
                    if (this.selection == a) {
                        this.rect.set((float) x, (float) (y - ((int) AndroidUtilities.dpf2(3.5f))), (float) (EmojiView.this.emojiSize + x), (float) ((EmojiView.this.emojiSize + y) + AndroidUtilities.m9dp(3.0f)));
                        canvas.drawRoundRect(this.rect, (float) AndroidUtilities.m9dp(4.0f), (float) AndroidUtilities.m9dp(4.0f), this.rectPaint);
                    }
                    String code = this.currentEmoji;
                    if (a != 0) {
                        String color;
                        switch (a) {
                            case 1:
                                color = "\ud83c\udffb";
                                break;
                            case 2:
                                color = "\ud83c\udffc";
                                break;
                            case 3:
                                color = "\ud83c\udffd";
                                break;
                            case 4:
                                color = "\ud83c\udffe";
                                break;
                            case 5:
                                color = "\ud83c\udfff";
                                break;
                            default:
                                color = TtmlNode.ANONYMOUS_REGION_ID;
                                break;
                        }
                        code = EmojiView.addColorToCode(code, color);
                    }
                    Drawable drawable2 = Emoji.getEmojiBigDrawable(code);
                    if (drawable2 != null) {
                        drawable2.setBounds(x, y, EmojiView.this.emojiSize + x, EmojiView.this.emojiSize + y);
                        drawable2.draw(canvas);
                    }
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$EmojiGridAdapter */
    private class EmojiGridAdapter extends BaseAdapter {
        private int emojiPage;

        public EmojiGridAdapter(int page) {
            this.emojiPage = page;
        }

        public Object getItem(int position) {
            return null;
        }

        public int getCount() {
            if (this.emojiPage == -1) {
                return Emoji.recentEmoji.size();
            }
            return EmojiData.dataColored[this.emojiPage].length;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View view, ViewGroup paramViewGroup) {
            String code;
            String coloredCode;
            ImageViewEmoji imageView = (ImageViewEmoji) view;
            if (imageView == null) {
                imageView = new ImageViewEmoji(EmojiView.this.getContext());
            }
            if (this.emojiPage == -1) {
                code = (String) Emoji.recentEmoji.get(position);
                coloredCode = code;
            } else {
                code = EmojiData.dataColored[this.emojiPage][position];
                coloredCode = code;
                String color = (String) Emoji.emojiColor.get(code);
                if (color != null) {
                    coloredCode = EmojiView.addColorToCode(coloredCode, color);
                }
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(coloredCode));
            imageView.setTag(code);
            return imageView;
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$EmojiPopupWindow */
    private class EmojiPopupWindow extends PopupWindow {
        private OnScrollChangedListener mSuperScrollListener;
        private ViewTreeObserver mViewTreeObserver;

        public EmojiPopupWindow() {
            init();
        }

        public EmojiPopupWindow(Context context) {
            super(context);
            init();
        }

        public EmojiPopupWindow(int width, int height) {
            super(width, height);
            init();
        }

        public EmojiPopupWindow(View contentView) {
            super(contentView);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);
            init();
        }

        public EmojiPopupWindow(View contentView, int width, int height) {
            super(contentView, width, height);
            init();
        }

        private void init() {
            if (EmojiView.superListenerField != null) {
                try {
                    this.mSuperScrollListener = (OnScrollChangedListener) EmojiView.superListenerField.get(this);
                    EmojiView.superListenerField.set(this, EmojiView.NOP);
                } catch (Exception e) {
                    this.mSuperScrollListener = null;
                }
            }
        }

        private void unregisterListener() {
            if (this.mSuperScrollListener != null && this.mViewTreeObserver != null) {
                if (this.mViewTreeObserver.isAlive()) {
                    this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
                }
                this.mViewTreeObserver = null;
            }
        }

        private void registerListener(View anchor) {
            if (this.mSuperScrollListener != null) {
                ViewTreeObserver vto = anchor.getWindowToken() != null ? anchor.getViewTreeObserver() : null;
                if (vto != this.mViewTreeObserver) {
                    if (this.mViewTreeObserver != null && this.mViewTreeObserver.isAlive()) {
                        this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
                    }
                    this.mViewTreeObserver = vto;
                    if (vto != null) {
                        vto.addOnScrollChangedListener(this.mSuperScrollListener);
                    }
                }
            }
        }

        public void showAsDropDown(View anchor, int xoff, int yoff) {
            try {
                super.showAsDropDown(anchor, xoff, yoff);
                registerListener(anchor);
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
        }

        public void update(View anchor, int xoff, int yoff, int width, int height) {
            super.update(anchor, xoff, yoff, width, height);
            registerListener(anchor);
        }

        public void update(View anchor, int width, int height) {
            super.update(anchor, width, height);
            registerListener(anchor);
        }

        public void showAtLocation(View parent, int gravity, int x, int y) {
            super.showAtLocation(parent, gravity, x, y);
            unregisterListener();
        }

        public void dismiss() {
            setFocusable(false);
            try {
                super.dismiss();
            } catch (Exception e) {
            }
            unregisterListener();
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$ImageViewEmoji */
    private class ImageViewEmoji extends ImageView {
        private float lastX;
        private float lastY;
        private boolean touched;
        private float touchedX;
        private float touchedY;

        public ImageViewEmoji(Context context) {
            super(context);
            setOnClickListener(new EmojiView$ImageViewEmoji$$Lambda$0(this));
            setOnLongClickListener(new EmojiView$ImageViewEmoji$$Lambda$1(this));
            setBackgroundDrawable(Theme.getSelectorDrawable(false));
            setScaleType(ScaleType.CENTER);
        }

        final /* synthetic */ void lambda$new$0$EmojiView$ImageViewEmoji(View v) {
            sendEmoji(null);
        }

        final /* synthetic */ boolean lambda$new$1$EmojiView$ImageViewEmoji(View view) {
            int yOffset = 0;
            String code = (String) view.getTag();
            String color = null;
            String toCheck = code.replace("\ud83c\udffb", TtmlNode.ANONYMOUS_REGION_ID);
            if (toCheck != code) {
                color = "\ud83c\udffb";
            }
            if (color == null) {
                toCheck = code.replace("\ud83c\udffc", TtmlNode.ANONYMOUS_REGION_ID);
                if (toCheck != code) {
                    color = "\ud83c\udffc";
                }
            }
            if (color == null) {
                toCheck = code.replace("\ud83c\udffd", TtmlNode.ANONYMOUS_REGION_ID);
                if (toCheck != code) {
                    color = "\ud83c\udffd";
                }
            }
            if (color == null) {
                toCheck = code.replace("\ud83c\udffe", TtmlNode.ANONYMOUS_REGION_ID);
                if (toCheck != code) {
                    color = "\ud83c\udffe";
                }
            }
            if (color == null) {
                toCheck = code.replace("\ud83c\udfff", TtmlNode.ANONYMOUS_REGION_ID);
                if (toCheck != code) {
                    color = "\ud83c\udfff";
                }
            }
            if (EmojiData.emojiColoredMap.containsKey(toCheck)) {
                int i;
                this.touched = true;
                this.touchedX = this.lastX;
                this.touchedY = this.lastY;
                if (color == null && EmojiView.this.pager.getCurrentItem() != 0) {
                    color = (String) Emoji.emojiColor.get(toCheck);
                }
                if (color != null) {
                    i = -1;
                    switch (color.hashCode()) {
                        case 1773375:
                            if (color.equals("\ud83c\udffb")) {
                                i = 0;
                                break;
                            }
                            break;
                        case 1773376:
                            if (color.equals("\ud83c\udffc")) {
                                boolean i2 = true;
                                break;
                            }
                            break;
                        case 1773377:
                            if (color.equals("\ud83c\udffd")) {
                                i2 = 2;
                                break;
                            }
                            break;
                        case 1773378:
                            if (color.equals("\ud83c\udffe")) {
                                i2 = 3;
                                break;
                            }
                            break;
                        case 1773379:
                            if (color.equals("\ud83c\udfff")) {
                                i2 = 4;
                                break;
                            }
                            break;
                    }
                    switch (i2) {
                        case 0:
                            EmojiView.this.pickerView.setSelection(1);
                            break;
                        case 1:
                            EmojiView.this.pickerView.setSelection(2);
                            break;
                        case 2:
                            EmojiView.this.pickerView.setSelection(3);
                            break;
                        case 3:
                            EmojiView.this.pickerView.setSelection(4);
                            break;
                        case 4:
                            EmojiView.this.pickerView.setSelection(5);
                            break;
                    }
                }
                EmojiView.this.pickerView.setSelection(0);
                view.getLocationOnScreen(EmojiView.this.location);
                int selection = EmojiView.this.pickerView.getSelection() * EmojiView.this.emojiSize;
                int selection2 = EmojiView.this.pickerView.getSelection() * 4;
                if (AndroidUtilities.isTablet()) {
                    i2 = 5;
                } else {
                    i2 = 1;
                }
                int x = selection + AndroidUtilities.m9dp((float) (selection2 - i2));
                if (EmojiView.this.location[0] - x < AndroidUtilities.m9dp(5.0f)) {
                    x += (EmojiView.this.location[0] - x) - AndroidUtilities.m9dp(5.0f);
                } else if ((EmojiView.this.location[0] - x) + EmojiView.this.popupWidth > AndroidUtilities.displaySize.x - AndroidUtilities.m9dp(5.0f)) {
                    x += ((EmojiView.this.location[0] - x) + EmojiView.this.popupWidth) - (AndroidUtilities.displaySize.x - AndroidUtilities.m9dp(5.0f));
                }
                int xOffset = -x;
                if (view.getTop() < 0) {
                    yOffset = view.getTop();
                }
                EmojiView.this.pickerView.setEmoji(toCheck, (AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 30.0f : 22.0f) - xOffset) + ((int) AndroidUtilities.dpf2(0.5f)));
                EmojiView.this.pickerViewPopup.setFocusable(true);
                EmojiView.this.pickerViewPopup.showAsDropDown(view, xOffset, (((-view.getMeasuredHeight()) - EmojiView.this.popupHeight) + ((view.getMeasuredHeight() - EmojiView.this.emojiSize) / 2)) - yOffset);
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (EmojiView.this.pager.getCurrentItem() == 0) {
                EmojiView.this.listener.onClearEmojiRecent();
            }
            return false;
        }

        private void sendEmoji(String override) {
            String code;
            if (override != null) {
                code = override;
            } else {
                code = (String) getTag();
            }
            new SpannableStringBuilder().append(code);
            if (override == null) {
                if (EmojiView.this.pager.getCurrentItem() != 0) {
                    String color = (String) Emoji.emojiColor.get(code);
                    if (color != null) {
                        code = EmojiView.addColorToCode(code, color);
                    }
                }
                EmojiView.this.addEmojiToRecent(code);
                if (EmojiView.this.listener != null) {
                    EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(code));
                }
            } else if (EmojiView.this.listener != null) {
                EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(override));
            }
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (this.touched) {
                if (event.getAction() == 1 || event.getAction() == 3) {
                    if (EmojiView.this.pickerViewPopup != null && EmojiView.this.pickerViewPopup.isShowing()) {
                        EmojiView.this.pickerViewPopup.dismiss();
                        String color = null;
                        switch (EmojiView.this.pickerView.getSelection()) {
                            case 1:
                                color = "\ud83c\udffb";
                                break;
                            case 2:
                                color = "\ud83c\udffc";
                                break;
                            case 3:
                                color = "\ud83c\udffd";
                                break;
                            case 4:
                                color = "\ud83c\udffe";
                                break;
                            case 5:
                                color = "\ud83c\udfff";
                                break;
                        }
                        String code = (String) getTag();
                        if (EmojiView.this.pager.getCurrentItem() != 0) {
                            if (color != null) {
                                Emoji.emojiColor.put(code, color);
                                code = EmojiView.addColorToCode(code, color);
                            } else {
                                Emoji.emojiColor.remove(code);
                            }
                            setImageDrawable(Emoji.getEmojiBigDrawable(code));
                            sendEmoji(null);
                            Emoji.saveEmojiColors();
                        } else {
                            code = code.replace("\ud83c\udffb", TtmlNode.ANONYMOUS_REGION_ID).replace("\ud83c\udffc", TtmlNode.ANONYMOUS_REGION_ID).replace("\ud83c\udffd", TtmlNode.ANONYMOUS_REGION_ID).replace("\ud83c\udffe", TtmlNode.ANONYMOUS_REGION_ID).replace("\ud83c\udfff", TtmlNode.ANONYMOUS_REGION_ID);
                            if (color != null) {
                                sendEmoji(EmojiView.addColorToCode(code, color));
                            } else {
                                sendEmoji(code);
                            }
                        }
                    }
                    this.touched = false;
                    this.touchedX = -10000.0f;
                    this.touchedY = -10000.0f;
                } else if (event.getAction() == 2) {
                    boolean ignore = false;
                    if (this.touchedX != -10000.0f) {
                        if (Math.abs(this.touchedX - event.getX()) > AndroidUtilities.getPixelsInCM(0.2f, true) || Math.abs(this.touchedY - event.getY()) > AndroidUtilities.getPixelsInCM(0.2f, false)) {
                            this.touchedX = -10000.0f;
                            this.touchedY = -10000.0f;
                        } else {
                            ignore = true;
                        }
                    }
                    if (!ignore) {
                        getLocationOnScreen(EmojiView.this.location);
                        float x = ((float) EmojiView.this.location[0]) + event.getX();
                        EmojiView.this.pickerView.getLocationOnScreen(EmojiView.this.location);
                        int position = (int) ((x - ((float) (EmojiView.this.location[0] + AndroidUtilities.m9dp(3.0f)))) / ((float) (EmojiView.this.emojiSize + AndroidUtilities.m9dp(4.0f))));
                        if (position < 0) {
                            position = 0;
                        } else if (position > 5) {
                            position = 5;
                        }
                        EmojiView.this.pickerView.setSelection(position);
                    }
                }
            }
            this.lastX = event.getX();
            this.lastY = event.getY();
            return super.onTouchEvent(event);
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$Listener */
    public interface Listener {
        boolean isExpanded();

        boolean isSearchOpened();

        boolean onBackspace();

        void onClearEmojiRecent();

        void onEmojiSelected(String str);

        void onGifSelected(Document document, Object obj);

        void onGifTab(boolean z);

        void onSearchOpenClose(boolean z);

        void onShowStickerSet(StickerSet stickerSet, InputStickerSet inputStickerSet);

        void onStickerSelected(Document document, Object obj);

        void onStickerSetAdd(StickerSetCovered stickerSetCovered);

        void onStickerSetRemove(StickerSetCovered stickerSetCovered);

        void onStickersGroupClick(int i);

        void onStickersSettingsClick();

        void onStickersTab(boolean z);
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$11 */
    class CLASSNAME extends SpanSizeLookup {
        CLASSNAME() {
        }

        public int getSpanSize(int position) {
            return EmojiView.this.flowLayoutManager.getSpanSizeForItem(position);
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$12 */
    class CLASSNAME extends ItemDecoration {
        CLASSNAME() {
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            int i = 0;
            outRect.left = 0;
            outRect.top = 0;
            outRect.bottom = 0;
            int position = parent.getChildAdapterPosition(view);
            if (!EmojiView.this.flowLayoutManager.isFirstRow(position)) {
                outRect.top = AndroidUtilities.m9dp(2.0f);
            }
            if (!EmojiView.this.flowLayoutManager.isLastInRow(position)) {
                i = AndroidUtilities.m9dp(2.0f);
            }
            outRect.right = i;
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$13 */
    class CLASSNAME extends OnScrollListener {
        CLASSNAME() {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            EmojiView.this.checkStickersTabY(recyclerView, dy);
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$15 */
    class CLASSNAME extends OnScrollListener {
        CLASSNAME() {
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == 1) {
                AndroidUtilities.hideKeyboard(EmojiView.this.searchEditText);
            }
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            EmojiView.this.checkScroll();
            EmojiView.this.checkStickersTabY(recyclerView, dy);
            EmojiView.this.checkSearchFieldScroll();
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$18 */
    class CLASSNAME implements OnPageChangeListener {
        CLASSNAME() {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            EmojiView.this.onPageScrolled(position, (EmojiView.this.getMeasuredWidth() - EmojiView.this.getPaddingLeft()) - EmojiView.this.getPaddingRight(), positionOffsetPixels);
        }

        public void onPageSelected(int position) {
            EmojiView.this.saveNewPage();
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$1 */
    class CLASSNAME implements StickerPreviewViewerDelegate {
        CLASSNAME() {
        }

        public void sendSticker(Document sticker, Object parent) {
            EmojiView.this.listener.onStickerSelected(sticker, parent);
        }

        public boolean needSend() {
            return true;
        }

        public void openSet(InputStickerSet set) {
            if (set != null) {
                EmojiView.this.listener.onShowStickerSet(null, set);
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$4 */
    class CLASSNAME extends SpanSizeLookup {
        CLASSNAME() {
        }

        public int getSpanSize(int position) {
            if (EmojiView.this.stickersGridView.getAdapter() == EmojiView.this.stickersGridAdapter) {
                if (position == 0) {
                    return EmojiView.this.stickersGridAdapter.stickersPerRow;
                }
                if (position == EmojiView.this.stickersGridAdapter.totalItems || (EmojiView.this.stickersGridAdapter.cache.get(position) != null && !(EmojiView.this.stickersGridAdapter.cache.get(position) instanceof Document))) {
                    return EmojiView.this.stickersGridAdapter.stickersPerRow;
                }
                return 1;
            } else if (position == EmojiView.this.stickersSearchGridAdapter.totalItems || (EmojiView.this.stickersSearchGridAdapter.cache.get(position) != null && !(EmojiView.this.stickersSearchGridAdapter.cache.get(position) instanceof Document))) {
                return EmojiView.this.stickersGridAdapter.stickersPerRow;
            } else {
                return 1;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$8 */
    class CLASSNAME extends SpanSizeLookup {
        CLASSNAME() {
        }

        public int getSpanSize(int position) {
            if ((EmojiView.this.trendingGridAdapter.cache.get(position) instanceof Integer) || position == EmojiView.this.trendingGridAdapter.totalItems) {
                return EmojiView.this.trendingGridAdapter.stickersPerRow;
            }
            return 1;
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$9 */
    class CLASSNAME extends OnScrollListener {
        CLASSNAME() {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            EmojiView.this.checkStickersTabY(recyclerView, dy);
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$EmojiPagesAdapter */
    private class EmojiPagesAdapter extends PagerAdapter implements IconTabProvider {
        private EmojiPagesAdapter() {
        }

        /* synthetic */ EmojiPagesAdapter(EmojiView x0, CLASSNAME x1) {
            this();
        }

        public void destroyItem(ViewGroup viewGroup, int position, Object object) {
            View view;
            if (position == 6) {
                view = EmojiView.this.stickersWrap;
            } else {
                view = (View) EmojiView.this.views.get(position);
            }
            viewGroup.removeView(view);
        }

        public boolean canScrollToTab(int position) {
            if (position != 6 || EmojiView.this.currentChatId == 0) {
                return true;
            }
            EmojiView.this.showStickerBanHint();
            return false;
        }

        public int getCount() {
            return EmojiView.this.views.size();
        }

        public Drawable getPageIconDrawable(int position) {
            return EmojiView.this.icons[position];
        }

        public void customOnDraw(Canvas canvas, int position) {
            if (position == 6 && !DataQuery.getInstance(EmojiView.this.currentAccount).getUnreadStickerSets().isEmpty() && EmojiView.this.dotPaint != null) {
                canvas.drawCircle((float) ((canvas.getWidth() / 2) + AndroidUtilities.m9dp(9.0f)), (float) ((canvas.getHeight() / 2) - AndroidUtilities.m9dp(8.0f)), (float) AndroidUtilities.m9dp(5.0f), EmojiView.this.dotPaint);
            }
        }

        public Object instantiateItem(ViewGroup viewGroup, int position) {
            View view;
            if (position == 6) {
                view = EmojiView.this.stickersWrap;
            } else {
                view = (View) EmojiView.this.views.get(position);
            }
            viewGroup.addView(view);
            return view;
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$GifsAdapter */
    private class GifsAdapter extends SelectionAdapter {
        private Context mContext;

        public GifsAdapter(Context context) {
            this.mContext = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            return false;
        }

        public int getItemCount() {
            return EmojiView.this.recentGifs.size();
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(new ContextLinkCell(this.mContext));
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            Document document = (Document) EmojiView.this.recentGifs.get(i);
            if (document != null) {
                ((ContextLinkCell) viewHolder.itemView).setGif(document, false);
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$StickersGridAdapter */
    private class StickersGridAdapter extends SelectionAdapter {
        private SparseArray<Object> cache = new SparseArray();
        private SparseArray<Object> cacheParents = new SparseArray();
        private Context context;
        private HashMap<Object, Integer> packStartPosition = new HashMap();
        private SparseIntArray positionToRow = new SparseIntArray();
        private SparseArray<Object> rowStartPack = new SparseArray();
        private int stickersPerRow;
        private int totalItems;

        public StickersGridAdapter(Context context) {
            this.context = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            return false;
        }

        public int getItemCount() {
            return this.totalItems != 0 ? this.totalItems + 1 : 0;
        }

        public Object getItem(int i) {
            return this.cache.get(i);
        }

        public int getPositionForPack(Object pack) {
            Integer pos = (Integer) this.packStartPosition.get(pack);
            if (pos == null) {
                return -1;
            }
            return pos.intValue();
        }

        public int getItemViewType(int position) {
            if (position == 0) {
                return 4;
            }
            Object object = this.cache.get(position);
            if (object == null) {
                return 1;
            }
            if (object instanceof Document) {
                return 0;
            }
            if (object instanceof String) {
                return 3;
            }
            return 2;
        }

        public int getTabForPosition(int position) {
            if (position == 0) {
                position = 1;
            }
            if (this.stickersPerRow == 0) {
                int width = EmojiView.this.getMeasuredWidth();
                if (width == 0) {
                    width = AndroidUtilities.displaySize.x;
                }
                this.stickersPerRow = width / AndroidUtilities.m9dp(72.0f);
            }
            int row = this.positionToRow.get(position, Integer.MIN_VALUE);
            if (row == Integer.MIN_VALUE) {
                return (EmojiView.this.stickerSets.size() - 1) + EmojiView.this.stickersTabOffset;
            }
            TL_messages_stickerSet pack = this.rowStartPack.get(row);
            if (!(pack instanceof String)) {
                return EmojiView.this.stickersTabOffset + EmojiView.this.stickerSets.indexOf(pack);
            } else if ("recent".equals(pack)) {
                return EmojiView.this.recentTabBum;
            } else {
                return EmojiView.this.favTabBum;
            }
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new StickerEmojiCell(this.context) {
                        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.m9dp(82.0f), NUM));
                        }
                    };
                    break;
                case 1:
                    view = new EmptyCell(this.context);
                    break;
                case 2:
                    view = new StickerSetNameCell(this.context);
                    ((StickerSetNameCell) view).setOnIconClickListener(new EmojiView$StickersGridAdapter$$Lambda$0(this));
                    break;
                case 3:
                    view = new StickerSetGroupInfoCell(this.context);
                    ((StickerSetGroupInfoCell) view).setAddOnClickListener(new EmojiView$StickersGridAdapter$$Lambda$1(this));
                    view.setLayoutParams(new LayoutParams(-1, -2));
                    break;
                case 4:
                    view = new View(this.context);
                    view.setLayoutParams(new LayoutParams(-1, EmojiView.this.searchFieldHeight));
                    break;
            }
            return new Holder(view);
        }

        final /* synthetic */ void lambda$onCreateViewHolder$0$EmojiView$StickersGridAdapter(View v) {
            if (EmojiView.this.groupStickerSet == null) {
                MessagesController.getEmojiSettings(EmojiView.this.currentAccount).edit().putLong("group_hide_stickers_" + EmojiView.this.info.var_id, EmojiView.this.info.stickerset != null ? EmojiView.this.info.stickerset.var_id : 0).commit();
                EmojiView.this.updateStickerTabs();
                if (EmojiView.this.stickersGridAdapter != null) {
                    EmojiView.this.stickersGridAdapter.notifyDataSetChanged();
                }
            } else if (EmojiView.this.listener != null) {
                EmojiView.this.listener.onStickersGroupClick(EmojiView.this.info.var_id);
            }
        }

        final /* synthetic */ void lambda$onCreateViewHolder$1$EmojiView$StickersGridAdapter(View v) {
            if (EmojiView.this.listener != null) {
                EmojiView.this.listener.onStickersGroupClick(EmojiView.this.info.var_id);
            }
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    Document sticker = (Document) this.cache.get(position);
                    StickerEmojiCell cell = holder.itemView;
                    cell.setSticker(sticker, this.cacheParents.get(position), false);
                    boolean z = EmojiView.this.recentStickers.contains(sticker) || EmojiView.this.favouriteStickers.contains(sticker);
                    cell.setRecent(z);
                    return;
                case 1:
                    EmptyCell cell2 = holder.itemView;
                    if (position == this.totalItems) {
                        int row = this.positionToRow.get(position - 1, Integer.MIN_VALUE);
                        if (row == Integer.MIN_VALUE) {
                            cell2.setHeight(1);
                            return;
                        }
                        ArrayList<Document> documents;
                        Object pack = this.rowStartPack.get(row);
                        if (pack instanceof TL_messages_stickerSet) {
                            documents = ((TL_messages_stickerSet) pack).documents;
                        } else if (!(pack instanceof String)) {
                            documents = null;
                        } else if ("recent".equals(pack)) {
                            documents = EmojiView.this.recentStickers;
                        } else {
                            documents = EmojiView.this.favouriteStickers;
                        }
                        if (documents == null) {
                            cell2.setHeight(1);
                            return;
                        } else if (documents.isEmpty()) {
                            cell2.setHeight(AndroidUtilities.m9dp(8.0f));
                            return;
                        } else {
                            int height = EmojiView.this.pager.getHeight() - (((int) Math.ceil((double) (((float) documents.size()) / ((float) this.stickersPerRow)))) * AndroidUtilities.m9dp(82.0f));
                            if (height <= 0) {
                                height = 1;
                            }
                            cell2.setHeight(height);
                            return;
                        }
                    }
                    cell2.setHeight(AndroidUtilities.m9dp(82.0f));
                    return;
                case 2:
                    StickerSetNameCell cell3 = holder.itemView;
                    if (position == EmojiView.this.groupStickerPackPosition) {
                        int icon = (EmojiView.this.groupStickersHidden && EmojiView.this.groupStickerSet == null) ? 0 : EmojiView.this.groupStickerSet != null ? R.drawable.stickersclose : R.drawable.stickerset_close;
                        Chat chat = EmojiView.this.info != null ? MessagesController.getInstance(EmojiView.this.currentAccount).getChat(Integer.valueOf(EmojiView.this.info.var_id)) : null;
                        String str = "CurrentGroupStickers";
                        Object[] objArr = new Object[1];
                        objArr[0] = chat != null ? chat.title : "Group Stickers";
                        cell3.setText(LocaleController.formatString(str, R.string.CurrentGroupStickers, objArr), icon);
                        return;
                    }
                    TL_messages_stickerSet object = this.cache.get(position);
                    if (object instanceof TL_messages_stickerSet) {
                        TL_messages_stickerSet set = object;
                        if (set.set != null) {
                            cell3.setText(set.set.title, 0);
                            return;
                        }
                        return;
                    } else if (object == EmojiView.this.recentStickers) {
                        cell3.setText(LocaleController.getString("RecentStickers", R.string.RecentStickers), 0);
                        return;
                    } else if (object == EmojiView.this.favouriteStickers) {
                        cell3.setText(LocaleController.getString("FavoriteStickers", R.string.FavoriteStickers), 0);
                        return;
                    } else {
                        return;
                    }
                case 3:
                    holder.itemView.setIsLast(position == this.totalItems + -1);
                    return;
                default:
                    return;
            }
        }

        public void notifyDataSetChanged() {
            int width = EmojiView.this.getMeasuredWidth();
            if (width == 0) {
                width = AndroidUtilities.displaySize.x;
            }
            this.stickersPerRow = width / AndroidUtilities.m9dp(72.0f);
            EmojiView.this.stickersLayoutManager.setSpanCount(this.stickersPerRow);
            this.rowStartPack.clear();
            this.packStartPosition.clear();
            this.positionToRow.clear();
            this.cache.clear();
            this.totalItems = 0;
            ArrayList<TL_messages_stickerSet> packs = EmojiView.this.stickerSets;
            int startRow = 0;
            int a = -3;
            while (a < packs.size()) {
                TL_messages_stickerSet pack = null;
                SparseArray sparseArray;
                int i;
                if (a == -3) {
                    sparseArray = this.cache;
                    i = this.totalItems;
                    this.totalItems = i + 1;
                    sparseArray.put(i, "search");
                    startRow++;
                } else {
                    ArrayList<Document> documents;
                    String key;
                    if (a == -2) {
                        documents = EmojiView.this.favouriteStickers;
                        key = "fav";
                        this.packStartPosition.put(key, Integer.valueOf(this.totalItems));
                    } else if (a == -1) {
                        documents = EmojiView.this.recentStickers;
                        key = "recent";
                        this.packStartPosition.put(key, Integer.valueOf(this.totalItems));
                    } else {
                        key = null;
                        pack = (TL_messages_stickerSet) packs.get(a);
                        documents = pack.documents;
                        this.packStartPosition.put(pack, Integer.valueOf(this.totalItems));
                    }
                    if (a == EmojiView.this.groupStickerPackNum) {
                        EmojiView.this.groupStickerPackPosition = this.totalItems;
                        if (documents.isEmpty()) {
                            this.rowStartPack.put(startRow, pack);
                            int startRow2 = startRow + 1;
                            this.positionToRow.put(this.totalItems, startRow);
                            this.rowStartPack.put(startRow2, pack);
                            startRow = startRow2 + 1;
                            this.positionToRow.put(this.totalItems + 1, startRow2);
                            sparseArray = this.cache;
                            i = this.totalItems;
                            this.totalItems = i + 1;
                            sparseArray.put(i, pack);
                            sparseArray = this.cache;
                            i = this.totalItems;
                            this.totalItems = i + 1;
                            sparseArray.put(i, "group");
                        }
                    }
                    if (!documents.isEmpty()) {
                        int b;
                        int count = (int) Math.ceil((double) (((float) documents.size()) / ((float) this.stickersPerRow)));
                        if (pack != null) {
                            this.cache.put(this.totalItems, pack);
                        } else {
                            this.cache.put(this.totalItems, documents);
                        }
                        this.positionToRow.put(this.totalItems, startRow);
                        for (b = 0; b < documents.size(); b++) {
                            int num = (b + 1) + this.totalItems;
                            this.cache.put(num, documents.get(b));
                            if (pack != null) {
                                this.cacheParents.put(num, pack);
                            } else {
                                this.cacheParents.put(num, key);
                            }
                            this.positionToRow.put((b + 1) + this.totalItems, (startRow + 1) + (b / this.stickersPerRow));
                        }
                        for (b = 0; b < count + 1; b++) {
                            if (pack != null) {
                                this.rowStartPack.put(startRow + b, pack);
                            } else {
                                this.rowStartPack.put(startRow + b, a == -1 ? "recent" : "fav");
                            }
                        }
                        this.totalItems += (this.stickersPerRow * count) + 1;
                        startRow += count + 1;
                    }
                }
                a++;
            }
            super.notifyDataSetChanged();
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$StickersSearchGridAdapter */
    private class StickersSearchGridAdapter extends SelectionAdapter {
        private SparseArray<Object> cache = new SparseArray();
        private SparseArray<Object> cacheParent = new SparseArray();
        boolean cleared;
        private Context context;
        private ArrayList<ArrayList<Document>> emojiArrays = new ArrayList();
        private HashMap<ArrayList<Document>, String> emojiStickers = new HashMap();
        private ArrayList<TL_messages_stickerSet> localPacks = new ArrayList();
        private HashMap<TL_messages_stickerSet, Integer> localPacksByName = new HashMap();
        private HashMap<TL_messages_stickerSet, Boolean> localPacksByShortName = new HashMap();
        private SparseIntArray positionToRow = new SparseIntArray();
        private SparseArray<StickerSetCovered> positionsToSets = new SparseArray();
        private int reqId;
        private int reqId2;
        private SparseArray<Object> rowStartPack = new SparseArray();
        private String searchQuery;
        private Runnable searchRunnable = new CLASSNAME();
        private ArrayList<StickerSetCovered> serverPacks = new ArrayList();
        private int totalItems;

        /* renamed from: org.telegram.ui.Components.EmojiView$StickersSearchGridAdapter$1 */
        class CLASSNAME implements Runnable {
            CLASSNAME() {
            }

            private void clear() {
                if (!StickersSearchGridAdapter.this.cleared) {
                    StickersSearchGridAdapter.this.cleared = true;
                    StickersSearchGridAdapter.this.emojiStickers.clear();
                    StickersSearchGridAdapter.this.emojiArrays.clear();
                    StickersSearchGridAdapter.this.localPacks.clear();
                    StickersSearchGridAdapter.this.serverPacks.clear();
                    StickersSearchGridAdapter.this.localPacksByShortName.clear();
                    StickersSearchGridAdapter.this.localPacksByName.clear();
                }
            }

            public void run() {
                if (!TextUtils.isEmpty(StickersSearchGridAdapter.this.searchQuery)) {
                    int a;
                    ArrayList<Document> newStickers;
                    int size;
                    TL_messages_stickerSet set;
                    int index;
                    EmojiView.this.progressDrawable.startAnimation();
                    StickersSearchGridAdapter.this.cleared = false;
                    ArrayList<Document> emojiStickersArray = new ArrayList(0);
                    LongSparseArray<Document> emojiStickersMap = new LongSparseArray(0);
                    HashMap<String, ArrayList<Document>> allStickers = DataQuery.getInstance(EmojiView.this.currentAccount).getAllStickers();
                    if (StickersSearchGridAdapter.this.searchQuery.length() <= 14) {
                        CharSequence emoji = StickersSearchGridAdapter.this.searchQuery;
                        int length = emoji.length();
                        a = 0;
                        while (a < length) {
                            CharSequence[] charSequenceArr;
                            if (a < length - 1 && ((emoji.charAt(a) == 55356 && emoji.charAt(a + 1) >= 57339 && emoji.charAt(a + 1) <= 57343) || (emoji.charAt(a) == 8205 && (emoji.charAt(a + 1) == 9792 || emoji.charAt(a + 1) == 9794)))) {
                                charSequenceArr = new CharSequence[2];
                                charSequenceArr[0] = emoji.subSequence(0, a);
                                charSequenceArr[1] = emoji.subSequence(a + 2, emoji.length());
                                emoji = TextUtils.concat(charSequenceArr);
                                length -= 2;
                                a--;
                            } else if (emoji.charAt(a) == 65039) {
                                charSequenceArr = new CharSequence[2];
                                charSequenceArr[0] = emoji.subSequence(0, a);
                                charSequenceArr[1] = emoji.subSequence(a + 1, emoji.length());
                                emoji = TextUtils.concat(charSequenceArr);
                                length--;
                                a--;
                            }
                            a++;
                        }
                        newStickers = allStickers != null ? (ArrayList) allStickers.get(emoji.toString()) : null;
                        if (!(newStickers == null || newStickers.isEmpty())) {
                            clear();
                            emojiStickersArray.addAll(newStickers);
                            size = newStickers.size();
                            for (a = 0; a < size; a++) {
                                Document document = (Document) newStickers.get(a);
                                emojiStickersMap.put(document.var_id, document);
                            }
                            StickersSearchGridAdapter.this.emojiStickers.put(emojiStickersArray, StickersSearchGridAdapter.this.searchQuery);
                            StickersSearchGridAdapter.this.emojiArrays.add(emojiStickersArray);
                        }
                    }
                    if (!(allStickers == null || allStickers.isEmpty() || StickersSearchGridAdapter.this.searchQuery.length() <= 1)) {
                        Object[] suggestions = Emoji.getSuggestion(StickersSearchGridAdapter.this.searchQuery.startsWith(":") ? StickersSearchGridAdapter.this.searchQuery : ":" + StickersSearchGridAdapter.this.searchQuery);
                        if (suggestions != null) {
                            size = Math.min(10, suggestions.length);
                            for (a = 0; a < size; a++) {
                                EmojiSuggestion suggestion = suggestions[a];
                                suggestion.emoji = suggestion.emoji.replace("\ufe0f", TtmlNode.ANONYMOUS_REGION_ID);
                                newStickers = allStickers != null ? (ArrayList) allStickers.get(suggestion.emoji) : null;
                                if (!(newStickers == null || newStickers.isEmpty())) {
                                    clear();
                                    if (!StickersSearchGridAdapter.this.emojiStickers.containsKey(newStickers)) {
                                        StickersSearchGridAdapter.this.emojiStickers.put(newStickers, suggestion.emoji);
                                        StickersSearchGridAdapter.this.emojiArrays.add(newStickers);
                                    }
                                }
                            }
                        }
                    }
                    ArrayList<TL_messages_stickerSet> local = DataQuery.getInstance(EmojiView.this.currentAccount).getStickerSets(0);
                    size = local.size();
                    for (a = 0; a < size; a++) {
                        set = (TL_messages_stickerSet) local.get(a);
                        index = set.set.title.toLowerCase().indexOf(StickersSearchGridAdapter.this.searchQuery);
                        if (index >= 0) {
                            if (index == 0 || set.set.title.charAt(index - 1) == ' ') {
                                clear();
                                StickersSearchGridAdapter.this.localPacks.add(set);
                                StickersSearchGridAdapter.this.localPacksByName.put(set, Integer.valueOf(index));
                            }
                        } else if (set.set.short_name != null) {
                            index = set.set.short_name.toLowerCase().indexOf(StickersSearchGridAdapter.this.searchQuery);
                            if (index >= 0 && (index == 0 || set.set.short_name.charAt(index - 1) == ' ')) {
                                clear();
                                StickersSearchGridAdapter.this.localPacks.add(set);
                                StickersSearchGridAdapter.this.localPacksByShortName.put(set, Boolean.valueOf(true));
                            }
                        }
                    }
                    local = DataQuery.getInstance(EmojiView.this.currentAccount).getStickerSets(3);
                    size = local.size();
                    for (a = 0; a < size; a++) {
                        set = (TL_messages_stickerSet) local.get(a);
                        index = set.set.title.toLowerCase().indexOf(StickersSearchGridAdapter.this.searchQuery);
                        if (index >= 0) {
                            if (index == 0 || set.set.title.charAt(index - 1) == ' ') {
                                clear();
                                StickersSearchGridAdapter.this.localPacks.add(set);
                                StickersSearchGridAdapter.this.localPacksByName.put(set, Integer.valueOf(index));
                            }
                        } else if (set.set.short_name != null) {
                            index = set.set.short_name.toLowerCase().indexOf(StickersSearchGridAdapter.this.searchQuery);
                            if (index >= 0 && (index == 0 || set.set.short_name.charAt(index - 1) == ' ')) {
                                clear();
                                StickersSearchGridAdapter.this.localPacks.add(set);
                                StickersSearchGridAdapter.this.localPacksByShortName.put(set, Boolean.valueOf(true));
                            }
                        }
                    }
                    if (!((StickersSearchGridAdapter.this.localPacks.isEmpty() && StickersSearchGridAdapter.this.emojiStickers.isEmpty()) || EmojiView.this.stickersGridView.getAdapter() == EmojiView.this.stickersSearchGridAdapter)) {
                        EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersSearchGridAdapter);
                    }
                    TL_messages_searchStickerSets req = new TL_messages_searchStickerSets();
                    req.var_q = StickersSearchGridAdapter.this.searchQuery;
                    StickersSearchGridAdapter.this.reqId = ConnectionsManager.getInstance(EmojiView.this.currentAccount).sendRequest(req, new EmojiView$StickersSearchGridAdapter$1$$Lambda$0(this, req));
                    if (Emoji.isValidEmoji(StickersSearchGridAdapter.this.searchQuery)) {
                        TL_messages_getStickers req2 = new TL_messages_getStickers();
                        req2.emoticon = StickersSearchGridAdapter.this.searchQuery;
                        req2.hash = 0;
                        StickersSearchGridAdapter.this.reqId2 = ConnectionsManager.getInstance(EmojiView.this.currentAccount).sendRequest(req2, new EmojiView$StickersSearchGridAdapter$1$$Lambda$1(this, req2, emojiStickersArray, emojiStickersMap));
                    }
                    StickersSearchGridAdapter.this.notifyDataSetChanged();
                }
            }

            final /* synthetic */ void lambda$run$1$EmojiView$StickersSearchGridAdapter$1(TL_messages_searchStickerSets req, TLObject response, TL_error error) {
                if (response instanceof TL_messages_foundStickerSets) {
                    AndroidUtilities.runOnUIThread(new EmojiView$StickersSearchGridAdapter$1$$Lambda$3(this, req, response));
                }
            }

            final /* synthetic */ void lambda$null$0$EmojiView$StickersSearchGridAdapter$1(TL_messages_searchStickerSets req, TLObject response) {
                if (req.var_q.equals(StickersSearchGridAdapter.this.searchQuery)) {
                    clear();
                    EmojiView.this.progressDrawable.stopAnimation();
                    StickersSearchGridAdapter.this.reqId = 0;
                    if (EmojiView.this.stickersGridView.getAdapter() != EmojiView.this.stickersSearchGridAdapter) {
                        EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersSearchGridAdapter);
                    }
                    StickersSearchGridAdapter.this.serverPacks.addAll(((TL_messages_foundStickerSets) response).sets);
                    StickersSearchGridAdapter.this.notifyDataSetChanged();
                }
            }

            final /* synthetic */ void lambda$run$3$EmojiView$StickersSearchGridAdapter$1(TL_messages_getStickers req2, ArrayList emojiStickersArray, LongSparseArray emojiStickersMap, TLObject response, TL_error error) {
                AndroidUtilities.runOnUIThread(new EmojiView$StickersSearchGridAdapter$1$$Lambda$2(this, req2, response, emojiStickersArray, emojiStickersMap));
            }

            final /* synthetic */ void lambda$null$2$EmojiView$StickersSearchGridAdapter$1(TL_messages_getStickers req2, TLObject response, ArrayList emojiStickersArray, LongSparseArray emojiStickersMap) {
                if (req2.emoticon.equals(StickersSearchGridAdapter.this.searchQuery)) {
                    StickersSearchGridAdapter.this.reqId2 = 0;
                    if (response instanceof TL_messages_stickers) {
                        TL_messages_stickers res = (TL_messages_stickers) response;
                        int oldCount = emojiStickersArray.size();
                        int size = res.stickers.size();
                        for (int a = 0; a < size; a++) {
                            Document document = (Document) res.stickers.get(a);
                            if (emojiStickersMap.indexOfKey(document.var_id) < 0) {
                                emojiStickersArray.add(document);
                            }
                        }
                        if (oldCount != emojiStickersArray.size()) {
                            StickersSearchGridAdapter.this.emojiStickers.put(emojiStickersArray, StickersSearchGridAdapter.this.searchQuery);
                            if (oldCount == 0) {
                                StickersSearchGridAdapter.this.emojiArrays.add(emojiStickersArray);
                            }
                            StickersSearchGridAdapter.this.notifyDataSetChanged();
                        }
                    }
                }
            }
        }

        public StickersSearchGridAdapter(Context context) {
            this.context = context;
        }

        public boolean isEnabled(ViewHolder holder) {
            return false;
        }

        public int getItemCount() {
            if (this.totalItems != 1) {
                return this.totalItems + 1;
            }
            return 2;
        }

        public Object getItem(int i) {
            return this.cache.get(i);
        }

        public void search(String text) {
            if (this.reqId != 0) {
                ConnectionsManager.getInstance(EmojiView.this.currentAccount).cancelRequest(this.reqId, true);
                this.reqId = 0;
            }
            if (this.reqId2 != 0) {
                ConnectionsManager.getInstance(EmojiView.this.currentAccount).cancelRequest(this.reqId2, true);
                this.reqId2 = 0;
            }
            if (TextUtils.isEmpty(text)) {
                this.searchQuery = null;
                this.localPacks.clear();
                this.emojiStickers.clear();
                this.serverPacks.clear();
                if (EmojiView.this.stickersGridView.getAdapter() != EmojiView.this.stickersGridAdapter) {
                    EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersGridAdapter);
                }
                notifyDataSetChanged();
            } else {
                this.searchQuery = text.toLowerCase();
            }
            AndroidUtilities.cancelRunOnUIThread(this.searchRunnable);
            AndroidUtilities.runOnUIThread(this.searchRunnable, 300);
        }

        public int getItemViewType(int position) {
            if (position == 0) {
                return 4;
            }
            if (position == 1 && this.totalItems == 1) {
                return 5;
            }
            Object object = this.cache.get(position);
            if (object == null) {
                return 1;
            }
            if (object instanceof Document) {
                return 0;
            }
            if (object instanceof StickerSetCovered) {
                return 3;
            }
            return 2;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new StickerEmojiCell(this.context) {
                        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.m9dp(82.0f), NUM));
                        }
                    };
                    break;
                case 1:
                    view = new EmptyCell(this.context);
                    break;
                case 2:
                    view = new StickerSetNameCell(this.context);
                    break;
                case 3:
                    view = new FeaturedStickerSetInfoCell(this.context, 17);
                    ((FeaturedStickerSetInfoCell) view).setAddOnClickListener(new EmojiView$StickersSearchGridAdapter$$Lambda$0(this));
                    break;
                case 4:
                    view = new View(this.context);
                    view.setLayoutParams(new LayoutParams(-1, EmojiView.this.searchFieldHeight));
                    break;
                case 5:
                    View frameLayout = new FrameLayout(this.context) {
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) (((float) (((EmojiView.this.stickersGridView.getMeasuredHeight() - EmojiView.this.searchFieldHeight) - AndroidUtilities.m9dp(8.0f)) / 3)) * 1.7f), NUM));
                        }
                    };
                    ImageView imageView = new ImageView(this.context);
                    imageView.setScaleType(ScaleType.CENTER);
                    imageView.setImageResource(R.drawable.stickers_none);
                    imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelEmptyText), Mode.MULTIPLY));
                    frameLayout.addView(imageView, LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 0.0f, 0.0f, 48.0f));
                    TextView textView = new TextView(this.context);
                    textView.setText(LocaleController.getString("NoStickersFound", R.string.NoStickersFound));
                    textView.setTextSize(1, 18.0f);
                    textView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
                    textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                    frameLayout.addView(textView, LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 30.0f, 0.0f, 0.0f));
                    view = frameLayout;
                    view.setLayoutParams(new LayoutParams(-1, -2));
                    break;
            }
            return new Holder(view);
        }

        final /* synthetic */ void lambda$onCreateViewHolder$0$EmojiView$StickersSearchGridAdapter(View v) {
            FeaturedStickerSetInfoCell parent1 = (FeaturedStickerSetInfoCell) v.getParent();
            StickerSetCovered pack = parent1.getStickerSet();
            if (EmojiView.this.installingStickerSets.indexOfKey(pack.set.var_id) < 0 && EmojiView.this.removingStickerSets.indexOfKey(pack.set.var_id) < 0) {
                if (parent1.isInstalled()) {
                    EmojiView.this.removingStickerSets.put(pack.set.var_id, pack);
                    EmojiView.this.listener.onStickerSetRemove(parent1.getStickerSet());
                } else {
                    EmojiView.this.installingStickerSets.put(pack.set.var_id, pack);
                    EmojiView.this.listener.onStickerSetAdd(parent1.getStickerSet());
                }
                parent1.setDrawProgress(true);
            }
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z;
            switch (holder.getItemViewType()) {
                case 0:
                    Document sticker = (Document) this.cache.get(position);
                    StickerEmojiCell cell = holder.itemView;
                    cell.setSticker(sticker, this.cacheParent.get(position), false);
                    z = EmojiView.this.recentStickers.contains(sticker) || EmojiView.this.favouriteStickers.contains(sticker);
                    cell.setRecent(z);
                    return;
                case 1:
                    EmptyCell cell2 = holder.itemView;
                    if (position == this.totalItems) {
                        int row = this.positionToRow.get(position - 1, Integer.MIN_VALUE);
                        if (row == Integer.MIN_VALUE) {
                            cell2.setHeight(1);
                            return;
                        }
                        ArrayList<Document> documents;
                        Object pack = this.rowStartPack.get(row);
                        if (pack instanceof TL_messages_stickerSet) {
                            documents = ((TL_messages_stickerSet) pack).documents;
                        } else if (pack instanceof ArrayList) {
                            documents = (ArrayList) pack;
                        } else {
                            documents = null;
                        }
                        if (documents == null) {
                            cell2.setHeight(1);
                            return;
                        } else if (documents.isEmpty()) {
                            cell2.setHeight(AndroidUtilities.m9dp(8.0f));
                            return;
                        } else {
                            int height = EmojiView.this.pager.getHeight() - (((int) Math.ceil((double) (((float) documents.size()) / ((float) EmojiView.this.stickersGridAdapter.stickersPerRow)))) * AndroidUtilities.m9dp(82.0f));
                            if (height <= 0) {
                                height = 1;
                            }
                            cell2.setHeight(height);
                            return;
                        }
                    }
                    cell2.setHeight(AndroidUtilities.m9dp(82.0f));
                    return;
                case 2:
                    StickerSetNameCell cell3 = holder.itemView;
                    TL_messages_stickerSet object = this.cache.get(position);
                    if (object instanceof TL_messages_stickerSet) {
                        TL_messages_stickerSet set = object;
                        if (TextUtils.isEmpty(this.searchQuery) || !this.localPacksByShortName.containsKey(set)) {
                            Integer start = (Integer) this.localPacksByName.get(set);
                            if (!(set.set == null || start == null)) {
                                cell3.setText(set.set.title, 0, start.intValue(), !TextUtils.isEmpty(this.searchQuery) ? this.searchQuery.length() : 0);
                            }
                            cell3.setUrl(null, 0);
                            return;
                        }
                        if (set.set != null) {
                            cell3.setText(set.set.title, 0);
                        }
                        cell3.setUrl(set.set.short_name, this.searchQuery.length());
                        return;
                    } else if (object instanceof ArrayList) {
                        cell3.setText((CharSequence) this.emojiStickers.get(object), 0);
                        cell3.setUrl(null, 0);
                        return;
                    } else {
                        return;
                    }
                case 3:
                    StickerSetCovered stickerSetCovered = (StickerSetCovered) this.cache.get(position);
                    FeaturedStickerSetInfoCell cell4 = holder.itemView;
                    boolean installing = EmojiView.this.installingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0;
                    boolean removing = EmojiView.this.removingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0;
                    if (installing || removing) {
                        if (installing && cell4.isInstalled()) {
                            EmojiView.this.installingStickerSets.remove(stickerSetCovered.set.var_id);
                            installing = false;
                        } else if (removing && !cell4.isInstalled()) {
                            EmojiView.this.removingStickerSets.remove(stickerSetCovered.set.var_id);
                            removing = false;
                        }
                    }
                    z = installing || removing;
                    cell4.setDrawProgress(z);
                    int idx = TextUtils.isEmpty(this.searchQuery) ? -1 : stickerSetCovered.set.title.toLowerCase().indexOf(this.searchQuery);
                    if (idx >= 0) {
                        cell4.setStickerSet(stickerSetCovered, false, idx, this.searchQuery.length());
                        return;
                    }
                    cell4.setStickerSet(stickerSetCovered, false);
                    if (!TextUtils.isEmpty(this.searchQuery) && stickerSetCovered.set.short_name.toLowerCase().startsWith(this.searchQuery)) {
                        cell4.setUrl(stickerSetCovered.set.short_name, this.searchQuery.length());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void notifyDataSetChanged() {
            this.rowStartPack.clear();
            this.positionToRow.clear();
            this.cache.clear();
            this.positionsToSets.clear();
            this.totalItems = 0;
            int startRow = 0;
            int serverSize = this.serverPacks.size();
            int localSize = this.localPacks.size();
            int emojiSize = this.emojiArrays.size();
            for (int a = -1; a < (serverSize + localSize) + emojiSize; a++) {
                Object pack = null;
                if (a == -1) {
                    SparseArray sparseArray = this.cache;
                    int i = this.totalItems;
                    this.totalItems = i + 1;
                    sparseArray.put(i, "search");
                    startRow++;
                } else {
                    ArrayList<Document> documents;
                    int idx = a;
                    if (idx < localSize) {
                        TL_messages_stickerSet set = (TL_messages_stickerSet) this.localPacks.get(idx);
                        documents = set.documents;
                        pack = set;
                    } else {
                        idx -= localSize;
                        if (idx < emojiSize) {
                            documents = (ArrayList) this.emojiArrays.get(idx);
                        } else {
                            StickerSetCovered set2 = (StickerSetCovered) this.serverPacks.get(idx - emojiSize);
                            documents = set2.covers;
                            StickerSetCovered pack2 = set2;
                        }
                    }
                    if (!documents.isEmpty()) {
                        int b;
                        int count = (int) Math.ceil((double) (((float) documents.size()) / ((float) EmojiView.this.stickersGridAdapter.stickersPerRow)));
                        if (pack2 != null) {
                            this.cache.put(this.totalItems, pack2);
                        } else {
                            this.cache.put(this.totalItems, documents);
                        }
                        if (a >= localSize && (pack2 instanceof StickerSetCovered)) {
                            SparseArray sparseArray2 = this.positionsToSets;
                            sparseArray2.put(this.totalItems, (StickerSetCovered) pack2);
                        }
                        this.positionToRow.put(this.totalItems, startRow);
                        int size = documents.size();
                        for (b = 0; b < size; b++) {
                            int num = (b + 1) + this.totalItems;
                            this.cache.put(num, documents.get(b));
                            if (pack2 != null) {
                                this.cacheParent.put(num, pack2);
                            }
                            this.positionToRow.put((b + 1) + this.totalItems, (startRow + 1) + (b / EmojiView.this.stickersGridAdapter.stickersPerRow));
                            if (a >= localSize && (pack2 instanceof StickerSetCovered)) {
                                this.positionsToSets.put(this.totalItems + (b + 1), (StickerSetCovered) pack2);
                            }
                        }
                        for (b = 0; b < count + 1; b++) {
                            if (pack2 != null) {
                                this.rowStartPack.put(startRow + b, pack2);
                            } else {
                                this.rowStartPack.put(startRow + b, documents);
                            }
                        }
                        this.totalItems += (EmojiView.this.stickersGridAdapter.stickersPerRow * count) + 1;
                        startRow += count + 1;
                    }
                }
            }
            super.notifyDataSetChanged();
        }
    }

    /* renamed from: org.telegram.ui.Components.EmojiView$TrendingGridAdapter */
    private class TrendingGridAdapter extends SelectionAdapter {
        private SparseArray<Object> cache = new SparseArray();
        private Context context;
        private SparseArray<StickerSetCovered> positionsToSets = new SparseArray();
        private ArrayList<StickerSetCovered> sets = new ArrayList();
        private int stickersPerRow;
        private int totalItems;

        public TrendingGridAdapter(Context context) {
            this.context = context;
        }

        public int getItemCount() {
            return this.totalItems;
        }

        public Object getItem(int i) {
            return this.cache.get(i);
        }

        public boolean isEnabled(ViewHolder holder) {
            return false;
        }

        public int getItemViewType(int position) {
            Object object = this.cache.get(position);
            if (object == null) {
                return 1;
            }
            if (object instanceof Document) {
                return 0;
            }
            return 2;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new StickerEmojiCell(this.context) {
                        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.m9dp(82.0f), NUM));
                        }
                    };
                    break;
                case 1:
                    view = new EmptyCell(this.context);
                    break;
                case 2:
                    view = new FeaturedStickerSetInfoCell(this.context, 17);
                    ((FeaturedStickerSetInfoCell) view).setAddOnClickListener(new EmojiView$TrendingGridAdapter$$Lambda$0(this));
                    break;
            }
            return new Holder(view);
        }

        final /* synthetic */ void lambda$onCreateViewHolder$0$EmojiView$TrendingGridAdapter(View v) {
            FeaturedStickerSetInfoCell parent1 = (FeaturedStickerSetInfoCell) v.getParent();
            StickerSetCovered pack = parent1.getStickerSet();
            if (EmojiView.this.installingStickerSets.indexOfKey(pack.set.var_id) < 0 && EmojiView.this.removingStickerSets.indexOfKey(pack.set.var_id) < 0) {
                if (parent1.isInstalled()) {
                    EmojiView.this.removingStickerSets.put(pack.set.var_id, pack);
                    EmojiView.this.listener.onStickerSetRemove(parent1.getStickerSet());
                } else {
                    EmojiView.this.installingStickerSets.put(pack.set.var_id, pack);
                    EmojiView.this.listener.onStickerSetAdd(parent1.getStickerSet());
                }
                parent1.setDrawProgress(true);
            }
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z = false;
            switch (holder.getItemViewType()) {
                case 0:
                    ((StickerEmojiCell) holder.itemView).setSticker((Document) this.cache.get(position), this.positionsToSets.get(position), false);
                    return;
                case 1:
                    ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.m9dp(82.0f));
                    return;
                case 2:
                    boolean unread;
                    boolean installing;
                    boolean removing;
                    ArrayList<Long> unreadStickers = DataQuery.getInstance(EmojiView.this.currentAccount).getUnreadStickerSets();
                    StickerSetCovered stickerSetCovered = (StickerSetCovered) this.sets.get(((Integer) this.cache.get(position)).intValue());
                    if (unreadStickers == null || !unreadStickers.contains(Long.valueOf(stickerSetCovered.set.var_id))) {
                        unread = false;
                    } else {
                        unread = true;
                    }
                    FeaturedStickerSetInfoCell cell = holder.itemView;
                    cell.setStickerSet(stickerSetCovered, unread);
                    if (unread) {
                        DataQuery.getInstance(EmojiView.this.currentAccount).markFaturedStickersByIdAsRead(stickerSetCovered.set.var_id);
                    }
                    if (EmojiView.this.installingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0) {
                        installing = true;
                    } else {
                        installing = false;
                    }
                    if (EmojiView.this.removingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0) {
                        removing = true;
                    } else {
                        removing = false;
                    }
                    if (installing || removing) {
                        if (installing && cell.isInstalled()) {
                            EmojiView.this.installingStickerSets.remove(stickerSetCovered.set.var_id);
                            installing = false;
                        } else if (removing && !cell.isInstalled()) {
                            EmojiView.this.removingStickerSets.remove(stickerSetCovered.set.var_id);
                            removing = false;
                        }
                    }
                    if (installing || removing) {
                        z = true;
                    }
                    cell.setDrawProgress(z);
                    return;
                default:
                    return;
            }
        }

        public void notifyDataSetChanged() {
            int width = EmojiView.this.getMeasuredWidth();
            if (width == 0) {
                if (AndroidUtilities.isTablet()) {
                    int smallSide = AndroidUtilities.displaySize.x;
                    int leftSide = (smallSide * 35) / 100;
                    if (leftSide < AndroidUtilities.m9dp(320.0f)) {
                        leftSide = AndroidUtilities.m9dp(320.0f);
                    }
                    width = smallSide - leftSide;
                } else {
                    width = AndroidUtilities.displaySize.x;
                }
                if (width == 0) {
                    width = 1080;
                }
            }
            this.stickersPerRow = Math.max(1, width / AndroidUtilities.m9dp(72.0f));
            EmojiView.this.trendingLayoutManager.setSpanCount(this.stickersPerRow);
            if (!EmojiView.this.trendingLoaded) {
                this.cache.clear();
                this.positionsToSets.clear();
                this.sets.clear();
                this.totalItems = 0;
                int num = 0;
                ArrayList<StickerSetCovered> packs = DataQuery.getInstance(EmojiView.this.currentAccount).getFeaturedStickerSets();
                for (int a = 0; a < packs.size(); a++) {
                    StickerSetCovered pack = (StickerSetCovered) packs.get(a);
                    if (!(DataQuery.getInstance(EmojiView.this.currentAccount).isStickerPackInstalled(pack.set.var_id) || (pack.covers.isEmpty() && pack.cover == null))) {
                        int count;
                        int b;
                        this.sets.add(pack);
                        this.positionsToSets.put(this.totalItems, pack);
                        SparseArray sparseArray = this.cache;
                        int i = this.totalItems;
                        this.totalItems = i + 1;
                        int num2 = num + 1;
                        sparseArray.put(i, Integer.valueOf(num));
                        int startRow = this.totalItems / this.stickersPerRow;
                        if (pack.covers.isEmpty()) {
                            count = 1;
                            this.cache.put(this.totalItems, pack.cover);
                        } else {
                            count = (int) Math.ceil((double) (((float) pack.covers.size()) / ((float) this.stickersPerRow)));
                            for (b = 0; b < pack.covers.size(); b++) {
                                this.cache.put(this.totalItems + b, pack.covers.get(b));
                            }
                        }
                        for (b = 0; b < this.stickersPerRow * count; b++) {
                            this.positionsToSets.put(this.totalItems + b, pack);
                        }
                        this.totalItems += this.stickersPerRow * count;
                        num = num2;
                    }
                }
                if (this.totalItems != 0) {
                    EmojiView.this.trendingLoaded = true;
                    EmojiView.this.featuredStickersHash = DataQuery.getInstance(EmojiView.this.currentAccount).getFeaturesStickersHashWithoutUnread();
                }
                super.notifyDataSetChanged();
            }
        }
    }

    static {
        Field f = null;
        try {
            f = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
        superListenerField = f;
    }

    static final /* synthetic */ void lambda$static$0$EmojiView() {
    }

    private static String addColorToCode(String code, String color) {
        String end = null;
        int length = code.length();
        if (length > 2 && code.charAt(code.length() - 2) == 8205) {
            end = code.substring(code.length() - 2);
            code = code.substring(0, code.length() - 2);
        } else if (length > 3 && code.charAt(code.length() - 3) == 8205) {
            end = code.substring(code.length() - 3);
            code = code.substring(0, code.length() - 3);
        }
        code = code + color;
        if (end != null) {
            return code + end;
        }
        return code;
    }

    public void addEmojiToRecent(String code) {
        if (Emoji.isValidEmoji(code)) {
            Emoji.addRecentEmoji(code);
            if (!(getVisibility() == 0 && this.pager.getCurrentItem() == 0)) {
                Emoji.sortEmoji();
            }
            Emoji.saveRecentEmoji();
            ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
        }
    }

    public EmojiView(boolean needStickers, boolean needGif, Context context, ChatFull chatFull) {
        FrameLayout frameLayout;
        super(context);
        this.stickersDrawable = context.getResources().getDrawable(R.drawable.ic_smiles2_stickers);
        Theme.setDrawableColorByKey(this.stickersDrawable, Theme.key_chat_emojiPanelIcon);
        Drawable[] drawableArr = new Drawable[7];
        drawableArr[0] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_recent, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[1] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_smile, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[2] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_nature, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[3] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_food, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[4] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_car, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[5] = Theme.createEmojiIconSelectorDrawable(context, R.drawable.ic_smiles2_objects, Theme.getColor(Theme.key_chat_emojiPanelIcon), Theme.getColor(Theme.key_chat_emojiPanelIconSelected));
        drawableArr[6] = this.stickersDrawable;
        this.icons = drawableArr;
        this.showGifs = needGif;
        this.info = chatFull;
        this.dotPaint = new Paint(1);
        this.dotPaint.setColor(Theme.getColor(Theme.key_chat_emojiPanelNewTrending));
        if (VERSION.SDK_INT >= 21) {
            this.outlineProvider = new CLASSNAME();
        }
        for (int i = 0; i < EmojiData.dataColored.length + 1; i++) {
            GridView gridView = new GridView(context);
            if (AndroidUtilities.isTablet()) {
                gridView.setColumnWidth(AndroidUtilities.m9dp(60.0f));
            } else {
                gridView.setColumnWidth(AndroidUtilities.m9dp(45.0f));
            }
            gridView.setNumColumns(-1);
            EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(i - 1);
            gridView.setAdapter(emojiGridAdapter);
            this.adapters.add(emojiGridAdapter);
            this.emojiGrids.add(gridView);
            frameLayout = new FrameLayout(context);
            frameLayout.addView(gridView, LayoutHelper.createFrame(-1, -1.0f, 51, 0.0f, 48.0f, 0.0f, 0.0f));
            this.views.add(frameLayout);
        }
        if (needStickers) {
            this.stickersWrap = new FrameLayout(context);
            DataQuery.getInstance(this.currentAccount).checkStickers(0);
            DataQuery.getInstance(this.currentAccount).checkFeaturedStickers();
            this.stickersGridView = new RecyclerListView(context) {
                boolean ignoreLayout;

                public boolean onInterceptTouchEvent(MotionEvent event) {
                    return super.onInterceptTouchEvent(event) || StickerPreviewViewer.getInstance().onInterceptTouchEvent(event, EmojiView.this.stickersGridView, EmojiView.this.getMeasuredHeight(), EmojiView.this.stickerPreviewViewerDelegate);
                }

                public void setVisibility(int visibility) {
                    if ((EmojiView.this.gifsGridView == null || EmojiView.this.gifsGridView.getVisibility() != 0) && (EmojiView.this.trendingGridView == null || EmojiView.this.trendingGridView.getVisibility() != 0)) {
                        super.setVisibility(visibility);
                    } else {
                        super.setVisibility(8);
                    }
                }

                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    if (EmojiView.this.firstAttach && EmojiView.this.stickersGridAdapter.getItemCount() > 0) {
                        this.ignoreLayout = true;
                        EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
                        EmojiView.this.firstAttach = false;
                        this.ignoreLayout = false;
                    }
                    super.onLayout(changed, l, t, r, b);
                    EmojiView.this.checkSearchFieldScroll();
                }

                public void requestLayout() {
                    if (!this.ignoreLayout) {
                        super.requestLayout();
                    }
                }
            };
            RecyclerListView recyclerListView = this.stickersGridView;
            LayoutManager gridLayoutManager = new GridLayoutManager(context, 5);
            this.stickersLayoutManager = gridLayoutManager;
            recyclerListView.setLayoutManager(gridLayoutManager);
            this.stickersLayoutManager.setSpanSizeLookup(new CLASSNAME());
            this.stickersGridView.setPadding(0, AndroidUtilities.m9dp(52.0f), 0, 0);
            this.stickersGridView.setClipToPadding(false);
            this.views.add(this.stickersWrap);
            this.stickersSearchGridAdapter = new StickersSearchGridAdapter(context);
            recyclerListView = this.stickersGridView;
            Adapter stickersGridAdapter = new StickersGridAdapter(context);
            this.stickersGridAdapter = stickersGridAdapter;
            recyclerListView.setAdapter(stickersGridAdapter);
            this.stickersGridView.setOnTouchListener(new EmojiView$$Lambda$0(this));
            this.stickersOnItemClickListener = new EmojiView$$Lambda$1(this);
            this.stickersGridView.setOnItemClickListener(this.stickersOnItemClickListener);
            this.stickersGridView.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.stickersWrap.addView(this.stickersGridView);
            this.searchEditTextContainer = new FrameLayout(context);
            this.searchEditTextContainer.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.stickersWrap.addView(this.searchEditTextContainer, new LayoutParams(-1, this.searchFieldHeight));
            this.searchBackground = new View(context);
            this.searchBackground.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.m9dp(3.0f), Theme.getColor(Theme.key_chat_emojiSearchBackground)));
            this.searchEditTextContainer.addView(this.searchBackground, LayoutHelper.createFrame(-1, 36.0f, 51, 14.0f, 14.0f, 14.0f, 0.0f));
            this.searchIconImageView = new ImageView(context);
            this.searchIconImageView.setScaleType(ScaleType.CENTER);
            this.searchIconImageView.setImageResource(R.drawable.sticker_search);
            this.searchIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelIcon), Mode.MULTIPLY));
            this.searchEditTextContainer.addView(this.searchIconImageView, LayoutHelper.createFrame(36, 36.0f, 51, 14.0f, 14.0f, 0.0f, 0.0f));
            this.clearSearchImageView = new ImageView(context);
            this.clearSearchImageView.setScaleType(ScaleType.CENTER);
            ImageView imageView = this.clearSearchImageView;
            Drawable closeProgressDrawable2 = new CloseProgressDrawable2();
            this.progressDrawable = closeProgressDrawable2;
            imageView.setImageDrawable(closeProgressDrawable2);
            this.clearSearchImageView.setScaleX(0.1f);
            this.clearSearchImageView.setScaleY(0.1f);
            this.clearSearchImageView.setAlpha(0.0f);
            this.clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelIcon), Mode.MULTIPLY));
            this.searchEditTextContainer.addView(this.clearSearchImageView, LayoutHelper.createFrame(36, 36.0f, 53, 14.0f, 14.0f, 14.0f, 0.0f));
            this.clearSearchImageView.setOnClickListener(new EmojiView$$Lambda$2(this));
            this.searchEditText = new EditTextBoldCursor(context) {

                /* renamed from: org.telegram.ui.Components.EmojiView$5$1 */
                class CLASSNAME extends AnimatorListenerAdapter {
                    CLASSNAME() {
                    }

                    public void onAnimationEnd(Animator animation) {
                        if (animation.equals(EmojiView.this.searchAnimation)) {
                            EmojiView.this.stickersGridView.setTranslationY(0.0f);
                            EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.m9dp(4.0f), 0, 0);
                            EmojiView.this.searchAnimation = null;
                        }
                    }

                    public void onAnimationCancel(Animator animation) {
                        if (animation.equals(EmojiView.this.searchAnimation)) {
                            EmojiView.this.searchAnimation = null;
                        }
                    }
                }

                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == 0) {
                        if (!EmojiView.this.listener.isSearchOpened()) {
                            if (EmojiView.this.searchAnimation != null) {
                                EmojiView.this.searchAnimation.cancel();
                                EmojiView.this.searchAnimation = null;
                            }
                            if (EmojiView.this.listener == null || !EmojiView.this.listener.isExpanded()) {
                                EmojiView.this.searchEditTextContainer.setTranslationY((float) AndroidUtilities.m9dp(0.0f));
                                EmojiView.this.stickersTab.setTranslationY((float) (-AndroidUtilities.m9dp(47.0f)));
                                EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.m9dp(4.0f), 0, 0);
                            } else {
                                EmojiView.this.searchAnimation = new AnimatorSet();
                                AnimatorSet access$2500 = EmojiView.this.searchAnimation;
                                r1 = new Animator[3];
                                r1[0] = ObjectAnimator.ofFloat(EmojiView.this.stickersTab, "translationY", new float[]{(float) (-AndroidUtilities.m9dp(47.0f))});
                                r1[1] = ObjectAnimator.ofFloat(EmojiView.this.stickersGridView, "translationY", new float[]{(float) (-AndroidUtilities.m9dp(48.0f))});
                                r1[2] = ObjectAnimator.ofFloat(EmojiView.this.searchEditTextContainer, "translationY", new float[]{(float) AndroidUtilities.m9dp(0.0f)});
                                access$2500.playTogether(r1);
                                EmojiView.this.searchAnimation.setDuration(200);
                                EmojiView.this.searchAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                                EmojiView.this.searchAnimation.addListener(new CLASSNAME());
                                EmojiView.this.searchAnimation.start();
                            }
                        }
                        EmojiView.this.listener.onSearchOpenClose(true);
                        EmojiView.this.searchEditText.requestFocus();
                        AndroidUtilities.showKeyboard(EmojiView.this.searchEditText);
                    }
                    return super.onTouchEvent(event);
                }
            };
            this.searchEditText.setTextSize(1, 16.0f);
            this.searchEditText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            this.searchEditText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            this.searchEditText.setBackgroundDrawable(null);
            this.searchEditText.setPadding(0, 0, 0, 0);
            this.searchEditText.setMaxLines(1);
            this.searchEditText.setLines(1);
            this.searchEditText.setSingleLine(true);
            this.searchEditText.setImeOptions(NUM);
            this.searchEditText.setHint(LocaleController.getString("SearchStickersHint", R.string.SearchStickersHint));
            this.searchEditText.setCursorColor(Theme.getColor(Theme.key_featuredStickers_addedIcon));
            this.searchEditText.setCursorSize(AndroidUtilities.m9dp(20.0f));
            this.searchEditText.setCursorWidth(1.5f);
            this.searchEditTextContainer.addView(this.searchEditText, LayoutHelper.createFrame(-1, 40.0f, 51, 46.0f, 12.0f, 46.0f, 0.0f));
            this.searchEditText.addTextChangedListener(new CLASSNAME());
            this.trendingGridView = new RecyclerListView(context);
            this.trendingGridView.setItemAnimator(null);
            this.trendingGridView.setLayoutAnimation(null);
            recyclerListView = this.trendingGridView;
            gridLayoutManager = new GridLayoutManager(context, 5) {
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };
            this.trendingLayoutManager = gridLayoutManager;
            recyclerListView.setLayoutManager(gridLayoutManager);
            this.trendingLayoutManager.setSpanSizeLookup(new CLASSNAME());
            this.trendingGridView.setOnScrollListener(new CLASSNAME());
            this.trendingGridView.setClipToPadding(false);
            this.trendingGridView.setPadding(0, AndroidUtilities.m9dp(48.0f), 0, 0);
            recyclerListView = this.trendingGridView;
            stickersGridAdapter = new TrendingGridAdapter(context);
            this.trendingGridAdapter = stickersGridAdapter;
            recyclerListView.setAdapter(stickersGridAdapter);
            this.trendingGridView.setOnItemClickListener(new EmojiView$$Lambda$3(this));
            this.trendingGridAdapter.notifyDataSetChanged();
            this.trendingGridView.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.trendingGridView.setVisibility(8);
            this.stickersWrap.addView(this.trendingGridView);
            if (needGif) {
                this.gifsGridView = new RecyclerListView(context);
                this.gifsGridView.setClipToPadding(false);
                this.gifsGridView.setPadding(0, AndroidUtilities.m9dp(48.0f), 0, 0);
                recyclerListView = this.gifsGridView;
                gridLayoutManager = new ExtendedGridLayoutManager(context, 100) {
                    private Size size = new Size();

                    protected Size getSizeForItem(int i) {
                        float f;
                        float f2 = 100.0f;
                        Document document = (Document) EmojiView.this.recentGifs.get(i);
                        Size size = this.size;
                        if (document.thumb == null || document.thumb.var_w == 0) {
                            f = 100.0f;
                        } else {
                            f = (float) document.thumb.var_w;
                        }
                        size.width = f;
                        Size size2 = this.size;
                        if (!(document.thumb == null || document.thumb.var_h == 0)) {
                            f2 = (float) document.thumb.var_h;
                        }
                        size2.height = f2;
                        for (int b = 0; b < document.attributes.size(); b++) {
                            DocumentAttribute attribute = (DocumentAttribute) document.attributes.get(b);
                            if ((attribute instanceof TL_documentAttributeImageSize) || (attribute instanceof TL_documentAttributeVideo)) {
                                this.size.width = (float) attribute.var_w;
                                this.size.height = (float) attribute.var_h;
                                break;
                            }
                        }
                        return this.size;
                    }
                };
                this.flowLayoutManager = gridLayoutManager;
                recyclerListView.setLayoutManager(gridLayoutManager);
                this.flowLayoutManager.setSpanSizeLookup(new CLASSNAME());
                this.gifsGridView.addItemDecoration(new CLASSNAME());
                this.gifsGridView.setOverScrollMode(2);
                recyclerListView = this.gifsGridView;
                stickersGridAdapter = new GifsAdapter(context);
                this.gifsAdapter = stickersGridAdapter;
                recyclerListView.setAdapter(stickersGridAdapter);
                this.gifsGridView.setOnScrollListener(new CLASSNAME());
                this.gifsGridView.setOnItemClickListener(new EmojiView$$Lambda$4(this));
                this.gifsGridView.setOnItemLongClickListener(new EmojiView$$Lambda$5(this));
                this.gifsGridView.setVisibility(8);
                this.stickersWrap.addView(this.gifsGridView);
            }
            this.stickersEmptyView = new TextView(context);
            this.stickersEmptyView.setText(LocaleController.getString("NoStickers", R.string.NoStickers));
            this.stickersEmptyView.setTextSize(1, 18.0f);
            this.stickersEmptyView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
            this.stickersEmptyView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.stickersWrap.addView(this.stickersEmptyView, LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 48.0f, 0.0f, 0.0f));
            this.stickersGridView.setEmptyView(this.stickersEmptyView);
            this.stickersTab = new ScrollSlidingTabStrip(context) {
                float downX;
                float downY;
                boolean draggingHorizontally;
                boolean draggingVertically;
                boolean first = true;
                float lastTranslateX;
                float lastX;
                boolean startedScroll;
                final int touchslop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                VelocityTracker vTracker;

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (ev.getAction() == 0) {
                        this.draggingHorizontally = false;
                        this.draggingVertically = false;
                        this.downX = ev.getRawX();
                        this.downY = ev.getRawY();
                    } else if (!(this.draggingVertically || this.draggingHorizontally || EmojiView.this.dragListener == null || Math.abs(ev.getRawY() - this.downY) < ((float) this.touchslop))) {
                        this.draggingVertically = true;
                        this.downY = ev.getRawY();
                        EmojiView.this.dragListener.onDragStart();
                        if (!this.startedScroll) {
                            return true;
                        }
                        EmojiView.this.pager.endFakeDrag();
                        this.startedScroll = false;
                        return true;
                    }
                    return super.onInterceptTouchEvent(ev);
                }

                public boolean onTouchEvent(MotionEvent ev) {
                    boolean z = false;
                    if (this.first) {
                        this.first = false;
                        this.lastX = ev.getX();
                    }
                    if (ev.getAction() == 0) {
                        this.draggingHorizontally = false;
                        this.draggingVertically = false;
                        this.downX = ev.getRawX();
                        this.downY = ev.getRawY();
                    } else if (!(this.draggingVertically || this.draggingHorizontally || EmojiView.this.dragListener == null)) {
                        if (Math.abs(ev.getRawX() - this.downX) >= ((float) this.touchslop)) {
                            this.draggingHorizontally = true;
                        } else if (Math.abs(ev.getRawY() - this.downY) >= ((float) this.touchslop)) {
                            this.draggingVertically = true;
                            this.downY = ev.getRawY();
                            EmojiView.this.dragListener.onDragStart();
                            if (this.startedScroll) {
                                EmojiView.this.pager.endFakeDrag();
                                this.startedScroll = false;
                            }
                        }
                    }
                    if (this.draggingVertically) {
                        if (this.vTracker == null) {
                            this.vTracker = VelocityTracker.obtain();
                        }
                        this.vTracker.addMovement(ev);
                        if (ev.getAction() == 1 || ev.getAction() == 3) {
                            this.vTracker.computeCurrentVelocity(1000);
                            float velocity = this.vTracker.getYVelocity();
                            this.vTracker.recycle();
                            this.vTracker = null;
                            if (ev.getAction() == 1) {
                                EmojiView.this.dragListener.onDragEnd(velocity);
                            } else {
                                EmojiView.this.dragListener.onDragCancel();
                            }
                            this.first = true;
                            this.draggingHorizontally = false;
                            this.draggingVertically = false;
                            return true;
                        }
                        EmojiView.this.dragListener.onDrag(Math.round(ev.getRawY() - this.downY));
                        return true;
                    }
                    float newTranslationX = EmojiView.this.stickersTab.getTranslationX();
                    if (EmojiView.this.stickersTab.getScrollX() == 0 && newTranslationX == 0.0f) {
                        if (this.startedScroll || this.lastX - ev.getX() >= 0.0f) {
                            if (this.startedScroll && this.lastX - ev.getX() > 0.0f && EmojiView.this.pager.isFakeDragging()) {
                                EmojiView.this.pager.endFakeDrag();
                                this.startedScroll = false;
                            }
                        } else if (EmojiView.this.pager.beginFakeDrag()) {
                            this.startedScroll = true;
                            this.lastTranslateX = EmojiView.this.stickersTab.getTranslationX();
                        }
                    }
                    if (this.startedScroll) {
                        try {
                            EmojiView.this.pager.fakeDragBy((float) ((int) (((ev.getX() - this.lastX) + newTranslationX) - this.lastTranslateX)));
                            this.lastTranslateX = newTranslationX;
                        } catch (Throwable e) {
                            try {
                                EmojiView.this.pager.endFakeDrag();
                            } catch (Exception e2) {
                            }
                            this.startedScroll = false;
                            FileLog.m13e(e);
                        }
                    }
                    this.lastX = ev.getX();
                    if (ev.getAction() == 3 || ev.getAction() == 1) {
                        this.first = true;
                        this.draggingHorizontally = false;
                        this.draggingVertically = false;
                        if (this.startedScroll) {
                            EmojiView.this.pager.endFakeDrag();
                            this.startedScroll = false;
                        }
                    }
                    if (this.startedScroll || super.onTouchEvent(ev)) {
                        z = true;
                    }
                    return z;
                }
            };
            this.stickersTab.setUnderlineHeight(AndroidUtilities.m9dp(1.0f));
            this.stickersTab.setIndicatorColor(Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelector));
            this.stickersTab.setUnderlineColor(Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelector));
            this.stickersTab.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.stickersTab.setVisibility(4);
            addView(this.stickersTab, LayoutHelper.createFrame(-1, 48, 51));
            this.stickersTab.setTranslationX((float) AndroidUtilities.displaySize.x);
            updateStickerTabs();
            this.stickersTab.setDelegate(new EmojiView$$Lambda$6(this));
            this.stickersGridView.setOnScrollListener(new CLASSNAME());
        }
        this.pager = new ViewPager(context) {
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        this.pager.setAdapter(new EmojiPagesAdapter(this, null));
        this.emojiTab = new LinearLayout(context) {
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        this.emojiTab.setOrientation(0);
        addView(this.emojiTab, LayoutHelper.createFrame(-1, 48.0f));
        this.pagerSlidingTabStrip = new PagerSlidingTabStrip(context);
        this.pagerSlidingTabStrip.setViewPager(this.pager);
        this.pagerSlidingTabStrip.setShouldExpand(true);
        this.pagerSlidingTabStrip.setIndicatorHeight(AndroidUtilities.m9dp(2.0f));
        this.pagerSlidingTabStrip.setUnderlineHeight(AndroidUtilities.m9dp(1.0f));
        this.pagerSlidingTabStrip.setIndicatorColor(Theme.getColor(Theme.key_chat_emojiPanelIconSelector));
        this.pagerSlidingTabStrip.setUnderlineColor(Theme.getColor(Theme.key_chat_emojiPanelShadowLine));
        this.emojiTab.addView(this.pagerSlidingTabStrip, LayoutHelper.createLinear(0, 48, 1.0f));
        this.pagerSlidingTabStrip.setOnPageChangeListener(new CLASSNAME());
        frameLayout = new FrameLayout(context);
        this.emojiTab.addView(frameLayout, LayoutHelper.createLinear(52, 48));
        this.backspaceButton = new ImageView(context) {
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == 0) {
                    EmojiView.this.backspacePressed = true;
                    EmojiView.this.backspaceOnce = false;
                    EmojiView.this.postBackspaceRunnable(350);
                } else if (event.getAction() == 3 || event.getAction() == 1) {
                    EmojiView.this.backspacePressed = false;
                    if (!(EmojiView.this.backspaceOnce || EmojiView.this.listener == null || !EmojiView.this.listener.onBackspace())) {
                        EmojiView.this.backspaceButton.performHapticFeedback(3);
                    }
                }
                super.onTouchEvent(event);
                return true;
            }
        };
        this.backspaceButton.setImageResource(R.drawable.ic_smiles_backspace);
        this.backspaceButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelBackspace), Mode.MULTIPLY));
        this.backspaceButton.setScaleType(ScaleType.CENTER);
        frameLayout.addView(this.backspaceButton, LayoutHelper.createFrame(52, 48.0f));
        this.shadowLine = new View(context);
        this.shadowLine.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelShadowLine));
        frameLayout.addView(this.shadowLine, LayoutHelper.createFrame(52, 1, 83));
        this.noRecentTextView = new TextView(context);
        this.noRecentTextView.setText(LocaleController.getString("NoRecent", R.string.NoRecent));
        this.noRecentTextView.setTextSize(1, 18.0f);
        this.noRecentTextView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
        this.noRecentTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.noRecentTextView.setGravity(17);
        this.noRecentTextView.setClickable(false);
        this.noRecentTextView.setFocusable(false);
        ((FrameLayout) this.views.get(0)).addView(this.noRecentTextView, LayoutHelper.createFrame(-2, -2.0f, 17, 0.0f, 48.0f, 0.0f, 0.0f));
        ((GridView) this.emojiGrids.get(0)).setEmptyView(this.noRecentTextView);
        addView(this.pager, 0, LayoutHelper.createFrame(-1, -1, 51));
        this.mediaBanTooltip = new CorrectlyMeasuringTextView(context);
        this.mediaBanTooltip.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.m9dp(3.0f), Theme.getColor(Theme.key_chat_gifSaveHintBackground)));
        this.mediaBanTooltip.setTextColor(Theme.getColor(Theme.key_chat_gifSaveHintText));
        this.mediaBanTooltip.setPadding(AndroidUtilities.m9dp(8.0f), AndroidUtilities.m9dp(7.0f), AndroidUtilities.m9dp(8.0f), AndroidUtilities.m9dp(7.0f));
        this.mediaBanTooltip.setGravity(16);
        this.mediaBanTooltip.setTextSize(1, 14.0f);
        this.mediaBanTooltip.setVisibility(4);
        addView(this.mediaBanTooltip, LayoutHelper.createFrame(-2, -2.0f, 53, 30.0f, 53.0f, 5.0f, 0.0f));
        this.emojiSize = AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 40.0f : 32.0f);
        this.pickerView = new EmojiColorPickerView(context);
        View view = this.pickerView;
        int dp = AndroidUtilities.m9dp((float) ((((AndroidUtilities.isTablet() ? 40 : 32) * 6) + 10) + 20));
        this.popupWidth = dp;
        int dp2 = AndroidUtilities.m9dp(AndroidUtilities.isTablet() ? 64.0f : 56.0f);
        this.popupHeight = dp2;
        this.pickerViewPopup = new EmojiPopupWindow(view, dp, dp2);
        this.pickerViewPopup.setOutsideTouchable(true);
        this.pickerViewPopup.setClippingEnabled(true);
        this.pickerViewPopup.setInputMethodMode(2);
        this.pickerViewPopup.setSoftInputMode(0);
        this.pickerViewPopup.getContentView().setFocusableInTouchMode(true);
        this.pickerViewPopup.getContentView().setOnKeyListener(new EmojiView$$Lambda$7(this));
        this.currentPage = MessagesController.getGlobalEmojiSettings().getInt("selected_page", 0);
        Emoji.loadRecentEmoji();
        ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
    }

    final /* synthetic */ boolean lambda$new$1$EmojiView(View v, MotionEvent event) {
        return StickerPreviewViewer.getInstance().onTouch(event, this.stickersGridView, getMeasuredHeight(), this.stickersOnItemClickListener, this.stickerPreviewViewerDelegate);
    }

    final /* synthetic */ void lambda$new$2$EmojiView(View view, int position) {
        if (this.stickersGridView.getAdapter() == this.stickersSearchGridAdapter) {
            StickerSetCovered pack = (StickerSetCovered) this.stickersSearchGridAdapter.positionsToSets.get(position);
            if (pack != null) {
                this.listener.onShowStickerSet(pack.set, null);
                return;
            }
        }
        if (view instanceof StickerEmojiCell) {
            StickerPreviewViewer.getInstance().reset();
            StickerEmojiCell cell = (StickerEmojiCell) view;
            if (!cell.isDisabled()) {
                cell.disable();
                this.listener.onStickerSelected(cell.getSticker(), cell.getParentObject());
            }
        }
    }

    final /* synthetic */ void lambda$new$3$EmojiView(View v) {
        this.searchEditText.setText(TtmlNode.ANONYMOUS_REGION_ID);
        AndroidUtilities.showKeyboard(this.searchEditText);
    }

    final /* synthetic */ void lambda$new$4$EmojiView(View view, int position) {
        StickerSetCovered pack = (StickerSetCovered) this.trendingGridAdapter.positionsToSets.get(position);
        if (pack != null) {
            this.listener.onShowStickerSet(pack.set, null);
        }
    }

    final /* synthetic */ void lambda$new$5$EmojiView(View view, int position) {
        if (position >= 0 && position < this.recentGifs.size() && this.listener != null) {
            this.listener.onGifSelected((Document) this.recentGifs.get(position), "gif");
        }
    }

    final /* synthetic */ boolean lambda$new$7$EmojiView(View view, int position) {
        if (position < 0 || position >= this.recentGifs.size()) {
            return false;
        }
        Document searchImage = (Document) this.recentGifs.get(position);
        Builder builder = new Builder(view.getContext());
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setMessage(LocaleController.getString("DeleteGif", R.string.DeleteGif));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK).toUpperCase(), new EmojiView$$Lambda$10(this, searchImage));
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show().setCanceledOnTouchOutside(true);
        return true;
    }

    final /* synthetic */ void lambda$null$6$EmojiView(Document searchImage, DialogInterface dialogInterface, int i) {
        DataQuery.getInstance(this.currentAccount).removeRecentGif(searchImage);
        this.recentGifs = DataQuery.getInstance(this.currentAccount).getRecentGifs();
        if (this.gifsAdapter != null) {
            this.gifsAdapter.notifyDataSetChanged();
        }
        if (this.recentGifs.isEmpty()) {
            updateStickerTabs();
            if (this.stickersGridAdapter != null) {
                this.stickersGridAdapter.notifyDataSetChanged();
            }
        }
    }

    final /* synthetic */ void lambda$new$8$EmojiView(int page) {
        int i = 8;
        if (this.gifsGridView != null) {
            TextView textView;
            if (page == this.gifTabNum + 1) {
                if (this.gifsGridView.getVisibility() != 0) {
                    this.listener.onGifTab(true);
                    showGifTab();
                }
            } else if (page == this.trendingTabNum + 1) {
                if (this.trendingGridView.getVisibility() != 0) {
                    showTrendingTab();
                }
            } else if (this.gifsGridView.getVisibility() == 0) {
                this.listener.onGifTab(false);
                this.gifsGridView.setVisibility(8);
                this.stickersGridView.setVisibility(0);
                this.searchEditTextContainer.setVisibility(0);
                int vis = this.stickersGridView.getVisibility();
                textView = this.stickersEmptyView;
                if (this.stickersGridAdapter.getItemCount() == 0) {
                    i = 0;
                }
                textView.setVisibility(i);
                checkScroll();
                saveNewPage();
            } else if (this.trendingGridView.getVisibility() == 0) {
                this.trendingGridView.setVisibility(8);
                this.stickersGridView.setVisibility(0);
                this.searchEditTextContainer.setVisibility(0);
                textView = this.stickersEmptyView;
                if (this.stickersGridAdapter.getItemCount() == 0) {
                    i = 0;
                }
                textView.setVisibility(i);
                saveNewPage();
            }
        }
        if (page == 0) {
            this.pager.setCurrentItem(0);
        } else if (page != this.gifTabNum + 1 && page != this.trendingTabNum + 1) {
            if (page == this.recentTabBum + 1) {
                this.stickersLayoutManager.scrollToPositionWithOffset(this.stickersGridAdapter.getPositionForPack("recent"), 0);
                checkStickersTabY(null, 0);
                this.stickersTab.onPageScrolled(this.recentTabBum + 1, (this.recentTabBum > 0 ? this.recentTabBum : this.stickersTabOffset) + 1);
            } else if (page == this.favTabBum + 1) {
                this.stickersLayoutManager.scrollToPositionWithOffset(this.stickersGridAdapter.getPositionForPack("fav"), 0);
                checkStickersTabY(null, 0);
                this.stickersTab.onPageScrolled(this.favTabBum + 1, (this.favTabBum > 0 ? this.favTabBum : this.stickersTabOffset) + 1);
            } else {
                int index = (page - 1) - this.stickersTabOffset;
                if (index < this.stickerSets.size()) {
                    if (index >= this.stickerSets.size()) {
                        index = this.stickerSets.size() - 1;
                    }
                    this.firstAttach = false;
                    this.stickersLayoutManager.scrollToPositionWithOffset(this.stickersGridAdapter.getPositionForPack(this.stickerSets.get(index)), 0);
                    checkStickersTabY(null, 0);
                    checkScroll();
                } else if (this.listener != null) {
                    this.listener.onStickersSettingsClick();
                }
            }
        }
    }

    final /* synthetic */ boolean lambda$new$9$EmojiView(View v, int keyCode, KeyEvent event) {
        if (keyCode != 82 || event.getRepeatCount() != 0 || event.getAction() != 1 || this.pickerViewPopup == null || !this.pickerViewPopup.isShowing()) {
            return false;
        }
        this.pickerViewPopup.dismiss();
        return true;
    }

    public void showSearchField(boolean show) {
        int position = this.stickersLayoutManager.findFirstVisibleItemPosition();
        if (show) {
            if (position == 1 || position == 2) {
                this.stickersLayoutManager.scrollToPosition(0);
                this.stickersTab.setTranslationY(0.0f);
            }
        } else if (position == 0) {
            this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
        }
    }

    public void hideSearchKeyboard() {
        AndroidUtilities.hideKeyboard(this.searchEditText);
    }

    public void closeSearch(boolean animated) {
        closeSearch(animated, -1);
    }

    public void closeSearch(boolean animated, long scrollToSet) {
        if (this.searchAnimation != null) {
            this.searchAnimation.cancel();
            this.searchAnimation = null;
        }
        this.searchEditText.setText(TtmlNode.ANONYMOUS_REGION_ID);
        if (scrollToSet != -1) {
            TL_messages_stickerSet set = DataQuery.getInstance(this.currentAccount).getStickerSetById(scrollToSet);
            if (set != null) {
                int pos = this.stickersGridAdapter.getPositionForPack(set);
                if (pos >= 0) {
                    this.stickersLayoutManager.scrollToPositionWithOffset(pos, AndroidUtilities.m9dp(60.0f));
                }
            }
        }
        if (animated) {
            this.searchAnimation = new AnimatorSet();
            AnimatorSet animatorSet = this.searchAnimation;
            r3 = new Animator[3];
            r3[0] = ObjectAnimator.ofFloat(this.stickersTab, "translationY", new float[]{0.0f});
            r3[1] = ObjectAnimator.ofFloat(this.stickersGridView, "translationY", new float[]{(float) (AndroidUtilities.m9dp(48.0f) - this.searchFieldHeight)});
            r3[2] = ObjectAnimator.ofFloat(this.searchEditTextContainer, "translationY", new float[]{(float) (AndroidUtilities.m9dp(48.0f) - this.searchFieldHeight)});
            animatorSet.playTogether(r3);
            this.searchAnimation.setDuration(200);
            this.searchAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            this.searchAnimation.addListener(new CLASSNAME());
            this.searchAnimation.start();
            return;
        }
        this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
        this.searchEditTextContainer.setTranslationY((float) (AndroidUtilities.m9dp(48.0f) - this.searchFieldHeight));
        this.stickersTab.setTranslationY(0.0f);
        this.stickersGridView.setPadding(0, AndroidUtilities.m9dp(52.0f), 0, 0);
        this.listener.onSearchOpenClose(false);
    }

    private void checkStickersTabY(View list, int dy) {
        if (list == null) {
            ScrollSlidingTabStrip scrollSlidingTabStrip = this.stickersTab;
            this.minusDy = 0;
            scrollSlidingTabStrip.setTranslationY((float) null);
        } else if (list.getVisibility() != 0) {
        } else {
            if (this.listener == null || !this.listener.isSearchOpened()) {
                if (dy > 0 && this.stickersGridView != null && this.stickersGridView.getVisibility() == 0) {
                    ViewHolder holder = this.stickersGridView.findViewHolderForAdapterPosition(0);
                    if (holder != null && holder.itemView.getTop() + this.searchFieldHeight >= this.stickersGridView.getPaddingTop()) {
                        return;
                    }
                }
                this.minusDy -= dy;
                if (this.minusDy > 0) {
                    this.minusDy = 0;
                } else if (this.minusDy < (-AndroidUtilities.m9dp(288.0f))) {
                    this.minusDy = -AndroidUtilities.m9dp(288.0f);
                }
                this.stickersTab.setTranslationY((float) Math.max(-AndroidUtilities.m9dp(47.0f), this.minusDy));
            }
        }
    }

    private void checkSearchFieldScroll() {
        if (this.stickersGridView == null) {
            return;
        }
        if (this.listener == null || !this.listener.isSearchOpened()) {
            ViewHolder holder = this.stickersGridView.findViewHolderForAdapterPosition(0);
            if (holder != null) {
                this.searchEditTextContainer.setTranslationY((float) holder.itemView.getTop());
            } else {
                this.searchEditTextContainer.setTranslationY((float) (-this.searchFieldHeight));
            }
        }
    }

    private void checkScroll() {
        int firstVisibleItem = this.stickersLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItem != -1 && this.stickersGridView != null) {
            int firstTab;
            if (this.favTabBum > 0) {
                firstTab = this.favTabBum;
            } else if (this.recentTabBum > 0) {
                firstTab = this.recentTabBum;
            } else {
                firstTab = this.stickersTabOffset;
            }
            if (this.stickersGridView.getVisibility() != 0) {
                if (!(this.gifsGridView == null || this.gifsGridView.getVisibility() == 0)) {
                    this.gifsGridView.setVisibility(0);
                }
                if (this.stickersEmptyView != null && this.stickersEmptyView.getVisibility() == 0) {
                    this.stickersEmptyView.setVisibility(8);
                }
                this.stickersTab.onPageScrolled(this.gifTabNum + 1, firstTab + 1);
                return;
            }
            this.stickersTab.onPageScrolled(this.stickersGridAdapter.getTabForPosition(firstVisibleItem) + 1, firstTab + 1);
        }
    }

    private void saveNewPage() {
        int newPage;
        if (this.pager.getCurrentItem() != 6) {
            newPage = 0;
        } else if (this.gifsGridView == null || this.gifsGridView.getVisibility() != 0) {
            newPage = 1;
        } else {
            newPage = 2;
        }
        if (this.currentPage != newPage) {
            this.currentPage = newPage;
            MessagesController.getGlobalEmojiSettings().edit().putInt("selected_page", newPage).commit();
        }
    }

    public void clearRecentEmoji() {
        Emoji.clearRecentEmoji();
        ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
    }

    private void showTrendingTab() {
        this.trendingGridView.setVisibility(0);
        this.stickersGridView.setVisibility(8);
        this.firstAttach = true;
        this.searchEditTextContainer.setVisibility(8);
        this.stickersEmptyView.setVisibility(8);
        this.gifsGridView.setVisibility(8);
        this.stickersTab.onPageScrolled(this.trendingTabNum + 1, (this.recentTabBum > 0 ? this.recentTabBum : this.stickersTabOffset) + 1);
        saveNewPage();
    }

    private void showGifTab() {
        this.gifsGridView.setVisibility(0);
        this.stickersGridView.setVisibility(8);
        this.firstAttach = true;
        this.searchEditTextContainer.setVisibility(8);
        this.stickersEmptyView.setVisibility(8);
        this.trendingGridView.setVisibility(8);
        this.stickersTab.onPageScrolled(this.gifTabNum + 1, (this.recentTabBum > 0 ? this.recentTabBum : this.stickersTabOffset) + 1);
        saveNewPage();
    }

    private void onPageScrolled(int position, int width, int positionOffsetPixels) {
        boolean z = true;
        int i = 0;
        if (this.stickersTab != null) {
            if (width == 0) {
                width = AndroidUtilities.displaySize.x;
            }
            int margin = 0;
            if (position == 5) {
                margin = -positionOffsetPixels;
                if (this.listener != null) {
                    Listener listener = this.listener;
                    if (positionOffsetPixels == 0) {
                        z = false;
                    }
                    listener.onStickersTab(z);
                }
            } else if (position == 6) {
                margin = -width;
                if (this.listener != null) {
                    this.listener.onStickersTab(true);
                }
            } else if (this.listener != null) {
                this.listener.onStickersTab(false);
            }
            if (this.emojiTab.getTranslationX() != ((float) margin)) {
                this.emojiTab.setTranslationX((float) margin);
                this.stickersTab.setTranslationX((float) (width + margin));
                ScrollSlidingTabStrip scrollSlidingTabStrip = this.stickersTab;
                if (margin >= 0) {
                    i = 4;
                }
                scrollSlidingTabStrip.setVisibility(i);
            }
        }
    }

    private void postBackspaceRunnable(int time) {
        AndroidUtilities.runOnUIThread(new EmojiView$$Lambda$8(this, time), (long) time);
    }

    final /* synthetic */ void lambda$postBackspaceRunnable$10$EmojiView(int time) {
        if (this.backspacePressed) {
            if (this.listener != null && this.listener.onBackspace()) {
                this.backspaceButton.performHapticFeedback(3);
            }
            this.backspaceOnce = true;
            postBackspaceRunnable(Math.max(50, time - 100));
        }
    }

    public void switchToGifRecent() {
        if (this.gifTabNum < 0 || this.recentGifs.isEmpty()) {
            this.switchToGifTab = true;
        } else {
            this.stickersTab.selectTab(this.gifTabNum + 1);
        }
        this.pager.setCurrentItem(6);
    }

    private void updateStickerTabs() {
        if (this.stickersTab != null) {
            int a;
            TL_messages_stickerSet pack;
            Chat chat;
            this.recentTabBum = -2;
            this.favTabBum = -2;
            this.gifTabNum = -2;
            this.trendingTabNum = -2;
            this.stickersTabOffset = 0;
            int lastPosition = this.stickersTab.getCurrentPosition();
            this.stickersTab.removeTabs();
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles2_smile);
            Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
            this.stickersTab.addIconTab(drawable);
            if (this.showGifs && !this.recentGifs.isEmpty()) {
                drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles_gif);
                Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
                this.stickersTab.addIconTab(drawable);
                this.gifTabNum = this.stickersTabOffset;
                this.stickersTabOffset++;
            }
            ArrayList<Long> unread = DataQuery.getInstance(this.currentAccount).getUnreadStickerSets();
            if (!(this.trendingGridAdapter == null || this.trendingGridAdapter.getItemCount() == 0 || unread.isEmpty())) {
                drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles_trend);
                Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
                this.stickersCounter = this.stickersTab.addIconTabWithCounter(drawable);
                this.trendingTabNum = this.stickersTabOffset;
                this.stickersTabOffset++;
                this.stickersCounter.setText(String.format("%d", new Object[]{Integer.valueOf(unread.size())}));
            }
            if (!this.favouriteStickers.isEmpty()) {
                this.favTabBum = this.stickersTabOffset;
                this.stickersTabOffset++;
                drawable = getContext().getResources().getDrawable(R.drawable.staredstickerstab);
                Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
                this.stickersTab.addIconTab(drawable);
            }
            if (!this.recentStickers.isEmpty()) {
                this.recentTabBum = this.stickersTabOffset;
                this.stickersTabOffset++;
                drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles2_recent);
                Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
                this.stickersTab.addIconTab(drawable);
            }
            this.stickerSets.clear();
            this.groupStickerSet = null;
            this.groupStickerPackPosition = -1;
            this.groupStickerPackNum = -10;
            ArrayList<TL_messages_stickerSet> packs = DataQuery.getInstance(this.currentAccount).getStickerSets(0);
            for (a = 0; a < packs.size(); a++) {
                pack = (TL_messages_stickerSet) packs.get(a);
                if (!(pack.set.archived || pack.documents == null || pack.documents.isEmpty())) {
                    this.stickerSets.add(pack);
                }
            }
            if (this.info != null) {
                long hiddenStickerSetId = MessagesController.getEmojiSettings(this.currentAccount).getLong("group_hide_stickers_" + this.info.var_id, -1);
                chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.info.var_id));
                if (chat == null || this.info.stickerset == null || !ChatObject.hasAdminRights(chat)) {
                    this.groupStickersHidden = hiddenStickerSetId != -1;
                } else if (this.info.stickerset != null) {
                    this.groupStickersHidden = hiddenStickerSetId == this.info.stickerset.var_id;
                }
                if (this.info.stickerset != null) {
                    pack = DataQuery.getInstance(this.currentAccount).getGroupStickerSetById(this.info.stickerset);
                    if (!(pack == null || pack.documents == null || pack.documents.isEmpty() || pack.set == null)) {
                        TL_messages_stickerSet set = new TL_messages_stickerSet();
                        set.documents = pack.documents;
                        set.packs = pack.packs;
                        set.set = pack.set;
                        if (this.groupStickersHidden) {
                            this.groupStickerPackNum = this.stickerSets.size();
                            this.stickerSets.add(set);
                        } else {
                            this.groupStickerPackNum = 0;
                            this.stickerSets.add(0, set);
                        }
                        if (!this.info.can_set_stickers) {
                            set = null;
                        }
                        this.groupStickerSet = set;
                    }
                } else if (this.info.can_set_stickers) {
                    pack = new TL_messages_stickerSet();
                    if (this.groupStickersHidden) {
                        this.groupStickerPackNum = this.stickerSets.size();
                        this.stickerSets.add(pack);
                    } else {
                        this.groupStickerPackNum = 0;
                        this.stickerSets.add(0, pack);
                    }
                }
            }
            a = 0;
            while (a < this.stickerSets.size()) {
                if (a == this.groupStickerPackNum) {
                    chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.info.var_id));
                    if (chat == null) {
                        this.stickerSets.remove(0);
                        a--;
                    } else {
                        this.stickersTab.addStickerTab(chat);
                    }
                } else {
                    TL_messages_stickerSet stickerSet = (TL_messages_stickerSet) this.stickerSets.get(a);
                    this.stickersTab.addStickerTab((Document) stickerSet.documents.get(0), stickerSet);
                }
                a++;
            }
            if (!(this.trendingGridAdapter == null || this.trendingGridAdapter.getItemCount() == 0 || !unread.isEmpty())) {
                drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles_trend);
                Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
                this.trendingTabNum = this.stickersTabOffset + this.stickerSets.size();
                this.stickersTab.addIconTab(drawable);
            }
            drawable = getContext().getResources().getDrawable(R.drawable.ic_smiles_settings);
            Theme.setDrawableColorByKey(drawable, Theme.key_chat_emojiPanelIcon);
            this.stickersTab.addIconTab(drawable);
            this.stickersTab.updateTabStyles();
            if (lastPosition != 0) {
                this.stickersTab.onPageScrolled(lastPosition, lastPosition);
            }
            if (this.switchToGifTab && this.gifTabNum >= 0 && this.gifsGridView.getVisibility() != 0) {
                showGifTab();
                this.switchToGifTab = false;
            }
            checkPanels();
        }
    }

    private void checkPanels() {
        int i = 8;
        if (this.stickersTab != null) {
            if (this.trendingTabNum == -2 && this.trendingGridView != null && this.trendingGridView.getVisibility() == 0) {
                this.gifsGridView.setVisibility(8);
                this.trendingGridView.setVisibility(8);
                this.stickersGridView.setVisibility(0);
                this.searchEditTextContainer.setVisibility(0);
                this.stickersEmptyView.setVisibility(this.stickersGridAdapter.getItemCount() != 0 ? 8 : 0);
            }
            if (this.gifTabNum == -2 && this.gifsGridView != null && this.gifsGridView.getVisibility() == 0) {
                this.listener.onGifTab(false);
                this.gifsGridView.setVisibility(8);
                this.trendingGridView.setVisibility(8);
                this.stickersGridView.setVisibility(0);
                this.searchEditTextContainer.setVisibility(0);
                TextView textView = this.stickersEmptyView;
                if (this.stickersGridAdapter.getItemCount() == 0) {
                    i = 0;
                }
                textView.setVisibility(i);
            } else if (this.gifTabNum == -2) {
            } else {
                if (this.gifsGridView != null && this.gifsGridView.getVisibility() == 0) {
                    this.stickersTab.onPageScrolled(this.gifTabNum + 1, (this.recentTabBum > 0 ? this.recentTabBum : this.stickersTabOffset) + 1);
                } else if (this.trendingGridView == null || this.trendingGridView.getVisibility() != 0) {
                    int position = this.stickersLayoutManager.findFirstVisibleItemPosition();
                    if (position != -1) {
                        int firstTab;
                        if (this.favTabBum > 0) {
                            firstTab = this.favTabBum;
                        } else if (this.recentTabBum > 0) {
                            firstTab = this.recentTabBum;
                        } else {
                            firstTab = this.stickersTabOffset;
                        }
                        this.stickersTab.onPageScrolled(this.stickersGridAdapter.getTabForPosition(position) + 1, firstTab + 1);
                    }
                } else {
                    this.stickersTab.onPageScrolled(this.trendingTabNum + 1, (this.recentTabBum > 0 ? this.recentTabBum : this.stickersTabOffset) + 1);
                }
            }
        }
    }

    public void addRecentSticker(Document document) {
        if (document != null) {
            DataQuery.getInstance(this.currentAccount).addRecentSticker(0, null, document, (int) (System.currentTimeMillis() / 1000), false);
            boolean wasEmpty = this.recentStickers.isEmpty();
            this.recentStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(0);
            if (this.stickersGridAdapter != null) {
                this.stickersGridAdapter.notifyDataSetChanged();
            }
            if (wasEmpty) {
                updateStickerTabs();
            }
        }
    }

    public void addRecentGif(Document document) {
        if (document != null) {
            boolean wasEmpty = this.recentGifs.isEmpty();
            this.recentGifs = DataQuery.getInstance(this.currentAccount).getRecentGifs();
            if (this.gifsAdapter != null) {
                this.gifsAdapter.notifyDataSetChanged();
            }
            if (wasEmpty) {
                updateStickerTabs();
            }
        }
    }

    public void requestLayout() {
        if (!this.isLayout) {
            super.requestLayout();
        }
    }

    public void updateUIColors() {
        if (AndroidUtilities.isInMultiwindow || this.forseMultiwindowLayout) {
            getBackground().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelBackground), Mode.MULTIPLY));
        } else {
            setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.emojiTab.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        }
        if (this.searchEditTextContainer != null) {
            this.searchEditTextContainer.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        }
        if (this.dotPaint != null) {
            this.dotPaint.setColor(Theme.getColor(Theme.key_chat_emojiPanelNewTrending));
        }
        if (this.stickersGridView != null) {
            this.stickersGridView.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        }
        if (this.trendingGridView != null) {
            this.trendingGridView.setGlowColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        }
        if (this.stickersEmptyView != null) {
            this.stickersEmptyView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
        }
        if (this.stickersTab != null) {
            this.stickersTab.setIndicatorColor(Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelector));
            this.stickersTab.setUnderlineColor(Theme.getColor(Theme.key_chat_emojiPanelStickerPackSelector));
            this.stickersTab.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
        }
        if (this.pagerSlidingTabStrip != null) {
            this.pagerSlidingTabStrip.setIndicatorColor(Theme.getColor(Theme.key_chat_emojiPanelIconSelector));
            this.pagerSlidingTabStrip.setUnderlineColor(Theme.getColor(Theme.key_chat_emojiPanelShadowLine));
        }
        if (this.backspaceButton != null) {
            this.backspaceButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelBackspace), Mode.MULTIPLY));
        }
        if (this.searchIconImageView != null) {
            this.clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelIcon), Mode.MULTIPLY));
        }
        if (this.shadowLine != null) {
            this.shadowLine.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelShadowLine));
        }
        if (this.noRecentTextView != null) {
            this.noRecentTextView.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelEmptyText));
        }
        if (this.mediaBanTooltip != null) {
            ((ShapeDrawable) this.mediaBanTooltip.getBackground()).getPaint().setColor(Theme.getColor(Theme.key_chat_gifSaveHintBackground));
            this.mediaBanTooltip.setTextColor(Theme.getColor(Theme.key_chat_gifSaveHintText));
        }
        if (this.stickersCounter != null) {
            this.stickersCounter.setTextColor(Theme.getColor(Theme.key_chat_emojiPanelBadgeText));
            Theme.setDrawableColor(this.stickersCounter.getBackground(), Theme.getColor(Theme.key_chat_emojiPanelBadgeBackground));
            this.stickersCounter.invalidate();
        }
        Theme.setDrawableColorByKey(this.stickersDrawable, Theme.key_chat_emojiPanelIcon);
        for (int a = 0; a < this.icons.length - 1; a++) {
            Theme.setEmojiDrawableColor(this.icons[a], Theme.getColor(Theme.key_chat_emojiPanelIcon), false);
            Theme.setEmojiDrawableColor(this.icons[a], Theme.getColor(Theme.key_chat_emojiPanelIconSelected), true);
        }
        if (this.searchBackground != null) {
            Theme.setDrawableColorByKey(this.searchBackground.getBackground(), Theme.key_chat_emojiSearchBackground);
            this.searchBackground.invalidate();
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.isLayout = true;
        if (AndroidUtilities.isInMultiwindow || this.forseMultiwindowLayout) {
            if (this.currentBackgroundType != 1) {
                if (VERSION.SDK_INT >= 21) {
                    setOutlineProvider((ViewOutlineProvider) this.outlineProvider);
                    setClipToOutline(true);
                    setElevation((float) AndroidUtilities.m9dp(2.0f));
                }
                setBackgroundResource(R.drawable.smiles_popup);
                getBackground().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_emojiPanelBackground), Mode.MULTIPLY));
                this.emojiTab.setBackgroundDrawable(null);
                this.currentBackgroundType = 1;
            }
        } else if (this.currentBackgroundType != 0) {
            if (VERSION.SDK_INT >= 21) {
                setOutlineProvider(null);
                setClipToOutline(false);
                setElevation(0.0f);
            }
            setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.emojiTab.setBackgroundColor(Theme.getColor(Theme.key_chat_emojiPanelBackground));
            this.currentBackgroundType = 0;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.emojiTab.getLayoutParams();
        FrameLayout.LayoutParams layoutParams1 = null;
        layoutParams.width = MeasureSpec.getSize(widthMeasureSpec);
        if (this.stickersTab != null) {
            layoutParams1 = (FrameLayout.LayoutParams) this.stickersTab.getLayoutParams();
            if (layoutParams1 != null) {
                layoutParams1.width = layoutParams.width;
            }
        }
        if (layoutParams.width != this.oldWidth) {
            if (!(this.stickersTab == null || layoutParams1 == null)) {
                onPageScrolled(this.pager.getCurrentItem(), (layoutParams.width - getPaddingLeft()) - getPaddingRight(), 0);
                this.stickersTab.setLayoutParams(layoutParams1);
            }
            this.emojiTab.setLayoutParams(layoutParams);
            this.oldWidth = layoutParams.width;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(layoutParams.width, NUM), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), NUM));
        this.isLayout = false;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.lastNotifyWidth != right - left) {
            this.lastNotifyWidth = right - left;
            reloadStickersAdapter();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private void reloadStickersAdapter() {
        if (this.stickersGridAdapter != null) {
            this.stickersGridAdapter.notifyDataSetChanged();
        }
        if (this.trendingGridAdapter != null) {
            this.trendingGridAdapter.notifyDataSetChanged();
        }
        if (this.stickersSearchGridAdapter != null) {
            this.stickersSearchGridAdapter.notifyDataSetChanged();
        }
        if (StickerPreviewViewer.getInstance().isVisible()) {
            StickerPreviewViewer.getInstance().close();
        }
        StickerPreviewViewer.getInstance().reset();
    }

    public void setListener(Listener value) {
        this.listener = value;
    }

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }

    public void setChatInfo(ChatFull chatInfo) {
        this.info = chatInfo;
        updateStickerTabs();
    }

    public void invalidateViews() {
        for (int a = 0; a < this.emojiGrids.size(); a++) {
            ((GridView) this.emojiGrids.get(a)).invalidateViews();
        }
    }

    public void setForseMultiwindowLayout(boolean value) {
        this.forseMultiwindowLayout = value;
    }

    public void onOpen(boolean forceEmoji) {
        boolean z = true;
        if (this.stickersTab != null) {
            if (!(this.currentPage == 0 || this.currentChatId == 0)) {
                this.currentPage = 0;
            }
            if (this.currentPage == 0 || forceEmoji) {
                if (this.pager.getCurrentItem() == 6) {
                    ViewPager viewPager = this.pager;
                    if (forceEmoji) {
                        z = false;
                    }
                    viewPager.setCurrentItem(0, z);
                }
            } else if (this.currentPage == 1) {
                if (this.pager.getCurrentItem() != 6) {
                    this.pager.setCurrentItem(6);
                }
                if (this.stickersTab.getCurrentPosition() != this.gifTabNum + 1) {
                    return;
                }
                if (this.recentTabBum >= 0) {
                    this.stickersTab.selectTab(this.recentTabBum + 1);
                } else if (this.favTabBum >= 0) {
                    this.stickersTab.selectTab(this.favTabBum + 1);
                } else if (this.gifTabNum >= 0) {
                    this.stickersTab.selectTab(this.gifTabNum + 2);
                } else {
                    this.stickersTab.selectTab(1);
                }
            } else if (this.currentPage == 2) {
                if (this.pager.getCurrentItem() != 6) {
                    this.pager.setCurrentItem(6);
                }
                if (this.stickersTab.getCurrentPosition() == this.gifTabNum + 1) {
                    return;
                }
                if (this.gifTabNum < 0 || this.recentGifs.isEmpty()) {
                    this.switchToGifTab = true;
                } else {
                    this.stickersTab.selectTab(this.gifTabNum + 1);
                }
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.stickersGridAdapter != null) {
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.stickersDidLoad);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recentImagesDidLoad);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.featuredStickersDidLoad);
            NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.groupStickersDidLoad);
            AndroidUtilities.runOnUIThread(new EmojiView$$Lambda$9(this));
        }
    }

    final /* synthetic */ void lambda$onAttachedToWindow$11$EmojiView() {
        updateStickerTabs();
        reloadStickersAdapter();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != 8) {
            Emoji.sortEmoji();
            ((EmojiGridAdapter) this.adapters.get(0)).notifyDataSetChanged();
            if (this.stickersGridAdapter != null) {
                NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.stickersDidLoad);
                NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recentDocumentsDidLoad);
                updateStickerTabs();
                reloadStickersAdapter();
                if (!(this.gifsGridView == null || this.gifsGridView.getVisibility() != 0 || this.listener == null)) {
                    Listener listener = this.listener;
                    boolean z = this.pager != null && this.pager.getCurrentItem() >= 6;
                    listener.onGifTab(z);
                }
            }
            if (this.trendingGridAdapter != null) {
                this.trendingLoaded = false;
                this.trendingGridAdapter.notifyDataSetChanged();
            }
            checkDocuments(true);
            checkDocuments(false);
            DataQuery.getInstance(this.currentAccount).loadRecents(0, true, true, false);
            DataQuery.getInstance(this.currentAccount).loadRecents(0, false, true, false);
            DataQuery.getInstance(this.currentAccount).loadRecents(2, false, true, false);
        }
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void onDestroy() {
        if (this.stickersGridAdapter != null) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.stickersDidLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recentDocumentsDidLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.featuredStickersDidLoad);
            NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.groupStickersDidLoad);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.pickerViewPopup != null && this.pickerViewPopup.isShowing()) {
            this.pickerViewPopup.dismiss();
        }
    }

    private void checkDocuments(boolean isGif) {
        int previousCount;
        if (isGif) {
            previousCount = this.recentGifs.size();
            this.recentGifs = DataQuery.getInstance(this.currentAccount).getRecentGifs();
            if (this.gifsAdapter != null) {
                this.gifsAdapter.notifyDataSetChanged();
            }
            if (previousCount != this.recentGifs.size()) {
                updateStickerTabs();
            }
            if (this.stickersGridAdapter != null) {
                this.stickersGridAdapter.notifyDataSetChanged();
                return;
            }
            return;
        }
        previousCount = this.recentStickers.size();
        int previousCount2 = this.favouriteStickers.size();
        this.recentStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(0);
        this.favouriteStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(2);
        for (int a = 0; a < this.favouriteStickers.size(); a++) {
            Document favSticker = (Document) this.favouriteStickers.get(a);
            for (int b = 0; b < this.recentStickers.size(); b++) {
                Document recSticker = (Document) this.recentStickers.get(b);
                if (recSticker.dc_id == favSticker.dc_id && recSticker.var_id == favSticker.var_id) {
                    this.recentStickers.remove(b);
                    break;
                }
            }
        }
        if (!(previousCount == this.recentStickers.size() && previousCount2 == this.favouriteStickers.size())) {
            updateStickerTabs();
        }
        if (this.stickersGridAdapter != null) {
            this.stickersGridAdapter.notifyDataSetChanged();
        }
        checkPanels();
    }

    public void setStickersBanned(boolean value, int chatId) {
        if (value) {
            this.currentChatId = chatId;
        } else {
            this.currentChatId = 0;
        }
        View view = this.pagerSlidingTabStrip.getTab(6);
        if (view != null) {
            view.setAlpha(this.currentChatId != 0 ? 0.5f : 1.0f);
            if (this.currentChatId != 0 && this.pager.getCurrentItem() == 6) {
                this.pager.setCurrentItem(0);
            }
        }
    }

    public void showStickerBanHint() {
        if (this.mediaBanTooltip.getVisibility() != 0) {
            Chat chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.currentChatId));
            if (chat != null && chat.banned_rights != null) {
                if (AndroidUtilities.isBannedForever(chat.banned_rights.until_date)) {
                    this.mediaBanTooltip.setText(LocaleController.getString("AttachStickersRestrictedForever", R.string.AttachStickersRestrictedForever));
                } else {
                    this.mediaBanTooltip.setText(LocaleController.formatString("AttachStickersRestricted", R.string.AttachStickersRestricted, LocaleController.formatDateForBan((long) chat.banned_rights.until_date)));
                }
                this.mediaBanTooltip.setVisibility(0);
                AnimatorSet AnimatorSet = new AnimatorSet();
                AnimatorSet.playTogether(new Animator[]{ObjectAnimator.ofFloat(this.mediaBanTooltip, "alpha", new float[]{0.0f, 1.0f})});
                AnimatorSet.addListener(new CLASSNAME());
                AnimatorSet.setDuration(300);
                AnimatorSet.start();
            }
        }
    }

    private void updateVisibleTrendingSets() {
        if (this.trendingGridAdapter != null && this.trendingGridAdapter != null) {
            try {
                int count = this.trendingGridView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = this.trendingGridView.getChildAt(a);
                    if ((child instanceof FeaturedStickerSetInfoCell) && ((Holder) this.trendingGridView.getChildViewHolder(child)) != null) {
                        FeaturedStickerSetInfoCell cell = (FeaturedStickerSetInfoCell) child;
                        ArrayList<Long> unreadStickers = DataQuery.getInstance(this.currentAccount).getUnreadStickerSets();
                        StickerSetCovered stickerSetCovered = cell.getStickerSet();
                        boolean unread = unreadStickers != null && unreadStickers.contains(Long.valueOf(stickerSetCovered.set.var_id));
                        cell.setStickerSet(stickerSetCovered, unread);
                        if (unread) {
                            DataQuery.getInstance(this.currentAccount).markFaturedStickersByIdAsRead(stickerSetCovered.set.var_id);
                        }
                        boolean installing = this.installingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0;
                        boolean removing = this.removingStickerSets.indexOfKey(stickerSetCovered.set.var_id) >= 0;
                        if (installing || removing) {
                            if (installing && cell.isInstalled()) {
                                this.installingStickerSets.remove(stickerSetCovered.set.var_id);
                                installing = false;
                            } else if (removing) {
                                if (!cell.isInstalled()) {
                                    this.removingStickerSets.remove(stickerSetCovered.set.var_id);
                                    removing = false;
                                }
                            }
                        }
                        boolean z = installing || removing;
                        cell.setDrawProgress(z);
                    }
                }
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
        }
    }

    public boolean areThereAnyStickers() {
        return this.stickersGridAdapter != null && this.stickersGridAdapter.getItemCount() > 0;
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        int count;
        int a;
        if (id == NotificationCenter.stickersDidLoad) {
            if (((Integer) args[0]).intValue() == 0) {
                if (this.trendingGridAdapter != null) {
                    if (this.trendingLoaded) {
                        updateVisibleTrendingSets();
                    } else {
                        this.trendingGridAdapter.notifyDataSetChanged();
                    }
                }
                updateStickerTabs();
                reloadStickersAdapter();
                checkPanels();
            }
        } else if (id == NotificationCenter.recentDocumentsDidLoad) {
            boolean isGif = ((Boolean) args[0]).booleanValue();
            int type = ((Integer) args[1]).intValue();
            if (isGif || type == 0 || type == 2) {
                checkDocuments(isGif);
            }
        } else if (id == NotificationCenter.featuredStickersDidLoad) {
            if (this.trendingGridAdapter != null) {
                if (this.featuredStickersHash != DataQuery.getInstance(this.currentAccount).getFeaturesStickersHashWithoutUnread()) {
                    this.trendingLoaded = false;
                }
                if (this.trendingLoaded) {
                    updateVisibleTrendingSets();
                } else {
                    this.trendingGridAdapter.notifyDataSetChanged();
                }
            }
            if (this.pagerSlidingTabStrip != null) {
                count = this.pagerSlidingTabStrip.getChildCount();
                for (a = 0; a < count; a++) {
                    this.pagerSlidingTabStrip.getChildAt(a).invalidate();
                }
            }
            updateStickerTabs();
        } else if (id == NotificationCenter.groupStickersDidLoad) {
            if (this.info != null && this.info.stickerset != null && this.info.stickerset.var_id == ((Long) args[0]).longValue()) {
                updateStickerTabs();
            }
        } else if (id == NotificationCenter.emojiDidLoad && this.stickersGridView != null) {
            count = this.stickersGridView.getChildCount();
            for (a = 0; a < count; a++) {
                View child = this.stickersGridView.getChildAt(a);
                if (child instanceof StickerSetNameCell) {
                    child.invalidate();
                }
            }
        }
    }
}
