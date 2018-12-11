package org.telegram.p005ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CLASSNAMER;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Components.LayoutHelper;

@SuppressLint({"NewApi"})
/* renamed from: org.telegram.ui.Cells.PhotoAttachCameraCell */
public class PhotoAttachCameraCell extends FrameLayout {
    private ImageView imageView;

    public PhotoAttachCameraCell(Context context) {
        super(context);
        this.imageView = new ImageView(context);
        this.imageView.setScaleType(ScaleType.CENTER);
        this.imageView.setImageResource(CLASSNAMER.drawable.instant_camera);
        this.imageView.setBackgroundColor(Theme.ACTION_BAR_VIDEO_EDIT_COLOR);
        addView(this.imageView, LayoutHelper.createFrame(80, 80.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.m10dp(86.0f), NUM), MeasureSpec.makeMeasureSpec(AndroidUtilities.m10dp(80.0f), NUM));
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogCameraIcon), Mode.MULTIPLY));
    }
}
