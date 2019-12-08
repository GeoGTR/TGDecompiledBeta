package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class LocationCell extends FrameLayout {
    private TextView addressTextView;
    private ShapeDrawable circleDrawable;
    private BackupImageView imageView;
    private TextView nameTextView;
    private boolean needDivider;
    private boolean wrapContent;

    public LocationCell(Context context, boolean z) {
        super(context);
        this.wrapContent = z;
        this.imageView = new BackupImageView(context);
        BackupImageView backupImageView = this.imageView;
        ShapeDrawable createCircleDrawable = Theme.createCircleDrawable(AndroidUtilities.dp(42.0f), -1);
        this.circleDrawable = createCircleDrawable;
        backupImageView.setBackground(createCircleDrawable);
        this.imageView.setSize(AndroidUtilities.dp(30.0f), AndroidUtilities.dp(30.0f));
        int i = 5;
        addView(this.imageView, LayoutHelper.createFrame(42, 42.0f, (LocaleController.isRTL ? 5 : 3) | 48, LocaleController.isRTL ? 0.0f : 15.0f, 11.0f, LocaleController.isRTL ? 15.0f : 0.0f, 0.0f));
        this.nameTextView = new TextView(context);
        this.nameTextView.setTextSize(1, 16.0f);
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setEllipsize(TruncateAt.END);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.nameTextView.setGravity(LocaleController.isRTL ? 5 : 3);
        int i2 = 16;
        addView(this.nameTextView, LayoutHelper.createFrame(-2, -2.0f, (LocaleController.isRTL ? 5 : 3) | 48, (float) (LocaleController.isRTL ? 16 : 73), 10.0f, (float) (LocaleController.isRTL ? 73 : 16), 0.0f));
        this.addressTextView = new TextView(context);
        this.addressTextView.setTextSize(1, 14.0f);
        this.addressTextView.setMaxLines(1);
        this.addressTextView.setEllipsize(TruncateAt.END);
        this.addressTextView.setSingleLine(true);
        this.addressTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
        this.addressTextView.setGravity(LocaleController.isRTL ? 5 : 3);
        TextView textView = this.addressTextView;
        if (!LocaleController.isRTL) {
            i = 3;
        }
        int i3 = i | 48;
        float f = (float) (LocaleController.isRTL ? 16 : 73);
        if (LocaleController.isRTL) {
            i2 = 73;
        }
        addView(textView, LayoutHelper.createFrame(-2, -2.0f, i3, f, 35.0f, (float) i2, 0.0f));
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        if (this.wrapContent) {
            super.onMeasure(i, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0f) + this.needDivider, NUM));
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(i), NUM), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0f) + this.needDivider, NUM));
        }
    }

    public BackupImageView getImageView() {
        return this.imageView;
    }

    public void setLocation(TL_messageMediaVenue tL_messageMediaVenue, String str, int i, boolean z) {
        setLocation(tL_messageMediaVenue, str, null, i, z);
    }

    public static int getColorForIndex(int i) {
        i %= 7;
        if (i == 0) {
            return -1351584;
        }
        if (i == 1) {
            return -868277;
        }
        if (i == 2) {
            return -12214795;
        }
        if (i == 3) {
            return -13187226;
        }
        if (i != 4) {
            return i != 5 ? -1285237 : -12338729;
        } else {
            return -7900675;
        }
    }

    public void setLocation(TL_messageMediaVenue tL_messageMediaVenue, String str, String str2, int i, boolean z) {
        this.needDivider = z;
        this.circleDrawable.getPaint().setColor(getColorForIndex(i));
        this.nameTextView.setText(tL_messageMediaVenue.title);
        if (str2 != null) {
            this.addressTextView.setText(str2);
        } else {
            this.addressTextView.setText(tL_messageMediaVenue.address);
        }
        this.imageView.setImage(str, null, null);
        setWillNotDraw(z ^ 1);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        if (this.needDivider) {
            canvas.drawLine((float) AndroidUtilities.dp(72.0f), (float) (getHeight() - 1), (float) getWidth(), (float) (getHeight() - 1), Theme.dividerPaint);
        }
    }
}
