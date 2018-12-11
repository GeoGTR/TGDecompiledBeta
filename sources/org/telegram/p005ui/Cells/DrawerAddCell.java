package org.telegram.p005ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CLASSNAMER;
import org.telegram.messenger.LocaleController;
import org.telegram.p005ui.ActionBar.Theme;
import org.telegram.p005ui.Components.LayoutHelper;

/* renamed from: org.telegram.ui.Cells.DrawerAddCell */
public class DrawerAddCell extends FrameLayout {
    private TextView textView;

    public DrawerAddCell(Context context) {
        super(context);
        this.textView = new TextView(context);
        this.textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        this.textView.setTextSize(1, 15.0f);
        this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.textView.setLines(1);
        this.textView.setMaxLines(1);
        this.textView.setSingleLine(true);
        this.textView.setGravity(19);
        this.textView.setCompoundDrawablePadding(AndroidUtilities.m10dp(34.0f));
        addView(this.textView, LayoutHelper.createFrame(-1, -1.0f, 51, 23.0f, 0.0f, 16.0f, 0.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), NUM), MeasureSpec.makeMeasureSpec(AndroidUtilities.m10dp(48.0f), NUM));
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
        this.textView.setText(LocaleController.getString("AddAccount", CLASSNAMER.string.AddAccount));
        Drawable drawable = getResources().getDrawable(CLASSNAMER.drawable.account_add);
        if (drawable != null) {
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), Mode.MULTIPLY));
        }
        this.textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }
}
