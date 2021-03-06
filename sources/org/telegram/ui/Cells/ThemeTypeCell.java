package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class ThemeTypeCell extends FrameLayout {
    private ImageView checkImage;
    private boolean needDivider;
    private TextView textView;

    public ThemeTypeCell(Context context) {
        super(context);
        setWillNotDraw(false);
        TextView textView2 = new TextView(context);
        this.textView = textView2;
        textView2.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.textView.setTextSize(1, 16.0f);
        this.textView.setLines(1);
        this.textView.setMaxLines(1);
        this.textView.setSingleLine(true);
        this.textView.setEllipsize(TextUtils.TruncateAt.END);
        int i = 5;
        this.textView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        TextView textView3 = this.textView;
        boolean z = LocaleController.isRTL;
        addView(textView3, LayoutHelper.createFrame(-1, -1.0f, (z ? 5 : 3) | 48, z ? 71.0f : 21.0f, 0.0f, z ? 21.0f : 23.0f, 0.0f));
        ImageView imageView = new ImageView(context);
        this.checkImage = imageView;
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
        this.checkImage.setImageResource(NUM);
        addView(this.checkImage, LayoutHelper.createFrame(19, 14.0f, (LocaleController.isRTL ? 3 : i) | 16, 23.0f, 0.0f, 23.0f, 0.0f));
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i), NUM), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50.0f) + (this.needDivider ? 1 : 0), NUM));
    }

    public void setValue(String str, boolean z, boolean z2) {
        this.textView.setText(str);
        this.checkImage.setVisibility(z ? 0 : 4);
        this.needDivider = z2;
    }

    public void setTypeChecked(boolean z) {
        this.checkImage.setVisibility(z ? 0 : 4);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0.0f : (float) AndroidUtilities.dp(20.0f), (float) (getMeasuredHeight() - 1), (float) (getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20.0f) : 0)), (float) (getMeasuredHeight() - 1), Theme.dividerPaint);
        }
    }
}
