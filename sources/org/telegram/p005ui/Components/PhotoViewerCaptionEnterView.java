package org.telegram.p005ui.Components;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.google.android.exoplayer2.CLASSNAMEC;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.beta.R;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Components.EmojiView.Listener;
import org.telegram.p005ui.Components.SizeNotifierFrameLayoutPhoto.SizeNotifierFrameLayoutPhotoDelegate;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;

/* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView */
public class PhotoViewerCaptionEnterView extends FrameLayout implements NotificationCenterDelegate, SizeNotifierFrameLayoutPhotoDelegate {
    float animationProgress = 0.0f;
    private int audioInterfaceState;
    private int captionMaxLength = 1024;
    private ActionMode currentActionMode;
    private PhotoViewerCaptionEnterViewDelegate delegate;
    private ImageView emojiButton;
    private int emojiPadding;
    private EmojiView emojiView;
    private boolean forceFloatingEmoji;
    private boolean innerTextChange;
    private int keyboardHeight;
    private int keyboardHeightLand;
    private boolean keyboardVisible;
    private int lastSizeChangeValue1;
    private boolean lastSizeChangeValue2;
    private String lengthText;
    private TextPaint lengthTextPaint;
    private EditTextCaption messageEditText;
    private AnimatorSet runningAnimation;
    private AnimatorSet runningAnimation2;
    private ObjectAnimator runningAnimationAudio;
    private int runningAnimationType;
    private SizeNotifierFrameLayoutPhoto sizeNotifierLayout;
    private View windowView;

    /* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView$2 */
    class CLASSNAME implements Callback {
        CLASSNAME() {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            PhotoViewerCaptionEnterView.this.currentActionMode = mode;
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (VERSION.SDK_INT >= 23) {
                PhotoViewerCaptionEnterView.this.fixActionMode(mode);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            if (PhotoViewerCaptionEnterView.this.currentActionMode == mode) {
                PhotoViewerCaptionEnterView.this.currentActionMode = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView$3 */
    class CLASSNAME implements Callback {
        CLASSNAME() {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            PhotoViewerCaptionEnterView.this.currentActionMode = mode;
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (VERSION.SDK_INT >= 23) {
                PhotoViewerCaptionEnterView.this.fixActionMode(mode);
            }
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            if (PhotoViewerCaptionEnterView.this.currentActionMode == mode) {
                PhotoViewerCaptionEnterView.this.currentActionMode = null;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView$4 */
    class CLASSNAME implements TextWatcher {
        boolean processChange = false;

        CLASSNAME() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!PhotoViewerCaptionEnterView.this.innerTextChange) {
                if (PhotoViewerCaptionEnterView.this.delegate != null) {
                    PhotoViewerCaptionEnterView.this.delegate.onTextChanged(charSequence);
                }
                if (before != count && count - before > 1) {
                    this.processChange = true;
                }
            }
        }

        public void afterTextChanged(Editable editable) {
            if (PhotoViewerCaptionEnterView.this.captionMaxLength - PhotoViewerCaptionEnterView.this.messageEditText.length() <= 128) {
                PhotoViewerCaptionEnterView.this.lengthText = String.format("%d", new Object[]{Integer.valueOf(PhotoViewerCaptionEnterView.this.captionMaxLength - PhotoViewerCaptionEnterView.this.messageEditText.length())});
            } else {
                PhotoViewerCaptionEnterView.this.lengthText = null;
            }
            PhotoViewerCaptionEnterView.this.invalidate();
            if (!PhotoViewerCaptionEnterView.this.innerTextChange && this.processChange) {
                ImageSpan[] spans = (ImageSpan[]) editable.getSpans(0, editable.length(), ImageSpan.class);
                for (Object removeSpan : spans) {
                    editable.removeSpan(removeSpan);
                }
                Emoji.replaceEmoji(editable, PhotoViewerCaptionEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.m9dp(20.0f), false);
                this.processChange = false;
            }
        }
    }

    /* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView$PhotoViewerCaptionEnterViewDelegate */
    public interface PhotoViewerCaptionEnterViewDelegate {
        void onCaptionEnter();

        void onTextChanged(CharSequence charSequence);

        void onWindowSizeChanged(int i);
    }

    /* renamed from: org.telegram.ui.Components.PhotoViewerCaptionEnterView$5 */
    class CLASSNAME implements Listener {
        CLASSNAME() {
        }

        public boolean onBackspace() {
            if (PhotoViewerCaptionEnterView.this.messageEditText.length() == 0) {
                return false;
            }
            PhotoViewerCaptionEnterView.this.messageEditText.dispatchKeyEvent(new KeyEvent(0, 67));
            return true;
        }

        public void onEmojiSelected(String symbol) {
            if (PhotoViewerCaptionEnterView.this.messageEditText.length() + symbol.length() <= PhotoViewerCaptionEnterView.this.captionMaxLength) {
                int i = PhotoViewerCaptionEnterView.this.messageEditText.getSelectionEnd();
                if (i < 0) {
                    i = 0;
                }
                try {
                    PhotoViewerCaptionEnterView.this.innerTextChange = true;
                    CharSequence localCharSequence = Emoji.replaceEmoji(symbol, PhotoViewerCaptionEnterView.this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.m9dp(20.0f), false);
                    PhotoViewerCaptionEnterView.this.messageEditText.setText(PhotoViewerCaptionEnterView.this.messageEditText.getText().insert(i, localCharSequence));
                    int j = i + localCharSequence.length();
                    PhotoViewerCaptionEnterView.this.messageEditText.setSelection(j, j);
                } catch (Throwable e) {
                    FileLog.m13e(e);
                } finally {
                    PhotoViewerCaptionEnterView.this.innerTextChange = false;
                }
            }
        }

        public void onStickerSelected(Document sticker, Object parent) {
        }

        public void onStickersSettingsClick() {
        }

        public void onGifSelected(Document gif, Object parent) {
        }

        public void onGifTab(boolean opened) {
        }

        public void onStickersTab(boolean opened) {
        }

        public void onClearEmojiRecent() {
        }

        public void onShowStickerSet(StickerSet stickerSet, InputStickerSet inputStickerSet) {
        }

        public void onStickerSetAdd(StickerSetCovered stickerSet) {
        }

        public void onStickerSetRemove(StickerSetCovered stickerSet) {
        }

        public void onStickersGroupClick(int chatId) {
        }

        public void onSearchOpenClose(boolean open) {
        }

        public boolean isSearchOpened() {
            return false;
        }

        public boolean isExpanded() {
            return false;
        }
    }

    public PhotoViewerCaptionEnterView(Context context, SizeNotifierFrameLayoutPhoto parent, View window) {
        super(context);
        setWillNotDraw(false);
        setBackgroundColor(Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.windowView = window;
        this.sizeNotifierLayout = parent;
        LinearLayout textFieldContainer = new LinearLayout(context);
        textFieldContainer.setOrientation(0);
        addView(textFieldContainer, LayoutHelper.createFrame(-1, -2.0f, 51, 2.0f, 0.0f, 0.0f, 0.0f));
        FrameLayout frameLayout = new FrameLayout(context);
        textFieldContainer.addView(frameLayout, LayoutHelper.createLinear(0, -2, 1.0f));
        this.emojiButton = new ImageView(context);
        this.emojiButton.setImageResource(R.drawable.ic_smile_w);
        this.emojiButton.setScaleType(ScaleType.CENTER_INSIDE);
        this.emojiButton.setPadding(AndroidUtilities.m9dp(4.0f), AndroidUtilities.m9dp(1.0f), 0, 0);
        frameLayout.addView(this.emojiButton, LayoutHelper.createFrame(48, 48, 83));
        this.emojiButton.setOnClickListener(new PhotoViewerCaptionEnterView$$Lambda$0(this));
        this.lengthTextPaint = new TextPaint(1);
        this.lengthTextPaint.setTextSize((float) AndroidUtilities.m9dp(13.0f));
        this.lengthTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.lengthTextPaint.setColor(-2500135);
        this.messageEditText = new EditTextCaption(context) {
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                try {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                } catch (Throwable e) {
                    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.m9dp(51.0f));
                    FileLog.m13e(e);
                }
            }

            protected void onSelectionChanged(int selStart, int selEnd) {
                super.onSelectionChanged(selStart, selEnd);
                if (selStart != selEnd) {
                    fixHandleView(false);
                } else {
                    fixHandleView(true);
                }
            }
        };
        if (VERSION.SDK_INT >= 23 && this.windowView != null) {
            this.messageEditText.setCustomSelectionActionModeCallback(new CLASSNAME());
            this.messageEditText.setCustomInsertionActionModeCallback(new CLASSNAME());
        }
        this.messageEditText.setHint(LocaleController.getString("AddCaption", R.string.AddCaption));
        this.messageEditText.setImeOptions(CLASSNAMEC.ENCODING_PCM_MU_LAW);
        this.messageEditText.setInputType(this.messageEditText.getInputType() | MessagesController.UPDATE_MASK_CHAT_ADMINS);
        this.messageEditText.setMaxLines(4);
        this.messageEditText.setHorizontallyScrolling(false);
        this.messageEditText.setTextSize(1, 18.0f);
        this.messageEditText.setGravity(80);
        this.messageEditText.setPadding(0, AndroidUtilities.m9dp(11.0f), 0, AndroidUtilities.m9dp(12.0f));
        this.messageEditText.setBackgroundDrawable(null);
        this.messageEditText.setCursorColor(-1);
        this.messageEditText.setCursorSize(AndroidUtilities.m9dp(20.0f));
        this.messageEditText.setTextColor(-1);
        this.messageEditText.setHintTextColor(-NUM);
        this.messageEditText.setFilters(new InputFilter[]{new LengthFilter(this.captionMaxLength)});
        frameLayout.addView(this.messageEditText, LayoutHelper.createFrame(-1, -2.0f, 83, 52.0f, 0.0f, 6.0f, 0.0f));
        this.messageEditText.setOnKeyListener(new PhotoViewerCaptionEnterView$$Lambda$1(this));
        this.messageEditText.setOnClickListener(new PhotoViewerCaptionEnterView$$Lambda$2(this));
        this.messageEditText.addTextChangedListener(new CLASSNAME());
        ImageView doneButton = new ImageView(context);
        doneButton.setScaleType(ScaleType.CENTER);
        doneButton.setImageResource(R.drawable.ic_done);
        textFieldContainer.addView(doneButton, LayoutHelper.createLinear(48, 48, 80));
        if (VERSION.SDK_INT >= 21) {
            doneButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR));
        }
        doneButton.setOnClickListener(new PhotoViewerCaptionEnterView$$Lambda$3(this));
    }

    final /* synthetic */ void lambda$new$0$PhotoViewerCaptionEnterView(View view) {
        if (isPopupShowing()) {
            openKeyboardInternal();
        } else {
            showPopup(1);
        }
    }

    final /* synthetic */ boolean lambda$new$1$PhotoViewerCaptionEnterView(View view, int i, KeyEvent keyEvent) {
        if (i == 4) {
            if (this.windowView != null && hideActionMode()) {
                return true;
            }
            if (!this.keyboardVisible && isPopupShowing()) {
                if (keyEvent.getAction() != 1) {
                    return true;
                }
                showPopup(0);
                return true;
            }
        }
        return false;
    }

    final /* synthetic */ void lambda$new$2$PhotoViewerCaptionEnterView(View view) {
        if (isPopupShowing()) {
            showPopup(AndroidUtilities.usingHardwareInput ? 0 : 2);
        }
    }

    final /* synthetic */ void lambda$new$3$PhotoViewerCaptionEnterView(View view) {
        this.delegate.onCaptionEnter();
    }

    protected void onDraw(Canvas canvas) {
        if (this.lengthText == null || getMeasuredHeight() <= AndroidUtilities.m9dp(48.0f)) {
            this.lengthTextPaint.setAlpha(0);
            this.animationProgress = 0.0f;
            return;
        }
        canvas.drawText(this.lengthText, (float) ((AndroidUtilities.m9dp(56.0f) - ((int) Math.ceil((double) this.lengthTextPaint.measureText(this.lengthText)))) / 2), (float) (getMeasuredHeight() - AndroidUtilities.m9dp(48.0f)), this.lengthTextPaint);
        if (this.animationProgress < 1.0f) {
            this.animationProgress += 0.14166667f;
            invalidate();
            if (this.animationProgress >= 1.0f) {
                this.animationProgress = 1.0f;
            }
            this.lengthTextPaint.setAlpha((int) (255.0f * this.animationProgress));
        }
    }

    public void setForceFloatingEmoji(boolean value) {
        this.forceFloatingEmoji = value;
    }

    public boolean hideActionMode() {
        if (VERSION.SDK_INT < 23 || this.currentActionMode == null) {
            return false;
        }
        try {
            this.currentActionMode.finish();
        } catch (Throwable e) {
            FileLog.m13e(e);
        }
        this.currentActionMode = null;
        return true;
    }

    @SuppressLint({"PrivateApi"})
    private void fixActionMode(ActionMode mode) {
        try {
            Class classActionMode = Class.forName("com.android.internal.view.FloatingActionMode");
            Field fieldToolbar = classActionMode.getDeclaredField("mFloatingToolbar");
            fieldToolbar.setAccessible(true);
            Object toolbar = fieldToolbar.get(mode);
            Class classToolbar = Class.forName("com.android.internal.widget.FloatingToolbar");
            Field fieldToolbarPopup = classToolbar.getDeclaredField("mPopup");
            Field fieldToolbarWidth = classToolbar.getDeclaredField("mWidthChanged");
            fieldToolbarPopup.setAccessible(true);
            fieldToolbarWidth.setAccessible(true);
            Object popup = fieldToolbarPopup.get(toolbar);
            Field fieldToolbarPopupParent = Class.forName("com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup").getDeclaredField("mParent");
            fieldToolbarPopupParent.setAccessible(true);
            if (((View) fieldToolbarPopupParent.get(popup)) != this.windowView) {
                fieldToolbarPopupParent.set(popup, this.windowView);
                Method method = classActionMode.getDeclaredMethod("updateViewLocationInWindow", new Class[0]);
                method.setAccessible(true);
                method.invoke(mode, new Object[0]);
            }
        } catch (Throwable e) {
            FileLog.m13e(e);
        }
    }

    private void onWindowSizeChanged() {
        int size = this.sizeNotifierLayout.getHeight();
        if (!this.keyboardVisible) {
            size -= this.emojiPadding;
        }
        if (this.delegate != null) {
            this.delegate.onWindowSizeChanged(size);
        }
    }

    public void onCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoad);
        this.sizeNotifierLayout.setDelegate(this);
    }

    public void onDestroy() {
        hidePopup();
        if (isKeyboardVisible()) {
            closeKeyboard();
        }
        this.keyboardVisible = false;
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoad);
        if (this.sizeNotifierLayout != null) {
            this.sizeNotifierLayout.setDelegate(null);
        }
    }

    public void setDelegate(PhotoViewerCaptionEnterViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void setFieldText(CharSequence text) {
        if (this.messageEditText != null) {
            this.messageEditText.setText(text);
            this.messageEditText.setSelection(this.messageEditText.getText().length());
            if (this.delegate != null) {
                this.delegate.onTextChanged(this.messageEditText.getText());
            }
            int old = this.captionMaxLength;
            this.captionMaxLength = MessagesController.getInstance(UserConfig.selectedAccount).maxCaptionLength;
            if (old != this.captionMaxLength) {
                this.messageEditText.setFilters(new InputFilter[]{new LengthFilter(this.captionMaxLength)});
            }
        }
    }

    public int getSelectionLength() {
        int i = 0;
        if (this.messageEditText == null) {
            return i;
        }
        try {
            return this.messageEditText.getSelectionEnd() - this.messageEditText.getSelectionStart();
        } catch (Throwable e) {
            FileLog.m13e(e);
            return i;
        }
    }

    public int getCursorPosition() {
        if (this.messageEditText == null) {
            return 0;
        }
        return this.messageEditText.getSelectionStart();
    }

    private void createEmojiView() {
        if (this.emojiView == null) {
            this.emojiView = new EmojiView(false, false, getContext(), null);
            this.emojiView.setListener(new CLASSNAME());
            this.sizeNotifierLayout.addView(this.emojiView);
        }
    }

    public void addEmojiToRecent(String code) {
        createEmojiView();
        this.emojiView.addEmojiToRecent(code);
    }

    public void replaceWithText(int start, int len, CharSequence text, boolean parseEmoji) {
        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(this.messageEditText.getText());
            builder.replace(start, start + len, text);
            if (parseEmoji) {
                Emoji.replaceEmoji(builder, this.messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.m9dp(20.0f), false);
            }
            this.messageEditText.setText(builder);
            if (text.length() + start <= this.messageEditText.length()) {
                this.messageEditText.setSelection(text.length() + start);
            } else {
                this.messageEditText.setSelection(this.messageEditText.length());
            }
        } catch (Throwable e) {
            FileLog.m13e(e);
        }
    }

    public void setFieldFocused(boolean focus) {
        if (this.messageEditText != null) {
            if (focus) {
                if (!this.messageEditText.isFocused()) {
                    this.messageEditText.postDelayed(new PhotoViewerCaptionEnterView$$Lambda$4(this), 600);
                }
            } else if (this.messageEditText.isFocused() && !this.keyboardVisible) {
                this.messageEditText.clearFocus();
            }
        }
    }

    final /* synthetic */ void lambda$setFieldFocused$4$PhotoViewerCaptionEnterView() {
        if (this.messageEditText != null) {
            try {
                this.messageEditText.requestFocus();
            } catch (Throwable e) {
                FileLog.m13e(e);
            }
        }
    }

    public CharSequence getFieldCharSequence() {
        return this.messageEditText.getText();
    }

    public int getEmojiPadding() {
        return this.emojiPadding;
    }

    public boolean isPopupView(View view) {
        return view == this.emojiView;
    }

    private void showPopup(int show) {
        if (show == 1) {
            if (this.emojiView == null) {
                createEmojiView();
            }
            this.emojiView.setVisibility(0);
            if (this.keyboardHeight <= 0) {
                this.keyboardHeight = MessagesController.getGlobalEmojiSettings().getInt("kbd_height", AndroidUtilities.m9dp(200.0f));
            }
            if (this.keyboardHeightLand <= 0) {
                this.keyboardHeightLand = MessagesController.getGlobalEmojiSettings().getInt("kbd_height_land3", AndroidUtilities.m9dp(200.0f));
            }
            int currentHeight = AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? this.keyboardHeightLand : this.keyboardHeight;
            LayoutParams layoutParams = (LayoutParams) this.emojiView.getLayoutParams();
            layoutParams.width = AndroidUtilities.displaySize.x;
            layoutParams.height = currentHeight;
            this.emojiView.setLayoutParams(layoutParams);
            if (!(AndroidUtilities.isInMultiwindow || this.forceFloatingEmoji)) {
                AndroidUtilities.hideKeyboard(this.messageEditText);
            }
            if (this.sizeNotifierLayout != null) {
                this.emojiPadding = currentHeight;
                this.sizeNotifierLayout.requestLayout();
                this.emojiButton.setImageResource(R.drawable.ic_keyboard_w);
                onWindowSizeChanged();
                return;
            }
            return;
        }
        if (this.emojiButton != null) {
            this.emojiButton.setImageResource(R.drawable.ic_smile_w);
        }
        if (this.emojiView != null) {
            this.emojiView.setVisibility(8);
        }
        if (this.sizeNotifierLayout != null) {
            if (show == 0) {
                this.emojiPadding = 0;
            }
            this.sizeNotifierLayout.requestLayout();
            onWindowSizeChanged();
        }
    }

    public void hidePopup() {
        if (isPopupShowing()) {
            showPopup(0);
        }
    }

    private void openKeyboardInternal() {
        showPopup(AndroidUtilities.usingHardwareInput ? 0 : 2);
        openKeyboard();
    }

    public void openKeyboard() {
        int currentSelection;
        try {
            currentSelection = this.messageEditText.getSelectionStart();
        } catch (Throwable e) {
            currentSelection = this.messageEditText.length();
            FileLog.m13e(e);
        }
        MotionEvent event = MotionEvent.obtain(0, 0, 0, 0.0f, 0.0f, 0);
        this.messageEditText.onTouchEvent(event);
        event.recycle();
        event = MotionEvent.obtain(0, 0, 1, 0.0f, 0.0f, 0);
        this.messageEditText.onTouchEvent(event);
        event.recycle();
        AndroidUtilities.showKeyboard(this.messageEditText);
        try {
            this.messageEditText.setSelection(currentSelection);
        } catch (Throwable e2) {
            FileLog.m13e(e2);
        }
    }

    public boolean isPopupShowing() {
        return this.emojiView != null && this.emojiView.getVisibility() == 0;
    }

    public void closeKeyboard() {
        AndroidUtilities.hideKeyboard(this.messageEditText);
    }

    public boolean isKeyboardVisible() {
        return (AndroidUtilities.usingHardwareInput && getTag() != null) || this.keyboardVisible;
    }

    public void onSizeChanged(int height, boolean isWidthGreater) {
        if (height > AndroidUtilities.m9dp(50.0f) && this.keyboardVisible && !AndroidUtilities.isInMultiwindow && !this.forceFloatingEmoji) {
            if (isWidthGreater) {
                this.keyboardHeightLand = height;
                MessagesController.getGlobalEmojiSettings().edit().putInt("kbd_height_land3", this.keyboardHeightLand).commit();
            } else {
                this.keyboardHeight = height;
                MessagesController.getGlobalEmojiSettings().edit().putInt("kbd_height", this.keyboardHeight).commit();
            }
        }
        if (isPopupShowing()) {
            int newHeight;
            if (isWidthGreater) {
                newHeight = this.keyboardHeightLand;
            } else {
                newHeight = this.keyboardHeight;
            }
            LayoutParams layoutParams = (LayoutParams) this.emojiView.getLayoutParams();
            if (!(layoutParams.width == AndroidUtilities.displaySize.x && layoutParams.height == newHeight)) {
                layoutParams.width = AndroidUtilities.displaySize.x;
                layoutParams.height = newHeight;
                this.emojiView.setLayoutParams(layoutParams);
                if (this.sizeNotifierLayout != null) {
                    this.emojiPadding = layoutParams.height;
                    this.sizeNotifierLayout.requestLayout();
                    onWindowSizeChanged();
                }
            }
        }
        if (this.lastSizeChangeValue1 == height && this.lastSizeChangeValue2 == isWidthGreater) {
            onWindowSizeChanged();
            return;
        }
        this.lastSizeChangeValue1 = height;
        this.lastSizeChangeValue2 = isWidthGreater;
        boolean oldValue = this.keyboardVisible;
        this.keyboardVisible = height > 0;
        if (this.keyboardVisible && isPopupShowing()) {
            showPopup(0);
        }
        if (!(this.emojiPadding == 0 || this.keyboardVisible || this.keyboardVisible == oldValue || isPopupShowing())) {
            this.emojiPadding = 0;
            this.sizeNotifierLayout.requestLayout();
        }
        onWindowSizeChanged();
    }

    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiDidLoad && this.emojiView != null) {
            this.emojiView.invalidateViews();
        }
    }
}
