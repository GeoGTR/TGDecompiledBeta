package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.messages_Dialogs;

final /* synthetic */ class MessagesController$$Lambda$209 implements Runnable {
    private final MessagesController arg$1;
    private final messages_Dialogs arg$2;
    private final boolean arg$3;
    private final int arg$4;

    MessagesController$$Lambda$209(MessagesController messagesController, messages_Dialogs messages_dialogs, boolean z, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = messages_dialogs;
        this.arg$3 = z;
        this.arg$4 = i;
    }

    public void run() {
        this.arg$1.lambda$null$111$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}
