package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ActionBar.AlertDialog;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$LaunchActivity$Zfo5ch07dnPuGi7d59vdlVijeac implements RequestDelegate {
    private final /* synthetic */ LaunchActivity f$0;
    private final /* synthetic */ int f$1;
    private final /* synthetic */ AlertDialog f$2;

    public /* synthetic */ -$$Lambda$LaunchActivity$Zfo5ch07dnPuGi7d59vdlVijeac(LaunchActivity launchActivity, int i, AlertDialog alertDialog) {
        this.f$0 = launchActivity;
        this.f$1 = i;
        this.f$2 = alertDialog;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$runLinkRequest$17$LaunchActivity(this.f$1, this.f$2, tLObject, tL_error);
    }
}
