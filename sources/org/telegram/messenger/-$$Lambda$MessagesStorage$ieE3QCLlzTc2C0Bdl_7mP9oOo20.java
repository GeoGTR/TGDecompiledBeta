package org.telegram.messenger;

import java.util.concurrent.CountDownLatch;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MessagesStorage$ieE3QCLlzTc2C0Bdl_7mP9oOo20 implements Runnable {
    private final /* synthetic */ MessagesStorage f$0;
    private final /* synthetic */ long f$1;
    private final /* synthetic */ int f$2;
    private final /* synthetic */ boolean[] f$3;
    private final /* synthetic */ CountDownLatch f$4;

    public /* synthetic */ -$$Lambda$MessagesStorage$ieE3QCLlzTc2C0Bdl_7mP9oOo20(MessagesStorage messagesStorage, long j, int i, boolean[] zArr, CountDownLatch countDownLatch) {
        this.f$0 = messagesStorage;
        this.f$1 = j;
        this.f$2 = i;
        this.f$3 = zArr;
        this.f$4 = countDownLatch;
    }

    public final void run() {
        this.f$0.lambda$checkMessageId$92$MessagesStorage(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
