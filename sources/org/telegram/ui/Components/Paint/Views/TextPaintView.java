package org.telegram.ui.Components.Paint.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Paint.Swatch;
import org.telegram.ui.Components.Paint.Views.EntityView.SelectionView;
import org.telegram.ui.Components.Point;
import org.telegram.ui.Components.Rect;

public class TextPaintView extends EntityView {
    private int baseFontSize;
    private EditTextOutline editText;
    private boolean stroke;
    private Swatch swatch;

    /* renamed from: org.telegram.ui.Components.Paint.Views.TextPaintView$1 */
    class C12171 implements TextWatcher {
        private int beforeCursorPosition = 0;
        private String text;

        C12171() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            this.text = s.toString();
            this.beforeCursorPosition = start;
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            TextPaintView.this.editText.removeTextChangedListener(this);
            if (TextPaintView.this.editText.getLineCount() > 9) {
                TextPaintView.this.editText.setText(this.text);
                TextPaintView.this.editText.setSelection(this.beforeCursorPosition);
            }
            TextPaintView.this.editText.addTextChangedListener(this);
        }
    }

    public class TextViewSelectionView extends SelectionView {
        public TextViewSelectionView(Context context) {
            super(context);
        }

        protected int pointInsideHandle(float x, float y) {
            float radius = (float) AndroidUtilities.dp(19.5f);
            float inset = radius + ((float) AndroidUtilities.dp(1.0f));
            float width = ((float) getWidth()) - (inset * 2.0f);
            float height = ((float) getHeight()) - (inset * 2.0f);
            float middle = (height / 2.0f) + inset;
            if (x > inset - radius && y > middle - radius && x < inset + radius && y < middle + radius) {
                return 1;
            }
            if (x > (inset + width) - radius && y > middle - radius && x < (inset + width) + radius && y < middle + radius) {
                return 2;
            }
            if (x <= inset || x >= width || y <= inset || y >= height) {
                return 0;
            }
            return 3;
        }

        protected void onDraw(Canvas canvas) {
            int xCount;
            TextViewSelectionView textViewSelectionView = this;
            Canvas canvas2 = canvas;
            super.onDraw(canvas);
            float space = (float) AndroidUtilities.dp(3.0f);
            float length = (float) AndroidUtilities.dp(3.0f);
            float thickness = (float) AndroidUtilities.dp(1.0f);
            float radius = (float) AndroidUtilities.dp(4.5f);
            float inset = (radius + thickness) + ((float) AndroidUtilities.dp(15.0f));
            float width = ((float) getWidth()) - (inset * 2.0f);
            float height = ((float) getHeight()) - (inset * 2.0f);
            int xCount2 = (int) Math.floor((double) (width / (space + length)));
            float xGap = (float) Math.ceil((double) (((width - (((float) xCount2) * (space + length))) + space) / 2.0f));
            int i = 0;
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= xCount2) {
                    break;
                }
                float x = (xGap + inset) + (((float) i3) * (length + space));
                float f = x + length;
                float f2 = inset + (thickness / 2.0f);
                int i4 = i3;
                float f3 = f;
                f = xGap;
                xGap = f2;
                xCount = xCount2;
                canvas2.drawRect(x, inset - (thickness / 2.0f), f3, xGap, textViewSelectionView.paint);
                canvas2.drawRect(x, (inset + height) - (thickness / 2.0f), x + length, (inset + height) + (thickness / 2.0f), textViewSelectionView.paint);
                i2 = i4 + 1;
                xGap = f;
                xCount2 = xCount;
            }
            xCount = xCount2;
            xCount2 = (int) Math.floor((double) (height / (space + length)));
            xGap = (float) Math.ceil((double) (((height - (((float) xCount2) * (space + length))) + space) / 2.0f));
            while (true) {
                i3 = i;
                int yCount;
                if (i3 < xCount2) {
                    float y = (xGap + inset) + (((float) i3) * (length + space));
                    x = inset + (thickness / 2.0f);
                    float f4 = y + length;
                    int i5 = i3;
                    f3 = x;
                    x = xGap;
                    xGap = f4;
                    yCount = xCount2;
                    canvas2.drawRect(inset - (thickness / 2.0f), y, f3, xGap, textViewSelectionView.paint);
                    canvas2.drawRect((inset + width) - (thickness / 2.0f), y, (inset + width) + (thickness / 2.0f), y + length, textViewSelectionView.paint);
                    i = i5 + 1;
                    xGap = x;
                    xCount2 = yCount;
                } else {
                    yCount = xCount2;
                    canvas2.drawCircle(inset, (height / 2.0f) + inset, radius, textViewSelectionView.dotPaint);
                    canvas2.drawCircle(inset, (height / 2.0f) + inset, radius, textViewSelectionView.dotStrokePaint);
                    canvas2.drawCircle(inset + width, (height / 2.0f) + inset, radius, textViewSelectionView.dotPaint);
                    canvas2.drawCircle(inset + width, (height / 2.0f) + inset, radius, textViewSelectionView.dotStrokePaint);
                    return;
                }
            }
        }
    }

    public TextPaintView(Context context, Point position, int fontSize, String text, Swatch swatch, boolean stroke) {
        super(context, position);
        this.baseFontSize = fontSize;
        this.editText = new EditTextOutline(context);
        this.editText.setBackgroundColor(0);
        this.editText.setPadding(AndroidUtilities.dp(7.0f), AndroidUtilities.dp(7.0f), AndroidUtilities.dp(7.0f), AndroidUtilities.dp(7.0f));
        this.editText.setClickable(false);
        this.editText.setEnabled(false);
        this.editText.setTextSize(0, (float) this.baseFontSize);
        this.editText.setText(text);
        this.editText.setTextColor(swatch.color);
        this.editText.setTypeface(null, 1);
        this.editText.setGravity(17);
        this.editText.setHorizontallyScrolling(false);
        this.editText.setImeOptions(268435456);
        this.editText.setFocusableInTouchMode(true);
        this.editText.setInputType(this.editText.getInputType() | MessagesController.UPDATE_MASK_CHAT_ADMINS);
        addView(this.editText, LayoutHelper.createFrame(-2, -2, 51));
        if (VERSION.SDK_INT >= 23) {
            this.editText.setBreakStrategy(0);
        }
        setSwatch(swatch);
        setStroke(stroke);
        updatePosition();
        this.editText.addTextChangedListener(new C12171());
    }

    public TextPaintView(Context context, TextPaintView textPaintView, Point position) {
        this(context, position, textPaintView.baseFontSize, textPaintView.getText(), textPaintView.getSwatch(), textPaintView.stroke);
        setRotation(textPaintView.getRotation());
        setScale(textPaintView.getScale());
    }

    public void setMaxWidth(int maxWidth) {
        this.editText.setMaxWidth(maxWidth);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updatePosition();
    }

    public String getText() {
        return this.editText.getText().toString();
    }

    public void setText(String text) {
        this.editText.setText(text);
    }

    public View getFocusedView() {
        return this.editText;
    }

    public void beginEditing() {
        this.editText.setEnabled(true);
        this.editText.setClickable(true);
        this.editText.requestFocus();
        this.editText.setSelection(this.editText.getText().length());
    }

    public void endEditing() {
        this.editText.clearFocus();
        this.editText.setEnabled(false);
        this.editText.setClickable(false);
        updateSelectionView();
    }

    public Swatch getSwatch() {
        return this.swatch;
    }

    public void setSwatch(Swatch swatch) {
        this.swatch = swatch;
        updateColor();
    }

    public void setStroke(boolean stroke) {
        this.stroke = stroke;
        updateColor();
    }

    private void updateColor() {
        if (this.stroke) {
            this.editText.setTextColor(-1);
            this.editText.setStrokeColor(this.swatch.color);
            this.editText.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            return;
        }
        this.editText.setTextColor(this.swatch.color);
        this.editText.setStrokeColor(0);
        this.editText.setShadowLayer(8.0f, 0.0f, 2.0f, -NUM);
    }

    protected Rect getSelectionBounds() {
        float scale = ((ViewGroup) getParent()).getScaleX();
        float width = (((float) getWidth()) * getScale()) + (((float) AndroidUtilities.dp(46.0f)) / scale);
        float height = (((float) getHeight()) * getScale()) + (((float) AndroidUtilities.dp(20.0f)) / scale);
        return new Rect((this.position.f24x - (width / 2.0f)) * scale, (this.position.f25y - (height / 2.0f)) * scale, width * scale, height * scale);
    }

    protected TextViewSelectionView createSelectionView() {
        return new TextViewSelectionView(getContext());
    }
}
