package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_authorizationForm;
import org.telegram.tgnet.TLRPC.TL_account_getAuthorizationForm;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ActionBar.AlertDialog;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$LaunchActivity$GofL0vsgJimoLHa3SGxufnpInEA implements RequestDelegate {
    private final /* synthetic */ LaunchActivity f$0;
    private final /* synthetic */ AlertDialog f$1;
    private final /* synthetic */ int f$2;
    private final /* synthetic */ TL_account_authorizationForm f$3;
    private final /* synthetic */ TL_account_getAuthorizationForm f$4;
    private final /* synthetic */ String f$5;
    private final /* synthetic */ String f$6;
    private final /* synthetic */ String f$7;

    public /* synthetic */ -$$Lambda$LaunchActivity$GofL0vsgJimoLHa3SGxufnpInEA(LaunchActivity launchActivity, AlertDialog alertDialog, int i, TL_account_authorizationForm tL_account_authorizationForm, TL_account_getAuthorizationForm tL_account_getAuthorizationForm, String str, String str2, String str3) {
        this.f$0 = launchActivity;
        this.f$1 = alertDialog;
        this.f$2 = i;
        this.f$3 = tL_account_authorizationForm;
        this.f$4 = tL_account_getAuthorizationForm;
        this.f$5 = str;
        this.f$6 = str2;
        this.f$7 = str3;
    }

    public final void run(TLObject tLObject, TL_error tL_error) {
        this.f$0.lambda$null$20$LaunchActivity(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, tLObject, tL_error);
    }
}
