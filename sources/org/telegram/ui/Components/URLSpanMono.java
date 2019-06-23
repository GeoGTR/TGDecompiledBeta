package org.telegram.ui.Components;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.TextStyleSpan.TextStyleRun;

public class URLSpanMono extends MetricAffectingSpan {
    private int currentEnd;
    private CharSequence currentMessage;
    private int currentStart;
    private byte currentType;
    private TextStyleRun style;

    public URLSpanMono(CharSequence charSequence, int i, int i2, byte b) {
        this(charSequence, i, i2, b, null);
    }

    public URLSpanMono(CharSequence charSequence, int i, int i2, byte b, TextStyleRun textStyleRun) {
        this.currentMessage = charSequence;
        this.currentStart = i;
        this.currentEnd = i2;
        this.currentType = b;
        this.style = textStyleRun;
    }

    public void copyToClipboard() {
        AndroidUtilities.addToClipboard(this.currentMessage.subSequence(this.currentStart, this.currentEnd).toString());
    }

    public void updateMeasureState(TextPaint textPaint) {
        textPaint.setTextSize((float) AndroidUtilities.dp((float) (SharedConfig.fontSize - 1)));
        textPaint.setFlags(textPaint.getFlags() | 128);
        TextStyleRun textStyleRun = this.style;
        if (textStyleRun != null) {
            textStyleRun.applyStyle(textPaint);
        } else {
            textPaint.setTypeface(Typeface.MONOSPACE);
        }
    }

    public void updateDrawState(TextPaint textPaint) {
        textPaint.setTextSize((float) AndroidUtilities.dp((float) (SharedConfig.fontSize - 1)));
        byte b = this.currentType;
        if (b == (byte) 2) {
            textPaint.setColor(-1);
        } else if (b == (byte) 1) {
            textPaint.setColor(Theme.getColor("chat_messageTextOut"));
        } else {
            textPaint.setColor(Theme.getColor("chat_messageTextIn"));
        }
        TextStyleRun textStyleRun = this.style;
        if (textStyleRun != null) {
            textStyleRun.applyStyle(textPaint);
            return;
        }
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setUnderlineText(false);
    }
}
