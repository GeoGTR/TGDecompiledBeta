package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ActionMode.Callback2;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import androidx.annotation.Keep;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.FloatingActionMode;
import org.telegram.ui.ActionBar.FloatingToolbar;

public class EditTextBoldCursor extends EditText {
    private static Class editorClass;
    private static Method getVerticalOffsetMethod;
    private static Field mEditor;
    private static Field mScrollYField;
    private static Field mShowCursorField;
    private int activeLineColor;
    private boolean allowDrawCursor = true;
    private boolean currentDrawHintAsHeader;
    private int cursorSize;
    private float cursorWidth = 2.0f;
    private Object editor;
    private StaticLayout errorLayout;
    private int errorLineColor;
    private TextPaint errorPaint;
    private CharSequence errorText;
    private boolean fixed;
    private FloatingActionMode floatingActionMode;
    private FloatingToolbar floatingToolbar;
    private OnPreDrawListener floatingToolbarPreDrawListener;
    private GradientDrawable gradientDrawable;
    private float headerAnimationProgress;
    private int headerHintColor;
    private AnimatorSet headerTransformAnimation;
    private float hintAlpha = 1.0f;
    private int hintColor;
    private StaticLayout hintLayout;
    private boolean hintVisible = true;
    private int ignoreBottomCount;
    private int ignoreTopCount;
    private long lastUpdateTime;
    private int lineColor;
    private Paint linePaint;
    private float lineSpacingExtra;
    private float lineY;
    private OnPreDrawListener listenerFixer;
    private Drawable mCursorDrawable;
    private Rect mTempRect;
    private boolean nextSetTextAnimated;
    private Rect rect = new Rect();
    private int scrollY;
    private boolean supportRtlHint;
    private boolean transformHintToHeader;
    private View windowView;

    @TargetApi(23)
    private class ActionModeCallback2Wrapper extends Callback2 {
        private final Callback mWrapped;

        public ActionModeCallback2Wrapper(Callback callback) {
            this.mWrapped = callback;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onCreateActionMode(actionMode, menu);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(actionMode, menu);
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrapped.onActionItemClicked(actionMode, menuItem);
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrapped.onDestroyActionMode(actionMode);
            EditTextBoldCursor.this.cleanupFloatingActionModeViews();
            EditTextBoldCursor.this.floatingActionMode = null;
        }

        public void onGetContentRect(ActionMode actionMode, View view, Rect rect) {
            Callback callback = this.mWrapped;
            if (callback instanceof Callback2) {
                ((Callback2) callback).onGetContentRect(actionMode, view, rect);
            } else {
                super.onGetContentRect(actionMode, view, rect);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void extendActionMode(ActionMode actionMode, Menu menu) {
    }

    /* Access modifiers changed, original: protected */
    public int getActionModeStyle() {
        return 1;
    }

    @TargetApi(26)
    public int getAutofillType() {
        return 0;
    }

    public EditTextBoldCursor(Context context) {
        super(context);
        if (VERSION.SDK_INT >= 26) {
            setImportantForAutofill(2);
        }
        init();
    }

    @SuppressLint({"PrivateApi"})
    private void init() {
        String str = "mShowCursor";
        this.linePaint = new Paint();
        this.errorPaint = new TextPaint(1);
        this.errorPaint.setTextSize((float) AndroidUtilities.dp(11.0f));
        if (VERSION.SDK_INT >= 26) {
            setImportantForAutofill(2);
        }
        try {
            if (mScrollYField == null) {
                mScrollYField = View.class.getDeclaredField("mScrollY");
                mScrollYField.setAccessible(true);
            }
        } catch (Throwable unused) {
        }
        try {
            if (editorClass == null) {
                mEditor = TextView.class.getDeclaredField("mEditor");
                mEditor.setAccessible(true);
                editorClass = Class.forName("android.widget.Editor");
                mShowCursorField = editorClass.getDeclaredField(str);
                mShowCursorField.setAccessible(true);
                getVerticalOffsetMethod = TextView.class.getDeclaredMethod("getVerticalOffset", new Class[]{Boolean.TYPE});
                getVerticalOffsetMethod.setAccessible(true);
                mShowCursorField = editorClass.getDeclaredField(str);
                mShowCursorField.setAccessible(true);
            }
        } catch (Throwable th) {
            FileLog.e(th);
        }
        try {
            this.gradientDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, new int[]{-11230757, -11230757});
            if (VERSION.SDK_INT >= 29) {
                setTextCursorDrawable(this.gradientDrawable);
            }
            this.editor = mEditor.get(this);
        } catch (Throwable unused2) {
        }
        this.cursorSize = AndroidUtilities.dp(24.0f);
    }

    @SuppressLint({"PrivateApi"})
    public void fixHandleView(boolean z) {
        if (z) {
            this.fixed = false;
        } else if (!this.fixed) {
            try {
                if (editorClass == null) {
                    editorClass = Class.forName("android.widget.Editor");
                    mEditor = TextView.class.getDeclaredField("mEditor");
                    mEditor.setAccessible(true);
                    this.editor = mEditor.get(this);
                }
                if (this.listenerFixer == null) {
                    Method declaredMethod = editorClass.getDeclaredMethod("getPositionListener", new Class[0]);
                    declaredMethod.setAccessible(true);
                    this.listenerFixer = (OnPreDrawListener) declaredMethod.invoke(this.editor, new Object[0]);
                }
                OnPreDrawListener onPreDrawListener = this.listenerFixer;
                onPreDrawListener.getClass();
                AndroidUtilities.runOnUIThread(new -$$Lambda$qzh_QoBZ7K2XdUWK2VAJcGTe1OY(onPreDrawListener), 500);
            } catch (Throwable unused) {
            }
            this.fixed = true;
        }
    }

    public void setTransformHintToHeader(boolean z) {
        if (this.transformHintToHeader != z) {
            this.transformHintToHeader = z;
            AnimatorSet animatorSet = this.headerTransformAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.headerTransformAnimation = null;
            }
        }
    }

    public void setAllowDrawCursor(boolean z) {
        this.allowDrawCursor = z;
        invalidate();
    }

    public void setCursorWidth(float f) {
        this.cursorWidth = f;
    }

    public void setCursorColor(int i) {
        this.gradientDrawable.setColor(i);
        invalidate();
    }

    public void setCursorSize(int i) {
        this.cursorSize = i;
    }

    public void setErrorLineColor(int i) {
        this.errorLineColor = i;
        this.errorPaint.setColor(this.errorLineColor);
        invalidate();
    }

    public void setLineColors(int i, int i2, int i3) {
        this.lineColor = i;
        this.activeLineColor = i2;
        this.errorLineColor = i3;
        this.errorPaint.setColor(this.errorLineColor);
        invalidate();
    }

    public void setHintVisible(boolean z) {
        if (this.hintVisible != z) {
            this.lastUpdateTime = System.currentTimeMillis();
            this.hintVisible = z;
            invalidate();
        }
    }

    public void setHintColor(int i) {
        this.hintColor = i;
        invalidate();
    }

    public void setHeaderHintColor(int i) {
        this.headerHintColor = i;
        invalidate();
    }

    public void setNextSetTextAnimated(boolean z) {
        this.nextSetTextAnimated = z;
    }

    public void setErrorText(CharSequence charSequence) {
        if (!TextUtils.equals(charSequence, this.errorText)) {
            this.errorText = charSequence;
            requestLayout();
        }
    }

    public boolean requestFocus(int i, Rect rect) {
        return super.requestFocus(i, rect);
    }

    public boolean hasErrorText() {
        return TextUtils.isEmpty(this.errorText) ^ 1;
    }

    public StaticLayout getErrorLayout(int i) {
        if (TextUtils.isEmpty(this.errorText)) {
            return null;
        }
        return new StaticLayout(this.errorText, this.errorPaint, i, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    public float getLineY() {
        return this.lineY;
    }

    public void setSupportRtlHint(boolean z) {
        this.supportRtlHint = z;
    }

    public void setText(CharSequence charSequence, BufferType bufferType) {
        super.setText(charSequence, bufferType);
        checkHeaderVisibility(this.nextSetTextAnimated);
        this.nextSetTextAnimated = false;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.hintLayout != null) {
            this.lineY = ((((float) (getMeasuredHeight() - this.hintLayout.getHeight())) / 2.0f) + ((float) this.hintLayout.getHeight())) + ((float) AndroidUtilities.dp(6.0f));
        }
    }

    public void setHintText(CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }
        this.hintLayout = new StaticLayout(charSequence, getPaint(), AndroidUtilities.dp(1000.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    public Layout getHintLayoutEx() {
        return this.hintLayout;
    }

    /* Access modifiers changed, original: protected */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        checkHeaderVisibility(true);
    }

    private void checkHeaderVisibility(boolean z) {
        boolean z2 = this.transformHintToHeader && (isFocused() || getText().length() > 0);
        if (this.currentDrawHintAsHeader != z2) {
            AnimatorSet animatorSet = this.headerTransformAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.headerTransformAnimation = null;
            }
            this.currentDrawHintAsHeader = z2;
            float f = 1.0f;
            if (z) {
                this.headerTransformAnimation = new AnimatorSet();
                AnimatorSet animatorSet2 = this.headerTransformAnimation;
                Animator[] animatorArr = new Animator[1];
                float[] fArr = new float[1];
                if (!z2) {
                    f = 0.0f;
                }
                fArr[0] = f;
                animatorArr[0] = ObjectAnimator.ofFloat(this, "headerAnimationProgress", fArr);
                animatorSet2.playTogether(animatorArr);
                this.headerTransformAnimation.setDuration(200);
                this.headerTransformAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                this.headerTransformAnimation.start();
            } else {
                if (!z2) {
                    f = 0.0f;
                }
                this.headerAnimationProgress = f;
            }
            invalidate();
        }
    }

    @Keep
    public void setHeaderAnimationProgress(float f) {
        this.headerAnimationProgress = f;
        invalidate();
    }

    @Keep
    public float getHeaderAnimationProgress() {
        return this.headerAnimationProgress;
    }

    public void setLineSpacing(float f, float f2) {
        super.setLineSpacing(f, f2);
        this.lineSpacingExtra = f;
    }

    public int getExtendedPaddingTop() {
        int i = this.ignoreTopCount;
        if (i == 0) {
            return super.getExtendedPaddingTop();
        }
        this.ignoreTopCount = i - 1;
        return 0;
    }

    public int getExtendedPaddingBottom() {
        int i = this.ignoreBottomCount;
        if (i == 0) {
            return super.getExtendedPaddingBottom();
        }
        this.ignoreBottomCount = i - 1;
        i = this.scrollY;
        return i != Integer.MAX_VALUE ? -i : 0;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        int green;
        int alpha;
        Canvas canvas2 = canvas;
        int extendedPaddingTop = getExtendedPaddingTop();
        this.scrollY = Integer.MAX_VALUE;
        try {
            this.scrollY = mScrollYField.getInt(this);
            mScrollYField.set(this, Integer.valueOf(0));
        } catch (Exception unused) {
        }
        this.ignoreTopCount = 1;
        this.ignoreBottomCount = 1;
        canvas.save();
        canvas2.translate(0.0f, (float) extendedPaddingTop);
        try {
            super.onDraw(canvas);
        } catch (Exception unused2) {
        }
        extendedPaddingTop = this.scrollY;
        if (extendedPaddingTop != Integer.MAX_VALUE) {
            try {
                mScrollYField.set(this, Integer.valueOf(extendedPaddingTop));
            } catch (Exception unused3) {
            }
        }
        canvas.restore();
        if ((length() == 0 || this.transformHintToHeader) && this.hintLayout != null && (this.hintVisible || this.hintAlpha != 0.0f)) {
            if ((this.hintVisible && this.hintAlpha != 1.0f) || !(this.hintVisible || this.hintAlpha == 0.0f)) {
                long currentTimeMillis = System.currentTimeMillis();
                long j = currentTimeMillis - this.lastUpdateTime;
                if (j < 0 || j > 17) {
                    j = 17;
                }
                this.lastUpdateTime = currentTimeMillis;
                if (this.hintVisible) {
                    this.hintAlpha += ((float) j) / 150.0f;
                    if (this.hintAlpha > 1.0f) {
                        this.hintAlpha = 1.0f;
                    }
                } else {
                    this.hintAlpha -= ((float) j) / 150.0f;
                    if (this.hintAlpha < 0.0f) {
                        this.hintAlpha = 0.0f;
                    }
                }
                invalidate();
            }
            extendedPaddingTop = getPaint().getColor();
            canvas.save();
            float lineLeft = this.hintLayout.getLineLeft(0);
            float lineWidth = this.hintLayout.getLineWidth(0);
            int i = lineLeft != 0.0f ? (int) (((float) null) - lineLeft) : 0;
            if (this.supportRtlHint && LocaleController.isRTL) {
                canvas2.translate(((float) (i + getScrollX())) + (((float) getMeasuredWidth()) - lineWidth), (this.lineY - ((float) this.hintLayout.getHeight())) - ((float) AndroidUtilities.dp(6.0f)));
            } else {
                canvas2.translate((float) (i + getScrollX()), (this.lineY - ((float) this.hintLayout.getHeight())) - ((float) AndroidUtilities.dp(6.0f)));
            }
            if (this.transformHintToHeader) {
                float f;
                float f2 = 1.0f - (this.headerAnimationProgress * 0.3f);
                float f3 = ((float) (-AndroidUtilities.dp(22.0f))) * this.headerAnimationProgress;
                int red = Color.red(this.headerHintColor);
                int green2 = Color.green(this.headerHintColor);
                int blue = Color.blue(this.headerHintColor);
                int alpha2 = Color.alpha(this.headerHintColor);
                int red2 = Color.red(this.hintColor);
                green = Color.green(this.hintColor);
                int blue2 = Color.blue(this.hintColor);
                alpha = Color.alpha(this.hintColor);
                if (this.supportRtlHint && LocaleController.isRTL) {
                    lineWidth += lineLeft;
                    f = 0.0f;
                    canvas2.translate(lineWidth - (lineWidth * f2), 0.0f);
                } else {
                    f = 0.0f;
                    if (lineLeft != 0.0f) {
                        canvas2.translate(lineLeft * (1.0f - f2), 0.0f);
                    }
                }
                canvas2.scale(f2, f2);
                canvas2.translate(f, f3);
                TextPaint paint = getPaint();
                lineLeft = (float) alpha;
                float f4 = (float) (alpha2 - alpha);
                lineWidth = this.headerAnimationProgress;
                paint.setColor(Color.argb((int) (lineLeft + (f4 * lineWidth)), (int) (((float) red2) + (((float) (red - red2)) * lineWidth)), (int) (((float) green) + (((float) (green2 - green)) * lineWidth)), (int) (((float) blue2) + (((float) (blue - blue2)) * lineWidth))));
            } else {
                getPaint().setColor(this.hintColor);
                getPaint().setAlpha((int) ((this.hintAlpha * 255.0f) * (((float) Color.alpha(this.hintColor)) / 255.0f)));
            }
            this.hintLayout.draw(canvas2);
            getPaint().setColor(extendedPaddingTop);
            canvas.restore();
        }
        try {
            if (this.allowDrawCursor && mShowCursorField != null) {
                Object obj = ((SystemClock.uptimeMillis() - mShowCursorField.getLong(this.editor)) % 1000 >= 500 || !isFocused()) ? null : 1;
                if (obj != null) {
                    Layout layout;
                    Rect bounds;
                    Rect rect;
                    canvas.save();
                    if (getVerticalOffsetMethod != null) {
                        if ((getGravity() & 112) != 48) {
                            green = ((Integer) getVerticalOffsetMethod.invoke(this, new Object[]{Boolean.valueOf(true)})).intValue();
                            canvas2.translate((float) getPaddingLeft(), (float) (getExtendedPaddingTop() + green));
                            layout = getLayout();
                            alpha = layout.getLineForOffset(getSelectionStart());
                            extendedPaddingTop = layout.getLineCount();
                            updateCursorPosition();
                            bounds = this.gradientDrawable.getBounds();
                            this.rect.left = bounds.left;
                            this.rect.right = bounds.left + AndroidUtilities.dp(this.cursorWidth);
                            this.rect.bottom = bounds.bottom;
                            this.rect.top = bounds.top;
                            if (this.lineSpacingExtra != 0.0f && alpha < extendedPaddingTop - 1) {
                                rect = this.rect;
                                rect.bottom = (int) (((float) rect.bottom) - this.lineSpacingExtra);
                            }
                            this.rect.top = this.rect.centerY() - (this.cursorSize / 2);
                            this.rect.bottom = this.rect.top + this.cursorSize;
                            this.gradientDrawable.setBounds(this.rect);
                            this.gradientDrawable.draw(canvas2);
                            canvas.restore();
                        }
                    } else if ((getGravity() & 112) != 48) {
                        green = getTotalPaddingTop() - getExtendedPaddingTop();
                        canvas2.translate((float) getPaddingLeft(), (float) (getExtendedPaddingTop() + green));
                        layout = getLayout();
                        alpha = layout.getLineForOffset(getSelectionStart());
                        extendedPaddingTop = layout.getLineCount();
                        updateCursorPosition();
                        bounds = this.gradientDrawable.getBounds();
                        this.rect.left = bounds.left;
                        this.rect.right = bounds.left + AndroidUtilities.dp(this.cursorWidth);
                        this.rect.bottom = bounds.bottom;
                        this.rect.top = bounds.top;
                        rect = this.rect;
                        rect.bottom = (int) (((float) rect.bottom) - this.lineSpacingExtra);
                        this.rect.top = this.rect.centerY() - (this.cursorSize / 2);
                        this.rect.bottom = this.rect.top + this.cursorSize;
                        this.gradientDrawable.setBounds(this.rect);
                        this.gradientDrawable.draw(canvas2);
                        canvas.restore();
                    }
                    green = 0;
                    canvas2.translate((float) getPaddingLeft(), (float) (getExtendedPaddingTop() + green));
                    layout = getLayout();
                    alpha = layout.getLineForOffset(getSelectionStart());
                    extendedPaddingTop = layout.getLineCount();
                    updateCursorPosition();
                    bounds = this.gradientDrawable.getBounds();
                    this.rect.left = bounds.left;
                    this.rect.right = bounds.left + AndroidUtilities.dp(this.cursorWidth);
                    this.rect.bottom = bounds.bottom;
                    this.rect.top = bounds.top;
                    rect = this.rect;
                    rect.bottom = (int) (((float) rect.bottom) - this.lineSpacingExtra);
                    this.rect.top = this.rect.centerY() - (this.cursorSize / 2);
                    this.rect.bottom = this.rect.top + this.cursorSize;
                    this.gradientDrawable.setBounds(this.rect);
                    this.gradientDrawable.draw(canvas2);
                    canvas.restore();
                }
            }
        } catch (Throwable unused4) {
        }
        if (this.lineColor != 0 && this.hintLayout != null) {
            if (!TextUtils.isEmpty(this.errorText)) {
                this.linePaint.setColor(this.errorLineColor);
                extendedPaddingTop = AndroidUtilities.dp(2.0f);
            } else if (isFocused()) {
                this.linePaint.setColor(this.activeLineColor);
                extendedPaddingTop = AndroidUtilities.dp(2.0f);
            } else {
                this.linePaint.setColor(this.lineColor);
                extendedPaddingTop = AndroidUtilities.dp(1.0f);
            }
            canvas.drawRect((float) getScrollX(), (float) ((int) this.lineY), (float) (getScrollX() + getMeasuredWidth()), this.lineY + ((float) extendedPaddingTop), this.linePaint);
        }
    }

    public void setWindowView(View view) {
        this.windowView = view;
    }

    private boolean updateCursorPosition() {
        Layout layout = getLayout();
        int selectionStart = getSelectionStart();
        int lineForOffset = layout.getLineForOffset(selectionStart);
        updateCursorPosition(layout.getLineTop(lineForOffset), layout.getLineTop(lineForOffset + 1), layout.getPrimaryHorizontal(selectionStart));
        return true;
    }

    private int clampHorizontalPosition(Drawable drawable, float f) {
        f = Math.max(0.5f, f - 0.5f);
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        int i = 0;
        if (drawable != null) {
            drawable.getPadding(this.mTempRect);
            i = drawable.getIntrinsicWidth();
        } else {
            this.mTempRect.setEmpty();
        }
        int scrollX = getScrollX();
        float f2 = f - ((float) scrollX);
        int width = (getWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight();
        float f3 = (float) width;
        if (f2 >= f3 - 1.0f) {
            return (width + scrollX) - (i - this.mTempRect.right);
        }
        int i2;
        if (Math.abs(f2) <= 1.0f || (TextUtils.isEmpty(getText()) && ((float) (1048576 - scrollX)) <= f3 + 1.0f && f <= 1.0f)) {
            i2 = this.mTempRect.left;
        } else {
            scrollX = (int) f;
            i2 = this.mTempRect.left;
        }
        return scrollX - i2;
    }

    private void updateCursorPosition(int i, int i2, float f) {
        int clampHorizontalPosition = clampHorizontalPosition(this.gradientDrawable, f);
        int dp = AndroidUtilities.dp(this.cursorWidth);
        GradientDrawable gradientDrawable = this.gradientDrawable;
        Rect rect = this.mTempRect;
        gradientDrawable.setBounds(clampHorizontalPosition, i - rect.top, dp + clampHorizontalPosition, i2 + rect.bottom);
    }

    public float getLineSpacingExtra() {
        return super.getLineSpacingExtra();
    }

    private void cleanupFloatingActionModeViews() {
        FloatingToolbar floatingToolbar = this.floatingToolbar;
        if (floatingToolbar != null) {
            floatingToolbar.dismiss();
            this.floatingToolbar = null;
        }
        if (this.floatingToolbarPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.floatingToolbarPreDrawListener);
            this.floatingToolbarPreDrawListener = null;
        }
    }

    public ActionMode startActionMode(Callback callback) {
        if (VERSION.SDK_INT < 23 || this.windowView == null) {
            return super.startActionMode(callback);
        }
        FloatingActionMode floatingActionMode = this.floatingActionMode;
        if (floatingActionMode != null) {
            floatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        this.floatingToolbar = new FloatingToolbar(getContext(), this.windowView, getActionModeStyle());
        this.floatingActionMode = new FloatingActionMode(getContext(), new ActionModeCallback2Wrapper(callback), this, this.floatingToolbar);
        this.floatingToolbarPreDrawListener = new -$$Lambda$EditTextBoldCursor$xTYV8NPdhWD9vABtK8op5OnAwKw(this);
        floatingActionMode = this.floatingActionMode;
        callback.onCreateActionMode(floatingActionMode, floatingActionMode.getMenu());
        FloatingActionMode floatingActionMode2 = this.floatingActionMode;
        extendActionMode(floatingActionMode2, floatingActionMode2.getMenu());
        this.floatingActionMode.invalidate();
        getViewTreeObserver().addOnPreDrawListener(this.floatingToolbarPreDrawListener);
        invalidate();
        return this.floatingActionMode;
    }

    public /* synthetic */ boolean lambda$startActionMode$0$EditTextBoldCursor() {
        this.floatingActionMode.updateViewLocationInWindow();
        return true;
    }

    public ActionMode startActionMode(Callback callback, int i) {
        if (VERSION.SDK_INT < 23 || this.windowView == null) {
            return super.startActionMode(callback, i);
        }
        return startActionMode(callback);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName("android.widget.EditText");
        StaticLayout staticLayout = this.hintLayout;
        if (staticLayout != null) {
            accessibilityNodeInfo.setContentDescription(staticLayout.getText());
        }
    }
}
