package org.telegram.messenger;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ActionBar.BaseFragment;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesController$QEj10fA0SyCIQdMlFZT2Ntp8yZg implements Runnable {
    private final /* synthetic */ MessagesController f$0;
    private final /* synthetic */ TL_error f$1;
    private final /* synthetic */ BaseFragment f$2;
    private final /* synthetic */ TLObject f$3;
    private final /* synthetic */ boolean f$4;
    private final /* synthetic */ boolean f$5;

    public /* synthetic */ -$$Lambda$MessagesController$QEj10fA0SyCIQdMlFZT2Ntp8yZg(MessagesController messagesController, TL_error tL_error, BaseFragment baseFragment, TLObject tLObject, boolean z, boolean z2) {
        this.f$0 = messagesController;
        this.f$1 = tL_error;
        this.f$2 = baseFragment;
        this.f$3 = tLObject;
        this.f$4 = z;
        this.f$5 = z2;
    }

    public final void run() {
        this.f$0.lambda$null$178$MessagesController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
    }
}
