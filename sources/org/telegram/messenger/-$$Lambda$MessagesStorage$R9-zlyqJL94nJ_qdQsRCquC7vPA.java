package org.telegram.messenger;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$R9-zlyqJL94nJ_qdQsRCquC7vPA implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ int f$1;
    private final /* synthetic */ int f$2;
    private final /* synthetic */ int f$3;
    private final /* synthetic */ int f$4;
    private final /* synthetic */ int f$5;

    public /* synthetic */ -$$Lambda$MessagesStorage$R9-zlyqJL94nJ_qdQsRCquC7vPA(MessagesStorage messagesStorage, int i, int i2, int i3, int i4, int i5) {
        this.f$0 = messagesStorage;
        this.f$1 = i;
        this.f$2 = i2;
        this.f$3 = i3;
        this.f$4 = i4;
        this.f$5 = i5;
    }

    public final void run() {
        this.f$0.lambda$updateChatInfo$80$MessagesStorage(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
    }
}
