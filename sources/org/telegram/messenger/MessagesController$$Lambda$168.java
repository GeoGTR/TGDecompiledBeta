package org.telegram.messenger;

import org.telegram.tgnet.TLObject;

final /* synthetic */ class MessagesController$$Lambda$168 implements Runnable {
    private final MessagesController arg$1;
    private final TLObject arg$2;

    MessagesController$$Lambda$168(MessagesController messagesController, TLObject tLObject) {
        this.arg$1 = messagesController;
        this.arg$2 = tLObject;
    }

    public void run() {
        this.arg$1.lambda$null$203$MessagesController(this.arg$2);
    }
}
