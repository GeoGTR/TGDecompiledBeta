package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.TL_dialog;

final /* synthetic */ class MessagesController$$Lambda$210 implements Runnable {
    private final MessagesController arg$1;
    private final TL_dialog arg$2;

    MessagesController$$Lambda$210(MessagesController messagesController, TL_dialog tL_dialog) {
        this.arg$1 = messagesController;
        this.arg$2 = tL_dialog;
    }

    public void run() {
        this.arg$1.lambda$null$130$MessagesController(this.arg$2);
    }
}
