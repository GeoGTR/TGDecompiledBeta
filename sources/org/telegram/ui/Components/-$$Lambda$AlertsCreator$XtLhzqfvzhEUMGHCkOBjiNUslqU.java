package org.telegram.ui.Components;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AlertsCreator$XtLhzqfvzhEUMGHCkOBjiNUslqU implements OnClickListener {
    private final /* synthetic */ long f$0;
    private final /* synthetic */ Runnable f$1;

    public /* synthetic */ -$$Lambda$AlertsCreator$XtLhzqfvzhEUMGHCkOBjiNUslqU(long j, Runnable runnable) {
        this.f$0 = j;
        this.f$1 = runnable;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        AlertsCreator.lambda$createColorSelectDialog$25(this.f$0, this.f$1, dialogInterface, i);
    }
}
