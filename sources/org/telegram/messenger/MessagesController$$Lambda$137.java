package org.telegram.messenger;

import java.util.ArrayList;

final /* synthetic */ class MessagesController$$Lambda$137 implements Runnable {
    private final MessagesController arg$1;
    private final ArrayList arg$2;
    private final ArrayList arg$3;

    MessagesController$$Lambda$137(MessagesController messagesController, ArrayList arrayList, ArrayList arrayList2) {
        this.arg$1 = messagesController;
        this.arg$2 = arrayList;
        this.arg$3 = arrayList2;
    }

    public void run() {
        this.arg$1.lambda$processUpdateArray$225$MessagesController(this.arg$2, this.arg$3);
    }
}
