package org.telegram.messenger;

import org.telegram.tgnet.TLObject;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$5oH6yJStIOcRh20JIGlIl-XiXB8 implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ TLObject f$1;
    private final /* synthetic */ String f$2;

    public /* synthetic */ -$$Lambda$MessagesStorage$5oH6yJStIOcRh20JIGlIl-XiXB8(MessagesStorage messagesStorage, TLObject tLObject, String str) {
        this.f$0 = messagesStorage;
        this.f$1 = tLObject;
        this.f$2 = str;
    }

    public final void run() {
        this.f$0.lambda$saveBotCache$69$MessagesStorage(this.f$1, this.f$2);
    }
}
