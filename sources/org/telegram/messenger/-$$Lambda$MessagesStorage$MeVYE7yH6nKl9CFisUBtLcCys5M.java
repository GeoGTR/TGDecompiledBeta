package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.TL_poll;
import org.telegram.tgnet.TLRPC.TL_pollResults;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$MeVYE7yH6nKl9CFisUBtLcCys5M implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ long f$1;
    private final /* synthetic */ TL_poll f$2;
    private final /* synthetic */ TL_pollResults f$3;

    public /* synthetic */ -$$Lambda$MessagesStorage$MeVYE7yH6nKl9CFisUBtLcCys5M(MessagesStorage messagesStorage, long j, TL_poll tL_poll, TL_pollResults tL_pollResults) {
        this.f$0 = messagesStorage;
        this.f$1 = j;
        this.f$2 = tL_poll;
        this.f$3 = tL_pollResults;
    }

    public final void run() {
        this.f$0.lambda$updateMessagePollResults$54$MessagesStorage(this.f$1, this.f$2, this.f$3);
    }
}
