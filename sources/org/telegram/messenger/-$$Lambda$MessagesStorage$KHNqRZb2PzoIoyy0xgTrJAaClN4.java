package org.telegram.messenger;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$KHNqRZb2PzoIoyy0xgTrJAaClN4 implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ long f$1;
    private final /* synthetic */ boolean f$2;

    public /* synthetic */ -$$Lambda$MessagesStorage$KHNqRZb2PzoIoyy0xgTrJAaClN4(MessagesStorage messagesStorage, long j, boolean z) {
        this.f$0 = messagesStorage;
        this.f$1 = j;
        this.f$2 = z;
    }

    public final void run() {
        this.f$0.lambda$setDialogUnread$141$MessagesStorage(this.f$1, this.f$2);
    }
}
