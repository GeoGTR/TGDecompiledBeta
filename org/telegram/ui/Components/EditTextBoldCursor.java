package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.SystemClock;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.beta.R;
import org.telegram.messenger.volley.DefaultRetryPolicy;
import org.telegram.tgnet.ConnectionsManager;

public class EditTextBoldCursor extends EditText {
    private static Method getVerticalOffsetMethod;
    private static Field mCursorDrawableField;
    private static Field mCursorDrawableResField;
    private static Field mEditor;
    private static Field mScrollYField;
    private static Field mShowCursorField;
    private boolean allowDrawCursor = true;
    private int cursorSize;
    private Object editor;
    private GradientDrawable gradientDrawable;
    private float hintAlpha = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
    private int hintColor;
    private StaticLayout hintLayout;
    private boolean hintVisible = true;
    private int ignoreBottomCount;
    private int ignoreTopCount;
    private long lastUpdateTime;
    private float lineSpacingExtra;
    private Drawable[] mCursorDrawable;
    private Rect rect = new Rect();
    private int scrollY;

    public EditTextBoldCursor(Context context) {
        super(context);
        if (mCursorDrawableField == null) {
            try {
                mScrollYField = View.class.getDeclaredField("mScrollY");
                mScrollYField.setAccessible(true);
                mCursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableResField.setAccessible(true);
                mEditor = TextView.class.getDeclaredField("mEditor");
                mEditor.setAccessible(true);
                Class editorClass = Class.forName("android.widget.Editor");
                mShowCursorField = editorClass.getDeclaredField("mShowCursor");
                mShowCursorField.setAccessible(true);
                mCursorDrawableField = editorClass.getDeclaredField("mCursorDrawable");
                mCursorDrawableField.setAccessible(true);
                getVerticalOffsetMethod = TextView.class.getDeclaredMethod("getVerticalOffset", new Class[]{Boolean.TYPE});
                getVerticalOffsetMethod.setAccessible(true);
            } catch (Throwable th) {
            }
        }
        try {
            this.gradientDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, new int[]{-11230757, -11230757});
            this.editor = mEditor.get(this);
            this.mCursorDrawable = (Drawable[]) mCursorDrawableField.get(this.editor);
            mCursorDrawableResField.set(this, Integer.valueOf(R.drawable.field_carret_empty));
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
        this.cursorSize = AndroidUtilities.dp(24.0f);
    }

    public void setAllowDrawCursor(boolean value) {
        this.allowDrawCursor = value;
    }

    public void setCursorColor(int color) {
        this.gradientDrawable.setColor(color);
    }

    public void setCursorSize(int value) {
        this.cursorSize = value;
    }

    public void setHintVisible(boolean value) {
        if (this.hintVisible != value) {
            this.lastUpdateTime = System.currentTimeMillis();
            this.hintVisible = value;
            invalidate();
        }
    }

    public void setHintColor(int value) {
        this.hintColor = value;
    }

    public void setHintText(String value) {
        this.hintLayout = new StaticLayout(value, getPaint(), AndroidUtilities.dp(1000.0f), Alignment.ALIGN_NORMAL, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f, false);
    }

    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        this.lineSpacingExtra = add;
    }

    public int getExtendedPaddingTop() {
        if (this.ignoreTopCount == 0) {
            return super.getExtendedPaddingTop();
        }
        this.ignoreTopCount--;
        return 0;
    }

    public int getExtendedPaddingBottom() {
        if (this.ignoreBottomCount == 0) {
            return super.getExtendedPaddingBottom();
        }
        this.ignoreBottomCount--;
        return this.scrollY != ConnectionsManager.DEFAULT_DATACENTER_ID ? -this.scrollY : 0;
    }

    protected void onDraw(Canvas canvas) {
        int topPadding = getExtendedPaddingTop();
        this.scrollY = ConnectionsManager.DEFAULT_DATACENTER_ID;
        try {
            this.scrollY = mScrollYField.getInt(this);
            mScrollYField.set(this, Integer.valueOf(0));
        } catch (Exception e) {
        }
        this.ignoreTopCount = 1;
        this.ignoreBottomCount = 1;
        canvas.save();
        canvas.translate(0.0f, (float) topPadding);
        try {
            super.onDraw(canvas);
        } catch (Exception e2) {
        }
        if (this.scrollY != Integer.MAX_VALUE) {
            try {
                mScrollYField.set(this, Integer.valueOf(this.scrollY));
            } catch (Exception e3) {
            }
        }
        canvas.restore();
        if (length() == 0 && this.hintLayout != null && (this.hintVisible || this.hintAlpha != 0.0f)) {
            if ((this.hintVisible && this.hintAlpha != DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) || !(this.hintVisible || this.hintAlpha == 0.0f)) {
                long newTime = System.currentTimeMillis();
                long dt = newTime - this.lastUpdateTime;
                if (dt < 0 || dt > 17) {
                    dt = 17;
                }
                this.lastUpdateTime = newTime;
                if (this.hintVisible) {
                    this.hintAlpha += ((float) dt) / 150.0f;
                    if (this.hintAlpha > DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                        this.hintAlpha = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
                    }
                } else {
                    this.hintAlpha -= ((float) dt) / 150.0f;
                    if (this.hintAlpha < 0.0f) {
                        this.hintAlpha = 0.0f;
                    }
                }
                invalidate();
            }
            int oldColor = getPaint().getColor();
            getPaint().setColor(this.hintColor);
            getPaint().setAlpha((int) (255.0f * this.hintAlpha));
            canvas.save();
            canvas.translate(0.0f, ((float) (getMeasuredHeight() - this.hintLayout.getHeight())) / 2.0f);
            this.hintLayout.draw(canvas);
            getPaint().setColor(oldColor);
            canvas.restore();
        }
        try {
            if (this.allowDrawCursor && mShowCursorField != null && this.mCursorDrawable != null && this.mCursorDrawable[0] != null) {
                if ((SystemClock.uptimeMillis() - mShowCursorField.getLong(this.editor)) % 1000 < 500) {
                    canvas.save();
                    int voffsetCursor = 0;
                    if ((getGravity() & 112) != 48) {
                        voffsetCursor = ((Integer) getVerticalOffsetMethod.invoke(this, new Object[]{Boolean.valueOf(true)})).intValue();
                    }
                    canvas.translate((float) getPaddingLeft(), (float) (getExtendedPaddingTop() + voffsetCursor));
                    Layout layout = getLayout();
                    int line = layout.getLineForOffset(getSelectionStart());
                    int lineCount = layout.getLineCount();
                    Rect bounds = this.mCursorDrawable[0].getBounds();
                    this.rect.left = bounds.left;
                    this.rect.right = bounds.left + AndroidUtilities.dp(2.0f);
                    this.rect.bottom = bounds.bottom;
                    this.rect.top = bounds.top;
                    if (this.lineSpacingExtra != 0.0f && line < lineCount - 1) {
                        Rect rect = this.rect;
                        rect.bottom = (int) (((float) rect.bottom) - this.lineSpacingExtra);
                    }
                    this.rect.top = this.rect.centerY() - (this.cursorSize / 2);
                    this.rect.bottom = this.rect.top + this.cursorSize;
                    this.gradientDrawable.setBounds(this.rect);
                    this.gradientDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        } catch (Throwable th) {
        }
    }
}
