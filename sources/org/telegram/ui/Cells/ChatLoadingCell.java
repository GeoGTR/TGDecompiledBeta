package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class ChatLoadingCell extends FrameLayout {
    private FrameLayout frameLayout;
    private RadialProgressView progressBar;

    public ChatLoadingCell(Context context) {
        super(context);
        this.frameLayout = new FrameLayout(context);
        this.frameLayout.setBackgroundResource(NUM);
        this.frameLayout.getBackground().setColorFilter(Theme.colorFilter);
        addView(this.frameLayout, LayoutHelper.createFrame(36, 36, 17));
        this.progressBar = new RadialProgressView(context);
        this.progressBar.setSize(AndroidUtilities.dp(28.0f));
        this.progressBar.setProgressColor(Theme.getColor("chat_serviceText"));
        this.frameLayout.addView(this.progressBar, LayoutHelper.createFrame(32, 32, 17));
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(i), NUM), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(44.0f), NUM));
    }

    public void setProgressVisible(boolean z) {
        this.frameLayout.setVisibility(z ? 0 : 4);
    }
}
