package org.telegram.p005ui;

import android.view.View;
import android.view.View.OnClickListener;
import org.telegram.p005ui.SettingsActivity.CLASSNAME;

/* renamed from: org.telegram.ui.SettingsActivity$5$$Lambda$4 */
final /* synthetic */ class SettingsActivity$5$$Lambda$4 implements OnClickListener {
    private final boolean[] arg$1;

    SettingsActivity$5$$Lambda$4(boolean[] zArr) {
        this.arg$1 = zArr;
    }

    public void onClick(View view) {
        CLASSNAME.lambda$onItemClick$4$SettingsActivity$5(this.arg$1, view);
    }
}
