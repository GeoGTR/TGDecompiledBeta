package org.telegram.messenger;

import org.telegram.p005ui.ActionBar.BaseFragment;
import org.telegram.tgnet.TLRPC.TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC.TL_error;

final /* synthetic */ class MessagesController$$Lambda$195 implements Runnable {
    private final MessagesController arg$1;
    private final TL_error arg$2;
    private final BaseFragment arg$3;
    private final TL_channels_inviteToChannel arg$4;

    MessagesController$$Lambda$195(MessagesController messagesController, TL_error tL_error, BaseFragment baseFragment, TL_channels_inviteToChannel tL_channels_inviteToChannel) {
        this.arg$1 = messagesController;
        this.arg$2 = tL_error;
        this.arg$3 = baseFragment;
        this.arg$4 = tL_channels_inviteToChannel;
    }

    public void run() {
        this.arg$1.lambda$null$149$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}
