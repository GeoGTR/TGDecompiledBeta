package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.Message;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SendMessagesHelper$imJ61LkDqCKC2zsI4rTSXfDp6Z0 implements Runnable {
    private final /* synthetic */ SendMessagesHelper f$0;
    private final /* synthetic */ Message f$1;
    private final /* synthetic */ long f$2;
    private final /* synthetic */ int f$3;
    private final /* synthetic */ Message f$4;
    private final /* synthetic */ int f$5;

    public /* synthetic */ -$$Lambda$SendMessagesHelper$imJ61LkDqCKC2zsI4rTSXfDp6Z0(SendMessagesHelper sendMessagesHelper, Message message, long j, int i, Message message2, int i2) {
        this.f$0 = sendMessagesHelper;
        this.f$1 = message;
        this.f$2 = j;
        this.f$3 = i;
        this.f$4 = message2;
        this.f$5 = i2;
    }

    public final void run() {
        this.f$0.lambda$null$5$SendMessagesHelper(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
    }
}
