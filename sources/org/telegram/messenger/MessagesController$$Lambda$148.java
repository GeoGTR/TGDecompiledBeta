package org.telegram.messenger;

import org.telegram.p005ui.ActionBar.AlertDialog;
import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;

final /* synthetic */ class MessagesController$$Lambda$148 implements RequestDelegate {
    private final MessagesController arg$1;
    private final AlertDialog[] arg$2;
    private final BaseFragment arg$3;
    private final int arg$4;

    MessagesController$$Lambda$148(MessagesController messagesController, AlertDialog[] alertDialogArr, BaseFragment baseFragment, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = alertDialogArr;
        this.arg$3 = baseFragment;
        this.arg$4 = i;
    }

    public void run(TLObject tLObject, TL_error tL_error) {
        this.arg$1.lambda$openByUserName$244$MessagesController(this.arg$2, this.arg$3, this.arg$4, tLObject, tL_error);
    }
}
