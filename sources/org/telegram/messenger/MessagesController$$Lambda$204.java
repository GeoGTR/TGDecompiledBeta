package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.Updates;

final /* synthetic */ class MessagesController$$Lambda$204 implements Runnable {
    private final MessagesController arg$1;
    private final Updates arg$2;

    MessagesController$$Lambda$204(MessagesController messagesController, Updates updates) {
        this.arg$1 = messagesController;
        this.arg$2 = updates;
    }

    public void run() {
        this.arg$1.lambda$null$155$MessagesController(this.arg$2);
    }
}
