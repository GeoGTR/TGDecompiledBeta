package org.telegram.messenger;

import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_error;

final /* synthetic */ class MessagesController$$Lambda$29 implements RequestDelegate {
    private final MessagesController arg$1;
    private final int arg$2;
    private final BaseFragment arg$3;
    private final TL_channels_editBanned arg$4;
    private final boolean arg$5;

    MessagesController$$Lambda$29(MessagesController messagesController, int i, BaseFragment baseFragment, TL_channels_editBanned tL_channels_editBanned, boolean z) {
        this.arg$1 = messagesController;
        this.arg$2 = i;
        this.arg$3 = baseFragment;
        this.arg$4 = tL_channels_editBanned;
        this.arg$5 = z;
    }

    public void run(TLObject tLObject, TL_error tL_error) {
        this.arg$1.lambda$setUserBannedRole$40$MessagesController(this.arg$2, this.arg$3, this.arg$4, this.arg$5, tLObject, tL_error);
    }
}
